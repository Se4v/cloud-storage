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
                <div class="flex items-center gap-3 py-1 group cursor-pointer pr-4">
                  <!-- File Icon -->
                  <div class="w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0 text-white bg-slate-400">
                    <el-icon :size="20">
                      <Document v-if="row.type === 'document'" />
                      <Picture v-else-if="row.type === 'image'" />
                      <Folder v-else-if="row.type === 'folder'" />
                      <Files v-else />
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
                <span class="text-sm text-slate-600 tabular-nums">{{ row.size || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="date" label="修改日期" width="180">
               <template #default="{ row }">
                <div class="flex items-center gap-1.5 text-sm text-slate-600">
                  <span class="whitespace-nowrap">{{ row.date }}</span>
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
  Folder,
  Picture,
  Files
} from '@element-plus/icons-vue'

const fileList = ref([
  {
    id: 1,
    name: 'Idea 版本控制配置.docx',
    size: '687.5KB',
    date: '2026-01-11 18:28',
    type: 'document'
  }
])

const selectedFiles = ref([])

const totalSize = computed(() => {
  if (fileList.value.length === 1) return fileList.value[0].size
  return '...'
})

const handleSelectionChange = (val) => {
  selectedFiles.value = val
}

const handleDownload = () => {
  const filesToDownload = selectedFiles.value.length > 0 ? selectedFiles.value : fileList.value
  console.log('Downloading:', filesToDownload)
}

const downloadFile = (file) => {
  console.log('Downloading single file:', file)
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