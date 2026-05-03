<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
<!--      <el-form-item label="管理费池比例(单位%)，默认20=20%" prop="feePoolRatio">
        <el-input
          v-model="queryParams.feePoolRatio"
          placeholder="请输入管理费池比例(单位%)，默认20=20%"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="县代理总比例(单位%)" prop="agentDiffCountyRatio">
        <el-input
          v-model="queryParams.agentDiffCountyRatio"
          placeholder="请输入县代理总比例(单位%)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="区代理总比例(单位%)" prop="agentDiffAreaRatio">
        <el-input
          v-model="queryParams.agentDiffAreaRatio"
          placeholder="请输入区代理总比例(单位%)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="市代理总比例(单位%)" prop="agentDiffCityRatio">
        <el-input
          v-model="queryParams.agentDiffCityRatio"
          placeholder="请输入市代理总比例(单位%)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="省代理总比例(单位%)" prop="agentDiffProvinceRatio">
        <el-input
          v-model="queryParams.agentDiffProvinceRatio"
          placeholder="请输入省代理总比例(单位%)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="全国代理总比例(单位%)" prop="agentDiffNationalRatio">
        <el-input
          v-model="queryParams.agentDiffNationalRatio"
          placeholder="请输入全国代理总比例(单位%)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="全国代理平级奖比例(单位%)：取全国级差奖励的X%" prop="nationalSameLevelRatio">
        <el-input
          v-model="queryParams.nationalSameLevelRatio"
          placeholder="请输入全国代理平级奖比例(单位%)：取全国级差奖励的X%"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="平台管理费比例(单位%)" prop="platformFeeRatio">
        <el-input
          v-model="queryParams.platformFeeRatio"
          placeholder="请输入平台管理费比例(单位%)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="直推奖励比例(单位%)" prop="directPushRatio">
        <el-input
          v-model="queryParams.directPushRatio"
          placeholder="请输入直推奖励比例(单位%)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="间推奖励比例(单位%)" prop="indirectPushRatio">
        <el-input
          v-model="queryParams.indirectPushRatio"
          placeholder="请输入间推奖励比例(单位%)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="服务中心比例(单位%)" prop="serviceCenterRatio">
        <el-input
          v-model="queryParams.serviceCenterRatio"
          placeholder="请输入服务中心比例(单位%)"
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
          v-hasPermi="['xms:miningMgmtFeeConfig:add']"
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
          v-hasPermi="['xms:miningMgmtFeeConfig:edit']"
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
          v-hasPermi="['xms:miningMgmtFeeConfig:remove']"
        >删除</el-button>
      </el-col>-->
<!--      <el-col :span="1.5">
        <el-button
          v-hasPermi="['xms:miningMgmtFeeConfig:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出</el-button>
      </el-col>-->
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="miningMgmtFeeConfigList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55" />
      <el-table-column v-if="false" align="center" label="主键id" prop="id"/>
<!--      <el-table-column label="管理费池比例(单位%)，默认20=20%" align="center" prop="feePoolRatio" />-->
      <el-table-column align="center" label="区代理比例" prop="agentDiffAreaRatio" >
        <template slot-scope="scope">
          <span>{{ scope.row.agentDiffAreaRatio }} %</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="县代理比例" prop="agentDiffCountyRatio"  >
        <template slot-scope="scope">
          <span>{{ scope.row.agentDiffCountyRatio }} %</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="市代理比例" prop="agentDiffCityRatio"  >
        <template slot-scope="scope">
          <span>{{ scope.row.agentDiffCityRatio }} %</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="省代理比例" prop="agentDiffProvinceRatio" >
        <template slot-scope="scope">
          <span>{{ scope.row.agentDiffProvinceRatio }} %</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="全国代理比例" prop="agentDiffNationalRatio"  >
        <template slot-scope="scope">
          <span>{{ scope.row.agentDiffNationalRatio }} %</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="全国代理平级奖比例" prop="nationalSameLevelRatio"  >
        <template slot-scope="scope">
          <span>{{ scope.row.nationalSameLevelRatio }} %</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="平台管理费比例" prop="platformFeeRatio"  >
        <template slot-scope="scope">
          <span>{{ scope.row.platformFeeRatio }} %</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="直推奖励比例" prop="directPushRatio" >
        <template slot-scope="scope">
          <span>{{ scope.row.directPushRatio }} %</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="间推奖励比例" prop="indirectPushRatio"  >
        <template slot-scope="scope">
          <span>{{ scope.row.indirectPushRatio }} %</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="服务中心比例" prop="serviceCenterRatio"  >
        <template slot-scope="scope">
          <span>{{ scope.row.serviceCenterRatio }} %</span>
        </template>
      </el-table-column>
