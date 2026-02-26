<template>
  <div class="h-full flex flex-col bg-slate-50">
    <!-- 面包屑 & 工具栏 -->
    <div class="bg-white border-b border-slate-200 px-6 py-4 flex flex-col gap-4">
      <!-- 面包屑导航 -->
      <div class="flex items-center gap-2 text-sm text-slate-500">
        <el-icon class="text-slate-400 cursor-pointer hover:text-blue-600"><HomeFilled /></el-icon>
        <span class="text-slate-300">/</span>
        <span class="font-medium text-slate-900">个人空间</span>
        <span v-if="currentPath.length > 0" class="text-slate-300">/</span>
        <span v-for="(item, index) in currentPath" :key="index" class="flex items-center gap-2">
          <span class="font-medium text-slate-900">{{ item }}</span>
          <span v-if="index < currentPath.length - 1" class="text-slate-300">/</span>
        </span>
      </div>

      <!-- 操作工具栏 -->
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <!-- 主要操作按钮 -->
          <button
              @click="handleUpload"
              class="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 active:scale-95 transition-all shadow-sm hover:shadow"
          >
            <el-icon><Upload /></el-icon>
            上传
          </button>

          <button
              @click="handleCreateFolder"
              class="inline-flex items-center gap-2 px-4 py-2 bg-white text-slate-700 border border-slate-200 text-sm font-medium rounded-lg hover:bg-slate-50 hover:border-slate-300 active:scale-95 transition-all"
          >
            <el-icon><Plus /></el-icon>
            新建
          </button>

          <button
              @click="handleBatchDownload"
              :disabled="selectedFiles.length === 0"
              class="inline-flex items-center gap-2 px-4 py-2 bg-white text-slate-700 border border-slate-200 text-sm font-medium rounded-lg hover:bg-slate-50 hover:border-slate-300 active:scale-95 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <el-icon><Download /></el-icon>
            下载
          </button>

          <button
              @click="handleShare"
              :disabled="selectedFiles.length === 0"
              class="inline-flex items-center gap-2 px-4 py-2 bg-white text-slate-700 border border-slate-200 text-sm font-medium rounded-lg hover:bg-slate-50 hover:border-slate-300 active:scale-95 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <el-icon><Share /></el-icon>
            分享
          </button>

          <el-dropdown trigger="click">
            <button
                class="inline-flex items-center gap-2 px-4 py-2 bg-white text-slate-700 border border-slate-200 text-sm font-medium rounded-lg hover:bg-slate-50 hover:border-slate-300 active:scale-95 transition-all"
            >
              <span>更多</span>
              <el-icon><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu class="min-w-[160px]">
                <el-dropdown-item @click="handleMove" :disabled="selectedFiles.length === 0">
                  <el-icon class="mr-2"><Folder /></el-icon>移动到
                </el-dropdown-item>
                <el-dropdown-item @click="handleCopy" :disabled="selectedFiles.length === 0">
                  <el-icon class="mr-2"><DocumentCopy /></el-icon>复制
                </el-dropdown-item>
                <el-dropdown-item @click="handleRename" :disabled="selectedFiles.length !== 1">
                  <el-icon class="mr-2"><EditPen /></el-icon>重命名
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleBatchDelete" :disabled="selectedFiles.length === 0" class="text-red-600">
                  <el-icon class="mr-2"><Delete /></el-icon>删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <!-- 搜索框 -->
        <div class="relative w-64">
          <el-input
              v-model="searchQuery"
              placeholder="搜索文件..."
              class="w-full"
              clearable
          >
            <template #prefix>
              <el-icon class="text-slate-400"><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
    </div>

    <!-- 文件列表 -->
    <div class="flex-1 overflow-auto p-6">
      <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <el-table
            ref="tableRef"
            :data="filteredFiles"
            style="width: 100%"
            @selection-change="handleSelectionChange"
            @row-click="handleRowClick"
            :row-class-name="getRowClassName"
            class="file-table"
        >
          <el-table-column type="selection" width="55" align="center" />

          <el-table-column label="项目名称" min-width="320">
            <template #default="{ row }">
              <div
                  class="flex items-center gap-3 cursor-pointer group"
                  @dblclick="handleOpenFile(row)"
              >
                <div
                    class="w-10 h-10 rounded-lg flex items-center justify-center text-white shadow-sm transition-transform group-hover:scale-105"
                    :class="getFileIconClass(row)"
                >
                  <el-icon :size="20">
                    <component :is="getFileIcon(row)" />
                  </el-icon>
                </div>
                <div class="flex flex-col">
                  <span class="font-medium text-slate-900 text-sm group-hover:text-blue-600 transition-colors">{{ row.name }}</span>
                  <span v-if="row.type === 'folder'" class="text-xs text-slate-400 mt-0.5">{{ row.itemCount }} 项</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="修改时间" width="260">
            <template #default="{ row }">
              <div class="flex items-center gap-2 text-sm text-slate-600">
                <span>{{ formatDate(row.modifiedTime) }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="大小" width="120">
            <template #default="{ row }">
              <span class="text-sm text-slate-600 font-medium tabular-nums">
                {{ row.type === 'folder' ? '-' : formatSize(row.size) }}
              </span>
            </template>
          </el-table-column>

          <el-table-column width="100" align="center">
            <template #default="{ row }">
              <el-dropdown trigger="click" @command="handleCommand($event, row)">
                <button
                    class="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition-colors opacity-0 group-hover:opacity-100"
                    :class="{ 'opacity-100': selectedFiles.includes(row) }"
                    @click.stop
                >
                  <el-icon><MoreFilled /></el-icon>
                </button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="download">
                      <el-icon class="mr-2"><Download /></el-icon>下载
                    </el-dropdown-item>
                    <el-dropdown-item command="share">
                      <el-icon class="mr-2"><Share /></el-icon>分享
                    </el-dropdown-item>
                    <el-dropdown-item command="rename">
                      <el-icon class="mr-2"><EditPen /></el-icon>重命名
                    </el-dropdown-item>
                    <el-dropdown-item command="move">
                      <el-icon class="mr-2"><Folder /></el-icon>移动
                    </el-dropdown-item>
                    <el-dropdown-item divided command="delete" class="text-red-600">
                      <el-icon class="mr-2"><Delete /></el-icon>删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>

        <!-- 空状态 -->
        <div v-if="filteredFiles.length === 0" class="py-20 flex flex-col items-center justify-center text-slate-400">
          <el-icon :size="64" class="mb-4 opacity-20"><FolderOpened /></el-icon>
          <p class="text-sm">暂无文件</p>
          <button
              @click="handleUpload"
              class="mt-4 text-blue-600 hover:text-blue-700 text-sm font-medium"
          >
            立即上传
          </button>
        </div>
      </div>
    </div>

    <!-- 底部状态栏 -->
    <div class="bg-white border-t border-slate-200 px-6 py-3 flex items-center justify-between text-sm text-slate-500">
      <div class="flex items-center gap-4">
        <span>共 {{ fileList.length }} 项数据</span>
        <span v-if="selectedFiles.length > 0" class="text-blue-600 font-medium">
          已选中 {{ selectedFiles.length }} 项
        </span>
      </div>

      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          background
          class="custom-pagination"
      />
    </div>

    <!-- 新建文件夹对话框 -->
    <el-dialog
        v-model="createFolderVisible"
        title="新建文件夹"
        width="400px"
        :close-on-click-modal="false"
        class="rounded-lg"
    >
      <div class="py-4">
        <el-input
            v-model="newFolderName"
            placeholder="请输入文件夹名称"
            maxlength="50"
            show-word-limit
            autofocus
            @keyup.enter="confirmCreateFolder"
        >
          <template #prefix>
            <el-icon class="text-slate-400"><Folder /></el-icon>
          </template>
        </el-input>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button
              @click="createFolderVisible = false"
              class="px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
          >
            取消
          </button>
          <button
              @click="confirmCreateFolder"
              class="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors"
          >
            确定
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 分享对话框 -->
    <el-dialog
        v-model="shareVisible"
        title="分享文件"
        width="500px"
        class="rounded-lg"
    >
      <div class="py-4 space-y-4">
        <div class="p-4 bg-slate-50 rounded-lg border border-slate-200">
          <p class="text-sm text-slate-600 mb-2">已选择 {{ selectedFiles.length }} 个文件</p>
          <div class="flex flex-wrap gap-2">
            <span
                v-for="file in selectedFiles.slice(0, 3)"
                :key="file.id"
                class="px-2 py-1 bg-white text-xs text-slate-700 rounded border border-slate-200 truncate max-w-[120px]"
            >
              {{ file.name }}
            </span>
            <span v-if="selectedFiles.length > 3" class="px-2 py-1 text-xs text-slate-500">
              +{{ selectedFiles.length - 3 }}
            </span>
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">分享有效期</label>
          <el-radio-group v-model="shareExpire">
            <el-radio-button label="1">1天</el-radio-button>
            <el-radio-button label="7">7天</el-radio-button>
            <el-radio-button label="30">30天</el-radio-button>
            <el-radio-button label="0">永久有效</el-radio-button>
          </el-radio-group>
        </div>

        <div v-if="shareLink" class="p-3 bg-blue-50 border border-blue-200 rounded-lg flex items-center justify-between">
          <code class="text-sm text-blue-900 font-mono truncate flex-1 mr-3">{{ shareLink }}</code>
          <button
              @click="copyShareLink"
              class="px-3 py-1.5 text-xs font-medium text-blue-700 bg-blue-100 hover:bg-blue-200 rounded transition-colors"
          >
            复制链接
          </button>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button
              @click="shareVisible = false"
              class="px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
          >
            取消
          </button>
          <button
              @click="generateShareLink"
              class="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors"
          >
            生成链接
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Folder,
  FolderOpened,
  Document,
  Picture,
  VideoCamera,
  Headset,
  Box,
  Upload,
  Plus,
  Download,
  Share,
  Delete,
  EditPen,
  DocumentCopy,
  ArrowDown,
  Search,
  MoreFilled,
  HomeFilled
} from '@element-plus/icons-vue'

