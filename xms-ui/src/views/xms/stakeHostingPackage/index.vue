<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="90px">
      <el-form-item label="套餐名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入套餐名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="托管天数" prop="days">
        <el-select v-model="queryParams.days" placeholder="请选择托管天数" clearable>
          <el-option
            v-for="dict in dict.type.t_stake_hosting_package_days"
            :key="dict.value"
            :label="dict.label"
            :value="parseInt(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option
            v-for="dict in dict.type.t_stake_hosting_package_status"
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
          v-hasPermi="['xms:stakeHostingPackage:add']"
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:stakeHostingPackage:edit']"
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
          v-hasPermi="['xms:stakeHostingPackage:export']"
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="stakeHostingPackageList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="套餐名称" align="center" prop="name" />
      <el-table-column label="托管天数" align="center" prop="days">
        <template slot-scope="scope">{{ scope.row.days }}天</template>
      </el-table-column>
      <el-table-column label="最低起购USDT" align="center" prop="minAmount" />
      <el-table-column label="服务费比例(%)" align="center" prop="serviceFeeRatio" />
      <el-table-column label="业绩积分系数" align="center" prop="performanceCoefficient" />
      <el-table-column label="排序" align="center" prop="sort" />
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_stake_hosting_package_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['xms:stakeHostingPackage:edit']"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
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
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="套餐名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入套餐名称" />
        </el-form-item>
        <el-form-item label="托管天数" prop="days">
          <el-select v-model="form.days" placeholder="请选择托管天数" :disabled="form.id != null">
            <el-option
              v-for="dict in dict.type.t_stake_hosting_package_days"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="最低起购USDT" prop="minAmount">
          <el-input v-model="form.minAmount" placeholder="请输入最低起购USDT" @input="onAmountInput('minAmount')" />
        </el-form-item>
        <el-form-item label="服务费比例" prop="serviceFeeRatio">
          <el-input v-model="form.serviceFeeRatio" placeholder="请输入服务费比例，例如10表示10%" @input="onAmountInput('serviceFeeRatio')" />
        </el-form-item>
        <el-form-item label="业绩积分系数" prop="performanceCoefficient">
          <el-input v-model="form.performanceCoefficient" placeholder="例如30天填1，90天填3" @input="onAmountInput('performanceCoefficient')" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.t_stake_hosting_package_status"
              :key="dict.value"
              :label="parseInt(dict.value)"
            >{{ dict.label }}</el-radio>
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
import { listStakeHostingPackage, getStakeHostingPackage, addStakeHostingPackage, updateStakeHostingPackage } from '@/api/xms/stakeHostingPackage'

export default {
  name: 'StakeHostingPackage',
  dicts: ['t_stake_hosting_package_days', 't_stake_hosting_package_status'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      stakeHostingPackageList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        days: null,
        status: null
      },
      form: {},
      rules: {
        name: [{ required: true, message: '套餐名称不能为空', trigger: 'blur' }],
        days: [{ required: true, message: '托管天数不能为空', trigger: 'change' }],
        minAmount: [{ required: true, message: '起购金额不能为空', trigger: 'blur' }],
        serviceFeeRatio: [{ required: true, message: '服务费比例不能为空', trigger: 'blur' }],
        performanceCoefficient: [{ required: true, message: '业绩积分系数不能为空', trigger: 'blur' }],
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
      listStakeHostingPackage(this.queryParams).then(response => {
        this.stakeHostingPackageList = response.rows
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
        name: null,
        days: null,
        minAmount: null,
        serviceFeeRatio: 0,
        performanceCoefficient: 1,
        sort: 0,
        status: 1,
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
      this.title = '新增托管套餐'
    },
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getStakeHostingPackage(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改托管套餐'
      })
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateStakeHostingPackage(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addStakeHostingPackage(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleExport() {
      this.download('xms/stakeHostingPackage/export', {
        ...this.queryParams
      }, `stakeHostingPackage_${new Date().getTime()}.xlsx`)
    },
    onAmountInput(prop) {
      this.form[prop] = String(this.form[prop] || '').replace(/[^\d.]/g, '').replace(/^\./g, '').replace(/\.{2,}/g, '.')
    }
  }
}
</script>
