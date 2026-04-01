<template>
  <div class="h-full flex flex-col bg-slate-50">
    <!-- 顶部工具栏 -->
    <div class="bg-white px-8 py-4 border-b border-slate-200">
      <div class="flex items-center justify-between">
        <!-- 左侧：删除按钮 -->
        <div class="flex items-center gap-3">
          <button
            @click="handleBatchDelete"
            :disabled="selectedMessages.length === 0"
            class="inline-flex items-center gap-2 px-4 py-2 bg-white text-slate-700 border border-slate-200 text-sm font-medium rounded-lg hover:bg-slate-50 hover:border-slate-300 hover:text-red-600 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <el-icon :size="16"><Delete /></el-icon>
            删除
          </button>
        </div>

        <!-- 右侧：搜索 -->
        <div class="flex items-center gap-3">
          <el-input
            v-model="searchQuery"
            placeholder="搜索消息标题或内容..."
            clearable
            class="w-64 !rounded-lg"
            @input="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon class="text-slate-400"><Search /></el-icon>
            </template>
          </el-input>
          <button
            @click="handleSearch"
            class="inline-flex items-center justify-center gap-2 h-9 px-4 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors whitespace-nowrap"
          >
            <el-icon :size="16"><Search /></el-icon>
            <span>搜索</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="flex-1 overflow-auto p-8">
      <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <!-- 表格 -->
        <el-table
          v-loading="loading"
          :data="paginatedMessageList"
          row-key="id"
          @selection-change="handleSelectionChange"
          class="message-table"
          header-cell-class-name="!bg-slate-50 !text-slate-700 !font-semibold !border-b !border-slate-200"
          :cell-style="{
            fontSize: '14px',
            color: '#334155',
            borderBottom: '1px solid #f1f5f9'
          }"
        >
          <el-table-column type="selection" width="48" align="center" />

          <!-- 标题列 -->
          <el-table-column label="标题" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="flex items-center gap-2">
                <span class="font-medium text-slate-900">{{ row.title }}</span>
                <span
                  v-if="!row.isRead"
                  class="w-2 h-2 bg-red-500 rounded-full flex-shrink-0"
                ></span>
              </div>
            </template>
          </el-table-column>

          <!-- 内容列 -->
          <el-table-column label="内容" min-width="400" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="text-slate-600">{{ row.content }}</span>
            </template>
          </el-table-column>

          <!-- 状态列 -->
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="row.isRead ? 'info' : 'danger'"
                size="small"
                class="!rounded-md !border-0"
                :class="row.isRead ? '!bg-slate-100 !text-slate-500' : '!bg-red-50 !text-red-600'"
              >
                {{ row.isRead ? '已读' : '未读' }}
              </el-tag>
            </template>
          </el-table-column>

          <!-- 通知时间列 -->
          <el-table-column label="通知时间" width="180" align="center">
            <template #default="{ row }">
              <span class="text-sm text-slate-600 whitespace-nowrap">{{ row.createTime }}</span>
            </template>
          </el-table-column>

          <!-- 操作列 -->
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <div class="flex items-center justify-center gap-1">
                <el-tooltip content="标记为已读" placement="top" :show-after="500">
                  <button
                    v-if="!row.isRead"
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:text-blue-600 hover:bg-blue-50 transition-colors"
                    @click.stop="markAsRead(row)"
                  >
                    <el-icon :size="16"><Check /></el-icon>
                  </button>
                </el-tooltip>

                <el-tooltip content="删除" placement="top" :show-after="500">
                  <button
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:text-red-600 hover:bg-red-50 transition-colors"
                    @click.stop="handleDelete(row)"
                  >
                    <el-icon :size="16"><Delete /></el-icon>
                  </button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>

          <!-- 空状态 -->
          <template #empty>
            <div class="py-12 flex flex-col items-center justify-center text-center">
              <div class="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center mb-4">
                <el-icon :size="32" class="text-slate-400"><Message /></el-icon>
              </div>
              <h3 class="text-sm font-medium text-slate-900 mb-1">
                {{ searchQuery ? '未找到匹配的消息' : '暂无消息' }}
              </h3>
              <p class="text-sm text-slate-500">
                {{ searchQuery ? '请尝试其他关键词搜索' : '您还没有收到任何通知消息' }}
              </p>
            </div>
          </template>
        </el-table>
      </div>
    </div>

    <!-- 底部状态栏 -->
    <div class="bg-white border-t border-slate-200 px-8 py-3 flex items-center justify-between text-sm text-slate-500">
      <div class="flex items-center gap-4">
        <span>共 <span class="font-medium text-slate-900">{{ total }}</span> 条消息</span>
        <span v-if="selectedMessages.length > 0" class="text-blue-600 font-medium">
          已选中 {{ selectedMessages.length }} 条
        </span>
        <span v-if="unreadCount > 0" class="text-red-600 font-medium">
          {{ unreadCount }} 条未读
        </span>
      </div>

      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="filteredMessageList.length"
        layout="prev, pager, next"
        background
        class="!gap-2"
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
  Delete
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
const pageSize = ref(10)
const total = ref(0)
const searchQuery = ref('')
const selectedMessages = ref([])

