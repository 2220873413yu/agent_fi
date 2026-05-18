<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="90px">
      <el-form-item label="市场slug" prop="marketSlug">
        <el-input v-model="queryParams.marketSlug" placeholder="请输入市场slug" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="事件标题" prop="eventTitle">
        <el-input v-model="queryParams.eventTitle" placeholder="请输入事件标题" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="市场问题" prop="marketQuestion">
        <el-input v-model="queryParams.marketQuestion" placeholder="请输入市场问题" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option
            v-for="dict in dict.type.t_polymarket_market_status"
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
          v-hasPermi="['xms:polymarketMarket:export']"
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="polymarketMarketList">
      <el-table-column label="市场" align="left" prop="marketQuestion" min-width="260" show-overflow-tooltip />
      <el-table-column label="市场slug" align="center" prop="marketSlug" width="220" show-overflow-tooltip />
      <el-table-column label="状态" align="center" prop="status" width="110">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_polymarket_market_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="下单次数" align="center" prop="orderCount" width="90" />
      <el-table-column label="总AFI" align="center" prop="totalAfiAmount" width="120" />
      <el-table-column label="总USDT" align="center" prop="totalUsdtAmount" width="120" />
      <el-table-column label="总份额" align="center" prop="totalShareAmount" width="120" />
      <el-table-column label="总兑付" align="center" prop="totalPayoutUsdtAmount" width="120" />
      <el-table-column label="赢家" align="center" prop="resultOutcomeName" width="120" />
      <el-table-column label="结束时间" align="center" prop="endTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="上次检查" align="center" prop="lastCheckTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastCheckTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="170">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['xms:polymarketMarket:query']"
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleDetail(scope.row)"
          >详情</el-button>
          <el-button
            v-hasPermi="['xms:polymarketMarket:edit']"
            size="mini"
            type="text"
            icon="el-icon-check"
            :disabled="scope.row.status !== 0"
            @click="handleSettle(scope.row)"
          >结算</el-button>
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
        <el-form-item label="市场slug">
          <el-input v-model="form.marketSlug" disabled />
        </el-form-item>
        <el-form-item label="市场问题">
          <el-input v-model="form.marketQuestion" disabled />
        </el-form-item>
        <el-form-item label="状态">
          <dict-tag :options="dict.type.t_polymarket_market_status" :value="form.status" />
        </el-form-item>
        <el-form-item label="UMA状态">
          <el-input v-model="form.umaResolutionStatus" disabled />
        </el-form-item>
        <el-form-item label="赢家">
          <el-input v-model="form.resultOutcomeName" disabled />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" disabled />
        </el-form-item>
        <el-form-item label="市场快照">
          <el-input v-model="form.marketSnapshotJson" type="textarea" :rows="8" disabled />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPolymarketMarket, getPolymarketMarket, settlePolymarketMarket } from '@/api/xms/polymarketMarket'

export default {
  name: 'PolymarketMarket',
  dicts: ['t_polymarket_market_status'],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      polymarketMarketList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        marketSlug: null,
        eventTitle: null,
        marketQuestion: null,
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
      listPolymarketMarket(this.queryParams).then(response => {
        this.polymarketMarketList = response.rows
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
      getPolymarketMarket(row.id).then(response => {
        this.form = response.data
        this.title = 'Polymarket市场详情'
        this.open = true
      })
    },
    handleSettle(row) {
      settlePolymarketMarket(row.marketSlug).then(response => {
        this.$modal.msgSuccess(response.data ? '已触发结算' : '市场未进入结算')
        this.getList()
      })
    },
    handleExport() {
      this.download('xms/polymarketMarket/export', {
        ...this.queryParams
      }, `polymarket_market_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
