<template>
  <div class="h-full flex flex-col bg-slate-50">
    <!-- 顶部工具栏 -->
    <div class="bg-white px-8 py-4 border-b border-slate-200">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="relative">
            <el-icon class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" :size="16">
              <Search />
            </el-icon>
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索消息..."
              class="w-72 pl-10 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all"
              @keyup.enter="handleSearch"
            />
          </div>
        </div>
        <div class="flex items-center gap-2">
          <button
            @click="markAllAsRead"
            :disabled="unreadCount === 0"
            class="inline-flex items-center gap-2 px-4 py-2 bg-white text-slate-700 border border-slate-200 text-sm font-medium rounded-lg hover:bg-slate-50 hover:border-slate-300 active:scale-95 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <el-icon><Check /></el-icon>
            全部已读
          </button>
          <button
            @click="clearAllMessages"
            :disabled="messageList.length === 0"
            class="inline-flex items-center gap-2 px-4 py-2 bg-white text-slate-700 border border-slate-200 text-sm font-medium rounded-lg hover:bg-slate-50 hover:border-slate-300 hover:text-red-600 active:scale-95 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <el-icon><Delete /></el-icon>
            清空消息
          </button>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="flex-1 overflow-auto p-8">
      <div class="max-w-4xl mx-auto">
        <!-- 消息列表 -->
        <div v-if="filteredMessageList.length > 0" class="space-y-3">
          <div
            v-for="message in filteredMessageList"
            :key="message.id"
            @click="handleMessageClick(message)"
            class="group bg-white rounded-lg border border-slate-200 p-4 hover:border-slate-300 hover:shadow-sm cursor-pointer transition-all"
            :class="{ 'bg-slate-50/80': !message.isRead }"
          >
            <div class="flex items-start gap-4">
              <!-- 消息图标 -->
              <div
                class="w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0"
                :class="getMessageIconClass(message.type)"
              >
                <el-icon :size="18">
                  <component :is="getMessageIcon(message.type)" />
                </el-icon>
              </div>

              <!-- 消息内容 -->
              <div class="flex-1 min-w-0">
                <div class="flex items-start justify-between gap-4">
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center gap-2 mb-1">
                      <h3 class="text-base font-semibold text-slate-900 truncate">
                        {{ message.title }}
                      </h3>
                      <span
                        v-if="!message.isRead"
                        class="w-2 h-2 bg-red-500 rounded-full flex-shrink-0"
                      ></span>
                    </div>
                    <p class="text-sm text-slate-600 leading-relaxed line-clamp-2">
                      {{ message.content }}
                    </p>
                  </div>
                  <span class="text-xs text-slate-400 flex-shrink-0 whitespace-nowrap">
                    {{ message.createTime }}
                  </span>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                <el-tooltip content="标记为已读" placement="top">
                  <button
                    v-if="!message.isRead"
                    @click.stop="markAsRead(message)"
                    class="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-all"
                  >
                    <el-icon :size="16"><Check /></el-icon>
                  </button>
                </el-tooltip>
                <el-tooltip content="删除" placement="top">
                  <button
                    @click.stop="deleteMessage(message)"
                    class="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-md transition-all"
                  >
                    <el-icon :size="16"><Delete /></el-icon>
                  </button>
                </el-tooltip>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else-if="!loading" class="py-20 flex flex-col items-center justify-center">
          <div class="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center mb-4">
            <el-icon class="text-slate-400" :size="32"><Message /></el-icon>
          </div>
          <h3 class="text-base font-medium text-slate-900 mb-1">
            {{ searchKeyword ? '未找到匹配的消息' : '暂无消息' }}
          </h3>
          <p class="text-sm text-slate-500">
            {{ searchKeyword ? '请尝试其他关键词搜索' : '您还没有收到任何通知消息' }}
          </p>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="py-12 flex flex-col items-center justify-center">
          <el-icon class="text-slate-400 animate-spin" :size="32"><Loading /></el-icon>
          <p class="text-sm text-slate-500 mt-4">加载中...</p>
        </div>
      </div>
    </div>

    <!-- 底部状态栏 -->
    <div class="bg-white border-t border-slate-200 px-8 py-3 flex items-center justify-between text-sm text-slate-500">
      <div class="flex items-center gap-4">
        <span>共 {{ total }} 条消息</span>
        <span v-if="unreadCount > 0" class="text-red-600 font-medium">
          {{ unreadCount }} 条未读
        </span>
      </div>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="prev, pager, next, sizes"
        background
        class="custom-pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Message,
  Search,
  Check,
  Delete,
  InfoFilled,
  WarningFilled,
  SuccessFilled,
  Loading
} from '@element-plus/icons-vue'
import axios from 'axios'
import { useUserStore } from '@/stores/user.js'

const userStore = useUserStore()

// API 基础配置
const API_BASE_URL = 'http://localhost:8080'

// 获取请求配置
const getAuthConfig = () => {
  const token = userStore.token
  return {
    headers: {
      'Authorization': token ? `Bearer ${token}` : '',
      'Content-Type': 'application/json'
    }
  }
}

