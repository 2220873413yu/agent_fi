<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="90px">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="市场slug" prop="marketSlug">
        <el-input v-model="queryParams.marketSlug" placeholder="请输入市场slug" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option
            v-for="dict in dict.type.t_polymarket_order_status"
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
          v-hasPermi="['xms:polymarketOrder:export']"
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:polymarketOrder:edit']"
          type="primary"
          plain
          icon="el-icon-check"
          size="mini"
          @click="handleSettlePending"
        >处理待结算</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="polymarketOrderList">
      <el-table-column label="订单号" align="center" prop="orderNo" width="180" />
      <el-table-column label="用户ID" align="center" prop="userId" width="90" />
      <el-table-column label="钱包地址" align="center" prop="account" width="180" show-overflow-tooltip />
      <el-table-column label="市场" align="left" prop="marketQuestion" min-width="260" show-overflow-tooltip />
      <el-table-column label="选择" align="center" prop="outcomeName" width="90" />
      <el-table-column label="下单AFI" align="center" prop="afiAmount" width="110" />
      <el-table-column label="AFI价格" align="center" prop="afiPrice" width="100" />
      <el-table-column label="等值USDT" align="center" prop="afiUsdtAmount" width="110" />
      <el-table-column label="成交价" align="center" prop="outcomePrice" width="90" />
      <el-table-column label="份额" align="center" prop="shareAmount" width="120" />
      <el-table-column label="兑付USDT" align="center" prop="payoutUsdtAmount" width="110" />
      <el-table-column label="状态" align="center" prop="status" width="110">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_polymarket_order_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="结束时间" align="center" prop="endTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结算时间" align="center" prop="settleTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.settleTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['xms:polymarketOrder:query']"
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleDetail(scope.row)"
          >详情</el-button>
          <el-button
            v-hasPermi="['xms:polymarketOrder:edit']"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleReview(scope.row)"
          >复核</el-button>
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

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" label-width="120px">
        <el-form-item label="订单号">
          <el-input v-model="form.orderNo" disabled />
        </el-form-item>
        <el-form-item label="市场问题">
          <el-input v-model="form.marketQuestion" disabled />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option
              v-for="dict in dict.type.t_polymarket_order_status"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="赢家下标">
          <el-input-number v-model="form.resultOutcomeIndex" :min="0" />
        </el-form-item>
        <el-form-item label="赢家结果">
          <el-input v-model="form.resultOutcomeName" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="下单快照">
          <el-input v-model="form.orderSnapshotJson" type="textarea" :rows="6" disabled />
        </el-form-item>
        <el-form-item label="结算快照">
          <el-input v-model="form.settleSnapshotJson" type="textarea" :rows="6" disabled />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPolymarketOrder, getPolymarketOrder, updatePolymarketOrder, settlePendingPolymarketOrder } from '@/api/xms/polymarketOrder'

export default {
  name: 'PolymarketOrder',
  dicts: ['t_polymarket_order_status'],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      polymarketOrderList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: null,
        userId: null,
        marketSlug: null,
        status: null
      },
      form: {}
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listPolymarketOrder(this.queryParams).then(response => {
        this.polymarketOrderList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.form = {}
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleDetail(row) {
      getPolymarketOrder(row.id).then(response => {
        this.form = response.data
        this.title = 'Polymarket订单详情'
        this.open = true
      })
    },
    handleReview(row) {
      getPolymarketOrder(row.id).then(response => {
        this.form = response.data
        this.title = 'Polymarket订单复核'
        this.open = true
      })
    },
    submitForm() {
      updatePolymarketOrder(this.form).then(() => {
        this.$modal.msgSuccess('修改成功')
        this.open = false
        this.getList()
      })
    },
    handleExport() {
      this.download('xms/polymarketOrder/export', {
        ...this.queryParams
      }, `polymarket_order_${new Date().getTime()}.xlsx`)
    },
    handleSettlePending() {
      settlePendingPolymarketOrder(100).then(response => {
        this.$modal.msgSuccess(`处理完成：${response.data || 0} 条`)
        this.getList()
      })
    }
  }
}
</script>
