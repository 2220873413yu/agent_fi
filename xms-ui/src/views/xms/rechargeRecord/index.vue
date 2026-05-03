<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="用户ID" prop="userId">
        <el-input
          v-model="queryParams.userId"
          clearable
          oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
          placeholder="请输入用户ID"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
<!--      <el-form-item label="用户编码" label-width="100px" prop="userAccount">
        <el-input
          v-model="queryParams.userAccount"
          clearable
          placeholder="请输入用户编码"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->

      <el-form-item label="订单号" label-width="100px" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          clearable
          placeholder="请输入订单号"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="交易hash" label-width="100px" prop="txId">
        <el-input
          v-model="queryParams.txId"
          clearable
          placeholder="请输入hash"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>

      <el-form-item label="钱包地址" label-width="100px" prop="remark">
        <el-input
          v-model="queryParams.remark"
          clearable
          placeholder="请输入钱包地址"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
<!--      <el-form-item label="充值类型" prop="coinType" label-width="100px">
        <el-select v-model="queryParams.coinType" placeholder="请选择" clearable>
          <el-option
            v-for="dict in dict.type.t_user_money_log_coin_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>-->

<!--      <el-form-item label="链id" prop="chainId" label-width="100px">
        <el-select v-model="queryParams.chainId" placeholder="请选择链" clearable>
          <el-option
            v-for="dict in dict.type.chain_type_enum"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>-->

        <!--      <el-form-item label="充值金额(原始金额)" prop="rechargeAmount">
                <el-input
                  v-model="queryParams.rechargeAmount"
                  placeholder="请输入充值金额(原始金额)"
                  clearable
                  @keyup.enter.native="handleQuery"
                />
              </el-form-item>
              <el-form-item label="人民币兑USDT汇率" prop="usdtRate">
                <el-input
                  v-model="queryParams.usdtRate"
                  placeholder="请输入人民币兑USDT汇率"
                  clearable
                  @keyup.enter.native="handleQuery"
                />
              </el-form-item>
              <el-form-item label="USDT金额" prop="usdtAmount">
                <el-input
                  v-model="queryParams.usdtAmount"
                  placeholder="请输入USDT金额"
                  clearable
                  @keyup.enter.native="handleQuery"
                />
              </el-form-item>
              <el-form-item label="USDT兑GCT汇率" prop="ptbRate">
                <el-input
                  v-model="queryParams.ptbRate"
                  placeholder="请输入USDT兑GCT汇率"
                  clearable
                  @keyup.enter.native="handleQuery"
                />
              </el-form-item>
              <el-form-item label="最终GCT金额" prop="ptbAmount">
                <el-input
                  v-model="queryParams.ptbAmount"
                  placeholder="请输入最终GCT金额"
                  clearable
                  @keyup.enter.native="handleQuery"
                />
              </el-form-item>-->
      <!--      <el-form-item label="支付凭证图片" prop="proofImg">
              <el-input
                v-model="queryParams.proofImg"
                placeholder="请输入支付凭证图片"
                clearable
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item label="审核时间" prop="auditTime">
              <el-date-picker clearable
                v-model="queryParams.auditTime"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择审核时间">
              </el-date-picker>
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

    <el-row :gutter="10" class="mb8">
      <!--      <el-col :span="1.5">
              <el-button
                type="primary"
                plain
                icon="el-icon-plus"
                size="mini"
                @click="handleAdd"
                v-hasPermi="['xms:rechargeRecord:add']"
              >新增</el-button>
            </el-col>-->
<!--      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['xms:rechargeRecord:edit']"
        >修改
        </el-button>
      </el-col>-->
      <!--      <el-col :span="1.5">
              <el-button
                type="danger"
                plain
                icon="el-icon-delete"
                size="mini"
                :disabled="multiple"
                @click="handleDelete"
                v-hasPermi="['xms:rechargeRecord:remove']"
              >删除</el-button>
            </el-col>-->
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:rechargeRecord:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="rechargeRecordList" @selection-change="handleSelectionChange">
<!--      <el-table-column type="selection" width="55" align="center"/>-->
<!--      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-if="scope.row.status === 0"
            v-hasPermi="['xms:rechargeRecord:edit']"
          >审核
          </el-button>
        </template>
      </el-table-column>-->
      <el-table-column v-if="false" align="center" label="主键id" prop="id"/>
      <el-table-column align="center" label="用户ID" prop="userId"/>
<!--      <el-table-column align="center" label="用户编码" prop="userCode"/>-->
      <el-table-column align="center" label="钱包地址" prop="remark"/>
      <el-table-column align="center" label="订单号" prop="orderNo"/>
      <el-table-column align="center" label="充值金额" prop="rechargeAmount">
        <template slot-scope="scope">
          <span>{{ scope.row.rechargeAmount }}</span>
        </template>
      </el-table-column>
