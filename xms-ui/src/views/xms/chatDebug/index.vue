<template>
  <div class="app-container chat-debug">
    <el-row :gutter="16">
      <el-col :xs="24" :md="7">
        <el-card shadow="never">
          <div slot="header">接口配置</div>
          <el-form label-position="top">
            <el-form-item label="接口地址">
              <el-input v-model="apiBase" placeholder="例如 http://localhost:18675" />
            </el-form-item>
            <el-form-item label="App登录Token">
              <el-input v-model="token" type="textarea" :rows="4" placeholder="会原样写入请求头 Authorization" />
            </el-form-item>
            <el-form-item label="会话ID">
              <el-input v-model="sessionId" placeholder="首次提问可留空" clearable />
            </el-form-item>
            <el-form-item label="调用方式">
              <el-radio-group v-model="chatMode">
                <el-radio-button label="http">HTTP版</el-radio-button>
                <el-radio-button label="hutool">Hutool版</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-button type="primary" plain icon="el-icon-refresh" :loading="sessionLoading" @click="loadSessions">加载会话</el-button>
          </el-form>
        </el-card>

        <el-card class="mt16" shadow="never">
          <div slot="header">会话列表</div>
          <el-table v-loading="sessionLoading" :data="sessions" size="mini" border @row-click="selectSession">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="标题" min-width="120" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="17">
        <el-card shadow="never">
          <div slot="header" class="chat-header">
            <span>聊天调试</span>
            <el-button size="mini" plain icon="el-icon-document" :disabled="!sessionId" @click="loadMessages">加载历史</el-button>
          </div>

          <div class="message-list">
            <div v-for="item in messages" :key="item.localId || item.id" :class="['message-item', item.role]">
              <div class="message-role">{{ item.role === 'user' ? '用户' : 'AI' }}</div>
              <div class="message-content">{{ item.content }}</div>
              <div v-if="item.imageUrlsText" class="message-images">{{ item.imageUrlsText }}</div>
            </div>
            <el-empty v-if="messages.length === 0" description="暂无消息" />
          </div>

          <el-form class="ask-form" label-position="top">
            <el-form-item label="图片URL，一行一个">
              <el-input v-model="imageUrlText" type="textarea" :rows="3" placeholder="https://..." />
            </el-form-item>
            <el-form-item label="问题">
              <el-input v-model="question" type="textarea" :rows="4" placeholder="输入问题后发送" @keydown.native.ctrl.enter.prevent="send" />
            </el-form-item>
            <el-button type="primary" icon="el-icon-s-promotion" :loading="sending" @click="send">发送</el-button>
            <el-button plain icon="el-icon-delete" @click="clearLocal">清空页面</el-button>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'
import { askChat, listChatMessages, listChatSessions } from '@/api/xms/chatDebug'

export default {
  name: 'ChatDebug',
  data() {
    return {
      apiBase: 'http://localhost:18675',
      token: getToken() || '',
      chatMode: 'http',
      sessionId: '',
      question: '',
      imageUrlText: '',
      sessions: [],
      messages: [],
      sending: false,
      sessionLoading: false
    }
  },
  methods: {
    loadSessions() {
      this.sessionLoading = true
      listChatSessions(this.apiBase, this.token, {}).then(res => {
        this.sessions = res.data || []
      }).finally(() => {
        this.sessionLoading = false
      })
    },
    loadMessages() {
      if (!this.sessionId) {
        this.$message.warning('请先输入或选择会话ID')
        return
      }
      listChatMessages(this.apiBase, this.token, { sessionId: this.sessionId }).then(res => {
        this.messages = (res.data || []).slice().reverse().map(this.formatMessage)
      })
    },
    selectSession(row) {
      this.sessionId = String(row.id)
      this.loadMessages()
    },
    send() {
      if (!this.question.trim()) {
        this.$message.warning('请输入问题')
        return
      }
      const imageUrls = this.imageUrlText.split('\n').map(item => item.trim()).filter(Boolean)
      const request = {
        sessionId: this.sessionId ? Number(this.sessionId) : null,
        question: this.question.trim(),
        imageUrls
      }
      this.messages.push({
        localId: Date.now(),
        role: 'user',
        content: request.question,
        imageUrlsText: imageUrls.join('\n')
      })
      this.sending = true
      askChat(this.apiBase, this.token, request, this.chatMode === 'hutool').then(res => {
        const data = res.data || {}
        if (res.code !== 200) {
          throw new Error(res.msg || '接口返回失败')
        }
        this.sessionId = String(data.sessionId || this.sessionId || '')
        this.messages.push({
          localId: Date.now() + 1,
          role: 'assistant',
          content: data.answer || ''
        })
        this.question = ''
        this.imageUrlText = ''
      }).catch(error => {
        this.messages.push({
          localId: Date.now() + 2,
          role: 'assistant',
          content: this.formatError(error)
        })
      }).finally(() => {
        this.sending = false
      })
    },
    clearLocal() {
      this.messages = []
      this.question = ''
      this.imageUrlText = ''
    },
    formatMessage(item) {
      return {
        ...item,
        imageUrlsText: this.formatImageUrls(item.imageUrls)
      }
    },
    formatImageUrls(imageUrls) {
      if (!imageUrls) {
        return ''
      }
      try {
        return JSON.parse(imageUrls).join('\n')
      } catch (e) {
        return imageUrls
      }
    },
    formatError(error) {
      const response = error && error.response
      if (response && response.data) {
        return '请求失败：' + (response.data.msg || JSON.stringify(response.data))
      }
      return '请求失败：' + (error.message || '未知错误')
    }
  }
}
</script>

<style scoped>
.chat-debug {
  background: #f6f7f9;
  min-height: calc(100vh - 84px);
}

.mt16 {
  margin-top: 16px;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.message-list {
  height: 430px;
  overflow-y: auto;
  padding: 12px;
  border: 1px solid #e4e7ed;
  background: #fff;
}

.message-item {
  max-width: 82%;
  margin-bottom: 12px;
  padding: 10px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fafafa;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-item.user {
  margin-left: auto;
  border-color: #b3d8ff;
  background: #ecf5ff;
}

.message-role {
  margin-bottom: 6px;
  color: #606266;
  font-size: 12px;
}

.message-content {
  color: #303133;
  line-height: 1.6;
}

.message-images {
  margin-top: 8px;
  color: #409eff;
  font-size: 12px;
}

.ask-form {
  margin-top: 16px;
}
</style>
