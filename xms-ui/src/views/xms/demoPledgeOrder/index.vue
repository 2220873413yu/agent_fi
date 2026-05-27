<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="90px">
      <el-form-item label="订单号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option
            v-for="dict in dict.type.t_demo_pledge_order_status"
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
          v-hasPermi="['xms:demoPledgeOrder:buy']"
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleBuy"
        >购买演示</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:demoPledgeOrder:export']"
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="demoPledgeOrderList">
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="订单号" align="center" prop="orderNo" width="180" />
      <el-table-column label="用户ID" align="center" prop="userId" width="100" />
      <el-table-column label="套餐名称" align="center" prop="packageName" />
      <el-table-column label="质押USDT金额" align="center" prop="pledgeUsdtAmount" />
      <el-table-column label="释放进度" align="center" width="110">
        <template slot-scope="scope">
          <span>{{ scope.row.releasedDays || 0 }}/{{ scope.row.releaseDays || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="日利率(%)" align="center" prop="dailyRate" width="100" />
      <el-table-column label="累计收益USDT" align="center" prop="totalRewardUsdtAmount" width="130" />
      <el-table-column label="收益状态" align="center" prop="rewardStatus" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_demo_pledge_reward_status" :value="scope.row.rewardStatus" />
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_demo_pledge_order_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="支付时间" align="center" prop="payTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.payTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="处理时间" align="center" prop="processTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.processTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="完成时间" align="center" prop="finishTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.finishTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="最近释放时间" align="center" prop="lastRewardTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastRewardTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="失败原因" align="center" prop="failReason" min-width="160" />
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

    <el-dialog title="购买示例质押套餐" :visible.sync="openBuy" width="500px" append-to-body>
      <el-form ref="buyForm" :model="buyForm" :rules="buyRules" label-width="110px">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="buyForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="套餐" prop="packageId">
          <el-select v-model="buyForm.packageId" placeholder="请选择套餐" filterable style="width: 100%">
            <el-option
              v-for="item in enabledPackageList"
              :key="item.id"
              :label="item.packageName + ' / ' + item.pledgeUsdtAmount + ' USDT / ' + item.releaseDays + '天 / 日' + item.dailyRate + '%'"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitBuy">确 定</el-button>
        <el-button @click="cancelBuy">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDemoPledgeOrder, buyDemoPledgeOrder } from '@/api/xms/demoPledgeOrder'
import { listDemoPledgePackage } from '@/api/xms/demoPledgePackage'

export default {
  name: 'DemoPledgeOrder',
  dicts: ['t_demo_pledge_order_status', 't_demo_pledge_reward_status'],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      demoPledgeOrderList: [],
      enabledPackageList: [],
      openBuy: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: null,
        userId: null,
        status: null
      },
      buyForm: {
        userId: null,
        packageId: null
      },
      buyRules: {
        userId: [{ required: true, message: '用户ID不能为空', trigger: 'blur' }],
        packageId: [{ required: true, message: '套餐不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listDemoPledgeOrder(this.queryParams).then(response => {
        this.demoPledgeOrderList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleBuy() {
      this.buyForm = {
        userId: null,
        packageId: null
      }
      listDemoPledgePackage({ status: 1, pageNum: 1, pageSize: 100 }).then(response => {
        this.enabledPackageList = response.rows
        this.openBuy = true
      })
    },
    cancelBuy() {
      this.openBuy = false
      this.resetForm('buyForm')
    },
    submitBuy() {
      this.$refs['buyForm'].validate(valid => {
        if (!valid) {
          return
        }
        buyDemoPledgeOrder({
          userId: Number(this.buyForm.userId),
          packageId: this.buyForm.packageId
        }).then(() => {
          this.$modal.msgSuccess('购买成功，已投递异步业绩处理')
          this.openBuy = false
          this.getList()
        })
      })
    },
    handleExport() {
      this.download('xms/demoPledgeOrder/export', {
        ...this.queryParams
      }, `demoPledgeOrder_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
