<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="币种" prop="coinType">
        <el-select v-model="queryParams.coinType" clearable placeholder="请选择币种">
          <el-option
            v-for="dict in dict.type.t_user_money_log_coin_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
<!--      <el-form-item label="币种编码" prop="coinCode">
        <el-input
          v-model="queryParams.coinCode"
          placeholder="请输入币种编码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="提现开关(1:开,2:关)" prop="withdrawOpen">
        <el-input
          v-model="queryParams.withdrawOpen"
          placeholder="请输入提现开关(1:开,2:关)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最小提现金额" prop="minWithdrawAmount">
        <el-input
          v-model="queryParams.minWithdrawAmount"
          placeholder="请输入最小提现金额"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手续费率(例如:5表示5%)" prop="feeRatio">
        <el-input
          v-model="queryParams.feeRatio"
          placeholder="请输入手续费率(例如:5表示5%)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->
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
          v-hasPermi="['xms:withdrawalConfig:add']"
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
          v-hasPermi="['xms:withdrawalConfig:edit']"
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
          v-hasPermi="['xms:withdrawalConfig:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['xms:withdrawalConfig:export']"
        >导出</el-button>
      </el-col>-->
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="withdrawalConfigList" @selection-change="handleSelectionChange">
<!--      <el-table-column type="selection" width="55" align="center" />-->
      <el-table-column v-if="false" align="center" label="主键id" prop="id"/>
      <el-table-column align="center" label="币种" prop="coinType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_money_log_coin_type" :value="scope.row.coinType"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="提现开关" prop="withdrawOpen" >
      <template slot-scope="scope">
        <dict-tag :options="dict.type.t_withdrawal_config_open" :value="scope.row.withdrawOpen"/>
      </template>
      </el-table-column>
      <el-table-column align="center" label="最小提现金额" prop="minWithdrawAmount" />
      <el-table-column align="center" label="单日提现免审核额度" prop="withdrawLimit" />
      <el-table-column align="center" label="单日免审核次数" prop="dailyFreeAuditCount" />
      <el-table-column align="center" label="提现手续费" prop="feeRatio" >
        <template slot-scope="scope">
          {{scope.row.feeRatio}}%
        </template>
      </el-table-column>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['xms:withdrawalConfig:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
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

    <!-- 添加或修改提现配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="币种" prop="coinType">
          <el-select v-model="form.coinType" placeholder="请选择币种">
            <el-option
              v-for="dict in dict.type.t_user_money_log_coin_type"
              :key="dict.value"
              :disabled="true"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="提现开关" prop="withdrawOpen">
          <el-select v-model="form.withdrawOpen" placeholder="请选择提现开关">
            <el-option
              v-for="dict in dict.type.t_withdrawal_config_open"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="最小提现金额" prop="minWithdrawAmount">
          <el-input v-model="form.minWithdrawAmount" oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
                    placeholder="请输入最小提现金额"
          />
        </el-form-item>

        <el-form-item label="单日提现免审核额度" prop="withdrawLimit">
          <el-input v-model="form.withdrawLimit" oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
                    placeholder="请输入单日提现免审核额度"
          />
        </el-form-item>

        <el-form-item label="单日免审核次数" prop="dailyFreeAuditCount">
          <el-input v-model="form.dailyFreeAuditCount"
                    oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
                    placeholder="请输入单日免审核次数"
          />
        </el-form-item>

        <el-form-item label="提现手续费" prop="feeRatio">
          <el-input
            v-model="form.feeRatio"
            maxlength="6"
            placeholder="请输入手续费率"
            @input="value => handleFeeRatioInput('feeRatio', value)"
          />
          <div class="form-tip">以百分比为单位，例如：1 表示 1%</div>
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
import { listWithdrawalConfig, getWithdrawalConfig, delWithdrawalConfig, addWithdrawalConfig, updateWithdrawalConfig } from "@/api/xms/withdrawalConfig";

export default {
  name: "WithdrawalConfig",
  dicts: ['t_user_money_log_coin_type','t_withdrawal_config_open'],
  data() {
    const feeRatioValidator = (rule, value, callback) => {
      if (value === null || value === undefined || value === "") {
        callback();
        return;
      }
      const pattern = /^\d+(\.\d{1,2})?$/;
      if (!pattern.test(String(value))) {
        callback(new Error("最多两位小数"));
        return;
      }
      callback();
    };

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
      // 提现配置表格数据
      withdrawalConfigList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        coinType: null,
        coinCode: null,
        withdrawOpen: null,
        minWithdrawAmount: null,
        feeRatio: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        coinType: [
          { required: true, message: "币种不能为空", trigger: "change" }
        ],
        withdrawOpen: [
          { required: true, message: "提现开关不能为空", trigger: "blur" }
        ],
        dailyFreeAuditCount: [
          { required: true, message: "单日免审核次数不能为空", trigger: "blur" }
        ],
        withdrawLimit: [
          { required: true, message: "单日提现免审核额度不能为空", trigger: "blur" }
        ],
        minWithdrawAmount: [
          { required: true, message: "最小提现金额不能为空", trigger: "blur" }
        ],
        feeRatio: [
          { required: true, message: "提现手续费率不能为空", trigger: "blur" },
          { validator: feeRatioValidator, trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询提现配置列表 */
    getList() {
      this.loading = true;
      listWithdrawalConfig(this.queryParams).then(response => {
        this.withdrawalConfigList = response.rows;
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
        coinType: null,
        withdrawOpen: null,
        dailyFreeAuditCount: null,
        withdrawLimit: null,
        minWithdrawAmount: null,
        feeRatio: null,
        createTime: null,
        updateTime: null,
        remark: null
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
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加提现配置";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getWithdrawalConfig(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改提现配置";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateWithdrawalConfig(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addWithdrawalConfig(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除提现配置编号为"' + ids + '"的数据项？').then(function() {
        return delWithdrawalConfig(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleFeeRatioInput(field, value) {
      this.form[field] = this.normalizeFeeRatio(value);
    },
    normalizeFeeRatio(value) {
      if (value === null || value === undefined) {
        return "";
      }
      const raw = String(value).replace(/[^\d.]/g, "");
      if (!raw) {
        return "";
      }
      const endsWithDot = raw.endsWith(".");
      const parts = raw.split(".");
      let intPart = parts[0] || "0";
      intPart = intPart.replace(/^0+(?=\d)/, "");
      if (intPart === "") {
        intPart = "0";
      }
      let decimalPart = parts[1] || "";
      decimalPart = decimalPart.slice(0, 2);
      if (endsWithDot && decimalPart === "") {
        return `${intPart}.`;
      }
      return decimalPart ? `${intPart}.${decimalPart}` : intPart;
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/withdrawalConfig/export', {
        ...this.queryParams
      }, `withdrawalConfig_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
