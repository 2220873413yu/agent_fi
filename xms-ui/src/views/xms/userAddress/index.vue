<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
<!--      <el-form-item label="省/洲" prop="province">
        <el-input
          v-model="queryParams.province"
          placeholder="请输入省/洲"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="市" prop="city">
        <el-input
          v-model="queryParams.city"
          placeholder="请输入市"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="区/县/街道地址" prop="area">
        <el-input
          v-model="queryParams.area"
          placeholder="请输入区/县/街道地址"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="详细地址/街道地址2" prop="detailed">
        <el-input
          v-model="queryParams.detailed"
          placeholder="请输入详细地址/街道地址2"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="queryParams.phone"
          placeholder="请输入手机号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="收货人姓名" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入收货人姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->
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
          v-model="queryParams.userAccount"
          clearable
          placeholder="请输入钱包地址"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
<!--      <el-form-item label="是否默认0:否,1:是" prop="addressState">
        <el-select v-model="queryParams.addressState" placeholder="请选择是否默认0:否,1:是" clearable>
          <el-option
            v-for="dict in dict.type.t_user_info_is_valid"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
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
          v-hasPermi="['xms:userAddress:add']"
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
          v-hasPermi="['xms:userAddress:edit']"
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
          v-hasPermi="['xms:userAddress:remove']"
        >删除</el-button>
      </el-col>-->
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:userAddress:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="userAddressList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55" />
      <el-table-column align="center" label="id" prop="id"/>
      <el-table-column align="center" label="用户ID" prop="userId" />
      <el-table-column align="center" label="钱包地址" prop="userAccount" />
      <el-table-column align="center" label="省" prop="province" />
      <el-table-column align="center" label="市" prop="city" />
      <el-table-column align="center" label="区" prop="area" />
      <el-table-column align="center" label="详细地址" prop="detailed" />
      <el-table-column align="center" label="手机号" prop="phone" />
      <el-table-column align="center" label="收货人姓名" prop="userName" />
      <el-table-column align="center" label="是否默认" prop="addressState">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_info_is_valid" :value="scope.row.addressState"/>
        </template>
      </el-table-column>
<!--      <el-table-column label="备注" align="center" prop="remark" />-->
      <el-table-column align="center" label="创建时间" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>

      <el-table-column align="center" label="更新时间" prop="updateTime" width="180">
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
            v-hasPermi="['xms:userAddress:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['xms:userAddress:remove']"
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

    <!-- 添加或修改收货地址对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="省/洲" prop="province">
          <el-input v-model="form.province" placeholder="请输入省/洲" />
        </el-form-item>
        <el-form-item label="市" prop="city">
          <el-input v-model="form.city" placeholder="请输入市" />
        </el-form-item>
        <el-form-item label="区/县/街道地址" prop="area">
          <el-input v-model="form.area" placeholder="请输入区/县/街道地址" />
        </el-form-item>
        <el-form-item label="详细地址/街道地址2" prop="detailed">
          <el-input v-model="form.detailed" placeholder="请输入详细地址/街道地址2" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="收货人姓名" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="是否默认0:否,1:是" prop="addressState">
          <el-select v-model="form.addressState" placeholder="请选择是否默认0:否,1:是">
            <el-option
              v-for="dict in dict.type.t_user_info_is_valid"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="是否删除 否 0  1 是" prop="deleted">
          <el-input v-model="form.deleted" placeholder="请输入是否删除 否 0  1 是" />
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
import { listUserAddress, getUserAddress, delUserAddress, addUserAddress, updateUserAddress } from "@/api/xms/userAddress";

export default {
  name: "UserAddress",
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
      // 收货地址表格数据
      userAddressList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        province: null,
        userAccount: null,
        city: null,
        area: null,
        detailed: null,
        phone: null,
        userName: null,
        userId: null,
        addressState: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        province: [
          { required: true, message: "省/洲不能为空", trigger: "blur" }
        ],
        city: [
          { required: true, message: "市不能为空", trigger: "blur" }
        ],
        phone: [
          { required: true, message: "手机号不能为空", trigger: "blur" }
        ],
        userName: [
          { required: true, message: "收货人姓名不能为空", trigger: "blur" }
        ],
        addressState: [
          { required: true, message: "是否默认0:否,1:是不能为空", trigger: "change" }
        ],
        deleted: [
          { required: true, message: "是否删除 否 0  1 是不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询收货地址列表 */
    getList() {
      this.loading = true;
      listUserAddress(this.queryParams).then(response => {
        this.userAddressList = response.rows;
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
        province: null,
        city: null,
        area: null,
        detailed: null,
        phone: null,
        userName: null,
        userId: null,
        addressState: null,
        remark: null,
        createTime: null,
        createBy: null,
        updateTime: null,
        updateBy: null,
        deleted: null
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
      this.title = "添加收货地址";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getUserAddress(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改收货地址";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateUserAddress(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addUserAddress(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除收货地址编号为"' + ids + '"的数据项？').then(function() {
        return delUserAddress(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/userAddress/export', {
        ...this.queryParams
      }, `userAddress_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
