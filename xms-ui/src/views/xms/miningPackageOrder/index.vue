<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="矿机编号" label-width="120px" prop="id">
        <el-input
          v-model="queryParams.id"
          oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
          placeholder="请输入矿机编号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="订单号" label-width="120px" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="主订单号" label-width="120px" prop="masterOrderNo">
        <el-input
          v-model="queryParams.masterOrderNo"
          placeholder="请输入主订单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="用户ID" label-width="120px" prop="userId">
        <el-input
          v-model="queryParams.userId"
          oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
          placeholder="请输入用户ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
<!--      <el-form-item label="支付金额" prop="payPrice">
        <el-input
          v-model="queryParams.payPrice"
          placeholder="请输入支付金额"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="运行天数" prop="runDays">
        <el-input
          v-model="queryParams.runDays"
          placeholder="请输入运行天数"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->
      <el-form-item label="订单来源" label-width="120px" prop="sourceType">
        <el-select v-model="queryParams.sourceType" clearable placeholder="请选择订单来源">
          <el-option
            v-for="dict in dict.type.t_mining_package_order_source_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
<!--      <el-form-item label="每日收益" prop="dayReward">
        <el-input
          v-model="queryParams.dayReward"
          placeholder="请输入每日收益"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="累计收益" prop="totalReward">
        <el-input
          v-model="queryParams.totalReward"
          placeholder="请输入累计收益"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->
      <el-form-item label="状态" label-width="120px" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
          <el-option
            v-for="dict in dict.type.t_mining_package_order_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
<!--      <el-form-item label="购买矿机业务状态 0:未处理,1:已处理" prop="bizStatus">
        <el-select v-model="queryParams.bizStatus" placeholder="请选择购买矿机业务状态 0:未处理,1:已处理" clearable>
          <el-option
            v-for="dict in dict.type.t_user_info_is_valid"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="质押矿机业务状态 0:未处理,1:已处理" prop="bizStatus1">
        <el-input
          v-model="queryParams.bizStatus1"
          placeholder="请输入质押矿机业务状态 0:未处理,1:已处理"
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
          v-hasPermi="['xms:miningPackageOrder:add']"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          type="primary"
        >新增</el-button>
      </el-col>-->
