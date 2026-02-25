<template>
  <div class="h-full flex flex-col bg-slate-50">
    <!-- 顶部工具栏 - 白色背景 -->
    <div class="bg-white px-8 py-4 border-b border-slate-200">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <el-button
              v-if="selectedLinks.length > 0"
              type="primary"
              class="!bg-slate-900 !border-slate-900 hover:!bg-slate-800 shadow-sm"
              @click="handleBatchDelete"
          >
            <el-icon class="mr-1"><Delete /></el-icon>
            删除外链
          </el-button>

          <el-button
              v-else
              disabled
              class="!opacity-50"
          >
            <el-icon class="mr-1"><Delete /></el-icon>
            删除外链
          </el-button>
        </div>
      </div>
    </div>

    <!-- 内容区域 - 淡灰背景 -->
    <div class="flex-1 overflow-auto p-8">
      <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <!-- 表格 -->
        <el-table
            ref="tableRef"
            v-loading="loading"
            :data="linkList"
            row-key="id"
            @selection-change="handleSelectionChange"
            class="link-table"
        >
          <el-table-column type="selection" width="48" align="center" />

          <!-- 链接名称 - 标签移到这里 -->
          <el-table-column label="链接名称" min-width="380">
            <template #default="{ row }">
              <div class="flex items-center gap-3 py-2">
                <div
                    class="w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0"
                    :class="getFileIconBg(row)"
                >
                  <el-icon :size="20" :class="getFileIconColor(row)">
                    <Document v-if="row.type === 'file'" />
                    <FolderOpened v-else />
                  </el-icon>
                </div>
                <div class="min-w-0 flex-1">
                  <!-- 第一行：文件名 + 标签并列 -->
                  <div class="flex items-center gap-2 flex-wrap">
                    <span class="text-sm font-medium text-slate-900 truncate" :title="row.name">
                      {{ row.name }}
                    </span>
                    <!-- 已过期标签 -->
                    <span
                        class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border flex-shrink-0"
                        :class="getExpireStatus(row).class"
                    >
                      {{ getExpireStatus(row).label }}
                    </span>
                    <!-- 有密码标签 -->
                    <span
                        v-if="row.isProtected"
                        class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium border border-amber-200 bg-amber-50 text-amber-700 flex-shrink-0"
                    >
                      <el-icon :size="10"><Lock /></el-icon>
                      <span>有密码</span>
                    </span>
                  </div>
                  <!-- 第二行：大小和路径 -->
                  <div class="mt-1 text-xs text-slate-500 flex items-center gap-2">
                    <span>{{ row.size || '—' }}</span>
                    <span class="w-1 h-1 rounded-full bg-slate-300"></span>
                    <span class="truncate max-w-[200px]">{{ row.path }}</span>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <!-- 过期时间 - 只显示时间 -->
          <el-table-column label="过期时间" width="180" sortable>
            <template #default="{ row }">
              <span class="text-sm text-slate-700 whitespace-nowrap">{{ row.expireTime }}</span>
            </template>
          </el-table-column>

          <!-- 创建时间 -->
          <el-table-column label="创建时间" width="180" sortable>
            <template #default="{ row }">
              <span class="text-sm text-slate-600 whitespace-nowrap">{{ row.createTime }}</span>
            </template>
          </el-table-column>

          <!-- 操作 - 图标平铺显示 -->
          <el-table-column label="操作" width="140" fixed="right" align="center">
            <template #default="{ row }">
              <div class="flex items-center justify-center gap-2">
                <el-tooltip content="编辑设置" placement="top">
                  <button
                      class="p-2 text-slate-500 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-all"
                      @click="handleEdit(row)"
                  >
                    <el-icon :size="18"><Edit /></el-icon>
                  </button>
                </el-tooltip>

                <el-tooltip content="复制链接" placement="top">
                  <button
                      class="p-2 text-slate-500 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-all"
                      @click="handleCopyLink(row)"
                  >
                    <el-icon :size="18"><CopyDocument /></el-icon>
                  </button>
                </el-tooltip>

                <el-tooltip content="删除链接" placement="top">
                  <button
                      class="p-2 text-slate-500 hover:text-red-600 hover:bg-red-50 rounded-md transition-all"
                      @click="handleDelete(row)"
                  >
                    <el-icon :size="18"><Delete /></el-icon>
                  </button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 空状态 -->
        <div v-if="!loading && linkList.length === 0" class="py-20 flex flex-col items-center justify-center">
          <div class="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center mb-4">
            <el-icon class="text-slate-400" :size="32"><Link /></el-icon>
          </div>
          <h3 class="text-sm font-medium text-slate-900 mb-1">暂无外链</h3>
          <p class="text-sm text-slate-500 mb-4">您还没有创建任何文件分享链接</p>
        </div>
      </div>
    </div>

    <!-- 底部栏 -->
    <div class="bg-white border-t border-slate-200 px-8 py-4 flex items-center justify-between">
      <div class="text-sm text-slate-500">
        共 <span class="font-medium text-slate-900">{{ total }}</span> 项数据
        <span v-if="selectedLinks.length > 0" class="ml-2 text-blue-600">
          已选中 <span class="font-medium">{{ selectedLinks.length }}</span> 项
        </span>
      </div>

      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          class="!font-sans"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  FolderOpened,
  Link,
  Delete,
  Lock,
  Edit,
  CopyDocument
} from '@element-plus/icons-vue'

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(3)
const selectedLinks = ref([])
const tableRef = ref(null)