<!--      <el-table-column label="备注" align="center" prop="remark" />-->
      <el-table-column align="center" label="创建时间" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="修改时间" prop="updateTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['xms:miningMgmtFeeConfig:edit']"
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
            v-hasPermi="['xms:miningMgmtFeeConfig:remove']"
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

    <!-- 添加或修改矿机管理费配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
<!--        <el-form-item label="管理费池比例(单位%)，默认20=20%" prop="feePoolRatio">
          <el-input v-model="form.feePoolRatio" placeholder="请输入管理费池比例(单位%)，默认20=20%" />
        </el-form-item>-->
        <el-form-item label="区代理比例" prop="agentDiffAreaRatio">
          <el-input
            v-model="form.agentDiffAreaRatio"
            placeholder="请输入区代理比例(单位%)"
            @input="value => handleRatioInput('agentDiffAreaRatio', value)"
          />
        </el-form-item>

        <el-form-item label="县代理比例" prop="agentDiffCountyRatio">
          <el-input
            v-model="form.agentDiffCountyRatio"
            placeholder="请输入县代理比例(单位%)"
            @input="value => handleRatioInput('agentDiffCountyRatio', value)"
          />
        </el-form-item>

        <el-form-item label="市代理比例" prop="agentDiffCityRatio">
          <el-input
            v-model="form.agentDiffCityRatio"
            placeholder="请输入市代理比例(单位%)"
            @input="value => handleRatioInput('agentDiffCityRatio', value)"
          />
        </el-form-item>
        <el-form-item label="省代理比例" prop="agentDiffProvinceRatio">
          <el-input
            v-model="form.agentDiffProvinceRatio"
            placeholder="请输入省代理比例(单位%)"
            @input="value => handleRatioInput('agentDiffProvinceRatio', value)"
          />
        </el-form-item>
        <el-form-item label="全国代理比例" prop="agentDiffNationalRatio">
          <el-input
            v-model="form.agentDiffNationalRatio"
            placeholder="请输入全国代理比例(单位%)"
            @input="value => handleRatioInput('agentDiffNationalRatio', value)"
          />
        </el-form-item>
        <el-form-item label="全国代理平级奖比例" prop="nationalSameLevelRatio">
          <el-input
            v-model="form.nationalSameLevelRatio"
            placeholder="请输入全国代理平级奖比例(单位%)"
            @input="value => handleRatioInput('nationalSameLevelRatio', value)"
          />
        </el-form-item>
        <el-form-item label="平台管理费比例" prop="platformFeeRatio">
          <el-input
            v-model="form.platformFeeRatio"
            placeholder="请输入平台管理费比例(单位%)"
            @input="value => handleRatioInput('platformFeeRatio', value)"
          />
        </el-form-item>
        <el-form-item label="直推奖励比例" prop="directPushRatio">
          <el-input
            v-model="form.directPushRatio"
            placeholder="请输入直推奖励比例(单位%)"
            @input="value => handleRatioInput('directPushRatio', value)"
          />
        </el-form-item>
        <el-form-item label="间推奖励比例" prop="indirectPushRatio">
          <el-input
            v-model="form.indirectPushRatio"
            placeholder="请输入间推奖励比例(单位%)"
            @input="value => handleRatioInput('indirectPushRatio', value)"
          />
        </el-form-item>
        <el-form-item label="服务中心比例" prop="serviceCenterRatio">
          <el-input
            v-model="form.serviceCenterRatio"
            placeholder="请输入服务中心比例(单位%)"
            @input="value => handleRatioInput('serviceCenterRatio', value)"
          />
        </el-form-item>
