<template>
  <div class="h-full flex flex-col bg-slate-50">
    <!-- 工具栏 -->
    <div class="bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between">
      <div class="flex flex-col gap-2">
        <!-- 存储空间信息 -->
        <div class="text-xs text-slate-500">
          已使用 {{ formatSize(usedStorage) }} | 总容量 {{ formatSize(totalStorage) }}
        </div>
        <!-- 主要操作按钮 -->
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
                <el-icon class="mr-2"><DocumentCopy /></el-icon>复制到
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

    <!-- 文件列表 -->
    <div class="flex-1 overflow-auto p-6">
      <!-- 面包屑导航 - 移到列表上方，显示文件夹路径 -->
      <div class="mb-4 flex items-center gap-2 text-sm text-slate-500 bg-white px-4 py-3 rounded-lg border border-slate-200 shadow-sm">
        <el-icon class="text-slate-400 cursor-pointer hover:text-blue-600" @click="goToRoot"><HomeFilled /></el-icon>
        <span class="text-slate-300">/</span>
        <span
            class="font-medium text-slate-900 cursor-pointer hover:text-blue-600 transition-colors"
            @click="goToRoot"
        >
          个人空间
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

      <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <el-table
            ref="tableRef"
            :data="filteredFiles"
            style="width: 100%"
            @selection-change="handleSelectionChange"
            @row-click="handleRowClick"
            :row-class-name="() => 'group hover:bg-slate-50/80 transition-colors'"
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
                    class="w-10 h-10 rounded-lg flex items-center justify-center text-white shadow-sm transition-transform group-hover:scale-105 bg-blue-500"
                >
                  <el-icon :size="20">
                    <component :is="getFileIcon(row)" />
                  </el-icon>
                </div>
                <div class="flex flex-col">
                  <span class="font-medium text-slate-900 text-sm group-hover:text-blue-600 transition-colors">{{ row.name }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="创建时间" width="260">
            <template #default="{ row }">
              <span class="text-sm text-slate-600">{{ row.createTime }}</span>
            </template>
          </el-table-column>

          <el-table-column label="大小" width="120">
            <template #default="{ row }">
              <span class="text-sm text-slate-600 font-medium tabular-nums">
                {{ row.type === 2 ? '-' : formatSize(row.size) }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <!-- 操作按钮直接显示，不用隐藏 -->
              <el-dropdown trigger="click" @command="handleCommand($event, row)">
                <button
                    class="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
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

    <!-- 移动到对话框 -->
    <el-dialog
        v-model="moveVisible"
        title="移动到"
        width="480px"
        :close-on-click-modal="false"
        class="rounded-lg move-dialog"
    >
      <div class="py-4">
        <!-- 空间根目录提示 -->
        <div class="flex items-center gap-2 px-3 py-2 bg-slate-50 rounded-lg mb-3">
          <el-icon class="text-blue-500"><Folder /></el-icon>
          <span class="text-sm font-medium text-slate-700">个人空间</span>
        </div>

        <!-- 文件夹树形结构 -->
        <div class="folder-tree-container border border-slate-200 rounded-lg overflow-auto max-h-[360px]">
          <el-tree
              ref="moveTreeRef"
              :data="folderTreeData"
              :props="{ label: 'name', children: 'children' }"
              node-key="id"
              :highlight-current="true"
              :expand-on-click-node="false"
              @node-click="handleTreeNodeClick"
              class="folder-tree"
          >
            <template #default="{ node, data }">
              <div class="flex items-center gap-2 py-1">
                <el-icon class="text-blue-400" :size="18"><Folder /></el-icon>
                <span class="text-sm text-slate-700">{{ data.name }}</span>
                <el-icon v-if="selectedTargetFolder && selectedTargetFolder.id === data.id" class="text-blue-600 ml-auto" :size="16"><Check /></el-icon>
              </div>
            </template>
          </el-tree>
        </div>
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
              :disabled="!selectedTargetFolder"
              class="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg transition-colors"
          >
            确认
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 复制到对话框 -->
    <el-dialog
        v-model="copyVisible"
        title="复制到"
        width="480px"
        :close-on-click-modal="false"
        class="rounded-lg move-dialog"
    >
      <div class="py-4">
        <!-- 空间根目录提示 -->
        <div class="flex items-center gap-2 px-3 py-2 bg-slate-50 rounded-lg mb-3">
          <el-icon class="text-blue-500"><Folder /></el-icon>
          <span class="text-sm font-medium text-slate-700">个人空间</span>
        </div>

        <!-- 文件夹树形结构 -->
        <div class="folder-tree-container border border-slate-200 rounded-lg overflow-auto max-h-[360px]">
          <el-tree
              ref="copyTreeRef"
              :data="folderTreeData"
              :props="{ label: 'name', children: 'children' }"
              node-key="id"
              :highlight-current="true"
              :expand-on-click-node="false"
              @node-click="handleCopyTreeNodeClick"
              class="folder-tree"
          >
            <template #default="{ node, data }">
              <div class="flex items-center gap-2 py-1">
                <el-icon class="text-blue-400" :size="18"><Folder /></el-icon>
                <span class="text-sm text-slate-700">{{ data.name }}</span>
                <el-icon v-if="selectedTargetFolder && selectedTargetFolder.id === data.id" class="text-blue-600 ml-auto" :size="16"><Check /></el-icon>
              </div>
            </template>
          </el-tree>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button
              @click="copyVisible = false"
              class="px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
          >
            取消
          </button>
          <button
              @click="confirmCopy"
              :disabled="!selectedTargetFolder"
              class="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg transition-colors"
          >
            确认
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { useRoute } from 'vue-router'
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
  HomeFilled,
  Check
} from '@element-plus/icons-vue'
import axios from 'axios'

const route = useRoute()

// 状态管理
const loading = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const selectedFiles = ref([])
const tableRef = ref(null)
const moveTreeRef = ref(null)
const copyTreeRef = ref(null)

// driveId 从路由参数获取
const driveId = computed(() => route.params.driveId)

// 面包屑路径历史（前端维护）
const pathHistory = ref([])

// 当前所在目录的 parentId（根目录为 null）
const currentParentId = ref(null)

// 文件列表
const fileList = ref([
  {
    id: '123',
    name: '3',
    type: 1,
    size: '123',
    createTime: '123123'
  }
])

// 存储空间（单位：字节）
const usedStorage = ref(49 * 1024 * 1024) // 49MB
const totalStorage = ref(1024 * 1024 * 1024) // 1GB

// 对话框状态
const createFolderVisible = ref(false)
const shareVisible = ref(false)
const moveVisible = ref(false)
const copyVisible = ref(false)
const newFolderName = ref('')
const shareExpire = ref('7')
const shareLink = ref('')
const selectedTargetFolder = ref(null)

// 文件夹树形数据（移动/复制对话框用）
const folderTreeData = ref([])

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
    2: FolderOpened,
    default: Document
  }
  return iconMap[file.type] || iconMap.default
}