// 状态管理
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(14)
const selectedFiles = ref([])
const currentPath = ref([])
const tableRef = ref(null)

// 对话框状态
const createFolderVisible = ref(false)
const shareVisible = ref(false)
const newFolderName = ref('')
const shareExpire = ref('7')
const shareLink = ref('')

// 模拟文件数据
const fileList = ref([
  {
    id: 1,
    name: '企业网盘移动端图标PNG格式',
    type: 'folder',
    modifiedTime: '2022-08-16 19:54:00',
    size: 0,
    itemCount: 24
  },
  {
    id: 2,
    name: '多种格式预览库',
    type: 'folder',
    modifiedTime: '2022-08-16 19:53:48',
    size: 0,
    itemCount: 156
  },
  {
    id: 3,
    name: '新建文件夹',
    type: 'folder',
    modifiedTime: '2022-08-16 19:53:29',
    size: 0,
    itemCount: 0
  },
  {
    id: 4,
    name: '新建文件夹 (1)',
    type: 'folder',
    modifiedTime: '2022-08-30 16:04:56',
    size: 0,
    itemCount: 3
  },
  {
    id: 5,
    name: '产品设计规范文档.pdf',
    type: 'pdf',
    modifiedTime: '2022-08-31 15:59:55',
    size: 3460300
  },
  {
    id: 6,
    name: '项目需求分析.xlsx',
    type: 'excel',
    modifiedTime: '2022-08-31 15:59:49',
    size: 24064
  },
  {
    id: 7,
    name: '会议纪要-20220830.docx',
    type: 'word',
    modifiedTime: '2022-08-31 15:59:49',
    size: 11400
  },
  {
    id: 8,
    name: 'logo-design-v2.png',
    type: 'image',
    modifiedTime: '2022-08-30 16:05:22',
    size: 13000
  },
  {
    id: 9,
    name: '演示视频.mp4',
    type: 'video',
    modifiedTime: '2022-08-30 16:05:22',
    size: 52428800
  },
  {
    id: 10,
    name: '录音文件.mp3',
    type: 'audio',
    modifiedTime: '2022-08-30 16:05:22',
    size: 2097152
  },
  {
    id: 11,
    name: 'source-code.zip',
    type: 'zip',
    modifiedTime: '2022-08-30 16:05:22',
    size: 10485760
  },
  {
    id: 12,
    name: 'index.html',
    type: 'code',
    modifiedTime: '2022-08-30 16:05:22',
    size: 4096
  }
])

