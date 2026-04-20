<template>
  <div class="h-full flex flex-col bg-slate-50 font-sans min-h-screen">
    <div class="bg-white px-8 py-4 border-b border-slate-200 flex-shrink-0 z-10 shadow-sm">
      <div class="flex items-center gap-2 cursor-pointer">
        <div class="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center">
          <div class="w-3.5 h-3.5 bg-white rounded-full"></div>
        </div>
        <span class="font-bold text-[17px] tracking-wide text-slate-900">云盘分享</span>
      </div>
    </div>

    <div class="flex-1 overflow-auto p-6 w-full max-w-[1200px] mx-auto">
      <!-- 提取码弹窗 -->
      <div v-if="showAccessCodeDialog" class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden min-h-[400px] flex flex-col items-center justify-center p-8">
        <div class="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mb-6">
          <el-icon :size="32" class="text-blue-600"><Lock /></el-icon>
        </div>
        <h2 class="text-xl font-semibold text-slate-900 mb-2">加密分享</h2>
        <p class="text-sm text-slate-500 mb-6">该分享需要输入提取码才能访问</p>
        <div class="flex items-center gap-3 w-full max-w-sm">
          <el-input
            v-model="accessCode"
            placeholder="请输入提取码"
            maxlength="6"
            show-word-limit
            class="flex-1"
            @keyup.enter="verifyAccessCode"
          />
          <button
            @click="verifyAccessCode"
            class="inline-flex items-center gap-2 px-6 py-2 bg-blue-600 text-white text-sm font-medium rounded-md hover:bg-blue-700 transition-colors shadow-sm"
          >
            确认
          </button>
        </div>
      </div>

      <!-- 文件列表 -->
      <div v-else class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden min-h-[600px] flex flex-col">

        <div class="px-6 py-5 border-b border-slate-200 flex flex-wrap gap-4 items-center justify-between bg-white">
          <div class="flex items-center gap-4">
            <el-avatar 
              :size="48" 
              src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" 
              class="border border-slate-200 bg-slate-50 text-slate-300" 
            />
            <div>
              <div class="text-[16px] font-semibold text-slate-900 flex items-center gap-2">
                <span>{{ shareInfo.username }}的分享</span>
              </div>
              <div class="text-[13px] text-slate-500 mt-1 flex items-center gap-3">
                <span>{{ formatExpireTime }}</span>
              </div>
            </div>
          </div>

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
import axios from 'axios'
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { ref, computed, onMounted } from 'vue'
import { Download, Document, Folder, Lock } from '@element-plus/icons-vue'

const API_BASE_URL = 'http://localhost:8080'

const route = useRoute()

// 面包屑路径历史
const pathHistory = ref([])
// 根文件夹ID（页面加载时保存最初的文件夹ID）
const rootFolderId = ref(null)
// 当前所在目录的 folderId（根目录为 null 或 0）
const currentFolderId = ref(null)
// 文件列表
const fileList = ref([])
const selectedFiles = ref([])

// 分享信息（从后端加载）
const shareInfo = ref({
  username: '', // 分享者名称
  expireTime: '',  // 过期时间
  linkType: 1 // 链接类型：1-公开链接，2-加密链接
})

// 是否显示提取码弹窗
const showAccessCodeDialog = ref(false)
// 用户输入的提取码
const accessCode = ref('')
// 当前linkKey
const currentLinkKey = ref('')

// 格式化过期时间显示
const formatExpireTime = computed(() => {
  if (!shareInfo.value.expireTime) return ''
  const expireDate = new Date(shareInfo.value.expireTime)
  const now = new Date()
  const diffMs = expireDate - now
  if (diffMs <= 0) return '已过期'
  const diffDays = Math.ceil(diffMs / (1000 * 60 * 60 * 24))
  if (diffDays === 1) return '1天后过期'
  return `${diffDays}天后过期`
})

const handleSelectionChange = (val) => {
  selectedFiles.value = val
}

// 双击行事件
const handleRowDblClick = (row) => {
  handleOpenFile(row)
}

// 打开文件/文件夹（仅处理文件夹进入）
const handleOpenFile = (file) => {
  if (file.type === 2) {
    // 进入文件夹：更新路径历史，加载文件夹内容
    pathHistory.value.push({ id: file.id, name: file.name })
    currentFolderId.value = file.id
    loadFileList(null, file.id)
  }
}

