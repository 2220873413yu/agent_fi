<template>
  <el-row :gutter="40" class="panel-group">
    <!-- <el-col :xs="12" :sm="12" :lg="1" class="card-panel-col">
      MCG
    </el-col> -->
    <el-col :lg="5" :sm="12" :xs="12" class="card-panel-col">
      <div class="card-panel" >
        <div class="card-panel-icon-wrapper icon-money">
          <svg-icon class-name="card-panel-icon" icon-class="money" />
        </div>
        <div class="card-panel-description">
          <div class="card-panel-text">
            XMS历史充值总额
          </div>
          <count-to :decimals="5" :duration='2000' :endVal='one' :startVal='0' class="card-panel-num" />
        </div>
      </div>
    </el-col>
    <el-col :lg="4" :sm="12" :xs="12" class="card-panel-col">
      <div class="card-panel"  >
        <div class="card-panel-icon-wrapper icon-money">
          <svg-icon class-name="card-panel-icon" icon-class="money" />
        </div>
        <div class="card-panel-description">
          <div class="card-panel-text">
            XMS今日充值
          </div>
          <count-to :decimals="5" :duration='2000' :endVal='five' :startVal='0'  class="card-panel-num" />
        </div>
      </div>
    </el-col>
    <el-col :lg="5" :sm="12" :xs="12" class="card-panel-col">
      <div class="card-panel"  >
        <div class="card-panel-icon-wrapper icon-money">
          <svg-icon class-name="card-panel-icon" icon-class="money" />
        </div>
        <div class="card-panel-description">
          <div class="card-panel-text">
            XMS历史提现总额
          </div>
          <count-to :decimals="5" :duration='2000' :endVal='two' :startVal='0' class="card-panel-num" />
        </div>
      </div>
    </el-col>
    <el-col :lg="4" :sm="12" :xs="12" class="card-panel-col">
      <div class="card-panel"  >
        <div class="card-panel-icon-wrapper icon-money">
          <svg-icon class-name="card-panel-icon" icon-class="money" />
        </div>
        <div class="card-panel-description">
          <div class="card-panel-text">
            XMS今日提现
          </div>
          <count-to :decimals="5" :duration='2000' :endVal='seven' :startVal='0'  class="card-panel-num" />
        </div>
      </div>
    </el-col>
    <el-col :lg="5" :sm="12" :xs="12" class="card-panel-col">
      <div class="card-panel"  >
        <div class="card-panel-icon-wrapper icon-money">
          <svg-icon class-name="card-panel-icon" icon-class="money" />
        </div>
        <div class="card-panel-description">
          <div class="card-panel-text">
            XMS提现审核资金总额
          </div>
          <count-to :decimals="5" :duration='2000' :endVal='nine' :startVal='0'  class="card-panel-num" />
        </div>
      </div>
    </el-col>
    <el-col :lg="5" :sm="12" :xs="12" class="card-panel-col">
      <div class="card-panel"  >
        <div class="card-panel-icon-wrapper icon-money">
          <svg-icon class-name="card-panel-icon" icon-class="money" />
        </div>
        <div class="card-panel-description">
          <div class="card-panel-text">
            USDT历史充值总额
          </div>
          <count-to  :decimals="5" :duration='2000' :endVal='three' :startVal='0' class="card-panel-num" />
        </div>
      </div>
    </el-col>
    <el-col :lg="4" :sm="12" :xs="12" class="card-panel-col">
      <div class="card-panel"  >
        <div class="card-panel-icon-wrapper icon-money">
          <svg-icon class-name="card-panel-icon" icon-class="money" />
        </div>
        <div class="card-panel-description">
          <div class="card-panel-text">
            USDT今日充值
          </div>
          <count-to :decimals="5" :duration='2000' :endVal='six' :startVal='0' class="card-panel-num" />
        </div>
      </div>
    </el-col>
    <el-col :lg="5" :sm="12" :xs="12" class="card-panel-col">
      <div class="card-panel"  >
        <div class="card-panel-icon-wrapper icon-money">
          <svg-icon class-name="card-panel-icon" icon-class="money" />
        </div>
        <div class="card-panel-description">
          <div class="card-panel-text">
            USDT历史提现总额
          </div>
          <count-to :decimals="5" :duration='2000' :endVal='four' :startVal='0'  class="card-panel-num" />
        </div>
      </div>
    </el-col>
    <el-col :lg="4" :sm="12" :xs="12" class="card-panel-col">
      <div class="card-panel"  >
        <div class="card-panel-icon-wrapper icon-money">
          <svg-icon class-name="card-panel-icon" icon-class="money" />
        </div>
        <div class="card-panel-description">
          <div class="card-panel-text">
            USDT今日提现
          </div>
          <count-to :decimals="5" :duration='2000' :endVal='eight' :startVal='0' class="card-panel-num" />
        </div>
      </div>
    </el-col>
    <el-col :lg="5" :sm="12" :xs="12" class="card-panel-col">
      <div class="card-panel"  >
        <div class="card-panel-icon-wrapper icon-money">
          <svg-icon class-name="card-panel-icon" icon-class="money" />
        </div>
        <div class="card-panel-description">
          <div class="card-panel-text">
            USDT提现审核资金总额
          </div>
          <count-to :decimals="5" :duration='2000' :endVal='ten' :startVal='0' class="card-panel-num" />
        </div>
      </div>
    </el-col>
  </el-row>
