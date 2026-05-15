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
      </el-form-item>

 -->

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
      </el-col> -->
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

      <el-table-column align="center" class-name="small-padding fixed-width" label="操作" width="150">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['xms:userinfo:edit']"
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
            v-if="scope.row.subNum>0"
            v-hasPermi="['xms:userinfo:childList']"
            icon="el-icon-search"
            size="mini"
            type="text"
            @click="toSubUser(scope.row)"
          >直推用户</el-button>
           <el-button
              v-if="scope.row.lastLoginIp"
              size="small"
              type="text"
              @click="showLoginIpHistory(scope.row)"
            >
              <i class="el-icon-view"></i> 查看登录记录
            </el-button>
<!--            <el-button
              type="text"
              size="small"
              @click="handleChangeGoogleCode(scope.row)"
              v-hasPermi="['xms:userinfo:edit:google']"
            >
              <i class="el-icon-refresh"></i> 更换谷歌
            </el-button>-->
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

      <el-table-column align="center" label="是否持有未出局的矿机" prop="isValid" width="80">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_info_is_valid" :value="scope.row.isValid"/>
        </template>
      </el-table-column>



      <el-table-column align="center" label="邀请用户信息" prop="inviteUserCode" width="180">
          <template slot-scope="scope">
            <div class="exchange-info" style="text-align: left;">
            用户编码: {{scope.row.inviteUserCode}}<br/>
            用户ID: {{scope.row.inviteUserId}}<br/>
            </div>
          </template>
      </el-table-column>

      <el-table-column align="center" label="等级信息" width="220">
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
            <div style="display: flex; align-items: center; flex-wrap: nowrap; gap: 4px; margin-top: 6px;">
              <span>后台保底:</span>
              <dict-tag :options="dict.type.t_user_info_game_level" :value="scope.row.adminGameLevel" />
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
      <el-table-column align="center" label="团队相关" width="150">
        <template slot-scope="scope">
          <div class="exchange-info" style="text-align: left;">
            直推用户数: {{ scope.row.subNum }}<br>
            团队用户数: {{ scope.row.umbrellaNum }}<br>
          </div>
        </template>
      </el-table-column>

      <el-table-column align="center" label="节点销售业绩" prop="account">
        <template slot-scope="scope">
          <div class="exchange-info" style="text-align: left;">
            直推: {{ scope.row.subUmbrellaNodePerformance }} USDT<br>
            团队: {{ scope.row.umbrellaNodePerformance }} USDT<br>
          </div>
        </template>
      </el-table-column>

      <el-table-column align="center" label="节点业绩" width="150">
        <template slot-scope="scope">
          <div class="exchange-info" style="text-align: left;">
            直推: {{ scope.row.subNodePerformance }}<br>
            团队: {{ scope.row.nodeTeamPerformance }}<br>
          </div>
        </template>
      </el-table-column>

      <el-table-column align="center" label="质押业绩" width="190">
        <template slot-scope="scope">
          <div class="exchange-info" style="text-align: left;">
            个人质押: {{ scope.row.performance || 0 }}<br>
            团队质押: {{ scope.row.umbrellaPerformance || 0 }}<br>
            小区质押: {{ scope.row.communityPerformance || 0 }}<br>
            指定收益率: {{ scope.row.stakeHostingStaticRate || 0 }}%
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
      <el-table-column align="center" label="备注" prop="remark" show-overflow-tooltip width="180"/>



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
<!--        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="form.userId" :disabled="true"/>
        </el-form-item>-->

<!--        <el-form-item label="用户编码" prop="userCode">
          <el-input v-model="form.userCode" :disabled="true"/>
        </el-form-item>-->
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="form.userId" :disabled="true"/>
        </el-form-item>

        <el-form-item label="钱包地址" prop="account">
          <el-input v-model="form.account" type="textarea"/>
        </el-form-item>

        <el-form-item label="真实等级">
          <el-select v-model="form.gameLevel" disabled placeholder="系统自动计算">
            <el-option
              v-for="dict in dict.type.t_user_info_game_level"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="节点赠送等级">
          <el-select v-model="form.minGameLevel" disabled placeholder="买节点后自动赠送">
            <el-option
              v-for="dict in dict.type.t_user_info_game_level"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="管理员保底等级" prop="adminGameLevel">
          <el-select v-model="form.adminGameLevel" placeholder="请选择管理员保底等级">
            <el-option
              v-for="dict in dict.type.t_user_info_game_level"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>

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

        <el-form-item label="托管指定收益率" prop="stakeHostingStaticRate">
          <el-input
            v-model="form.stakeHostingStaticRate"
            placeholder="0表示按G7规则，例如0.5表示0.5%"
            @input="onStakeHostingStaticRateInput"
          >
            <template slot="append">%</template>
          </el-input>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            :autosize="{ minRows: 3, maxRows: 5 }"
            maxlength="500"
            placeholder="请输入后台用户备注"
            show-word-limit
            type="textarea"
          />
        </el-form-item>