// 加载文件列表
const loadFileList = async (linkKey = null, parentId = null) => {
  try {
    const { data: res } = await axios.get(`${API_BASE_URL}/api/share/file`, {
      params: { linkKey: linkKey, parentId: parentId }
    })
    if (res.code !== 200) {
      ElMessage.error(res.msg || '加载文件列表失败')
      return
    }
    fileList.value = res.data || []
    selectedFiles.value = []
  } catch (error) {
    console.error('加载文件列表失败:', error)
    ElMessage.error(error.message || '网络异常')
    fileList.value = []
  }
}

// 加载分享信息
const loadShareInfo = async (linkKey) => {
  try {
    const { data: res } = await axios.get(`${API_BASE_URL}/api/share/info`, { params: { linkKey: linkKey } })
    if (res.code !== 200) {
      ElMessage.error(res.msg || '加载失败')
      shareInfo.value = { username: '', expireTime: '', linkType: 1 }
      return
    }
    shareInfo.value = res.data || { username: '', expireTime: '', linkType: 1 }
  } catch (error) {
    console.error('加载分享信息失败:', error)
    ElMessage.error(error.message || '网络异常')
    shareInfo.value = { username: '', expireTime: '', linkType: 1 }
  }
}

// 面包屑导航 - 返回根目录
const goToRoot = () => {
  pathHistory.value = []
  currentFolderId.value = rootFolderId.value
  loadFileList(null, rootFolderId.value)
}

// 面包屑导航 - 点击某个路径层级
const goToPath = (index) => {
  // 保留从0到index的路径，删除后面的
  pathHistory.value = pathHistory.value.slice(0, index + 1)
  const targetId = pathHistory.value.length > 0 ? pathHistory.value[pathHistory.value.length - 1].id : rootFolderId.value
  currentFolderId.value = targetId
  loadFileList(null, targetId)
}

// 验证提取码
const verifyAccessCode = async () => {
  if (!accessCode.value.trim()) {
    ElMessage.warning('请输入提取码')
    return
  }
  try {
    const { data: res } = await axios.post(`${API_BASE_URL}/api/share/check`, {
      linkKey: currentLinkKey.value,
      accessCode: accessCode.value.trim()
    })
    if (res.code !== 200) {
      ElMessage.error(res.msg || '提取码错误')
      return
    }
    // 验证成功，隐藏弹窗并加载文件列表
    showAccessCodeDialog.value = false
    await loadFileList(currentLinkKey.value, null)
    // 取第一个文件夹的id作为rootFolderId
    if (fileList.value.length > 0 && fileList.value[0].type === 2) {
      rootFolderId.value = fileList.value[0].id
    }
  } catch (error) {
    console.error('验证提取码失败:', error)
    ElMessage.error(error.message || '网络异常')
  }
}

// 页面初始化
onMounted(async () => {
  const linkKey = route.params.linkKey
  currentLinkKey.value = linkKey
  // 先加载分享信息
  await loadShareInfo(linkKey)
  
  // 根据linkType决定后续操作
  if (shareInfo.value.linkType === 1) {
    // 公开链接，直接加载文件列表
    await loadFileList(linkKey, null)
    // 取第一个文件夹的id作为rootFolderId
    if (fileList.value.length > 0 && fileList.value[0].type === 2) {
      rootFolderId.value = fileList.value[0].id
    }
  } else if (shareInfo.value.linkType === 2) {
    // 加密链接，显示提取码弹窗
    showAccessCodeDialog.value = true
  }
})

const handleDownload = async () => {
  const filesToDownload = selectedFiles.value.length ? selectedFiles.value : fileList.value
  if (!filesToDownload.length) return

  try {
    const ids = filesToDownload.map(f => f.id)
    const res = await axios.post(`${API_BASE_URL}/api/share/download`, { ids: ids }, { responseType: 'blob' })

    // 从响应头中获取文件名
    const disposition = res.headers['content-disposition']
    const filenameRegex = /filename\*=UTF-8''([^;]+)|filename="([^"]+)"/
    const match = disposition?.match(filenameRegex)
    const filename = match?.[1] ? decodeURIComponent(match[1]) : match?.[2] || 'download'

    // 创建下载链接
    const url = window.URL.createObjectURL(res.data)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败')
  }
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