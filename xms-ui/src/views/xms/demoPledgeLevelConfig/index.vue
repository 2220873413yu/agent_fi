<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="90px">
      <el-form-item label="等级" prop="level">
        <el-select v-model="queryParams.level" placeholder="请选择等级" clearable>
          <el-option
            v-for="dict in dict.type.t_user_info_game_level"
            :key="dict.value"
            :label="dict.label"
            :value="parseInt(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:demoPledgeLevelConfig:add']"
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:demoPledgeLevelConfig:edit']"
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:demoPledgeLevelConfig:remove']"
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:demoPledgeLevelConfig:export']"
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="demoPledgeLevelConfigList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="等级" align="center" prop="level">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_info_game_level" :value="scope.row.level" />
        </template>
      </el-table-column>
      <el-table-column label="个人业绩" align="center" prop="performance" />
      <el-table-column label="团队业绩" align="center" prop="teamPerformance" />
      <el-table-column label="小区业绩" align="center" prop="communityPerformance" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['xms:demoPledgeLevelConfig:edit']"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            v-hasPermi="['xms:demoPledgeLevelConfig:remove']"
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="等级" prop="level">
          <el-select v-model="form.level" placeholder="请选择等级" style="width: 100%">
            <el-option
              v-for="dict in dict.type.t_user_info_game_level"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="个人业绩" prop="performance">
          <el-input v-model="form.performance" placeholder="请输入个人业绩" @input="onAmountInput('performance')" />
        </el-form-item>
        <el-form-item label="团队业绩" prop="teamPerformance">
          <el-input v-model="form.teamPerformance" placeholder="请输入团队业绩" @input="onAmountInput('teamPerformance')" />
        </el-form-item>
        <el-form-item label="小区业绩" prop="communityPerformance">
          <el-input v-model="form.communityPerformance" placeholder="请输入小区业绩" @input="onAmountInput('communityPerformance')" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
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
import { listDemoPledgeLevelConfig, getDemoPledgeLevelConfig, addDemoPledgeLevelConfig, updateDemoPledgeLevelConfig, delDemoPledgeLevelConfig } from '@/api/xms/demoPledgeLevelConfig'

export default {
  name: 'DemoPledgeLevelConfig',
  dicts: ['t_user_info_game_level'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      demoPledgeLevelConfigList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        level: null
      },
      form: {},
      rules: {
        level: [{ required: true, message: '等级不能为空', trigger: 'change' }],
        performance: [{ required: true, message: '个人业绩不能为空', trigger: 'blur' }],
        teamPerformance: [{ required: true, message: '团队业绩不能为空', trigger: 'blur' }],
        communityPerformance: [{ required: true, message: '小区业绩不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listDemoPledgeLevelConfig(this.queryParams).then(response => {
        this.demoPledgeLevelConfigList = response.rows
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
        performance: 0,
        teamPerformance: 0,
        communityPerformance: 0,
        remark: null
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
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增示例质押等级配置'
    },
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getDemoPledgeLevelConfig(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改示例质押等级配置'
      })
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateDemoPledgeLevelConfig(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addDemoPledgeLevelConfig(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除示例质押等级配置编号为"' + ids + '"的数据项？').then(() => {
        return delDemoPledgeLevelConfig(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('xms/demoPledgeLevelConfig/export', {
        ...this.queryParams
      }, `demoPledgeLevelConfig_${new Date().getTime()}.xlsx`)
    },
    onAmountInput(prop) {
      this.form[prop] = String(this.form[prop] || '').replace(/[^\d.]/g, '').replace(/^\./g, '').replace(/\.{2,}/g, '.')
    }
  }
}
</script>
