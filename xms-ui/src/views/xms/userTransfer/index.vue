<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="转账用户UID" label-width="120px" prop="fromUserId">
        <el-input
          v-model="queryParams.fromUserId"
          oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
          placeholder="请输入转账用户UID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>

      <el-form-item label="接收用户UID" prop="toUserId" label-width="120px">
        <el-input
          v-model="queryParams.toUserId"
          oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
          placeholder="请输入接收用户UID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>

      <el-form-item label="转账钱包地址" label-width="120px" prop="fromAccount">
        <el-input
          v-model="queryParams.fromAccount"
          placeholder="请输入转账钱包地址"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>

      <el-form-item label="接收钱包地址" label-width="120px" prop="toAccount">
        <el-input
          v-model="queryParams.toAccount"
          placeholder="请输入接收钱包地址"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>

<!--      <el-form-item label="转账用户账号" label-width="120px" prop="fromUserCode">
        <el-input
          v-model="queryParams.fromUserCode"
          clearable
          placeholder="请输入转账用户账号"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->

<!--      -->


<!--      <el-form-item label="接收用户账号" label-width="120px" prop="toUserCode">
        <el-input
          v-model="queryParams.toUserCode"
          clearable
          placeholder="请输入接收用户账号"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->


      <el-form-item label="转账订单号" label-width="120px" prop="code">
        <el-input
          v-model="queryParams.code"
          clearable
          placeholder="请输入转账订单号"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
<!--      <el-form-item label="转账额度" prop="changeBalance">
        <el-input
          v-model="queryParams.changeBalance"
          placeholder="请输入转账额度"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手续费" prop="feeBalance">
        <el-input
          v-model="queryParams.feeBalance"
          placeholder="请输入手续费"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手续费率" prop="feeRatio">
        <el-input
          v-model="queryParams.feeRatio"
          placeholder="请输入手续费率"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="到账金额，用户实际收到的金额" prop="actualAmount">
        <el-input
          v-model="queryParams.actualAmount"
          placeholder="请输入到账金额，用户实际收到的金额"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->

      <el-form-item label="转账币种" label-width="120px" prop="coinType">
        <el-select v-model="queryParams.coinType" clearable placeholder="请选择转账币种">
          <el-option
            v-for="dict in dict.type.t_user_money_log_coin_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="转账时间" label-width="100px">
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
          v-hasPermi="['xms:userTransfer:add']"
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
          v-hasPermi="['xms:userTransfer:edit']"
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
          v-hasPermi="['xms:userTransfer:remove']"
        >删除</el-button>
      </el-col>-->
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:userTransfer:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="userTransferList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55" />
      <el-table-column v-if="false" align="center" label="主键id" prop="id"/>
<!--      <el-table-column label="转账用户UID" align="center" prop="fromUserId" />-->
      <el-table-column align="center" label="转账钱包地址" prop="fromAccount" />
<!--      <el-table-column label="接收用户UID" align="center" prop="toUserId" />-->
      <el-table-column align="center" label="接收钱包地址" prop="toAccount" />
      <el-table-column align="center" label="转账订单号" prop="code" />

      <el-table-column align="center" label="转账币种" prop="coinType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_money_log_coin_type" :value="scope.row.coinType"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="转账额度" prop="changeBalance" />
      <el-table-column align="center" label="到账金额" prop="actualAmount" />
      <el-table-column align="center" label="手续费" prop="feeBalance" />
      <el-table-column align="center" label="手续费率" prop="feeRatio">
        <template slot-scope="scope">
          {{scope.row.feeRatio}}%
        </template>
      </el-table-column>

      <el-table-column align="center" label="转账时间" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
<!--      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['xms:userTransfer:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['xms:userTransfer:remove']"
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

    <!-- 添加或修改用户转账记录对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="转账用户UID" prop="fromUserId">
          <el-input v-model="form.fromUserId" placeholder="请输入转账用户UID" />
        </el-form-item>
        <el-form-item label="转账用户账号" prop="fromAccount">
          <el-input v-model="form.fromAccount" placeholder="请输入转账用户账号" />
        </el-form-item>
        <el-form-item label="接收用户UID" prop="toUserId">
          <el-input v-model="form.toUserId" placeholder="请输入接收用户UID" />
        </el-form-item>
        <el-form-item label="接收用户账号" prop="toAccount">
          <el-input v-model="form.toAccount" placeholder="请输入接收用户账号" />
        </el-form-item>
        <el-form-item label="转账订单号" prop="code">
          <el-input v-model="form.code" placeholder="请输入转账订单号" />
        </el-form-item>
        <el-form-item label="转账额度" prop="changeBalance">
          <el-input v-model="form.changeBalance" placeholder="请输入转账额度" />
        </el-form-item>
        <el-form-item label="手续费" prop="feeBalance">
          <el-input v-model="form.feeBalance" placeholder="请输入手续费" />
        </el-form-item>
        <el-form-item label="手续费率" prop="feeRatio">
          <el-input v-model="form.feeRatio" placeholder="请输入手续费率" />
        </el-form-item>
        <el-form-item label="到账金额，用户实际收到的金额" prop="actualAmount">
          <el-input v-model="form.actualAmount" placeholder="请输入到账金额，用户实际收到的金额" />
        </el-form-item>
        <el-form-item label="转账币种 1:USDT,2:SGM" prop="coinType">
          <el-select v-model="form.coinType" placeholder="请选择转账币种 1:USDT,2GCT">
            <el-option
              v-for="dict in dict.type.t_user_money_log_coin_type"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
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
import { listUserTransfer, getUserTransfer, delUserTransfer, addUserTransfer, updateUserTransfer } from "@/api/xms/userTransfer";

export default {
  name: "UserTransfer",
  dicts: ['t_user_money_log_coin_type'],
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
      // 用户转账记录表格数据
      userTransferList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 转账币种 1:USDT,2GCT时间范围
      daterangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        fromUserId: null,
        fromUserCode: null,
        fromAccount: null,
        toUserId: null,
        toUserCode: null,
        toAccount: null,
        code: null,
        changeBalance: null,
        feeBalance: null,
        feeRatio: null,
        actualAmount: null,
        createTime: null,
        coinType: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        fromUserId: [
          { required: true, message: "转账用户UID不能为空", trigger: "blur" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询用户转账记录列表 */
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      if (null != this.daterangeCreateTime && '' != this.daterangeCreateTime) {
        this.queryParams.params["beginCreateTime"] = this.daterangeCreateTime[0];
        this.queryParams.params["endCreateTime"] = this.daterangeCreateTime[1];
      }
      listUserTransfer(this.queryParams).then(response => {
        this.userTransferList = response.rows;
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
        fromUserId: null,
        fromAccount: null,
        toUserId: null,
        toAccount: null,
        code: null,
        changeBalance: null,
        feeBalance: null,
        feeRatio: null,
        actualAmount: null,
        createTime: null,
        coinType: null
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
      this.title = "添加用户转账记录";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getUserTransfer(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改用户转账记录";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateUserTransfer(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addUserTransfer(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除用户转账记录编号为"' + ids + '"的数据项？').then(function() {
        return delUserTransfer(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/userTransfer/export', {
        ...this.queryParams
      }, `userTransfer_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
