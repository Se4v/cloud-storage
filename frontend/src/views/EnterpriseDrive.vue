<template>
  <div class="h-full flex flex-col bg-slate-50">
    <!-- 工具栏 -->
    <div class="bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between">
      <div class="flex flex-col gap-2">
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
              :disabled="selectedFiles.length !== 1"
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
        :close-on-click-modal="false"
    >
      <div class="py-4 space-y-4">
        <el-form :model="shareForm" label-width="100px">
          <el-form-item label="链接名称">
            <el-input v-model="shareForm.linkName" placeholder="请输入链接名称" />
          </el-form-item>
          <el-form-item label="分享类型">
            <el-radio-group v-model="shareForm.linkType">
              <el-radio :label="1">公开链接</el-radio>
              <el-radio :label="2">加密链接</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="提取码" v-if="shareForm.linkType === 2">
            <el-input v-model="shareForm.accessCode" placeholder="请输入提取码" maxlength="6" show-word-limit />
          </el-form-item>
          <el-form-item label="过期时间">
            <el-date-picker
                v-model="shareForm.expireTime"
                type="datetime"
                placeholder="选择过期时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
            />
          </el-form-item>
        </el-form>
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

    <!-- 隐藏的文件选择输入框 -->
    <input
        ref="fileInputRef"
        type="file"
        multiple
        style="display: none"
        @change="handleFileChange"
    />

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
import { useUserStore } from '@/stores/user.js'
import { useUploadStore } from '@/stores/upload.js'

const route = useRoute()
const userStore = useUserStore()
const uploadStore = useUploadStore()
const API_BASE_URL = 'http://localhost:8080'

// 获取请求配置（包含认证头）
const getAuthConfig = () => {
  const token = userStore.token
  const orgId = userStore.orgId
  return {
    headers: {
      'Authorization': token ? `Bearer ${token}` : '',
      'Content-Type': 'application/json',
      'X-Org-Id': orgId || ''
    }
  }
}

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
const fileInputRef = ref(null)

// driveId 从路由参数获取
const driveId = computed(() => route.params.driveId)

// 面包屑路径历史（前端维护）
const pathHistory = ref([])

// 当前所在目录的 parentId（根目录为 0）
const currentParentId = ref(0)

// 文件列表
const fileList = ref([
  {
    id: 123,
    name: 'asda',
    size: 1234,
    createTime: '2312'
  }
])

// 对话框状态
const createFolderVisible = ref(false)
const shareVisible = ref(false)
const moveVisible = ref(false)
const copyVisible = ref(false)
const newFolderName = ref('')
const selectedTargetFolder = ref(null)

