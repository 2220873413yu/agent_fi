<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="用户ID" label-width="100px" prop="userId">
        <el-input
          v-model="queryParams.userId"
          clearable
          oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
          placeholder="请输入用户用户ID"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>

      <el-form-item label="提现单号" label-width="100px" prop="code">
        <el-input
          v-model="queryParams.code"
          clearable
          placeholder="请输入提现单号"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>

      <el-form-item label="交易hash" label-width="100px" prop="remark">
        <el-input
          v-model="queryParams.remark"
          clearable
          placeholder="请输入交易hash"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>


<!--      <el-form-item label="团队长账号" prop="teamUserAccount" label-width="100px">-->
<!--        <el-input-->
<!--          v-model="queryParams.teamUserAccount"-->
<!--          placeholder="请输入团队长账号"-->
<!--          clearable-->
<!--          @keyup.enter.native="handleQuery"-->
<!--        />-->
<!--      </el-form-item>-->

      <el-form-item label="状态" label-width="100px" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
          <el-option
            v-for="dict in dict.type.t_withdrawal_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

<!--      <el-form-item label="链" prop="chainId">
        <el-select v-model="queryParams.chainId" placeholder="请选择状态" clearable>
          <el-option
            v-for="dict in dict.type.chain_type_enum"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>-->
<!--      <el-form-item label="币种" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择币种" clearable>
          <el-option
            v-for="dict in dict.type.t_withdrawal_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>-->
<!--      <el-form-item label="提现地址" prop="address">
        <el-input
          v-model="queryParams.address"
          placeholder="请输入提现地址"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->
      <el-form-item label="创建时间" label-width="100px">
        <el-date-picker
          v-model="daterangeCreateTime"
          end-placeholder="结束日期"
          range-separator="-"
          start-placeholder="开始日期"
          style="width: 240px"
          type="datetimerange"
          value-format="yyyy-MM-dd HH:mm:ss"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 币安钱包余额显示区域 -->
