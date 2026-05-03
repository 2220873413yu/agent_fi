<template>
  <div class="dashboard-container">
    <div class="dashboard-panel">
      <!-- 顶部概览区域 - 最重要的4个指标 -->
      <el-row :gutter="20" align="stretch" class="overview-row mb-10" type="flex">
        <el-col :lg="6" :md="12" :sm="12" :xs="24" class="overview-col">
          <el-card class="overview-card" shadow="hover">
            <div class="overview-content">
              <div class="overview-icon">
                <i class="el-icon-user-solid"></i>
              </div>
              <div class="overview-data">
                <div class="overview-label">总计用户</div>
                <div class="overview-value">
                  <count-to :duration='2000' :endVal='truncate4(fromData.userTotal)' :startVal='0' />
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :lg="6" :md="12" :sm="12" :xs="24" class="overview-col">
          <el-card class="overview-card" shadow="hover">
            <div class="overview-content">
              <div class="overview-icon">
                <i class="el-icon-circle-plus"></i>
              </div>
              <div class="overview-data">
                <div class="overview-label">今日新增</div>
                <div class="overview-value">
                  <count-to :duration='2000' :endVal='truncate4(fromData.todayUserCount)' :startVal='0' />
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

<!--        <el-col :lg="4" :md="6" :sm="12" :xs="12">
          <el-card class="overview-card" shadow="hover">
            <div class="overview-content">
              <div class="overview-icon">
                <i class="el-icon-s-flag"></i>
              </div>
              <div class="overview-data">
                <div class="overview-label">全网服务身份</div>
                <div class="overview-value">
                  <count-to :decimals="0" :duration='2000' :endVal='truncate4(fromData.v7)' :startVal='0' />
                </div>
              </div>
            </div>
          </el-card>
        </el-col>-->

       <el-col :lg="6" :md="12" :sm="12" :xs="24" class="overview-col">
          <el-card class="overview-card" shadow="hover">
            <div class="overview-content">
              <div class="overview-icon">
                <i class="el-icon-s-platform"></i>
              </div>
              <div class="overview-data">
                <div class="overview-label">全网质押节点量</div>
                <div class="overview-value">
                  <count-to :decimals="0" :duration='2000' :endVal='truncate4(fromData.v8)' :startVal='0' />
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :lg="6" :md="12" :sm="12" :xs="24" class="overview-col">
          <el-card class="overview-card" shadow="hover">
            <div class="overview-content">
              <div class="overview-icon">
                <i class="el-icon-s-claim"></i>
              </div>
              <div class="overview-data">
                <div class="overview-label">今日新增节点量</div>
                <div class="overview-value">
                  <count-to :decimals="0" :duration='2000' :endVal='truncate4(fromData.v9)' :startVal='0' />
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

<!--        <el-col :xs="12" :sm="6" :md="6" :lg="6">
          <el-card class="overview-card" shadow="hover">
            <div class="overview-content">
              <div class="overview-icon">
                <i class="el-icon-money"></i>
              </div>
              <div class="overview-data">
                <div class="overview-label">累计分发BSC->TRX</div>
                <div class="overview-value">
                  <count-to :startVal='0' :endVal='truncate4(fromData.v10)' :duration='2000' />
                </div>
              </div>
            </div>
          </el-card>
        </el-col>-->
      </el-row>

      <!-- 主要数据卡片 -->
      <el-row :gutter="20" align="stretch" class="dashboard-main-row" type="flex">
        <!-- 用户数据卡片 -->
