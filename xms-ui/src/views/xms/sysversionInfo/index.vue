<template>
    <div class="app-container">
      <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="100px" size="small">
        <el-form-item label="是否强制更新" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="请选择是否强制更新">
            <el-option
              v-for="dict in dict.type.t_sys_version_info_status"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select v-model="queryParams.deviceType" clearable placeholder="请选择设备类型">
            <el-option
              v-for="dict in dict.type.t_sys_version_info_device_type"
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
        <el-col :span="1.5">
          <el-button
            v-hasPermi="['xms:sysversionInfo:add']"
            icon="el-icon-plus"
            plain
            size="mini"
            type="primary"
            @click="handleAdd"
          >新增</el-button>
        </el-col>
        <!-- <el-col :span="1.5">
          <el-button
            type="success"
            plain
            icon="el-icon-edit"
            size="mini"
            :disabled="single"
            @click="handleUpdate"
            v-hasPermi="['xms:sysversionInfo:edit']"
          >修改</el-button>
        </el-col> -->
        <el-col :span="1.5">
          <el-button
            v-hasPermi="['xms:sysversionInfo:remove']"
            :disabled="multiple"
            icon="el-icon-delete"
            plain
            size="mini"
            type="danger"
            @click="handleDelete"
          >删除</el-button>
        </el-col>
        <!-- <el-col :span="1.5">
          <el-button
            type="warning"
            plain
            icon="el-icon-download"
            size="mini"
            @click="handleExport"
            v-hasPermi="['xms:sysversionInfo:export']"
          >导出</el-button>
        </el-col> -->
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="sysversionInfoList" @selection-change="handleSelectionChange">
        <el-table-column align="center" type="selection" width="55" />
        <el-table-column align="center" label="id" prop="id" />
        <el-table-column align="center" label="版本号" prop="versionNo" />
        <el-table-column align="center" label="版本下载路径" prop="versionUrl" />
        <el-table-column align="center" label="是否强制更新" prop="status">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.t_sys_version_info_status" :value="scope.row.status"/>
          </template>
        </el-table-column>
        <el-table-column align="center" label="设备类型" prop="deviceType">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.t_sys_version_info_device_type" :value="scope.row.deviceType"/>
          </template>
        </el-table-column>
        <el-table-column align="center" label="更新内容备注" prop="note" />
        <el-table-column align="center" label="热更新链接" prop="remark" />
        <el-table-column align="center" label="创建时间" prop="createTime" >
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
          <template slot-scope="scope">
            <el-button
              v-hasPermi="['xms:sysversionInfo:edit']"
              icon="el-icon-edit"
              size="mini"
              type="text"
              @click="handleUpdate(scope.row)"
            >修改</el-button>
            <el-button
              v-hasPermi="['xms:sysversionInfo:remove']"
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

      <!-- 添加或修改版本对话框 -->
      <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
        <el-form ref="form" :model="form" :rules="rules" label-width="110px">
          <el-form-item label="版本号" prop="versionNo">
            <el-input v-model="form.versionNo" placeholder="请输入版本号" />
          </el-form-item>
          <el-form-item label="版本下载路径" prop="versionUrl">
            <el-input v-model="form.versionUrl" placeholder="请输入版本下载路径" />
          </el-form-item>
          <el-form-item label="热更新链接" prop="remark">
            <el-input v-model="form.remark" placeholder="请输入热更新链接" />
          </el-form-item>
          <el-form-item label="是否强制更新" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio
                v-for="dict in dict.type.t_sys_version_info_status"
                :key="dict.value"
                :label="dict.value"
              >{{dict.label}}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="设备类型" prop="deviceType">
            <el-select v-model="form.deviceType" placeholder="请选择设备类型">
              <el-option
                v-for="dict in dict.type.t_sys_version_info_device_type"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="更新内容备注" prop="note">
            <el-input v-model="form.note" placeholder="请输入内容" type="textarea" />
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
  import { listSysversionInfo, getSysversionInfo, delSysversionInfo, addSysversionInfo, updateSysversionInfo } from "@/api/xms/sysversionInfo";

  export default {
    name: "SysversionInfo",
    dicts: ['t_sys_version_info_device_type', 't_sys_version_info_status'],
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
        // 版本表格数据
        sysversionInfoList: [],
        // 弹出层标题
        title: "",
        // 是否显示弹出层
        open: false,
        // 查询参数
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          versionNo: null,
          versionUrl: null,
          status: null,
          deviceType: null,
          note: null,
          createTime: null,
          updateTime: null,
          activeFlag: null
        },
        // 表单参数
        form: {},
        // 表单校验
        rules: {
          versionNo: [
            { required: true, message: "版本号不能为空", trigger: "blur" }
          ],
          status: [
            { required: true, message: "是否强制更新不能为空", trigger: "change" }
          ],
          deviceType: [
            { required: true, message: "设备类型：ios android不能为空", trigger: "change" }
          ],
        }
      };
    },
    created() {
      this.getList();
    },
    methods: {
      /** 查询版本列表 */
      getList() {
        this.loading = true;
        listSysversionInfo(this.queryParams).then(response => {
          this.sysversionInfoList = response.rows;
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
          versionNo: null,
          versionUrl: null,
          status: null,
          deviceType: null,
          note: null,
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
        this.title = "添加版本";
      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        this.reset();
        const id = row.id || this.ids
        getSysversionInfo(id).then(response => {
          this.form = response.data;
          this.open = true;
          this.title = "修改版本";
        });
      },
      /** 提交按钮 */
      submitForm() {
        this.$refs["form"].validate(valid => {
          if (valid) {
            if (this.form.id != null) {
              updateSysversionInfo(this.form).then(response => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              });
            } else {
              addSysversionInfo(this.form).then(response => {
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
        this.$modal.confirm('是否确认删除版本编号为"' + ids + '"的数据项？').then(function() {
          return delSysversionInfo(ids);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {});
      },
      /** 导出按钮操作 */
      handleExport() {
        this.download('xms/sysversionInfo/export', {
          ...this.queryParams
        }, `sysversionInfo_${new Date().getTime()}.xlsx`)
      }
    }
  };
  </script>