// 格式化文件大小
const formatSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedFiles.value = selection
}

// 行点击
const handleRowClick = (row) => {
  tableRef.value?.toggleRowSelection(row)
}

// 加载文件列表
const loadFileList = async (parentId = null) => {
  if (!driveId.value) {
    ElMessage.error('未获取到 driveId')
    return
  }

  loading.value = true
  try {
    let url = `/api/personal/list?driveId=${driveId.value}`
    if (parentId !== null) {
      url += `&parentId=${parentId}`
    }
    const response = await axios.get(url)
    if (response.data.code === 200) {
      fileList.value = response.data.data || []
      total.value = fileList.value.length
      selectedFiles.value = []
    } else {
      ElMessage.error(response.data.message || '加载失败')
    }
  } catch (error) {
    console.error('加载文件列表失败:', error)
    fileList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 打开文件/文件夹
const handleOpenFile = (file) => {
  if (file.type === 2) {
    // 进入文件夹：更新路径历史，重新加载文件列表
    pathHistory.value.push({ id: file.id, name: file.name })
    currentParentId.value = file.id
    loadFileList(file.id)
  } else {
    ElMessage.info(`预览文件: ${file.name}`)
  }
}

// 面包屑导航 - 返回根目录
const goToRoot = () => {
  pathHistory.value = []
  currentParentId.value = null
  loadFileList(null)
}

// 面包屑导航 - 点击某个路径层级
const goToPath = (index) => {
  // 保留从0到index的路径，删除后面的
  pathHistory.value = pathHistory.value.slice(0, index + 1)
  const targetId = pathHistory.value.length > 0 ? pathHistory.value[pathHistory.value.length - 1].id : null
  currentParentId.value = targetId
  loadFileList(targetId)
}

// 初始化加载
onMounted(() => {
  loadFileList()
})

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
const confirmCreateFolder = async () => {
  if (!newFolderName.value.trim()) {
    ElMessage.warning('请输入文件夹名称')
    return
  }

  if (!driveId.value) {
    ElMessage.error('未获取到 driveId')
    return
  }

  try {
    const response = await axios.post('/api/personal/create', {
      parentId: currentParentId.value,
      folderName: newFolderName.value.trim()
    })

    if (response.data.code === 200) {
      createFolderVisible.value = false
      ElMessage.success('创建成功')
      // 重新加载文件列表
      loadFileList(currentParentId.value)
    } else {
      ElMessage.error(response.data.message || '创建失败')
    }
  } catch (error) {
    console.error('创建文件夹失败:', error)
    ElMessage.error('创建失败')
  }
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
      handleMoveSingle(row)
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

    const response = await axios.post('/api/personal/rename', {
      entryId: row.id,
      newName: value
    })

    if (response.data.code === 200) {
      ElMessage.success('重命名成功')
      loadFileList(currentParentId.value)
    } else {
      ElMessage.error(response.data.message || '重命名失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重命名失败:', error)
      ElMessage.error('重命名失败')
    }
  }
}

// 删除单个文件
const handleDeleteSingle = (row) => {
  ElMessageBox.confirm(`确定删除 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    try {
      const response = await axios.post('/api/personal/delete', [row.id])
      if (response.data.code === 200) {
        ElMessage.success('删除成功')
        loadFileList(currentParentId.value)
      } else {
        ElMessage.error(response.data.message || '删除失败')
      }
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 加载文件夹树形数据（用于移动/复制对话框）
const loadFolderTree = async () => {
  if (!driveId.value) return

  try {
    // TODO: 后端接口 /api/personal/folder 实现后取消注释
    // const response = await axios.get(`/api/personal/folder?driveId=${driveId.value}`)
    // if (response.data.code === 200) {
    //   folderTreeData.value = response.data.data || []
    // }

    // 暂时使用空数据
    folderTreeData.value = []
  } catch (error) {
    console.error('加载文件夹树失败:', error)
    folderTreeData.value = []
  }
}

// 移动
const handleMove = () => {
  if (selectedFiles.value.length === 0) return
  selectedTargetFolder.value = null
  moveVisible.value = true
  loadFolderTree()
}

// 移动单个文件
const handleMoveSingle = (row) => {
  selectedFiles.value = [row]
  handleMove()
}

// 处理树节点点击
const handleTreeNodeClick = (data) => {
  selectedTargetFolder.value = data
}

// 确认移动
const confirmMove = async () => {
  if (!selectedTargetFolder.value) {
    ElMessage.warning('请选择目标文件夹')
    return
  }

  try {
    const ids = selectedFiles.value.map(f => f.id)
    const response = await axios.post('/api/personal/move', {
      entryIds: ids,
      targetId: selectedTargetFolder.value.id
    })

    if (response.data.code === 200) {
      ElMessage.success(`已将 ${selectedFiles.value.length} 个文件移动到 "${selectedTargetFolder.value.name}"`)
      moveVisible.value = false
      selectedTargetFolder.value = null
      selectedFiles.value = []
      loadFileList(currentParentId.value)
    } else {
      ElMessage.error(response.data.message || '移动失败')
    }
  } catch (error) {
    console.error('移动失败:', error)
    ElMessage.error('移动失败')
  }
}

// 复制
const handleCopy = () => {
  if (selectedFiles.value.length === 0) return

  // 校验：只能复制单个文件，不能复制文件夹
  if (selectedFiles.value.length !== 1) {
    ElMessage.warning('只能复制单个文件')
    return
  }

  const selectedFile = selectedFiles.value[0]
  if (selectedFile.type === 2) {
    ElMessage.warning('不能复制文件夹，请选择文件')
    return
  }

  selectedTargetFolder.value = null
  copyVisible.value = true
  loadFolderTree()
}

// 处理复制对话框中的树节点点击
const handleCopyTreeNodeClick = (data) => {
  selectedTargetFolder.value = data
}

// 确认复制
const confirmCopy = async () => {
  if (!selectedTargetFolder.value) {
    ElMessage.warning('请选择目标文件夹')
    return
  }

  const selectedFile = selectedFiles.value[0]
  try {
    const response = await axios.post('/api/personal/copy', {
      entryId: selectedFile.id,
      targetId: selectedTargetFolder.value.id
    })

    if (response.data.code === 200) {
      ElMessage.success(`已将 "${selectedFile.name}" 复制到 "${selectedTargetFolder.value.name}"`)
      copyVisible.value = false
      selectedTargetFolder.value = null
      selectedFiles.value = []
      loadFileList(currentParentId.value)
    } else {
      ElMessage.error(response.data.message || '复制失败')
    }
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
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
  }).then(async () => {
    try {
      const ids = selectedFiles.value.map(f => f.id)
      const response = await axios.post('/api/personal/delete', ids)
      if (response.data.code === 200) {
        ElMessage.success('删除成功')
        selectedFiles.value = []
        loadFileList(currentParentId.value)
      } else {
        ElMessage.error(response.data.message || '删除失败')
      }
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
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

/* 移动到对话框样式 */
:deep(.move-dialog .el-dialog__body) {
  padding: 16px 24px;
}

.folder-tree-container {
  background: #fff;
}

:deep(.folder-tree) {
  padding: 8px 0;
}

:deep(.folder-tree .el-tree-node__content) {
  height: 40px;
  padding-right: 12px;
  border-radius: 6px;
  margin: 0 8px;
}

:deep(.folder-tree .el-tree-node__content:hover) {
  background-color: #f1f5f9;
}

:deep(.folder-tree .el-tree-node:focus > .el-tree-node__content) {
  background-color: #eff6ff;
}

:deep(.folder-tree .el-tree-node.is-current > .el-tree-node__content) {
  background-color: #eff6ff;
}

:deep(.folder-tree .el-tree-node__expand-icon) {
  color: #94a3b8;
  font-size: 14px;
}

:deep(.folder-tree .el-tree-node__expand-icon.is-leaf) {
  color: transparent;
}
</style>