<!--        <el-col :lg="8" :sm="12" :xs="24" class="mb-10">
          <el-card class="dashboard-card user-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <i class="el-icon-s-finance"></i>
                <span>质押OORT数据</span>
              </div>
            </template>
            <div class="card-content">
              <div class="data-list">
                <div class="data-item">
                  <div class="data-label">质押订单量</div>
                  <div class="data-value">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v6)' :duration='2000' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">质押OORT总金额</div>
                  <div class="data-value">
                    <count-to :decimals="getDecimalLen(fromData.v11)"
                              :duration='2000'
                              :endVal='toNumber(fromData.v11)'
                              :startVal='0' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">锁仓总量</div>
                  <div class="data-value">
                    <count-to :decimals="getDecimalLen(fromData.v12)"
                              :duration='2000'
                              :endVal='toNumber(fromData.v12)'
                              :startVal='0' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">已释放总量</div>
                  <div class="data-value">
                    <count-to :decimals="getDecimalLen(fromData.v13)"
                              :duration='2000'
                              :endVal='toNumber(fromData.v13)'
                              :startVal='0' />
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>-->

        <el-col :lg="7" :md="12" :sm="24" :xs="24" class="mb-10 dashboard-main-col">
          <el-card class="dashboard-card user-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <i class="el-icon-s-promotion"></i>
                <span>节点数据</span>
              </div>
            </template>
            <div class="card-content">
              <div class="data-list stats-grid stats-grid-3">
                <div class="data-item">
                  <div class="data-label">A1用户人数</div>
                  <div class="data-value">
                    <count-to :decimals="getDecimalLen(fromData.v28)"
                              :duration='2000'
                              :endVal='toNumber(fromData.v28)'
                              :startVal='0' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">A2人数</div>
                  <div class="data-value">
                    <count-to :decimals="getDecimalLen(fromData.v29)"
                              :duration='2000'
                              :endVal='toNumber(fromData.v29)'
                              :startVal='0' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">A3人数</div>
                  <div class="data-value">
                    <count-to :decimals="getDecimalLen(fromData.v30)"
                              :duration='2000'
                              :endVal='toNumber(fromData.v30)'
                              :startVal='0' />
                  </div>
                </div>
<!--                <div class="data-item">
                  <div class="data-label">省代理人数</div>
                  <div class="data-value">
                    <count-to :decimals="getDecimalLen(fromData.v31)"
                              :duration='2000'
                              :endVal='toNumber(fromData.v31)'
                              :startVal='0' />
                  </div>
                </div>-->
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :lg="5" :md="12" :sm="24" :xs="24" class="mb-10 dashboard-main-col">
          <el-card class="dashboard-card withdraw-card summary-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <i class="el-icon-s-order"></i>
                <span>用户提现业务</span>
              </div>
            </template>
            <div class="card-content">
              <div class="data-list single-stat-list">
                <div class="data-item single-stat-item">
                  <div class="data-label">累计提现USDT</div>
                  <div class="data-value single-stat-value">
                    <count-to :decimals="getDecimalLen(fromData.v32)"
                              :duration='2000'
                              :endVal='toNumber(fromData.v32)'
                              :startVal='0' />
                  </div>
                  <div class="single-stat-tip">历史累计提现金额</div>
                </div>
<!--                <div class="data-item">
                  <div class="data-label">累计提现DFC</div>
                  <div class="data-value">
                    <count-to :decimals="getDecimalLen(fromData.v33)"
                              :duration='2000'
                              :endVal='toNumber(fromData.v33)'
                              :startVal='0' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">累计提现OORT</div>
                  <div class="data-value">
                    <count-to :decimals="getDecimalLen(fromData.v34)"
                              :duration='2000'
                              :endVal='toNumber(fromData.v34)'
                              :startVal='0' />
                  </div>
                </div>

                <div class="data-item">
                  <div class="data-label">累计提现产出DFC</div>
                  <div class="data-value">
                    <count-to :decimals="getDecimalLen(fromData.v35)"
                              :duration='2000'
                              :endVal='toNumber(fromData.v35)'
                              :startVal='0' />
                  </div>
                </div>-->
              </div>
            </div>
          </el-card>
        </el-col>

