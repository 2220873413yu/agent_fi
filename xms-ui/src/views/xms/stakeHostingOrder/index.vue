<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="90px">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="钱包地址" prop="account">
        <el-input v-model="queryParams.account" placeholder="请输入钱包地址" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="订单来源" prop="sourceType">
        <el-select v-model="queryParams.sourceType" placeholder="请选择来源" clearable>
          <el-option
            v-for="dict in dict.type.t_stake_hosting_order_source_type"
            :key="dict.value"
            :label="dict.label"
            :value="parseInt(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="支付状态" prop="payStatus">
        <el-select v-model="queryParams.payStatus" placeholder="请选择支付状态" clearable>
          <el-option
            v-for="dict in dict.type.t_stake_hosting_order_pay_status"
            :key="dict.value"
            :label="dict.label"
            :value="parseInt(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="业务状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择业务状态" clearable>
          <el-option
            v-for="dict in dict.type.t_stake_hosting_order_status"
            :key="dict.value"
            :label="dict.label"
            :value="parseInt(dict.value)"
          />
        </el-select>
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
          v-hasPermi="['xms:stakeHostingOrder:add']"
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
        >拨付托管</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:stakeHostingOrder:export']"
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="stakeHostingOrderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="订单号" align="center" prop="orderNo" width="180" />
      <el-table-column label="用户ID" align="center" prop="userId" width="100" />
      <el-table-column label="钱包地址" align="center" prop="account" width="180" />
      <el-table-column label="套餐" align="center" prop="packageName" />
      <el-table-column label="天数" align="center" prop="packageDays" width="80">
        <template slot-scope="scope">{{ scope.row.packageDays }}天</template>
      </el-table-column>
      <el-table-column label="托管金额" align="center" prop="stakeUsdtAmount" />
      <el-table-column label="来源" align="center" prop="sourceType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_stake_hosting_order_source_type" :value="scope.row.sourceType" />
        </template>
      </el-table-column>
      <el-table-column label="支付状态" align="center" prop="payStatus">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_stake_hosting_order_pay_status" :value="scope.row.payStatus" />
        </template>
      </el-table-column>
      <el-table-column label="业务状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_stake_hosting_order_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="运行天数" align="center" prop="runDays" />
      <el-table-column label="今日收益" align="center" prop="todayReward" />
      <el-table-column label="累计收益" align="center" prop="totalStaticReward" />
      <el-table-column label="是否回本" align="center" prop="isReturnPrincipal">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_stake_hosting_order_return_principal" :value="scope.row.isReturnPrincipal" />
        </template>
      </el-table-column>
      <el-table-column label="支付hash" align="center" prop="payHash" width="180" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付时间" align="center" prop="payTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.payTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="完成时间" align="center" prop="finishTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.finishTime) }}</span>
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

    <el-dialog :title="title" :visible.sync="open" width="520px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="钱包地址" prop="account">
          <el-input v-model="form.account" placeholder="请输入钱包地址" />
        </el-form-item>
        <el-form-item label="托管套餐" prop="packageId">
          <el-select v-model="form.packageId" placeholder="请选择托管套餐" style="width: 100%">
            <el-option
              v-for="item in packageOptions"
              :key="item.id"
              :label="item.name + ' / ' + item.days + '天 / 起购' + item.minAmount + 'U'"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="托管金额" prop="stakeUsdtAmount">
          <el-input
            v-model="form.stakeUsdtAmount"
            maxlength="6"
            placeholder="请输入整数USDT金额"
            show-word-limit
            @input="onIntegerInput('stakeUsdtAmount')"
          />
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
import { listStakeHostingOrder, addStakeHostingOrder } from '@/api/xms/stakeHostingOrder'
import { listStakeHostingPackage } from '@/api/xms/stakeHostingPackage'

export default {
  name: 'StakeHostingOrder',
  dicts: [
    't_stake_hosting_order_source_type',
    't_stake_hosting_order_pay_status',
    't_stake_hosting_order_status',
    't_stake_hosting_order_return_principal'
  ],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      stakeHostingOrderList: [],
      packageOptions: [],
      title: '',
      open: false,
      daterangeCreateTime: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: null,
        userId: null,
        account: null,
        sourceType: null,
        payStatus: null,
        status: null
      },
      form: {},
      rules: {
        account: [{ required: true, message: '钱包地址不能为空', trigger: 'blur' }],
        packageId: [{ required: true, message: '托管套餐不能为空', trigger: 'change' }],
        stakeUsdtAmount: [{ required: true, message: '托管金额不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
    this.getPackageOptions()
  },
  methods: {
    getList() {
      this.loading = true
      this.queryParams.params = {}
      if (this.daterangeCreateTime && this.daterangeCreateTime.length === 2) {
        this.queryParams.params['beginCreateTime'] = this.daterangeCreateTime[0]
        this.queryParams.params['endCreateTime'] = this.daterangeCreateTime[1]
      }
      listStakeHostingOrder(this.queryParams).then(response => {
        this.stakeHostingOrderList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getPackageOptions() {
      listStakeHostingPackage({ pageNum: 1, pageSize: 100, status: 1 }).then(response => {
        this.packageOptions = response.rows || []
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        account: null,
        packageId: null,
        stakeUsdtAmount: null,
        remark: null
      }
      this.resetForm('form')
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
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '后台拨付托管订单'
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          addStakeHostingOrder(this.form).then(() => {
            this.$modal.msgSuccess('拨付成功')
            this.open = false
            this.getList()
          })
        }
      })
    },
    handleExport() {
      this.download('xms/stakeHostingOrder/export', {
        ...this.queryParams
      }, `stakeHostingOrder_${new Date().getTime()}.xlsx`)
    },
    onIntegerInput(prop) {
      this.form[prop] = String(this.form[prop] || '').replace(/[^\d]/g, '').slice(0, 6)
    }
  }
}
</script>
