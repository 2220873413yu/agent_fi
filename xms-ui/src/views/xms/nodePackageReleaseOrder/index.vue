<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="110px">
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
      <el-form-item label="来源订单号" prop="nodeOrderNo">
        <el-input
          v-model="queryParams.nodeOrderNo"
          placeholder="请输入来源订单号"
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
      <el-form-item label="释放状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择释放状态" clearable>
          <el-option
            v-for="dict in dict.type.t_node_package_release_order_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="初始化批次" prop="initBatchNo">
        <el-input
          v-model="queryParams.initBatchNo"
          placeholder="请输入初始化批次"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最后释放日期" prop="lastReleaseDay">
        <el-input
          v-model="queryParams.lastReleaseDay"
          placeholder="格式yyyyMMdd"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="daterangeCreateTime"
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
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['xms:nodePackageReleaseOrder:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="nodePackageReleaseOrderList">
      <el-table-column label="用户ID" align="center" prop="userId" width="90" />
      <el-table-column label="钱包地址" align="center" prop="address" min-width="180" show-overflow-tooltip />
      <el-table-column label="释放订单号" align="center" prop="releaseNo" min-width="170" show-overflow-tooltip />
      <el-table-column label="来源订单号" align="center" prop="nodeOrderNo" min-width="170" show-overflow-tooltip />
      <el-table-column label="节点等级" align="center" prop="packageLevel" width="90">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_node_plan_node_level" :value="scope.row.packageLevel" />
        </template>
      </el-table-column>
      <el-table-column label="订单权重" align="center" prop="weightMultiplier" width="100" />
      <el-table-column label="总权重" align="center" prop="totalWeight" width="110" />
      <el-table-column label="每权重AFI" align="center" prop="amountPerWeight" width="120" />
      <el-table-column label="总释放AFI" align="center" prop="totalReleaseAmount" width="130" />
      <el-table-column label="每日释放AFI" align="center" prop="dailyReleaseAmount" width="130" />
      <el-table-column label="已释放AFI" align="center" prop="releasedAmount" width="120" />
      <el-table-column label="剩余AFI" align="center" prop="remainingAmount" width="120" />
      <el-table-column label="已运行天数" align="center" prop="runDays" width="100" />
      <el-table-column label="总天数" align="center" prop="totalDays" width="80" />
      <el-table-column label="最后释放日期" align="center" prop="lastReleaseDay" width="120" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_node_package_release_order_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="初始化批次" align="center" prop="initBatchNo" min-width="150" show-overflow-tooltip />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
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
import { listNodePackageReleaseOrder } from '@/api/xms/nodePackageReleaseOrder'

export default {
  name: 'NodePackageReleaseOrder',
  dicts: ['t_node_plan_node_level', 't_node_package_release_order_status'],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      nodePackageReleaseOrderList: [],
      daterangeCreateTime: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: null,
        address: null,
        nodeOrderNo: null,
        packageLevel: null,
        status: null,
        initBatchNo: null,
        lastReleaseDay: null
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
        this.queryParams.beginCreateTime = this.daterangeCreateTime[0] + ' 00:00:00'
        this.queryParams.endCreateTime = this.daterangeCreateTime[1] + ' 23:59:59'
      } else {
        this.queryParams.beginCreateTime = null
        this.queryParams.endCreateTime = null
      }
      listNodePackageReleaseOrder(this.queryParams).then(response => {
        this.nodePackageReleaseOrderList = response.rows
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
      this.download('xms/nodePackageReleaseOrder/export', {
        ...this.queryParams
      }, `nodePackageReleaseOrder_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
