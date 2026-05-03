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
<!--      <el-form-item label="线性释放天数(如270)" prop="linearDays">
        <el-input
          v-model="queryParams.linearDays"
          placeholder="请输入线性释放天数(如270)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="桶内累计应线性释放总量" prop="totalAmount">
        <el-input
          v-model="queryParams.totalAmount"
          placeholder="请输入桶内累计应线性释放总量"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="桶内已释放总量" prop="releasedAmount">
        <el-input
          v-model="queryParams.releasedAmount"
          placeholder="请输入桶内已释放总量"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="桶内剩余待释放量" prop="remainingAmount">
        <el-input
          v-model="queryParams.remainingAmount"
          placeholder="请输入桶内剩余待释放量"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->
      <el-form-item label="线性释放状态" label-width="120px" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
          <el-option
            v-for="dict in dict.type.t_stake_order_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
<!--      <el-form-item label="时间格式为:yyyymmdd,例如:20260101" prop="startTime">
        <el-input
          v-model="queryParams.startTime"
          placeholder="请输入时间格式为:yyyymmdd,例如:20260101"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="上次释放时间 格式为:yyyymmdd,例如:20260101" prop="lastReleaseTime">
        <el-input
          v-model="queryParams.lastReleaseTime"
          placeholder="请输入上次释放时间 格式为:yyyymmdd,例如:20260101"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->
      <el-form-item label="创建时间" label-width="120px">
        <el-date-picker
          v-model="daterangeCreateTime"
          end-placeholder="结束日期"
          range-separator="-"
          start-placeholder="开始日期"
          style="width: 240px"
          type="datetimerange"
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
          v-hasPermi="['xms:stakeReleaseBucket:add']"
          icon="el-icon-plus"
          plain
          size="mini"
          type="primary"
          @click="handleAdd"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:stakeReleaseBucket:edit']"
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
          v-hasPermi="['xms:stakeReleaseBucket:remove']"
          :disabled="multiple"
          icon="el-icon-delete"
          plain
          size="mini"
          type="danger"
          @click="handleDelete"
        >删除</el-button>
      </el-col>-->
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:stakeReleaseBucket:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="stakeReleaseBucketList" @selection-change="handleSelectionChange">
<!--      <el-table-column type="selection" width="55" align="center" />-->
      <el-table-column v-if="false" align="center" label="主键id" prop="id"/>
      <el-table-column align="center" label="用户ID" prop="userId" />
      <el-table-column align="center" label="释放天数" prop="linearDays" />
      <el-table-column align="center" label="剩余天数" prop="haveDays" />
      <el-table-column align="center" label="累计应释放总量" prop="totalAmount" />
      <el-table-column align="center" label="剩余待释放量" prop="remainingAmount" />
      <el-table-column align="center" label="每日释放数量" prop="dailyReleaseAmount" />
      <el-table-column align="center" label="订单状态" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_stake_order_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
<!--      <el-table-column label="时间格式为:yyyymmdd,例如:20260101" align="center" prop="startTime" />-->
<!--      <el-table-column align="center" label="上次释放时间" prop="lastReleaseTime" />-->
      <el-table-column align="center" label="来源数据">
        <template slot-scope="scope">
          <el-button type="text" @click="openSourcePreview(scope.row.sourceSnapshot)">查看</el-button>
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
            v-hasPermi="['xms:stakeReleaseBucket:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['xms:stakeReleaseBucket:remove']"
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

    <!-- 来源数据展示 -->
    <el-dialog :title="sourcePreviewTitle" :visible.sync="sourcePreviewOpen" append-to-body center width="600px">
      <div style="white-space: pre-line;">
        {{ sourcePreviewText }}
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="sourcePreviewOpen = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 添加或修改质押收益线性释放对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户id" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户id" />
        </el-form-item>
        <el-form-item label="线性释放天数(如270)" prop="linearDays">
          <el-input v-model="form.linearDays" placeholder="请输入线性释放天数(如270)" />
        </el-form-item>
        <el-form-item label="桶内累计应线性释放总量" prop="totalAmount">
          <el-input v-model="form.totalAmount" placeholder="请输入桶内累计应线性释放总量" />
        </el-form-item>
        <el-form-item label="桶内已释放总量" prop="releasedAmount">
          <el-input v-model="form.releasedAmount" placeholder="请输入桶内已释放总量" />
        </el-form-item>
        <el-form-item label="桶内剩余待释放量" prop="remainingAmount">
          <el-input v-model="form.remainingAmount" placeholder="请输入桶内剩余待释放量" />
        </el-form-item>
        <el-form-item label="状态 0:释放中,1:已完成,2:暂停" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态 0:释放中,1:已完成,2:暂停">
            <el-option
              v-for="dict in dict.type.t_stake_order_status"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="时间格式为:yyyymmdd,例如:20260101" prop="startTime">
          <el-input v-model="form.startTime" placeholder="请输入时间格式为:yyyymmdd,例如:20260101" />
        </el-form-item>
        <el-form-item label="上次释放时间 格式为:yyyymmdd,例如:20260101" prop="lastReleaseTime">
          <el-input v-model="form.lastReleaseTime" placeholder="请输入上次释放时间 格式为:yyyymmdd,例如:20260101" />
        </el-form-item>
        <el-form-item label="来源快照(json)：记录桶由哪些订单/天数/金额" prop="sourceSnapshot">
          <el-input v-model="form.sourceSnapshot" placeholder="请输入内容" type="textarea" />
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
import { listStakeReleaseBucket, getStakeReleaseBucket, delStakeReleaseBucket, addStakeReleaseBucket, updateStakeReleaseBucket } from "@/api/xms/stakeReleaseBucket";

