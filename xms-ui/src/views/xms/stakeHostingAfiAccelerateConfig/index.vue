<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="90px">
      <el-form-item label="质押比例" prop="pledgeRatio">
        <el-input v-model="queryParams.pledgeRatio" placeholder="请输入质押比例" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option
            v-for="dict in dict.type.t_stake_hosting_afi_config_status"
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
        <el-button v-hasPermi="['xms:stakeHostingAfiAccelerateConfig:add']" type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['xms:stakeHostingAfiAccelerateConfig:edit']" type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['xms:stakeHostingAfiAccelerateConfig:remove']" type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['xms:stakeHostingAfiAccelerateConfig:export']" type="warning" plain icon="el-icon-download" size="mini" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="质押比例(%)" align="center" prop="pledgeRatio" />
      <el-table-column label="加速倍率" align="center" prop="accelerateRate" />
      <el-table-column label="排序" align="center" prop="sort" />
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_stake_hosting_afi_config_status" :value="scope.row.status" />
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
          <el-button v-hasPermi="['xms:stakeHostingAfiAccelerateConfig:edit']" size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button v-hasPermi="['xms:stakeHostingAfiAccelerateConfig:remove']" size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="质押比例" prop="pledgeRatio">
          <el-input v-model="form.pledgeRatio" placeholder="例如1表示1%" @input="onAmountInput('pledgeRatio')" />
        </el-form-item>
        <el-form-item label="加速倍率" prop="accelerateRate">
          <el-input v-model="form.accelerateRate" placeholder="例如1.10" @input="onAmountInput('accelerateRate')" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in dict.type.t_stake_hosting_afi_config_status" :key="dict.value" :label="parseInt(dict.value)">{{ dict.label }}</el-radio>
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
import { listStakeHostingAfiAccelerateConfig, getStakeHostingAfiAccelerateConfig, addStakeHostingAfiAccelerateConfig, updateStakeHostingAfiAccelerateConfig, delStakeHostingAfiAccelerateConfig } from '@/api/xms/stakeHostingAfiAccelerateConfig'

export default {
  name: 'StakeHostingAfiAccelerateConfig',
  dicts: ['t_stake_hosting_afi_config_status'],
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
        pledgeRatio: null,
        status: null
      },
      form: {},
      rules: {
        pledgeRatio: [{ required: true, message: '质押比例不能为空', trigger: 'blur' }],
        accelerateRate: [{ required: true, message: '加速倍率不能为空', trigger: 'blur' }],
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
      listStakeHostingAfiAccelerateConfig(this.queryParams).then(response => {
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
      this.form = { id: null, pledgeRatio: null, accelerateRate: null, sort: 0, status: 1, remark: null }
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
      this.title = '新增AFI质押加速配置'
    },
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getStakeHostingAfiAccelerateConfig(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改AFI质押加速配置'
      })
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateStakeHostingAfiAccelerateConfig(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addStakeHostingAfiAccelerateConfig(this.form).then(() => {
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
      this.$modal.confirm('是否确认删除AFI质押加速配置编号为"' + ids + '"的数据项？').then(() => {
        return delStakeHostingAfiAccelerateConfig(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('xms/stakeHostingAfiAccelerateConfig/export', {
        ...this.queryParams
      }, `stakeHostingAfiAccelerateConfig_${new Date().getTime()}.xlsx`)
    },
    onAmountInput(prop) {
      this.form[prop] = String(this.form[prop] || '').replace(/[^\d.]/g, '').replace(/^\./g, '').replace(/\.{2,}/g, '.')
    }
  }
}
</script>