// 状态
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const searchKeyword = ref('')

// 消息列表
const messageList = ref([
  // 示例数据
  {
    id: 1,
    title: '新周报',
    content: '你收到了 14 份新周报，请及时查看并处理',
    type: 1,
    isRead: false,
    createTime: '2024-01-15 10:30'
  },
  {
    id: 2,
    title: '面试进度通知',
    content: '你推荐的候选人 曲妮妮 已通过第三轮面试，进入录用审批流程',
    type: 1,
    isRead: false,
    createTime: '2024-01-15 09:15'
  },
  {
    id: 3,
    title: '系统功能更新',
    content: '这种模板可以区分多种通知类型，帮助你更好地管理不同类型的消息',
    type: 2,
    isRead: true,
    createTime: '2024-01-14 16:45'
  },
  {
    id: 4,
    title: '功能使用提示',
    content: '左侧图标用于区分不同的类型，方便你快速识别消息的重要性',
    type: 3,
    isRead: true,
    createTime: '2024-01-14 14:20'
  },
  {
    id: 5,
    title: '显示优化说明',
    content: '内容不要超过两行字，超出时自动截断，点击消息可查看完整内容',
    type: 1,
    isRead: true,
    createTime: '2024-01-13 11:00'
  }
])

// 过滤后的消息列表
const filteredMessageList = computed(() => {
  if (!searchKeyword.value.trim()) {
    return messageList.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return messageList.value.filter(msg => 
    msg.title.toLowerCase().includes(keyword) ||
    msg.content.toLowerCase().includes(keyword)
  )
})

// 未读消息数量
const unreadCount = computed(() => {
  return messageList.value.filter(msg => !msg.isRead).length
})

// 获取消息图标
const getMessageIcon = (type) => {
  const iconMap = {
    1: InfoFilled,
    2: WarningFilled,
    3: SuccessFilled
  }
  return iconMap[type] || InfoFilled
}

// 获取消息图标样式
const getMessageIconClass = (type) => {
  const classMap = {
    1: 'bg-red-100 text-red-500',
    2: 'bg-cyan-100 text-cyan-500',
    3: 'bg-yellow-100 text-yellow-500'
  }
  return classMap[type] || 'bg-slate-100 text-slate-600'
}

// 加载消息列表
const loadMessageList = async () => {
  loading.value = true
  try {
    // TODO: 接入真实API
    // const response = await axios.get(`${API_BASE_URL}/api/messages`, getAuthConfig())
    // if (response.data.code === 200) {
    //   messageList.value = response.data.data || []
    //   total.value = response.data.total || 0
    // }
    
    // 模拟数据
    total.value = messageList.value.length
  } catch (error) {
    console.error('加载消息列表失败:', error)
    ElMessage.error('加载消息列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadMessageList()
}

// 标记单条消息为已读
const markAsRead = (message) => {
  message.isRead = true
  ElMessage.success('已标记为已读')
}

// 标记所有消息为已读
const markAllAsRead = () => {
  ElMessageBox.confirm(
    '确定要将所有消息标记为已读吗？',
    '确认操作',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    messageList.value.forEach(msg => {
      msg.isRead = true
    })
    ElMessage.success('已全部标记为已读')
  })
}

// 删除消息
const deleteMessage = (message) => {
  ElMessageBox.confirm(
    `确定要删除消息 "${message.title}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: '!bg-red-600 !border-red-600 hover:!bg-red-700'
    }
  ).then(() => {
    const index = messageList.value.findIndex(item => item.id === message.id)
    if (index > -1) {
      messageList.value.splice(index, 1)
      total.value--
      ElMessage.success('删除成功')
    }
  })
}

// 清空所有消息
const clearAllMessages = () => {
  ElMessageBox.confirm(
    '确定要清空所有消息吗？此操作不可恢复。',
    '确认清空',
    {
      confirmButtonText: '清空',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: '!bg-red-600 !border-red-600 hover:!bg-red-700'
    }
  ).then(() => {
    messageList.value = []
    total.value = 0
    ElMessage.success('已清空所有消息')
  })
}

// 处理消息点击
const handleMessageClick = (message) => {
  if (!message.isRead) {
    message.isRead = true
  }
  // TODO: 可以跳转到消息详情或执行其他操作
  console.log('查看消息详情:', message)
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  loadMessageList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadMessageList()
}

// 页面加载
onMounted(() => {
  loadMessageList()
})
</script>

<style scoped>
/* 分页样式优化 */
:deep(.custom-pagination) {
  --el-pagination-hover-color: #2563eb;
}

:deep(.custom-pagination .el-pagination__sizes .el-input .el-input__inner) {
  border-radius: 6px;
}

:deep(.custom-pagination .el-pager li) {
  border-radius: 6px;
  font-weight: 500;
}

:deep(.custom-pagination .el-pager li.is-active) {
  background-color: #2563eb;
  color: white;
}

/* 行限制样式 */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
