<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="110px">
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="钱包地址" prop="account">
        <el-input v-model="queryParams.account" placeholder="请输入钱包地址" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="统计日期" prop="statDay">
        <el-input v-model="queryParams.statDay" placeholder="yyyyMMdd" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="收益率来源" prop="rateSource">
        <el-select v-model="queryParams.rateSource" placeholder="请选择收益率来源" clearable>
          <el-option
            v-for="dict in dict.type.t_stake_hosting_daily_team_performance_rate_source"
            :key="dict.value"
            :label="dict.label"
            :value="parseInt(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="计算状态" prop="calcStatus">
        <el-select v-model="queryParams.calcStatus" placeholder="请选择计算状态" clearable>
          <el-option
            v-for="dict in dict.type.t_stake_hosting_daily_team_performance_calc_status"
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
          v-hasPermi="['xms:stakeHostingDailyTeamPerformance:export']"
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
      <el-table-column label="统计日期" align="center" prop="statDay" width="110" />
      <el-table-column label="团队新增USDT" align="center" prop="teamNewAmount" width="130" />
      <el-table-column label="团队到期USDT" align="center" prop="teamExpiredAmount" width="130" />
      <el-table-column label="昨日团队新增" align="center" prop="previousTeamTvl" width="130" />
      <el-table-column label="今日团队新增" align="center" prop="currentTeamTvl" width="130" />
      <el-table-column label="G_day" align="center" prop="gDay" width="100">
        <template slot-scope="scope">
          <span>{{ formatPercent(scope.row.gDay) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Gsmooth" align="center" prop="gSmooth" width="110">
        <template slot-scope="scope">
          <span>{{ formatPercent(scope.row.gSmooth) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="基础静态收益率" align="center" prop="baseStaticRate" width="140">
        <template slot-scope="scope">
          <span>{{ formatPercent(scope.row.baseStaticRate) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="收益率来源" align="center" prop="rateSource" width="120">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_stake_hosting_daily_team_performance_rate_source" :value="scope.row.rateSource" />
        </template>
      </el-table-column>
      <el-table-column label="计算状态" align="center" prop="calcStatus" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_stake_hosting_daily_team_performance_calc_status" :value="scope.row.calcStatus" />
        </template>
      </el-table-column>
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
import { listStakeHostingDailyTeamPerformance } from '@/api/xms/stakeHostingDailyTeamPerformance'

export default {
  name: 'StakeHostingDailyTeamPerformance',
  dicts: [
    't_stake_hosting_daily_team_performance_rate_source',
    't_stake_hosting_daily_team_performance_calc_status'
  ],
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
        statDay: null,
        rateSource: null,
        calcStatus: null
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listStakeHostingDailyTeamPerformance(this.queryParams).then(response => {
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
      this.download('xms/stakeHostingDailyTeamPerformance/export', {
        ...this.queryParams
      }, `stakeHostingDailyTeamPerformance_${new Date().getTime()}.xlsx`)
    },
    formatPercent(value) {
      if (value === null || value === undefined || value === '') {
        return '-'
      }
      return `${value} %`
    }
  }
}
</script>
