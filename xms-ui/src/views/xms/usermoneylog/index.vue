<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="用户UID" label-width="120px" prop="userId">
        <el-input
          v-model="queryParams.userId"
          clearable
          placeholder="请输入用户UID"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
<!--      <el-form-item label="钱包地址" prop="userAccount" label-width="120px">
        <el-input
          v-model="queryParams.userAccount"
          placeholder="请输入钱包地址"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->
      <el-form-item label="币种" label-width="120px" prop="coinType">
        <el-select v-model="queryParams.coinType" clearable placeholder="请选择币种">
          <el-option
            v-for="dict in dict.type.t_user_money_log_coin_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="流水号" label-width="120px" prop="serialCode">
        <el-input
          v-model="queryParams.serialCode"
          clearable
          placeholder="请输入流水号"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="来源订单" label-width="120px" prop="sourceCode">
        <el-input
          v-model="queryParams.sourceCode"
          clearable
          placeholder="请输入来源订单"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="来源类型" label-width="120px" prop="sourceType">
        <el-select v-model="queryParams.sourceType" clearable placeholder="请选择来源类型">
          <el-option
            v-for="dict in dict.type.t_user_money_log_source_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" label-width="120px">
        <el-date-picker
          v-model="daterangeCreateTime"
          end-placeholder="结束日期"
          range-separator="-"
          start-placeholder="开始日期"
          style="width: 240px"
          type="daterange"
          value-format="yyyy-MM-dd"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <!-- <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['xms:usermoneylog:add']"
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
          v-hasPermi="['xms:usermoneylog:edit']"
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
          v-hasPermi="['xms:usermoneylog:remove']"
        >删除</el-button>
      </el-col>-->
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:usermoneylog:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="usermoneylogList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55" />
      <!-- <el-table-column label="主键id" align="center" prop="id" /> -->
      <el-table-column align="center" label="用户UID" prop="userId" />
      <el-table-column align="center" label="钱包地址" prop="userAccount" />
      <el-table-column align="center" label="币种" prop="coinType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_money_log_coin_type" :value="scope.row.coinType"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="变动额度" prop="changeBalance" />
      <el-table-column align="center" label="变动前余额" prop="beforeBalance" />
      <el-table-column align="center" label="变动后余额" prop="afterBalance" />
      <el-table-column align="center" label="流水号" prop="serialCode" />
      <el-table-column align="center" label="来源订单" prop="sourceCode" />
      <el-table-column align="center" label="来源类型" prop="sourceType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_money_log_source_type" :value="scope.row.sourceType"/>
        </template>
      </el-table-column>
<!--      <el-table-column label="备注" align="center" prop="remark" />-->
      <el-table-column align="center" label="创建时间" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <!-- <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['xms:usermoneylog:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['xms:usermoneylog:remove']"
          >删除</el-button>
        </template>
      </el-table-column> -->
    </el-table>

    <pagination
      v-show="total>0"
      :limit.sync="queryParams.pageSize"
      :page.sync="queryParams.pageNum"
      :total="total"
      @pagination="getList"
    />

    <!-- 添加或修改钱包流水对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户UID" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户UID" />
        </el-form-item>
        <el-form-item label="币种(对应钱包)" prop="coinType">
          <el-select v-model="form.coinType" placeholder="请选择币种(对应钱包)">
            <el-option
              v-for="dict in dict.type.t_user_money_log_coin_type"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="变动额度" prop="changeBalance">
          <el-input v-model="form.changeBalance" placeholder="请输入变动额度" />
        </el-form-item>
        <el-form-item label="变动前余额" prop="beforeBalance">
          <el-input v-model="form.beforeBalance" placeholder="请输入变动前余额" />
        </el-form-item>
        <el-form-item label="变动后余额" prop="afterBalance">
          <el-input v-model="form.afterBalance" placeholder="请输入变动后余额" />
        </el-form-item>
        <el-form-item label="流水号" prop="serialCode">
          <el-input v-model="form.serialCode" placeholder="请输入流水号" />
        </el-form-item>
        <el-form-item label="来源订单" prop="sourceCode">
          <el-input v-model="form.sourceCode" placeholder="请输入来源订单" />
        </el-form-item>
        <el-form-item label="来源类型(1.充值 2.提现 3.推荐奖 4.级差奖 5.平级奖 6.购买套餐)" prop="sourceType">
          <el-select v-model="form.sourceType" placeholder="请选择来源类型(1.充值 2.提现 3.推荐奖 4.级差奖 5.平级奖 6.购买套餐)">
            <el-option
              v-for="dict in dict.type.t_user_money_log_source_type"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="是否删除" prop="activeFlag">
          <el-input v-model="form.activeFlag" placeholder="请输入是否删除" />
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
import { listUsermoneylog, getUsermoneylog, delUsermoneylog, addUsermoneylog, updateUsermoneylog } from "@/api/xms/usermoneylog";

export default {
  name: "Usermoneylog",
  dicts: ['t_user_money_log_source_type', 't_user_money_log_coin_type'],
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
      // 钱包流水表格数据
      usermoneylogList: [],
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
        coinType: null,
        changeBalance: null,
        beforeBalance: null,
        afterBalance: null,
        userAccount: null,
        serialCode: null,
        sourceCode: null,
        sourceType: null,
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
        activeFlag: [
          { required: true, message: "是否删除不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询钱包流水列表 */
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      if (null != this.daterangeCreateTime && '' != this.daterangeCreateTime) {
        this.queryParams.params["beginCreateTime"] = this.daterangeCreateTime[0];
        this.queryParams.params["endCreateTime"] = this.daterangeCreateTime[1];
      }
      listUsermoneylog(this.queryParams).then(response => {
        this.usermoneylogList = response.rows;
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
        coinType: null,
        changeBalance: null,
        beforeBalance: null,
        afterBalance: null,
        serialCode: null,
        sourceCode: null,
        sourceType: null,
        remark: null,
        createTime: null,
        updateTime: null,
        activeFlag: null
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
      this.title = "添加钱包流水";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getUsermoneylog(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改钱包流水";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateUsermoneylog(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addUsermoneylog(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除钱包流水编号为"' + ids + '"的数据项？').then(function() {
        return delUsermoneylog(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/usermoneylog/export', {
        ...this.queryParams
      }, `usermoneylog_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
