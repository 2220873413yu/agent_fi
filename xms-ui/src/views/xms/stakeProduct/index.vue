<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
<!--      <el-form-item label="质押名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入质押名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最低质押" prop="stakeUnitAmountMin">
        <el-input
          v-model="queryParams.stakeUnitAmountMin"
          placeholder="请输入最低质押"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最高质押" prop="maxStakeAmount">
        <el-input
          v-model="queryParams.maxStakeAmount"
          placeholder="请输入最高质押"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="静态日利率 例如: 1就是1%" prop="staticRatio">
        <el-input
          v-model="queryParams.staticRatio"
          placeholder="请输入静态日利率 例如: 1就是1%"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="出局倍数" prop="exitMultiplier">
        <el-input
          v-model="queryParams.exitMultiplier"
          placeholder="请输入出局倍数"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="是否上架 0:否,1:是" prop="isEnabled">
        <el-select v-model="queryParams.isEnabled" placeholder="请选择是否上架 0:否,1:是" clearable>
          <el-option
            v-for="dict in dict.type.t_user_info_is_valid"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>-->
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
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
          v-hasPermi="['xms:stakeProduct:add']"
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
          v-hasPermi="['xms:stakeProduct:edit']"
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
          v-hasPermi="['xms:stakeProduct:remove']"
        >删除</el-button>
      </el-col>-->
<!--      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['xms:stakeProduct:export']"
        >导出</el-button>
      </el-col>-->
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="stakeProductList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键id" align="center" prop="id" v-if="false"/>
<!--      <el-table-column label="质押名称" align="center" prop="name" />-->
<!--      <el-table-column label="销量" align="center" prop="sales" />-->
<!--      <el-table-column label="最低质押" align="center" prop="stakeUnitAmountMin" />
      <el-table-column label="最高质押" align="center" prop="maxStakeAmount" />-->
      <el-table-column label="静态日利率" align="center" prop="staticRatio" >
      <template slot-scope="scope">
        {{ scope.row.staticRatio }} %
      </template>
      </el-table-column>
      <el-table-column label="出局倍数" align="center" prop="exitMultiplier" />
      <el-table-column label="是否上架" align="center" prop="isEnabled">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_info_is_valid" :value="scope.row.isEnabled"/>
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
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['xms:stakeProduct:edit']"
          >修改</el-button>
<!--          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['xms:stakeProduct:remove']"
          >删除</el-button>-->
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

    <!-- 添加或修改质押套餐对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
<!--        <el-form-item label="质押名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入质押名称" />
        </el-form-item>-->
<!--        <el-form-item label="最低质押" prop="stakeUnitAmountMin">
          <el-input v-model="form.stakeUnitAmountMin"
                    oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
                    placeholder="请输入最低质押" />
        </el-form-item>
        <el-form-item label="最高质押" prop="maxStakeAmount">
          <el-input v-model="form.maxStakeAmount"
                    oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
                    placeholder="请输入最高质押" />
        </el-form-item>-->
        <el-form-item label="静态日利率" prop="staticRatio">
          <el-input
            v-model="form.staticRatio"
            placeholder="请输入静态日利率"
            @input="value => handleDecimal2Input('staticRatio', value)"
          />
          <div class="form-tip">以百分比为单位，例如：1 表示 1%</div>
        </el-form-item>

        <el-form-item label="出局倍数" prop="exitMultiplier">
          <el-input
            v-model="form.exitMultiplier"
            placeholder="请输入出局倍数"
            @input="value => handleDecimal2Input('exitMultiplier', value)"
          />
        </el-form-item>

        <el-form-item label="是否上架" prop="isEnabled">
          <el-select v-model="form.isEnabled" placeholder="请选择是否上架 0:否,1:是">
            <el-option
              v-for="dict in dict.type.t_user_info_is_valid"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
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
import { listStakeProduct, getStakeProduct, delStakeProduct, addStakeProduct, updateStakeProduct } from "@/api/xms/stakeProduct";

export default {
  name: "StakeProduct",
  dicts: ['t_user_info_is_valid'],
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
      // 质押套餐表格数据
      stakeProductList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        sales: null,
        stakeUnitAmountMin: null,
        maxStakeAmount: null,
        staticRatio: null,
        exitMultiplier: null,
        isEnabled: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          { required: true, message: "质押名称不能为空", trigger: "blur" }
        ],
        sales: [
          { required: true, message: "销量不能为空", trigger: "blur" }
        ],
        stakeUnitAmountMin: [
          { required: true, message: "最低质押不能为空", trigger: "blur" }
        ],
        staticRatio: [
          { required: true, message: "静态日利率 例如: 1就是1%不能为空", trigger: "blur" }
        ],
        isEnabled: [
          { required: true, message: "是否上架 0:否,1:是不能为空", trigger: "change" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    handleDecimal2Input(field, value) {
      this.form[field] = this.normalizeDecimal2(value);
    },
    normalizeDecimal2(value) {
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
    /** 查询质押套餐列表 */
    getList() {
      this.loading = true;
      listStakeProduct(this.queryParams).then(response => {
        this.stakeProductList = response.rows;
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
        name: null,
        sales: null,
        stakeUnitAmountMin: null,
        maxStakeAmount: null,
        staticRatio: null,
        exitMultiplier: null,
        isEnabled: null,
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
      this.title = "添加质押套餐";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getStakeProduct(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改质押套餐";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateStakeProduct(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addStakeProduct(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除质押套餐编号为"' + ids + '"的数据项？').then(function() {
        return delStakeProduct(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/stakeProduct/export', {
        ...this.queryParams
      }, `stakeProduct_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
