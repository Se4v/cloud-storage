<template>
  <div class="p-6 max-w-[1600px] mx-auto space-y-6">
    <!-- 控制栏：左侧操作按钮，右侧搜索 -->
    <div class="flex flex-col lg:flex-row lg:items-center justify-between gap-4 bg-white rounded-xl border border-slate-200 shadow-sm p-4">
      <!-- 左侧：操作按钮 -->
      <div class="flex items-center gap-3">
        <button
            @click="handleBatchRestore"
            :disabled="selectedRows.length === 0 || loading"
            :class="[
            'inline-flex items-center justify-center gap-2 h-9 px-4 rounded-md text-sm font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2',
            selectedRows.length > 0
              ? 'bg-blue-600 hover:bg-blue-700 text-white'
              : 'bg-blue-100 text-blue-300 cursor-not-allowed'
          ]"
        >
          <el-icon :size="16"><RefreshLeft /></el-icon>
          恢复
        </button>

        <button
            @click="handleBatchDelete"
            :disabled="selectedRows.length === 0 || loading"
            :class="[
            'inline-flex items-center justify-center gap-2 h-9 px-4 rounded-md text-sm font-medium transition-colors border',
            selectedRows.length > 0
              ? 'border-red-200 text-red-600 hover:bg-red-50 bg-white'
              : 'border-slate-200 text-slate-400 cursor-not-allowed bg-slate-50'
          ]"
        >
          <el-icon :size="16"><Delete /></el-icon>
          彻底删除
        </button>
      </div>

      <!-- 右侧：搜索 -->
      <div class="flex flex-col sm:flex-row gap-3">
        <div class="relative w-full sm:w-64">
          <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <el-icon class="text-slate-400" :size="16"><Search /></el-icon>
          </div>
          <input
              v-model="searchQuery"
              type="text"
              placeholder="搜索删除者或文件名..."
              class="w-full h-9 pl-9 pr-4 rounded-md border border-slate-200 bg-white text-sm placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
              @keyup.enter="handleSearch"
          />
        </div>

        <div class="flex gap-2">
          <button
              @click="handleSearch"
              class="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-all duration-200 shadow-sm hover:shadow-md active:scale-95"
          >
            <el-icon :size="16"><Search /></el-icon>
            查询
          </button>
        </div>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <el-table
          v-loading="loading"
          :data="paginatedTableData"
          row-key="id"
          @selection-change="handleSelectionChange"
          class="w-full"
          header-cell-class-name="!bg-slate-50 !text-slate-700 !font-semibold !border-b !border-slate-200"
          row-class-name="hover:bg-slate-50/50 transition-colors"
      >
        <el-table-column type="selection" width="48" align="center" />

        <el-table-column label="删除者" min-width="140" align="left">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <span class="text-slate-900 font-medium">{{ row.deleter || '-' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="文件名" min-width="240" align="left">
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

        <el-table-column label="删除时间" min-width="160" align="left">
          <template #default="{ row }">
            <div class="flex items-center gap-1.5 text-sm text-slate-600">
              <span class="whitespace-nowrap">{{ row.deleteTime }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="大小" min-width="100" align="left">
          <template #default="{ row }">
            <span class="text-sm text-slate-600 tabular-nums">{{ formatFileSize(row.size) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="140" align="left">
          <template #default="{ row }">
            <div class="flex items-center gap-1">
              <button
                  class="inline-flex items-center justify-center w-8 h-8 rounded-md text-slate-600 hover:text-blue-600 hover:bg-blue-50 transition-colors"
                  @click="selectedRows = [row]; handleBatchRestore()"
                  title="恢复"
              >
                <el-icon :size="16"><RefreshLeft /></el-icon>
              </button>
              <button
                  class="inline-flex items-center justify-center w-8 h-8 rounded-md text-slate-600 hover:text-red-600 hover:bg-red-50 transition-colors"
                  @click="selectedRows = [row]; handleBatchDelete()"
                  title="彻底删除"
              >
                <el-icon :size="16"><Delete /></el-icon>
              </button>
            </div>
          </template>
        </el-table-column>

        <!-- 空状态 -->
        <template #empty>
          <div class="flex flex-col items-center justify-center text-slate-400 py-12">
            <el-icon :size="48" class="mb-2 opacity-50"><Delete /></el-icon>
            <p class="text-sm">暂无回收站数据</p>
          </div>
        </template>
      </el-table>

      <!-- 分页 -->
      <div class="px-6 py-4 border-t border-slate-200 bg-slate-50/50 flex items-center justify-between">
        <span class="text-sm text-slate-500">
          共 <span class="font-medium text-slate-900">{{ filteredTableData.length }}</span> 条记录
        </span>
        <el-pagination
            v-model:current-page="currentPage"
            :page-size="10"
            :total="total"
            layout="prev, pager, next"
            background
            class="!gap-2"
            @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import {
  Search,
  Delete,
  RefreshLeft,
  Document,
  Folder
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user.js';

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

const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref([])

const tableData = ref([
  {
    id: 123,
    deleter: 'asdsad',
  }
])
const loading = ref(false)

// 获取回收站列表
const loadRecycleList = async () => {
  loading.value = true
  try {
    const { data: res } = await axios.get(`${API_BASE_URL}/api/recycle/manage`, getAuthConfig())
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

// 处理表格选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 搜索（前端筛选）
const filteredTableData = computed(() => {
  let data = tableData.value

  if (searchQuery.value) {
    const keyword = searchQuery.value.toLowerCase()
    data = data.filter(item =>
        item.deleter?.toLowerCase().includes(keyword) ||
        item.name?.toLowerCase().includes(keyword)
    )
  }

  return data
})

// 分页后的数据
const paginatedTableData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredTableData.value.slice(start, end)
})

// 搜索
const handleSearch = () => {
  currentPage.value = 1
}

// 分页
const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 批量恢复
const handleBatchRestore = async () => {
  if (!selectedRows.value.length) return
  try {
    const selectedNames = selectedRows.value.map(row => row.name)
    const msg = `确定要恢复${selectedNames.length > 1 ? `选中的 ${selectedNames.length} 个文件` : `"${selectedNames[0]}"`}吗？`
    await ElMessageBox.confirm(msg, '确认恢复', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
      customClass: 'custom-message-box'
    })

    loading.value = true
    const { data: res } = await axios.post(`${API_BASE_URL}/api/recycle/manage/restore`, {
      ids: selectedRows.value.map(row => row.id)
    }, getAuthConfig())
    if (res.code !== 200) {
      ElMessage.error(res.msg || '恢复失败')
      return
    }
    ElMessage.success('恢复成功')
    await loadRecycleList()
    selectedRows.value = []
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error(error.message || '恢复失败')
    }
  } finally {
    loading.value = false
  }
}

// 批量彻底删除
const handleBatchDelete = async () => {
  if (!selectedRows.value.length) return
  try {
    const selectedNames = selectedRows.value.map(row => row.name)
    const msg = `确定要彻底删除${selectedNames.length > 1 ? `选中的${selectedNames.length}个文件` : `"${selectedNames[0]}"`}吗？此操作不可恢复！`
    await ElMessageBox.confirm(msg, '确认彻底删除', {
      confirmButtonText: '彻底删除',
      cancelButtonText: '取消',
      type: 'error',
      customClass: 'custom-message-box'
    })

    loading.value = true
    const { data: res } = await axios.post(`${API_BASE_URL}/api/recycle/manage/delete`, {
      ids: selectedRows.value.map(row => row.id)
    }, getAuthConfig())
    if (res.code !== 200) {
      ElMessage.error(res.msg || '删除失败')
      return
    }
    ElMessage.success('已彻底删除')
    await loadRecycleList()
    selectedRows.value = []
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error(error.message || '删除失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 分页器样式 */
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #2563eb;
  border-radius: 6px;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
  color: #2563eb;
}

:deep(.el-dropdown-menu__item) {
  font-size: 13px;
  padding: 8px 16px;
  line-height: 1.5;
}

:deep(.el-dropdown-menu__item:not(.is-disabled):hover) {
  background-color: rgb(241 245 249);
  color: rgb(15 23 42);
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