<!--      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['xms:miningPackageOrder:edit']"
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
          v-hasPermi="['xms:miningPackageOrder:remove']"
        >删除</el-button>
      </el-col>-->
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:miningPackageOrder:export']"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          type="warning"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="miningPackageOrderList" @selection-change="handleSelectionChange">
<!--      <el-table-column type="selection" width="55" align="center" />-->
            <el-table-column align="center" class-name="small-padding fixed-width" label="操作" width="100">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.status > 0"
            v-hasPermi="['xms:miningPackageOrder:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleEditDayReward(scope.row)"
          >修改每日收益</el-button>
          <el-button
            v-if="scope.row.status >1"
            v-hasPermi="['xms:miningPackageOrder:remove']"
            :icon="scope.row.status == 3 ? 'el-icon-video-play' : 'el-icon-video-pause'"
            size="mini"
            type="text"
            @click="handleToggleStatus(scope.row)"
          >{{ scope.row.status == 3 ? '启用' : '暂停' }}</el-button>

          <el-button
            v-if="scope.row.status == 1 && scope.row.stakeType == 2"
            v-hasPermi="['xms:miningPackageOrder:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleShipment(scope.row)"
          >{{ scope.row.shippingStatus == 0 ? '发货' : '修改物流' }}</el-button>
        </template>
      </el-table-column>
      <el-table-column v-if="false" align="center" label="主键id" prop="id"/>
      <el-table-column align="center" label="矿机编号" prop="id" />
      <el-table-column align="center" label="订单号" prop="orderNo" />
      <el-table-column align="center" label="主订单号" prop="masterOrderNo" />
      <el-table-column align="center" label="用户ID" prop="userId" />
      <el-table-column align="center" label="支付方式" prop="payType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_mining_package_order_pay_type" :value="scope.row.payType"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="支付USDT" prop="payValidNum1" />
      <el-table-column align="center" label="支付DFC" prop="payValidNum2" />
      <el-table-column align="center" label="支付锁定USDT" prop="payValidNum4" />
      <el-table-column align="center" label="下单时dfc价格" prop="dfcPrice">
        <template slot-scope="scope">
          <span v-if="scope.row.payType==2">{{ scope.row.dfcPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="运行天数" prop="runDays" />
      <el-table-column align="center" label="订单来源" prop="sourceType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_mining_package_order_source_type" :value="scope.row.sourceType"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="每日收益" prop="dayReward" />
      <el-table-column align="center" label="累计收益" prop="totalReward" />
      <el-table-column align="center" label="物流公司" prop="shippingCompany" />
      <el-table-column align="center" label="物流单号" prop="trackingNo" />

      <el-table-column align="center" label="质押状态" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_mining_package_order_status" :value="scope.row.status"/>
        </template>
      </el-table-column>

      <el-table-column align="center" label="质押类型" prop="stakeType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_mining_package_order_stake_type" :value="scope.row.stakeType"/>
        </template>
      </el-table-column>

      <el-table-column align="center" label="启动期剩余天数" prop="stakeStartupRemainingDays" />


      <el-table-column align="center" label="购买矿机业务状态" prop="bizStatus">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_info_is_valid" :value="scope.row.bizStatus"/>
        </template>
      </el-table-column>

      <el-table-column align="center" label="质押矿机业务状态" prop="bizStatus1">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_info_is_valid" :value="scope.row.bizStatus1"/>
        </template>
      </el-table-column>


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


    </el-table>

    <pagination
      v-show="total>0"
      :limit.sync="queryParams.pageSize"
      :page.sync="queryParams.pageNum"
      :total="total"
      @pagination="getList"
    />

    <!-- 添加或修改矿机订单对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
<!--        <el-form-item label="矿机编号" prop="miningNo">
          <el-input v-model="form.miningNo" placeholder="请输入矿机编号" />
        </el-form-item>
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="form.orderNo" placeholder="请输入订单号" />
        </el-form-item>-->
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户ID" />
        </el-form-item>

        <el-form-item label="支付金额" prop="payPrice">
          <el-input v-model="form.payPrice" placeholder="请输入支付金额" />
        </el-form-item>
        <!--
        <el-form-item label="运行天数" prop="runDays">
          <el-input v-model="form.runDays" placeholder="请输入运行天数" />
        </el-form-item>
        <el-form-item label="订单来源" prop="sourceType">
          <el-select v-model="form.sourceType" placeholder="请选择订单来源 0:购买,1:后台拨付">
            <el-option
              v-for="dict in dict.type.t_mining_package_order_source_type"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>

         <el-form-item label="每日收益" prop="dayReward">
          <el-input v-model="form.dayReward" placeholder="请输入每日收益" />
        </el-form-item>
        <el-form-item label="累计收益" prop="totalReward">
          <el-input v-model="form.totalReward" placeholder="请输入累计收益" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.t_mining_package_order_status"
              :key="dict.value"
              :label="parseInt(dict.value)"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="购买矿机业务状态 0:未处理,1:已处理" prop="bizStatus">
          <el-radio-group v-model="form.bizStatus">
            <el-radio
              v-for="dict in dict.type.t_user_info_is_valid"
              :key="dict.value"
              :label="parseInt(dict.value)"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="质押矿机业务状态 0:未处理,1:已处理" prop="bizStatus1">
          <el-input v-model="form.bizStatus1" placeholder="请输入质押矿机业务状态 0:未处理,1:已处理" />
        </el-form-item>-->
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 发货对话框（独立，不影响原业务） -->
    <el-dialog :title="shipmentTitle" :visible.sync="shipmentOpen" append-to-body width="500px">
      <el-form ref="shipmentForm" :model="shipmentForm" :rules="shipmentRules" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="shipmentForm.addressName" :readonly="true" placeholder="无" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="shipmentForm.addressPhone" :readonly="true" placeholder="无" />
        </el-form-item>
        <el-form-item label="收货地址">
          <el-input v-model="shipmentForm.addressText" :readonly="true" :rows="3" placeholder="无" type="textarea" />
        </el-form-item>
        <el-form-item label="物流公司" prop="shippingCompany">
          <el-input v-model="shipmentForm.shippingCompany"
                    maxlength="40"
                    placeholder="请输入物流公司"
                    show-word-limit />
        </el-form-item>
        <el-form-item label="物流单号" prop="trackingNo">
          <el-input v-model="shipmentForm.trackingNo"
                    maxlength="40"
                    placeholder="请输入物流单号"
                    show-word-limit />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitShipment">确 定</el-button>
        <el-button @click="cancelShipment">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 修改每日收益对话框（独立，不影响原业务） -->
    <el-dialog :title="dayRewardTitle" :visible.sync="dayRewardOpen" append-to-body width="400px">
      <el-form ref="dayRewardForm" :model="dayRewardForm" :rules="dayRewardRules" label-width="90px">
        <el-form-item label="每日收益" prop="dayReward">
          <el-input v-model="dayRewardForm.dayReward"
                    placeholder="请输入每日收益"
                    @input="value => handleDayRewardInput('dayReward', value)" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitDayReward">确 定</el-button>
        <el-button @click="cancelDayReward">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listMiningPackageOrder, getMiningPackageOrder,
  handleProcessShipment,handleStopOrOpenOrder,handleUpdateDayReward,
  delMiningPackageOrder, addMiningPackageOrder, updateMiningPackageOrder } from "@/api/xms/miningPackageOrder";

export default {
  name: "MiningPackageOrder",
  dicts: ['t_mining_package_order_source_type', 't_mining_package_order_status', 't_user_info_is_valid',
  't_mining_package_order_pay_type','t_mining_package_order_stake_type'],
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
      // 矿机订单表格数据
      miningPackageOrderList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 发货弹窗
      shipmentOpen: false,
      shipmentTitle: "发货",
      // 修改每日收益弹窗
      dayRewardOpen: false,
      dayRewardTitle: "修改每日收益",
      // 是否删除时间范围
      daterangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        miningNo: null,
        orderNo: null,
        masterOrderNo: null,
        userId: null,
        payPrice: null,
        runDays: null,
        sourceType: null,
        dayReward: null,
        totalReward: null,
        status: null,
        bizStatus: null,
        bizStatus1: null
      },
      // 表单参数
      form: {},
      shipmentForm: {},
      dayRewardForm: {},
      // 表单校验
      rules: {
        userId: [
          { required: true, message: "用户id不能为空", trigger: "blur" }
        ]
      },
      shipmentRules: {
        shippingCompany: [
          { required: true, message: "物流公司不能为空", trigger: "blur" }
        ],
        trackingNo: [
          { required: true, message: "物流单号不能为空", trigger: "blur" }
        ]
      },
      dayRewardRules: {
        dayReward: [
          { required: true, message: "每日收益不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询矿机订单列表 */
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      if (null != this.daterangeCreateTime && '' !== this.daterangeCreateTime) {
        this.queryParams.params["beginCreateTime"] = this.daterangeCreateTime[0];
        this.queryParams.params["endCreateTime"] = this.daterangeCreateTime[1];
      }
      listMiningPackageOrder(this.queryParams).then(response => {
        this.miningPackageOrderList = response.rows;
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
        miningNo: null,
        orderNo: null,
        userId: null,
        payPrice: null,
        runDays: null,
        sourceType: null,
        dayReward: null,
        totalReward: null,
        createTime: null,
        updateTime: null,
        status: null,
        bizStatus: null,
        bizStatus1: null
      };
      this.resetForm("form");
    },
    resetShipment() {
      this.shipmentForm = {
        id: null,
        remark: null,
        addressName: null,
        addressPhone: null,
        addressText: null,
        shippingCompany: null,
        trackingNo: null
      };
      this.resetForm("shipmentForm");
    },
    resetDayReward() {
      this.dayRewardForm = {
        id: null,
        dayReward: null
      };
      this.resetForm("dayRewardForm");
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
      this.title = "添加矿机订单";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getMiningPackageOrder(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改矿机订单";
      });
    },
    handleShipment(row) {
      this.resetShipment();
      const id = row.id || this.ids
      getMiningPackageOrder(id).then(response => {
        const data = response.data || {};
        const addressInfo = this.parseAddressInfo(data.remark);
        this.shipmentForm = {
          id: data.id,
          remark: data.remark,
          addressName: addressInfo.name,
          addressPhone: addressInfo.phone,
          addressText: addressInfo.address,
          shippingCompany: data.shippingCompany,
          trackingNo: data.trackingNo
        };
        this.shipmentOpen = true;
        this.shipmentTitle = "发货";
      });
    },
    handleEditDayReward(row) {
      this.resetDayReward();
      this.dayRewardForm.id = row.id;
      this.dayRewardForm.dayReward = row.dayReward;
      this.dayRewardOpen = true;
      this.dayRewardTitle = "修改每日收益";
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateMiningPackageOrder(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addMiningPackageOrder(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    submitShipment() {
      this.$refs["shipmentForm"].validate(valid => {
        if (valid) {
          handleProcessShipment({
            id: this.shipmentForm.id,
            shippingCompany: this.shipmentForm.shippingCompany,
            trackingNo: this.shipmentForm.trackingNo
          }).then(response => {
            this.$modal.msgSuccess("发货成功");
            this.shipmentOpen = false;
            this.getList();
          });
        }
      });
    },
    submitDayReward() {
      this.$refs["dayRewardForm"].validate(valid => {
        if (valid) {
          handleUpdateDayReward({
            id: this.dayRewardForm.id,
            dayReward: this.dayRewardForm.dayReward
          }).then(() => {
            this.$modal.msgSuccess("修改成功");
            this.dayRewardOpen = false;
            this.getList();
          });
        }
      });
    },
    cancelDayReward() {
      this.dayRewardOpen = false;
      this.resetDayReward();
    },
    cancelShipment() {
      this.shipmentOpen = false;
      this.resetShipment();
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除矿机订单编号为"' + ids + '"的数据项？').then(function() {
        return delMiningPackageOrder(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleToggleStatus(row) {
      const id = row.id;
      const nextStatus = row.status == 3 ? 2 : 3;
      const actionText = row.status == 3 ? "启用" : "暂停";
      this.$modal.confirm(`确认要${actionText}该订单吗？`).then(() => {
        return handleStopOrOpenOrder({ id: id, status: nextStatus });
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(`${actionText}成功`);
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/miningPackageOrder/export', {
        ...this.queryParams
      }, `miningPackageOrder_${new Date().getTime()}.xlsx`)
    },
    parseAddressInfo(remark) {
      const emptyResult = { name: "", phone: "", address: "" };
      if (!remark) {
        return emptyResult;
      }
      try {
        const address = JSON.parse(remark);
        const addressText = [
          address.province,
          address.city,
          address.area,
          address.detailed
        ].filter(Boolean).join("");
        return {
          name: address.userName || "",
          phone: address.phone || "",
          address: addressText
        };
      } catch (e) {
        return {
          name: "",
          phone: "",
          address: remark
        };
      }
    }
    ,
    handleDayRewardInput(field, value) {
      this.dayRewardForm[field] = this.normalizeDecimal2(value);
    },
    normalizeDecimal2(value) {
      if (value === null || value === undefined) {
        return "";
      }
      const raw = String(value).replace(/[^\d.]/g, "");
      if (!raw) {
        return "";
      }
      const endsWithDot = raw.endsWith(".");
      const parts = raw.split(".");
      let intPart = parts[0] || "0";
      intPart = intPart.replace(/^0+(?=\d)/, "");
      if (intPart === "") {
        intPart = "0";
      }
      let decimalPart = parts[1] || "";
      decimalPart = decimalPart.slice(0, 2);
      if (endsWithDot && decimalPart === "") {
        return `${intPart}.`;
      }
      return decimalPart ? `${intPart}.${decimalPart}` : intPart;
    }
  }
};
</script>
