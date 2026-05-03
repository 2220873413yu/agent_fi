<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="用户ID" prop="userId" label-width="120px">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入用户id"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="质押订单号" prop="orderNo" label-width="120px">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入质押订单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
<!--      <el-form-item label="质押金额/USDT单位" prop="stakeUsdtAmount">
        <el-input
          v-model="queryParams.stakeUsdtAmount"
          placeholder="请输入质押金额/USDT单位"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->
      <el-form-item label="订单状态" prop="status" label-width="120px">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option
            v-for="dict in dict.type.t_stake_order_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="是否首单" prop="hashFirstShipOrder" label-width="120px">
        <el-select v-model="queryParams.hashFirstShipOrder" placeholder="请选择是否首单" clearable>
          <el-option
            v-for="dict in dict.type.t_user_info_is_valid"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="发货状态" prop="shipStatus" label-width="120px">
        <el-select v-model="queryParams.shipStatus" placeholder="请选择是否首单" clearable>
          <el-option
            v-for="dict in dict.type.t_stake_order_ship_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="支付hash" prop="payHash" label-width="120px">
        <el-input
          v-model="queryParams.payHash"
          placeholder="请输入支付hash"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>

<!--      <el-form-item label="剩余可产出" prop="remainingOutAmount">
        <el-input
          v-model="queryParams.remainingOutAmount"
          placeholder="请输入剩余可产出"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="出局目标产出总额" prop="allOutAmount">
        <el-input
          v-model="queryParams.allOutAmount"
          placeholder="请输入出局目标产出总额"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->
      <el-form-item label="创建时间" label-width="120px">
        <el-date-picker
          v-model="daterangeCreateTime"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="datetimerange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
