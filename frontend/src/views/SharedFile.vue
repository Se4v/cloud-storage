<template>
  <div class="h-full flex flex-col bg-[#f5f6f7] overflow-hidden">
    <!-- 1. 全局顶部 Logo 栏 (类似夸克顶栏) -->
    <div class="h-16 bg-white px-8 flex items-center justify-between shrink-0 z-20 shadow-[0_1px_2px_rgba(0,0,0,0.03)]">
      <div class="flex items-center gap-2">
        <div class="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center">
          <el-icon class="text-white" :size="20"><Cloudy /></el-icon>
        </div>
        <div class="flex items-baseline gap-1">
          <span class="text-xl font-bold text-slate-900 tracking-tight">夸克</span>
          <span class="text-lg font-medium text-slate-700">夸克网盘</span>
        </div>
      </div>
      <div class="flex items-center gap-4">
        <button class="px-6 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors">
          登录账号
        </button>
        <button class="px-6 py-2 bg-[#ffefd5] text-[#b8860b] rounded-lg text-sm font-medium hover:bg-[#ffebcd] transition-colors">
          会员中心
        </button>
      </div>
    </div>

    <!-- 主容器 -->
    <div class="flex-1 overflow-auto">
      <div class="max-w-[1200px] mx-auto p-6 space-y-4">
        
        <!-- 2. 分享信息卡片 (下载按钮与用户名并列) -->
        <div class="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
          <div class="flex items-start justify-between">
            <!-- 左侧：头像、名称、统计信息 -->
            <div class="flex gap-4">
              <div class="w-14 h-14 rounded-full bg-yellow-400 flex items-center justify-center text-white text-xl font-bold shadow-inner">
                {{ shareInfo.shareUserName?.charAt(0) }}
              </div>
              <div class="space-y-1">
                <div class="flex items-center gap-2">
                  <h1 class="text-lg font-bold text-slate-800">{{ shareInfo.shareUserName }}的分享</h1>
                  <span class="bg-orange-100 text-orange-600 text-[10px] px-1.5 py-0.5 rounded-sm font-bold">SVIP</span>
                </div>
                <div class="flex items-center gap-4 text-xs text-slate-400">
                  <span>共 {{ fileList.length }} 个文件</span>
                  <span>{{ formatSize(shareInfo.totalSize || 0) }}</span>
                  <span class="flex items-center gap-1"><el-icon><Clock /></el-icon> 永久有效</span>
                  <button class="hover:text-blue-600">举报</button>
                </div>
              </div>
            </div>

            <!-- 右侧：搜索框与下载按钮并列显示 -->
            <div class="flex items-center gap-4">
              <!-- 搜索框 -->
              <div class="relative w-64 hidden md:block">
                <el-input
                  v-model="searchQuery"
                  placeholder="搜索链接中的文件"
                  class="quark-search"
                  clearable
                >
                  <template #prefix>
                    <el-icon class="text-slate-400"><Search /></el-icon>
                  </template>
                </el-input>
              </div>
              
              <!-- 客户端按钮 (模拟图片) -->
              <button class="flex items-center gap-2 px-4 py-2 border border-slate-200 text-slate-600 rounded-lg text-sm hover:bg-slate-50 transition-colors">
                <el-icon><Monitor /></el-icon>
                去客户端查看
              </button>

              <!-- 下载按钮 (侧重显示) -->
              <button
                @click="handleBatchDownload"
                class="flex items-center gap-2 px-6 py-2.5 bg-blue-600 text-white rounded-lg text-sm font-bold hover:bg-blue-700 active:scale-95 transition-all shadow-md shadow-blue-100"
              >
                <el-icon :size="18"><Download /></el-icon>
                下载
              </button>
            </div>
          </div>
        </div>

        <!-- 3. 文件展示区域 -->
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm flex flex-col min-h-[500px]">
          <!-- 列表工具栏 -->
          <div class="px-6 py-4 flex items-center justify-between">
            <div class="text-sm font-bold text-slate-800">全部文件</div>
            <div class="flex items-center gap-2 text-slate-400">
              <el-icon class="p-2 cursor-pointer hover:bg-slate-50 rounded"><Sort /></el-icon>
              <el-icon class="p-2 cursor-pointer hover:bg-slate-50 rounded"><List /></el-icon>
              <el-icon class="p-2 cursor-pointer hover:bg-slate-50 rounded"><Menu /></el-icon>
            </div>
          </div>

          <!-- 表格主体 (夸克风格) -->
          <el-table
            ref="tableRef"
            v-loading="loading"
            :data="fileList"
            class="quark-table"
            @selection-change="handleSelectionChange"
            @row-click="handleRowClick"
          >
            <el-table-column type="selection" width="55" align="center" />
            
            <el-table-column label="文件名" min-width="400">
              <template #default="{ row }">
                <div class="flex items-center gap-3 py-1 group">
                  <!-- 文件/文件夹图标 -->
                  <div class="w-10 h-10 flex items-center justify-center shrink-0">
                    <img v-if="row.type === 2" src="https://img.icons8.com/color/48/folder-invoices.png" class="w-8 h-8" />
                    <div v-else 
                      class="w-8 h-8 rounded flex items-center justify-center text-white"
                      :class="getFileIconBgClass(row.type, row.name)"
                    >
                      <el-icon :size="16">
                        <component :is="getFileIcon(row.type, row.name)" />
                      </el-icon>
                    </div>
                  </div>
                  <span class="text-sm text-slate-700 font-medium truncate group-hover:text-blue-600 transition-colors">{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="大小" width="160">
              <template #default="{ row }">
                <span class="text-sm text-slate-400 tabular-nums">
                  {{ row.type === 2 ? '1项' : formatSize(row.size) }}
                </span>
              </template>
            </el-table-column>

            <el-table-column label="修改日期" width="200">
              <template #default="{ row }">
                <span class="text-sm text-slate-400 tabular-nums">{{ row.updateTime }}</span>
              </template>
            </el-table-column>
          </el-table>

          <!-- 空状态 -->
          <div v-if="!loading && fileList.length === 0" class="flex-1 flex flex-col items-center justify-center text-slate-300">
            <el-icon :size="64"><FolderOpened /></el-icon>
            <p class="mt-2 text-sm">暂无内容</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 4. 底部固定操作栏 -->
    <div class="h-20 bg-white border-t border-slate-100 px-8 flex items-center justify-between shrink-0 z-20 shadow-[0_-1px_3px_rgba(0,0,0,0.02)]">
      <div class="flex items-center gap-3">
        <div class="w-[300px] px-4 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-500 flex items-center justify-between">
          <span>转存至：<span class="font-bold text-slate-700">我的网盘</span></span>
          <el-icon class="cursor-pointer hover:text-blue-600"><FolderOpened /></el-icon>
        </div>
      </div>
      <div class="flex items-center gap-3">
        <div v-if="selectedFiles.length > 0" class="text-sm text-slate-400 mr-4">
          已选择 {{ selectedFiles.length }} 个文件
        </div>
        <button
          @click="handleBatchDownload"
          class="flex items-center gap-2 px-10 py-2.5 border border-slate-200 text-slate-700 rounded-lg text-sm font-bold hover:bg-slate-50 transition-all"
        >
          <el-icon :size="18"><Download /></el-icon>
          下载
        </button>
        <!-- 已移除“保存到网盘”按钮 -->
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Download, Search, Clock, FolderOpened, Menu, Sort, List, Monitor, Cloudy,
  Document, Picture, VideoCamera, Headset, Box, Files
} from '@element-plus/icons-vue'