<!--      <el-table-column label="到账数量" align="center" prop="rechargeAmount">
      <template slot-scope="scope">
        <span>{{ scope.row.rechargeValidNum2 }} MAI</span>
      </template>
      </el-table-column>-->
      <el-table-column align="center" label="币种" prop="coinType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_money_log_coin_type" :value="scope.row.coinType"/>
        </template>
      </el-table-column>
<!--
      <el-table-column align="center" label="充值状态" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_recharge_record_status" :value="scope.row.status"/>
        </template>
      </el-table-column>-->

      <el-table-column align="center" label="交易hash" prop="txId" width="300"/>
      <el-table-column align="center" label="创建时间" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
<!--      <el-table-column align="center" label="更新时间" prop="updateTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>-->

    </el-table>

    <pagination
      v-show="total>0"
      :limit.sync="queryParams.pageSize"
      :page.sync="queryParams.pageNum"
      :total="total"
      @pagination="getList"
    />

    <!-- 添加或修改充值记录对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户UID" prop="userId">
          <el-input v-model="form.userId" disabled placeholder="请输入用户UID"/>
        </el-form-item>
        <el-form-item label="充值订单号" prop="orderNo">
          <el-input v-model="form.orderNo" disabled placeholder="请输入充值订单号"/>
        </el-form-item>
        <el-form-item label="充值类型" prop="rechargeType">
          <el-select v-model="form.rechargeType" disabled placeholder="请选择充值类型">
            <el-option
              v-for="dict in dict.type.t_recharge_payment_config_type"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="充值信息">
          <el-card class="exchange-form-card">
            <div  v-if="form.rechargeType == 1">
              <div class="exchange-form-item">
                <span class="exchange-label">充值金额：</span>
                <el-input
                  v-model="form.rechargeAmount"
                  class="exchange-input"
                  disabled
                  placeholder="请输入充值金额"
                >
                  <template slot="append">CNY</template>
                </el-input>
              </div>

              <div class="exchange-form-item">
                <span class="exchange-label">USDT汇率：</span>
                <el-input
                  v-model="form.usdtRate"
                  class="exchange-input"
                  disabled
                  placeholder="请输入汇率"
                >
                  <template slot="prepend">1 USDT =</template>
                  <template slot="append">CNY</template>
                </el-input>
              </div>
              <div class="exchange-form-item">
                <span class="exchange-label">USDT金额：</span>
                <el-input
                  v-model="form.usdtAmount"
                  class="exchange-input"
                  disabled
                  placeholder="请输入USDT金额"
                >
                  <template slot="append">USDT</template>
                </el-input>
              </div>
              <div class="exchange-form-item">
                <span class="exchange-label">GCT汇率：</span>
                <el-input
                  v-model="form.ptbRate"
                  class="exchange-input"
                  disabled
                  placeholder="请输入汇率"
                >
                  <template slot="prepend">1 USDT =</template>
                  <template slot="append">GCT</template>
                </el-input>
              </div>
              <div class="exchange-form-item">
                <span class="exchange-label">到账GCT：</span>
                <el-input
                  v-model="form.ptbAmount"
                  class="exchange-input"
                  disabled
                  placeholder="请输入GCT金额"
                >
                  <template slot="append">GCT</template>
                </el-input>
              </div>
            </div>

            <div  v-if="form.rechargeType == 2">
              <div class="exchange-form-item" >
                <span class="exchange-label">充值金额：</span>
                <el-input
                  v-model="form.usdtAmount"
                  class="exchange-input"
                  disabled
                  placeholder="请输入充值金额"
                >
                  <template slot="append">USDT</template>
                </el-input>
              </div>
              <div class="exchange-form-item">
                <span class="exchange-label">GCT汇率：</span>
                <el-input
                  v-model="form.ptbRate"
                  class="exchange-input"
                  disabled
                  placeholder="请输入汇率"
                >
                  <template slot="prepend">1 USDT =</template>
                  <template slot="append">GCT</template>
                </el-input>
              </div>
              <div class="exchange-form-item">
                <span class="exchange-label">到账GCT：</span>
                <el-input
                  v-model="form.ptbAmount"
                  class="exchange-input"
                  disabled
                  placeholder="请输入GCT金额"
                >
                  <template slot="append">GCT</template>
                </el-input>
              </div>

            </div>


          </el-card>
        </el-form-item>

        <!-- <el-form-item label="充值金额(原始金额)" prop="rechargeAmount">
          <el-input v-model="form.rechargeAmount" placeholder="请输入充值金额(原始金额)"/>
        </el-form-item>
        <el-form-item label="人民币兑USDT汇率" prop="usdtRate">
          <el-input v-model="form.usdtRate" placeholder="请输入人民币兑USDT汇率"/>
        </el-form-item>
        <el-form-item label="USDT金额" prop="usdtAmount">
          <el-input v-model="form.usdtAmount" placeholder="请输入USDT金额"/>
        </el-form-item>
        <el-form-item label="USDT兑PTB汇率" prop="ptbRate">
          <el-input v-model="form.ptbRate" placeholder="请输入USDT兑PTB汇率"/>
        </el-form-item>
        <el-form-item label="最终PTB金额" prop="ptbAmount">
          <el-input v-model="form.ptbAmount" placeholder="请输入最终PTB金额"/>
        </el-form-item> -->
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option
              v-for="dict in dict.type.t_recharge_record_status"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="交易凭证" prop="proofImg">
          <image-preview :height="150" :src="form.proofImg" :width="150"/>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注"/>
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
import {
  listRechargeRecord,
  getRechargeRecord,
  delRechargeRecord,
  addRechargeRecord,
  updateRechargeRecord
} from "@/api/xms/rechargeRecord";

