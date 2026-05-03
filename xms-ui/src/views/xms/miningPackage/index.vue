<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
<!--      <el-form-item label="矿机名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入矿机名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="矿机价格" prop="price">
        <el-input
          v-model="queryParams.price"
          placeholder="请输入矿机价格"
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
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleOpenAllocate"
          v-hasPermi="['xms:miningPackage:add']"
        >矿机拨付</el-button>
      </el-col>
<!--      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['xms:miningPackage:edit']"
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
          v-hasPermi="['xms:miningPackage:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['xms:miningPackage:export']"
        >导出</el-button>
      </el-col>-->
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="miningPackageList" @selection-change="handleSelectionChange">
<!--      <el-table-column type="selection" width="55" align="center" />-->
      <el-table-column v-if="false" align="center" label="主键id" prop="id"/>
      <el-table-column align="center" label="矿机名称" prop="name" />
      <el-table-column align="center" label="销量" prop="sales" />
      <el-table-column align="center" label="可售库存数量" prop="availableStock" />
      <el-table-column align="center" label="矿机价格" prop="price" />
      <el-table-column align="center" label="算力数值" prop="remark" />
      <el-table-column align="center" label="是否上架" prop="status" >
      <template slot-scope="scope">
        <dict-tag :options="dict.type.t_user_info_is_valid" :value="scope.row.status"/>
      </template>
      </el-table-column>
<!--      <el-table-column label="remark" align="center" prop="remark" />-->
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            v-hasPermi="['xms:miningPackage:edit']"
            @click="handleUpdate(scope.row)"
            icon="el-icon-edit"
          >修改</el-button>
<!--          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['xms:miningPackage:remove']"
          >删除</el-button>-->
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :page.sync="queryParams.pageNum"
      :total="total"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改矿机套餐对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
<!--        <el-form-item label="矿机名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入矿机名称" />
        </el-form-item>-->
        <el-form-item label="矿机价格" prop="price">
          <el-input v-model="form.price" maxlength="10"
                    placeholder="请输入矿机价格" show-word-limit
                    @input="handleFeeRatioInput"/>
        </el-form-item>

        <el-form-item label="可售库存数量" prop="availableStock">
          <el-input v-model="form.availableStock" maxlength="10"
                    oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }" placeholder="请输入可售库存数量"
                    show-word-limit
          />
        </el-form-item>

        <el-form-item label="是否上架" prop="status">
          <el-select v-model="form.status" placeholder="请选择是否上架">
            <el-option
              v-for="dict in dict.type.t_user_info_is_valid"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="算力数值" prop="remark">
          <el-input v-model="form.remark"
                    placeholder="请输入算力数值"
                    @input="handleRemarkInput" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 矿机拨付对话框 -->
    <el-dialog :title="allocateTitle" :visible.sync="allocateOpen" append-to-body width="500px">
      <el-form ref="allocateForm" :model="allocateForm" :rules="allocateRules" label-width="90px">
        <el-form-item label="钱包地址" prop="account">
          <el-input v-model="allocateForm.account"  placeholder="请输入钱包地址" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="支付方式" prop="payType">
          <el-select v-model="allocateForm.payType" placeholder="请选择支付方式">
            <el-option :value="1" label="USDT" />
            <el-option :value="2" label="DFC" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitAllocate">确 定</el-button>
        <el-button @click="cancelAllocate">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listMiningPackage, getMiningPackage,
  handleAdminAllocateMiningMachine,
  delMiningPackage, addMiningPackage, updateMiningPackage } from "@/api/xms/miningPackage";

export default {
  name: "MiningPackage",
  dicts: ['card_type', 't_user_info_is_valid'],
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
      // 矿机套餐表格数据
      miningPackageList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否显示拨付弹出层
      allocateOpen: false,
      // 拨付弹出层标题
      allocateTitle: "矿机拨付",
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        sales: null,
        price: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 拨付表单参数
      allocateForm: {
        account: "",
        payType: null
      },
      // 表单校验
      rules: {
        name: [
          { required: true, message: "矿机名称不能为空", trigger: "blur" }
        ],
        sales: [
          { required: true, message: "销量不能为空", trigger: "blur" }
        ],
        availableStock: [
          { required: true, message: "可售库存不能为空", trigger: "blur" }
        ],
        remark: [
          { required: true, message: "算力数值不能为空", trigger: "blur" }
        ],
        status: [
          { required: true, message: "是否上架 0:否,1:是不能为空", trigger: "change" }
        ],
        price: [
          { required: true, message: "矿机价格不能为空", trigger: "blur" },
          { validator: feeRatioValidator, trigger: "blur" }
        ]
      },
      // 拨付表单校验
      allocateRules: {
        account: [
          { required: true, message: "钱包地址不能为空", trigger: "blur" },
          { max: 100, message: "钱包地址最多100个字符", trigger: "blur" }
        ],
        payType: [
          { required: true, message: "支付方式不能为空", trigger: "change" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    handleFeeRatioInput(value) {
      this.form.price = this.normalizeFeeRatio(value);
    },
    handleRemarkInput(value) {
      this.form.remark = this.normalizeFeeRatio(value);
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
    /** 查询矿机套餐列表 */
    getList() {
      this.loading = true;
      listMiningPackage(this.queryParams).then(response => {
        this.miningPackageList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 拨付对话框取消
    cancelAllocate() {
      this.allocateOpen = false;
      this.resetAllocate();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        name: null,
        sales: null,
        price: null,
        status: null,
        availableStock: null,
        createTime: null,
        updateTime: null,
        remark: null
      };
      this.resetForm("form");
    },
    // 拨付表单重置
    resetAllocate() {
      this.allocateForm = {
        account: "",
        payType: null
      };
      this.resetForm("allocateForm");
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
      this.title = "添加矿机套餐";
    },
    /** 拨付按钮操作 */
    handleOpenAllocate() {
      this.resetAllocate();
      this.allocateOpen = true;
      this.allocateTitle = "矿机拨付";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getMiningPackage(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改矿机套餐";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateMiningPackage(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addMiningPackage(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 提交拨付 */
    submitAllocate() {
      this.$refs["allocateForm"].validate(valid => {
        if (valid) {
          handleAdminAllocateMiningMachine(this.allocateForm).then(() => {
            this.$modal.msgSuccess("拨付成功");
            this.allocateOpen = false;
            this.resetAllocate();
            this.getList();
          });
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除矿机套餐编号为"' + ids + '"的数据项？').then(function() {
        return delMiningPackage(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/miningPackage/export', {
        ...this.queryParams
      }, `miningPackage_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