// 过滤后的文件列表
const filteredFiles = computed(() => {
  if (!searchQuery.value) return fileList.value
  const query = searchQuery.value.toLowerCase()
  return fileList.value.filter(file =>
      file.name.toLowerCase().includes(query)
  )
})

// 获取文件图标
const getFileIcon = (file) => {
  const iconMap = {
    folder: FolderOpened,
    image: Picture,
    video: VideoCamera,
    audio: Headset,
    pdf: Document,
    word: Document,
    excel: Document,
    zip: Box,
    code: Document,
    default: Document
  }
  return iconMap[file.type] || iconMap.default
}

// 获取文件图标背景色
const getFileIconClass = (file) => {
  const classMap = {
    folder: 'bg-blue-500',
    image: 'bg-purple-500',
    video: 'bg-red-500',
    audio: 'bg-amber-500',
    pdf: 'bg-red-600',
    word: 'bg-blue-600',
    excel: 'bg-green-600',
    zip: 'bg-yellow-600',
    code: 'bg-slate-600',
    default: 'bg-slate-400'
  }
  return classMap[file.type] || classMap.default
}

// 格式化日期
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) return '今天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`

  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-')
}

// 格式化文件大小
const formatSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

// 表格行类名
const getRowClassName = () => {
  return 'group hover:bg-slate-50/80 transition-colors'
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedFiles.value = selection
}

// 行点击
const handleRowClick = (row) => {
  tableRef.value?.toggleRowSelection(row)
}