// 模拟数据
const linkList = ref([
  {
    id: '1',
    name: '腾讯云企业网盘使用手册.pdf',
    type: 'file',
    fileType: 'pdf',
    size: '2.4 MB',
    path: '/企业文档/产品资料',
    expireTime: '2022-09-22 11:08:34',
    createTime: '2022-09-21 11:08:34',
    isProtected: false,
    status: 'expired'
  },
  {
    id: '2',
    name: '新建 Word 文档 (2).docx',
    type: 'file',
    fileType: 'word',
    size: '156 KB',
    path: '/个人文档/工作资料',
    expireTime: '2022-09-15 10:46:56',
    createTime: '2022-09-14 10:46:56',
    isProtected: true,
    status: 'expired'
  },
  {
    id: '3',
    name: '企业网盘移动端图标规范',
    type: 'folder',
    size: null,
    path: '/设计规范',
    expireTime: '2022-09-21 10:34:16',
    createTime: '2022-09-14 10:34:19',
    isProtected: false,
    status: 'expired'
  }
])

// 获取文件图标背景色
const getFileIconBg = (row) => {
  if (row.type === 'folder') return 'bg-blue-100'
  if (row.fileType === 'pdf') return 'bg-red-100'
  if (row.fileType === 'word') return 'bg-blue-100'
  if (row.fileType === 'excel') return 'bg-green-100'
  if (row.fileType === 'image') return 'bg-purple-100'
  return 'bg-blue-100'
}

// 获取文件图标颜色
const getFileIconColor = (row) => {
  if (row.type === 'folder') return 'text-blue-600'
  if (row.fileType === 'pdf') return 'text-red-600'
  if (row.fileType === 'word') return 'text-blue-600'
  if (row.fileType === 'excel') return 'text-green-600'
  if (row.fileType === 'image') return 'text-purple-600'
  return 'text-blue-600'
}

// 获取过期状态
const getExpireStatus = (row) => {
  const statusMap = {
    expired: { label: '已过期', class: 'bg-red-50 text-red-700 border-red-200' },
    warning: { label: '即将过期', class: 'bg-amber-50 text-amber-700 border-amber-200' },
    active: { label: '有效', class: 'bg-emerald-50 text-emerald-700 border-emerald-200' }
  }
  return statusMap[row.status] || statusMap.active
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedLinks.value = selection
}

// 编辑
const handleEdit = (row) => {
  ElMessage.info(`编辑 ${row.name} 的设置`)
}

// 复制链接
const handleCopyLink = (row) => {
  ElMessage.success(`已复制链接：${row.name}`)
}

// 删除单个
const handleDelete = (row) => {
  ElMessageBox.confirm(
      `确定要删除外链 "${row.name}" 吗？删除后该链接将立即失效。`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: '!bg-red-600 !border-red-600 hover:!bg-red-700'
      }
  ).then(() => {
    const index = linkList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      linkList.value.splice(index, 1)
      total.value--
      ElMessage.success('删除成功')
    }
  })
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(
      `确定要删除选中的 ${selectedLinks.value.length} 个外链吗？`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: '!bg-red-600 !border-red-600 hover:!bg-red-700'
      }
  ).then(() => {
    const ids = selectedLinks.value.map(item => item.id)
    linkList.value = linkList.value.filter(item => !ids.includes(item.id))
    total.value -= ids.length
    selectedLinks.value = []
    ElMessage.success('批量删除成功')
  })
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}
</script>

<style scoped>
/* 表格样式优化 - shadcn 风格 */
:deep(.link-table) {
  --el-table-header-bg-color: #f8fafc;
  --el-table-header-text-color: #475569;
  --el-table-row-hover-bg-color: #f1f5f9;
  --el-table-border-color: transparent;
  --el-table-text-color: #334155;
}

:deep(.link-table .el-table__header th) {
  font-weight: 600;
  font-size: 0.875rem;
  height: 48px;
  border-bottom: 1px solid #e2e8f0;
  background-color: #f8fafc;
}

:deep(.link-table .el-table__row) {
  transition: all 0.2s ease;
}

:deep(.link-table .el-table__row td) {
  border-bottom: 1px solid #f1f5f9;
  padding: 12px 0;
}

:deep(.link-table .el-table__row:hover td) {
  background-color: #f8fafc;
}

:deep(.link-table .el-checkbox__inner) {
  border-color: #cbd5e1;
  border-radius: 4px;
  width: 18px;
  height: 18px;
}

:deep(.link-table .el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #2563eb;
  border-color: #2563eb;
}

/* 分页样式优化 */
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #0f172a;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
  color: #0f172a;
}
</style>