<!--        <el-col :lg="12" :md="24" :sm="24" :xs="24" class="mb-10 dashboard-main-col">
          <el-card class="dashboard-card invest-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <i class="el-icon-s-operation"></i>
                <span>质押业务</span>
              </div>
            </template>
            <div class="card-content">
              <div class="data-list stats-grid stats-grid-2">
                <div class="data-item">
                  <div class="data-label">全网质押金额</div>
                  <div class="data-value">
                    <count-to :decimals="getDecimalLen(fromData.v36)"
                              :duration='2000'
                              :endVal='toNumber(fromData.v36)'
                              :startVal='0' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">未出局的收益</div>
                  <div class="data-value">
                    <count-to :decimals="getDecimalLen(fromData.v37)"
                              :duration='2000'
                              :endVal='toNumber(fromData.v37)'
                              :startVal='0' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">今日静奖总收益</div>
                  <div class="data-value">
                    <count-to :decimals="getDecimalLen(fromData.v38)"
                              :duration='2000'
                              :endVal='toNumber(fromData.v38)'
                              :startVal='0' />
                  </div>
                </div>

                <div class="data-item">
                  <div class="data-label">今日动奖总收益</div>
                  <div class="data-value">
                    <count-to :decimals="getDecimalLen(fromData.v39)"
                              :duration='2000'
                              :endVal='toNumber(fromData.v39)'
                              :startVal='0' />
                  </div>
                </div>

&lt;!&ndash;                <div class="data-item">
                  <div class="data-label">累计转账产出DFC</div>
                  <div class="data-value">
                    <count-to :decimals="getDecimalLen(fromData.v40)"
                              :duration='2000'
                              :endVal='toNumber(fromData.v40)'
                              :startVal='0' />
                  </div>
                </div>&ndash;&gt;
              </div>
            </div>
          </el-card>
        </el-col>-->




        <!-- 定期投资数据卡片 -->
<!--        <el-col :xs="24" :sm="12" :lg="8" class="mb-10">
          <el-card class="dashboard-card invest-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <i class="el-icon-time"></i>
                <span>定期投资数据</span>
              </div>
            </template>
            <div class="card-content">
              <div class="data-list">
                <div class="data-item">
                  <div class="data-label">今日定期质押USDT</div>
                  <div class="data-value">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v9)' :duration='2000' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">当前定期矿机FSN本金</div>
                  <div class="data-value">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v13)' :duration='2000' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">当前全网未领取利息</div>
                  <div class="data-value highlight">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v16)' :duration='2000' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">当前全网未领取余额宝</div>
                  <div class="data-value highlight">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v17)' :duration='2000' />
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>-->

        <!-- 收益数据卡片 -->
<!--        <el-col :xs="24" :sm="12" :lg="8" class="mb-10">
          <el-card class="dashboard-card profit-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <i class="el-icon-data-line"></i>
                <span>收益数据</span>
              </div>
            </template>
            <div class="card-content">
              <div class="data-list">
                <div class="data-item">
                  <div class="data-label">今日全网静态(FSN)</div>
                  <div class="data-value highlight">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v14)' :decimals="4" :duration='2000' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">全网静态(FSN)</div>
                  <div class="data-value highlight">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v24)' :decimals="4" :duration='2000' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">今日全网动态(FSN)</div>
                  <div class="data-value highlight">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v15)' :decimals="4" :duration='2000' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">全网动态(FSN)</div>
                  <div class="data-value highlight">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v25)' :decimals="4" :duration='2000' />
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>-->

        <!-- 提现数据卡片 -->
<!--        <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8" class="mb-10">
          <el-card class="dashboard-card user-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <i class="el-icon-bottom"></i>
                <span>XLS提现数据</span>
              </div>
            </template>
            <div class="card-content">
              <div class="data-list">
                <div class="data-item">
                  <div class="data-label">今日提现</div>
                  <div class="data-value highlight">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v18)' :decimals="6" :duration='2000' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">历史提现</div>
                  <div class="data-value">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v20)' :decimals="6" :duration='2000' />
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>-->

<!--
        <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8" class="mb-10">
          <el-card class="dashboard-card invest-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <i class="el-icon-bottom"></i>
                <span>全网当前资产余额</span>
              </div>
            </template>
            <div class="card-content">
              <div class="data-list">
                <div class="data-item">
                  <div class="data-label">XLS</div>
                  <div class="data-value highlight">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v26)' :decimals="6" :duration='2000' />
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
-->

        <!-- FSN提现数据卡片 -->
