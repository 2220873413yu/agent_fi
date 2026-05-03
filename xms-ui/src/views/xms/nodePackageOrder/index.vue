<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="订单号" prop="orderNo" label-width="120px">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="用户ID" prop="userId" label-width="120px">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入用户ID"
          oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"

          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="钱包地址" prop="address" label-width="120px">
        <el-input
          v-model="queryParams.address"
          placeholder="请输入钱包地址"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="支付hash" prop="hash" label-width="120px">
        <el-input
          v-model="queryParams.hash"
          placeholder="请输入支付hash"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>

      <el-table-column align="packageLevel" label="节点等级">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_node_plan_node_level" :value="scope.row.packageLevel"/>
        </template>
      </el-table-column>

      <el-form-item label="订单来源" prop="sourceType" label-width="120px">
        <el-select v-model="queryParams.sourceType" placeholder="请选择订单来源" clearable>
          <el-option
            v-for="dict in dict.type.t_node_package_order_source_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="订单状态" prop="status" label-width="120px">
        <el-select v-model="queryParams.status" placeholder="请选择订单状态" clearable>
          <el-option
            v-for="dict in dict.type.t_node_package_order_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="业务处理状态" prop="bizStatus" label-width="120px">
        <el-select v-model="queryParams.bizStatus" placeholder="请选择业务处理状态" clearable>
          <el-option
            v-for="dict in dict.type.t_user_info_is_valid"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" label-width="120px">
        <el-date-picker
          v-model="daterangeCreateTime"
          style="width: 240px"
          value-format="yyyy-MM-dd HH:mm:ss"
          type="datetimerange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
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
          v-hasPermi="['xms:nodePackageOrder:add']"
        >拨付节点</el-button>
      </el-col>
<!--      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['xms:nodePackageOrder:edit']"
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
          v-hasPermi="['xms:nodePackageOrder:remove']"
        >删除</el-button>
      </el-col>-->
<!--      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['xms:nodePackageOrder:export']"
        >导出</el-button>
      </el-col>-->
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="nodePackageOrderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键id" align="center" prop="id" v-if="false"/>
      <el-table-column label="订单号" align="center" prop="orderNo" />
      <el-table-column label="用户ID" align="center" prop="userId" />
      <el-table-column label="钱包地址" align="center" prop="address" width="180"/>
      <el-table-column label="支付hash" align="center" prop="hash" width="180"/>
      <el-table-column label="节点等级" align="center" prop="packageLevel">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_node_plan_node_level" :value="scope.row.packageLevel"/>
        </template>
      </el-table-column>
<!--      <el-table-column label="直推奖励比例" align="center" prop="directReferralRate" >
        <template slot-scope="scope">
          {{scope.row.directReferralRate}}%
        </template>
      </el-table-column>
      <el-table-column label="间推奖励比例" align="center" prop="indirectReferralRate"  >
        <template slot-scope="scope">
          {{scope.row.indirectReferralRate}}%
        </template>
      </el-table-column>-->
      <el-table-column label="权重系数" align="center" prop="weightMultiplier" />
      <el-table-column label="手续费减免比例" align="center" prop="predOrderFeeReliefRate"  >
        <template slot-scope="scope">
          {{scope.row.predOrderFeeReliefRate}}%
        </template>
      </el-table-column>
      <el-table-column label="支付金额" align="center" prop="orderValueUsdt" />
      <el-table-column label="订单来源" align="center" prop="sourceType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_node_package_order_source_type" :value="scope.row.sourceType"/>
        </template>
      </el-table-column>
      <el-table-column label="订单状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_node_package_order_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="业务处理状态" align="center" prop="bizStatus">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_info_is_valid" :value="scope.row.bizStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付时间" align="center" prop="payTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.payTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="修改时间" prop="updateTime" >
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
<!--      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['xms:nodePackageOrder:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['xms:nodePackageOrder:remove']"
          >删除</el-button>
        </template>
      </el-table-column>-->
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改节点购买记录对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">


        <el-form-item label="节点等级" prop="packageLevel">
          <el-select v-model="form.packageLevel" placeholder="请选择等级">
            <el-option
              v-for="dict in dict.type.t_node_plan_node_level.filter(item => Number(item.value) > 0)"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="钱包地址" prop="address">
          <el-input
            v-model="form.address"
            placeholder="请输入钱包地址"
            maxlength="80"
            show-word-limit
          />
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
import { listNodePackageOrder, getNodePackageOrder, delNodePackageOrder, addNodePackageOrder, updateNodePackageOrder } from "@/api/xms/nodePackageOrder";

export default {
  name: "NodePackageOrder",
  dicts: ['t_node_package_order_source_type', 't_node_plan_node_level','t_node_package_order_status', 't_user_info_is_valid'],
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
      // 节点购买记录表格数据
      nodePackageOrderList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 支付时间时间范围
      daterangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: null,
        userId: null,
        address: null,
        hash: null,
        packageLevel: null,
        directReferralRate: null,
        indirectReferralRate: null,
        weightMultiplier: null,
        predOrderFeeReliefRate: null,
        orderValueUsdt: null,
        sourceType: null,
        status: null,
        bizStatus: null,
        createTime: null,
        payTime: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        address: [
          { required: true, message: "钱包地址不能为空", trigger: "blur" },
        ],
        userId: [
          { required: true, message: "用户id不能为空", trigger: "blur" }
        ],
        packageLevel: [
          { required: true, message: "节点等级不能为空", trigger: "blur" }
        ],
        directReferralRate: [
          { required: true, message: "下单时直推奖励比例快照不能为空", trigger: "blur" }
        ],
        weightMultiplier: [
          { required: true, message: "下单时权重系数快照不能为空", trigger: "blur" }
        ],
        predOrderFeeReliefRate: [
          { required: true, message: "下单时预测下单手续费减免比例快照不能为空", trigger: "blur" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询节点购买记录列表 */
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      if (null != this.daterangeCreateTime && '' != this.daterangeCreateTime) {
        this.queryParams.params["beginCreateTime"] = this.daterangeCreateTime[0];
        this.queryParams.params["endCreateTime"] = this.daterangeCreateTime[1];
      }
      listNodePackageOrder(this.queryParams).then(response => {
        this.nodePackageOrderList = response.rows;
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
        orderNo: null,
        userId: null,
        address: null,
        hash: null,
        packageLevel: null,
        directReferralRate: null,
        indirectReferralRate: null,
        weightMultiplier: null,
        predOrderFeeReliefRate: null,
        orderValueUsdt: null,
        sourceType: null,
        status: null,
        bizStatus: null,
        createTime: null,
        updateTime: null,
        payTime: null
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
      this.title = "添加节点购买记录";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getNodePackageOrder(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改节点购买记录";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateNodePackageOrder(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addNodePackageOrder(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除节点购买记录编号为"' + ids + '"的数据项？').then(function() {
        return delNodePackageOrder(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/nodePackageOrder/export', {
        ...this.queryParams
      }, `nodePackageOrder_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