export default {
  name: "RechargeRecord",
  dicts: ['t_recharge_payment_config_type', 't_recharge_record_status','t_user_money_log_coin_type'],
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
      // 充值记录表格数据
      rechargeRecordList: [],
      // 是否删除时间范围
      daterangeCreateTime: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: null,
        orderNo: null,
        userAccount: null,
        chainId:null,
        paymentConfigInfo: null,
        rechargeAmount: null,
        txId: null,
        usdtRate: null,
        remark: null,
        usdtAmount: null,
        ptbRate: null,
        ptbAmount: null,
        status: null,
        proofImg: null,
        auditTime: null,
        createTime: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        userId: [
          {required: true, message: "用户UID不能为空", trigger: "blur"}
        ],
        orderNo: [
          {required: true, message: "充值订单号不能为空", trigger: "blur"}
        ],
        paymentConfigInfo: [
          {required: true, message: "收款信息(保存当时收款的快照信息)不能为空", trigger: "blur"}
        ],
        rechargeAmount: [
          {required: true, message: "充值金额(原始金额)不能为空", trigger: "blur"}
        ],
        usdtRate: [
          {required: true, message: "人民币兑USDT汇率不能为空", trigger: "blur"}
        ],
        usdtAmount: [
          {required: true, message: "USDT金额不能为空", trigger: "blur"}
        ],
        ptbRate: [
          {required: true, message: "USDT兑GCT汇率不能为空", trigger: "blur"}
        ],
        ptbAmount: [
          {required: true, message: "最终GCT金额不能为空", trigger: "blur"}
        ],
        status: [
          {required: true, message: "状态(0:待审核 1:已通过 2:已拒绝)不能为空", trigger: "change"}
        ],
        deleted: [
          {required: true, message: "是否删除(0:否 1:是)不能为空", trigger: "blur"}
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    // 检查是否为有效的JSON字符串
    isJSON(str) {
      try {
        JSON.parse(str);
        return true;
      } catch (e) {
        return false;
      }
    },
    // 解析支付配置信息
    parsePaymentInfo(jsonStr) {
      try {
        return JSON.parse(jsonStr);
      } catch (e) {
        return {};
      }
    },
    /** 查询充值记录列表 */
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      if (null != this.daterangeCreateTime && '' != this.daterangeCreateTime) {
        this.queryParams.params["beginCreateTime"] = this.daterangeCreateTime[0];
        this.queryParams.params["endCreateTime"] = this.daterangeCreateTime[1];
      }
      listRechargeRecord(this.queryParams).then(response => {
        this.rechargeRecordList = response.rows;
        this.total = response.total;
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
        orderNo: null,
        paymentConfigInfo: null,
        rechargeType: null,
        chainId: null,
        rechargeAmount: null,
        usdtRate: null,
        usdtAmount: null,
        ptbRate: null,
        ptbAmount: null,
        status: null,
        proofImg: null,
        remark: null,
        auditTime: null,
        createTime: null,
        createBy: null,
        updateTime: null,
        updateBy: null,
        deleted: null
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
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加充值记录";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getRechargeRecord(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改充值记录";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateRechargeRecord(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addRechargeRecord(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除充值记录编号为"' + ids + '"的数据项？').then(function () {
        return delRechargeRecord(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/rechargeRecord/export', {
        ...this.queryParams
      }, `rechargeRecord_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
<style scoped>
.exchange-info {
  padding: 8px;
  line-height: 1.5;
}

.exchange-info p {
  margin: 4px 0;
  color: #606266;
}

.exchange-form-card {
  margin: 10px 0;
  background-color: #f8f9fb;
}

.exchange-form-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.exchange-form-item:last-child {
  margin-bottom: 0;
}

.exchange-label {
  width: 100px;
  text-align: right;
  padding-right: 12px;
  color: #606266;
}

.exchange-input {
  flex: 1;
}
</style>