// 消息列表
const messageList = ref([])

// 过滤后的消息列表（仅搜索标题）
const filteredMessageList = computed(() => {
  if (!searchQuery.value.trim()) {
    return messageList.value
  }
  const keyword = searchQuery.value.toLowerCase()
  return messageList.value.filter(msg =>
    msg.title.toLowerCase().includes(keyword) ||
    msg.content.toLowerCase().includes(keyword)
  )
})

// 分页后的消息列表
const paginatedMessageList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredMessageList.value.slice(start, end)
})

// 未读消息数量
const unreadCount = computed(() => {
  return messageList.value.filter(msg => !msg.isRead).length
})

// 选择变化
const handleSelectionChange = (selection) => {
  selectedMessages.value = selection
}

// 加载消息列表
const loadMessageList = async () => {
  loading.value = true
  try {
    const response = await axios.get(`${API_BASE_URL}/api/notice`, getAuthConfig())
    if (response.data.code === 200) {
      messageList.value = response.data.data || []
      total.value = messageList.value.length
    } else {
      ElMessage.error('加载消息列表失败')
    }
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
}

// 标记单条消息为已读
const markAsRead = async (message) => {
  try {
    const res = await axios.post(`${API_BASE_URL}/api/notice/read`, { ids: [message.id] }, getAuthConfig())
    if (res.data.code === 200) {
      message.isRead = true
      ElMessage.success('已标记为已读')
    } else {
      ElMessage.error('标记已读失败')
    }
  } catch (error) {
    console.error('标记已读失败:', error)
    ElMessage.error('标记已读失败')
  }
}

// 删除单条消息
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除消息 "${row.title}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: '!bg-red-600 !border-red-600 !rounded-lg',
      cancelButtonClass: '!rounded-lg',
      customClass: '!rounded-xl'
    }
  ).then(async () => {
    try {
      const res = await axios.post(`${API_BASE_URL}/api/notice/delete`, { ids: [row.id] }, getAuthConfig())
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadMessageList()
      } else {
        ElMessage.error('删除失败')
      }
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedMessages.value.length === 0) {
    ElMessage.warning('请先选择要删除的消息')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedMessages.value.length} 条消息吗？`,
    '确认批量删除',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: '!bg-red-600 !border-red-600 !rounded-lg',
      cancelButtonClass: '!rounded-lg',
      customClass: '!rounded-xl'
    }
  ).then(async () => {
    try {
      const ids = selectedMessages.value.map(item => item.id)
      const res = await axios.post(`${API_BASE_URL}/api/notice/delete`, { ids }, getAuthConfig())
      if (res.data.code === 200) {
        ElMessage.success('批量删除成功')
        loadMessageList()
        selectedMessages.value = []
      } else {
        ElMessage.error('批量删除失败')
      }
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  })
}

// 分页
const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 页面加载
onMounted(() => {
  loadMessageList()
})
</script>

<style scoped>
/* 表格行高 */
:deep(.el-table__row) {
  height: 64px;
}

/* 分页器样式 */
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #2563eb;
  border-radius: 6px;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
  color: #2563eb;
}
</style>
