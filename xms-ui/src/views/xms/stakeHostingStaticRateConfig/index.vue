<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="120px">
      <el-form-item label="Gsmooth下限" prop="minG">
        <el-input v-model="queryParams.minG" placeholder="请输入下限" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="Gsmooth上限" prop="maxG">
        <el-input v-model="queryParams.maxG" placeholder="请输入上限" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option
            v-for="dict in dict.type.t_stake_hosting_static_rate_config_status"
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
        <el-button v-hasPermi="['xms:stakeHostingStaticRateConfig:add']" type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['xms:stakeHostingStaticRateConfig:edit']" type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['xms:stakeHostingStaticRateConfig:remove']" type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['xms:stakeHostingStaticRateConfig:export']" type="warning" plain icon="el-icon-download" size="mini" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

      <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="Gsmooth下限" align="center" prop="minG">
        <template slot-scope="scope">
          <span>{{ formatPercent(scope.row.minG) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Gsmooth上限" align="center" prop="maxG">
        <template slot-scope="scope">
          <span>{{ scope.row.maxG == null ? '无上限' : formatPercent(scope.row.maxG) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="日化收益率" align="center" prop="staticRate">
        <template slot-scope="scope">
          <span>{{ formatPercent(scope.row.staticRate) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sort" width="80" />
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_stake_hosting_static_rate_config_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button v-hasPermi="['xms:stakeHostingStaticRateConfig:edit']" size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button v-hasPermi="['xms:stakeHostingStaticRateConfig:remove']" size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="520px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="130px">
        <el-form-item label="Gsmooth下限" prop="minG">
          <el-input v-model="form.minG" placeholder="例如-20" @input="onRateInput('minG', true)" />
        </el-form-item>
        <el-form-item label="Gsmooth上限" prop="maxG">
          <el-input v-model="form.maxG" placeholder="最高档可留空，表示无上限" @input="onRateInput('maxG', true)" />
        </el-form-item>
        <el-form-item label="日化收益率" prop="staticRate">
          <el-input v-model="form.staticRate" placeholder="例如0.5表示0.5%" @input="onRateInput('staticRate', false)" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in dict.type.t_stake_hosting_static_rate_config_status" :key="dict.value" :label="parseInt(dict.value)">{{ dict.label }}</el-radio>
          </el-radio-group>
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
import {
  listStakeHostingStaticRateConfig,
  getStakeHostingStaticRateConfig,
  addStakeHostingStaticRateConfig,
  updateStakeHostingStaticRateConfig,
  delStakeHostingStaticRateConfig
} from '@/api/xms/stakeHostingStaticRateConfig'

export default {
  name: 'StakeHostingStaticRateConfig',
  dicts: ['t_stake_hosting_static_rate_config_status'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      configList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        minG: null,
        maxG: null,
        status: null
      },
      form: {},
      rules: {
        minG: [{ required: true, message: 'Gsmooth下限不能为空', trigger: 'blur' }],
        staticRate: [{ required: true, message: '日化收益率不能为空', trigger: 'blur' }],
        status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listStakeHostingStaticRateConfig(this.queryParams).then(response => {
        this.configList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = { id: null, minG: null, maxG: null, staticRate: null, sort: 0, status: 1, remark: null }
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
      this.title = '新增G7静态收益率配置'
    },
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getStakeHostingStaticRateConfig(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改G7静态收益率配置'
      })
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (!valid) {
          return
        }
        const minG = Number(this.form.minG)
        const maxG = this.form.maxG === null || this.form.maxG === '' ? null : Number(this.form.maxG)
        const staticRate = Number(this.form.staticRate)
        if (Number.isNaN(minG)) {
          this.$modal.msgError('Gsmooth下限格式不正确')
          return
        }
        if (maxG !== null && Number.isNaN(maxG)) {
          this.$modal.msgError('Gsmooth上限格式不正确')
          return
        }
        if (Number.isNaN(staticRate)) {
          this.$modal.msgError('日化收益率格式不正确')
          return
        }
        if (maxG !== null && maxG <= minG) {
          this.$modal.msgError('Gsmooth上限必须大于下限')
          return
        }
        if (staticRate < 0) {
          this.$modal.msgError('日化收益率不能小于0')
          return
        }
        this.form.maxG = maxG
        if (this.form.id != null) {
          updateStakeHostingStaticRateConfig(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
        } else {
          addStakeHostingStaticRateConfig(this.form).then(() => {
            this.$modal.msgSuccess('新增成功')
            this.open = false
            this.getList()
          })
        }
      })
    },
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除G7静态收益率配置编号为"' + ids + '"的数据项？').then(() => {
        return delStakeHostingStaticRateConfig(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('xms/stakeHostingStaticRateConfig/export', {
        ...this.queryParams
      }, `stakeHostingStaticRateConfig_${new Date().getTime()}.xlsx`)
    },
    onRateInput(prop, allowNegative) {
      let value = String(this.form[prop] || '')
      value = value.replace(/[^\d.-]/g, '').replace(/(?!^)-/g, '').replace(/\.{2,}/g, '.')
      if (!allowNegative) {
        value = value.replace(/-/g, '')
      }
      this.form[prop] = value
    },
    formatPercent(value) {
      if (value === undefined || value === null || value === '') {
        return '-'
      }
      return value + ' %'
    }
  }
}
</script>