</template>

<script>
import CountTo from 'vue-count-to';
// import {listStatistics} from '@/api/mcg/statistics';

export default {
  components: {
    CountTo
  },
  data() {
    return {
      one:0,
      two:0,
      three:0,
      four:0,
      five:0,
      six:0,
      seven:0,
      eight:0,
      nine:0,
      ten:0,
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      // listStatistics().then(response => {
      //   console.log(response.data);
      //   const data = response.data
      //   this.one = data[1];
      //   this.two = data[2];
      //   this.three = data[3];
      //   this.four = data[4];
      //   this.five = data[5];
      //   this.six = data[6];
      //   this.seven = data[7];
      //   this.eight = data[8];
      //   this.nine = data[9];
      //   this.ten = data[10];
      // });
    }

  }
}
</script>

<style lang="scss" scoped>
.panel-group {
  margin-top: 18px;

  .card-panel-col {
    margin-bottom: 32px;
  }

  .card-panel {
    height: 108px;
    cursor: pointer;
    font-size: 12px;
    position: relative;
    overflow: hidden;
    color: #666;
    background: #fff;
    box-shadow: 4px 4px 40px rgba(0, 0, 0, .05);
    border-color: rgba(0, 0, 0, .05);

    &:hover {
      .card-panel-icon-wrapper {
        color: #fff;
      }

      .icon-people {
        background: #40c9c6;
      }

      .icon-message {
        background: #36a3f7;
      }

      .icon-money {
        background: #f4516c;
      }

      .icon-shopping {
        background: #34bfa3
      }
    }

    .icon-people {
      color: #40c9c6;
    }

    .icon-message {
      color: #36a3f7;
    }

    .icon-money {
      color: #f4516c;
    }

    .icon-shopping {
      color: #34bfa3
    }

    .card-panel-icon-wrapper {
      float: left;
      margin: 14px 0 0 14px;
      padding: 16px;
      transition: all 0.38s ease-out;
      border-radius: 6px;
    }

    .card-panel-icon {
      float: left;
      font-size: 48px;
    }

    .card-panel-description {
      float: right;
      font-weight: bold;
      margin: 26px;
      margin-left: 0px;

      .card-panel-text {
        line-height: 18px;
        color: rgba(0, 0, 0, 0.45);
        font-size: 16px;
        margin-bottom: 12px;
      }

      .card-panel-num {
        font-size: 16px;
      }
    }
  }
}

@media (max-width:550px) {
  .card-panel-description {
    display: none;
  }

  .card-panel-icon-wrapper {
    float: none !important;
    width: 100%;
    height: 100%;
    margin: 0 !important;

    .svg-icon {
      display: block;
      margin: 14px auto !important;
      float: none !important;
    }
  }
}
</style>
