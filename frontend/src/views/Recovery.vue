<template>
  <div class="h-full flex flex-col bg-slate-50">
    <!-- 顶部工具栏 - 白色背景 -->
    <div class="bg-white px-8 py-4 border-b border-slate-200 flex-shrink-0">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <button
              @click="handleBatchRestore"
              :disabled="selectedRows.length === 0 || loading"
              class="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors shadow-sm"
          >
            <el-icon :size="16"><RefreshLeft /></el-icon>
            还原
          </button>

          <button
              @click="handleBatchDelete"
              :disabled="selectedRows.length === 0 || loading"
              class="inline-flex items-center gap-2 px-4 py-2 border border-slate-300 bg-white text-slate-700 text-sm font-medium rounded-md hover:bg-slate-50 hover:text-red-600 hover:border-red-300 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <el-icon :size="16"><Delete /></el-icon>
            彻底删除
          </button>

          <button
              @click="handleClearAll"
              :disabled="tableData.length === 0 || loading"
              class="inline-flex items-center gap-2 px-4 py-2 border border-slate-300 bg-white text-slate-700 text-sm font-medium rounded-md hover:bg-slate-50 hover:text-red-600 hover:border-red-300 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <el-icon :size="16"><DeleteFilled /></el-icon>
            清空
          </button>
        </div>
      </div>
    </div>

    <!-- 内容区域 - 淡灰背景，与上下隔开 -->
    <div class="flex-1 overflow-auto p-6">
      <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <!-- 表格 -->
        <el-table
            ref="tableRef"
            v-loading="loading"
            :data="tableData"
            row-key="id"
            @selection-change="handleSelectionChange"
            class="recovery-table"
            :header-cell-style="{
            background: '#f8fafc',
            color: '#475569',
            fontWeight: 600,
            fontSize: '14px',
            height: '48px',
            borderBottom: '1px solid #e2e8f0'
          }"
            :cell-style="{
            fontSize: '14px',
            color: '#334155',
            borderBottom: '1px solid #f1f5f9'
          }"
        >
          <el-table-column
              type="selection"
              width="48"
              align="center"
          />

          <el-table-column
              prop="name"
              label="文件名"
              min-width="280"
          >
            <template #default="{ row }">
              <div class="flex items-center gap-3 py-1">
                <div class="w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0 text-white bg-slate-400">
                  <el-icon :size="20">
                    <component :is="getFileIcon(row.type)" />
                  </el-icon>
                </div>
                <div class="flex flex-col min-w-0">
                  <span class="text-sm font-medium text-slate-900 truncate">{{ row.name }}</span>
                  <span class="text-xs text-slate-400 truncate mt-0.5">{{ row.path }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column
              prop="deleteTime"
              label="删除时间"
              width="180"
              sortable
          >
            <template #default="{ row }">
              <div class="flex items-center gap-1.5 text-sm text-slate-600">
                <span class="whitespace-nowrap">{{ row.deleteTime }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column
              prop="expireTime"
              label="过期时间"
              width="180"
              sortable
          >
            <template #default="{ row }">
              <div class="flex items-center gap-1.5 text-sm text-slate-600">
                <span class="whitespace-nowrap">{{ row.expireTime }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column
              prop="size"
              label="大小"
              width="120"
              align="left"
              sortable
          >
            <template #default="{ row }">
              <span class="text-sm text-slate-600 tabular-nums">{{ formatFileSize(row.size) }}</span>
            </template>
          </el-table-column>

          <el-table-column
              label="操作"
              width="140"
              fixed="right"
              align="center"
          >
            <template #default="{ row }">
              <div class="flex items-center justify-center gap-1">
                <button
                    class="p-2 text-slate-500 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-all"
                    @click="handleRestore(row)"
                    title="还原"
                >
                  <el-icon :size="18"><RefreshLeft /></el-icon>
                </button>
                <button
                    class="p-2 text-slate-500 hover:text-red-600 hover:bg-red-50 rounded-md transition-all"
                    @click="handleDelete(row)"
                    title="彻底删除"
                >
                  <el-icon :size="18"><Delete /></el-icon>
                </button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 空状态 -->
        <div v-if="tableData.length === 0 && !loading" class="py-20 flex flex-col items-center justify-center">
          <div class="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center mb-4">
            <el-icon class="text-slate-400" :size="32"><Delete /></el-icon>
          </div>
          <h3 class="text-sm font-medium text-slate-900 mb-1">暂无文件</h3>
          <p class="text-sm text-slate-500">您还没有删除任何文件</p>
        </div>
      </div>
    </div>

    <!-- 底部状态栏 -->
    <div class="bg-white border-t border-slate-200 px-6 py-3 flex items-center justify-between text-sm text-slate-500">
      <div class="flex items-center gap-4">
        <span>共 {{ total }} 项数据</span>
        <span v-if="selectedRows.length > 0" class="text-blue-600 font-medium">
          已选中 {{ selectedRows.length }} 项
        </span>
      </div>

      <el-pagination
          v-model:current-page="currentPage"
          :page-size="10"
          :total="total"
          layout="total, prev, pager, next"
          background
          class="custom-pagination"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import {
  Document,
  Folder,
  Delete,
  DeleteFilled,
  RefreshLeft
} from '@element-plus/icons-vue'
import { useUserStore } from "@/stores/user.js";

const userStore = useUserStore()
const API_BASE_URL = 'http://localhost:8080'

// 获取请求配置（包含认证头）
const getAuthConfig = () => {
  const token = userStore.token
  return {
    headers: {
      'Authorization': token ? `Bearer ${token}` : '',
      'Content-Type': 'application/json'
    }
  }
}

const tableRef = ref()
const selectedRows = ref([])
const currentPage = ref(1)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)

// 获取回收站列表
const loadRecycleList = async () => {
  loading.value = true
  try {
    const { data: res } = await axios.get(`${API_BASE_URL}/api/recycle`, getAuthConfig())
    if (res.code !== 200) {
      ElMessage.error(res.msg || '获取回收站列表失败')
      return
    }
    tableData.value = res.data || []
    total.value = tableData.value.length
  } catch (error) {
    ElMessage.error(error.message || '获取回收站列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadRecycleList()
})

// 格式化文件大小
const formatFileSize = (size) => {
  if (!size || size === '0' || size === 'null') return '-'
  const numSize = parseInt(size)
  if (isNaN(numSize)) return '-'
  if (numSize === 0) return '-'
  if (numSize < 1024) return numSize + ' B'
  if (numSize < 1024 * 1024) return (numSize / 1024).toFixed(1) + ' KB'
  if (numSize < 1024 * 1024 * 1024) return (numSize / 1024 / 1024).toFixed(1) + ' MB'
  return (numSize / 1024 / 1024 / 1024).toFixed(2) + ' GB'
}

// 获取文件图标
const getFileIcon = (type) => {
  return type === 2 ? Folder : Document
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 还原单个
const handleRestore = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要还原文件 "${row.name}" 吗？`,
      '确认还原',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info',
        customClass: 'custom-message-box'
      }
    )

    loading.value = true
    const restoreData = {
      ids: [row.id]
    }
    const { data: res } = await axios.post(`${API_BASE_URL}/api/recycle/restore`, restoreData, getAuthConfig())
    if (res.code !== 200) {
      ElMessage.error(res.msg || '还原失败')
      return
    }
    ElMessage.success('还原成功')
    await loadRecycleList()
    selectedRows.value = []
  } catch (error) {
    ElMessage.error(error.message || '还原失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 彻底删除单个
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定要彻底删除 "${row.name}" 吗？此操作不可恢复！`,
        '确认彻底删除',
        {
          confirmButtonText: '彻底删除',
          cancelButtonText: '取消',
          type: 'error',
          customClass: 'custom-message-box'
        }
    )

    loading.value = true
    const deleteData = {
      ids: [row.id]
    }
    const response = await axios.post(`${API_BASE_URL}/api/recycle/delete`, deleteData, getAuthConfig())
    const { code, msg } = response.data
    if (code === 200) {
      ElMessage.success('已彻底删除')
      await loadRecycleList()
      selectedRows.value = []
    } else {
      ElMessage.error(msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || error.message || '删除失败'
      ElMessage.error(errorMsg)
      console.error(error)
    }
  } finally {
    loading.value = false
  }
}

// 批量还原
const handleBatchRestore = async () => {
  if (selectedRows.value.length === 0) return

  const isMulti = selectedRows.value.length > 1

  try {
    await ElMessageBox.confirm(
        isMulti
            ? `确定要还原选中的 ${selectedRows.value.length} 个文件吗？`
            : `确定要还原 "${selectedRows.value[0].name}" 吗？`,
        '确认还原',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info',
          customClass: 'custom-message-box'
        }
    )

    loading.value = true
    const ids = selectedRows.value.map(row => row.id)
    const restoreData = {
      ids: ids
    }
    const response = await axios.post(`${API_BASE_URL}/api/recycle/restore`, restoreData, getAuthConfig())
    const { code, msg } = response.data
    if (code === 200) {
      ElMessage.success('还原成功')
      await loadRecycleList()
      selectedRows.value = []
      tableRef.value?.clearSelection()
    } else {
      ElMessage.error(msg || '还原失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || error.message || '还原失败'
      ElMessage.error(errorMsg)
      console.error(error)
    }
  } finally {
    loading.value = false
  }
}

// 批量彻底删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return

  const isMulti = selectedRows.value.length > 1

  try {
    await ElMessageBox.confirm(
        isMulti
            ? `确定要彻底删除选中的 ${selectedRows.value.length} 个文件吗？此操作不可恢复！`
            : `确定要彻底删除 "${selectedRows.value[0].name}" 吗？此操作不可恢复！`,
        '确认彻底删除',
        {
          confirmButtonText: '彻底删除',
          cancelButtonText: '取消',
          type: 'error',
          customClass: 'custom-message-box'
        }
    )

    loading.value = true
    const ids = selectedRows.value.map(row => row.id)
    const deleteData = {
      ids: ids
    }
    const response = await axios.post(`${API_BASE_URL}/api/recycle/delete`, deleteData, getAuthConfig())
    const { code, msg } = response.data
    if (code === 200) {
      ElMessage.success('已彻底删除')
      await loadRecycleList()
      selectedRows.value = []
      tableRef.value?.clearSelection()
    } else {
      ElMessage.error(msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || error.message || '删除失败'
      ElMessage.error(errorMsg)
      console.error(error)
    }
  } finally {
    loading.value = false
  }
}

// 清空所有
const handleClearAll = async () => {
  if (tableData.value.length === 0) {
    ElMessage.info('回收站已是空的')
    return
  }

  try {
    await ElMessageBox.confirm(
        '确定要清空回收站吗？所有文件将被彻底删除且不可恢复！',
        '确认清空回收站',
        {
          confirmButtonText: '清空回收站',
          cancelButtonText: '取消',
          type: 'error',
          customClass: 'custom-message-box'
        }
    )

    loading.value = true
    const response = await axios.post(`${API_BASE_URL}/api/recycle/clear`, {}, getAuthConfig())
    const { code, msg } = response.data
    if (code === 200) {
      ElMessage.success('回收站已清空')
      await loadRecycleList()
      selectedRows.value = []
      tableRef.value?.clearSelection()
    } else {
      ElMessage.error(msg || '清空失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || error.message || '清空失败'
      ElMessage.error(errorMsg)
      console.error(error)
    }
  } finally {
    loading.value = false
  }
}

</script>

<style scoped>
/* 表格样式优化 - 参考安全外链页面 */
:deep(.recovery-table) {
  --el-table-header-bg-color: #f8fafc;
  --el-table-header-text-color: #475569;
  --el-table-row-hover-bg-color: #f1f5f9;
  --el-table-border-color: transparent;
  --el-table-text-color: #334155;
}

:deep(.recovery-table .el-table__header th) {
  font-weight: 600;
  font-size: 0.875rem;
  height: 48px;
  border-bottom: 1px solid #e2e8f0;
  background-color: #f8fafc;
}

:deep(.recovery-table .el-table__row) {
  transition: all 0.2s ease;
}

:deep(.recovery-table .el-table__row td) {
  border-bottom: 1px solid #f1f5f9;
  padding: 12px 0;
  vertical-align: middle;
}

:deep(.recovery-table .el-table__row:hover td) {
  background-color: #f8fafc;
}

:deep(.recovery-table .el-checkbox__inner) {
  border-radius: 4px;
  border-color: #cbd5e1;
}

:deep(.recovery-table .el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #2563eb;
  border-color: #2563eb;
}

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

/* 消息框样式 */
:deep(.custom-message-box) {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1);
}

:deep(.custom-message-box .el-message-box__header) {
  padding: 20px 20px 0;
}

:deep(.custom-message-box .el-message-box__title) {
  font-weight: 600;
  color: #0f172a;
  font-size: 16px;
}

:deep(.custom-message-box .el-message-box__content) {
  padding: 20px;
  color: #475569;
}

:deep(.custom-message-box .el-message-box__btns) {
  padding: 0 20px 20px;
}

:deep(.custom-message-box .el-button) {
  border-radius: 6px;
  padding: 8px 16px;
  font-weight: 500;
  transition: all 0.2s;
}

:deep(.custom-message-box .el-button--default) {
  border-color: #e2e8f0;
  color: #475569;
}

:deep(.custom-message-box .el-button--default:hover) {
  border-color: #cbd5e1;
  color: #0f172a;
  background: #f8fafc;
}

:deep(.custom-message-box .el-button--primary) {
  background: #2563eb;
  border-color: #2563eb;
}

:deep(.custom-message-box .el-button--primary:hover) {
  background: #1d4ed8;
  border-color: #1d4ed8;
}
</style>