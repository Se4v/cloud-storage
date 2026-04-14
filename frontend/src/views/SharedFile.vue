<template>
  <div class="h-full flex flex-col bg-slate-50 font-sans min-h-screen">
    <!-- Top Header -->
    <div class="bg-white px-8 py-4 border-b border-slate-200 flex-shrink-0 z-10 shadow-sm">
      <div class="flex items-center gap-2 cursor-pointer">
        <div class="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center">
          <div class="w-3.5 h-3.5 bg-white rounded-full"></div>
        </div>
        <span class="font-bold text-[17px] tracking-wide text-slate-900">云盘分享</span>
      </div>
    </div>

    <!-- Main Content -->
    <div class="flex-1 overflow-auto p-6 w-full max-w-[1200px] mx-auto">
      <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden min-h-[600px] flex flex-col">
        
        <!-- User Info & Top Actions -->
        <div class="px-6 py-5 border-b border-slate-200 flex flex-wrap gap-4 items-center justify-between bg-white">
          <div class="flex items-center gap-4">
            <el-avatar 
              :size="48" 
              src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" 
              class="border border-slate-200 bg-slate-50 text-slate-300" 
            />
            <div>
              <div class="text-[16px] font-semibold text-slate-900 flex items-center gap-2">
                <span>夸父*836的分享</span>
              </div>
              <div class="text-[13px] text-slate-500 mt-1 flex items-center gap-3">
                <span>共 {{ fileList.length }} 个文件 {{ totalSize }}</span>
                <span>1天后过期</span>
              </div>
            </div>
          </div>
          
          <!-- Download Button -->
          <div class="flex items-center gap-3">
            <button
              @click="handleDownload"
              :disabled="selectedFiles.length === 0 && fileList.length === 0"
              class="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors shadow-sm"
            >
              <el-icon :size="16"><Download /></el-icon>
              下载
            </button>
          </div>
        </div>

        <!-- Breadcrumb Navigation -->
        <div class="px-6 py-3 border-b border-slate-200 flex items-center gap-2 text-sm text-slate-500 bg-slate-50/50">
          <span
            class="font-medium text-slate-900 cursor-pointer hover:text-blue-600 transition-colors"
            @click="goToRoot"
          >
            全部文件
          </span>
          <template v-if="pathHistory.length > 0">
            <span v-for="(item, index) in pathHistory" :key="item.id" class="flex items-center gap-2">
              <span class="text-slate-300">/</span>
              <span
                class="font-medium cursor-pointer hover:text-blue-600 transition-colors"
                :class="{ 'text-slate-900': index === pathHistory.length - 1, 'text-slate-600': index !== pathHistory.length - 1 }"
                @click="goToPath(index)"
              >
                {{ item.name }}
              </span>
            </span>
          </template>
        </div>

        <!-- File List Tools -->
        <div class="px-6 py-4 flex items-center justify-between bg-white">
          <div class="text-sm text-slate-700 font-medium">全部文件</div>
        </div>

        <!-- File List Table -->
        <div class="flex-1 px-6 pb-6 bg-white">
          <el-table 
            :data="fileList" 
            style="width: 100%" 
            @selection-change="handleSelectionChange"
            @row-dblclick="handleRowDblClick"
            class="share-table" 
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
            <el-table-column type="selection" width="48" align="center" />
            <el-table-column label="文件名" min-width="450">
              <template #default="{ row }">
                <div 
                  class="flex items-center gap-3 py-1 group cursor-pointer pr-4"
                  @dblclick.stop="handleOpenFile(row)"
                >
                   <div class="w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0 text-white bg-slate-400">
                    <el-icon :size="20">
                      <Document v-if="row.type === 1" />
                      <Folder v-else-if="row.type === 2" />
                    </el-icon>
                  </div>
                  <span class="text-sm font-medium text-slate-900 group-hover:text-blue-600 transition-colors truncate">{{ row.name }}</span>
                  
                  <!-- Hover Actions -->
                  <div class="hidden group-hover:flex items-center gap-2 ml-auto shrink-0">
                    <button
                      class="p-2 text-slate-500 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-all"
                      @click.stop="downloadFile(row)"
                      title="下载"
                    >
                      <el-icon :size="18"><Download /></el-icon>
                    </button>
                  </div>
                </div>
              </template>
            </el-table-column>
             <el-table-column prop="size" label="大小" width="150">
              <template #default="{ row }">
                <span class="text-sm text-slate-600 tabular-nums">{{ row.size ? formatBytes(toBigInt(row.size)) : '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="date" label="创建日期" width="180">
               <template #default="{ row }">
                <div class="flex items-center gap-1.5 text-sm text-slate-600">
                  <span class="whitespace-nowrap">{{ row.createTime }}</span>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  Download,
  Document,
  Folder
} from '@element-plus/icons-vue'

// 面包屑路径历史
const pathHistory = ref([])

// 当前所在目录的 folderId（根目录为 null 或 0）
const currentFolderId = ref(null)

// 模拟数据 - 包含文件夹和文件
const fileList = ref([
  {
    id: 1,
    name: 'Idea 版本控制配置.docx',
    size: '703488',
    type: 1, // 1=文件
    createTime: '2026-01-11 18:28',
  },
  {
    id: 2,
    name: '我的文件夹',
    size: null, // 文件夹没有大小
    type: 2, // 2=文件夹
    createTime: '2026-01-10 10:00',
  },
  {
    id: 3,
    name: '项目资料',
    size: null,
    type: 2,
    createTime: '2026-01-09 15:30',
  }
])

const selectedFiles = ref([])

const totalSize = computed(() => {
  if (fileList.value.length === 0) return '0 B'
  // 将所有文件大小转换为BigInt并求和（文件夹不计算大小）
  let total = 0n
  for (const file of fileList.value) {
    if (file.type === 1 && file.size) {
      total += toBigInt(file.size)
    }
  }
  return formatBytes(total)
})

const handleSelectionChange = (val) => {
  selectedFiles.value = val
}

// 双击行事件
const handleRowDblClick = (row) => {
  handleOpenFile(row)
}

// 打开文件/文件夹
const handleOpenFile = (file) => {
  if (file.type === 2) {
    // 进入文件夹：更新路径历史，加载文件夹内容
    pathHistory.value.push({ id: file.id, name: file.name })
    currentFolderId.value = file.id
    // TODO: 调用API加载文件夹内容
    loadFolderContent(file.id)
  } else {
    // 预览文件
    console.log('预览文件:', file)
    // TODO: 实现文件预览逻辑
  }
}

// 加载文件夹内容（模拟）
const loadFolderContent = (folderId) => {
  // TODO: 调用后端API获取文件夹内容
  console.log('加载文件夹内容:', folderId)
  // 模拟数据更新
  fileList.value = [
    {
      id: folderId * 10 + 1,
      name: `文件夹${folderId}中的文件1.txt`,
      size: '1024',
      type: 1,
      createTime: '2026-01-11 18:28',
    },
    {
      id: folderId * 10 + 2,
      name: `子文件夹`,
      size: null,
      type: 2,
      createTime: '2026-01-10 10:00',
    }
  ]
  selectedFiles.value = []
}

// 面包屑导航 - 返回根目录
const goToRoot = () => {
  pathHistory.value = []
  currentFolderId.value = null
  // TODO: 重新加载根目录文件列表
  console.log('返回根目录')
  // 模拟根目录数据
  fileList.value = [
    {
      id: 1,
      name: 'Idea 版本控制配置.docx',
      size: '703488',
      type: 1,
      createTime: '2026-01-11 18:28',
    },
    {
      id: 2,
      name: '我的文件夹',
      size: null,
      type: 2,
      createTime: '2026-01-10 10:00',
    },
    {
      id: 3,
      name: '项目资料',
      size: null,
      type: 2,
      createTime: '2026-01-09 15:30',
    }
  ]
  selectedFiles.value = []
}

// 面包屑导航 - 点击某个路径层级
const goToPath = (index) => {
  // 保留从0到index的路径，删除后面的
  pathHistory.value = pathHistory.value.slice(0, index + 1)
  const targetId = pathHistory.value.length > 0 ? pathHistory.value[pathHistory.value.length - 1].id : null
  currentFolderId.value = targetId
  // TODO: 加载对应层级的文件列表
  console.log('导航到路径层级:', targetId)
  loadFolderContent(targetId)
}

const handleDownload = () => {
  const filesToDownload = selectedFiles.value.length > 0 ? selectedFiles.value : fileList.value
  console.log('Downloading:', filesToDownload)
}

const downloadFile = (file) => {
  console.log('Downloading single file:', file)
}

// 辅助函数：将后端返回的字符串转换为BigInt
const toBigInt = (value) => {
  if (value === null || value === undefined) return 0n
  if (typeof value === 'bigint') return value
  if (typeof value === 'number') return BigInt(Math.floor(value))
  if (typeof value === 'string') {
    // 去除可能的引号和空白字符
    const cleanValue = value.replace(/["']/g, '').trim()
    if (cleanValue === '' || cleanValue === 'null') return 0n
    try {
      return BigInt(cleanValue)
    } catch (e) {
      console.error('无法转换为BigInt:', value, e)
      return 0n
    }
  }
  return 0n
}

// 格式化存储大小
const formatBytes = (bytes) => {
  if (bytes === 0n || bytes === 0) return '0 B'
  const k = 1024n
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  let size = BigInt(bytes)
  let i = 0
  while (size >= k && i < sizes.length - 1) {
    size = size / k
    i++
  }
  return `${Number(size).toFixed(2)} ${sizes[i]}`
}
</script>

<style scoped>
/* 表格样式优化 - 参考 Recovery 页面 */
:deep(.share-table) {
  --el-table-header-bg-color: #f8fafc;
  --el-table-header-text-color: #475569;
  --el-table-row-hover-bg-color: #f1f5f9;
  --el-table-border-color: transparent;
  --el-table-text-color: #334155;
}

:deep(.share-table .el-table__header th) {
  font-weight: 600;
  font-size: 0.875rem;
  height: 48px;
  border-bottom: 1px solid #e2e8f0;
  background-color: #f8fafc;
}

:deep(.share-table .el-table__row) {
  transition: all 0.2s ease;
}

:deep(.share-table .el-table__row td) {
  border-bottom: 1px solid #f1f5f9;
  padding: 12px 0;
  vertical-align: middle;
}

:deep(.share-table .el-table__row:hover td) {
  background-color: #f8fafc;
}

:deep(.share-table .el-checkbox__inner) {
  border-radius: 4px;
  border-color: #cbd5e1;
}

:deep(.share-table .el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #2563eb;
  border-color: #2563eb;
}

:deep(.share-table .el-table__inner-wrapper::before) {
  display: none;
}
</style>