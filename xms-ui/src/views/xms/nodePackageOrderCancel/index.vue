<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="110px">
      <el-form-item label="订单号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="原订单ID" prop="originOrderId">
        <el-input
          v-model="queryParams.originOrderId"
          placeholder="请输入原订单ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入用户ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="钱包地址" prop="address">
        <el-input
          v-model="queryParams.address"
          placeholder="请输入钱包地址"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="节点等级" prop="packageLevel">
        <el-select v-model="queryParams.packageLevel" placeholder="请选择节点等级" clearable>
          <el-option
            v-for="dict in dict.type.t_node_plan_node_level"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="订单来源" prop="sourceType">
        <el-select v-model="queryParams.sourceType" placeholder="请选择订单来源" clearable>
          <el-option
            v-for="dict in dict.type.t_node_package_order_source_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="原订单状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择原订单状态" clearable>
          <el-option
            v-for="dict in dict.type.t_node_package_order_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="取消人" prop="cancelBy">
        <el-input
          v-model="queryParams.cancelBy"
          placeholder="请输入取消人"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="取消时间">
        <el-date-picker
          v-model="daterangeCancelTime"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
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
          v-hasPermi="['xms:nodePackageOrderCancel:export']"
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="nodePackageOrderCancelList">
      <el-table-column label="原订单ID" align="center" prop="originOrderId" width="90" />
      <el-table-column label="订单号" align="center" prop="orderNo" min-width="170" show-overflow-tooltip />
      <el-table-column label="用户ID" align="center" prop="userId" width="90" />
      <el-table-column label="钱包地址" align="center" prop="address" min-width="180" show-overflow-tooltip />
      <el-table-column label="节点等级" align="center" prop="packageLevel" width="90">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_node_plan_node_level" :value="scope.row.packageLevel" />
        </template>
      </el-table-column>
      <el-table-column label="支付金额" align="center" prop="orderValueUsdt" width="110" />
      <el-table-column label="订单来源" align="center" prop="sourceType" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_node_package_order_source_type" :value="scope.row.sourceType" />
        </template>
      </el-table-column>
      <el-table-column label="原状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_node_package_order_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="支付hash" align="center" prop="hash" min-width="180" show-overflow-tooltip />
      <el-table-column label="取消人" align="center" prop="cancelBy" width="110" />
      <el-table-column label="取消时间" align="center" prop="cancelTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.cancelTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="释放订单ID" align="center" prop="releaseOrderId" width="110" />
      <el-table-column label="取消前释放状态" align="center" prop="releaseStatusBefore" width="130">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_node_package_release_order_status" :value="scope.row.releaseStatusBefore" />
        </template>
      </el-table-column>
      <el-table-column label="已释放AFI" align="center" prop="releasedAmountSnapshot" width="120" />
      <el-table-column label="剩余AFI" align="center" prop="remainingAmountSnapshot" width="120" />
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listNodePackageOrderCancel } from '@/api/xms/nodePackageOrderCancel'

export default {
  name: 'NodePackageOrderCancel',
  dicts: ['t_node_plan_node_level', 't_node_package_order_source_type', 't_node_package_order_status', 't_node_package_release_order_status'],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      nodePackageOrderCancelList: [],
      daterangeCancelTime: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        originOrderId: null,
        orderNo: null,
        userId: null,
        address: null,
        packageLevel: null,
        sourceType: null,
        status: null,
        cancelBy: null,
        beginCancelTime: null,
        endCancelTime: null
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      if (this.daterangeCancelTime && this.daterangeCancelTime.length === 2) {
        this.queryParams.beginCancelTime = this.daterangeCancelTime[0] + ' 00:00:00'
        this.queryParams.endCancelTime = this.daterangeCancelTime[1] + ' 23:59:59'
      } else {
        this.queryParams.beginCancelTime = null
        this.queryParams.endCancelTime = null
      }
      listNodePackageOrderCancel(this.queryParams).then(response => {
        this.nodePackageOrderCancelList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.daterangeCancelTime = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleExport() {
      this.download('xms/nodePackageOrderCancel/export', {
        ...this.queryParams
      }, `nodePackageOrderCancel_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
