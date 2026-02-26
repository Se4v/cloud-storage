<template>
  <div class="h-full flex flex-col bg-slate-50">
    <!-- 面包屑 & 工具栏 -->
    <div class="bg-white border-b border-slate-200 px-6 py-4 flex flex-col gap-4">
      <!-- 面包屑导航 -->
      <div class="flex items-center gap-2 text-sm text-slate-500">
        <el-icon class="text-slate-400 cursor-pointer hover:text-blue-600"><HomeFilled /></el-icon>
        <span class="text-slate-300">/</span>
        <span class="font-medium text-slate-900">企业空间</span>
        <template v-if="currentPath.length > 0">
          <span class="text-slate-300">/</span>
          <span v-for="(item, index) in currentPath" :key="index" class="flex items-center gap-2">
            <span class="font-medium text-slate-900">{{ item }}</span>
            <span v-if="index < currentPath.length - 1" class="text-slate-300">/</span>
          </span>
        </template>
      </div>

      <!-- 操作工具栏 -->
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
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
                  <span v-if="row.type === 'folder'" class="text-xs text-slate-400 mt-0.5">{{ row.itemCount || 0 }} 项</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="创建时间" width="260">
            <template #default="{ row }">
              <div class="flex items-center gap-2 text-sm text-slate-600">
                <span>{{ formatDate(row.createTime) }}</span>
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

          <el-table-column label="操作" width="150" align="center">
            <template #default="{ row }">
              <div class="flex items-center justify-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity" :class="{ 'opacity-100': selectedFiles.includes(row) }">
                <button
                    @click.stop="handleDownload(row)"
                    class="p-1.5 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-colors"
                    title="下载"
                >
                  <el-icon><Download /></el-icon>
                </button>
                <button
                    @click.stop="handleShareSingle(row)"
                    class="p-1.5 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-colors"
                    title="分享"
                >
                  <el-icon><Share /></el-icon>
                </button>
                <el-dropdown trigger="click" @command="handleCommand($event, row)">
                  <button
                      class="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-md transition-colors"
                      @click.stop
                  >
                    <el-icon><MoreFilled /></el-icon>
                  </button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="rename">
                        <el-icon class="mr-2"><EditPen /></el-icon>重命名
                      </el-dropdown-item>
                      <el-dropdown-item command="move">
                        <el-icon class="mr-2"><Folder /></el-icon>移动
                      </el-dropdown-item>
                      <el-dropdown-item command="copy">
                        <el-icon class="mr-2"><DocumentCopy /></el-icon>复制
                      </el-dropdown-item>
                      <el-dropdown-item divided command="delete" class="text-red-600">
                        <el-icon class="mr-2"><Delete /></el-icon>删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
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
          :total="fileList.length"
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

    <!-- 移动到对话框 -->
    <el-dialog
        v-model="moveVisible"
        title="移动到"
        width="400px"
        class="rounded-lg"
    >
      <div class="py-4">
        <div class="p-3 bg-slate-50 rounded-lg border border-slate-200 mb-4">
          <p class="text-xs text-slate-500">选择目标位置：</p>
        </div>
        <el-radio-group v-model="moveTarget" class="flex flex-col gap-2">
          <el-radio label="personal">个人空间</el-radio>
          <el-radio label="enterprise">企业空间根目录</el-radio>
          <el-radio label="folder1">项目资料</el-radio>
          <el-radio label="folder2">合同文档</el-radio>
        </el-radio-group>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button
              @click="moveVisible = false"
              class="px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
          >
            取消
          </button>
          <button
              @click="confirmMove"
              class="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors"
          >
            确定
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

// 面包屑路径
const currentPath = ref(['家电产品部', '家电产品二部'])

// 文件数据
const fileList = ref([
  {
    id: 1,
    name: '项目重要文件',
    type: 'folder',
    createTime: '2020-06-20 12:30:00',
    size: 0,
    itemCount: 5
  },
  {
    id: 2,
    name: '合同资料',
    type: 'folder',
    createTime: '2020-05-20 14:30:00',
    size: 0,
    itemCount: 12
  },
  {
    id: 3,
    name: '产品体验报告.excl',
    type: 'excel',
    createTime: '2020-03-20 14:20:00',
    size: 35840000
  },
  {
    id: 4,
    name: '5月核算清单.txt',
    type: 'txt',
    createTime: '2020-05-20 12:30:00',
    size: 35650000
  },
  {
    id: 5,
    name: '财务报表.ppt',
    type: 'ppt',
    createTime: '2020-05-20 16:30:20',
    size: 40680000
  },
  {
    id: 6,
    name: '私密音频.mp3',
    type: 'audio',
    createTime: '2020-05-20 18:20:00',
    size: 5242880
  },
  {
    id: 7,
    name: '产品未公开视频.mp4',
    type: 'video',
    createTime: '2020-05-20 20:20:00',
    size: 104857600
  },
  {
    id: 8,
    name: '私密文档.pdf',
    type: 'pdf',
    createTime: '2020-05-20 18:30:00',
    size: 23920000
  },
  {
    id: 9,
    name: '文件名.txt',
    type: 'txt',
    createTime: '2020-05-20 11:10:00',
    size: 44890000
  }
])

