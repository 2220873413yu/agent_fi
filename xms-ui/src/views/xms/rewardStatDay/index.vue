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

      <el-form-item label="钱包地址" label-width="120px" prop="userAccount">
        <el-input
          v-model="queryParams.userAccount"
          clearable
          placeholder="请输入钱包地址"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
<!--      <el-form-item label="日期 类似 yyyymmdd" prop="statDate">
        <el-input
          v-model="queryParams.statDate"
          placeholder="请输入日期 类似 yyyymmdd"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="静态奖励" prop="staticAmount">
        <el-input
          v-model="queryParams.staticAmount"
          placeholder="请输入静态奖励"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="动态奖励(互动奖励)" prop="dynamicAmount">
        <el-input
          v-model="queryParams.dynamicAmount"
          placeholder="请输入动态奖励(互动奖励)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="团队收益" prop="teamAmount">
        <el-input
          v-model="queryParams.teamAmount"
          placeholder="请输入团队收益"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="总收益" prop="totalAmount">
        <el-input
          v-model="queryParams.totalAmount"
          placeholder="请输入总收益"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->
      <el-form-item label="创建时间">
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
<!--      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['xms:rewardStatDay:add']"
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
          v-hasPermi="['xms:rewardStatDay:edit']"
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
          v-hasPermi="['xms:rewardStatDay:remove']"
        >删除</el-button>
      </el-col>-->
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:rewardStatDay:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="rewardStatDayList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55" />
      <el-table-column v-if="false" align="center" label="主键" prop="id"/>
      <el-table-column align="center" label="用户ID" prop="userId" />
      <el-table-column align="center" label="钱包地址" prop="userAccount" />
      <el-table-column align="center" label="静态奖励" prop="staticAmount" />
      <el-table-column align="center" label="互动奖励" prop="dynamicAmount" />
      <el-table-column align="center" label="团队收益" prop="teamAmount" />
      <el-table-column align="center" label="总收益" prop="totalAmount" />
      <el-table-column align="center" label="创建时间" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="日期" prop="statDate" />
<!--      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['xms:rewardStatDay:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['xms:rewardStatDay:remove']"
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

    <!-- 添加或修改每日奖励汇总对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户id" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户id" />
        </el-form-item>
        <el-form-item label="日期 类似 yyyymmdd" prop="statDate">
          <el-input v-model="form.statDate" placeholder="请输入日期 类似 yyyymmdd" />
        </el-form-item>
        <el-form-item label="静态奖励" prop="staticAmount">
          <el-input v-model="form.staticAmount" placeholder="请输入静态奖励" />
        </el-form-item>
        <el-form-item label="动态奖励(互动奖励)" prop="dynamicAmount">
          <el-input v-model="form.dynamicAmount" placeholder="请输入动态奖励(互动奖励)" />
        </el-form-item>
        <el-form-item label="团队收益" prop="teamAmount">
          <el-input v-model="form.teamAmount" placeholder="请输入团队收益" />
        </el-form-item>
        <el-form-item label="总收益" prop="totalAmount">
          <el-input v-model="form.totalAmount" placeholder="请输入总收益" />
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
import { listRewardStatDay, getRewardStatDay, delRewardStatDay, addRewardStatDay, updateRewardStatDay } from "@/api/xms/rewardStatDay";

export default {
  name: "RewardStatDay",
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
      // 每日奖励汇总表格数据
      rewardStatDayList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 总收益时间范围
      daterangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: null,
        userAccount: null,
        statDate: null,
        staticAmount: null,
        dynamicAmount: null,
        teamAmount: null,
        totalAmount: null,
        createTime: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        userId: [
          { required: true, message: "用户id不能为空", trigger: "blur" }
        ],
        statDate: [
          { required: true, message: "日期 类似 yyyymmdd不能为空", trigger: "blur" }
        ],
        createTime: [
          { required: true, message: "创建时间不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询每日奖励汇总列表 */
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      if (null != this.daterangeCreateTime && '' != this.daterangeCreateTime) {
        this.queryParams.params["beginCreateTime"] = this.daterangeCreateTime[0];
        this.queryParams.params["endCreateTime"] = this.daterangeCreateTime[1];
      }
      listRewardStatDay(this.queryParams).then(response => {
        this.rewardStatDayList = response.rows;
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
        statDate: null,
        staticAmount: null,
        dynamicAmount: null,
        teamAmount: null,
        totalAmount: null,
        createTime: null
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
      this.title = "添加每日奖励汇总";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getRewardStatDay(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改每日奖励汇总";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateRewardStatDay(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addRewardStatDay(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除每日奖励汇总编号为"' + ids + '"的数据项？').then(function() {
        return delRewardStatDay(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/rewardStatDay/export', {
        ...this.queryParams
      }, `rewardStatDay_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
