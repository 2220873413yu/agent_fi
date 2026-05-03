<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="用户ID" label-width="120px" prop="userId">
        <el-input
          v-model="queryParams.userId"
          clearable
          oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
          placeholder="请输入用户ID"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="钱包地址" label-width="120px" prop="address">
        <el-input
          v-model="queryParams.address"
          clearable
          placeholder="请输入钱包地址"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
<!--      <el-form-item label="swap数量" prop="swapAmount">
        <el-input
          v-model="queryParams.swapAmount"
          placeholder="请输入swap数量"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->
      <el-form-item label="交易hash" label-width="120px" prop="txHash">
        <el-input
          v-model="queryParams.txHash"
          clearable
          placeholder="请输入交易hash"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="创建时间" label-width="120px">
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
<!--      <el-form-item label="业务状态" prop="bizStatus">
        <el-select v-model="queryParams.bizStatus" placeholder="请选择业务状态" clearable>
          <el-option
            v-for="dict in dict.type.t_ido_order_biz_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>-->

      <el-form-item label="是否处理" label-width="120px" prop="bizStatus1">
        <el-select v-model="queryParams.bizStatus1" clearable placeholder="请选择">
          <el-option
            v-for="dict in dict.type.t_user_info_is_valid"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
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
          v-hasPermi="['xms:swapOrder:add']"
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
          v-hasPermi="['xms:swapOrder:edit']"
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
          v-hasPermi="['xms:swapOrder:remove']"
        >删除</el-button>
      </el-col>-->
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:swapOrder:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="swapOrderList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55" />
      <el-table-column v-if="false" align="center" label="主键ID" prop="id"/>
      <el-table-column align="center" label="用户ID" prop="userId" />
      <el-table-column align="center" label="钱包地址" prop="address" />
      <el-table-column align="center" label="swap数量" prop="swapAmount" />
      <el-table-column align="center" label="交易hash" prop="txHash" />
      <el-table-column align="center" label="业务状态" prop="bizStatus">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_ido_order_biz_status" :value="scope.row.bizStatus"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="是否处理" prop="bizStatus1" width="80">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_info_is_valid" :value="scope.row.bizStatus1"/>
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
<!--      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['xms:swapOrder:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['xms:swapOrder:remove']"
          >删除</el-button>
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

    <!-- 添加或修改swap订单对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户id 如果没有存在用户系统为0" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户id 如果没有存在用户系统为0" />
        </el-form-item>
        <el-form-item label="购买的钱包地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入购买的钱包地址" />
        </el-form-item>
        <el-form-item label="swap数量" prop="swapAmount">
          <el-input v-model="form.swapAmount" placeholder="请输入swap数量" />
        </el-form-item>
        <el-form-item label="交易hash" prop="txHash">
          <el-input v-model="form.txHash" placeholder="请输入交易hash" />
        </el-form-item>
        <el-form-item label="业务状态 1:待处理,2:已处理,3:未注册丢弃" prop="bizStatus">
          <el-select v-model="form.bizStatus" placeholder="请选择业务状态 1:待处理,2:已处理,3:未注册丢弃">
            <el-option
              v-for="dict in dict.type.t_ido_order_biz_status"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="是否处理提现额度 0:否,1:是" prop="bizStatus1">
          <el-input v-model="form.bizStatus1" placeholder="请输入是否处理提现额度 0:否,1:是" />
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
import { listSwapOrder, getSwapOrder, delSwapOrder, addSwapOrder, updateSwapOrder } from "@/api/xms/swapOrder";

export default {
  name: "SwapOrder",
  dicts: ['t_ido_order_biz_status','t_user_info_is_valid'],
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
      // swap订单表格数据
      swapOrderList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否处理提现额度 0:否,1:是时间范围
      daterangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: null,
        address: null,
        swapAmount: null,
        txHash: null,
        createTime: null,
        bizStatus: null,
        bizStatus1: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        txHash: [
          { required: true, message: "交易hash不能为空", trigger: "blur" }
        ],
        createTime: [
          { required: true, message: "创建时间不能为空", trigger: "blur" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询swap订单列表 */
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      if (null != this.daterangeCreateTime && '' != this.daterangeCreateTime) {
        this.queryParams.params["beginCreateTime"] = this.daterangeCreateTime[0];
        this.queryParams.params["endCreateTime"] = this.daterangeCreateTime[1];
      }
      listSwapOrder(this.queryParams).then(response => {
        this.swapOrderList = response.rows;
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
        address: null,
        swapAmount: null,
        txHash: null,
        createTime: null,
        bizStatus: null,
        updateTime: null,
        bizStatus1: null
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
      this.title = "添加swap订单";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getSwapOrder(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改swap订单";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateSwapOrder(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addSwapOrder(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除swap订单编号为"' + ids + '"的数据项？').then(function() {
        return delSwapOrder(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/swapOrder/export', {
        ...this.queryParams
      }, `swapOrder_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
