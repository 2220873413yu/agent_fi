<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="订单号" label-width="120px" prop="orderCode">
        <el-input
          v-model="queryParams.orderCode"
          clearable
          placeholder="请输入订单号"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="用户ID" label-width="120px" prop="userId">
        <el-input
          v-model="queryParams.userId"
          clearable
          oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
          placeholder="请输入用户ID"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>

<!--      <el-form-item label="数量" prop="amount">
        <el-input
          v-model="queryParams.amount"
          placeholder="请输入数量"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="业务类型：例如 级差、平级等等(枚举类型有多少个还不确定)1:矿机静态释放" prop="businessType">
        <el-select v-model="queryParams.businessType" placeholder="请选择业务类型：例如 级差、平级等等(枚举类型有多少个还不确定)1:矿机静态释放" clearable>
          <el-option
            v-for="dict in dict.type.xms_reward_record_business_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>-->
<!--      <el-form-item label="业务类型" prop="businessType" label-width="120px">
        <el-select v-model="queryParams.businessType" placeholder="请选择来源类型" clearable>
          <el-option
            v-for="dict in dict.type.xms_reward_record_business_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>-->

      <el-form-item label="来源类型" label-width="120px" prop="sourceType">
        <el-select v-model="queryParams.sourceType" clearable placeholder="请选择来源类型">
          <el-option
            v-for="dict in dict.type.reward_record_source_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="来源订单号" label-width="120px" prop="sourceOrderCode">
        <el-input
          v-model="queryParams.sourceOrderCode"
          clearable
          placeholder="请输入来源订单号"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="来源用户ID" label-width="120px" prop="sourceUserId">
        <el-input
          v-model="queryParams.sourceUserId"
          clearable
          placeholder="请输入来源用户"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
<!--      <el-form-item label="gtId" prop="gtId">
      <el-input
        v-model="queryParams.gtId"
        placeholder="请输入gtId"
        clearable
        @keyup.enter.native="handleQuery"
      />
    </el-form-item>
      <el-form-item label="结算时price" prop="realTimePrice">
        <el-input
          v-model="queryParams.realTimePrice"
          placeholder="请输入结算时price"
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
          v-hasPermi="['xms:rewardRecord:add']"
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
          v-hasPermi="['xms:rewardRecord:edit']"
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
          v-hasPermi="['xms:rewardRecord:remove']"
        >删除</el-button>
      </el-col>-->
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:rewardRecord:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="rewardRecordList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55" />
      <el-table-column v-if="false" align="center" label="id" prop="id"/>
      <el-table-column align="center" label="订单号" prop="orderCode" />
      <el-table-column align="center" label="用户ID" prop="userId" />
      <el-table-column align="center" label="数量" prop="amount" />
      <el-table-column align="center" label="币种" prop="coinType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_money_log_coin_type" :value="scope.row.coinType"/>
        </template>
      </el-table-column>
<!--      <el-table-column label="业务类型" align="center" prop="businessType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.xms_reward_record_business_type" :value="scope.row.businessType"/>
        </template>
      </el-table-column>-->
      <el-table-column align="center" label="来源类型" prop="sourceType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.reward_record_source_type" :value="scope.row.sourceType"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="来源订单号" prop="sourceOrderCode" />
      <el-table-column align="center" label="来源用户" prop="sourceUserId" />
<!--      <el-table-column label="gtId" align="center" prop="gtId" />-->
<!--      <el-table-column label="废弃" align="center" prop="settlementStatus" />-->
<!--      <el-table-column label="结算时价格" align="center" prop="realTimePrice" />-->
<!--      <el-table-column label="备注" align="center" prop="remark" />-->
      <el-table-column align="center" label="创建时间" prop="createTime" width="180">
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
            v-hasPermi="['xms:rewardRecord:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['xms:rewardRecord:remove']"
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

    <!-- 添加或修改奖金记录对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="订单号" prop="orderCode">
          <el-input v-model="form.orderCode" placeholder="请输入订单号" />
        </el-form-item>
        <el-form-item label="用户id" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户id" />
        </el-form-item>
        <el-form-item label="数量" prop="amount">
          <el-input v-model="form.amount" placeholder="请输入数量" />
        </el-form-item>
        <el-form-item label="业务类型：例如 级差、平级等等(枚举类型有多少个还不确定)1:矿机静态释放" prop="businessType">
          <el-select v-model="form.businessType" placeholder="请选择业务类型：例如 级差、平级等等(枚举类型有多少个还不确定)1:矿机静态释放">
            <el-option
              v-for="dict in dict.type.xms_reward_record_business_type"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="来源类型: 1:每日静态产出" prop="sourceType">
          <el-select v-model="form.sourceType" placeholder="请选择来源类型: 1:每日静态产出">
            <el-option
              v-for="dict in dict.type.reward_record_source_type"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="来源订单号" prop="sourceOrderCode">
          <el-input v-model="form.sourceOrderCode" placeholder="请输入来源订单号" />
        </el-form-item>
        <el-form-item label="来源用户" prop="sourceUserId">
          <el-input v-model="form.sourceUserId" placeholder="请输入来源用户" />
        </el-form-item>
        <el-form-item label="gtId" prop="gtId">
          <el-input v-model="form.gtId" placeholder="请输入gtId" />
        </el-form-item>
        <el-form-item label="结算时price" prop="realTimePrice">
          <el-input v-model="form.realTimePrice" placeholder="请输入结算时price" />
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
import { listRewardRecord, getRewardRecord, delRewardRecord, addRewardRecord, updateRewardRecord } from "@/api/xms/rewardRecord";

export default {
  name: "RewardRecord",
  dicts: ['reward_record_source_type','t_user_money_log_coin_type', 'xms_reward_record_business_type'],
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
      // 奖金记录表格数据
      rewardRecordList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 备注时间范围
      daterangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderCode: null,
        userId: null,
        amount: null,
        businessType: null,
        userAccount: null,
        sourceType: null,
        sourceOrderCode: null,
        sourceUserId: null,
        gtId: null,
        settlementStatus: null,
        realTimePrice: null,
        createTime: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询奖金记录列表 */
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      if (null != this.daterangeCreateTime && '' != this.daterangeCreateTime) {
        this.queryParams.params["beginCreateTime"] = this.daterangeCreateTime[0];
        this.queryParams.params["endCreateTime"] = this.daterangeCreateTime[1];
      }
      listRewardRecord(this.queryParams).then(response => {
        this.rewardRecordList = response.rows;
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
        orderCode: null,
        userId: null,
        amount: null,
        businessType: null,
        sourceType: null,
        sourceOrderCode: null,
        sourceUserId: null,
        gtId: null,
        settlementStatus: null,
        realTimePrice: null,
        remark: null,
        createTime: null,
        updateTime: null
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
      this.title = "添加奖金记录";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getRewardRecord(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改奖金记录";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateRewardRecord(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addRewardRecord(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除奖金记录编号为"' + ids + '"的数据项？').then(function() {
        return delRewardRecord(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/rewardRecord/export', {
        ...this.queryParams
      }, `rewardRecord_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
