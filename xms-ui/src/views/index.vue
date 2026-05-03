<template>
  <div class="dashboard-editor-container">
    <div class="--mb--rich-text"  >欢迎登录Agent Fi管理后台</div>
    <panel-group @handleSetLineChartData="handleSetLineChartData" />

<!--    <el-row style="background:#fff;padding:16px 16px 0;margin-bottom:32px;">
      <line-chart :chart-data="userRegisterGroup" />
    </el-row>-->
    <el-row >
      <el-col :lg="24" :sm="24" :xs="24">
        <div class="chart-wrapper">
          <line-chart :chart-data="userRegisterGroup" />
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="32" class="chart-grid">
<!--      <el-col :lg="12" :sm="24" :xs="24">
        <div class="chart-wrapper">
          <recharge-bar-chart  :chart-data="lineRechargeChartData" title="近7天充值数据"/>
        </div>
      </el-col>-->
      <el-col :lg="12" :sm="24" :xs="24">
        <div class="chart-wrapper">
          <bar-chart :chart-data="lineNodeOrderChartData" title="近7天节点订单数据"/>
        </div>
      </el-col>
      <el-col :lg="12" :sm="24" :xs="24">
        <div class="chart-wrapper">
          <bar-chart :chart-data="lineWdwChartData" title="近7天提现数据"/>
        </div>
      </el-col>
    </el-row>
<!--    <el-row :gutter="32" class="chart-grid">
      <el-col :lg="24" :sm="24" :xs="24">
        <div class="chart-wrapper">
          <transfer-bar-chart :chart-data="lineTransferChartData" title="近7天转账数据"/>
        </div>
      </el-col>
    </el-row>-->
    <el-row class="chart-grid">
      <el-col :lg="24" :sm="24" :xs="24">
        <div class="chart-wrapper">
<!--          <LineChartType :chart-data="lineOrderChartData" title="报单" title1="拨比"/>-->
        </div>
      </el-col>
    </el-row>

  </div>
</template>

<script>
import PanelGroup from './dashboard/PanelGroup'
import LineChart from './dashboard/LineChart'
import RaddarChart from './dashboard/RaddarChart'
import LineChartType from './dashboard/LineChartType'
import BarChart from './dashboard/BarChart'
import RechargeBarChart from './dashboard/RechargeBarChart'
import TransferBarChart from './dashboard/TransferBarChart'
import {listGroupTotal,listOrderGroupTotal} from '@/api/xms/statistics';
const lineChartData = {
  newVisitis: {
    expectedData: [100, 120, 161, 134, 105, 160, 165],
    key: [1, 2,3, 4,5, 6, 7,8],
  },
  messages: {
    expectedData: [200, 192, 120, 144, 160, 130, 140],
  },
  purchases: {
    expectedData: [80, 100, 121, 104, 105, 90, 100],
  },
  shoppings: {
    expectedData: [130, 140, 141, 142, 145, 150, 160],
  }
}

export default {
  name: 'Index',
  components: {
    PanelGroup,
    LineChart,
    RaddarChart,
    LineChartType,
    RechargeBarChart,
    TransferBarChart,
    BarChart
  },
  data() {
    return {
      // lineChartData: lineChartData.newVisitis
      userRegisterGroup: {},
      lineRechargeChartData: {},
      lineOrderChartData: {},
      lineNodeOrderChartData: {},
      lineWdwChartData: {},
      lineTransferChartData: {},
    }
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      listGroupTotal().then(response =>{
        this.userRegisterGroup=response.data.userRegisterGroup
        this.lineRechargeChartData=response.data.lineRechargeChartData
        this.lineNodeOrderChartData=response.data.lineNodeOrderChartData
        this.lineWdwChartData=response.data.lineWdwChartData
        this.lineTransferChartData=response.data.lineTransferChartData
        console.log("lineWdwChartData",this.lineWdwChartData)
        console.log('lineRechargeChartData',this.lineRechargeChartData)
      })
        listOrderGroupTotal().then(response =>{
          this.lineOrderChartData=response.data
        })
    },
    handleSetLineChartData(type) {
      this.lineChartData = lineChartData[type]
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-editor-container {
  padding: 32px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  position: relative;

  .chart-wrapper {
    background: rgba(255, 255, 255, 0.95);
    padding: 20px;
    margin-bottom: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.25s ease;
    position: relative;
    overflow: hidden;

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    &:nth-child(1) {
      border-top: 2px solid #67C23A;
    }

    &:nth-child(2) {
      border-top: 2px solid #409EFF;
    }

    &:nth-child(3) {
      border-top: 2px solid #E6A23C;
    }
  }
}

.chart-grid {
  margin-top: 24px;

  ::v-deep .chart {
    min-height: 320px;
  }
}

.order-section {
  margin-top: 32px;

  .section-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 16px;
  }

  .summary-card {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.05);
    margin-bottom: 20px;

    .label {
      font-size: 14px;
      color: #909399;
    }
    .value {
      font-size: 28px;
      font-weight: 600;
      margin: 6px 0 4px;
      color: #303133;

      &.success { color: #67c23a; }
      &.warning { color: #e6a23c; }
      &.danger { color: #f56c6c; }
    }
    .desc {
      color: #c0c4cc;
      font-size: 12px;
    }
  }

  .order-charts {
    .chart-card {
      background: #fff;
      border-radius: 8px;
      padding: 20px;
      margin-bottom: 20px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.05);

      .chart-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 12px;
      }

    }
  }
}

@media (max-width:1024px) {
  .chart-wrapper {
    padding: 8px;
  }
}
.--mb--rich-text{
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  font-weight: 600;
  font-size: 28px;
  color: #303133;
  font-style: normal;
  letter-spacing: 1px;
  text-align: center;
  width: 100%;
  margin: 0px 0 20px 0;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
}
</style>
