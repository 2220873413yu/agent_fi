<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="90px">
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="钱包地址" prop="account">
        <el-input v-model="queryParams.account" placeholder="请输入钱包地址" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="周开始" prop="weekStartTime">
        <el-input v-model="queryParams.weekStartTime" placeholder="yyyyMMddHHmmss" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="settleStatus">
        <el-select v-model="queryParams.settleStatus" placeholder="请选择状态" clearable>
          <el-option
            v-for="dict in dict.type.t_stake_hosting_weekly_community_performance_settle_status"
            :key="dict.value"
            :label="dict.label"
            :value="parseInt(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:stakeHostingWeeklyCommunityPerformance:export']"
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="performanceList">
      <el-table-column label="用户ID" align="center" prop="userId" width="110" />
      <el-table-column label="钱包地址" align="center" prop="account" min-width="180" show-overflow-tooltip />
      <el-table-column label="周开始" align="center" prop="weekStartTime" width="150" />
      <el-table-column label="周结束" align="center" prop="weekEndTime" width="150" />
      <el-table-column label="个人新增" align="center" prop="selfNewPerformance" />
      <el-table-column label="团队新增" align="center" prop="teamNewPerformance" />
      <el-table-column label="直推区合计" align="center" prop="totalLinePerformance" />
      <el-table-column label="最大区" align="center" prop="maxLinePerformance" />
      <el-table-column label="新增小区" align="center" prop="communityNewPerformance" />
      <el-table-column label="状态" align="center" prop="settleStatus" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_stake_hosting_weekly_community_performance_settle_status" :value="scope.row.settleStatus" />
        </template>
      </el-table-column>
      <el-table-column label="分红批次" align="center" prop="batchNo" width="180" show-overflow-tooltip />
      <el-table-column label="更新时间" align="center" prop="updateTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listStakeHostingWeeklyCommunityPerformance } from '@/api/xms/stakeHostingWeeklyCommunityPerformance'

export default {
  name: 'StakeHostingWeeklyCommunityPerformance',
  dicts: ['t_stake_hosting_weekly_community_performance_settle_status'],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      performanceList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: null,
        account: null,
        weekStartTime: null,
        settleStatus: null
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listStakeHostingWeeklyCommunityPerformance(this.queryParams).then(response => {
        this.performanceList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleExport() {
      this.download('xms/stakeHostingWeeklyCommunityPerformance/export', {
        ...this.queryParams
      }, `stakeHostingWeeklyCommunityPerformance_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
