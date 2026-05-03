<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
<!--
      <el-form-item label="区间起始(含)" prop="startIndex">
        <el-input
          v-model="queryParams.startIndex"
          placeholder="请输入区间起始(含)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="区间结束(含)" prop="endIndex">
        <el-input
          v-model="queryParams.endIndex"
          placeholder="请输入区间结束(含)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="质押金额(DFC)" prop="stakeAmount">
        <el-input
          v-model="queryParams.stakeAmount"
          placeholder="请输入质押金额(DFC)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="日产出(DFC)" prop="dayReward">
        <el-input
          v-model="queryParams.dayReward"
          placeholder="请输入日产出(DFC)"
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
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:miningPackageTier:add']"
          icon="el-icon-plus"
          plain
          size="mini"
          type="primary"
          @click="handleAdd"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:miningPackageTier:edit']"
          :disabled="single"
          icon="el-icon-edit"
          plain
          size="mini"
          type="success"
          @click="handleUpdate"
        >修改</el-button>
      </el-col>
<!--      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['xms:miningPackageTier:remove']"
        >删除</el-button>
      </el-col>-->
<!--      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:miningPackageTier:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出</el-button>
      </el-col>-->
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="miningPackageTierList" @selection-change="handleSelectionChange">
<!--      <el-table-column type="selection" width="55" align="center" />-->
      <el-table-column v-if="false" align="center" label="主键id" prop="id"/>
      <el-table-column align="center" label="区间起始(含)" prop="startIndex" />
      <el-table-column align="center" label="区间结束(含)" prop="endIndex" />
      <el-table-column align="center" label="质押金额" prop="stakeAmount">
        <template slot-scope="scope">
         {{ scope.row.stakeAmount }} DFC
        </template>
      </el-table-column>
      <el-table-column align="center" label="日产出" prop="dayReward">
      <template slot-scope="scope">
        {{ scope.row.dayReward }} DFC
      </template>
      </el-table-column>
      <el-table-column align="center" label="创建时间" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="修改时间" prop="updateTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
<!--      <el-table-column label="备注" align="center" prop="remark" />-->
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['xms:miningPackageTier:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            v-hasPermi="['xms:miningPackageTier:remove']"
            icon="el-icon-delete"
            size="mini"
            type="text"
            @click="handleDelete(scope.row)"
          >删除</el-button>
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

    <!-- 添加或修改矿机质押区间配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="区间起始(含)" prop="startIndex">
          <el-input v-model="form.startIndex"
                    oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
                    placeholder="请输入区间起始" />
        </el-form-item>
        <el-form-item label="区间结束(含)" prop="endIndex">
          <el-input v-model="form.endIndex"
                    oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
                    placeholder="请输入区间结束" />
        </el-form-item>
        <el-form-item label="质押金额(DFC)" prop="stakeAmount">
          <el-input v-model="form.stakeAmount"
                    maxlength="10" placeholder="请输入质押金额"
                    show-word-limit
                    @input="handleStakeAmountInput" />
        </el-form-item>
        <el-form-item label="日产出(DFC)" prop="dayReward">
          <el-input v-model="form.dayReward"
                    maxlength="10" placeholder="请输入日产出"
                    show-word-limit
                    @input="handleDayRewardInput" />
        </el-form-item>
<!--        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>-->
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listMiningPackageTier, getMiningPackageTier, delMiningPackageTier, addMiningPackageTier, updateMiningPackageTier } from "@/api/xms/miningPackageTier";

export default {
  name: "MiningPackageTier",
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
      // 矿机质押区间配置表格数据
      miningPackageTierList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        startIndex: null,
        endIndex: null,
        stakeAmount: null,
        dayReward: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        startIndex: [
          { required: true, message: "区间起始(含)不能为空", trigger: "blur" }
        ],
        endIndex: [
          { required: true, message: "区间结束(含)不能为空", trigger: "blur" }
        ],
        stakeAmount: [
          { required: true, message: "质押金额(DFC)不能为空", trigger: "blur" }
        ],
        dayReward: [
          { required: true, message: "日产出(DFC)不能为空", trigger: "blur" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    handleStakeAmountInput(value) {
      this.form.stakeAmount = this.normalizeFeeRatio(value);
    },
    handleDayRewardInput(value) {
      this.form.dayReward = this.normalizeFeeRatio(value);
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
    /** 查询矿机质押区间配置列表 */
    getList() {
      this.loading = true;
      listMiningPackageTier(this.queryParams).then(response => {
        this.miningPackageTierList = response.rows;
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
        startIndex: null,
        endIndex: null,
        stakeAmount: null,
        dayReward: null,
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
      this.title = "添加矿机质押区间配置";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getMiningPackageTier(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改矿机质押区间配置";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateMiningPackageTier(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addMiningPackageTier(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除矿机质押区间配置编号为"' + ids + '"的数据项？').then(function() {
        return delMiningPackageTier(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/miningPackageTier/export', {
        ...this.queryParams
      }, `miningPackageTier_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
