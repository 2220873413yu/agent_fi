<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="规格模板名称" prop="ruleName">
        <el-input
          v-model="queryParams.ruleName"
          placeholder="请输入规格模板名称"
          clearable
          @keyup.enter.native="handleQuery"
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
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['xms:diyStoreProductRule:add']"
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
          v-hasPermi="['xms:diyStoreProductRule:edit']"
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
          v-hasPermi="['xms:diyStoreProductRule:remove']"
        >删除</el-button>
      </el-col>-->
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['xms:diyStoreProductRule:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="diyStoreProductRuleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="id" align="center" prop="id" v-if="false"/>
      <el-table-column label="规格名称" align="center" prop="ruleName" />
      <el-table-column label="规格名称(英文)" align="center" prop="ruleNameEn" />
      <el-table-column label="规格值" align="center" min-width="280" show-overflow-tooltip>
        <template slot-scope="scope">
          <span>{{ formatRuleValueDisplay(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="规格值(英文)" align="center" min-width="280" show-overflow-tooltip>
        <template slot-scope="scope">
          <span>{{ formatRuleValueEnDisplay(scope.row) }}</span>
        </template>
      </el-table-column>
<!--      <el-table-column label="备注" align="center" prop="remark" />-->
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
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['xms:diyStoreProductRule:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['xms:diyStoreProductRule:remove']"
          >删除</el-button>
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

    <!-- 添加或修改商品规则值对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="820px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="规格模板名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="请输入规格模板名称，如：Mac配置模板、手机规格模板" />
          <div style="color: #909399; font-size: 12px; margin-top: 4px;">
            示例：Mac配置模板（包含 CPU / 硬盘 / 颜色 / 尺寸）
          </div>
        </el-form-item>
        <el-form-item label="规格模板名称(英文)" prop="ruleNameEn">
          <el-input v-model="form.ruleNameEn" placeholder="Please enter template name, e.g. Mac Config Template" />
          <div style="color: #909399; font-size: 12px; margin-top: 4px;">
            Example: Mac Config Template (CPU / Disk / Color / Size)
          </div>
        </el-form-item>
        <el-form-item label="规格值" prop="ruleValue">
          <div style="display: flex; gap: 10px; align-items: center; margin-bottom: 8px;">
            <el-input
              v-model="singleInputValue"
              placeholder="输入单个规格值，如 m3"
              style="width: 320px;"
              @keyup.enter.native="addRuleValue"
            />
            <el-button type="primary" plain size="mini" @click="addRuleValue">添加规格值</el-button>
          </div>
          <div>
            <el-tag
              v-for="(value, valueIndex) in ruleValueList"
              :key="`${value}-${valueIndex}`"
              closable
              style="margin-right: 8px; margin-bottom: 6px;"
              @close="removeRuleValue(valueIndex)"
            >
              {{ value }}
            </el-tag>
          </div>
          <div style="color: #909399; font-size: 12px; margin-bottom: 8px;">
            示例：规格模板名称=CPU，逐个添加 m3、m3 pro、m3 max（不需要输入逗号拼接）
          </div>
        </el-form-item>
        <el-form-item label="规格值(英文)" prop="ruleValueEn">
          <div style="display: flex; gap: 10px; align-items: center; margin-bottom: 8px;">
            <el-input
              v-model="singleInputValueEn"
              placeholder="Input one english value, e.g. m3 pro"
              style="width: 320px;"
              @keyup.enter.native="addRuleValueEn"
            />
            <el-button type="primary" plain size="mini" @click="addRuleValueEn">Add Value</el-button>
          </div>
          <div>
            <el-tag
              v-for="(value, valueIndex) in ruleValueEnList"
              :key="`${value}-en-${valueIndex}`"
              closable
              style="margin-right: 8px; margin-bottom: 6px;"
              @close="removeRuleValueEn(valueIndex)"
            >
              {{ value }}
            </el-tag>
          </div>
          <div style="color: #909399; font-size: 12px; margin-bottom: 8px;">
            Example: Template=CPU, add values one by one: m3, m3 pro, m3 max
          </div>
        </el-form-item>
<!--        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>-->
<!--        <el-form-item label="是否删除 0否1是" prop="deleted">
          <el-input v-model="form.deleted" placeholder="请输入是否删除 0否1是" />
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
import { listDiyStoreProductRule, getDiyStoreProductRule, delDiyStoreProductRule, addDiyStoreProductRule, updateDiyStoreProductRule } from "@/api/xms/diyStoreProductRule";

export default {
  name: "DiyStoreProductRule",
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
      // 商品规则值表格数据
      diyStoreProductRuleList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        ruleName: null,
        ruleValue: null,
      },
      // 表单参数
      form: {},
      // 规格值列表（单个输入逐个添加）
      ruleValueList: [],
      // 英文规格值列表（单个输入逐个添加）
      ruleValueEnList: [],
      // 规格值输入框
      singleInputValue: "",
      // 英文规格值输入框
      singleInputValueEn: "",
      // 表单校验
      rules: {
        ruleName: [
          { required: true, message: "规格模板名称不能为空", trigger: "blur" }
        ],
        ruleNameEn: [
          { required: true, message: "规格模板名称(英文)不能为空", trigger: "blur" }
        ],
        ruleValue: [
          {
            validator: (_rule, _value, callback) => {
              if (!Array.isArray(this.ruleValueList) || this.ruleValueList.length === 0) {
                callback(new Error("请至少添加一个规格值"));
                return;
              }
              callback();
            },
            trigger: "change"
          }
        ],
        ruleValueEn: [
          {
            validator: (_rule, _value, callback) => {
              if (!Array.isArray(this.ruleValueEnList) || this.ruleValueEnList.length === 0) {
                callback(new Error("请至少添加一个英文规格值"));
                return;
              }
              callback();
            },
            trigger: "change"
          }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询商品规则值列表 */
    getList() {
      this.loading = true;
      listDiyStoreProductRule(this.queryParams).then(response => {
        this.diyStoreProductRuleList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    formatRuleValueDisplay(row) {
      const ruleName = (row && row.ruleName ? row.ruleName : "").trim();
      const raw = row ? row.ruleValue : "";
      if (!raw) {
        return ruleName ? `${ruleName}：-` : "-";
      }
      try {
        const parsed = typeof raw === "string" ? JSON.parse(raw) : raw;
        let values = [];
        if (Array.isArray(parsed) && parsed.length > 0) {
          const first = parsed[0] || {};
          if (Array.isArray(first.detail)) {
            values = first.detail;
          } else if (Array.isArray(first.values)) {
            values = first.values;
          } else if (typeof first.attrValues === "string") {
            values = first.attrValues.split(",");
          } else if (typeof first === "string") {
            values = parsed;
          }
        }
        const normalized = values.map(v => (v || "").trim()).filter(Boolean);
        if (normalized.length === 0) {
          return ruleName ? `${ruleName}：-` : "-";
        }
        return `${ruleName || "规格值"}：${normalized.join(" / ")}`;
      } catch (e) {
        return String(raw);
      }
    },
    formatRuleValueEnDisplay(row) {
      const ruleName = (row && row.ruleNameEn ? row.ruleNameEn : "").trim();
      const raw = row ? row.ruleValueEn : "";
      if (!raw) {
        return ruleName ? `${ruleName}: -` : "-";
      }
      try {
        const parsed = typeof raw === "string" ? JSON.parse(raw) : raw;
        let values = [];
        if (Array.isArray(parsed) && parsed.length > 0) {
          const first = parsed[0] || {};
          if (Array.isArray(first.detail)) {
            values = first.detail;
          } else if (Array.isArray(first.values)) {
            values = first.values;
          } else if (typeof first.attrValues === "string") {
            values = first.attrValues.split(",");
          } else if (typeof first === "string") {
            values = parsed;
          }
        }
        const normalized = values.map(v => (v || "").trim()).filter(Boolean);
        if (normalized.length === 0) {
          return ruleName ? `${ruleName}: -` : "-";
        }
        return `${ruleName || "Spec Values"}: ${normalized.join(" / ")}`;
      } catch (e) {
        return String(raw);
      }
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
        ruleName: null,
        ruleNameEn: null,
        ruleValue: null,
        ruleValueEn: null,
        remark: null,
        createTime: null,
        createBy: null,
        updateTime: null,
        updateBy: null,
        deleted: null
      };
      this.ruleValueList = [];
      this.ruleValueEnList = [];
      this.singleInputValue = "";
      this.singleInputValueEn = "";
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
      this.title = "添加商品规则值";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getDiyStoreProductRule(id).then(response => {
        this.form = response.data;
        this.ruleValueList = this.parseRuleValueList(response.data.ruleValue);
        this.ruleValueEnList = this.parseRuleValueList(response.data.ruleValueEn);
        this.syncRuleValueFromList();
        this.syncRuleValueEnFromList();
        this.open = true;
        this.title = "修改商品规则值";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.syncRuleValueFromList();
      this.syncRuleValueEnFromList();
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateDiyStoreProductRule(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addDiyStoreProductRule(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除商品规则值编号为"' + ids + '"的数据项？').then(function() {
        return delDiyStoreProductRule(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/diyStoreProductRule/export', {
        ...this.queryParams
      }, `diyStoreProductRule_${new Date().getTime()}.xlsx`)
    },
    addRuleValue() {
      const value = (this.singleInputValue || "").trim();
      if (!value) {
        return;
      }
      if (value.includes(",") || value.includes("，")) {
        this.$message.warning("规格值请单个添加，不要输入逗号");
        return;
      }
      if (!Array.isArray(this.ruleValueList)) {
        this.ruleValueList = [];
      }
      if (!this.ruleValueList.includes(value)) {
        this.ruleValueList.push(value);
      }
      this.singleInputValue = "";
      this.syncRuleValueFromList();
    },
    removeRuleValue(valueIndex) {
      if (!Array.isArray(this.ruleValueList)) {
        return;
      }
      this.ruleValueList.splice(valueIndex, 1);
      this.syncRuleValueFromList();
    },
    addRuleValueEn() {
      const value = (this.singleInputValueEn || "").trim();
      if (!value) {
        return;
      }
      if (value.includes(",") || value.includes("，")) {
        this.$message.warning("English spec value must be added one by one");
        return;
      }
      if (!Array.isArray(this.ruleValueEnList)) {
        this.ruleValueEnList = [];
      }
      if (!this.ruleValueEnList.includes(value)) {
        this.ruleValueEnList.push(value);
      }
      this.singleInputValueEn = "";
      this.syncRuleValueEnFromList();
    },
    removeRuleValueEn(valueIndex) {
      if (!Array.isArray(this.ruleValueEnList)) {
        return;
      }
      this.ruleValueEnList.splice(valueIndex, 1);
      this.syncRuleValueEnFromList();
    },
    parseRuleValueList(ruleValue) {
      if (!ruleValue) {
        return [];
      }
      try {
        const parsed = typeof ruleValue === "string" ? JSON.parse(ruleValue) : ruleValue;
        if (Array.isArray(parsed) && parsed.length > 0) {
          const first = parsed[0];
          if (first && Array.isArray(first.detail)) {
            return first.detail.map(v => (v || "").trim()).filter(Boolean);
          }
          if (Array.isArray(first && first.values)) {
            return first.values.map(v => (v || "").trim()).filter(Boolean);
          }
          if (typeof (first && first.attrValues) === "string") {
            return first.attrValues.split(",").map(v => v.trim()).filter(Boolean);
          }
          if (typeof first === "string") {
            return parsed.map(v => (v || "").trim()).filter(Boolean);
          }
        }
        return [];
      } catch (e) {
        return [];
      }
    },
    syncRuleValueFromList() {
      const detail = (this.ruleValueList || []).map(v => (v || "").trim()).filter(Boolean);
      this.form.ruleValue = JSON.stringify([
        {
          value: (this.form.ruleName || "").trim(),
          detail
        }
      ]);
    },
    syncRuleValueEnFromList() {
      const detail = (this.ruleValueEnList || []).map(v => (v || "").trim()).filter(Boolean);
      this.form.ruleValueEn = JSON.stringify([
        {
          value: (this.form.ruleNameEn || "").trim(),
          detail
        }
      ]);
    }
  }
};
</script>