<!--        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="删除标记 0:未删,1:已删" prop="deleted">
          <el-input v-model="form.deleted" placeholder="请输入删除标记 0:未删,1:已删" />
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
import { listMiningMgmtFeeConfig, getMiningMgmtFeeConfig, delMiningMgmtFeeConfig, addMiningMgmtFeeConfig, updateMiningMgmtFeeConfig } from "@/api/xms/miningMgmtFeeConfig";

export default {
  name: "MiningMgmtFeeConfig",
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
      // 矿机管理费配置表格数据
      miningMgmtFeeConfigList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        feePoolRatio: null,
        agentDiffCountyRatio: null,
        agentDiffAreaRatio: null,
        agentDiffCityRatio: null,
        agentDiffProvinceRatio: null,
        agentDiffNationalRatio: null,
        nationalSameLevelRatio: null,
        platformFeeRatio: null,
        directPushRatio: null,
        indirectPushRatio: null,
        serviceCenterRatio: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        agentDiffCountyRatio: [
          { required: true, message: "县代理比例不能为空", trigger: "blur" }
        ],
        agentDiffAreaRatio: [
          { required: true, message: "区代理比例不能为空", trigger: "blur" }
        ],
        agentDiffCityRatio: [
          { required: true, message: "市代理比例不能为空", trigger: "blur" }
        ],
        agentDiffProvinceRatio: [
          { required: true, message: '省代理比例不能为空', trigger: "blur" }
        ],
        agentDiffNationalRatio: [
          { required: true, message: "全国代理比例不能为空", trigger: "blur" }
        ],
        nationalSameLevelRatio: [
          { required: true, message: "全国代理平级奖比例", trigger: "blur" }
        ],
        platformFeeRatio: [
          { required: true, message: "平台管理费比例不能为空", trigger: "blur" }
        ],
        directPushRatio: [
          { required: true, message: "直推奖励比例不能为空", trigger: "blur" }
        ],
        indirectPushRatio: [
          { required: true, message: "间推奖励比例不能为空", trigger: "blur" }
        ],
        serviceCenterRatio: [
          { required: true, message: "服务中心比例不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    handleRatioInput(field, value) {
      this.form[field] = this.normalizeDecimal2(value);
    },
    normalizeDecimal2(value) {
      if (value === null || value === undefined) {
        return "";
      }
      const raw = String(value).replace(/[^\d.]/g, "");
      if (!raw) {
        return "";
      }
      const endsWithDot = raw.endsWith(".");
      const parts = raw.split(".");
      let intPart = parts[0] || "0";
      intPart = intPart.replace(/^0+(?=\d)/, "");
      if (intPart === "") {
        intPart = "0";
      }
      let decimalPart = parts[1] || "";
      decimalPart = decimalPart.slice(0, 2);
      if (endsWithDot && decimalPart === "") {
        return `${intPart}.`;
      }
      return decimalPart ? `${intPart}.${decimalPart}` : intPart;
    },
    /** 查询矿机管理费配置列表 */
    getList() {
      this.loading = true;
      listMiningMgmtFeeConfig(this.queryParams).then(response => {
        this.miningMgmtFeeConfigList = response.rows;
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
        feePoolRatio: null,
        agentDiffCountyRatio: null,
        agentDiffAreaRatio: null,
        agentDiffCityRatio: null,
        agentDiffProvinceRatio: null,
        agentDiffNationalRatio: null,
        nationalSameLevelRatio: null,
        platformFeeRatio: null,
        directPushRatio: null,
        indirectPushRatio: null,
        serviceCenterRatio: null,
        remark: null,
        createTime: null,
        updateTime: null,
        deleted: null
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
      this.title = "添加矿机管理费配置";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getMiningMgmtFeeConfig(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改矿机管理费配置";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateMiningMgmtFeeConfig(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addMiningMgmtFeeConfig(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除矿机管理费配置编号为"' + ids + '"的数据项？').then(function() {
        return delMiningMgmtFeeConfig(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/miningMgmtFeeConfig/export', {
        ...this.queryParams
      }, `miningMgmtFeeConfig_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
