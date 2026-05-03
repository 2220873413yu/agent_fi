<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="等级" prop="level">
        <el-select v-model="queryParams.level" clearable placeholder="请选择等级">
          <el-option
            v-for="dict in dict.type.t_user_info_game_level"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
<!--      <el-form-item label="团队业绩(购买矿机数量)" prop="teamPerformance">
        <el-input
          v-model="queryParams.teamPerformance"
          placeholder="请输入团队业绩(购买矿机数量)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="小区业绩(购买矿机数量)" prop="communityPerformance">
        <el-input
          v-model="queryParams.communityPerformance"
          placeholder="请输入小区业绩(购买矿机数量)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="需要满足的线数量(比如2条线)" prop="requiredLegNum">
        <el-input
          v-model="queryParams.requiredLegNum"
          placeholder="请输入需要满足的线数量(比如2条线)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="线内代理最小等级(level)" prop="legLevelMin">
        <el-input
          v-model="queryParams.legLevelMin"
          placeholder="请输入线内代理最小等级(level)"
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
          v-hasPermi="['xms:userLevelConfig:add']"
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
          v-hasPermi="['xms:userLevelConfig:edit']"
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
          v-hasPermi="['xms:userLevelConfig:remove']"
        >删除</el-button>
      </el-col>-->
<!--      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:userLevelConfig:export']"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          type="warning"
        >导出</el-button>
      </el-col>-->
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="userLevelConfigList" @selection-change="handleSelectionChange">
<!--      <el-table-column type="selection" width="55" align="center" />-->
      <el-table-column v-if="false" align="center" label="主键id" prop="id"/>
      <el-table-column align="center" label="等级编码" prop="level">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_info_game_level" :value="scope.row.level"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="大区业绩(质押业绩)" prop="teamPerformance" />
      <el-table-column align="center" label="个人业绩" prop="performance" />
      <el-table-column align="center" label="小区业绩(质押业绩)" prop="communityPerformance" />
      <el-table-column align="center" label="需要满足的线数量" prop="requiredLegNum" />
      <el-table-column align="center" label="线内代理最小等级" prop="legLevelMin">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_info_game_level" :value="scope.row.legLevelMin"/>
        </template>
      </el-table-column>

      <el-table-column align="center" label="每条线里需要几个该等级" prop="legLevelCount" />
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

      <!--      <el-table-column label="备注" align="center" prop="remark" />-->
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            v-if="scope.row.level>0"
            v-hasPermi="['xms:userLevelConfig:edit']"
            @click="handleUpdate(scope.row)"
            icon="el-icon-edit"
          >修改</el-button>
<!--          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['xms:userLevelConfig:remove']"
          >删除</el-button>-->
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :limit.sync="queryParams.pageSize"
      :page.sync="queryParams.pageNum"
      @pagination="getList"
    />

    <!-- 添加或修改用户等级考核配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="等级" prop="level">
          <el-select v-model="form.level" placeholder="请选择等级">
            <el-option
              v-for="dict in dict.type.t_user_info_game_level"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
              disabled
            ></el-option>
          </el-select>

        </el-form-item>
        <el-form-item v-if="form.level ==2 " label="大区业绩" prop="teamPerformance">
          <el-input v-model="form.teamPerformance"
                    oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
                    placeholder="请输入大区业绩(质押业绩)" />
        </el-form-item>

        <el-form-item v-if="form.level ==2 " label="小区业绩" prop="communityPerformance">
          <el-input v-model="form.communityPerformance"
                    oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
                    placeholder="请输入小区业绩(质押业绩)" />
        </el-form-item>

        <el-form-item v-if="form.level == 1" label="个人业绩" prop="performance">
          <el-input v-model="form.performance"
                    oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
                    placeholder="请输入个人业绩" />
        </el-form-item>

        <el-form-item v-if="form.level > 1" label="需要满足的线数量(比如2条线)" prop="requiredLegNum">
          <el-input v-model="form.requiredLegNum"
                    oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
                    placeholder="请输入需要满足的线数量(比如2条线)" />
        </el-form-item>
<!--        <el-form-item label="线内代理最小等级(level)" prop="legLevelMin" v-if="form.level != 1">
          <el-input v-model="form.legLevelMin"
                    placeholder="请输入线内代理最小等级(level)" />
        </el-form-item>-->

        <el-form-item v-if="form.level > 1" label="线内代理最小等级" prop="legLevelMin">
          <el-select v-model="form.legLevelMin" placeholder="请选择等级">
            <el-option
              v-for="dict in filteredLevelDicts"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item v-if="form.level > 1" label="每条线里需要几个该等级及以上代理" prop="legLevelCount">
          <el-input v-model="form.legLevelCount"
                    oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
                    placeholder="每条线里需要几个该等级及以上代理" />
        </el-form-item>
<!--        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="删除标记,默认0,1已删除" prop="deleted">
          <el-input v-model="form.deleted" placeholder="请输入删除标记,默认0,1已删除" />
        </el-form-item>-->
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listUserLevelConfig, getUserLevelConfig, delUserLevelConfig, addUserLevelConfig, updateUserLevelConfig } from "@/api/xms/userLevelConfig";

export default {
  name: "UserLevelConfig",
  dicts: ['t_user_info_game_level'],
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
      // 用户等级考核配置表格数据
      userLevelConfigList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        level: null,
        teamPerformance: null,
        performance: null,
        communityPerformance: null,
        requiredLegNum: null,
        legLevelMin: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        level: [
          { required: true, message: "等级编码0:暂无,1:区代理,2:县代理,3:市代理,4:省代理,5:全国代理不能为空", trigger: "blur" }
        ],
        teamPerformance: [
          { required: true, message: "大区业绩不能为空", trigger: "blur" }
        ],
        performance: [
          { required: true, message: "个人业绩不能为空", trigger: "blur" }
        ],
        communityPerformance: [
          { required: true, message: "小区业绩不能为空", trigger: "blur" }
        ],
        requiredLegNum: [
          { required: true, message: "需要满足的线数量不能为空", trigger: "blur" }
        ],
        legLevelMin: [
          { required: true, message: "线内代理最小等级不能为空", trigger: "blur" }
        ],
        legLevelCount: [
          { required: true, message: "每条线里需要几个该等级不能为空", trigger: "blur" }
        ],
      }
    };
  },
  computed: {
    filteredLevelDicts() {
      const options = (this.dict && this.dict.type && this.dict.type.t_user_info_game_level) || [];
      return options.filter(item => Number(item.value) > 0);
    }
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询用户等级考核配置列表 */
    getList() {
      this.loading = true;
      listUserLevelConfig(this.queryParams).then(response => {
        this.userLevelConfigList = response.rows;
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
        level: null,
        teamPerformance: null,
        communityPerformance: null,
        requiredLegNum: null,
        legLevelMin: null,
        legLevelCount: 1,
        createTime: null,
        updateTime: null,
        remark: null,
        deleted: null,
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
      this.title = "添加用户等级考核配置";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getUserLevelConfig(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改用户等级考核配置";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateUserLevelConfig(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addUserLevelConfig(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除用户等级考核配置编号为"' + ids + '"的数据项？').then(function() {
        return delUserLevelConfig(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/userLevelConfig/export', {
        ...this.queryParams
      }, `userLevelConfig_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