<!--      <el-form-item label="支付时间" prop="payTime">
        <el-date-picker clearable
          v-model="queryParams.payTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择支付时间">
        </el-date-picker>
      </el-form-item>-->

      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
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
          v-hasPermi="['xms:stakeOrder:add']"
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
          v-hasPermi="['xms:stakeOrder:edit']"
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
          v-hasPermi="['xms:stakeOrder:remove']"
        >删除</el-button>
      </el-col>-->
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['xms:stakeOrder:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="stakeOrderList" @selection-change="handleSelectionChange">
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            v-if="scope.row.status == 0"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['xms:stakeOrder:edit']"
          >手动完成</el-button>

          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            v-if="scope.row.status == 1 && scope.row.hashFirstShipOrder == 1"
            @click="handleShip(scope.row)"
            v-hasPermi="['xms:stakeOrder:edit']"
          >去发货</el-button>

          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            v-if="scope.row.status == 1"
            @click="handleDisable(scope.row)"
            v-hasPermi="['xms:stakeOrder:edit']"
          >下架质押订单</el-button>
          <!--          <el-button
                      size="mini"
                      type="text"
                      icon="el-icon-delete"
                      @click="handleDelete(scope.row)"
                      v-hasPermi="['xms:stakeOrder:remove']"
                    >删除</el-button>-->
        </template>
      </el-table-column>
      <el-table-column type="selection" width="55" align="center" v-if="false"/>
      <el-table-column label="主键id" align="center" prop="id" v-if="false"/>
      <el-table-column label="用户ID" align="center" prop="userId" />
      <el-table-column label="质押订单号" align="center" prop="orderNo" />
      <el-table-column label="质押金额" align="center" prop="stakeUsdtAmount" />
      <el-table-column label="订单状态" align="center" prop="status" width="180">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_stake_order_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="剩余可产出" align="center" prop="remainingOutAmount" />
      <el-table-column label="出局目标产出总额" align="center" prop="allOutAmount" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="支付hash" align="center" prop="payHash" />
      <el-table-column label="购买信息" align="center" prop="productSnapshot" width="360">
        <template slot-scope="scope">
          <div v-if="scope.row.productSnapshotObj" style="display: flex; align-items: flex-start; gap: 10px; text-align: left;">
            <image-preview
              :src="getSnapshotCover(scope.row.productSnapshotObj)"
              :width="52"
              :height="52"
            />
            <div style="line-height: 1.6;">
              <div style="font-weight: 500; color: #303133;">
                {{ getSnapshotName(scope.row.productSnapshotObj) }}
              </div>
              <div style="font-size: 12px; color: #606266;">
                规格：{{ getSnapshotSkuText(scope.row.productSnapshotObj) || "-" }}
              </div>
              <div style="font-size: 12px; color: #e6a23c;">
                商品单价：{{ getSnapshotCurrency(scope.row.productSnapshotObj) }} {{ getSnapshotPrice(scope.row.productSnapshotObj) }}
              </div>
              <div style="font-size: 12px; color: #67c23a;">
                支付总价：{{ getSnapshotCurrency(scope.row.productSnapshotObj) }} {{ scope.row.stakeUsdtAmount || "-" }}
              </div>
              <div style="font-size: 12px; color: #909399;">
                购买数量：{{ getSnapshotBuyCount(scope.row) }}
              </div>
            </div>
          </div>
          <span v-else style="color: #909399;">-</span>
        </template>

      </el-table-column>

      <el-table-column label="发货状态" align="center" prop="shipStatus" >
      <template slot-scope="scope">
        <dict-tag v-if="scope.row.hashFirstShipOrder == 1" :options="dict.type.t_stake_order_ship_status" :value="scope.row.shipStatus"/>
      </template>
      </el-table-column>


<!--      <el-table-column label="商品信息" align="center" prop="cartInfoList" width="300">
        <template slot-scope="scope">
          <div v-for="(item,index) in scope.row.cartInfoList"
               :key="index">
                <span v-if="item.cartInfoMap.productInfo.attrInfo">
                   <image-preview :src="item.cartInfoMap.productInfo.attrInfo.image" :width="50" :height="50"/>
                </span>
            <span v-else>
                <image-preview :src="item.cartInfoMap.productInfo.image" :width="50" :height="50"/>
                </span>
            <span>
                  {{ item.cartInfoMap.productInfo.storeName }}
                  <span v-if="item.cartInfoMap.productInfo.attrInfo">&nbsp;{{ item.cartInfoMap.productInfo.attrInfo.sku }}</span>
                </span>
            <span> | ￥{{ item.cartInfoMap.truePrice }}×{{ item.cartInfoMap.cartNum }}</span>
          </div>
        </template>
      </el-table-column>-->
      <el-table-column label="支付时间" align="center" prop="payTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.payTime) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="修改时间" align="center" prop="updateTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
<!--      <el-table-column label="备注" align="center" prop="remark" />-->



    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改质押订单对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
<!--        <el-form-item label="用户id" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户id" />
        </el-form-item>
        <el-form-item label="质押订单号" prop="orderNo">
          <el-input v-model="form.orderNo" placeholder="请输入质押订单号" />
        </el-form-item>
        <el-form-item label="质押金额/USDT单位" prop="stakeUsdtAmount">
          <el-input v-model="form.stakeUsdtAmount" placeholder="请输入质押金额/USDT单位" />
        </el-form-item>
        <el-form-item label="状态 0:待链上支付确认,1:产出中,2:已出局(已完成),3:已过期未支付,4:已暂停产出" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.t_stake_order_status"
              :key="dict.value"
              :label="parseInt(dict.value)"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="剩余可产出" prop="remainingOutAmount">
          <el-input v-model="form.remainingOutAmount" placeholder="请输入剩余可产出" />
        </el-form-item>
        <el-form-item label="出局目标产出总额" prop="allOutAmount">
          <el-input v-model="form.allOutAmount" placeholder="请输入出局目标产出总额" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="支付时间" prop="payTime">
          <el-date-picker clearable
            v-model="form.payTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择支付时间">
          </el-date-picker>
        </el-form-item>-->
        <el-form-item label="支付hash" prop="payHash">
          <el-input v-model="form.payHash"
                    type="text"
                    maxlength="100" show-word-limit
                    placeholder="请输入支付hash" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 发货对话框 -->
    <el-dialog title="订单发货" :visible.sync="shipOpen" width="620px" append-to-body>
      <el-form ref="shipFormRef" :model="shipForm" :rules="shipRules" label-width="100px">
        <el-form-item label="收货人信息">
          <div v-if="shipForm.receiverInfoObj" style="background: #f8f9fb; border-radius: 4px; padding: 10px; line-height: 1.8;">
            <div>收货人：{{ shipForm.receiverInfoObj.userName || "-" }}</div>
            <div>手机号：{{ shipForm.receiverInfoObj.phone || "-" }}</div>
            <div>地址：{{ formatReceiverAddress(shipForm.receiverInfoObj) }}</div>
          </div>
          <span v-else style="color: #909399;">-</span>
        </el-form-item>
        <el-form-item label="物流公司" prop="shipCompany">
          <el-input v-model="shipForm.shipCompany" placeholder="请输入物流公司，如：顺丰" show-word-limit maxlength="64" />
        </el-form-item>
        <el-form-item label="物流单号" prop="shipNo">
          <el-input v-model="shipForm.shipNo" placeholder="请输入物流单号" show-word-limit maxlength="64" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="shipSubmitting" @click="submitShip">发 货</el-button>
        <el-button @click="cancelShip">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listStakeOrder, getStakeOrder, delStakeOrder,
  handleDisableStakeOrder,handleOrderShipped,
  addStakeOrder, updateStakeOrder } from "@/api/xms/stakeOrder";

