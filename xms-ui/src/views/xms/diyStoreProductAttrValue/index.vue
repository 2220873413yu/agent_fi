<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="商品属性索引值(attr_value|attr_value[|...])" prop="sku">
        <el-input
          v-model="queryParams.sku"
          placeholder="请输入商品属性索引值(attr_value|attr_value[|...])"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="销售价" prop="price">
        <el-input
          v-model="queryParams.price"
          placeholder="请输入销售价"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="唯一值(可放规则签名)" prop="codeUnique">
        <el-input
          v-model="queryParams.codeUnique"
          placeholder="请输入唯一值(可放规则签名)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="成本价" prop="cost">
        <el-input
          v-model="queryParams.cost"
          placeholder="请输入成本价"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="商品条码" prop="barCode">
        <el-input
          v-model="queryParams.barCode"
          placeholder="请输入商品条码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="重量(可选)" prop="weight">
        <el-input
          v-model="queryParams.weight"
          placeholder="请输入重量(可选)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="体积(可选)" prop="volume">
        <el-input
          v-model="queryParams.volume"
          placeholder="请输入体积(可选)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="扩展字段1(保留)" prop="brokerage">
        <el-input
          v-model="queryParams.brokerage"
          placeholder="请输入扩展字段1(保留)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="扩展字段2(保留)" prop="brokerageTwo">
        <el-input
          v-model="queryParams.brokerageTwo"
          placeholder="请输入扩展字段2(保留)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="拼团价(保留)" prop="pinkPrice">
        <el-input
          v-model="queryParams.pinkPrice"
          placeholder="请输入拼团价(保留)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="拼团库存(保留)" prop="pinkStock">
        <el-input
          v-model="queryParams.pinkStock"
          placeholder="请输入拼团库存(保留)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="秒杀价(保留)" prop="seckillPrice">
        <el-input
          v-model="queryParams.seckillPrice"
          placeholder="请输入秒杀价(保留)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="秒杀库存(保留)" prop="seckillStock">
        <el-input
          v-model="queryParams.seckillStock"
          placeholder="请输入秒杀库存(保留)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="积分(保留)" prop="integral">
        <el-input
          v-model="queryParams.integral"
          placeholder="请输入积分(保留)"
          clearable
          @keyup.enter.native="handleQuery"
        />
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
          v-hasPermi="['xms:diyStoreProductAttrValue:add']"
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
          v-hasPermi="['xms:diyStoreProductAttrValue:edit']"
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
          v-hasPermi="['xms:diyStoreProductAttrValue:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['xms:diyStoreProductAttrValue:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="diyStoreProductAttrValueList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="${comment}" align="center" prop="id" />
      <el-table-column label="商品ID" align="center" prop="productId" />
      <el-table-column label="商品属性索引值(attr_value|attr_value[|...])" align="center" prop="sku" />
      <el-table-column label="属性对应库存" align="center" prop="stock" />
      <el-table-column label="销量" align="center" prop="sales" />
      <el-table-column label="销售价" align="center" prop="price" />
      <el-table-column label="图片" align="center" prop="image" width="100">
        <template slot-scope="scope">
          <image-preview :src="scope.row.image" :width="50" :height="50"/>
        </template>
      </el-table-column>
      <el-table-column label="唯一值(可放规则签名)" align="center" prop="codeUnique" />
      <el-table-column label="成本价" align="center" prop="cost" />
      <el-table-column label="商品条码" align="center" prop="barCode" />
      <el-table-column label="重量(可选)" align="center" prop="weight" />
      <el-table-column label="体积(可选)" align="center" prop="volume" />
      <el-table-column label="扩展字段1(保留)" align="center" prop="brokerage" />
      <el-table-column label="扩展字段2(保留)" align="center" prop="brokerageTwo" />
      <el-table-column label="拼团价(保留)" align="center" prop="pinkPrice" />
      <el-table-column label="拼团库存(保留)" align="center" prop="pinkStock" />
      <el-table-column label="秒杀价(保留)" align="center" prop="seckillPrice" />
      <el-table-column label="秒杀库存(保留)" align="center" prop="seckillStock" />
      <el-table-column label="积分(保留)" align="center" prop="integral" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['xms:diyStoreProductAttrValue:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['xms:diyStoreProductAttrValue:remove']"
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

    <!-- 添加或修改商品属性值(SKU)对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="商品属性索引值(attr_value|attr_value[|...])" prop="sku">
          <el-input v-model="form.sku" placeholder="请输入商品属性索引值(attr_value|attr_value[|...])" />
        </el-form-item>
        <el-form-item label="销售价" prop="price">
          <el-input v-model="form.price" placeholder="请输入销售价" />
        </el-form-item>
        <el-form-item label="图片" prop="image">
          <image-upload v-model="form.image"/>
        </el-form-item>
        <el-form-item label="唯一值(可放规则签名)" prop="codeUnique">
          <el-input v-model="form.codeUnique" placeholder="请输入唯一值(可放规则签名)" />
        </el-form-item>
        <el-form-item label="成本价" prop="cost">
          <el-input v-model="form.cost" placeholder="请输入成本价" />
        </el-form-item>
        <el-form-item label="商品条码" prop="barCode">
          <el-input v-model="form.barCode" placeholder="请输入商品条码" />
        </el-form-item>
        <el-form-item label="重量(可选)" prop="weight">
          <el-input v-model="form.weight" placeholder="请输入重量(可选)" />
        </el-form-item>
        <el-form-item label="体积(可选)" prop="volume">
          <el-input v-model="form.volume" placeholder="请输入体积(可选)" />
        </el-form-item>
        <el-form-item label="扩展字段1(保留)" prop="brokerage">
          <el-input v-model="form.brokerage" placeholder="请输入扩展字段1(保留)" />
        </el-form-item>
        <el-form-item label="扩展字段2(保留)" prop="brokerageTwo">
          <el-input v-model="form.brokerageTwo" placeholder="请输入扩展字段2(保留)" />
        </el-form-item>
        <el-form-item label="拼团价(保留)" prop="pinkPrice">
          <el-input v-model="form.pinkPrice" placeholder="请输入拼团价(保留)" />
        </el-form-item>
        <el-form-item label="拼团库存(保留)" prop="pinkStock">
          <el-input v-model="form.pinkStock" placeholder="请输入拼团库存(保留)" />
        </el-form-item>
        <el-form-item label="秒杀价(保留)" prop="seckillPrice">
          <el-input v-model="form.seckillPrice" placeholder="请输入秒杀价(保留)" />
        </el-form-item>
        <el-form-item label="秒杀库存(保留)" prop="seckillStock">
          <el-input v-model="form.seckillStock" placeholder="请输入秒杀库存(保留)" />
        </el-form-item>
        <el-form-item label="积分(保留)" prop="integral">
          <el-input v-model="form.integral" placeholder="请输入积分(保留)" />
        </el-form-item>
        <el-form-item label="是否删除 0否1是" prop="deleted">
          <el-input v-model="form.deleted" placeholder="请输入是否删除 0否1是" />
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
import { listDiyStoreProductAttrValue, getDiyStoreProductAttrValue, delDiyStoreProductAttrValue, addDiyStoreProductAttrValue, updateDiyStoreProductAttrValue } from "@/api/xms/diyStoreProductAttrValue";

export default {
  name: "DiyStoreProductAttrValue",
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
      // 商品属性值(SKU)表格数据
      diyStoreProductAttrValueList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        productId: null,
        sku: null,
        stock: null,
        sales: null,
        price: null,
        image: null,
        codeUnique: null,
        cost: null,
        barCode: null,
        weight: null,
        volume: null,
        brokerage: null,
        brokerageTwo: null,
        pinkPrice: null,
        pinkStock: null,
        seckillPrice: null,
        seckillStock: null,
        integral: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        productId: [
          { required: true, message: "商品ID不能为空", trigger: "blur" }
        ],
        sku: [
          { required: true, message: "商品属性索引值(attr_value|attr_value[|...])不能为空", trigger: "blur" }
        ],
        stock: [
          { required: true, message: "属性对应库存不能为空", trigger: "blur" }
        ],
        price: [
          { required: true, message: "销售价不能为空", trigger: "blur" }
        ],
        codeUnique: [
          { required: true, message: "唯一值(可放规则签名)不能为空", trigger: "blur" }
        ],
        cost: [
          { required: true, message: "成本价不能为空", trigger: "blur" }
        ],
        weight: [
          { required: true, message: "重量(可选)不能为空", trigger: "blur" }
        ],
        volume: [
          { required: true, message: "体积(可选)不能为空", trigger: "blur" }
        ],
        brokerage: [
          { required: true, message: "扩展字段1(保留)不能为空", trigger: "blur" }
        ],
        brokerageTwo: [
          { required: true, message: "扩展字段2(保留)不能为空", trigger: "blur" }
        ],
        pinkPrice: [
          { required: true, message: "拼团价(保留)不能为空", trigger: "blur" }
        ],
        pinkStock: [
          { required: true, message: "拼团库存(保留)不能为空", trigger: "blur" }
        ],
        seckillPrice: [
          { required: true, message: "秒杀价(保留)不能为空", trigger: "blur" }
        ],
        seckillStock: [
          { required: true, message: "秒杀库存(保留)不能为空", trigger: "blur" }
        ],
        deleted: [
          { required: true, message: "是否删除 0否1是不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询商品属性值(SKU)列表 */
    getList() {
      this.loading = true;
      listDiyStoreProductAttrValue(this.queryParams).then(response => {
        this.diyStoreProductAttrValueList = response.rows;
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
        productId: null,
        sku: null,
        stock: null,
        sales: null,
        price: null,
        image: null,
        codeUnique: null,
        cost: null,
        barCode: null,
        weight: null,
        volume: null,
        brokerage: null,
        brokerageTwo: null,
        pinkPrice: null,
        pinkStock: null,
        seckillPrice: null,
        seckillStock: null,
        integral: null,
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
      this.title = "添加商品属性值(SKU)";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getDiyStoreProductAttrValue(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改商品属性值(SKU)";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateDiyStoreProductAttrValue(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addDiyStoreProductAttrValue(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除商品属性值(SKU)编号为"' + ids + '"的数据项？').then(function() {
        return delDiyStoreProductAttrValue(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('xms/diyStoreProductAttrValue/export', {
        ...this.queryParams
      }, `diyStoreProductAttrValue_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
