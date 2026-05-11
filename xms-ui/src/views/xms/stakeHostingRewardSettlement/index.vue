<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="90px">
      <el-form-item label="结算单号" prop="settlementNo">
        <el-input v-model="queryParams.settlementNo" placeholder="请输入结算单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="源订单号" prop="sourceOrderNo">
        <el-input v-model="queryParams.sourceOrderNo" placeholder="请输入源订单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="源用户ID" prop="sourceUserId">
        <el-input v-model="queryParams.sourceUserId" placeholder="请输入源用户ID" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="接收用户ID" prop="receiveUserId">
        <el-input v-model="queryParams.receiveUserId" placeholder="请输入接收用户ID" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="奖励类型" prop="rewardType">
        <el-select v-model="queryParams.rewardType" placeholder="请选择奖励类型" clearable>
          <el-option
            v-for="dict in dict.type.t_stake_hosting_reward_settlement_reward_type"
            :key="dict.value"
            :label="dict.label"
            :value="parseInt(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="到账状态" prop="arrivalStatus">
        <el-select v-model="queryParams.arrivalStatus" placeholder="请选择到账状态" clearable>
          <el-option
            v-for="dict in dict.type.t_stake_hosting_reward_settlement_arrival_status"
            :key="dict.value"
            :label="dict.label"
            :value="parseInt(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="结算日期" prop="settlementDay">
        <el-input v-model="queryParams.settlementDay" placeholder="yyyyMMdd" clearable @keyup.enter.native="handleQuery" />
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
          v-hasPermi="['xms:stakeHostingRewardSettlement:export']"
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="stakeHostingRewardSettlementList">
      <el-table-column label="结算单号" align="center" prop="settlementNo" width="180" />
      <el-table-column label="源订单号" align="center" prop="sourceOrderNo" width="180" />
      <el-table-column label="源用户ID" align="center" prop="sourceUserId" width="100" />
      <el-table-column label="接收用户ID" align="center" prop="receiveUserId" width="100" />
      <el-table-column label="奖励类型" align="center" prop="rewardType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_stake_hosting_reward_settlement_reward_type" :value="scope.row.rewardType" />
        </template>
      </el-table-column>
      <el-table-column label="奖励等级" align="center" prop="rewardLevel">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_info_game_level" :value="scope.row.rewardLevel" />
        </template>
      </el-table-column>
      <el-table-column label="奖励基数" align="center" prop="rewardBaseAmount" />
      <el-table-column label="奖励比例" align="center" prop="rewardRatio">
        <template slot-scope="scope">
          <span>{{ formatPercent(scope.row.rewardRatio) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="奖励金额" align="center" prop="rewardAmount" />
      <el-table-column label="静态毛收益" align="center" prop="grossStaticReward" />
      <el-table-column label="基础静态收益率" align="center" prop="baseStaticRate" width="140">
        <template slot-scope="scope">
          <span>{{ formatPercent(scope.row.baseStaticRate) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="AFI加速倍率" align="center" prop="afiAccelerateRate" width="120" />
      <el-table-column label="实际静态收益率" align="center" prop="actualStaticRate" width="140">
        <template slot-scope="scope">
          <span>{{ formatPercent(scope.row.actualStaticRate) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="服务费比例" align="center" prop="serviceFeeRatio">
        <template slot-scope="scope">
          <span>{{ formatPercent(scope.row.serviceFeeRatio) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="服务费金额" align="center" prop="serviceFeeAmount" />
      <el-table-column label="静态净收益" align="center" prop="netStaticReward" />
      <el-table-column label="到账状态" align="center" prop="arrivalStatus">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_stake_hosting_reward_settlement_arrival_status" :value="scope.row.arrivalStatus" />
        </template>
      </el-table-column>
      <el-table-column label="未到账原因" align="center" prop="skipReason">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_stake_hosting_reward_settlement_skip_reason" :value="scope.row.skipReason" />
        </template>
      </el-table-column>
      <el-table-column label="结算日期" align="center" prop="settlementDay" />
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
import { listStakeHostingRewardSettlement } from '@/api/xms/stakeHostingRewardSettlement'

export default {
  name: 'StakeHostingRewardSettlement',
  dicts: [
    't_stake_hosting_reward_settlement_reward_type',
    't_stake_hosting_reward_settlement_arrival_status',
    't_stake_hosting_reward_settlement_skip_reason',
    't_user_info_game_level'
  ],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      stakeHostingRewardSettlementList: [],
      daterangeCreateTime: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        settlementNo: null,
        sourceOrderNo: null,
        sourceUserId: null,
        receiveUserId: null,
        rewardType: null,
        arrivalStatus: null,
        settlementDay: null
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
      listStakeHostingRewardSettlement(this.queryParams).then(response => {
        this.stakeHostingRewardSettlementList = response.rows
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
      this.download('xms/stakeHostingRewardSettlement/export', {
        ...this.queryParams
      }, `stakeHostingRewardSettlement_${new Date().getTime()}.xlsx`)
    },
    formatPercent(value) {
      if (value === undefined || value === null || value === '') {
        return '-'
      }
      return value + ' %'
    }
  }
}
</script>
