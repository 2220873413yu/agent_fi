<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="等级" prop="level">
        <el-select v-model="queryParams.level" clearable placeholder="请选择等级">
          <el-option
            v-for="dict in dict.type.t_user_info_game_level"
            :key="dict.value"
            :label="dict.label"
            :value="parseInt(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="userLevelConfigList">
      <el-table-column align="center" label="等级" prop="level">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_info_game_level" :value="scope.row.level" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="个人托管业绩" prop="performance" />
      <el-table-column align="center" label="小区托管业绩" prop="communityPerformance" />
      <el-table-column align="center" label="团队奖励比例(%)" prop="teamRewardRatio" />
      <el-table-column align="center" label="全球手续费分红(%)" prop="globalFeeDividendRatio" />
      <el-table-column align="center" label="创建时间" prop="createTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="修改时间" prop="updateTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.level > 0"
            v-hasPermi="['xms:userLevelConfig:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :limit.sync="queryParams.pageSize"
      :page.sync="queryParams.pageNum"
      :total="total"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" append-to-body width="520px">
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="等级" prop="level">
          <el-select v-model="form.level" disabled placeholder="请选择等级" style="width: 100%">
            <el-option
              v-for="dict in dict.type.t_user_info_game_level"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="个人托管业绩" prop="performance">
          <el-input-number
            v-model="form.performance"
            :controls="false"
            :min="0.01"
            :precision="2"
            placeholder="请输入个人托管业绩"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="小区托管业绩" prop="communityPerformance">
          <el-input-number
            v-model="form.communityPerformance"
            :controls="false"
            :min="0.01"
            :precision="2"
            placeholder="请输入小区托管业绩"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="团队奖励比例" prop="teamRewardRatio">
          <el-input-number
            v-model="form.teamRewardRatio"
            :controls="false"
            :min="0"
            :precision="2"
            placeholder="请输入团队奖励比例"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="全球分红比例" prop="globalFeeDividendRatio">
          <el-input-number
            v-model="form.globalFeeDividendRatio"
            :controls="false"
            :min="0"
            :precision="2"
            placeholder="请输入全球手续费分红比例"
            style="width: 100%"
          />
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
import { listUserLevelConfig, getUserLevelConfig, updateUserLevelConfig } from '@/api/xms/userLevelConfig'

export default {
  name: 'UserLevelConfig',
  dicts: ['t_user_info_game_level'],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      userLevelConfigList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        level: null
      },
      form: {},
      rules: {
        level: [
          { required: true, message: '等级不能为空', trigger: 'change' }
        ],
        performance: [
          { required: true, message: '个人托管业绩不能为空', trigger: 'blur' }
        ],
        communityPerformance: [
          { required: true, message: '小区托管业绩不能为空', trigger: 'blur' }
        ],
        teamRewardRatio: [
          { required: true, message: '团队奖励比例不能为空', trigger: 'blur' }
        ],
        globalFeeDividendRatio: [
          { required: true, message: '全球手续费分红比例不能为空', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listUserLevelConfig(this.queryParams).then(response => {
        this.userLevelConfigList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        id: null,
        level: null,
        performance: null,
        communityPerformance: null,
        teamRewardRatio: null,
        globalFeeDividendRatio: null
      }
      this.resetForm('form')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleUpdate(row) {
      this.reset()
      getUserLevelConfig(row.id).then(response => {
        this.form = {
          id: response.data.id,
          level: response.data.level,
          performance: response.data.performance,
          communityPerformance: response.data.communityPerformance,
          teamRewardRatio: response.data.teamRewardRatio,
          globalFeeDividendRatio: response.data.globalFeeDividendRatio
        }
        this.open = true
        this.title = '修改用户等级考核配置'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        updateUserLevelConfig(this.form).then(() => {
          this.$modal.msgSuccess('修改成功')
          this.open = false
          this.getList()
        })
      })
    }
  }
}
</script>
