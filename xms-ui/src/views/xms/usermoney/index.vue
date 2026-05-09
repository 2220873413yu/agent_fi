<template>
    <div class="app-container">
      <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" class="custom-form" label-width="68px" size="small">
        <el-form-item label="用户ID" prop="id">
          <el-input
            v-model="queryParams.id"
            clearable
            placeholder="请输入用户ID"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>

        <el-form-item label="钱包地址" prop="account">
          <el-input
            v-model="queryParams.account"
            placeholder="请输入钱包地址"
            clearable
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>

<!--        <el-form-item label="用户编码" prop="userCode">
          <el-input
            v-model="queryParams.userCode"
            clearable
            placeholder="请输入用户编码"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>-->
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
            v-hasPermi="['xms:usermoney:add']"
          >新增</el-button>
        </el-col> -->
        <el-col :span="1.5">
          <el-button
            v-hasPermi="['xms:usermoney:edit']"
            :disabled="single"
            icon="el-icon-edit"
            plain
            size="mini"
            type="success"
            @click="handleUpdate"
          >平台扣拨</el-button>
        </el-col>
        <!-- <el-col :span="1.5">
          <el-button
            type="danger"
            plain
            icon="el-icon-delete"
            size="mini"
            :disabled="multiple"
            @click="handleDelete"
            v-hasPermi="['xms:usermoney:remove']"
          >删除</el-button>
        </el-col>-->
        <el-col :span="1.5">
          <el-button
            v-hasPermi="['xms:usermoney:export']"
            icon="el-icon-download"
            plain
            size="mini"
            type="warning"
            @click="handleExport"
          >导出</el-button>
        </el-col>
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="usermoneyList" :default-sort="defaultSort" @selection-change="handleSelectionChange" @sort-change="handleSortChange">
        <el-table-column align="center" type="selection" width="55" />
        <el-table-column align="center" label="用户ID" prop="id" />
        <el-table-column align="center" label="钱包地址" prop="account"/>
<!--        <el-table-column align="center" label="用户编码" prop="userCode"/>-->
        <el-table-column :sort-orders="['descending', 'ascending']" align="center" label="USDT" prop="validNum1" sortable="custom" />
        <el-table-column :sort-orders="['descending', 'ascending']" align="center" label="AFI" prop="validNum2" sortable="custom" />
<!--
        <el-table-column :sort-orders="['descending', 'ascending']" align="center" label="OORT" prop="validNum3" sortable="custom"/>
        <el-table-column :sort-orders="['descending', 'ascending']" align="center" label="锁定USDT" prop="validNum4" sortable="custom"/>
        <el-table-column :sort-orders="['descending', 'ascending']" align="center" label="产出DFC" prop="validNum5" sortable="custom"/>-->
<!--        <el-table-column label="链信值" align="center" prop="validNum4" sortable="custom" :sort-orders="['descending', 'ascending']" />-->
<!--
        <el-table-column label="资产5余额数" align="center" prop="validNum5" />
        <el-table-column label="资产6余额数" align="center" prop="validNum6" />
        <el-table-column label="资产7余额数" align="center" prop="validNum7" />
        <el-table-column label="资产8余额数" align="center" prop="validNum8" />
        <el-table-column label="资产9余额数" align="center" prop="validNum9" />-->
<!--        <el-table-column label="创建时间" align="center" prop="createTime" >
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>-->
        <el-table-column align="center" label="修改时间" prop="updateTime" >
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.updateTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
          <template slot-scope="scope">
            <el-button
              v-hasPermi="['xms:usermoney:edit']"
              icon="el-icon-edit"
              size="mini"
              type="text"
              @click="handleUpdate(scope.row)"
            >平台扣拨</el-button>
            <!-- <el-button
              size="mini"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['xms:usermoney:remove']"
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

      <!-- 添加或修改用户钱包对话框 -->
      <el-dialog :close-on-click-modal="false" :title="title" :visible.sync="open" append-to-body width="500px">
        <el-form ref="form" :model="form" :rules="rules" class="custom-form" label-width="100px">
<!--          <el-form-item label="用户ID" prop="id">
            <el-input v-model="form.id" placeholder="请输入用户ID" :disabled="true" />
          </el-form-item>-->
          <el-form-item label="用户昵称" prop="account">
            <el-input v-model="form.account" :disabled="true" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="用户编码" prop="userCode">
            <el-input v-model="form.userCode" :disabled="true" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="资产类型" prop="coinType">
            <el-select v-model="form.coinType" placeholder="请选择资产">
              <el-option
                v-for="dict in dict.type.t_user_money_log_coin_type"
                :key="dict.value"
                :label="dict.label"
                :value="parseInt(dict.value)"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="变更数" prop="changeBalance">
            <el-input v-model="form.changeBalance" placeholder="请输入变更数" type=number />
          </el-form-item>

        <el-form-item :rules="[{ required: true, message: '请输入google验证码' }]" label="google验证码" prop="autoCode">
          <el-input v-model="form.autoCode" placeholder="google验证码"></el-input>
        </el-form-item>
