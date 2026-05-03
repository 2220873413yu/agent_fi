<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="商品ID" prop="id" label-width="120px">
        <el-input
          v-model="queryParams.id"
          placeholder="请输入商品ID"
          oninput="if(isNaN(value)) { value = null } else { value = value.replace('.', '') }"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>

      <el-form-item label="商品名称" prop="productName" label-width="120px">
        <el-input
          v-model="queryParams.productName"
          placeholder="请输入商品名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
<!--      <el-form-item label="商品编码" prop="productCode">
        <el-input
          v-model="queryParams.productCode"
          placeholder="请输入商品编码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="价格" prop="price">
        <el-input
          v-model="queryParams.price"
          placeholder="请输入价格"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="总库存冗余 -1不限" prop="stock">
        <el-input
          v-model="queryParams.stock"
          placeholder="请输入总库存冗余 -1不限"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->
      <el-form-item label="是否上架" prop="isEnabled" label-width="120px">
        <el-select v-model="queryParams.isEnabled" placeholder="请选择是否上架" clearable>
          <el-option
            v-for="dict in dict.type.t_user_info_is_valid"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
<!--      <el-form-item label="排序" prop="sort">
        <el-input
          v-model="queryParams.sort"
          placeholder="请输入排序"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>-->
      <el-form-item label="创建时间" label-width="120px">
        <el-date-picker
          v-model="daterangeCreateTime"
          style="width: 240px"
          value-format="yyyy-MM-dd HH:mm:ss"
          type="datetimerange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['xms:diyStoreProduct:add']"
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
          v-hasPermi="['xms:diyStoreProduct:edit']"
        >修改</el-button>
      </el-col>