<!--        <el-form-item label="团队流水分润" prop="teamRewardRatio">
          <el-input v-model="form.teamRewardRatio"
                    oninput="if(!/^\d*\.?\d*$/.test(value)) { value = value.replace(/[^\d.]/g, ''); }"/>
        </el-form-item>-->

<!--        <el-form-item label="谷歌Key" prop="googleKey">
          <el-input v-model="form.googleKey" type="password" :disabled="true"/>
        </el-form-item>-->

<!--        <el-form-item label="谷歌Key" prop="googleKey" v-hasPermi="['xms:userinfo:edit:google']">
          <el-input v-model="form.googleKey" type="textarea" :disabled="true"/>
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

    <!-- IP记录查看弹窗 -->
    <el-dialog
      :close-on-click-modal="false"
      :visible.sync="loginIpDialogVisible"
      title="最近登录IP记录"
      width="600px"
    >
      <div v-if="currentUserLoginIps && currentUserLoginIps.length > 0">
        <div class="ip-list">
          <div
            v-for="(record, index) in currentUserLoginIps"
            :key="index"
            class="ip-item"
          >
            <div class="ip-index-circle">{{ index + 1 }}</div>
            <div class="ip-content">
              <div class="ip-header">
                <div class="ip-address" title="点击查询IP详情" @click="openIpQuery(record.ip)">
                  <i class="el-icon-location-outline"></i>
                  {{ record.ip }}
                </div>
                <div :class="getIpTypeClass(record.ip)" class="ip-type">
                  {{ getIpType(record.ip) }}
                </div>
              </div>
              <div class="ip-time">
                <i class="el-icon-time"></i>
                <span class="time-relative">{{ formatTime(record.loginTime) }}</span>
                <span v-if="record.loginTime !== '未知时间'" class="time-absolute">
                  ({{ record.loginTime }})
                </span>
              </div>
            </div>
          </div>
        </div>
        <div class="ip-summary">
          <div class="summary-content">
            <i class="el-icon-data-line"></i>
            <span>共找到 <strong>{{ currentUserLoginIps.length }}</strong> 条登录记录</span>
          </div>
        </div>
      </div>
      <div v-else class="no-data">
        <div class="no-data-icon">
          <i class="el-icon-warning-outline"></i>
        </div>
        <div class="no-data-text">
          <h4>暂无登录记录</h4>
          <p>该用户还没有登录记录</p>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="loginIpDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 更换谷歌验证码弹窗 -->
    <el-dialog
      :close-on-click-modal="false"
      :visible.sync="googleCodeDialogVisible"
      title="更换谷歌验证码"
      width="500px"
    >
      <div v-if="newGoogleCode">
        <div class="google-code-container">
          <div class="google-code-header">
            <i class="el-icon-key"></i>
            <span>新的谷歌验证码</span>
          </div>
          <div class="google-code-content">
            <div class="google-code-text">
              {{ newGoogleCode }}
            </div>
            <el-button
              class="copy-button"
              icon="el-icon-document-copy"
              size="small"
              type="primary"
              @click="copyGoogleCode"
            >
              复制
            </el-button>
          </div>
          <div class="google-code-tip">
            <i class="el-icon-warning-outline"></i>
            <span>请妥善保存新的谷歌验证码，旧的验证码将失效</span>
          </div>
        </div>
      </div>
      <div v-else-if="googleCodeLoading" class="loading-container">
        <i class="el-icon-loading"></i>
        <span>正在生成新的谷歌验证码...</span>
      </div>
      <div v-else class="error-container">
        <i class="el-icon-warning"></i>
        <span>生成谷歌验证码失败，请重试</span>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="googleCodeDialogVisible = false">关闭</el-button>

      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  offTeamWithdrawal,
  listUserinfo,
  getUserinfo,
  delUserinfo,
  addUserinfo,
  updateUserinfo,
  changeGoogleCode,
  closeTeamMining
} from "@/api/xms/userinfo";
import { getSystemAuth } from "@/api/system/config";
import {sendCode} from "@/api/system/user";