<!--        <el-form-item v-if="systemAuth === '2'" :rules="[{ required: true, message: '输入手机验证码' }]" label="手机验证码" prop="autoCode">
          <el-input v-model="form.autoCode" placeholder="输入手机验证码"></el-input>
          <el-button class="primary" style="background: #00e1ffe5"  @click="sendCode(1)">获取手机验证码</el-button>
        </el-form-item>-->

        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button style="width: 100px;" type="primary" @click="submitForm">确 定</el-button>
          <el-button style="width: 100px;" @click="cancel">取 消</el-button>
        </div>
      </el-dialog>
    </div>
  </template>

  <script>
  import { listUsermoney, getUsermoney, delUsermoney, addUsermoney, updateUsermoney } from "@/api/xms/usermoney";
  import { getSystemAuth } from "@/api/system/config";
  import {sendCode} from "@/api/system/user";
  import {sendEmailCode} from "@/api/login";

  export default {
    name: "Usermoney",
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
        systemAuth:'',
        // 总条数
        total: 0,
        // 用户钱包表格数据
        usermoneyList: [],
        // 弹出层标题
        title: "",
        // 是否显示弹出层
        open: false,
        // 查询参数
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          userCode: null,
          id: null,
          account: null
        },
        defaultSort: {prop: 'id', order: 'descending'},
        // 表单参数
        form: {},
        // 表单校验
        rules: {
          userId: [
            { required: true, message: "用户ID不能为空", trigger: "blur" }
          ],
          coinType: [
            { required: true, message: "请选择资产类型", trigger: "blur" }
          ],
          changeBalance: [
            { required: true, message: "请输入变更金额", trigger: "blur" }
          ],
          autoCode: [
            { required: true, message: "验证码不能为空", trigger: "blur" }
          ]
        },
        isCounting: false,
        countDown: 30,
        countText: "获取验证码",
        intervalId: null
      };
    },
    created() {
      this.getList();
      this.getSystemAuth();
    },
    methods: {
      /** 排序触发事件 */
      handleSortChange(column, prop, order) {
        this.queryParams.orderByColumn = column.prop;
        this.queryParams.isAsc = column.order;
        this.getList();
      },
      /** 查询用户钱包列表 */
      getList() {
        this.loading = true;
        listUsermoney(this.queryParams).then(response => {
          this.usermoneyList = response.rows;
          this.total = response.total;
          this.loading = false;
        });
      },
      /** 查询用户信息列表 */
      getSystemAuth() {
        getSystemAuth().then(response => {
          this.systemAuth = response.msg;
        });
      },
      sendCode(type){
        sendCode(type).then(response => {
          this.$modal.msgSuccess(response.msg);
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
          validNum1: null,
          validNum2: null,
          validNum3: null,
          validNum4: null,
          validNum5: null,
          validNum6: null,
          validNum7: null,
          validNum8: null,
          validNum9: null,
          createTime: null,
          updateTime: null,
          account: null,
          version: null,
          activeFlag: null,
          changeBalance: null,
          coinType: null,
          autoCode: null,
          email: null,
          emailCode: null
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
        this.title = "添加用户钱包";
      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        this.reset();
        const id = row.id || this.ids
        getUsermoney(id).then(response => {
          this.form = response.data;
          this.open = true;
          this.title = "修改用户钱包";
        });
      },
      /** 提交按钮 */
      submitForm() {
        this.$refs["form"].validate(valid => {
          if (valid) {
            if (this.form.id != null) {
              updateUsermoney(this.form).then(response => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              });
            } else {
              addUsermoney(this.form).then(response => {
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
        this.$modal.confirm('是否确认删除用户钱包编号为"' + ids + '"的数据项？').then(function() {
          return delUsermoney(ids);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {});
      },
      /** 导出按钮操作 */
      handleExport() {
        this.download('xms/usermoney/export', {
          ...this.queryParams
        }, `usermoney_${new Date().getTime()}.xlsx`)
      }
    }
  };
  </script>

  <style scoped>
  .custom-form .el-form-item {
    margin-bottom: 22px;
  }

  .custom-form .el-input {
    font-size: 14px;
  }

  .custom-form .el-button {
    border-radius: 4px;
    font-weight: 500;
  }

  .custom-form .el-select {
    width: 100%;
  }

  .el-dialog {
    border-radius: 8px;
  }

  .el-dialog__header {
    padding: 20px;
    border-bottom: 1px solid #f0f0f0;
  }

  .el-dialog__body {
    padding: 30px 20px;
  }

  .el-dialog__footer {
    padding: 10px 20px 20px;
    border-top: 1px solid #f0f0f0;
  }

  .dialog-footer {
    text-align: right;
  }

  .dialog-footer .el-button {
    min-width: 100px;
  }
  </style>
