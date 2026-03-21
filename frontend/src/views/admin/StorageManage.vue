<template>
  <div class="p-6 max-w-[1600px] mx-auto space-y-6">
    <!-- 控制栏：左侧批量操作按钮，右侧搜索 -->
    <div class="flex flex-col lg:flex-row lg:items-center justify-between gap-4 bg-white rounded-xl border border-slate-200 shadow-sm p-4">
      <!-- 左侧：批量操作按钮 -->
      <div class="flex items-center gap-3">
        <button
          @click="handleBatchEnable"
          :disabled="selectedStorages.length === 0"
          :class="[
            'inline-flex items-center justify-center gap-2 h-9 px-4 rounded-md text-sm font-medium transition-colors border',
            selectedStorages.length > 0
              ? 'border-emerald-200 text-emerald-700 hover:bg-emerald-50 bg-white'
              : 'border-slate-200 text-slate-400 cursor-not-allowed bg-slate-50'
          ]"
        >
          <el-icon :size="16"><Check /></el-icon>
          批量启用
        </button>

        <button
          @click="handleBatchDisable"
          :disabled="selectedStorages.length === 0"
          :class="[
            'inline-flex items-center justify-center gap-2 h-9 px-4 rounded-md text-sm font-medium transition-colors border',
            selectedStorages.length > 0
              ? 'border-amber-200 text-amber-700 hover:bg-amber-50 bg-white'
              : 'border-slate-200 text-slate-400 cursor-not-allowed bg-slate-50'
          ]"
        >
          <el-icon :size="16"><CircleClose /></el-icon>
          批量禁用
        </button>
      </div>

      <!-- 右侧：搜索筛选 -->
      <div class="flex flex-col sm:flex-row gap-3">
        <div class="relative w-full sm:w-64">
          <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <el-icon class="text-slate-400" :size="16">
              <Search />
            </el-icon>
          </div>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索文件名称..."
            class="w-full h-9 pl-9 pr-4 rounded-md border border-slate-200 bg-white text-sm placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
            @keyup.enter="handleSearch"
          />
        </div>

        <div class="flex gap-2">
          <select
            v-model="filterStatus"
            class="h-9 px-3 rounded-md border border-slate-200 bg-white text-sm text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent cursor-pointer"
          >
            <option value="">全部状态</option>
            <option value="true">正常</option>
            <option value="false">禁用</option>
          </select>

          <button
            @click="handleSearch"
            class="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-all duration-200 shadow-sm hover:shadow-md active:scale-95"
          >
            <el-icon :size="16"><Search /></el-icon>
            查询
          </button>
          <button
            @click="handleReset"
            class="h-9 px-4 border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 rounded-md text-sm font-medium transition-colors"
          >
            重置
          </button>
        </div>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-sm text-left">
          <thead class="bg-slate-50 border-b border-slate-200">
            <tr>
              <th class="px-6 py-3.5 font-semibold text-slate-700 w-10">
                <input
                  type="checkbox"
                  :checked="isAllSelected"
                  @change="toggleSelectAll"
                  class="rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                />
              </th>
              <th class="px-6 py-3.5 font-semibold text-slate-700">名称</th>
              <th class="px-6 py-3.5 font-semibold text-slate-700">上传时间</th>
              <th class="px-6 py-3.5 font-semibold text-slate-700">大小</th>
              <th class="px-6 py-3.5 font-semibold text-slate-700">状态</th>
              <th class="px-6 py-3.5 font-semibold text-slate-700">引用计数</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr
              v-for="item in tableData"
              :key="item.id"
              class="hover:bg-slate-50/50 transition-colors"
            >
              <td class="px-6 py-4" @click.stop>
                <input
                  type="checkbox"
                  v-model="selectedStorages"
                  :value="item.id"
                  class="rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                />
              </td>
              <td class="px-6 py-4">
                <span class="font-medium text-slate-900">{{ item.name }}</span>
              </td>
              <td class="px-6 py-4">
                <span class="text-slate-700">{{ item.uploadTime }}</span>
              </td>
              <td class="px-6 py-4">
                <span class="text-slate-700 font-medium">{{ formatSize(item.size) }}</span>
              </td>
              <td class="px-6 py-4">
                <span
                  :class="[
                    'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border',
                    item.isEnabled
                      ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                      : 'bg-red-50 text-red-700 border-red-200'
                  ]"
                >
                  <span
                    :class="[
                      'w-1.5 h-1.5 rounded-full mr-1.5',
                      item.isEnabled ? 'bg-emerald-500' : 'bg-red-500'
                    ]"
                  ></span>
                  {{ item.isEnabled ? '正常' : '禁用' }}
                </span>
              </td>
              <td class="px-6 py-4">
                <div class="flex items-center gap-2">
                  <span class="text-slate-700 font-medium">{{ item.refCount }}</span>
                  <el-tooltip v-if="item.refCount > 0" content="该文件被引用，删除可能影响其他用户" placement="top">
                    <el-icon class="text-amber-500 cursor-help" :size="14"><Warning /></el-icon>
                  </el-tooltip>
                </div>
              </td>
            </tr>

            <!-- 空状态 -->
            <tr v-if="tableData.length === 0">
              <td colspan="6" class="px-6 py-12 text-center">
                <div class="flex flex-col items-center justify-center text-slate-400">
                  <div class="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center mb-4">
                    <el-icon :size="32" class="text-slate-400"><Document /></el-icon>
                  </div>
                  <h3 class="text-sm font-medium text-slate-900 mb-1">暂无存储数据</h3>
                  <p class="text-sm text-slate-500">没有找到符合条件的文件</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="px-6 py-4 border-t border-slate-200 bg-slate-50/50 flex items-center justify-between">
        <span class="text-sm text-slate-500">
          共 <span class="font-medium text-slate-900">{{ total }}</span> 条记录
        </span>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="prev, pager, next, sizes"
          background
          class="!gap-2"
          @size-change="handleSizeChange"
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
  Document,
  Warning,
  Check,
  CircleClose
} from '@element-plus/icons-vue'

