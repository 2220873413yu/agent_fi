<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>网体关系树形图</span>
          <div class="operation-box">
            <div class="search-box">
              <el-input
                v-model="queryParams.userId"
                clearable
                placeholder="请输入钱包地址"
                style="width: 200px"
                @keyup.enter.native="handleQuery"
              >
                <el-button slot="append" icon="el-icon-search" @click="handleQuery" />
              </el-input>
            </div>
            <el-button
              v-hasPermi="['xms:userinfo:export']"
              type="warning"
              plain
              icon="el-icon-download"
              size="mini"
              @click="handleExport"
            >
              导出
            </el-button>
          </div>
        </div>
      </template>

      <div class="tree-scroll-wrapper">
        <el-tree
          class="network-tree"
          :data="treeData"
          :default-expand-all="false"
          :expand-on-click-node="false"
          :load="loadNode"
          :props="defaultProps"
          lazy
          node-key="id"
          @node-click="handleNodeClick"
        >
          <span slot-scope="{ data }" class="custom-tree-node">
            <span class="node-info">
              <el-tag size="mini" type="info">备注: {{ data.remark }}</el-tag>
              <el-tag size="mini" type="info">钱包地址: {{ data.account }}</el-tag>
              <el-tag size="mini" type="info">用户ID: {{ data.userId }}</el-tag>
              <el-tag v-if="data.level" size="mini" type="warning">节点等级: {{ data.level }}</el-tag>
              <el-tag size="mini" type="warning">团队等级(真): {{ data.gameLevel }}</el-tag>
              <el-tag size="mini" type="warning">团队等级(后台): {{ data.adminGameLevel }}</el-tag>
              <el-tag size="mini" type="danger">直推人数: {{ data.subNum }}</el-tag>
              <el-tag size="mini" type="danger">团队人数: {{ data.umbrellaNum }}</el-tag>
              <el-tag size="mini" type="primary">团队节点数量: {{ data.nodeTeamPerformance }}</el-tag>
              <el-tag size="mini" type="success">团队节点支付: {{ data.umbrellaNodePerformance }}</el-tag>
              <el-tag size="mini" type="success">团队节点金额: {{ data.allUmbrellaNodePerformance }}</el-tag>
              <el-tag size="mini" type="success">自身托管: {{ data.performance }}</el-tag>
              <el-tag size="mini" type="success">团队托管: {{ data.umbrellaPerformance }}</el-tag>
              <el-tag size="mini" type="success">小区托管: {{ data.communityPerformance }}</el-tag>
            </span>
          </span>
        </el-tree>
      </div>
    </el-card>

    <!-- 用户详情弹窗 -->
    <el-dialog
      :visible.sync="dialogVisible"
      title="用户详情"
      width="520px"
    >
      <div v-if="currentNode">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="备注">
            {{ currentNode.remark }}
          </el-descriptions-item>
          <el-descriptions-item label="钱包地址">
            {{ currentNode.account }}
          </el-descriptions-item>
          <el-descriptions-item label="用户ID">
            {{ currentNode.userId }}
          </el-descriptions-item>
          <el-descriptions-item label="节点等级">
            {{ currentNode.level }}
          </el-descriptions-item>
          <el-descriptions-item label="团队等级(真)">
            {{ currentNode.gameLevel }}
          </el-descriptions-item>
          <el-descriptions-item label="团队等级(后台)">
            {{ currentNode.adminGameLevel }}
          </el-descriptions-item>
          <el-descriptions-item label="直推人数">
            {{ currentNode.subNum }}
          </el-descriptions-item>
          <el-descriptions-item label="团队人数">
            {{ currentNode.umbrellaNum }}
          </el-descriptions-item>
          <el-descriptions-item label="团队节点数量">
            {{ currentNode.nodeTeamPerformance }}
          </el-descriptions-item>
          <el-descriptions-item label="团队节点支付">
            {{ currentNode.umbrellaNodePerformance }}
          </el-descriptions-item>
          <el-descriptions-item label="团队节点金额">
            {{ currentNode.allUmbrellaNodePerformance }}
          </el-descriptions-item>
          <el-descriptions-item label="自身托管">
            {{ currentNode.performance }}
          </el-descriptions-item>
          <el-descriptions-item label="团队托管">
            {{ currentNode.umbrellaPerformance }}
          </el-descriptions-item>
          <el-descriptions-item label="小区托管">
            {{ currentNode.communityPerformance }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { queryNetBody1 } from '@/api/xms/userinfo'

export default {
  name: 'TreeDiagram',
  data() {
    return {
      queryParams: {
        userId: null
      },
      treeData: [],
      defaultProps: {
        children: 'children',
        label: 'label',
        isLeaf: (data) => {
          return Number(data.subNum || 0) <= 0
        }
      },
      dialogVisible: false,
      currentNode: null
    }
  },
  created() {
    this.init()
  },
  methods: {
    // 加载子节点
    async loadNode(node, resolve) {
      console.log('开始加载节点:', node.level, '节点数据:', node.data)
      if (node.level === 0) {
        // 根节点，直接使用已有数据
        console.log('根节点数据:', this.treeData)
        resolve(this.treeData)
      } else {
        // 子节点
        // 检查节点是否有直推用户
        if (node.data.subNum <= 0) {
          console.log('该节点没有直推用户，无需请求接口')
          resolve([])
          return
        }

        // 检查是否已有children数据缓存
        if (node.data._children && node.data._children.length > 0) {
          console.log('使用缓存的子节点数据:', node.data._children)
          resolve(node.data._children)
          return
        }

        try {
          console.log('开始请求子节点数据, ID:', node.data.account)
          const response = await queryNetBody1(node.data.account)
          console.log('后端返回的原始数据:', response)

          if (response.data && response.data.length > 0) {
            console.log('解析的节点数据:', response.data[0])
            // 只取子节点数组，不包括当前节点
            const children = response.data[0].children || []
            console.log('子节点数量:', children.length)
            console.log('子节点原始数据:', children)

            const transformedChildren = children.map(child => this.transformNodeData(child))
            console.log('转换后的子节点数据:', transformedChildren)

            // 缓存子节点数据到父节点
            node.data._children = transformedChildren

            resolve(transformedChildren)
          } else {
            console.log('未获取到子节点数据或数据为空')
            resolve([])
          }
        } catch (error) {
          console.error('获取子节点数据出错:', error)
          this.$message.error('获取子节点数据失败')
          resolve([])
        }
      }
    },

    // 转换单个节点数据
    transformNodeData(node) {
      console.log('转换节点原始数据:', node)
      const transformedNode = {
        id: node.id,
        name: node.name,
        remark: node.remark,
        account: node.account,
        userId: node.userId || node.id,
        level: node.level,
        gameLevel: node.gameLevel,
        adminGameLevel: node.adminGameLevel,
        subNum: node.subNum,
        umbrellaNum: node.umbrellaNum,
        nodeTeamPerformance: node.nodeTeamPerformance,
        umbrellaNodePerformance: node.umbrellaNodePerformance,
        allUmbrellaNodePerformance: node.allUmbrellaNodePerformance,
        performance: node.performance,
        umbrellaPerformance: node.umbrellaPerformance,
        communityPerformance: node.communityPerformance,
        _children: node.children ? node.children.map(child => this.transformNodeData(child)) : []
      }
      console.log('转换后节点数据:', transformedNode)
      return transformedNode
    },

    // 查询数据
    async handleQuery() {
      try {
        // 先清空现有数据
        this.treeData = []

        const response = await queryNetBody1(this.queryParams.userId)
        if (response.data && response.data.length > 0) {
          // 设置根节点数据
          this.treeData = [this.transformNodeData(response.data[0])]
        }
      } catch (error) {
        console.error('Error fetching network data:', error)
        // this.$message.error('获取网络数据失败');
        this.treeData = []
      }
    },

    // 导出当前查询用户对应的完整网体树数据
    handleExport() {
      this.download('xms/userinfo/netBodyExport', {
        userId: this.queryParams.userId
      }, `网体关系树_${new Date().getTime()}.xlsx`)
    },

    // 处理节点点击
    handleNodeClick(data) {
      this.currentNode = { ...data } // 创建数据副本
      this.dialogVisible = true
    },

    // 初始化
    async init() {
      if (this.$route.query.userId) {
        this.queryParams.userId = this.$route.query.userId
      }
      await this.handleQuery()
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;

  .box-card {
    margin-bottom: 20px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .operation-box {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .tree-scroll-wrapper {
    width: 100%;
    max-height: calc(100vh - 180px);
    overflow: auto;
    border: 1px solid #ebeef5;
    border-radius: 4px;
  }

  .custom-tree-node {
    display: inline-flex;
    align-items: center;
    flex: 0 0 auto;
    min-width: max-content;
    font-size: 14px;
    padding: 8px;
    white-space: nowrap;

    .node-info {
      display: inline-flex;
      align-items: center;
      flex-wrap: nowrap;
      gap: 6px;
      color: #666;
      font-size: 13px;
      white-space: nowrap;
    }
  }
}

::v-deep .network-tree {
  background: transparent;
  width: max-content;
  min-width: 100%;
  padding: 20px;

  .el-tree-node {
    min-width: max-content;
  }

  .el-tree-node__content {
    width: max-content;
    min-width: 100%;
    height: auto;
    padding: 4px 8px;
    border-radius: 4px;
    transition: all 0.3s;
    cursor: pointer;

    &:hover {
      background-color: #cceeff
    }

    .el-tree-node__expand-icon {
      padding: 6px;
      margin-right: 8px;

      &.expanded {
        transform: rotate(90deg);
      }

      &:not(.is-leaf) {
        color: #E65D6E;
        font-size: 14px;

        &:hover {
          background-color: rgba(24, 144, 255, 0.1);
          border-radius: 2px;
        }
      }
    }
  }

  .el-tree-node.is-current > .el-tree-node__content {
    background-color: #e6f7ff;
  }

  .el-tree-node__label {
    font-size: 14px;
  }
}
</style>