// 分享表单
const shareForm = ref({
  linkName: '',
  linkType: 1,
  accessCode: '',
  expireTime: null
})

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
const loadFileList = async (parentId = 0) => {
  if (!driveId.value) {
    ElMessage.error('未获取到 driveId')
    return
  }

  loading.value = true
  try {
    const res = await axios.get(`${API_BASE_URL}/api/enterprise`, {
      ...getAuthConfig(),
      params: {
        driveId: driveId.value,
        parentId: parentId
      }
    })
    if (res.data.code === 200) {
      fileList.value = res.data.data || []
      total.value = fileList.value.length
      selectedFiles.value = []
    } else {
      ElMessage.error(res.data.msg || '加载失败')
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
const handleOpenFile = async (file) => {
  if (file.type === 2) {
    // 进入文件夹：更新路径历史，重新加载文件列表
    pathHistory.value.push({id: file.id, name: file.name})
    currentParentId.value = file.id
    await loadFileList(file.id)
  } else {
    // 预览文件：传入id获取预签名链接并在新窗口打开
    try {
      const res = await axios.get(`${API_BASE_URL}/api/enterprise/preview`, {
        params: {
          id: file.id,
          driveId: driveId.value
        },
        ...getAuthConfig()
      })
      if (res.data.code === 200 && res.data.data) {
        const previewUrl = res.data.data
        // 在新窗口打开预签名链接进行预览
        window.open(previewUrl, '_blank')
      } else {
        ElMessage.error(res.data.msg || '获取预览链接失败')
      }
    } catch (error) {
      console.error('获取预览链接失败:', error)
      ElMessage.error('获取预览链接失败')
    }
  }
}

// 面包屑导航 - 返回根目录
const goToRoot = () => {
  pathHistory.value = []
  currentParentId.value = 0
  loadFileList(0)
}

// 面包屑导航 - 点击某个路径层级
const goToPath = (index) => {
  // 保留从0到index的路径，删除后面的
  pathHistory.value = pathHistory.value.slice(0, index + 1)
  const targetId = pathHistory.value.length > 0 ? pathHistory.value[pathHistory.value.length - 1].id : 0
  currentParentId.value = targetId
  loadFileList(targetId)
}

// 初始化加载
onMounted(() => {
  loadFileList()
})

// 上传
const handleUpload = () => {
  fileInputRef.value?.click()
}

// 处理文件选择
const handleFileChange = async (event) => {
  const files = Array.from(event.target.files)
  if (files.length === 0) return

  // 检查是否选择了文件夹
  for (const file of files) {
    if (file.webkitRelativePath && file.webkitRelativePath.includes('/')) {
      ElMessage.warning('暂不支持上传文件夹，请选择文件')
      event.target.value = ''
      return
    }
  }

  // 检查driveId
  if (!driveId.value) {
    ElMessage.error('未获取到 driveId')
    event.target.value = ''
    return
  }

  // 为每个文件创建上传任务
  const fileTaskMap = new Map()
  for (const file of files) {
    const taskId = uploadStore.addTask(file)
    fileTaskMap.set(file, taskId)
  }

  // 显示上传面板
  uploadStore.showUploadPanel()

  // 计算每个文件的SHA256和分片信息
  ElMessage.info('正在准备上传，计算文件校验值...')

  const uploadArgs = []

  for (const file of files) {
    const taskId = fileTaskMap.get(file)
    uploadStore.startTask(taskId)

    try {
      const sha256 = await calculateSHA256(file)
      const totalChunks = calculateTotalChunks(file.size)

      uploadArgs.push({
        entryName: file.name,
        sha256: sha256,
        fileSize: file.size,
        totalChunks: totalChunks,
        mimeType: file.type || 'application/octet-stream'
      })

      // 将taskId和sha256关联
      const task = uploadStore.uploadTasks.find(t => t.id === taskId)
      if (task) {
        task.sha256 = sha256
      }
    } catch (error) {
      console.error(`计算文件 "${file.name}" SHA256失败:`, error)
      uploadStore.failTask(taskId, '文件处理失败')
    }
  }

  // 清空input，允许再次选择相同的文件
  event.target.value = ''

  if (uploadArgs.length === 0) {
    ElMessage.warning('没有可上传的文件')
    return
  }

  // 调用initUpload接口
  try {
    const initResponse = await axios.post(`${API_BASE_URL}/api/enterprise/init-upload`, {
      driveId: driveId.value,
      parentId: currentParentId.value,
      argList: uploadArgs
    }, getAuthConfig())

    if (initResponse.data.code !== 200) {
      ElMessage.error(initResponse.data.msg || '初始化上传失败')
      // 将所有任务标记为失败
      uploadArgs.forEach(arg => {
        const task = uploadStore.uploadTasks.find(t => t.sha256 === arg.sha256)
        if (task) {
          uploadStore.failTask(task.id, '初始化上传失败')
        }
      })
      return
    }

    const initResult = initResponse.data.data
    const viewList = initResult.viewList || []

    // 处理每个文件的上传
    for (const view of viewList) {
      const file = files.find(f => {
        const task = uploadStore.uploadTasks.find(t => t.sha256 === view.sha256)
        return task !== undefined
      })

      const task = uploadStore.uploadTasks.find(t => t.sha256 === view.sha256)
      if (!file || !task) {
        console.error(`找不到文件: ${view.entryName}`)
        continue
      }

      if (!view.success) {
        uploadStore.failTask(task.id, view.message || '初始化失败')
        continue
      }

      // 根据返回的上传类型执行相应的上传逻辑
      await uploadSingleFile(file, view, task.id)
    }

  } catch (error) {
    console.error('初始化上传失败:', error)
    ElMessage.error('上传初始化失败，请重试')
    // 将所有未完成的任务标记为失败
    uploadStore.uploadTasks
        .filter(t => t.status === 'uploading' || t.status === 'waiting')
        .forEach(task => uploadStore.failTask(task.id, '上传初始化失败'))
  }
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
    const response = await axios.post(`${API_BASE_URL}/api/enterprise/create`, {
      driveId: driveId.value,
      parentId: currentParentId.value,
      folderName: newFolderName.value.trim()
    }, getAuthConfig())

    if (response.data.code === 200) {
      createFolderVisible.value = false
      newFolderName.value = ''
      ElMessage.success('创建成功')
      // 重新加载文件列表
      loadFileList(currentParentId.value)
    } else {
      ElMessage.error(response.data.msg || '创建失败')
    }
  } catch (error) {
    console.error('创建文件夹失败:', error)
    ElMessage.error('创建失败')
  }
}

// 批量下载
const handleBatchDownload = async () => {
  if (selectedFiles.value.length === 0) return

  try {
    const ids = selectedFiles.value.map(f => f.id)
    const response = await axios.post(`${API_BASE_URL}/api/enterprise/download`, {
      ids: ids
    }, {
      ...getAuthConfig(),
      responseType: 'blob'
    })

    // 从响应头中获取文件名
    const contentDisposition = response.headers['content-disposition']
    let filename = 'download'
    if (contentDisposition) {
      const encodedMatch = contentDisposition.match(/filename\*=UTF-8''([^;]+)/)
      if (encodedMatch) {
        filename = decodeURIComponent(encodedMatch[1])
      } else {
        // 回退到 filename="xxx"（英文文件名）
        const plainMatch = contentDisposition.match(/filename="(.+?)"/)
        if (plainMatch) {
          filename = plainMatch[1]
        }
      }
    }

    // 创建下载链接
    const blob = new Blob([response.data])
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(downloadUrl)

    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败')
  }
}

// 分享
const handleShare = () => {
  if (selectedFiles.value.length !== 1) {
    ElMessage.warning('只能选择一条记录进行分享')
    return
  }
  // 初始化分享表单
  shareForm.value = {
    linkName: selectedFiles.value[0].name,
    linkType: 1,
    accessCode: '',
    expireTime: null
  }
  shareVisible.value = true
}

// 生成分享链接
const generateShareLink = async () => {
  if (!shareForm.value.linkName.trim()) {
    ElMessage.warning('请输入链接名称')
    return
  }
  if (!shareForm.value.expireTime) {
    ElMessage.warning('请选择过期时间')
    return
  }
  if (shareForm.value.linkType === 2 && !shareForm.value.accessCode.trim()) {
    ElMessage.warning('请输入提取码')
    return
  }

  try {
    // 构造创建分享链接的请求数据
    const createData = {
      id: selectedFiles.value[0].id,
      driveId: driveId.value,
      linkName: shareForm.value.linkName,
      linkType: shareForm.value.linkType,
      accessCode: shareForm.value.linkType === 2 ? shareForm.value.accessCode : null,
      expireTime: shareForm.value.expireTime
    }

    const response = await axios.post(`${API_BASE_URL}/api/enterprise/share`, createData, getAuthConfig())

    if (response.data.code === 200) {
      ElMessage.success('分享链接已生成')
    } else {
      ElMessage.error(response.data.msg || '生成分享链接失败')
    }
  } catch (error) {
    console.error('生成分享链接失败:', error)
    ElMessage.error('生成分享链接失败')
  }
}

// 更多操作命令
const handleCommand = async (command, row) => {
  switch (command) {
    case 'download':
      try {
        const response = await axios.post(`${API_BASE_URL}/api/enterprise/download`, {
          ids: [row.id]
        }, {
          ...getAuthConfig(),
          responseType: 'blob'
        })

        // 从响应头中获取文件名
        const contentDisposition = response.headers['content-disposition']
        let filename = row.name || 'download'
        if (contentDisposition) {
          const encodedMatch = contentDisposition.match(/filename\*=UTF-8''([^;]+)/)
          if (encodedMatch) {
            filename = decodeURIComponent(encodedMatch[1])
          } else {
            // 回退到 filename="xxx"（英文文件名）
            const plainMatch = contentDisposition.match(/filename="(.+?)"/)
            if (plainMatch) {
              filename = plainMatch[1]
            }
          }
        }

        // 创建下载链接
        const blob = new Blob([response.data])
        const downloadUrl = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = downloadUrl
        link.download = filename
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(downloadUrl)

        ElMessage.success('下载成功')
      } catch (error) {
        console.error('下载失败:', error)
        ElMessage.error('下载失败')
      }
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

    const response = await axios.post(`${API_BASE_URL}/api/enterprise/rename`, {
      id: row.id,
      driveId: driveId.value,
      newEntryName: value
    }, getAuthConfig())

    if (response.data.code === 200) {
      ElMessage.success('重命名成功')
      loadFileList(currentParentId.value)
    } else {
      ElMessage.error(response.data.msg || '重命名失败')
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
      const response = await axios.post(`${API_BASE_URL}/api/enterprise/delete`, {
        driveId: driveId.value,
        ids: [row.id]
      }, getAuthConfig())
      if (response.data.code === 200) {
        ElMessage.success('删除成功')
        loadFileList(currentParentId.value)
      } else {
        ElMessage.error(response.data.msg || '删除失败')
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
    const res = await axios.get(`${API_BASE_URL}/api/enterprise/folder`, {
      ...getAuthConfig(),
      params: {
        driveId: driveId.value
      }
    })
    if (res.data.code === 200) {
      folderTreeData.value = res.data.data || []
    } else {
      folderTreeData.value = []
      ElMessage.error(res.data.msg || '加载文件夹列表失败')
    }
  } catch (error) {
    console.error('加载文件夹树失败:', error)
    folderTreeData.value = []
    ElMessage.error('加载文件夹列表失败')
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
    const response = await axios.post(`${API_BASE_URL}/api/enterprise/move`, {
      ids: ids,
      driveId: driveId.value,
      targetId: selectedTargetFolder.value.id
    }, getAuthConfig())

    if (response.data.code === 200) {
      ElMessage.success(`已将 ${selectedFiles.value.length} 个文件移动到 "${selectedTargetFolder.value.name}"`)
      moveVisible.value = false
      selectedTargetFolder.value = null
      selectedFiles.value = []
      loadFileList(currentParentId.value)
    } else {
      ElMessage.error(response.data.msg || '移动失败')
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
    const response = await axios.post(`${API_BASE_URL}/api/enterprise/copy`, {
      id: selectedFile.id,
      driveId: driveId.value,
      targetId: selectedTargetFolder.value.id
    }, getAuthConfig())

    if (response.data.code === 200) {
      ElMessage.success(`已将 "${selectedFile.name}" 复制到 "${selectedTargetFolder.value.name}"`)
      copyVisible.value = false
      selectedTargetFolder.value = null
      selectedFiles.value = []
      loadFileList(currentParentId.value)
    } else {
      ElMessage.error(response.data.msg || '复制失败')
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

// 上传相关常量
const CHUNK_SIZE = 10 * 1024 * 1024 // 10MB 分片大小

// 计算文件的SHA256值
const calculateSHA256 = async (file) => {
  const arrayBuffer = await file.arrayBuffer()
  const hashBuffer = await crypto.subtle.digest('SHA-256', arrayBuffer)
  const hashArray = Array.from(new Uint8Array(hashBuffer))
  const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
  return hashHex
}

// 计算分片数量
const calculateTotalChunks = (fileSize) => {
  return Math.ceil(fileSize / CHUNK_SIZE)
}

// 获取文件分片
const getFileChunk = (file, start, end) => {
  return file.slice(start, end)
}

// 上传单个文件
const uploadSingleFile = async (file, initView, taskId) => {
  // 1. 秒传成功 (isSkip = true)
  if (initView.isSkip) {
    uploadStore.markAsSkipped(taskId)
    ElMessage.success(`文件 "${file.name}" 秒传成功`)
    // 刷新文件列表
    loadFileList(currentParentId.value)
    return
  }

  // 2. 小文件直传 (uploadUrl 不为 null)
  if (initView.uploadUrl) {
    await uploadSmallFile(file, initView, taskId)
    return
  }

  // 3. 大文件分片上传 (isMultipart = true)
  if (initView.isMultipart) {
    await uploadLargeFile(file, initView, taskId)
    return
  }
}

// 上传小文件（直接上传到MinIO）
const uploadSmallFile = async (file, initView, taskId) => {
  try {
    // 使用预签名URL直接上传到MinIO
    const response = await axios.put(initView.uploadUrl, file, {
      headers: {
        'Content-Type': file.type || 'application/octet-stream'
      },
      onUploadProgress: (progressEvent) => {
        // 更新上传进度
        uploadStore.updateProgress(taskId, progressEvent.loaded, progressEvent.total)
      }
    })

    // 上传完成后通知后端
    const simpleUploadRes = await axios.post(`${API_BASE_URL}/api/enterprise/simple-upload`, {
      sha256: initView.sha256
    }, getAuthConfig())

    if (simpleUploadRes.data.code === 200) {
      uploadStore.completeTask(taskId)
      ElMessage.success(`文件 "${file.name}" 上传成功`)
      // 刷新文件列表
      loadFileList(currentParentId.value)
    } else {
      uploadStore.failTask(taskId, simpleUploadRes.data.msg || '上传记录失败')
    }
  } catch (error) {
    console.error('小文件上传失败:', error)
    uploadStore.failTask(taskId, '上传失败')
  }
}

// 上传大文件（分片上传）
const uploadLargeFile = async (file, initView, taskId) => {
  try {
    const totalChunks = calculateTotalChunks(file.size)
    const chunkUrls = initView.chunkUrls || []
    const uploadedChunksSet = new Set(initView.uploadedChunks || [])

    // 设置总分片数
    const task = uploadStore.uploadTasks.find(t => t.id === taskId)
    if (task) {
      task.totalChunks = totalChunks
    }

    // 批量上报的数组
    const chunkReportBatch = []

    // 上传每个分片
    for (let i = 1; i <= totalChunks; i++) {
      // 如果已经上传过了，跳过
      if (uploadedChunksSet.has(i)) {
        continue
      }

      const start = (i - 1) * CHUNK_SIZE
      const end = Math.min(i * CHUNK_SIZE, file.size)
      const chunk = getFileChunk(file, start, end)

      // 获取分片上传URL (chunkUrls从1开始，所以使用i)
      const chunkUrl = chunkUrls[i - 1]
      if (!chunkUrl) {
        console.error(`分片 ${i} 的上传URL不存在`)
        continue
      }

      // 上传分片到MinIO
      const response = await axios.put(chunkUrl, chunk, {
        headers: {
          'Content-Type': 'application/octet-stream'
        }
      })

      // 获取ETag
      const etag = response.headers.etag || response.headers.ETag

      // 添加到批量上报数组
      chunkReportBatch.push({
        sha256: initView.sha256,
        chunkNumber: String(i),
        etag: etag ? etag.replace(/"/g, '') : ''
      })

      // 更新进度
      uploadedChunksSet.add(i)
      uploadStore.updateChunkProgress(taskId, uploadedChunksSet.size, totalChunks)

      // 每5个分片批量上报
      if (chunkReportBatch.length >= 5) {
        await reportUploadedChunks(chunkReportBatch)
        chunkReportBatch.length = 0 // 清空数组
      }
    }

    // 循环结束后，上报剩余的分片（如果有）
    if (chunkReportBatch.length > 0) {
      await reportUploadedChunks(chunkReportBatch)
    }

    // 所有分片上传完成，调用合并接口
    await mergeChunks(initView.sha256, file.name, taskId)

  } catch (error) {
    console.error('大文件上传失败:', error)
    uploadStore.failTask(taskId, '上传失败')
  }
}

// 批量上报已上传的分片
const reportUploadedChunks = async (chunks) => {
  if (chunks.length === 0) return

  try {
    const response = await axios.post(`${API_BASE_URL}/api/enterprise/upload-chunk`, {
      argList: chunks
    }, getAuthConfig())

    if (response.data.code !== 200) {
      console.error('批量上报分片失败:', response.data.msg)
    }
  } catch (error) {
    console.error('批量上报分片失败:', error)
  }
}

// 合并分片
const mergeChunks = async (sha256, fileName, taskId) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/api/enterprise/merge-chunks`, {
      sha256: sha256
    }, getAuthConfig())

    if (response.data.code === 200) {
      uploadStore.completeTask(taskId)
      ElMessage.success(`文件 "${fileName}" 上传成功`)
      // 刷新文件列表
      loadFileList(currentParentId.value)
    } else {
      uploadStore.failTask(taskId, response.data.msg || '合并分片失败')
    }
  } catch (error) {
    console.error('合并分片失败:', error)
    uploadStore.failTask(taskId, '合并分片失败')
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
      const response = await axios.post(`${API_BASE_URL}/api/enterprise/delete`, {
        driveId: driveId.value,
        ids: ids
      }, getAuthConfig())
      if (response.data.code === 200) {
        ElMessage.success('删除成功')
        selectedFiles.value = []
        loadFileList(currentParentId.value)
      } else {
        ElMessage.error(response.data.msg || '删除失败')
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