<!--        <el-col :xs="24" :sm="12" :lg="8" class="mb-10">
          <el-card class="dashboard-card invest-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <i class="el-icon-bottom"></i>
                <span>BPAY提现数据</span>
              </div>
            </template>
            <div class="card-content">
              <div class="data-list">
                <div class="data-item">
                  <div class="data-label">今日提现</div>
                  <div class="data-value highlight">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v19)' :decimals="4" :duration='2000' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">历史提现</div>
                  <div class="data-value">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v21)' :decimals="4" :duration='2000' />
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>-->

        <!-- 充值数据 -->
<!--        <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8" class="mb-10">
          <el-card class="dashboard-card profit-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <i class="el-icon-bottom"></i>
                <span>USDT充值数据</span>
              </div>
            </template>
            <div class="card-content">
              <div class="data-list">
                <div class="data-item">
                  <div class="data-label">总计充值</div>
                  <div class="data-value">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v22)' :decimals="6" :duration='2000' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">今日充值</div>
                  <div class="data-value">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v23)' :decimals="6" :duration='2000' />
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>-->

        <!--   全网当前资产余额     -->
<!--        <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8" class="mb-10">
          <el-card class="dashboard-card user-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <i class="el-icon-bottom"></i>
                <span>全网当前资产余额</span>
              </div>
            </template>
            <div class="card-content">
              <div class="data-list">
                <div class="data-item">
                  <div class="data-label">USDT</div>
                  <div class="data-value highlight">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v26)' :decimals="6" :duration='2000' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">Bpay</div>
                  <div class="data-value">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v27)' :decimals="6" :duration='2000' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">佣金钱包</div>
                  <div class="data-value">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v28)' :decimals="4" :duration='2000' />
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>-->
      </el-row>

      <!-- 第二行数据卡片 -->
      <el-row v-if="false" :gutter="20" align="stretch" type="flex">
        <!-- 全网拨款信息 -->
        <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8" class="mb-10">
          <el-card class="dashboard-card invest-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <i class="el-icon-bottom"></i>
                <span>全网当前资产余额</span>
              </div>
            </template>
            <div class="card-content">
              <div class="data-list">
                <div class="data-item">
                  <div class="data-label">USDT</div>
                  <div class="data-value highlight">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v26)' :decimals="6" :duration='2000' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">Bpay</div>
                  <div class="data-value">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v27)' :decimals="6" :duration='2000' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">佣金钱包</div>
                  <div class="data-value">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v31)' :decimals="4" :duration='2000' />
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>-->



        <!--   全网扣款信息     -->
<!--        <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8" class="mb-10">
          <el-card class="dashboard-card profit-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <i class="el-icon-bottom"></i>
                <span>全网扣款信息</span>
              </div>
            </template>
            <div class="card-content">
              <div class="data-list">
                <div class="data-item">
                  <div class="data-label">BOOMAI</div>
                  <div class="data-value highlight">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v32)' :decimals="6" :duration='2000' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">MAI</div>
                  <div class="data-value">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v33)' :decimals="6" :duration='2000' />
                  </div>
                </div>
                <div class="data-item">
                  <div class="data-label">佣金钱包</div>
                  <div class="data-value">
                    <count-to :startVal='0' :endVal='truncate4(fromData.v34)' :decimals="4" :duration='2000' />
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>-->

      </el-row>
    </div>
  </div>
</template>

