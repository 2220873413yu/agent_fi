<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="90px">
      <el-form-item label="甯傚満slug" prop="marketSlug">
        <el-input v-model="queryParams.marketSlug" placeholder="璇疯緭鍏ュ競鍦簊lug" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="浜嬩欢鏍囬" prop="eventTitle">
        <el-input v-model="queryParams.eventTitle" placeholder="璇疯緭鍏ヤ簨浠舵爣棰? clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="甯傚満闂" prop="marketQuestion">
        <el-input v-model="queryParams.marketQuestion" placeholder="璇疯緭鍏ュ競鍦洪棶棰? clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="鐘舵€? prop="status">
        <el-select v-model="queryParams.status" placeholder="璇烽€夋嫨鐘舵€? clearable>
          <el-option
            v-for="dict in dict.type.t_polymarket_market_status"
            :key="dict.value"
            :label="dict.label"
            :value="parseInt(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">鎼滅储</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">閲嶇疆</el-button>
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
        >瀵煎嚭</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="polymarketMarketList">
      <el-table-column label="甯傚満" align="left" prop="marketQuestion" min-width="260" show-overflow-tooltip />
      <el-table-column label="甯傚満slug" align="center" prop="marketSlug" width="220" show-overflow-tooltip />
      <el-table-column label="鐘舵€? align="center" prop="status" width="110">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_polymarket_market_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="涓嬪崟娆℃暟" align="center" prop="orderCount" width="90" />
      <el-table-column label="鎬籄FI" align="center" prop="totalAfiAmount" width="120" />
      <el-table-column label="鎬绘墜缁垂AFI" align="center" prop="totalFeeAfiAmount" width="130" />
      <el-table-column label="鎬籙SDT" align="center" prop="totalUsdtAmount" width="120" />
      <el-table-column label="鎬讳唤棰? align="center" prop="totalShareAmount" width="120" />
      <el-table-column label="鎬诲厬浠楿SDT绛夊€? align="center" prop="totalPayoutUsdtAmount" width="130" />
      <el-table-column label="鎬诲厬浠楢FI" align="center" prop="totalPayoutAfiAmount" width="120" />
      <el-table-column label="璧㈠" align="center" prop="resultOutcomeName" width="120" />
      <el-table-column label="缁撴潫鏃堕棿" align="center" prop="endTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="涓婃妫€鏌? align="center" prop="lastCheckTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastCheckTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="鎿嶄綔" align="center" class-name="small-padding fixed-width" width="170">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['xms:polymarketMarket:query']"
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleDetail(scope.row)"
          >璇︽儏</el-button>
          <el-button
            v-hasPermi="['xms:polymarketMarket:edit']"
            size="mini"
            type="text"
            icon="el-icon-check"
            :disabled="scope.row.status !== 0"
            @click="handleSettle(scope.row)"
          >缁撶畻</el-button>
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
        <el-form-item label="甯傚満slug">
          <el-input v-model="form.marketSlug" disabled />
        </el-form-item>
        <el-form-item label="甯傚満闂">
          <el-input v-model="form.marketQuestion" disabled />
        </el-form-item>
        <el-form-item label="鐘舵€?>
          <dict-tag :options="dict.type.t_polymarket_market_status" :value="form.status" />
        </el-form-item>
        <el-form-item label="UMA鐘舵€?>
          <el-input v-model="form.umaResolutionStatus" disabled />
        </el-form-item>
        <el-form-item label="璧㈠">
          <el-input v-model="form.resultOutcomeName" disabled />
        </el-form-item>
        <el-form-item label="总手续费AFI">
          <el-input v-model="form.totalFeeAfiAmount" disabled />
        </el-form-item>
        <el-form-item label="鎬诲厬浠楿SDT绛夊€?>
          <el-input v-model="form.totalPayoutUsdtAmount" disabled />
        </el-form-item>
        <el-form-item label="鎬诲厬浠楢FI">
          <el-input v-model="form.totalPayoutAfiAmount" disabled />
        </el-form-item>
        <el-form-item label="澶囨敞">
          <el-input v-model="form.remark" type="textarea" :rows="3" disabled />
        </el-form-item>
        <el-form-item label="甯傚満蹇収">
          <el-input v-model="form.marketSnapshotJson" type="textarea" :rows="8" disabled />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">鍏?闂?/el-button>
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
        this.title = 'Polymarket甯傚満璇︽儏'
        this.open = true
      })
    },
    handleSettle(row) {
      settlePolymarketMarket(row.marketSlug).then(response => {
        this.$modal.msgSuccess(response.data ? '宸茶Е鍙戠粨绠? : '甯傚満鏈繘鍏ョ粨绠?)
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