const API_BASE_URL = 'http://localhost:8080'

// 获取认证配置
const getAuthConfig = () => {
  const token = localStorage.getItem('token')
  return {
    headers: {
      'Authorization': token ? `Bearer ${token}` : '',
      'Content-Type': 'application/json'
    }
  }
}

// 搜索与筛选
const searchQuery = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const selectedStorages = ref([])

// 表格数据
const tableData = ref([])

// 加载存储列表
const loadStorageList = async () => {
  try {
    const res = await axios.get(`${API_BASE_URL}/api/storage/all`, getAuthConfig())
    if (res.data.code === 200) {
      tableData.value = res.data.data || []
      total.value = tableData.value.length
    } else {
      ElMessage.error(res.data.msg || '获取存储列表失败')
    }
  } catch (error) {
    console.error('获取存储列表失败:', error)
    ElMessage.error(error.response?.data?.msg || '获取存储列表失败')
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadStorageList()
})

// 全选逻辑
const isAllSelected = computed(() => {
  return tableData.value.length > 0 && selectedStorages.value.length === tableData.value.length
})

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedStorages.value = []
  } else {
    selectedStorages.value = tableData.value.map(item => item.id)
  }
}

// 格式化文件大小
const formatSize = (bytes) => {
  if (bytes === 0) return '0 B'
  if (!bytes) return '-'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  // TODO: 调用API进行搜索
  ElMessage.success('搜索: ' + (searchQuery.value || '全部'))
}

const handleReset = () => {
  searchQuery.value = ''
  filterStatus.value = ''
  currentPage.value = 1
  handleSearch()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  handleSearch()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  handleSearch()
}

// 批量更新存储项状态
const updateStorageItemsStatus = async (ids, isEnabled) => {
  try {
    const submitData = {
      storageIds: ids,
      isEnabled: isEnabled
    }
    const res = await axios.post(`${API_BASE_URL}/api/storage/update`, submitData, getAuthConfig())
    if (res.data.code === 200) {
      ElMessage.success(isEnabled ? '批量启用成功' : '批量禁用成功')
      await loadStorageList()
    } else {
      ElMessage.error(res.data.msg || (isEnabled ? '批量启用失败' : '批量禁用失败'))
    }
  } catch (error) {
    console.error(isEnabled ? '批量启用失败:' : '批量禁用失败:', error)
    ElMessage.error(error.response?.data?.msg || (isEnabled ? '批量启用失败' : '批量禁用失败'))
  }
}

// 批量启用
const handleBatchEnable = async () => {
  if (selectedStorages.value.length === 0) return
  try {
    await ElMessageBox.confirm(
      `确定要启用选中的 ${selectedStorages.value.length} 个文件吗？`,
      '批量启用',
      {
        confirmButtonText: '启用',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    await updateStorageItemsStatus(selectedStorages.value, true)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量启用失败:', error)
    }
  }
}

// 批量禁用
const handleBatchDisable = async () => {
  if (selectedStorages.value.length === 0) return
  try {
    await ElMessageBox.confirm(
      `确定要禁用选中的 ${selectedStorages.value.length} 个文件吗？`,
      '批量禁用',
      {
        confirmButtonText: '禁用',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await updateStorageItemsStatus(selectedStorages.value, false)
    selectedStorages.value = []
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量禁用失败:', error)
    }
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

/* 复选框样式优化 */
input[type="checkbox"] {
  cursor: pointer;
}

/* 平滑滚动 */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgb(203 213 225);
  border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgb(148 163 184);
}
</style>