// 打开文件/文件夹
const handleOpenFile = (file) => {
  if (file.type === 'folder') {
    currentPath.value.push(file.name)
    ElMessage.success(`进入文件夹: ${file.name}`)
    // 这里应该加载文件夹内容
  } else {
    ElMessage.info(`预览文件: ${file.name}`)
  }
}

// 上传
const handleUpload = () => {
  ElMessage.info('打开上传对话框')
}

// 新建文件夹
const handleCreateFolder = () => {
  newFolderName.value = '新建文件夹'
  createFolderVisible.value = true
}

// 确认创建文件夹
const confirmCreateFolder = () => {
  if (!newFolderName.value.trim()) {
    ElMessage.warning('请输入文件夹名称')
    return
  }

  const newFolder = {
    id: Date.now(),
    name: newFolderName.value,
    type: 'folder',
    modifiedTime: new Date().toISOString().replace('T', ' ').substring(0, 19),
    size: 0,
    itemCount: 0
  }

  fileList.value.unshift(newFolder)
  createFolderVisible.value = false
  ElMessage.success('创建成功')
}

// 批量下载
const handleBatchDownload = () => {
  if (selectedFiles.value.length === 0) return
  ElMessage.success(`开始下载 ${selectedFiles.value.length} 个文件`)
}

// 分享
const handleShare = () => {
  if (selectedFiles.value.length === 0) return
  shareLink.value = ''
  shareVisible.value = true
}

// 生成分享链接
const generateShareLink = () => {
  shareLink.value = `https://drive.company.com/s/${Math.random().toString(36).substring(2, 15)}`
  ElMessage.success('分享链接已生成')
}

// 复制分享链接
const copyShareLink = () => {
  navigator.clipboard.writeText(shareLink.value)
  ElMessage.success('链接已复制到剪贴板')
}

// 更多操作命令
const handleCommand = (command, row) => {
  switch (command) {
    case 'download':
      ElMessage.success(`下载: ${row.name}`)
      break
    case 'share':
      selectedFiles.value = [row]
      handleShare()
      break
    case 'rename':
      handleRenameSingle(row)
      break
    case 'move':
      ElMessage.info('移动到...')
      break
    case 'delete':
      handleDeleteSingle(row)
      break
  }
}

// 重命名单个文件
const handleRenameSingle = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新名称', '重命名', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: row.name,
      inputPattern: /.+/,
      inputErrorMessage: '名称不能为空'
    })

    row.name = value
    row.modifiedTime = new Date().toISOString().replace('T', ' ').substring(0, 19)
    ElMessage.success('重命名成功')
  } catch {
    // 用户取消
  }
}

// 删除单个文件
const handleDeleteSingle = (row) => {
  ElMessageBox.confirm(`确定删除 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger'
  }).then(() => {
    const index = fileList.value.findIndex(f => f.id === row.id)
    if (index > -1) {
      fileList.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  }).catch(() => {})
}

// 移动
const handleMove = () => {
  ElMessage.info('选择目标文件夹...')
}

// 复制
const handleCopy = () => {
  ElMessage.success(`已复制 ${selectedFiles.value.length} 个项目`)
}

// 重命名（批量时禁用，已在按钮中控制）
const handleRename = () => {
  if (selectedFiles.value.length === 1) {
    handleRenameSingle(selectedFiles.value[0])
  }
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedFiles.value.length === 0) return

  ElMessageBox.confirm(`确定删除选中的 ${selectedFiles.value.length} 个项目吗？`, '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger'
  }).then(() => {
    const ids = selectedFiles.value.map(f => f.id)
    fileList.value = fileList.value.filter(f => !ids.includes(f.id))
    selectedFiles.value = []
    ElMessage.success('删除成功')
  }).catch(() => {})
}
</script>

<style scoped>
/* 自定义表格样式 - shadcn 风格 */
:deep(.file-table) {
  --el-table-header-bg-color: #f8fafc;
  --el-table-header-text-color: #64748b;
  --el-table-row-hover-bg-color: #f8fafc;
  --el-table-border-color: #e2e8f0;
  --el-table-text-color: #334155;
}

:deep(.file-table .el-table__header) {
  font-weight: 600;
  font-size: 13px;
  letter-spacing: 0.025em;
}

:deep(.file-table .el-table__cell) {
  padding: 12px 0;
}

:deep(.file-table .el-checkbox__inner) {
  border-radius: 4px;
  border-color: #cbd5e1;
}

:deep(.file-table .el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #2563eb;
  border-color: #2563eb;
}

/* 分页器样式优化 */
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

/* 动画效果 */
:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  margin: 0;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  font-weight: 600;
}

:deep(.el-dialog__body) {
  padding: 0 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #e2e8f0;
}

/* 输入框样式 */
:deep(.el-input__inner) {
  border-radius: 8px;
  border-color: #e2e8f0;
}

:deep(.el-input__inner:focus) {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}
</style>