<!--      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['xms:diyStoreProduct:remove']"
        >删除</el-button>
      </el-col>-->
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['xms:diyStoreProduct:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="diyStoreProductList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键id" align="center" prop="id" v-if="false"/>
      <el-table-column label="商品名称" align="center" prop="productName" />
<!--      <el-table-column label="商品编码" align="center" prop="productCode" />-->
      <el-table-column label="封面图" align="center" prop="coverImage" width="100">
        <template slot-scope="scope">
          <image-preview :src="scope.row.coverImage" :width="50" :height="50"/>
        </template>
      </el-table-column>
<!--      <el-table-column label="轮播图(JSON数组)" align="center" prop="sliderImage" width="100">
        <template slot-scope="scope">
          <image-preview :src="scope.row.sliderImage" :width="50" :height="50"/>
        </template>
      </el-table-column>
      <el-table-column label="详情图(JSON数组)" align="center" prop="detailImage" width="100">
        <template slot-scope="scope">
          <image-preview :src="scope.row.detailImage" :width="50" :height="50"/>
        </template>
      </el-table-column>-->
      <el-table-column label="价格" align="center" prop="price" />
      <el-table-column label="总销量" align="center" prop="sales" />
<!--      <el-table-column label="总库存冗余" align="center" prop="stock" />-->
      <el-table-column label="是否上架" align="center" prop="isEnabled">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.t_user_info_is_valid" :value="scope.row.isEnabled"/>
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sort" />
<!--      <el-table-column label="备注" align="center" prop="remark" />-->
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['xms:diyStoreProduct:edit']"
          >修改</el-button>
<!--          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['xms:diyStoreProduct:remove']"
          >删除</el-button>-->
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

    <!-- 添加或修改商品对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="1100px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="88px" class="product-edit-form">
        <el-tabs v-model="activeLang" type="card" style="margin-bottom: 8px;">
          <el-tab-pane label="中文" name="zh" />
          <el-tab-pane label="英文" name="en" />
        </el-tabs>
        <el-divider content-position="left">
          商品基础属性（{{ activeLang === 'zh' ? '中文' : '英文' }}）
        </el-divider>
        <el-row v-if="activeLang === 'zh'" :gutter="16">
          <el-col :span="24">
            <el-form-item label="商品名称" prop="productName">
              <el-input v-model="form.productName" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="封面图" prop="coverImage" class="image-form-item">
              <image-upload v-model="form.coverImage" :limit="1"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="轮播图" prop="sliderImage" class="image-form-item">
              <image-upload v-model="form.sliderImage" :limit="3"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="详情图" prop="detailImage" class="image-form-item">
              <image-upload v-model="form.detailImage" :limit="1"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-else :gutter="16">
          <el-col :span="24">
            <el-form-item label="商品名称(EN)" prop="productNameEn">
              <el-input v-model="form.productNameEn" placeholder="Please input product name in English" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="封面图(EN)" prop="coverImageEn" class="image-form-item">
              <image-upload v-model="form.coverImageEn" :limit="1"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="轮播图(EN)" prop="sliderImageEn" class="image-form-item">
              <image-upload v-model="form.sliderImageEn" :limit="3"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="详情图(EN)" prop="detailImageEn" class="image-form-item">
              <image-upload v-model="form.detailImageEn" :limit="1"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">商品配置</el-divider>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="8">
            <el-form-item label="排序" prop="sort">
              <el-input v-model="form.sort" placeholder="请输入排序" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="是否上架" prop="isEnabled">
              <el-select v-model="form.isEnabled" placeholder="请选择是否上架" style="width: 100%;">
                <el-option
                  v-for="dict in dict.type.t_user_info_is_valid"
                  :key="dict.value"
                  :label="dict.label"
                  :value="parseInt(dict.value)"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="规格类型" prop="specType">
              <el-radio-group v-model="form.specType"skuTextE @change="handleSpecTypeChange">
                <el-radio :label="0">单规格</el-radio>
                <el-radio :label="1">多规格</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <template v-if="form.specType === 0">
          <el-form-item label="规格明细">
            <el-table :data="[{ specName: '默认规格' }]" border size="mini">
              <el-table-column label="规格" min-width="180">
                <template slot-scope="scope">
                  <span>{{ scope.row.specName }}</span>
                </template>
              </el-table-column>
              <el-table-column label="价格" min-width="220">
                <template>
                  <el-form-item prop="singleSpecPrice" style="margin-bottom: 0;">
                    <el-input v-model="form.singleSpecPrice" placeholder="请输入价格" size="mini" />
                  </el-form-item>
                </template>
              </el-table-column>
              <el-table-column v-if="activeLang === 'en'" label="图片(英文)" min-width="220">
                <template>
                  <el-form-item prop="singleSpecImageEn" style="margin-bottom: 0;">
                    <image-upload v-model="form.singleSpecImageEn" :limit="1"/>
                  </el-form-item>
                </template>
              </el-table-column>
              <el-table-column v-else label="图片" min-width="220">
                <template>
                  <el-form-item prop="singleSpecImage" style="margin-bottom: 0;">
                    <image-upload v-model="form.singleSpecImage" :limit="1"/>
                  </el-form-item>
                </template>
              </el-table-column>
            </el-table>
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item label="选择规格" prop="specAttrIds">
            <el-select
              v-model="form.specAttrIds"
              multiple
              :loading="specAttrLoading"
              clearable
              filterable
              collapse-tags
              style="width: 100%;"
              placeholder="请选择规格模板（可多选）"
              @change="handleRuleSelectionChange"
            >
              <el-option
                v-for="item in specAttrOptions"
                :key="item.id"
                :label="formatRuleOptionLabel(item)"
                :value="item.id"
              />
            </el-select>
            <div style="color: #909399; font-size: 12px; margin-top: 4px;">
              先选择规格模板（如 CPU / 尺寸 / 硬盘），再点击“生成规格组合”。
            </div>
          </el-form-item>

          <el-form-item label="已选规格">
            <div v-if="selectedSpecPreviewList.length" style="padding: 8px 10px; background: #f8f9fb; border-radius: 4px;">
              <div
                v-for="item in selectedSpecPreviewList"
                :key="`preview-${item.id}`"
                style="line-height: 1.8; color: #303133; font-size: 13px;"
              >
                {{ item.attrName || "-" }}：{{ item.displayValues }}
              </div>
            </div>
            <el-button type="primary" plain size="mini" icon="el-icon-magic-stick" style="margin-top: 8px;" @click="generateSkuRows">
              生成规格组合
            </el-button>
          </el-form-item>

          <el-form-item label="规格明细">
            <el-table v-if="skuTableData.length" :data="skuTableData" border size="mini">
              <el-table-column
                v-for="column in skuRuleColumns"
                :key="`col-${column}`"
                :label="column"
                min-width="120"
              >
                <template slot-scope="scope">
                  <span>{{ scope.row.specMap[column] || "-" }}</span>
                </template>
              </el-table-column>
              <el-table-column label="价格" min-width="140">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.price" placeholder="价格" size="mini" />
                </template>
              </el-table-column>
              <el-table-column v-if="activeLang === 'zh'" label="图片" min-width="160">
                <template slot-scope="scope">
                  <image-upload v-model="scope.row.image" :limit="1"/>
                </template>
              </el-table-column>
              <el-table-column v-else label="图片(英文)" min-width="160">
                <template slot-scope="scope">
                  <image-upload v-model="scope.row.imageEn" :limit="1"/>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="90" align="center" fixed="right">
                <template slot-scope="scope">
                  <el-button type="text" size="mini" style="color: #f56c6c;" @click="removeSkuRow(scope.$index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-else style="color: #909399; font-size: 12px;">
              暂未生成规格组合，请先选择规格后点击“生成规格组合”。
            </div>
          </el-form-item>
        </template>

      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDiyStoreProduct, getDiyStoreProduct, delDiyStoreProduct, addDiyStoreProduct, getProductRuleList } from "@/api/xms/diyStoreProduct";