<script>
import CountTo from 'vue-count-to'
import {getUserDataBoard} from '@/api/xms/statistics';
export default {
  components: {
    CountTo
  },
  data() {
    return {
      fromData: {
        userTotal: 0,
        todayUserCount: 0,
        v7: 0,
        v18: 0,
        v20: 0,
        v22: 0,
        v23: 0,
        v26: 0,
        v27: 0,
        v29: 0,
        v30: 0,
        v32: 0,
        v33: 0
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      getUserDataBoard().then(response => {
        this.fromData = response.data
      })
    },
    handleSetLineChartData(type) {
      this.$emit('handleSetLineChartData', type)
    },
    toNumber(val) {
      if (val === null || val === undefined || val === '') {
        return 0
      }
      const num = Number(val)
      return isNaN(num) ? 0 : num
    },
    getDecimalLen(val) {
      if (val === null || val === undefined) {
        return 0
      }
      const str = String(val)
      const dotIndex = str.indexOf('.')
      if (dotIndex < 0) {
        return 0
      }
      return str.length - dotIndex - 1
    },
    // 新增截断方法
    truncate4(val) {
      if (typeof val !== 'number') val = Number(val)
      if (isNaN(val)) return 0
      const factor = 10000
      return val >= 0
        ? Math.floor(val * factor) / factor
        : Math.ceil(val * factor) / factor
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  min-height: auto;
}

.dashboard-panel {
  padding: 8px 0;
}

.overview-row,
.dashboard-main-row {
  display: flex;
  flex-wrap: wrap;
}

.overview-col,
.dashboard-main-col {
  display: flex;
}

.mb-10 {
  margin-bottom: 20px;
}

// 顶部概览卡片
.overview-card {
  width: 100%;
  min-height: 110px;
  border-radius: 8px;
  transition: all 0.3s;
  cursor: pointer;
  border: none;
  background: linear-gradient(135deg, rgba(255,255,255,0.95) 0%, rgba(255,255,255,0.8) 100%);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
  }

  .overview-content {
    height: 100%;
    display: flex;
    align-items: center;
    padding: 0 15px;

    .overview-icon {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      background: linear-gradient(135deg, #409EFF 0%, #64B5F6 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 15px;
      box-shadow: 0 4px 10px rgba(64, 158, 255, 0.2);

      i {
        color: white;
        font-size: 24px;
      }
    }

    .overview-data {
      flex: 1;

      .overview-label {
        font-size: 14px;
        color: #606266;
        margin-bottom: 5px;
      }

      .overview-value {
        font-size: 22px;
        font-weight: 600;
        color: #303133;
      }
    }
  }

  &:nth-child(1) .overview-icon {
    background: linear-gradient(135deg, #67C23A 0%, #95D475 100%);
    box-shadow: 0 4px 10px rgba(103, 194, 58, 0.2);
  }

  &:nth-child(2) .overview-icon {
    background: linear-gradient(135deg, #E6A23C 0%, #F5CD79 100%);
    box-shadow: 0 4px 10px rgba(230, 162, 60, 0.2);
  }

  &:nth-child(3) .overview-icon {
    background: linear-gradient(135deg, #409EFF 0%, #64B5F6 100%);
    box-shadow: 0 4px 10px rgba(64, 158, 255, 0.2);
  }

  &:nth-child(4) .overview-icon {
    background: linear-gradient(135deg, #F56C6C 0%, #FFA9A9 100%);
    box-shadow: 0 4px 10px rgba(245, 108, 108, 0.2);
  }
}

// 数据卡片
.dashboard-card {
  width: 100%;
  min-height: 100%;
  border-radius: 8px;
  transition: all 0.3s;
  position: relative;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  }

  .card-header {
    display: flex;
    align-items: center;
    font-size: 16px;
    padding: 12px 15px;
    font-weight: 600;
    border-bottom: 1px solid #f0f0f0;

    i {
      margin-right: 8px;
      font-size: 18px;
    }
  }

  .card-content {
    padding: 15px;
  }

  .data-list {
    display: grid;
    gap: 12px;
    min-height: 100%;
  }

  .data-item {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    flex-direction: column;
    padding: 15px 12px;
    border-radius: 8px;
    background-color: #f8f9fa;
    border-left: 3px solid transparent;
    transition: all 0.3s;
    min-height: 88px;

    &:hover {
      background-color: #f0f2f5;
      border-left-color: #409EFF;
      transform: translateY(-2px);
    }

    .data-label {
      color: #606266;
      font-size: 14px;
      font-weight: 500;
      line-height: 1.4;
    }

    .data-value {
      margin-top: auto;
      width: 100%;
      font-size: 22px;
      font-weight: 700;
      color: #303133;
      word-break: break-all;
      text-align: left;
      line-height: 1.2;

      &.highlight {
        color: #409EFF;
        font-size: 24px;
      }
    }
  }
}

.stats-grid-2 {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.stats-grid-3 {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.single-stat-list {
  grid-template-columns: 1fr;
  height: 100%;

  .data-item {
    min-height: 192px;
  }

  .data-label {
    font-size: 15px;
    margin-bottom: 14px;
  }

  .data-value {
    font-size: 34px;
    color: #E6A23C;
  }
}

.summary-card {
  .card-content {
    height: 100%;
  }
}

.single-stat-item {
  align-items: center !important;
  justify-content: center !important;
  text-align: center;
  padding: 24px 18px !important;
  background: linear-gradient(180deg, #fffaf3 0%, #fff 100%) !important;
  border: 1px solid #f6d8a8;
  border-left: 3px solid #E6A23C !important;

  &:hover {
    border-left-color: #d48806 !important;
    background: linear-gradient(180deg, #fff7e6 0%, #fff 100%) !important;
  }

  .data-label {
    width: 100%;
    margin-bottom: 12px;
    color: #8c6d1f;
    font-size: 15px;
    font-weight: 600;
    text-align: center;
  }
}

.single-stat-value {
  width: 100% !important;
  margin-top: 0 !important;
  text-align: center !important;
  font-size: 38px !important;
  line-height: 1.1 !important;
  color: #E6A23C !important;
  word-break: break-word !important;
}

.single-stat-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

// 不同卡片类型的样式
.user-card {
  border-top: 3px solid #67C23A;
  .card-header {
    color: #67C23A;
  }
}

.invest-card {
  border-top: 3px solid #409EFF;
  .card-header {
    color: #409EFF;
  }
}

.profit-card {
  border-top: 3px solid #F56C6C;
  .card-header {
    color: #F56C6C;
  }
}

.withdraw-card {
  border-top: 3px solid #E6A23C;
  .card-header {
    color: #E6A23C;
  }
}

.fsn-withdraw-card {
  border-top: 3px solid #909399;
  .card-header {
    color: #909399;
  }
}

// 简化数字增长动画效果
.count-to {
  display: inline-block;
}

// 响应式优化
@media (max-width: 768px) {
  .dashboard-container {
    padding: 8px;
  }

  .dashboard-panel {
    padding: 4px 0;
  }

  .overview-card {
    margin-bottom: 10px;
    min-height: 96px;

    .overview-content {
      padding: 0 10px;

      .overview-icon {
        width: 50px;
        height: 50px;
        margin-right: 10px;

        i {
          font-size: 20px;
        }
      }

      .overview-data {
        .overview-label {
          font-size: 12px;
        }

        .overview-value {
          font-size: 18px;
        }
      }
    }
  }

  .dashboard-card {
    margin-bottom: 15px;

    .card-header {
      padding: 10px 12px;
      font-size: 14px;

      i {
        font-size: 16px;
      }
    }

    .card-content {
      padding: 12px;
    }

    .data-item {
      padding: 12px 10px;
      min-height: 72px;

      .data-label {
        font-size: 13px;
      }

      .data-value {
        font-size: 18px;

        &.highlight {
          font-size: 20px;
        }
      }
    }
  }

  .stats-grid-2,
  .stats-grid-3 {
    grid-template-columns: 1fr;
  }

  .single-stat-list {
    .data-item {
      min-height: 120px;
    }

    .data-value {
      font-size: 28px;
    }
  }

  .single-stat-value {
    font-size: 30px !important;
  }
}

@media (min-width: 769px) and (max-width: 992px) {
  .dashboard-container {
    padding: 12px;
  }

  .overview-card {
    .overview-content {
      .overview-icon {
        width: 55px;
        height: 55px;

        i {
          font-size: 22px;
        }
      }

      .overview-data {
        .overview-value {
          font-size: 20px;
        }
      }
    }
  }
}

@media (min-width: 1200px) {
  .dashboard-container {
    padding: 20px;
  }

  .dashboard-main-row {
    align-items: stretch;
  }
}

// 大屏幕下优化3列布局的间距
@media (min-width: 1600px) {
  .dashboard-container {
    padding: 24px;
  }

  .el-row {
    margin-left: -12px !important;
    margin-right: -12px !important;
  }

  .el-col {
    padding-left: 12px !important;
    padding-right: 12px !important;
  }
}
</style>