// 搜索和分页
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const selectedFiles = ref([])
const tableRef = ref(null)

// 过滤后的文件列表
const filteredFiles = computed(() => {
  if (!searchQuery.value) return fileList.value
  const query = searchQuery.value.toLowerCase()
  return fileList.value.filter(file =>
      file.name.toLowerCase().includes(query)
  )
})

// 对话框状态
const createFolderVisible = ref(false)
const shareVisible = ref(false)
const moveVisible = ref(false)
const newFolderName = ref('')
const shareExpire = ref('7')
const shareLink = ref('')
const moveTarget = ref('')

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
    ppt: Document,
    txt: Document,
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
    ppt: 'bg-orange-500',
    txt: 'bg-slate-500',
    zip: 'bg-yellow-600',
    code: 'bg-slate-600',
    default: 'bg-slate-400'
  }
  return classMap[file.type] || classMap.default
}

// 格式化日期
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
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
    // 实际应用中这里应该加载文件夹内容
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
    createTime: new Date().toISOString().replace('T', ' ').substring(0, 19),
    size: 0,
    itemCount: 0
  }

  fileList.value.unshift(newFolder)
  createFolderVisible.value = false
  ElMessage.success('创建成功')
}

// 下载单个文件
const handleDownload = (row) => {
  ElMessage.success(`开始下载: ${row.name}`)
}

// 批量下载
const handleBatchDownload = () => {
  if (selectedFiles.value.length === 0) return
  ElMessage.success(`开始下载 ${selectedFiles.value.length} 个文件`)
}

// 分享单个文件
const handleShareSingle = (row) => {
  selectedFiles.value = [row]
  shareLink.value = ''
  shareVisible.value = true
}

// 批量分享
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
      handleDownload(row)
      break
    case 'rename':
      handleRenameSingle(row)
      break
    case 'move':
      handleMoveSingle(row)
      break
    case 'copy':
      ElMessage.success(`已复制: ${row.name}`)
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
    row.createTime = new Date().toISOString().replace('T', ' ').substring(0, 19)
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

// 移动文件
const handleMove = () => {
  if (selectedFiles.value.length === 0) return
  moveTarget.value = ''
  moveVisible.value = true
}

const handleMoveSingle = (row) => {
  selectedFiles.value = [row]
  handleMove()
}

const confirmMove = () => {
  if (!moveTarget.value) {
    ElMessage.warning('请选择目标位置')
    return
  }

  ElMessage.success(`已移动 ${selectedFiles.value.length} 个文件`)
  moveVisible.value = false

  // 从列表移除
  const ids = selectedFiles.value.map(f => f.id)
  fileList.value = fileList.value.filter(f => !ids.includes(f.id))
  selectedFiles.value = []
}

// 复制
const handleCopy = () => {
  if (selectedFiles.length === 0) return
  ElMessage.success(`已复制 ${selectedFiles.value.length} 个项目`)
}

// 重命名（批量时禁用）
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
/* 文件表格样式 */
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

/* 分页器样式 */
:deep(.custom-pagination) {
  --el-pagination-hover-color: #2563eb;
}

:deep(.custom-pagination .el-pager li) {
  border-radius: 6px;
  font-weight: 500;
}

:deep(.custom-pagination .el-pager li.is-active) {
  background-color: #2563eb;
  color: white;
}

/* 对话框样式 */
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

/* 移动到对话框中的单选框 */
:deep(.el-radio-group) {
  width: 100%;
}

:deep(.el-radio) {
  margin-right: 0;
  padding: 8px 12px;
  border-radius: 6px;
  width: 100%;
  transition: all 0.2s;
}

:deep(.el-radio:hover) {
  background-color: #f8fafc;
}

:deep(.el-radio.is-checked) {
  background-color: #eff6ff;
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>