export default {
  name: "DiyStoreProduct",
  dicts: ['t_user_info_is_valid'],
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
      // 商品表格数据
      diyStoreProductList: [],
      // 弹出层标题
      title: "",
      // 弹框语言切换（中文/英文）
      activeLang: "zh",
      // 是否显示弹出层
      open: false,
      // 是否删除 0否1是时间范围
      daterangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        productName: null,
        productCode: null,
        coverImage: null,
        sliderImage: null,
        detailImage: null,
        price: null,
        sales: null,
        stock: null,
        isEnabled: null,
        sort: null,
        createTime: null,
      },
      // 表单参数
      form: {},
      // 全部规格选项
      specAttrOptions: [],
      // 规格加载中
      specAttrLoading: false,
      // 已选规格预览
      selectedSpecPreviewList: [],
      // 多规格表头（规格名）
      skuRuleColumns: [],
      // 多规格明细（笛卡尔积后）
      skuTableData: [],
      // 表单校验
      rules: {
        productName: [
          { required: true, message: "商品名称不能为空", trigger: "blur" }
        ],
        productNameEn: [
          { required: true, message: "英文商品名称不能为空", trigger: "blur" }
        ],
        coverImage: [
          { required: true, message: "封面图不能为空", trigger: "change" }
        ],
        coverImageEn: [
          { required: true, message: "英文封面图不能为空", trigger: "change" }
        ],
        sliderImage: [
          { required: true, message: "轮播图不能为空", trigger: "change" }
        ],
        sliderImageEn: [
          { required: true, message: "英文轮播图不能为空", trigger: "change" }
        ],
        detailImage: [
          { required: true, message: "详情图不能为空", trigger: "change" }
        ],
        detailImageEn: [
          { required: true, message: "英文详情图不能为空", trigger: "change" }
        ],
        specType: [
          { required: true, message: "请选择规格类型", trigger: "change" }
        ],
        specAttrIds: [
          {
            validator: (_rule, value, callback) => {
              if (this.form.specType !== 1) {
                callback();
                return;
              }
              const ids = Array.isArray(value) ? value : (value ? [value] : []);
              if (ids.length === 0) {
                callback(new Error("多规格模式下请至少选择一个规格"));
                return;
              }
              callback();
            },
            trigger: "change"
          }
        ],
        singleSpecPrice: [
          {
            validator: (_rule, value, callback) => {
              if (this.form.specType !== 0) {
                callback();
                return;
              }
              if (value === null || value === undefined || value === "") {
                callback(new Error("单规格价格不能为空"));
                return;
              }
              if (!this.isValidPrice(value)) {
                callback(new Error("单规格价格格式错误，最多2位小数"));
                return;
              }
              callback();
            },
            trigger: "blur"
          }
        ],
        singleSpecImage: [
          {
            validator: (_rule, value, callback) => {
              if (this.form.specType !== 0) {
                callback();
                return;
              }
              if (!this.hasImageValue(value)) {
                callback(new Error("单规格图片不能为空"));
                return;
              }
              callback();
            },
            trigger: "change"
          }
        ],
        singleSpecImageEn: [
          {
            validator: (_rule, value, callback) => {
              if (this.form.specType !== 0) {
                callback();
                return;
              }
              if (!this.hasImageValue(value)) {
                callback(new Error("单规格英文图片不能为空"));
                return;
              }
              callback();
            },
            trigger: "change"
          }
        ],
        isEnabled: [
          { required: true, message: "是否上架 0否1是不能为空", trigger: "change" }
        ],
        sort: [
          { required: true, message: "排序不能为空", trigger: "blur" },
          {
            validator: (_rule, value, callback) => {
              const sortVal = String(value === null || value === undefined ? "" : value).trim();
              if (!sortVal) {
                callback(new Error("排序不能为空"));
                return;
              }
              if (!/^\d+$/.test(sortVal)) {
                callback(new Error("排序只能输入整数"));
                return;
              }
              callback();
            },
            trigger: "blur"
          }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询商品列表 */
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      if (null != this.daterangeCreateTime && '' != this.daterangeCreateTime) {
        this.queryParams.params["beginCreateTime"] = this.daterangeCreateTime[0];
        this.queryParams.params["endCreateTime"] = this.daterangeCreateTime[1];
      }
      listDiyStoreProduct(this.queryParams).then(response => {
        this.diyStoreProductList = response.rows;
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
        productName: null,
        productNameEn: null,
        productCode: null,
        coverImage: null,
        coverImageEn: null,
        sliderImage: null,
        sliderImageEn: null,
        detailImage: null,
        detailImageEn: null,
        price: null,
        specType: 0,
        specAttrIds: [],
        singleSpecPrice: null,
        singleSpecImage: null,
        singleSpecImageEn: null,
        sales: null,
        stock: null,
        isEnabled: null,
        sort: null,
        remark: null,
        createTime: null,
        createBy: null,
        updateTime: null,
        updateBy: null,
        deleted: null
      };
      this.selectedSpecPreviewList = [];
      this.skuRuleColumns = [];
      this.skuTableData = [];
      this.activeLang = "zh";
      this.$nextTick(() => {
        if (this.$refs.form) {
          this.$refs.form.clearValidate();
        }
      });
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
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    async handleAdd() {
      this.reset();
      await this.loadSpecAttrOptions();
      this.refreshSelectedSpecPreview();
      this.open = true;
      this.title = "添加商品";
    },
    /** 修改按钮操作 */
    async handleUpdate(row) {
      this.reset();
      await this.loadSpecAttrOptions();
      const id = row.id || this.ids
      getDiyStoreProduct(id).then(response => {
        this.form = response.data;
        if (this.form.specType === null || this.form.specType === undefined) {
          this.$set(this.form, "specType", 0);
        }
        if (!Array.isArray(this.form.specAttrIds)) {
          this.$set(this.form, "specAttrIds", this.form.specAttrIds ? [this.form.specAttrIds] : []);
        }
        if (this.form.singleSpecPrice === undefined) {
          this.$set(this.form, "singleSpecPrice", this.form.price || null);
        }
        if (this.form.singleSpecImage === undefined) {
          this.$set(this.form, "singleSpecImage", null);
        }
        if (this.form.singleSpecImageEn === undefined) {
          this.$set(this.form, "singleSpecImageEn", null);
        }
        this.refreshSelectedSpecPreview();
        this.hydrateSkuDataFromProductDetail(this.form);
        this.open = true;
        this.title = "修改商品";
      });
    },
    handleSpecTypeChange(value) {
      if (value === 0) {
        this.form.specAttrIds = [];
        this.skuRuleColumns = [];
        this.skuTableData = [];
      } else {
        this.form.singleSpecPrice = null;
        this.form.singleSpecImage = null;
        this.form.singleSpecImageEn = null;
      }
      this.refreshSelectedSpecPreview();
    },
    handleRuleSelectionChange() {
      this.refreshSelectedSpecPreview();
      this.skuRuleColumns = [];
      this.skuTableData = [];
    },
    async loadSpecAttrOptions() {
      this.specAttrLoading = true;
      try {
        const res = await getProductRuleList();
        this.specAttrOptions = Array.isArray(res) ? res : [];
        this.refreshSelectedSpecPreview();
      } finally {
        this.specAttrLoading = false;
      }
    },
    parseRuleValues(ruleValue) {
      if (!ruleValue) {
        return [];
      }
      try {
        const parsed = typeof ruleValue === "string" ? JSON.parse(ruleValue) : ruleValue;
        if (!Array.isArray(parsed) || parsed.length === 0) {
          return [];
        }
        const first = parsed[0] || {};
        if (Array.isArray(first.detail)) {
          return first.detail.map(v => (v || "").trim()).filter(Boolean);
        }
        if (Array.isArray(first.values)) {
          return first.values.map(v => (v || "").trim()).filter(Boolean);
        }
        if (typeof first.attrValues === "string") {
          return first.attrValues.split(/[,，]/).map(v => v.trim()).filter(Boolean);
        }
        if (typeof first === "string") {
          return parsed.map(v => (v || "").trim()).filter(Boolean);
        }
        return [];
      } catch (e) {
        return [];
      }
    },
    formatRuleOptionLabel(item) {
      const values = this.parseRuleValues(item && item.ruleValue);
      return `${(item && item.ruleName) || "-"}（${values.length ? values.join(" / ") : "-"}）`;
    },
    isValidPrice(value) {
      const val = String(value === null || value === undefined ? "" : value).trim();
      return /^(0|[1-9]\d*)(\.\d{1,2})?$/.test(val);
    },
    hasImageValue(value) {
      if (value === null || value === undefined) {
        return false;
      }
      if (Array.isArray(value)) {
        return value.some(v => String(v || "").trim());
      }
      if (typeof value === "string") {
        return value.trim().length > 0;
      }
      if (typeof value === "object") {
        if (value.url && String(value.url).trim()) {
          return true;
        }
        return Object.keys(value).length > 0;
      }
      return !!value;
    },
    formatAttrValuesForPreview(attrValues) {
      if (Array.isArray(attrValues)) {
        const arr = attrValues.map(v => (v || "").trim()).filter(Boolean);
        return arr.length > 0 ? arr.join(" / ") : "-";
      }
      const values = String(attrValues || "")
        .split(/[,，]/)
        .map(v => v.trim())
        .filter(Boolean);
      return values.length > 0 ? values.join(" / ") : "-";
    },
    refreshSelectedSpecPreview() {
      const raw = this.form.specAttrIds;
      const ids = Array.isArray(raw) ? raw : (raw || raw === 0 ? [raw] : []);
      const idSet = new Set(ids.map(id => String(id)));
      this.selectedSpecPreviewList = (this.specAttrOptions || [])
        .filter(item => idSet.has(String(item.id)))
        .map(item => ({
          id: item.id,
          attrName: item.ruleName || item.attrName || "-",
          displayValues: this.parseRuleValues(item.ruleValue).join(" / ") || this.formatAttrValuesForPreview(item.attrValues)
        }));
    },
    getSelectedRuleList() {
      const idSet = new Set((this.form.specAttrIds || []).map(id => String(id)));
      return (this.specAttrOptions || [])
        .filter(item => idSet.has(String(item.id)))
        .map(item => ({
          id: item.id,
          ruleName: item.ruleName || item.attrName || "-",
          values: this.parseRuleValues(item.ruleValue)
        }));
    },
    buildCartesianCombinations(ruleList) {
      if (!Array.isArray(ruleList) || ruleList.length === 0) {
        return [];
      }
      const result = [];
      const walk = (index, currentMap) => {
        if (index === ruleList.length) {
          result.push({ ...currentMap });
          return;
        }
        const rule = ruleList[index];
        rule.values.forEach(v => {
          currentMap[rule.ruleName] = v;
          walk(index + 1, currentMap);
        });
      };
      walk(0, {});
      return result;
    },
    generateSkuRows() {
      const ruleList = this.getSelectedRuleList();
      if (ruleList.length === 0) {
        this.$message.warning("请先选择规格模板");
        return;
      }
      const invalidRule = ruleList.find(r => !Array.isArray(r.values) || r.values.length === 0);
      if (invalidRule) {
        this.$message.warning(`规格【${invalidRule.ruleName}】没有可用规格值`);
        return;
      }
      const combinations = this.buildCartesianCombinations(ruleList);
      const previousMap = new Map((this.skuTableData || []).map(item => [item.sku, item]));
      this.skuRuleColumns = ruleList.map(r => r.ruleName);
      this.skuTableData = combinations.map(specMap => {
        const sku = this.skuRuleColumns.map(col => specMap[col]).join("|");
        const oldRow = previousMap.get(sku);
        return {
          sku,
          specMap,
          price: oldRow ? oldRow.price : null,
          image: oldRow ? oldRow.image : null,
          imageEn: oldRow ? oldRow.imageEn : null
        };
      });
      this.$message.success(`已生成 ${this.skuTableData.length} 条规格组合`);
    },
    removeSkuRow(index) {
      if (!Array.isArray(this.skuTableData) || this.skuTableData.length === 0) {
        return;
      }
      if (this.skuTableData.length <= 1) {
        this.$message.warning("至少保留一个规格组合");
        return;
      }
      this.skuTableData.splice(index, 1);
      this.$message.success("已删除规格组合");
    },
    hydrateSkuDataFromProductDetail(productData) {
      const attrValueList = Array.isArray(productData && productData.attrValueList)
        ? productData.attrValueList
        : [];
      if (!attrValueList.length) {
        return;
      }
      if (productData.specType === 0) {
        const first = attrValueList[0];
        this.form.singleSpecPrice = first && first.price !== undefined && first.price !== null ? first.price : this.form.singleSpecPrice;
        this.form.singleSpecImage = first ? first.image : this.form.singleSpecImage;
        this.form.singleSpecImageEn = first ? first.imageEn : this.form.singleSpecImageEn;
        return;
      }
      let columns = this.selectedSpecPreviewList.map(item => item.attrName);
      if (!columns.length) {
        columns = this.getSelectedRuleList().map(item => item.ruleName);
      }
      if (!columns.length) {
        const firstSku = attrValueList[0] && attrValueList[0].sku ? String(attrValueList[0].sku) : "";
        const size = firstSku ? firstSku.split("|").length : 0;
        columns = Array.from({ length: size }, (_v, idx) => `规格${idx + 1}`);
      }
      if (!columns.length) {
        return;
      }
      this.skuRuleColumns = columns;
      this.skuTableData = attrValueList.map(item => {
        const sku = item && item.sku ? item.sku : "";
        const parts = sku ? String(sku).split("|") : [];
        const specMap = {};
        this.skuRuleColumns.forEach((col, idx) => {
          specMap[col] = parts[idx] || "";
        });
        return {
          sku,
          specMap,
          price: item ? item.price : null,
          image: item ? item.image : null,
          imageEn: item ? item.imageEn : null
        };
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          if (this.form.specType === 0) {
            if (this.form.singleSpecPrice === null || this.form.singleSpecPrice === undefined || this.form.singleSpecPrice === "") {
              this.$message.warning("请填写单规格价格");
              return;
            }
            if (this.activeLang === "en" && !this.hasImageValue(this.form.singleSpecImageEn)) {
              this.$message.warning("请上传单规格英文图片");
              return;
            }
            if (this.activeLang === "zh" && !this.hasImageValue(this.form.singleSpecImage)) {
              this.$message.warning("请上传单规格图片");
              return;
            }
          }
          this.$message.warning("请完善必填项后再提交");
          return;
        }
        const payload = { ...this.form };
        let attrValueList = [];
        if (this.form.specType === 0) {
          if (this.form.singleSpecPrice === null || this.form.singleSpecPrice === undefined || this.form.singleSpecPrice === "") {
            this.$message.warning("请填写单规格价格");
            return;
          }
          if (!this.hasImageValue(this.form.singleSpecImage)) {
            this.$message.warning("请上传单规格图片");
            return;
          }
          if (!this.hasImageValue(this.form.singleSpecImageEn)) {
            this.$message.warning("请上传单规格英文图片");
            return;
          }
          attrValueList = [{
            sku: "默认",
            price: this.form.singleSpecPrice,
            image: this.form.singleSpecImage,
            imageEn: this.form.singleSpecImageEn,
            stock: "0",
            sales: "0"
          }];
          payload.price = this.form.singleSpecPrice;
        } else {
          if (!Array.isArray(this.form.specAttrIds) || this.form.specAttrIds.length === 0) {
            this.$message.warning("请选择规格后再生成规格组合");
            return;
          }
          if (!Array.isArray(this.skuTableData) || this.skuTableData.length === 0) {
            this.$message.warning("请先点击生成规格组合");
            return;
          }
          const missing = this.skuTableData.find(item => (item.price === null || item.price === undefined || item.price === "") || !this.hasImageValue(item.image) || !this.hasImageValue(item.imageEn));
          if (missing) {
            this.$message.warning(`规格组合【${missing.sku || "-"}】请完善价格、中文图、英文图`);
            return;
          }
          const invalidPriceRow = this.skuTableData.find(item => !this.isValidPrice(item.price));
          if (invalidPriceRow) {
            this.$message.warning(`规格组合【${invalidPriceRow.sku || "-"}】价格格式错误，最多2位小数`);
            return;
          }
          attrValueList = this.skuTableData.map(item => ({
            sku: item.sku,
            price: item.price,
            image: item.image,
            imageEn: item.imageEn,
            stock: "0",
            sales: "0"
          }));
          payload.price = this.skuTableData[0].price;
        }
        payload.attrValueList = attrValueList;
        addDiyStoreProduct(payload).then(response => {
          this.$modal.msgSuccess(this.form.id ? "修改成功" : "新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除商品编号为"' + ids + '"的数据项？').then(function() {
        return delDiyStoreProduct(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/diyStoreProduct/export', {
        ...this.queryParams
      }, `diyStoreProduct_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>

<style scoped>
.product-edit-form {
  max-height: 72vh;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 6px;
}

.image-form-item {
  margin-bottom: 10px;
}

.image-form-item :deep(.el-form-item__content) {
  display: flex;
  justify-content: flex-start;
}
</style>
