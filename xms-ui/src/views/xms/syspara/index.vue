<template>
    <div class="app-container">
      <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
        <el-form-item label="参数内码" prop="paraCode">
          <el-input
            v-model="queryParams.paraCode"
            clearable
            placeholder="请输入参数内码"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="参数描述" prop="paraDesc">
          <el-input
            v-model="queryParams.paraDesc"
            clearable
            placeholder="请输入参数描述"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="queryParams.remark"
            clearable
            placeholder="请输入参数描述"
            @keyup.enter.native="handleQuery"
          />
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
            v-hasPermi="['xms:syspara:add']"
          >新增</el-button>
        </el-col> -->
        <el-col :span="1.5">
          <el-button
            v-hasPermi="['xms:syspara:edit']"
            :disabled="single"
            icon="el-icon-edit"
            plain
            size="mini"
            type="success"
            @click="handleUpdate"
          >修改</el-button>
        </el-col>
        <!-- <el-col :span="1.5">
          <el-button
            type="danger"
            plain
            icon="el-icon-delete"
            size="mini"
            :disabled="multiple"
            @click="handleDelete"
            v-hasPermi="['xms:syspara:remove']"
          >删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="warning"
            plain
            icon="el-icon-download"
            size="mini"
            @click="handleExport"
            v-hasPermi="['xms:syspara:export']"
          >导出</el-button>
        </el-col> -->
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="sysparaList" @selection-change="handleSelectionChange">
        <el-table-column align="center" type="selection" width="55" />
        <!-- <el-table-column label="主键id" align="center" prop="sysParaId" /> -->
        <el-table-column align="center" label="参数内码" prop="paraCode" />
        <el-table-column align="center" label="参数值" prop="paraValue" />
        <el-table-column align="center" label="参数描述" prop="paraDesc" />
        <el-table-column align="center" label="备注" prop="remark" />
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
        <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
          <template slot-scope="scope">
            <el-button
              v-hasPermi="['xms:syspara:edit']"
              icon="el-icon-edit"
              size="mini"
              type="text"
              @click="handleUpdate(scope.row)"
            >修改</el-button>
            <!-- <el-button
              size="mini"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['xms:syspara:remove']"
            >删除</el-button> -->
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

      <!-- 添加或修改系统参数对话框 -->
      <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
        <el-form ref="form" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="参数内码" prop="paraCode">
            <el-input v-model="form.paraCode" :disabled="true" placeholder="请输入参数内码" />
          </el-form-item>
          <el-form-item label="参数值" prop="paraValue">
            <el-input v-model="form.paraValue" placeholder="请输入参数值" />
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="form.remark" placeholder="请输入参数值" />
          </el-form-item>
          <el-form-item label="谷歌验证码" prop="autoCode">
            <el-input v-model="form.autoCode" placeholder="请输入谷歌验证码" />
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
  import { listSyspara, getSyspara, delSyspara, addSyspara, updateSyspara } from "@/api/xms/syspara";

  export default {
    name: "Syspara",
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
        // 系统参数表格数据
        sysparaList: [],
        // 弹出层标题
        title: "",
        // 是否显示弹出层
        open: false,
        // 查询参数
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          paraCode: null,
          paraValue: null,
          remark: null,
          paraDesc: null,
          visible: null,
          activeFlag: null
        },
        // 表单参数
        form: {},
        // 表单校验
        rules: {
          paraValue: [
            { required: true, message: "参数值不能为空", trigger: "blur" }
          ],
          autoCode: [
            { required: true, message: "谷歌验证码不能为空", trigger: "blur" }
          ]
        }
      };
    },
    created() {
      this.getList();
    },
    methods: {
      /** 查询系统参数列表 */
      getList() {
        this.loading = true;
        listSyspara(this.queryParams).then(response => {
          this.sysparaList = response.rows;
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
          sysParaId: null,
          paraCode: null,
          remark: null,
          paraValue: null,
          paraDesc: null,
          visible: null,
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
        this.resetForm("queryForm");
        this.handleQuery();
      },
      // 多选框选中数据
      handleSelectionChange(selection) {
        this.ids = selection.map(item => item.sysParaId)
        this.single = selection.length!==1
        this.multiple = !selection.length
      },
      /** 新增按钮操作 */
      handleAdd() {
        this.reset();
        this.open = true;
        this.title = "添加系统参数";
      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        this.reset();
        const sysParaId = row.sysParaId || this.ids
        getSyspara(sysParaId).then(response => {
          this.form = response.data;
          this.open = true;
          this.title = "修改系统参数";
        });
      },
      /** 提交按钮 */
      submitForm() {
        this.$refs["form"].validate(valid => {
          if (valid) {
            if (this.form.sysParaId != null) {
              updateSyspara(this.form).then(response => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              });
            } else {
              addSyspara(this.form).then(response => {
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
        const sysParaIds = row.sysParaId || this.ids;
        this.$modal.confirm('是否确认删除系统参数编号为"' + sysParaIds + '"的数据项？').then(function() {
          return delSyspara(sysParaIds);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {});
      },
      /** 导出按钮操作 */
      handleExport() {
        this.download('xms/syspara/export', {
          ...this.queryParams
        }, `syspara_${new Date().getTime()}.xlsx`)
      }
    }
  };
  </script>
