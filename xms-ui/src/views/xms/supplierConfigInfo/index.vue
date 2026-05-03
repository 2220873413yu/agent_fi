<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="APPID" prop="appId">
        <el-input
          v-model="queryParams.appId"
          clearable
          placeholder="请输入供应商链APPID"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:supplierConfigInfo:add']"
          icon="el-icon-plus"
          plain
          size="mini"
          type="primary"
          @click="handleAdd"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:supplierConfigInfo:edit']"
          :disabled="single"
          icon="el-icon-edit"
          plain
          size="mini"
          type="success"
          @click="handleUpdate"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:supplierConfigInfo:remove']"
          :disabled="multiple"
          icon="el-icon-delete"
          plain
          size="mini"
          type="danger"
          @click="handleDelete"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:supplierConfigInfo:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="supplierConfigInfoList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55" />
      <el-table-column align="center" label="id" prop="id" />
      <el-table-column align="center" label="供应商链APPID" prop="appId" />
      <el-table-column align="center" label="签名/密钥" prop="secret" />
      <el-table-column align="center" label="回调地址" prop="notifyUrl" />
      <el-table-column align="center" label="供应商名称" prop="name" />
      <el-table-column align="center" label="接口地址前缀/授权token" prop="urlPrefix" />
      <el-table-column align="center" label="接口地址后缀/授权refreshtoken" prop="urlSuffix" />
      <el-table-column align="center" label="文件地址前缀" prop="fileUrlPrefix" />
      <el-table-column align="center" label="环境" prop="mode" />
      <el-table-column align="center" label="备注" prop="remark" />
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['xms:supplierConfigInfo:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            v-hasPermi="['xms:supplierConfigInfo:remove']"
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

    <!-- 添加或修改供应商配置信息对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="供应商链APPID" prop="appId">
          <el-input v-model="form.appId" placeholder="请输入供应商链APPID" />
        </el-form-item>
        <el-form-item label="签名/密钥" prop="secret">
          <el-input v-model="form.secret" placeholder="请输入签名/密钥" />
        </el-form-item>
        <el-form-item label="回调地址" prop="notifyUrl">
          <el-input v-model="form.notifyUrl" placeholder="请输入回调地址" />
        </el-form-item>
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="接口地址前缀/授权token" prop="urlPrefix">
          <el-input v-model="form.urlPrefix" placeholder="请输入接口地址前缀" />
        </el-form-item>
        <el-form-item label="接口地址后缀/授权refreshtoken" prop="urlSuffix">
          <el-input v-model="form.urlSuffix" placeholder="请输入接口地址后缀" />
        </el-form-item>
        <el-form-item label="文件地址前缀" prop="fileUrlPrefix">
          <el-input v-model="form.fileUrlPrefix" placeholder="请输入文件地址前缀" />
        </el-form-item>
        <el-form-item label="环境" prop="mode">
          <el-input v-model="form.mode" placeholder="环境" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
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
import { listSupplierConfigInfo, getSupplierConfigInfo, delSupplierConfigInfo, addSupplierConfigInfo, updateSupplierConfigInfo } from "@/api/xms/supplierConfigInfo";

export default {
  name: "SupplierConfigInfo",
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
      // 供应商配置信息表格数据
      supplierConfigInfoList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        appId: null,
        secret: null,
        notifyUrl: null,
        name: null,
        urlPrefix: null,
        urlSuffix: null,
        mode: null,
        fileUrlPrefix: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        appId: [
          { required: true, message: "供应商链APPID不能为空", trigger: "blur" }
        ],
        secret: [
          { required: true, message: "签名/密钥不能为空", trigger: "blur" }
        ],
    /*    notifyUrl: [
          { required: true, message: "回调地址不能为空", trigger: "blur" }
        ],*/
        name: [
          { required: true, message: "供应商名称不能为空", trigger: "blur" }
        ],
        urlPrefix: [
          { required: true, message: "接口地址前缀不能为空", trigger: "blur" }
        ]

      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询供应商配置信息列表 */
    getList() {
      this.loading = true;
      listSupplierConfigInfo(this.queryParams).then(response => {
        this.supplierConfigInfoList = response.rows;
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
        appId: null,
        secret: null,
        notifyUrl: null,
        name: null,
        urlPrefix: null,
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
      this.title = "添加供应商配置信息";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getSupplierConfigInfo(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改供应商配置信息";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateSupplierConfigInfo(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addSupplierConfigInfo(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除供应商配置信息编号为"' + ids + '"的数据项？').then(function() {
        return delSupplierConfigInfo(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/supplierConfigInfo/export', {
        ...this.queryParams
      }, `supplierConfigInfo_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
