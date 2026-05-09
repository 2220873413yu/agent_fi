<template>
  <div class="app-container">
    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never">
          <div class="stat-label">当前余额(USDT)</div>
          <div class="stat-value">{{ pool.balanceAmount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never">
          <div class="stat-label">累计收入(USDT)</div>
          <div class="stat-value">{{ pool.totalIncomeAmount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never">
          <div class="stat-label">累计支出(USDT)</div>
          <div class="stat-value">{{ pool.totalExpenseAmount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never">
          <div class="stat-label">奖池编码</div>
          <div class="stat-code">{{ pool.poolCode || '-' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="mt16" shadow="never">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="最近收入时间">{{ pool.lastIncomeTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="最近支出时间">{{ pool.lastExpenseTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ pool.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ pool.updateTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ pool.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-row :gutter="10" class="mb8 mt16">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:stakeHostingGlobalDividendPool:adjust']"
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdjust(1)"
        >手动增加</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:stakeHostingGlobalDividendPool:adjust']"
          type="danger"
          plain
          icon="el-icon-minus"
          size="mini"
          @click="handleAdjust(2)"
        >手动扣减</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getInfo" />
    </el-row>

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="调账类型" prop="flowType">
          <el-radio-group v-model="form.flowType" disabled>
            <el-radio v-for="dict in dict.type.t_stake_hosting_global_dividend_pool_log_flow_type" :key="dict.value" :label="parseInt(dict.value)">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input v-model="form.amount" placeholder="请输入调账金额" @input="onAmountInput('amount')" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
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
import { getStakeHostingGlobalDividendPool, adjustStakeHostingGlobalDividendPool } from '@/api/xms/stakeHostingGlobalDividendPool'

export default {
  name: 'StakeHostingGlobalDividendPool',
  dicts: ['t_stake_hosting_global_dividend_pool_log_flow_type'],
  data() {
    return {
      loading: false,
      showSearch: false,
      pool: {},
      open: false,
      title: '',
      form: {},
      rules: {
        amount: [{ required: true, message: '金额不能为空', trigger: 'blur' }],
        flowType: [{ required: true, message: '调账类型不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getInfo()
  },
  methods: {
    getInfo() {
      this.loading = true
      getStakeHostingGlobalDividendPool().then(response => {
        this.pool = response.data || {}
        this.loading = false
      })
    },
    handleAdjust(flowType) {
      this.form = {
        flowType: flowType,
        amount: null,
        remark: null
      }
      this.title = flowType === 1 ? '手动增加奖池' : '手动扣减奖池'
      this.open = true
      this.$nextTick(() => this.resetForm('form'))
    },
    cancel() {
      this.open = false
      this.form = {}
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          adjustStakeHostingGlobalDividendPool(this.form).then(() => {
            this.$modal.msgSuccess('操作成功')
            this.open = false
            this.getInfo()
          })
        }
      })
    },
    onAmountInput(prop) {
      this.form[prop] = String(this.form[prop] || '').replace(/[^\d.]/g, '').replace(/^\./g, '').replace(/\.{2,}/g, '.')
    }
  }
}
</script>

<style scoped>
.mt16 {
  margin-top: 16px;
}

.stat-label {
  color: #606266;
  font-size: 13px;
}

.stat-value {
  margin-top: 8px;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.stat-code {
  margin-top: 8px;
  color: #303133;
  font-size: 15px;
  font-weight: 600;
  word-break: break-all;
}
</style>
