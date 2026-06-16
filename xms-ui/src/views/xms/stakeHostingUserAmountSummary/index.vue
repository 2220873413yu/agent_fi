<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:stakeHostingUserAmountSummary:increase']"
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdjust('increase')"
        >手动增加</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:stakeHostingUserAmountSummary:decrease']"
          type="danger"
          plain
          icon="el-icon-minus"
          size="mini"
          @click="handleAdjust('decrease')"
        >手动扣除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          plain
          icon="el-icon-refresh"
          size="mini"
          @click="getList"
        >刷新</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="summaryList">
      <el-table-column label="ID" align="center" prop="id" width="90" />
      <el-table-column label="全平台托管累计金额" align="center" prop="totalAmount" width="180" />
      <el-table-column label="备注" align="center" prop="remark" min-width="220" show-overflow-tooltip />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['xms:stakeHostingUserAmountSummary:increase']"
            size="mini"
            type="text"
            icon="el-icon-plus"
            @click="handleAdjust('increase')"
          >增加</el-button>
          <el-button
            v-hasPermi="['xms:stakeHostingUserAmountSummary:decrease']"
            size="mini"
            type="text"
            icon="el-icon-minus"
            @click="handleAdjust('decrease')"
          >扣除</el-button>
          <el-button
            v-hasPermi="['xms:stakeHostingUserAmountSummary:edit']"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleRemark(scope.row)"
          >备注</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :close-on-click-modal="false" :title="adjustTitle" :visible.sync="adjustOpen" append-to-body width="500px">
      <el-form ref="adjustForm" :model="adjustForm" :rules="adjustRules" label-width="100px">
        <el-form-item label="调整金额" prop="amount">
          <el-input v-model="adjustForm.amount" placeholder="请输入调整金额" type="number" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitAdjust">确 定</el-button>
        <el-button @click="cancelAdjust">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :close-on-click-modal="false" title="修改备注" :visible.sync="remarkOpen" append-to-body width="500px">
      <el-form ref="remarkForm" :model="remarkForm" label-width="100px">
        <el-form-item label="备注" prop="remark">
          <el-input v-model="remarkForm.remark" placeholder="请输入备注" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitRemark">确 定</el-button>
        <el-button @click="cancelRemark">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listStakeHostingUserAmountSummary,
  manualAdjustStakeHostingUserAmount,
  updateStakeHostingUserAmountRemark
} from '@/api/xms/stakeHostingUserAmountSummary'

export default {
  name: 'StakeHostingUserAmountSummary',
  data() {
    const validateAmount = (rule, value, callback) => {
      const amount = Number(value)
      if (!value && value !== 0) {
        callback(new Error('请输入调整金额'))
      } else if (amount <= 0) {
        callback(new Error('调整金额必须大于0'))
      } else {
        callback()
      }
    }
    return {
      loading: true,
      total: 0,
      summaryList: [],
      adjustOpen: false,
      remarkOpen: false,
      adjustType: 'increase',
      adjustTitle: '',
      queryParams: {
        pageNum: 1,
        pageSize: 10
      },
      adjustForm: {
        amount: null
      },
      remarkForm: {
        remark: null
      },
      adjustRules: {
        amount: [{ validator: validateAmount, trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listStakeHostingUserAmountSummary(this.queryParams).then(response => {
        this.summaryList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    resetAdjust() {
      this.adjustForm = {
        amount: null
      }
      this.resetForm('adjustForm')
    },
    resetRemark() {
      this.remarkForm = {
        remark: null
      }
      this.resetForm('remarkForm')
    },
    handleAdjust(type) {
      this.resetAdjust()
      this.adjustType = type
      this.adjustTitle = type === 'increase' ? '手动增加全平台托管累计金额' : '手动扣除全平台托管累计金额'
      this.adjustOpen = true
    },
    handleRemark(row) {
      this.resetRemark()
      this.remarkForm = {
        remark: row.remark
      }
      this.remarkOpen = true
    },
    cancelAdjust() {
      this.adjustOpen = false
      this.resetAdjust()
    },
    cancelRemark() {
      this.remarkOpen = false
      this.resetRemark()
    },
    submitAdjust() {
      this.$refs.adjustForm.validate(valid => {
        if (!valid) {
          return
        }
        const amount = String(this.adjustForm.amount)
        const adjustAmount = this.adjustType === 'increase' ? amount : '-' + amount
        manualAdjustStakeHostingUserAmount({
          amount: adjustAmount
        }).then(() => {
          this.$modal.msgSuccess(this.adjustType === 'increase' ? '增加成功' : '扣除成功')
          this.adjustOpen = false
          this.getList()
        })
      })
    },
    submitRemark() {
      updateStakeHostingUserAmountRemark(this.remarkForm).then(() => {
        this.$modal.msgSuccess('备注修改成功')
        this.remarkOpen = false
        this.getList()
      })
    }
  }
}
</script>
