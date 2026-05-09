<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="100px">
      <el-form-item label="质押单号" prop="pledgeNo">
        <el-input v-model="queryParams.pledgeNo" placeholder="请输入质押单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="托管订单号" prop="stakeHostingOrderNo">
        <el-input v-model="queryParams.stakeHostingOrderNo" placeholder="请输入托管订单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="钱包地址" prop="account">
        <el-input v-model="queryParams.account" placeholder="请输入钱包地址" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option
            v-for="dict in dict.type.t_stake_hosting_afi_pledge_status"
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
        <el-button v-hasPermi="['xms:stakeHostingAfiPledge:export']" type="warning" plain icon="el-icon-download" size="mini" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="pledgeList">
      <el-table-column label="质押单号" align="center" prop="pledgeNo" width="180" />
      <el-table-column label="托管订单号" align="center" prop="stakeHostingOrderNo" width="180" />
      <el-table-column label="用户ID" align="center" prop="userId" width="100" />
      <el-table-column label="钱包地址" align="center" prop="account" width="180" />
      <el-table-column label="托管金额" align="center" prop="stakeUsdtAmount" />
      <el-table-column label="AFI数量" align="center" prop="afiAmount" />
      <el-table-column label="AFI价格" align="center" prop="afiPrice" />
      <el-table-column label="等值USDT" align="center" prop="afiUsdtAmount" />
      <el-table-column label="命中比例(%)" align="center" prop="pledgeRatio" />
      <el-table-column label="加速倍率" align="center" prop="accelerateRate" />
      <el-table-column label="生效日期" align="center" prop="effectiveDay" />
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_stake_hosting_afi_pledge_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="质押时间" align="center" prop="pledgeTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.pledgeTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="退还时间" align="center" prop="returnTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.returnTime) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listStakeHostingAfiPledge } from '@/api/xms/stakeHostingAfiPledge'

export default {
  name: 'StakeHostingAfiPledge',
  dicts: ['t_stake_hosting_afi_pledge_status'],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      pledgeList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        pledgeNo: null,
        stakeHostingOrderNo: null,
        userId: null,
        account: null,
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
      listStakeHostingAfiPledge(this.queryParams).then(response => {
        this.pledgeList = response.rows
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
      this.download('xms/stakeHostingAfiPledge/export', {
        ...this.queryParams
      }, `stakeHostingAfiPledge_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