<!--    <el-row class="wallet-balance-container mb8">
      <el-col :span="24">
        <div class="wallet-balance-card">
          <div class="wallet-icon">
            <i class="el-icon-wallet"></i>
          </div>
          <div class="wallet-info">
            <div class="wallet-label">币安资金账户钱包余额 (USDT)</div>
            <div class="wallet-amount">
              <span class="amount-number">{{ walletFundingBalance || '加载中...' }}</span>
              <span class="amount-currency">USDT</span>
            </div>
          </div>
          <div class="wallet-actions">
            <el-button
              type="primary"
              size="mini"
              icon="el-icon-refresh"
              @click="refreshBalance"
              :loading="balanceLoading"
            >刷新余额</el-button>
          </div>
        </div>
      </el-col>
    </el-row>-->

    <el-row :gutter="10" class="mb8">
      <!-- <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['xms:withdrawal:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['xms:withdrawal:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['xms:withdrawal:remove']"
        >删除</el-button>
      </el-col>-->
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:withdrawal:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="withdrawalList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55" />
      <!-- <el-table-column label="主键id" align="center" prop="id" /> -->
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template v-if="scope.row.status === 0" slot-scope="scope">
          <el-button
            v-if="scope.row.status==0"
            v-hasPermi="['xms:withdrawal:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >审核</el-button>
          <!--          <el-button
                      size="mini"
                      type="text"
                      icon="el-icon-delete"
                      @click="handleDelete(scope.row)"
                      v-hasPermi="['xms:withdrawal:remove']"
                    >删除</el-button>-->
        </template>
      </el-table-column>
      <el-table-column align="center" label="用户ID" prop="userId"/>
      <el-table-column align="center" label="钱包地址" prop="userAccount" />
      <!--      <el-table-column label="用户账号" align="center" prop="userAccount"/>-->
      <el-table-column align="center" label="提现单号" prop="code" />
      <el-table-column align="center" label="提现币种" prop="coinType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_money_log_coin_type" :value="scope.row.coinType"/>
        </template>
      </el-table-column>

<!--      <el-table-column label="链" align="center" prop="chainId">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.chain_type_enum" :value="scope.row.chainId"/>
        </template>
      </el-table-column>-->
      <el-table-column align="center" label="提现额度" prop="changeBalance">
        <template slot-scope="scope">
          {{scope.row.changeBalance}}
        </template>
      </el-table-column>

      <el-table-column align="center" label="手续费" prop="feeBalance" >
        <template slot-scope="scope">
          {{scope.row.feeBalance}}
        </template>
      </el-table-column>
      <el-table-column align="center" label="到账金额"  >
        <template slot-scope="scope">
          {{scope.row.changeBalance - scope.row.feeBalance}}
        </template>
      </el-table-column>
<!--      <el-table-column align="center" label="手续费率" prop="feeRatio" >
        <template slot-scope="scope">
          {{scope.row.feeRatio}}%
        </template>
      </el-table-column> -->

      <el-table-column align="center" label="手续费率" prop="systemFeeRatio" >
        <template slot-scope="scope">
          {{scope.row.feeRatio}}%
        </template>
      </el-table-column>


      <el-table-column align="center" label="状态" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_withdrawal_status" :value="scope.row.status"/>
        </template>
      </el-table-column>

      <el-table-column align="center" label="交易hash" prop="remark" />
      <el-table-column align="center" label="创建时间" prop="createTime" >
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="提现到账日期" prop="creditedTime" >
        <template slot-scope="scope">
           <span>{{ parseTime(scope.row.creditedTime) }}</span>
        </template>
      </el-table-column>

      <el-table-column align="center" label="修改时间" prop="updateTime" >
        <template slot-scope="scope">
           <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>

    </el-table>

    <pagination
      v-show="total>0"
      :limit.sync="queryParams.pageSize"
      :page.sync="queryParams.pageNum"
      :total="total"
      @pagination="getList"
    />

    <!-- 添加或修改提现对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="用户UID" prop="userId">
          <el-input v-model="form.userId" disabled placeholder="请输入用户UID"/>
        </el-form-item>
        <el-form-item label="提现单号" prop="code">
          <el-input v-model="form.code" disabled placeholder="请输入提现单号"/>
        </el-form-item>

        <el-form-item label="提现金额" prop="changeBalance">
          <el-input v-model="form.changeBalance" disabled placeholder="请输入"/>
        </el-form-item>

        <el-form-item label="提现手续费" prop="feeBalance">
          <el-input v-model="form.feeBalance" disabled placeholder="请输入"/>
        </el-form-item>

        <el-form-item label="审核" prop="status">
          <el-radio-group v-model="form.status" size="medium">
            <el-radio v-for="(item, index) in statusStant" :key="index" :disabled="item.disabled"
                      :label="item.value">{{item.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入内容" type="textarea" />
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
import { listWithdrawal,
  getWithdrawal, delWithdrawal, addWithdrawal, updateWithdrawal } from "@/api/xms/withdrawal";

export default {
  name: "Withdrawal",
  dicts: ['t_withdrawal_status', 't_user_money_log_coin_type', 't_withdrawal_type','t_recharge_payment_config_type','chain_type_enum'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 提现表格数据
      withdrawalList: [],
      walletFundingBalance: null,
      balanceLoading: false,
      statusStant: [{
        "label": "审核通过",
        "value": 1
      }, {
        "label": "审核拒绝",
        "value": 2
      }],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否删除时间范围
      daterangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: null,
        teamUserAccount: null,
        code: null,
        userAccount: null,
        userCode: null,
        changeBalance: null,
        feeBalance: null,
        status: null,
        chainId: null,
        type: null,
        address: null,
        createTime: null,
        activeFlag: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        userId: [
          { required: true, message: "用户UID不能为空", trigger: "blur" }
        ],
        createTime: [
          { required: true, message: "创建时间不能为空", trigger: "blur" }
        ],
        status: [
          { required: true, message: "审核状态不能为空", trigger: "blur" }
        ],
        activeFlag: [
          { required: true, message: "是否删除不能为空", trigger: "blur" }
        ]
      },
      isCounting: false,
      countDown: 30,
      countText: "获取验证码",
      intervalId: null
    };
  },
  created() {
    // 检查是否有团队用户账号参数
    if (this.$route.query.teamUserAccount) {
      this.queryParams.teamUserAccount = this.$route.query.teamUserAccount;
      console.log('接收到团队用户账号参数:', this.$route.query.teamUserAccount);
    }
    console.log('查询参数:', this.queryParams);
    this.getList();
  },
  methods: {
    /** 查询提现列表 */
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      if (null != this.daterangeCreateTime && '' != this.daterangeCreateTime) {
        this.queryParams.params["beginCreateTime"] = this.daterangeCreateTime[0];
        this.queryParams.params["endCreateTime"] = this.daterangeCreateTime[1];
      }
      console.log('发送查询请求，参数:', this.queryParams);
      listWithdrawal(this.queryParams).then(response => {
        console.log('查询响应:', response);
        this.withdrawalList = response.rows;
        this.total = response.total;
        this.loading = false;
      }).catch(error => {
        console.error('查询失败:', error);
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        userId: null,
        code: null,
        changeBalance: null,
        feeBalance: null,
        status: null,
        type: null,
        address: null,
        remark: null,
        createTime: null,
        updateTime: null,
        activeFlag: null,
        autoCode: null,
        actualAmount: null
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.daterangeCreateTime = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加提现";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getWithdrawal(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改提现";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateWithdrawal(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addWithdrawal(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除提现编号为"' + ids + '"的数据项？').then(function() {
        return delWithdrawal(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/withdrawal/export', {
        ...this.queryParams
      }, `withdrawal_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>

<style scoped>
.el-input {
  font-size: 14px;
}

.el-button {
  border-radius: 4px;
  font-weight: 500;
}

.el-form-item {
  margin-bottom: 22px;
}

/* 钱包余额卡片样式 */
.wallet-balance-container {
  margin-bottom: 20px;
}

.wallet-balance-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.2);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  position: relative;
  overflow: hidden;
}

.wallet-balance-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%);
  pointer-events: none;
}

.wallet-balance-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.3);
}

.wallet-icon {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  backdrop-filter: blur(10px);
}

.wallet-icon i {
  font-size: 28px;
  color: white;
}

.wallet-info {
  flex: 1;
  color: white;
}

.wallet-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
  font-weight: 500;
}

.wallet-amount {
  display: flex;
  align-items: baseline;
  margin-bottom: 8px;
}

.amount-number {
  font-size: 32px;
  font-weight: 700;
  margin-right: 8px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.amount-currency {
  font-size: 18px;
  font-weight: 500;
  opacity: 0.8;
}

.wallet-status {
  display: flex;
  align-items: center;
  font-size: 12px;
  opacity: 0.8;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
  animation: pulse 2s infinite;
}

.status-dot.online {
  background: #52c41a;
  box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.7);
}

.status-dot.offline {
  background: #faad14;
  box-shadow: 0 0 0 0 rgba(250, 173, 20, 0.7);
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(82, 196, 26, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(82, 196, 26, 0);
  }
}

.wallet-actions {
  margin-left: 20px;
}

.wallet-actions .el-button {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.wallet-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-1px);
}

.wallet-actions .el-button:active {
  transform: translateY(0);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .wallet-balance-card {
    flex-direction: column;
    text-align: center;
    padding: 16px;
  }

  .wallet-icon {
    margin-right: 0;
    margin-bottom: 16px;
  }

  .wallet-actions {
    margin-left: 0;
    margin-top: 16px;
  }

  .amount-number {
    font-size: 28px;
  }
}
</style>
