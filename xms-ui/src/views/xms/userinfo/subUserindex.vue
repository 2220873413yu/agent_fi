<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="100px" size="small">
      <el-form-item label="用户ID" label-width="120px" prop="userId">
        <el-input
          v-model="queryParams.userId"
          clearable
          oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
          placeholder="请输入用户ID"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>

      <el-form-item label="用户编码" label-width="120px" prop="userCode">
        <el-input
          v-model="queryParams.userCode"
          clearable
          placeholder="请输入用户编码"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>


      <el-form-item label="钱包地址" label-width="120px" prop="account">
        <el-input
          v-model="queryParams.account"
          clearable
          placeholder="请输入钱包地址"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>

      <!--      <el-form-item label="结算等级" label-width="120px" prop="finaGameLevel">
              <el-select v-model="queryParams.finaGameLevel" clearable placeholder="请选择">
                <el-option
                  v-for="dict in dict.type.t_user_info_game_level"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>-->

      <el-form-item label="用户等级" label-width="120px" prop="gameLevel">
        <el-select v-model="queryParams.gameLevel" clearable placeholder="请选择">
          <el-option
            v-for="dict in dict.type.t_user_info_game_level"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="节点赠送" label-width="120px" prop="minGameLevel">
        <el-select v-model="queryParams.minGameLevel" placeholder="请选择" clearable>
          <el-option
            v-for="dict in dict.type.t_user_info_game_level"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="节点等级" label-width="120px" prop="nodeLevel">
        <el-select v-model="queryParams.nodeLevel" clearable placeholder="请选择节点等级">
          <el-option
            v-for="dict in dict.type.t_node_plan_node_level"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="账号状态" label-width="120px" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
          <el-option
            v-for="dict in dict.type.t_user_info_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="提现状态" prop="withdrawalOpenOrClose" label-width="120px">
        <el-select v-model="queryParams.withdrawalOpenOrClose" placeholder="请选择" clearable>
          <el-option
            v-for="dict in dict.type.biz_open_or_close"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <!--      <el-form-item label="是否有效用户" prop="isValid" label-width="120px">
              <el-select v-model="queryParams.isValid" placeholder="请选择" clearable>
                <el-option
                  v-for="dict in dict.type.t_user_info_is_valid"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>   -->




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
      <!-- <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['xms:userinfo:add']"
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
          v-hasPermi="['xms:userinfo:edit']"
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
          v-hasPermi="['xms:userinfo:remove']"
        >删除</el-button>
      </el-col>
       -->
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:userinfo:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="userinfoList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55" />

      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['xms:usermoney:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            v-if="scope.row.umbrellaNum>0"
            v-hasPermi="['xms:userinfo:childList']"
            icon="el-icon-search"
            size="mini"
            type="text"
            @click="toChildUser(scope.row)"
          >团队用户</el-button>
          <el-button
            v-if="scope.row.totalSubNum>0"
            v-hasPermi="['xms:userinfo:childList']"
            icon="el-icon-search"
            size="mini"
            type="text"
            @click="toSubUser(scope.row)"
          >直推用户</el-button>
        </template>
      </el-table-column>
      <el-table-column align="center" label="用户ID" prop="userId"/>
      <!--      <el-table-column align="center" label="用户编码" prop="userCode" />-->

      <!--      <el-table-column label="昵称" align="center" prop="nickName" />
            <el-table-column label="头像" align="center" prop="avatar" width="100">
              <template slot-scope="scope">
                <image-preview :src="scope.row.avatar" :width="50" :height="50"/>
              </template>
            </el-table-column>
            <el-table-column label="openId" align="center" prop="juOpenId" show-overflow-tooltip width="150"/>-->
      <el-table-column align="center" label="钱包地址" prop="account" width="150"/>

      <!--      <el-table-column align="center" label="是否有效用户" prop="isValid" width="80">
              <template slot-scope="scope">
                <dict-tag :options="dict.type.t_user_info_is_valid" :value="scope.row.isValid"/>
              </template>
            </el-table-column> -->



      <el-table-column align="center" label="邀请用户信息" prop="inviteUserCode" width="180">
        <template slot-scope="scope">
          <div class="exchange-info" style="text-align: left;">
            用户编码: {{scope.row.inviteUserCode}}<br/>
            用户ID: {{scope.row.inviteUserId}}<br/>
          </div>
        </template>
      </el-table-column>

      <el-table-column align="center" label="等级信息" width="180">
        <template slot-scope="scope">
          <div style="text-align: left;">
            <div style="display: flex; align-items: center; flex-wrap: nowrap; gap: 4px; margin-bottom: 6px;">
              <span>真实等级:</span>
              <dict-tag :options="dict.type.t_user_info_game_level" :value="scope.row.gameLevel"/>
            </div>
            <div style="display: flex; align-items: center; flex-wrap: nowrap; gap: 4px;">
              <span>节点赠送:</span>
              <dict-tag :options="dict.type.t_user_info_game_level" :value="scope.row.minGameLevel"/>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column align="nodeLevel" label="节点等级">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_node_plan_node_level" :value="scope.row.nodeLevel"/>
        </template>
      </el-table-column>

      <!--
            -->
      <!--      <el-table-column label="今日新增业绩" align="center" prop="todayPerformance" width="150"/>
            <el-table-column label="团队提现总额" align="center" prop="teamWithdrawBalance" width="150"/>-->
      <!--      <el-table-column label="今日新增" align="center" width="150">
              <template slot-scope="scope">
                <div class="exchange-info" style="text-align: left;">
                  个人业绩: {{ scope.row.todayPerformance }}<br>
                  团队人数: {{ scope.row.todayPerformance }}<br>
                  团队业绩: {{ scope.row.todayTeamPerformance }}<br>
                </div>
              </template>
            </el-table-column>-->
      <!--      <el-table-column label="小区业绩" align="center" prop="communityPerformance" />-->
      <!--      <el-table-column label="未释放数量信息" align="center" width="150">
              <template slot-scope="scope">
                <div class="exchange-info" style="text-align: left;">
                  个人: {{ scope.row.userRemainAmount }}<br>
                  团队: {{ scope.row.teamRemainAmount }} <br>
                </div>
              </template>
            </el-table-column>-->
      <!--      <el-table-column label="提现信息(BOOMAI)" align="center" width="150">
              <template slot-scope="scope">
                <div class="exchange-info" style="text-align: left;">
                  团队提现: {{ scope.row.teamWithdrawBalance }}<br>
                  我的提现: {{ scope.row.withdrawalBalance }} <br>
                </div>
              </template>
            </el-table-column>-->

      <!--      <el-table-column label="今日我的销毁" align="center" width="150">
              <template slot-scope="scope">
                <div class="exchange-info" style="text-align: left;">
                  {{ scope.row.todayPerformance }} USDT<br>
                  {{ scope.row.todayPerformanceV1 }} BOOMAI<br>
                </div>
              </template>
            </el-table-column>-->

      <!--      <el-table-column label="今日团销毁" align="center" width="150">
              <template slot-scope="scope">
                <div class="exchange-info" style="text-align: left;">
                  {{ scope.row.todayUmbrellaPerformance }} USDT<br>
                  {{ scope.row.todayUmbrellaPerformanceV1 }} BOOMAI<br>
                </div>
              </template>
            </el-table-column>-->


      <!--      <el-table-column align="center" label="节点相关" width="150">
              <template slot-scope="scope">
                <div class="exchange-info" style="text-align: left;">
                  直推节点: {{ scope.row.subPerformance }} <br>
                  团队节点: {{ scope.row.umbrellaPerformance }} <br>
                </div>
              </template>
            </el-table-column>-->
      <el-table-column align="center" label="团队相关"width="150">
        <template slot-scope="scope">
          <div class="exchange-info" style="text-align: left;">
            直推用户数: {{ scope.row.subNum }}<br>
            团队用户数: {{ scope.row.umbrellaNum }}<br>
          </div>
        </template>
      </el-table-column>

      <el-table-column align="center" label="节点业绩"width="150">
        <template slot-scope="scope">
          <div class="exchange-info" style="text-align: left;">
            直推: {{ scope.row.subNodePerformance }}<br>
            团队: {{ scope.row.nodeTeamPerformance }}<br>
          </div>
        </template>
      </el-table-column>

      <el-table-column align="center" label="全球分红权重" width="190">
        <template slot-scope="scope">
          <div class="exchange-info" style="text-align: left;">
            个人权重: {{ scope.row.globalDividendWeight || 0 }}<br>
            团队权重: {{ scope.row.globalDividendUmbrellaWeight || 0 }}<br>
            小区权重: {{ scope.row.globalDividendCommunityWeight || 0 }}
          </div>
        </template>
      </el-table-column>

      <!--      <el-table-column align="center" label="质押业绩"width="150">
              <template slot-scope="scope">
                <div class="exchange-info" style="text-align: left;">
                  我的业绩: {{ scope.row.performance }}<br>
                  团队业绩: {{ scope.row.umbrellaPerformance }}<br>
                  大区业绩: {{ scope.row.maxLegPerformance }}<br>
                  小区业绩: {{ scope.row.communityPerformance }}<br>
                </div>
              </template>
            </el-table-column>-->

      <!--      <el-table-column label="提现信息" align="center"width="150">
              <template slot-scope="scope">
                <div class="exchange-info" style="text-align: left;">
                  个人提现: {{ scope.row.withdrawalBalance }}<br>
                  团队提现: {{ scope.row.teamWithdrawBalance }}<br>
                </div>
              </template>
            </el-table-column>-->

      <el-table-column align="center" label="账号状态" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_info_status" :value="scope.row.status"/>
        </template>
      </el-table-column>

      <el-table-column align="center" label="提现状态" prop="withdrawalOpenOrClose">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.biz_open_or_close" :value="scope.row.withdrawalOpenOrClose"/>
        </template>
      </el-table-column>

      <!--      <el-table-column label="我的算力" align="center" prop="performanceV1" width="150"/>-->
      <!--      <el-table-column label="业绩相关(BOOMAI)" align="center" width="150">
              <template slot-scope="scope">
                <div class="exchange-info" style="text-align: left;">
                  我的业绩: {{ scope.row.performanceV1 }} <br>
                  直推业绩: {{ scope.row.subPerformanceV1 }} <br>
                  团队业绩: {{ scope.row.umbrellaPerformanceV1 }} <br>
                </div>
              </template>
            </el-table-column>-->
      <!--      -->
      <!--      <el-table-column label="备注" align="center" prop="mnemonic" />-->



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
    <!-- 添加或修改用户信息对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="180px">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="form.userId" :disabled="true"/>
        </el-form-item>

        <el-form-item label="钱包地址" prop="account">
          <el-input v-model="form.account"
                    :disabled="true"
                    type="textarea"/>
        </el-form-item>

<!--        <el-form-item label="等级" prop="gameLevel">
          <el-select v-model="form.gameLevel" placeholder="请选择">
            <el-option
              v-for="dict in dict.type.t_user_info_game_level"
              :key="dict.value"
              :disabled="true"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>


        <el-form-item label="虚拟等级" prop="minGameLevel">
          <el-select v-model="form.minGameLevel" placeholder="请选择">
            <el-option
              v-for="dict in dict.type.t_user_info_game_level"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>-->

        <el-form-item label="提现状态" prop="withdrawalOpenOrClose">
          <el-select v-model="form.withdrawalOpenOrClose" placeholder="请选择">
            <el-option
              v-for="dict in dict.type.biz_open_or_close"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="账号状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option
              v-for="dict in dict.type.t_user_info_status"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>

<!--        <el-form-item label="保底等级" prop="minGameLevel">
          <el-select v-model="form.minGameLevel" placeholder="请选择">
            <el-option
              v-for="dict in dict.type.t_user_info_game_level"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="团队流水分润" prop="teamRewardRatio">
          <el-input v-model="form.teamRewardRatio"
                    oninput="if(!/^\d*\.?\d*$/.test(value)) { value = value.replace(/[^\d.]/g, ''); }"/>
        </el-form-item>

        <el-form-item label="登录密码" prop="loginPwd">
          <el-input v-model="form.loginPwd" placeholder="输入密码表示修改,不修改则不填写" type="password" show-password autocomplete="new-password"/>
        </el-form-item>-->

        <el-form-item  :rules="[{ required: true, message: '请输入google验证码' }]" label="google验证码" prop="autoCode">
          <el-input v-model="form.autoCode" placeholder="google验证码"></el-input>
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
import {  subUserList , listUserinfo, getUserinfo, delUserinfo, addUserinfo, updateUserinfo,childListUserinfo } from "@/api/xms/userinfo";
import { getSystemAuth } from "@/api/system/config";
import {sendCode} from "@/api/system/user";
export default {
  name: "Userinfo",
  dicts: ['t_user_info_game_level', 'biz_open_or_close','t_node_plan_node_level',
    't_user_info_is_valid', 't_user_info_status','t_w3_community_subsidy_level'],
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
      // 用户信息表格数据
      userinfoList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否删除时间范围
      daterangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: null,
        autoMining: null,
        userCode: null,
        autoWithdrawal: null,
        finaGameLevel: null,
        autoTransfer: null,
        account: null,
        mnemonic: null,
        partnerStatus: null,
        withdrawalOpenOrClose: null,
        nodeIdentity: null,
        recAddress: null,
        gameLevel: null,
        minGameLevel: null,
        isValid: null,
        loginPwd: null,
        loginSalt: null,
        email: null,
        payPwd: null,
        paySalt: null,
        inviteUserCode: null,
        status: null,
        subNum: null,
        umbrellaNum: null,
        performance: null,
        umbrellaPerformance: null,
        activeFlag: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        status: [
          { required: true, message: "状态不能为空", trigger: "change" }
        ],
        minGameLevel: [
          { required: true, message: "保底等级不能为空", trigger: "change" }
        ],
        email: [
          { required: true, message: "邮箱不能为空", trigger: "change" }
        ],
        teamRewardRatio: [
          { required: true, message: "团队流水分润不能为空", trigger: "change" }
        ],
        withdrawalOpenOrClose: [
          { required: true, message: "提现状态不能为空", trigger: "change" }
        ]
      }
    };
  },

  created() {
    const userId = this.$route.params && this.$route.params.userId;
    this.topUserId = userId;
    this.getList();
    this.getSystemAuth();
  },
  methods: {
    toChildUser(row){
      this.$router.push("/xms/userinfo/childindex/list/" + row.userId);
    },
    toSubUser(row){
      this.$router.push("/xms/userinfo/subUserindex/list/" + row.userId);
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
    /** 查询用户信息列表 */
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      if (null != this.daterangeCreateTime && '' != this.daterangeCreateTime) {
        this.queryParams.params["beginCreateTime"] = this.daterangeCreateTime[0];
        this.queryParams.params["endCreateTime"] = this.daterangeCreateTime[1];
      }
      this.queryParams.params["topUserId"] = this.topUserId;
      subUserList(this.queryParams).then(response => {
        this.userinfoList = response.rows;
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
        userId: null,
        userCode: null,
        account: null,
        recAddress: null,
        gameLevel: null,
        isValid: null,
        email: null,
        loginPwd: null,
        mnemonic: null,
        googleKey: null,
        minGameLevel: null,
        withdrawalOpenOrClose: null,
        teamRewardRatio: null,
        loginSalt: null,
        inviteUserCode: null,
        status: null,
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
      this.daterangeCreateTime = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.userId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加用户信息";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const userId = row.userId || this.ids
      getUserinfo(userId).then(response => {
        this.form = response.data;
        this.form.loginPwd = null;
        this.form.payPwd = null;
        this.open = true;
        this.title = "修改用户信息";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.userId != null) {
            updateUserinfo(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addUserinfo(this.form).then(response => {
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
      const userIds = row.userId || this.ids;
      this.$modal.confirm('是否确认删除用户信息编号为"' + userIds + '"的数据项？').then(function() {
        return delUserinfo(userIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.queryParams.ids = this.ids;
      this.queryParams.params["topUserId"] = this.topUserId;
      this.download('xms/userinfo/subUserExport', {
        ...this.queryParams
      }, `userinfo_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