export default {
  name: "Userinfo",
  dicts: ['t_user_info_game_level','t_node_plan_node_level',
    'biz_open_or_close','t_user_info_is_valid', 't_user_info_status','t_w3_community_subsidy_level'],
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
        stakeHostingStaticRate: null,
        nodeIdentity: null,
        recAddress: null,
        gameLevel: null,
        minGameLevel: null,
        adminGameLevel: null,
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
      // IP记录弹窗相关
      loginIpDialogVisible: false,
      currentUserLoginIps: [],
      currentUser: null,
      // 谷歌验证码弹窗相关
      googleCodeDialogVisible: false,
      newGoogleCode: '',
      googleCodeLoading: false,
      currentGoogleUser: null,
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        status: [
          { required: true, message: "状态不能为空", trigger: "change" }
        ],
        adminGameLevel: [
          { required: true, message: '管理员保底等级不能为空', trigger: 'change' }
        ],
        email: [
          { required: true, message: "邮箱不能为空", trigger: "change" }
        ],
        teamRewardRatio: [
          { required: true, message: "团队流水分润不能为空", trigger: "change" }
        ],
        withdrawalOpenOrClose: [
          { required: true, message: "提现状态不能为空", trigger: "change" }
        ],
        stakeHostingStaticRate: [
          { required: true, message: "托管指定收益率不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
    this.getSystemAuth();
  },
  methods: {
    toSubUser(row){
      this.$router.push("/xms/userinfo/subUserindex/list/" + row.userId);
    },
    getEffectiveLevel(row) {
      return Math.max(
        Number(row.gameLevel || 0),
        Number(row.minGameLevel || 0),
        Number(row.adminGameLevel || 0)
      )
    },
    toChildUser(row){
      this.$router.push("/xms/userinfo/childindex/list/" + row.userId);
    },
    /** 查询用户信息列表 */
    getSystemAuth() {
      getSystemAuth().then(response => {
        this.systemAuth = response.msg;
      });
    },
    /** 查询用户信息列表 */
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      if (null != this.daterangeCreateTime && '' !== this.daterangeCreateTime) {
        this.queryParams.params["beginCreateTime"] = this.daterangeCreateTime[0];
        this.queryParams.params["endCreateTime"] = this.daterangeCreateTime[1];
      }
      listUserinfo(this.queryParams).then(response => {
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
        remark: null,
        googleKey: null,
        minGameLevel: null,
        adminGameLevel: null,
        withdrawalOpenOrClose: null,
        stakeHostingStaticRate: 0,
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
        this.form.stakeHostingStaticRate = this.form.stakeHostingStaticRate || 0;
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
          if (Number(this.form.stakeHostingStaticRate || 0) < 0) {
            this.$modal.msgError("托管指定收益率不能小于0");
            return;
          }
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
    /** 托管指定收益率只允许输入非负数字。 */
    onStakeHostingStaticRateInput() {
      this.form.stakeHostingStaticRate = String(this.form.stakeHostingStaticRate || '')
        .replace(/[^\d.]/g, '')
        .replace(/^\./g, '')
        .replace(/\.{2,}/g, '.');
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
      this.download('xms/userinfo/export', {
        ...this.queryParams
      }, `userinfo_${new Date().getTime()}.xlsx`)
    },
    openWithdrawalHandleUpdate(row) {
      this.$confirm('确认开启该用户的团队提现吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return offTeamWithdrawal(row.userId, 0)
      }).then(response => {
        if (response.code === 200) {
          this.$message.success('开启成功')
          this.getList() // 刷新列表
        }
      }).catch(() => {
        // 取消操作或发生错误时的处理
      })
    },
    closeWithdrawalHandleUpdate(row) {
      this.$confirm('确认关闭该用户的团队提现吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return offTeamWithdrawal(row.userId, 1)
      }).then(response => {
        if (response.code === 200) {
          this.$message.success('关闭成功')
          this.getList() // 刷新列表
        }
      }).catch(() => {
        // 取消操作或发生错误时的处理
      })
    },
    // 显示登录IP历史记录
    showLoginIpHistory(row) {
      this.currentUser = row;
      this.loginIpDialogVisible = true;

      // 解析IP记录字符串为数组
      if (row.lastLoginIp) {
        this.currentUserLoginIps = row.lastLoginIp.split(',').map(item => {
          const trimmedItem = item.trim();
          if (trimmedItem.includes('/')) {
            const [loginTime, ip] = trimmedItem.split('/');
            return {
              loginTime: loginTime.trim(),
              ip: ip.trim()
            };
          } else {
            // 兼容旧格式（只有IP的情况）
            return {
              loginTime: '未知时间',
              ip: trimmedItem
            };
          }
        }).filter(record => record.ip);
      } else {
        this.currentUserLoginIps = [];
      }
    },
    // 获取IP类型标识
    getIpType(ip) {
      if (!ip) return '';

      if (ip.startsWith('192.168') || ip.startsWith('10.') || ip.startsWith('172.')) {
        return '内网';
      } else if (ip.startsWith('127.')) {
        return '本地';
      } else {
        return '外网';
      }
    },
    // 获取IP类型样式类
    getIpTypeClass(ip) {
      if (!ip) return '';

      if (ip.startsWith('192.168') || ip.startsWith('10.') || ip.startsWith('172.')) {
        return 'ip-type-internal';
      } else if (ip.startsWith('127.')) {
        return 'ip-type-local';
      } else {
        return 'ip-type-external';
      }
    },
    // 格式化时间显示
    formatTime(timeStr) {
      if (!timeStr || timeStr === '未知时间') {
        return '未知时间';
      }
      // 如果时间格式是 YYYY-MM-DD HH:mm:ss，可以进行格式化
      try {
        const date = new Date(timeStr);
        if (isNaN(date.getTime())) {
          return timeStr; // 如果无法解析，返回原始字符串
        }
        // 格式化为更友好的显示
        const now = new Date();
        const diffMs = now - date;
        const diffMins = Math.floor(diffMs / (1000 * 60));
        const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
        const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

        if (diffMins < 1) {
          return '刚刚';
        } else if (diffMins < 60) {
          return `${diffMins}分钟前`;
        } else if (diffHours < 24) {
          return `${diffHours}小时前`;
        } else if (diffDays < 7) {
          return `${diffDays}天前`;
        } else {
          return timeStr; // 超过一周显示原始时间
        }
      } catch (error) {
        return timeStr;
      }
    },
    // 处理更换谷歌验证码
    handleChangeGoogleCode(row) {
      this.$confirm('确认要更换该用户的谷歌验证码吗？', '确认更换', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 用户确认后才执行更换操作
        this.executeChangeGoogleCode(row);
      }).catch(() => {
        // 用户取消，不执行任何操作
      });
    },
    // 执行更换谷歌验证码
    executeChangeGoogleCode(row) {
      this.currentGoogleUser = row;
      this.googleCodeDialogVisible = true;
      this.googleCodeLoading = true;
      this.newGoogleCode = '';

      // 调用更换谷歌验证码API
      changeGoogleCode(row.userId).then(response => {
        this.googleCodeLoading = false;
        // 直接使用返回的字符串作为谷歌验证码
        this.newGoogleCode = response;
        this.$message.success('谷歌验证码生成成功');
      }).catch(error => {
        this.googleCodeLoading = false;
        console.error('更换谷歌验证码失败:', error);
        this.$message.error('生成谷歌验证码失败');
      });
    },
    // 复制谷歌验证码
    copyGoogleCode() {
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(this.newGoogleCode).then(() => {
          this.$message.success('谷歌验证码已复制到剪贴板');
        }).catch(err => {
          console.error('复制失败:', err);
          this.fallbackCopyTextToClipboard(this.newGoogleCode);
        });
      } else {
        this.fallbackCopyTextToClipboard(this.newGoogleCode);
      }
    },
    // 兼容性复制方法
    fallbackCopyTextToClipboard(text) {
      const textArea = document.createElement("textarea");
      textArea.value = text;
      textArea.style.top = "0";
      textArea.style.left = "0";
      textArea.style.position = "fixed";
      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();

      try {
        const successful = document.execCommand('copy');
        if (successful) {
          this.$message.success('谷歌验证码已复制到剪贴板');
        } else {
          this.$message.error('复制失败，请手动复制');
        }
      } catch (err) {
        console.error('复制失败:', err);
        this.$message.error('复制失败，请手动复制');
      }

      document.body.removeChild(textArea);
    },
    // 打开IP查询页面
    openIpQuery(ip) {
      if (!ip) return;

      // 构建查询URL
      const queryUrl = `https://zh-hans.ipshu.com/ipv4/${ip}`;

      // 在新标签页中打开
      window.open(queryUrl, '_blank');
    }
  }
};
</script>

