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
            <option value="active">正常</option>
            <option value="disabled">禁用</option>
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
                <div class="flex items-center gap-3">
                  <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center text-white shadow-sm">
                    <el-icon :size="18"><Document /></el-icon>
                  </div>
                  <span class="font-medium text-slate-900">{{ item.name }}</span>
                </div>
              </td>
              <td class="px-6 py-4">
                <span class="text-slate-700">{{ formatDate(item.uploadTime) }}</span>
              </td>
              <td class="px-6 py-4">
                <span class="text-slate-700 font-medium">{{ formatSize(item.size) }}</span>
              </td>
              <td class="px-6 py-4">
                <span
                  :class="[
                    'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border',
                    item.status === 'active'
                      ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                      : 'bg-red-50 text-red-700 border-red-200'
                  ]"
                >
                  <span
                    :class="[
                      'w-1.5 h-1.5 rounded-full mr-1.5',
                      item.status === 'active' ? 'bg-emerald-500' : 'bg-red-500'
                    ]"
                  ></span>
                  {{ item.status === 'active' ? '正常' : '禁用' }}
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
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Document,
  Warning,
  Check,
  CircleClose
} from '@element-plus/icons-vue'

// 搜索与筛选
const searchQuery = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const selectedStorages = ref([])

// 模拟表格数据
const tableData = ref([
  {
    id: 1,
    name: '项目文档.pdf',
    type: 'PDF 文档',
    uploadTime: '2024-03-10 14:30:00',
    size: 15728640, // 15MB
    status: 'active',
    refCount: 5
  },
  {
    id: 2,
    name: '产品原型图.fig',
    type: 'Figma 文件',
    uploadTime: '2024-03-09 10:15:00',
    size: 52428800, // 50MB
    status: 'active',
    refCount: 3
  },
  {
    id: 3,
    name: '年度报表.xlsx',
    type: 'Excel 表格',
    uploadTime: '2024-03-08 16:45:00',
    size: 2097152, // 2MB
    status: 'disabled',
    refCount: 0
  },
  {
    id: 4,
    name: '团队合影.jpg',
    type: 'JPEG 图片',
    uploadTime: '2024-03-07 09:20:00',
    size: 8388608, // 8MB
    status: 'active',
    refCount: 12
  },
  {
    id: 5,
    name: '演示视频.mp4',
    type: 'MP4 视频',
    uploadTime: '2024-03-06 11:00:00',
    size: 536870912, // 512MB
    status: 'active',
    refCount: 2
  },
  {
    id: 6,
    name: '源代码.zip',
    type: 'ZIP 压缩包',
    uploadTime: '2024-03-05 15:30:00',
    size: 104857600, // 100MB
    status: 'disabled',
    refCount: 1
  },
  {
    id: 7,
    name: '设计规范.pdf',
    type: 'PDF 文档',
    uploadTime: '2024-03-04 08:45:00',
    size: 31457280, // 30MB
    status: 'active',
    refCount: 8
  },
  {
    id: 8,
    name: '会议纪要.docx',
    type: 'Word 文档',
    uploadTime: '2024-03-03 17:00:00',
    size: 1048576, // 1MB
    status: 'active',
    refCount: 0
  }
])

// 更新总记录数
total.value = tableData.value.length

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

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
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

// 批量启用
const handleBatchEnable = () => {
  if (selectedStorages.value.length === 0) return
  ElMessageBox.confirm(
    `确定要启用选中的 ${selectedStorages.value.length} 个文件吗？`,
    '批量启用',
    {
      confirmButtonText: '启用',
      cancelButtonText: '取消',
      type: 'success'
    }
  ).then(() => {
    tableData.value.forEach(item => {
      if (selectedStorages.value.includes(item.id)) {
        item.status = 'active'
      }
    })
    ElMessage.success('批量启用成功')
  }).catch(() => {})
}

// 批量禁用
const handleBatchDisable = () => {
  if (selectedStorages.value.length === 0) return
  ElMessageBox.confirm(
    `确定要禁用选中的 ${selectedStorages.value.length} 个文件吗？`,
    '批量禁用',
    {
      confirmButtonText: '禁用',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    tableData.value.forEach(item => {
      if (selectedStorages.value.includes(item.id)) {
        item.status = 'disabled'
      }
    })
    selectedStorages.value = []
    ElMessage.success('批量禁用成功')
  }).catch(() => {})
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