export default {
  name: "StakeReleaseBucket",
  dicts: ['t_stake_order_status'],
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
      // 质押收益线性释放表格数据
      stakeReleaseBucketList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 来源数据弹窗
      sourcePreviewOpen: false,
      sourcePreviewTitle: "来源数据",
      sourcePreviewText: "",
      // 来源快照(json)：记录桶由哪些订单/天数/金额时间范围
      daterangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: null,
        linearDays: null,
        totalAmount: null,
        releasedAmount: null,
        remainingAmount: null,
        status: null,
        startTime: null,
        lastReleaseTime: null,
        createTime: null,
        sourceSnapshot: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        userId: [
          { required: true, message: "用户id不能为空", trigger: "blur" }
        ],
        linearDays: [
          { required: true, message: "线性释放天数(如270)不能为空", trigger: "blur" }
        ],
        totalAmount: [
          { required: true, message: "桶内累计应线性释放总量不能为空", trigger: "blur" }
        ],
        releasedAmount: [
          { required: true, message: "桶内已释放总量不能为空", trigger: "blur" }
        ],
        remainingAmount: [
          { required: true, message: "桶内剩余待释放量不能为空", trigger: "blur" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询质押收益线性释放列表 */
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      if (null != this.daterangeCreateTime && '' != this.daterangeCreateTime) {
        this.queryParams.params["beginCreateTime"] = this.daterangeCreateTime[0];
        this.queryParams.params["endCreateTime"] = this.daterangeCreateTime[1];
      }
      listStakeReleaseBucket(this.queryParams).then(response => {
        this.stakeReleaseBucketList = response.rows;
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
        linearDays: null,
        totalAmount: null,
        releasedAmount: null,
        remainingAmount: null,
        status: null,
        startTime: null,
        lastReleaseTime: null,
        createTime: null,
        updateTime: null,
        sourceSnapshot: null
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
      this.title = "添加质押收益线性释放";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getStakeReleaseBucket(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改质押收益线性释放";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateStakeReleaseBucket(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addStakeReleaseBucket(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除质押收益线性释放编号为"' + ids + '"的数据项？').then(function() {
        return delStakeReleaseBucket(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/stakeReleaseBucket/export', {
        ...this.queryParams
      }, `stakeReleaseBucket_${new Date().getTime()}.xlsx`)
    },
    openSourcePreview(snapshot) {
      this.sourcePreviewText = this.formatSourceSnapshot(snapshot);
      this.sourcePreviewOpen = true;
    },
    formatSourceSnapshot(snapshot) {
      if (!snapshot) {
        return "无来源数据";
      }
      try {
        const list = JSON.parse(snapshot);
        if (!Array.isArray(list) || list.length === 0) {
          return "无来源数据";
        }
        return list.map((item, index) => {
          const orderNo = item.orderNo || "-";
          const remainDays = item.orderRemainDays != null ? item.orderRemainDays : "-";
          const amount = item.todayLinearAmount || "-";
          return `来源${index + 1}：订单号 ${orderNo}\n剩余天数 ${remainDays}\n本次线性金额 ${amount}`;
        }).join("\n\n");
      } catch (e) {
        return snapshot;
      }
    }
  }
};
</script>
