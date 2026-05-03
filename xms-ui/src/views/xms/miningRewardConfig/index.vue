<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
<!--      <el-form-item label="奖励角色" prop="rewardLevel">
        <el-select v-model="queryParams.rewardLevel" placeholder="请选择奖励角色" clearable>
          <el-option
            v-for="dict in dict.type.t_mining_reward_config_reward_level"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="奖励比例" prop="rewardRatio">
        <el-input
          v-model="queryParams.rewardRatio"
          placeholder="请输入奖励比例"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->
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
          v-hasPermi="['xms:miningRewardConfig:add']"
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
          v-hasPermi="['xms:miningRewardConfig:edit']"
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
          v-hasPermi="['xms:miningRewardConfig:remove']"
        >删除</el-button>
      </el-col>-->
<!--      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:miningRewardConfig:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出</el-button>
      </el-col>-->
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="miningRewardConfigList" @selection-change="handleSelectionChange">
<!--      <el-table-column type="selection" width="55" align="center" />-->
      <el-table-column v-if="false" align="center" label="主键ID" prop="id"/>
      <el-table-column align="center" label="奖励角色" prop="rewardLevel">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_mining_reward_config_reward_level" :value="scope.row.rewardLevel"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="奖励比例" prop="rewardRatio" >
        <template slot-scope="scope">
          {{ scope.row.rewardRatio }} %
        </template>
      </el-table-column>
      <el-table-column align="center" label="创建时间" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="更新时间" prop="updateTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="创建者" prop="createBy" />
      <el-table-column align="center" label="更新者" prop="updateBy" />
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['xms:miningRewardConfig:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
<!--          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['xms:miningRewardConfig:remove']"
          >删除</el-button>-->
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

    <!-- 添加或修改矿机奖励分配配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="奖励角色" prop="rewardLevel">
          <el-select v-model="form.rewardLevel" placeholder="请选择奖励角色">
            <el-option
              v-for="dict in dict.type.t_mining_reward_config_reward_level"
              :key="dict.value"
              :disabled="true"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="奖励比例" prop="rewardRatio">
          <el-input v-model="form.rewardRatio"
                    oninput="value = value.replace(/^(\d+)(\.\d{0,2})?.*$/, '$1$2')"
                    placeholder="请输入奖励比例" />
          <div class="form-tip">以百分比为单位，例如：1 表示 1%</div>
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
import { listMiningRewardConfig, getMiningRewardConfig, delMiningRewardConfig, addMiningRewardConfig, updateMiningRewardConfig } from "@/api/xms/miningRewardConfig";

export default {
  name: "MiningRewardConfig",
  dicts: ['t_mining_reward_config_reward_level'],
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
      // 矿机奖励分配配置表格数据
      miningRewardConfigList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        rewardLevel: null,
        rewardRatio: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        rewardLevel: [
          { required: true, message: "奖励角色 1:直推,2:间推,3:市代理,4:省代理,5:全国代理不能为空", trigger: "change" }
        ],
        rewardRatio: [
          { required: true, message: "奖励比例 例如:1代表1%不能为空", trigger: "blur" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询矿机奖励分配配置列表 */
    getList() {
      this.loading = true;
      listMiningRewardConfig(this.queryParams).then(response => {
        this.miningRewardConfigList = response.rows;
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
        rewardLevel: null,
        rewardRatio: null,
        createTime: null,
        updateTime: null,
        createBy: null,
        updateBy: null
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
      this.title = "添加矿机奖励分配配置";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getMiningRewardConfig(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改矿机奖励分配配置";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateMiningRewardConfig(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addMiningRewardConfig(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除矿机奖励分配配置编号为"' + ids + '"的数据项？').then(function() {
        return delMiningRewardConfig(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/miningRewardConfig/export', {
        ...this.queryParams
      }, `miningRewardConfig_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
