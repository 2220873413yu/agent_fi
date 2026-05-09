<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="90px">
      <el-form-item label="流水单号" prop="logNo">
        <el-input v-model="queryParams.logNo" placeholder="请输入流水单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="流水类型" prop="flowType">
        <el-select v-model="queryParams.flowType" placeholder="请选择流水类型" clearable>
          <el-option
            v-for="dict in dict.type.t_stake_hosting_global_dividend_pool_log_flow_type"
            :key="dict.value"
            :label="dict.label"
            :value="parseInt(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="业务类型" prop="bizType">
        <el-select v-model="queryParams.bizType" placeholder="请选择业务类型" clearable>
          <el-option
            v-for="dict in dict.type.t_stake_hosting_global_dividend_pool_log_biz_type"
            :key="dict.value"
            :label="dict.label"
            :value="parseInt(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="来源订单号" prop="sourceOrderNo">
        <el-input v-model="queryParams.sourceOrderNo" placeholder="请输入来源订单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="来源批次号" prop="sourceBatchNo">
        <el-input v-model="queryParams.sourceBatchNo" placeholder="请输入来源批次号" clearable @keyup.enter.native="handleQuery" />
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
          v-hasPermi="['xms:stakeHostingGlobalDividendPoolLog:export']"
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="poolLogList">
      <el-table-column label="流水单号" align="center" prop="logNo" width="180" />
      <el-table-column label="奖池编码" align="center" prop="poolCode" width="210" />
      <el-table-column label="流水类型" align="center" prop="flowType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_stake_hosting_global_dividend_pool_log_flow_type" :value="scope.row.flowType" />
        </template>
      </el-table-column>
      <el-table-column label="业务类型" align="center" prop="bizType" width="130">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_stake_hosting_global_dividend_pool_log_biz_type" :value="scope.row.bizType" />
        </template>
      </el-table-column>
      <el-table-column label="变动金额" align="center" prop="changeAmount" />
      <el-table-column label="变动前余额" align="center" prop="beforeAmount" />
      <el-table-column label="变动后余额" align="center" prop="afterAmount" />
      <el-table-column label="来源订单号" align="center" prop="sourceOrderNo" width="180" />
      <el-table-column label="来源用户ID" align="center" prop="sourceUserId" width="100" />
      <el-table-column label="来源结算日" align="center" prop="sourceSettlementDay" width="110" />
      <el-table-column label="来源批次号" align="center" prop="sourceBatchNo" width="180" />
      <el-table-column label="备注" align="center" prop="remark" width="180" />
      <el-table-column label="创建人" align="center" prop="createBy" width="100" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listStakeHostingGlobalDividendPoolLog } from '@/api/xms/stakeHostingGlobalDividendPoolLog'

export default {
  name: 'StakeHostingGlobalDividendPoolLog',
  dicts: [
    't_stake_hosting_global_dividend_pool_log_flow_type',
    't_stake_hosting_global_dividend_pool_log_biz_type'
  ],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      poolLogList: [],
      daterangeCreateTime: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        logNo: null,
        flowType: null,
        bizType: null,
        sourceOrderNo: null,
        sourceBatchNo: null
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
      listStakeHostingGlobalDividendPoolLog(this.queryParams).then(response => {
        this.poolLogList = response.rows
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
      this.download('xms/stakeHostingGlobalDividendPoolLog/export', {
        ...this.queryParams
      }, `stakeHostingGlobalDividendPoolLog_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
