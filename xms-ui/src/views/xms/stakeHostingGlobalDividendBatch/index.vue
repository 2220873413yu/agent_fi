<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="90px">
      <el-form-item label="批次号" prop="batchNo">
        <el-input v-model="queryParams.batchNo" placeholder="请输入批次号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="结算日" prop="settlementDay">
        <el-input v-model="queryParams.settlementDay" placeholder="yyyyMMdd" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option
            v-for="dict in dict.type.t_stake_hosting_global_dividend_batch_status"
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
          v-hasPermi="['xms:stakeHostingGlobalDividendBatch:export']"
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="batchList">
      <el-table-column label="批次号" align="center" prop="batchNo" width="180" />
      <el-table-column label="结算日" align="center" prop="settlementDay" width="110" />
      <el-table-column label="周期开始时间" align="center" prop="periodStartTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.periodStartTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="周期结束时间" align="center" prop="periodEndTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.periodEndTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="计划分红金额" align="center" prop="planAmount" />
      <el-table-column label="实际分红金额" align="center" prop="actualAmount" />
      <el-table-column label="参与人数" align="center" prop="userCount" />
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_stake_hosting_global_dividend_batch_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" width="180" />
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
import { listStakeHostingGlobalDividendBatch } from '@/api/xms/stakeHostingGlobalDividendBatch'

export default {
  name: 'StakeHostingGlobalDividendBatch',
  dicts: ['t_stake_hosting_global_dividend_batch_status'],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      batchList: [],
      daterangeCreateTime: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        batchNo: null,
        settlementDay: null,
        status: null
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
      listStakeHostingGlobalDividendBatch(this.queryParams).then(response => {
        this.batchList = response.rows
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
      this.download('xms/stakeHostingGlobalDividendBatch/export', {
        ...this.queryParams
      }, `stakeHostingGlobalDividendBatch_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
