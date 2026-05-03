<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
<!--      <el-form-item label="节点名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入节点名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="节点价格" prop="price">
        <el-input
          v-model="queryParams.price"
          placeholder="请输入节点价格"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="等级" prop="level">
        <el-input
          v-model="queryParams.level"
          placeholder="请输入等级"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="直推奖励比例(%)" prop="directReferralRate">
        <el-input
          v-model="queryParams.directReferralRate"
          placeholder="请输入直推奖励比例(%)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="间推奖励比例(%)，无则0" prop="indirectReferralRate">
        <el-input
          v-model="queryParams.indirectReferralRate"
          placeholder="请输入间推奖励比例(%)，无则0"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="权重系数(倍数)" prop="weightMultiplier">
        <el-input
          v-model="queryParams.weightMultiplier"
          placeholder="请输入权重系数(倍数)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="预测下单手续费减免比例(%)" prop="predOrderFeeReliefRate">
        <el-input
          v-model="queryParams.predOrderFeeReliefRate"
          placeholder="请输入预测下单手续费减免比例(%)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->
      <el-form-item label="是否上架" prop="status" label-width="120px">
        <el-select v-model="queryParams.status" placeholder="请选择是否上架" clearable>
          <el-option
            v-for="dict in dict.type.t_user_info_is_valid"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
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
          v-hasPermi="['xms:nodePackage:add']"
        >新增</el-button>
      </el-col>-->
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['xms:nodePackage:edit']"
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
          v-hasPermi="['xms:nodePackage:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['xms:nodePackage:export']"
        >导出</el-button>
      </el-col>-->
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="nodePackageList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键id" align="center" prop="id" v-if="false"/>
<!--      <el-table-column label="节点名称" align="center" prop="name" />-->
      <el-table-column label="销量" align="center" prop="sales" />
      <el-table-column label="节点价格" align="center" prop="price" />
      <el-table-column label="等级" align="center" prop="level">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_node_plan_node_level" :value="scope.row.level"/>
        </template>
      </el-table-column>
      <el-table-column label="直推奖励比例" align="center" prop="directReferralRate">
        <template slot-scope="scope">
          {{scope.row.directReferralRate}}%
        </template>
      </el-table-column>
      <el-table-column label="间推奖励比例" align="center" prop="indirectReferralRate" >
        <template slot-scope="scope">
          {{scope.row.indirectReferralRate}}%
        </template>
      </el-table-column>
      <el-table-column label="权重系数(倍数)" align="center" prop="weightMultiplier" />
      <el-table-column label="预测下单手续费减免比例" align="center" prop="predOrderFeeReliefRate" >
        <template slot-scope="scope">
          {{scope.row.predOrderFeeReliefRate}}%
        </template>
      </el-table-column>
      <el-table-column label="是否上架" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_info_is_valid" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="创建时间" prop="createTime" >
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="修改时间" prop="updateTime" >
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
            v-hasPermi="['xms:nodePackage:edit']"
          >修改</el-button>
<!--          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['xms:nodePackage:remove']"
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

    <!-- 添加或修改节点套餐对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
<!--        <el-form-item label="节点名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入节点名称" />
        </el-form-item>-->


        <el-form-item label="节点等级" prop="level">
          <el-select v-model="form.level" placeholder="请选择等级">
            <el-option
              v-for="dict in dict.type.t_node_plan_node_level"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
              disabled
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="节点价格" prop="price">
          <el-input
            v-model="form.price"
            placeholder="请输入节点价格"
            @input="onNumericDecimal2('price')"
          />
        </el-form-item>
        <el-form-item label="直推奖励比例" prop="directReferralRate">
          <el-input
            v-model="form.directReferralRate"
            placeholder="请输入直推奖励比例"
            @input="onNumericDecimal2('directReferralRate')"
          />
          <div class="form-tip">以百分比为单位，例如：1 表示 1%</div>
        </el-form-item>

        <el-form-item label="间推奖励比例" prop="indirectReferralRate">
          <el-input
            v-model="form.indirectReferralRate"
            placeholder="请输入间推奖励比例"
            @input="onNumericDecimal2('indirectReferralRate')"
          />
          <div class="form-tip">以百分比为单位，例如：1 表示 1%</div>
        </el-form-item>


        <el-form-item label="预测下单手续费减免比例" prop="predOrderFeeReliefRate">
          <el-input
            v-model="form.predOrderFeeReliefRate"
            placeholder="请输入预测下单手续费减免比例"
            @input="onNumericDecimal2('predOrderFeeReliefRate')"
          />
          <div class="form-tip">以百分比为单位，例如：1 表示 1%</div>
        </el-form-item>

        <el-form-item label="权重系数(倍数)" prop="weightMultiplier">
          <el-input
            v-model="form.weightMultiplier"
            placeholder="请输入权重系数(倍数)"
            @input="onNumericDecimal2('weightMultiplier')"
          />
        </el-form-item>

        <el-form-item label="是否上架" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.t_user_info_is_valid"
              :key="dict.value"
              :label="parseInt(dict.value)"
            >{{dict.label}}</el-radio>
          </el-radio-group>
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
import { listNodePackage, getNodePackage, delNodePackage, addNodePackage, updateNodePackage } from "@/api/xms/nodePackage";

export default {
  name: "NodePackage",
   dicts: ['t_user_info_is_valid','t_node_plan_node_level'],
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
      // 节点套餐表格数据
      nodePackageList: [],
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
        price: null,
        level: null,
        directReferralRate: null,
        indirectReferralRate: null,
        weightMultiplier: null,
        predOrderFeeReliefRate: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          { required: true, message: "节点名称不能为空", trigger: "blur" }
        ],
        price: [
          { required: true, message: "节点价格不能为空", trigger: "blur" }
        ],
        directReferralRate: [
          { required: true, message: "直推奖励比例不能为空", trigger: "blur" }
        ],
        indirectReferralRate: [
          { required: true, message: "间推奖励比例不能为空", trigger: "blur" }
        ],
        weightMultiplier: [
          { required: true, message: "权重系数不能为空", trigger: "blur" }
        ],
        predOrderFeeReliefRate: [
          { required: true, message: "预测下单手续费减免比例不能为空", trigger: "blur" }
        ],
        status: [
          { required: true, message: "是否上架不能为空", trigger: "change" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 仅允许数字，单个小数点，小数部分最多2位 */
    onNumericDecimal2(key) {
      let s = String(this.form[key] == null ? "" : this.form[key]);
      s = s.replace(/[^\d.]/g, "");
      const dot = s.indexOf(".");
      let out;
      if (dot === -1) {
        out = s;
      } else {
        const intPart = s.slice(0, dot).replace(/\./g, "");
        const frac = s.slice(dot + 1).replace(/\./g, "").slice(0, 2);
        out = intPart + "." + frac;
      }
      if (out.startsWith(".")) {
        out = "0" + out;
      }
      if (this.form[key] !== out) {
        this.$set(this.form, key, out);
      }
    },
    /** 查询节点套餐列表 */
    getList() {
      this.loading = true;
      listNodePackage(this.queryParams).then(response => {
        this.nodePackageList = response.rows;
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
        price: null,
        level: null,
        directReferralRate: null,
        indirectReferralRate: null,
        weightMultiplier: null,
        predOrderFeeReliefRate: null,
        status: null,
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
      this.title = "添加节点套餐";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getNodePackage(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改节点套餐";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateNodePackage(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addNodePackage(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除节点套餐编号为"' + ids + '"的数据项？').then(function() {
        return delNodePackage(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/nodePackage/export', {
        ...this.queryParams
      }, `nodePackage_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
