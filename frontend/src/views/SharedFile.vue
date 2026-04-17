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
      <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden min-h-[600px] flex flex-col">

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
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import {
  Download,
  Document,
  Folder
} from '@element-plus/icons-vue'
import {ElMessage} from "element-plus";

const API_BASE_URL = 'http://localhost:8080'

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
  expireTime: ''  // 过期时间
})

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
    const res = await axios.get(`${API_BASE_URL}/api/share/file`, {
      params: {
        linkKey: linkKey.value,
        parentId: parentId
      }
    })
    if (res.data.code === 200) {
      fileList.value = res.data.data || []
      selectedFiles.value = []
    } else {
      ElMessage.error(res.data.msg || '加载文件列表失败')
    }
  } catch (error) {
    console.error('加载文件列表失败:', error)
    fileList.value = []
  }
}

// 加载分享信息
const loadShareInfo = async (linkKey = null) => {
  try {
    const res = await axios.get(`${API_BASE_URL}/api/share/info`, {
      params: {
        linkKey: linkKey.value
      }
    })
    if (res.data.code === 200) {
      shareInfo.value = res.data.data || {
        username: '',
        expireTime: ''
      }
    } else {
      ElMessage.error(res.data.msg || '加载失败')
    }
  } catch (error) {
    console.error('加载分享信息失败:', error)
    shareInfo.value = {
      username: '',
      expireTime: ''
    }
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

// 页面初始化
onMounted(async () => {
  // TODO: 从路由参数或其他方式获取linkKey等参数
  const linkKey = route.params.linkKey
  // 加载分享信息和根目录文件列表
  await Promise.all([
    loadShareInfo(linkKey),
    loadFileList(linkKey, null)
  ])
  // loadFileList完成后，取第一个文件夹的id作为rootFolderId
  if (fileList.value.length > 0 && fileList.value[0].type === 2) {
    rootFolderId.value = fileList.value[0].id
  }
})

const handleDownload = async () => {
  const filesToDownload = selectedFiles.value.length > 0 ? selectedFiles.value : fileList.value
  if (filesToDownload.length === 0) return

  try {
    const ids = filesToDownload.map(f => f.id)
    const response = await axios.post(`${API_BASE_URL}/api/share/download`, {
      ids: ids
    }, {
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