<style scoped>
.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
}

.el-dropdown-menu__item i {
  margin-right: 5px;
}

.el-table .el-dropdown .el-button {
  padding: 3px 8px;
  font-size: 12px;
}

/* IP记录弹窗样式 */
.ip-list {
  max-height: 400px;
  overflow-y: auto;
  padding: 0;
}

.ip-item {
  display: flex;
  align-items: flex-start;
  padding: 16px 0;
  border-bottom: 1px solid #f0f2f5;
  transition: all 0.3s ease;
  position: relative;
}

.ip-item:hover {
  background: linear-gradient(90deg, #f8f9ff 0%, #ffffff 100%);
  transform: translateX(2px);
}

.ip-item:last-child {
  border-bottom: none;
}

.ip-index-circle {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  margin-right: 16px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.ip-content {
  flex: 1;
  min-width: 0;
}

.ip-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.ip-address {
  font-weight: 600;
  color: #303133;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 16px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 4px;
  padding: 4px 8px;
  margin: -4px -8px;
}

.ip-address:hover {
  background-color: #e6f7ff;
  color: #1890ff;
  transform: translateX(2px);
}

.ip-address i {
  margin-right: 8px;
  color: #409EFF;
  font-size: 18px;
}

.ip-time {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  margin-top: 4px;
  flex-wrap: wrap;
  gap: 4px;
}

.ip-time i {
  margin-right: 6px;
  color: #C0C4CC;
}

.time-relative {
  font-weight: 500;
  color: #606266;
}

.time-absolute {
  font-size: 12px;
  color: #C0C4CC;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.ip-type {
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 500;
  border-radius: 16px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  flex-shrink: 0;
}

.ip-type-internal {
  background: linear-gradient(135deg, #E6F7FF 0%, #BAE7FF 100%);
  color: #1890FF;
  border: 1px solid #91D5FF;
}

.ip-type-external {
  background: linear-gradient(135deg, #FFF2E6 0%, #FFD8BF 100%);
  color: #FA8C16;
  border: 1px solid #FFBB96;
}

.ip-type-local {
  background: linear-gradient(135deg, #F6FFED 0%, #D9F7BE 100%);
  color: #52C41A;
  border: 1px solid #B7EB8F;
}

.ip-summary {
  margin-top: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 8px;
  border: 1px solid #dee2e6;
}

.summary-content {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #495057;
  font-size: 14px;
}

.summary-content i {
  margin-right: 8px;
  color: #409EFF;
  font-size: 16px;
}

.summary-content strong {
  color: #409EFF;
  font-weight: 600;
  margin: 0 4px;
}

.no-data {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.no-data-icon {
  margin-bottom: 16px;
}

.no-data-icon i {
  font-size: 64px;
  color: #C0C4CC;
  opacity: 0.6;
}

.no-data-text h4 {
  margin: 0 0 8px 0;
  color: #606266;
  font-size: 16px;
  font-weight: 500;
}

.no-data-text p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

/* 表格内按钮样式 */
.el-button--text {
  color: #409EFF;
  padding: 0;
}

.el-button--text:hover {
  color: #66b1ff;
}

.el-button--text i {
  margin-right: 4px;
}

/* 谷歌验证码弹窗样式 */
.google-code-container {
  padding: 20px 0;
}

.google-code-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.google-code-header i {
  margin-right: 8px;
  font-size: 20px;
  color: #409EFF;
}

.google-code-content {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.google-code-text {
  flex: 1;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  padding: 8px 12px;
  background: white;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  word-break: break-all;
}

.copy-button {
  flex-shrink: 0;
}

.google-code-tip {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fff7e6;
  border: 1px solid #ffd591;
  border-radius: 6px;
  color: #d46b08;
  font-size: 14px;
}

.google-code-tip i {
  margin-right: 8px;
  font-size: 16px;
}

.loading-container {
  text-align: center;
  padding: 40px 0;
  color: #409EFF;
}

.loading-container i {
  font-size: 24px;
  margin-bottom: 12px;
  display: block;
  animation: rotating 2s linear infinite;
}

.loading-container span {
  font-size: 14px;
}

.error-container {
  text-align: center;
  padding: 40px 0;
  color: #F56C6C;
}

.error-container i {
  font-size: 24px;
  margin-bottom: 12px;
  display: block;
}

.error-container span {
  font-size: 14px;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
