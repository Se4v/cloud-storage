<template>
  <div class="min-h-screen bg-[#f7f8fa] flex flex-col font-sans">
    <!-- Top Header -->
    <header class="h-14 bg-white flex items-center px-6 shadow-sm z-10">
      <div class="flex items-center gap-2 cursor-pointer">
        <div class="w-7 h-7 bg-blue-600 rounded-full flex items-center justify-center">
          <div class="w-3 h-3 bg-white rounded-full"></div>
        </div>
        <span class="font-bold text-[17px] tracking-wide text-gray-900">云盘分享</span>
      </div>
    </header>

    <!-- Main Content -->
    <main class="flex-1 w-full max-w-[1100px] mx-auto pt-6 px-4 pb-12">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden min-h-[600px] flex flex-col">
        
        <!-- User Info & Top Actions -->
        <div class="px-6 py-5 border-b border-gray-100 flex flex-wrap gap-4 items-center justify-between bg-white">
          <div class="flex items-center gap-4">
            <el-avatar 
              :size="48" 
              src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" 
              class="border border-gray-200 bg-gray-50 text-gray-300" 
            />
            <div>
              <div class="text-[16px] font-medium text-gray-900 flex items-center gap-2">
                <span>夸父*836的分享</span>
              </div>
              <div class="text-[13px] text-gray-500 mt-1 flex items-center gap-3">
                <span>共 {{ fileList.length }} 个文件 {{ totalSize }}</span>
                <span>1天后过期</span>
              </div>
            </div>
          </div>
          
          <!-- Download Button -->
          <div class="flex items-center gap-3">
            <el-button 
              type="primary" 
              class="!bg-blue-600 hover:!bg-blue-700 !border-none !rounded-lg !px-6 h-9 transition-all shadow-sm flex items-center gap-1.5"
              @click="handleDownload"
              :disabled="selectedFiles.length === 0 && fileList.length === 0"
            >
              <template #icon>
                <Download class="w-4 h-4" />
              </template>
              <span class="font-medium text-[13px]">下载</span>
            </el-button>
          </div>
        </div>

        <!-- File List Tools -->
        <div class="px-6 py-4 flex items-center justify-between bg-white">
          <div class="text-[14px] text-gray-700 font-medium">全部文件</div>
          <div class="flex items-center gap-3 text-gray-500">
            <div class="p-1.5 hover:bg-gray-100 rounded-md cursor-pointer transition-colors text-gray-500" title="排序">
              <Sort class="w-[18px] h-[18px]" />
            </div>
          </div>
        </div>

        <!-- File List Table -->
        <div class="flex-1 px-6 pb-6 bg-white">
          <el-table 
            :data="fileList" 
            style="width: 100%" 
            @selection-change="handleSelectionChange" 
            class="!border-none share-table" 
            :header-cell-style="{ background: '#fff', color: '#64748b', fontWeight: 500, fontSize: '13px', borderBottom: '1px solid #f1f5f9', padding: '10px 0' }" 
            :cell-style="{ borderBottom: '1px solid #f8fafc', padding: '12px 0' }"
          >
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column label="文件名" min-width="450">
              <template #default="{ row }">
                <div class="flex items-center gap-3 group cursor-pointer pr-4">
                  <!-- File Icon -->
                  <div class="w-8 h-8 shrink-0 flex items-center justify-center bg-blue-50 text-blue-600 rounded-lg">
                    <Document v-if="row.type === 'document'" class="w-5 h-5" />
                    <Picture v-else-if="row.type === 'image'" class="w-5 h-5" />
                    <Folder v-else-if="row.type === 'folder'" class="w-5 h-5" />
                    <Files v-else class="w-5 h-5" />
                  </div>
                  <span class="text-[14px] text-gray-800 group-hover:text-blue-600 transition-colors truncate">{{ row.name }}</span>
                  
                  <!-- Hover Actions -->
                  <div class="hidden group-hover:flex items-center gap-2 ml-auto shrink-0">
                    <div class="w-8 h-8 rounded-md flex items-center justify-center hover:bg-gray-100 text-gray-500 hover:text-blue-600 transition-colors" title="下载" @click.stop="downloadFile(row)">
                      <Download class="w-4 h-4" />
                    </div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="size" label="大小" width="150">
              <template #default="{ row }">
                <span class="text-[13px] text-gray-500">{{ row.size || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="date" label="修改日期" width="180">
               <template #default="{ row }">
                <span class="text-[13px] text-gray-500">{{ row.date }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Download, Sort, Menu, Grid, Document, Folder, Picture, Files } from '@element-plus/icons-vue'

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
/* Customize Element Plus Table styling to align with shadcn/modern UI */
.share-table :deep(.el-table__row:hover > td.el-table__cell) {
  background-color: #f8fafc !important;
}
.share-table :deep(.el-checkbox__inner) {
  border-radius: 4px;
  border-color: #cbd5e1;
  width: 16px;
  height: 16px;
}
.share-table :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #2563eb;
  border-color: #2563eb;
}
.share-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}
</style>