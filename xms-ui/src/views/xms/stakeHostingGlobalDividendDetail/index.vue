<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="90px">
      <el-form-item label="批次号" prop="batchNo">
        <el-input v-model="queryParams.batchNo" placeholder="请输入批次号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="钱包地址" prop="account">
        <el-input v-model="queryParams.account" placeholder="请输入钱包地址" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="奖励等级" prop="rewardLevel">
        <el-select v-model="queryParams.rewardLevel" placeholder="请选择奖励等级" clearable>
          <el-option
            v-for="dict in dict.type.t_user_info_game_level"
            :key="dict.value"
            :label="dict.label"
            :value="parseInt(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="daterangeCreateTime"
          style="width: 240px"
          value-format="yyyy-MM-dd HH:mm:ss"
          type="datetimerange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:stakeHostingGlobalDividendDetail:export']"
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="detailList">
      <el-table-column label="批次号" align="center" prop="batchNo" width="180" show-overflow-tooltip />
      <el-table-column label="用户ID" align="center" prop="userId" width="100" />
      <el-table-column label="钱包地址" align="center" prop="account" width="180" show-overflow-tooltip />
      <el-table-column label="奖励等级" align="center" prop="rewardLevel" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_info_game_level" :value="scope.row.rewardLevel" />
        </template>
      </el-table-column>
      <el-table-column label="等级分红比例(%)" align="center" prop="levelDividendRatio" width="130" />
      <el-table-column label="等级奖池金额" align="center" prop="levelPoolAmount" width="120" />
      <el-table-column label="上期小区权重" align="center" prop="previousCommunityWeight" width="130" />
      <el-table-column label="本期小区权重" align="center" prop="communityWeight" width="130" />
      <el-table-column label="本期分红权重" align="center" prop="dividendWeight" width="130" />
      <el-table-column label="等级分红权重" align="center" prop="levelDividendWeight" width="130" />
      <el-table-column label="分红金额" align="center" prop="rewardAmount" width="120" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listStakeHostingGlobalDividendDetail } from '@/api/xms/stakeHostingGlobalDividendDetail'

export default {
  name: 'StakeHostingGlobalDividendDetail',
  dicts: ['t_user_info_game_level'],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      detailList: [],
      daterangeCreateTime: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        batchNo: null,
        userId: null,
        account: null,
        rewardLevel: null
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      this.queryParams.params = {}
      if (this.daterangeCreateTime && this.daterangeCreateTime.length === 2) {
        this.queryParams.params['beginCreateTime'] = this.daterangeCreateTime[0]
        this.queryParams.params['endCreateTime'] = this.daterangeCreateTime[1]
      }
      listStakeHostingGlobalDividendDetail(this.queryParams).then(response => {
        this.detailList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.daterangeCreateTime = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleExport() {
      this.download('xms/stakeHostingGlobalDividendDetail/export', {
        ...this.queryParams
      }, `stakeHostingGlobalDividendDetail_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
