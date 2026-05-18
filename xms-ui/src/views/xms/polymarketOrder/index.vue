<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="90px">
      <el-form-item label="璁㈠崟鍙? prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="璇疯緭鍏ヨ鍗曞彿" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="鐢ㄦ埛ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="璇疯緭鍏ョ敤鎴稩D" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="甯傚満slug" prop="marketSlug">
        <el-input v-model="queryParams.marketSlug" placeholder="璇疯緭鍏ュ競鍦簊lug" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="涓氬姟绫诲瀷" prop="bizType">
        <el-select v-model="queryParams.bizType" placeholder="璇烽€夋嫨涓氬姟绫诲瀷" clearable>
          <el-option
            v-for="dict in dict.type.t_polymarket_order_biz_type"
            :key="dict.value"
            :label="dict.label"
            :value="parseInt(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="鐘舵€? prop="status">
        <el-select v-model="queryParams.status" placeholder="璇烽€夋嫨鐘舵€? clearable>
          <el-option
            v-for="dict in dict.type.t_polymarket_order_status"
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
          v-hasPermi="['xms:polymarketOrder:export']"
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >瀵煎嚭</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:polymarketOrder:edit']"
          type="primary"
          plain
          icon="el-icon-check"
          size="mini"
          @click="handleSettlePending"
        >澶勭悊寰呯粨绠楀競鍦?/el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="polymarketOrderList">
      <el-table-column label="璁㈠崟鍙? align="center" prop="orderNo" width="180" />
      <el-table-column label="鐢ㄦ埛ID" align="center" prop="userId" width="90" />
      <el-table-column label="閽卞寘鍦板潃" align="center" prop="account" width="180" show-overflow-tooltip />
      <el-table-column label="甯傚満" align="left" prop="marketQuestion" min-width="260" show-overflow-tooltip />
      <el-table-column label="涓氬姟绫诲瀷" align="center" prop="bizType" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_polymarket_order_biz_type" :value="scope.row.bizType" />
        </template>
      </el-table-column>
      <el-table-column label="閫夋嫨" align="center" prop="outcomeName" width="90" />
      <el-table-column label="涓嬪崟AFI" align="center" prop="afiAmount" width="110" />
      <el-table-column label="手续费AFI" align="center" prop="feeAfiAmount" width="110" />
      <el-table-column label="总扣款AFI" align="center" prop="totalPayAfiAmount" width="120" />
      <el-table-column label="AFI浠锋牸" align="center" prop="afiPrice" width="100" />
      <el-table-column label="绛夊€糢SDT" align="center" prop="afiUsdtAmount" width="110" />
      <el-table-column label="鎴愪氦浠? align="center" prop="outcomePrice" width="90" />
      <el-table-column label="浠介" align="center" prop="shareAmount" width="120" />
      <el-table-column label="鍏戜粯USDT绛夊€? align="center" prop="payoutUsdtAmount" width="120" />
      <el-table-column label="缁撶畻AFI浠? align="center" prop="payoutAfiPrice" width="110" />
      <el-table-column label="鍏戜粯AFI" align="center" prop="payoutAfiAmount" width="120" />
      <el-table-column label="鐘舵€? align="center" prop="status" width="110">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_polymarket_order_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="缁撴潫鏃堕棿" align="center" prop="endTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="缁撶畻鏃堕棿" align="center" prop="settleTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.settleTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="鎿嶄綔" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['xms:polymarketOrder:query']"
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleDetail(scope.row)"
          >璇︽儏</el-button>
          <el-button
            v-hasPermi="['xms:polymarketOrder:edit']"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleReview(scope.row)"
          >澶嶆牳</el-button>
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
        <el-form-item label="璁㈠崟鍙?>
          <el-input v-model="form.orderNo" disabled />
        </el-form-item>
        <el-form-item label="甯傚満闂">
          <el-input v-model="form.marketQuestion" disabled />
        </el-form-item>
        <el-form-item label="鐘舵€? prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option
              v-for="dict in dict.type.t_polymarket_order_status"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="璧㈠涓嬫爣">
          <el-input-number v-model="form.resultOutcomeIndex" :min="0" />
        </el-form-item>
        <el-form-item label="璧㈠缁撴灉">
          <el-input v-model="form.resultOutcomeName" />
        </el-form-item>
        <el-form-item label="手续费AFI">
          <el-input v-model="form.feeAfiAmount" disabled />
        </el-form-item>
        <el-form-item label="总扣款AFI">
          <el-input v-model="form.totalPayAfiAmount" disabled />
        </el-form-item>
        <el-form-item label="鍏戜粯USDT绛夊€?>
          <el-input v-model="form.payoutUsdtAmount" disabled />
        </el-form-item>
        <el-form-item label="缁撶畻AFI浠锋牸">
          <el-input v-model="form.payoutAfiPrice" disabled />
        </el-form-item>
        <el-form-item label="鍏戜粯AFI">
          <el-input v-model="form.payoutAfiAmount" disabled />
        </el-form-item>
        <el-form-item label="澶囨敞">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="涓嬪崟蹇収">
          <el-input v-model="form.orderSnapshotJson" type="textarea" :rows="6" disabled />
        </el-form-item>
        <el-form-item label="缁撶畻蹇収">
          <el-input v-model="form.settleSnapshotJson" type="textarea" :rows="6" disabled />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">纭?瀹?/el-button>
        <el-button @click="cancel">鍙?娑?/el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPolymarketOrder, getPolymarketOrder, updatePolymarketOrder, settlePendingPolymarketOrder } from '@/api/xms/polymarketOrder'

export default {
  name: 'PolymarketOrder',
  dicts: ['t_polymarket_order_status', 't_polymarket_order_biz_type'],
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
        bizType: null,
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
        this.title = 'Polymarket璁㈠崟璇︽儏'
        this.open = true
      })
    },
    handleReview(row) {
      getPolymarketOrder(row.id).then(response => {
        this.form = response.data
        this.title = 'Polymarket璁㈠崟澶嶆牳'
        this.open = true
      })
    },
    submitForm() {
      updatePolymarketOrder(this.form).then(() => {
        this.$modal.msgSuccess('淇敼鎴愬姛')
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
        this.$modal.msgSuccess(`澶勭悊瀹屾垚锛?{response.data || 0} 涓競鍦篳)
        this.getList()
      })
    }
  }
}
</script>