export default {
  name: "StakeOrder",
  dicts: ['t_stake_order_status','t_stake_order_ship_status','t_user_info_is_valid'],
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
      // 质押订单表格数据
      stakeOrderList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 发货弹窗
      shipOpen: false,
      shipSubmitting: false,
      // 支付hash时间范围
      daterangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: null,
        orderNo: null,
        stakeUsdtAmount: null,
        shipStatus: null,
        hashFirstShipOrder: null,
        status: null,
        remainingOutAmount: null,
        allOutAmount: null,
        createTime: null,
        payTime: null,
        payHash: null
      },
      // 表单参数
      form: {},
      // 发货表单
      shipForm: {
        id: null,
        receiverInfoObj: null,
        shipCompany: "",
        shipNo: ""
      },
      // 表单校验
      rules: {
        userId: [
          { required: true, message: "用户id不能为空", trigger: "blur" }
        ],
        orderNo: [
          { required: true, message: "质押订单号不能为空", trigger: "blur" }
        ],
        payHash: [
          { required: true, message: "支付hash不能为空", trigger: "blur" }
        ],
        stakeUsdtAmount: [
          { required: true, message: "质押金额/USDT单位不能为空", trigger: "blur" }
        ],
        status: [
          { required: true, message: "状态 0:待链上支付确认,1:产出中,2:已出局(已完成),3:已过期未支付,4:已暂停产出不能为空", trigger: "change" }
        ],
      },
      shipRules: {
        shipCompany: [
          { required: true, message: "物流公司不能为空", trigger: "blur" }
        ],
        shipNo: [
          { required: true, message: "物流单号不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询质押订单列表 */
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      if (null != this.daterangeCreateTime && '' != this.daterangeCreateTime) {
        this.queryParams.params["beginCreateTime"] = this.daterangeCreateTime[0];
        this.queryParams.params["endCreateTime"] = this.daterangeCreateTime[1];
      }
      listStakeOrder(this.queryParams).then(response => {
        const rows = Array.isArray(response.rows) ? response.rows : [];
        this.stakeOrderList = rows.map(row => ({
          ...row,
          productSnapshotObj: this.parseProductSnapshot(row.productSnapshot)
        }));
        this.total = response.total;
        this.loading = false;
      });
    },
    parseProductSnapshot(snapshot) {
      if (!snapshot) {
        return null;
      }
      if (typeof snapshot === "object") {
        return snapshot;
      }
      if (typeof snapshot !== "string") {
        return null;
      }
      try {
        const parsed = JSON.parse(snapshot);
        return parsed && typeof parsed === "object" ? parsed : null;
      } catch (e) {
        return null;
      }
    },
    getSnapshotCover(snapshot) {
      if (!snapshot) {
        return "";
      }
      return snapshot.skuImageZh || snapshot.productCoverImageZh || snapshot.skuImageEn || snapshot.productCoverImageEn || "";
    },
    getSnapshotName(snapshot) {
      if (!snapshot) {
        return "-";
      }
      return snapshot.productNameZh || snapshot.productNameEn || "-";
    },
    getSnapshotSkuText(snapshot) {
      if (!snapshot) {
        return "";
      }
      return snapshot.skuTextZh || snapshot.skuTextEn || "";
    },
    getSnapshotCurrency(snapshot) {
      if (!snapshot) {
        return "USDT";
      }
      return snapshot.currency || "USDT";
    },
    getSnapshotPrice(snapshot) {
      if (!snapshot) {
        return "-";
      }
      const v = snapshot.dealPrice;
      if (v === null || v === undefined || v === "") {
        return "-";
      }
      return v;
    },
    getSnapshotBuyCount(row) {
      if (!row || !row.productSnapshotObj) {
        return "-";
      }
      const total = Number(row.stakeUsdtAmount);
      const unit = Number(row.productSnapshotObj.dealPrice);
      if (!Number.isFinite(total) || !Number.isFinite(unit) || unit <= 0) {
        return "-";
      }
      const count = total / unit;
      if (!Number.isFinite(count)) {
        return "-";
      }
      const text = count.toFixed(6).replace(/\.?0+$/, "");
      return text || "0";
    },
    parseReceiverInfo(receiverInfo) {
      if (!receiverInfo) {
        return null;
      }
      if (typeof receiverInfo === "object") {
        return receiverInfo;
      }
      if (typeof receiverInfo !== "string") {
        return null;
      }
      try {
        const parsed = JSON.parse(receiverInfo);
        return parsed && typeof parsed === "object" ? parsed : null;
      } catch (e) {
        return null;
      }
    },
    formatReceiverAddress(receiver) {
      if (!receiver) {
        return "-";
      }
      const arr = [receiver.province, receiver.city, receiver.area, receiver.detailed]
        .map(v => String(v || "").trim())
        .filter(Boolean);
      return arr.length ? arr.join(" ") : "-";
    },
    resetShipForm() {
      this.shipForm = {
        id: null,
        receiverInfoObj: null,
        shipCompany: "",
        shipNo: ""
      };
      this.shipSubmitting = false;
      this.$nextTick(() => {
        if (this.$refs.shipFormRef) {
          this.$refs.shipFormRef.resetFields();
        }
      });
    },
    handleShip(row) {
      this.resetShipForm();
      this.shipForm.id = row.id;
      this.shipForm.receiverInfoObj = this.parseReceiverInfo(row.receiverInfo);
      this.shipForm.shipCompany = row.shipCompany || "";
      this.shipForm.shipNo = row.shipNo || "";
      this.shipOpen = true;
    },
    cancelShip() {
      this.shipOpen = false;
      this.resetShipForm();
    },
    submitShip() {
      this.$refs.shipFormRef.validate(valid => {
        if (!valid) {
          return;
        }
        const data = {
          id: this.shipForm.id,
          shipCompany: String(this.shipForm.shipCompany || "").trim(),
          shipNo: String(this.shipForm.shipNo || "").trim()
        };
        this.shipSubmitting = true;
        handleOrderShipped(data).then(() => {
          this.$modal.msgSuccess("发货成功");
          this.shipOpen = false;
          this.resetShipForm();
          this.getList();
        }).finally(() => {
          this.shipSubmitting = false;
        });
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
        orderNo: null,
        stakeUsdtAmount: null,
        status: null,
        remainingOutAmount: null,
        allOutAmount: null,
        createTime: null,
        updateTime: null,
        remark: null,
        payTime: null,
        payHash: null
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
      this.title = "添加质押订单";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getStakeOrder(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改质押订单";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateStakeOrder(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addStakeOrder(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除质押订单编号为"' + ids + '"的数据项？').then(function() {
        return delStakeOrder(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 下架质押订单 */
    handleDisable(row) {
      const data = {
        id: row.id
      };
      this.$modal.confirm('是否确认下架质押订单编号为"' + row.id + '"的数据项？').then(() => {
        return handleDisableStakeOrder(data);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("下架成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/stakeOrder/export', {
        ...this.queryParams
      }, `stakeOrder_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