const route = useRoute()
const loading = ref(false)
const tableRef = ref(null)
const searchQuery = ref('')
const selectedFiles = ref([])

// 分享信息 (Mock 数据参考图片)
const shareInfo = ref({
  shareId: '',
  shareUserName: '图欧*公益资源',
  totalSize: 99213742080, // 92.4G
})

// 文件列表 (Mock 数据参考图片)
const fileList = ref([
  { id: 1, name: '科学探索馆', type: 2, size: 0, updateTime: '2024-11-04 11:22' },
  { id: 2, name: '项目资源包.zip', type: 1, size: 1024 * 1024 * 1024 * 2.5, updateTime: '2024-11-03 14:30' },
  { id: 3, name: '年度总结.pdf', type: 1, size: 1024 * 1024 * 12, updateTime: '2024-11-02 10:15' }
])

// 选择处理
const handleSelectionChange = (selection) => {
  selectedFiles.value = selection
}

const handleRowClick = (row) => {
  tableRef.value?.toggleRowSelection(row)
}

// 下载处理
const handleBatchDownload = () => {
  if (selectedFiles.value.length === 0) {
    ElMessage.warning('请选择要下载的文件')
    return
  }
  ElMessage.success(`正在为您准备 ${selectedFiles.value.length} 个文件的下载任务...`)
}

// 辅助方法
const getFileIcon = (type, name) => {
  if (type === 2) return FolderOpened
  const ext = name.split('.').pop().toLowerCase()
  if (['jpg','png','gif','jpeg'].includes(ext)) return Picture
  if (['mp4','mov','avi'].includes(ext)) return VideoCamera
  if (['zip','7z','rar'].includes(ext)) return Box
  return Document
}

const getFileIconBgClass = (type, name) => {
  if (type === 2) return 'bg-[#7ec1ff]'
  const ext = name.split('.').pop().toLowerCase()
  if (ext === 'pdf') return 'bg-[#ff7e7e]'
  if (ext === 'zip' || ext === 'rar') return 'bg-[#ffb97e]'
  if (ext === 'mp4') return 'bg-[#ac86ff]'
  return 'bg-[#94a3b8]'
}

const formatSize = (bytes) => {
  if (bytes === 0 || !bytes) return '0 B'
  const k = 1024, sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

onMounted(() => {
  // 模拟加载
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
})
</script>

<style scoped>
/* 搜索框夸克样式 */
:deep(.quark-search .el-input__wrapper) {
  background-color: #f5f6f7;
  box-shadow: none !important;
  border-radius: 8px;
  border: 1px solid transparent;
  transition: all 0.2s;
  height: 40px;
}
:deep(.quark-search .el-input__wrapper.is-focus) {
  background-color: #fff;
  border-color: #2563eb;
}

/* 表格样式重构 (夸克简约风格) */
:deep(.quark-table) {
  --el-table-header-bg-color: transparent;
  --el-table-border-color: transparent;
  --el-table-row-hover-bg-color: #f8fafc;
}

:deep(.quark-table .el-table__header th) {
  color: #94a3b8;
  font-weight: normal;
  font-size: 13px;
  height: 50px;
}

:deep(.quark-table .el-table__cell) {
  border-bottom: 1px solid #f8fafc;
}

:deep(.quark-table .el-checkbox__inner) {
  border-radius: 4px;
  border-color: #e2e8f0;
}

:deep(.quark-table .el-table__row) {
  cursor: pointer;
}

:deep(.quark-table::before) {
  display: none;
}

/* 隐藏滚动条 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 10px;
}
</style>
