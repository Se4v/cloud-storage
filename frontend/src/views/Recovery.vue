<template>
  <div class="h-full flex flex-col bg-slate-50">
    <!-- 顶部工具栏 - 白色背景 -->
    <div class="bg-white px-8 py-4 border-b border-slate-200 flex-shrink-0">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <button
              @click="handleBatchRestore"
              :disabled="selectedRows.length === 0"
              class="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors shadow-sm"
          >
            <el-icon :size="16"><RefreshLeft /></el-icon>
            还原
          </button>

          <button
              @click="handleBatchDelete"
              :disabled="selectedRows.length === 0"
              class="inline-flex items-center gap-2 px-4 py-2 border border-slate-300 bg-white text-slate-700 text-sm font-medium rounded-md hover:bg-slate-50 hover:text-red-600 hover:border-red-300 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <el-icon :size="16"><Delete /></el-icon>
            彻底删除
          </button>

          <button
              @click="handleClearAll"
              :disabled="tableData.length === 0"
              class="inline-flex items-center gap-2 px-4 py-2 border border-slate-300 bg-white text-slate-700 text-sm font-medium rounded-md hover:bg-slate-50 hover:text-red-600 hover:border-red-300 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <el-icon :size="16"><DeleteFilled /></el-icon>
            清空
          </button>
        </div>
      </div>
    </div>

    <!-- 内容区域 - 淡灰背景，与上下隔开 -->
    <div class="flex-1 overflow-auto p-6">
      <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <!-- 表格 -->
        <el-table
            ref="tableRef"
            :data="tableData"
            row-key="id"
            @selection-change="handleSelectionChange"
            class="recovery-table"
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
          <el-table-column
              type="selection"
              width="48"
              align="center"
          />

          <el-table-column
              prop="fileName"
              label="文件名"
              min-width="280"
          >
            <template #default="{ row }">
              <div class="flex items-center gap-3 py-1">
                <div
                    class="w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0"
                    :class="getFileIconBg(row.fileType)"
                >
                  <el-icon :size="20" :class="getFileIconColor(row.fileType)">
                    <component :is="getFileIcon(row.fileType)" />
                  </el-icon>
                </div>
                <div class="flex flex-col min-w-0">
                  <span class="text-sm font-medium text-slate-900 truncate">{{ row.fileName }}</span>
                  <span v-if="row.subCount" class="text-xs text-slate-500 mt-0.5">{{ row.subCount }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column
              prop="filePath"
              label="文件路径"
              min-width="200"
              show-overflow-tooltip
          >
            <template #default="{ row }">
              <span class="text-sm text-slate-600">{{ row.filePath }}</span>
            </template>
          </el-table-column>

          <el-table-column
              prop="deleteTime"
              label="删除时间"
              width="180"
              sortable
          >
            <template #default="{ row }">
              <div class="flex items-center gap-1.5 text-sm text-slate-600">
                <el-icon :size="14" class="text-slate-400"><Clock /></el-icon>
                <span class="whitespace-nowrap">{{ row.deleteTime }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column
              prop="fileSize"
              label="大小"
              width="120"
              align="left"
              sortable
          >
            <template #default="{ row }">
              <span class="text-sm text-slate-600 tabular-nums">{{ row.fileSize }}</span>
            </template>
          </el-table-column>

          <el-table-column
              label="操作"
              width="140"
              fixed="right"
              align="center"
          >
            <template #default="{ row }">
              <div class="flex items-center justify-center gap-1">
                <button
                    class="p-2 text-slate-500 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-all"
                    @click="handleRestore(row)"
                    title="还原"
                >
                  <el-icon :size="18"><RefreshLeft /></el-icon>
                </button>
                <button
                    class="p-2 text-slate-500 hover:text-red-600 hover:bg-red-50 rounded-md transition-all"
                    @click="handleDelete(row)"
                    title="彻底删除"
                >
                  <el-icon :size="18"><Delete /></el-icon>
                </button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 空状态 -->
        <div v-if="tableData.length === 0" class="py-20 flex flex-col items-center justify-center">
          <div class="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center mb-4">
            <el-icon class="text-slate-400" :size="32"><Delete /></el-icon>
          </div>
          <h3 class="text-sm font-medium text-slate-900 mb-1">回收站是空的</h3>
          <p class="text-sm text-slate-500">暂无已删除的文件</p>
        </div>
      </div>
    </div>

    <!-- 底部栏 - 白色背景 -->
    <div class="bg-white border-t border-slate-200 px-8 py-4 flex items-center justify-between flex-shrink-0">
      <div class="text-sm text-slate-500">
        共 <span class="font-medium text-slate-900">{{ total }}</span> 项文件
        <span v-if="selectedRows.length > 0" class="ml-2 text-blue-600">
          已选中 <span class="font-medium">{{ selectedRows.length }}</span> 项
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
  Folder,
  Picture,
  Film,
  Headset,
  Box,
  Delete,
  DeleteFilled,
  RefreshLeft,
  DocumentCopy,
  Clock
} from '@element-plus/icons-vue'

const tableRef = ref()
const selectedRows = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(6)

// 模拟数据
const tableData = ref([
  {
    id: 1,
    fileName: '设计文档.zip',
    fileType: 'zip',
    subCount: null,
    filePath: '企业空间/设计中心',
    deleteTime: '2022-04-27 14:45',
    fileSize: '31.2 MB'
  },
  {
    id: 2,
    fileName: '4月计划图.jpg',
    fileType: 'image',
    subCount: null,
    filePath: '企业空间/产品团队',
    deleteTime: '2020-04-26 18:30',
    fileSize: '2.1 MB'
  },
  {
    id: 3,
    fileName: '云盘需求（第一版）',
    fileType: 'doc',
    subCount: null,
    filePath: '企业空间/产品团队',
    deleteTime: '2020-04-26 18:30',
    fileSize: '6.2 MB'
  },
  {
    id: 4,
    fileName: '4月经费统计',
    fileType: 'excel',
    subCount: null,
    filePath: '企业空间/产品团队',
    deleteTime: '2020-04-26 18:30',
    fileSize: '340 KB'
  },
  {
    id: 5,
    fileName: '项目资料汇总',
    fileType: 'folder',
    subCount: '3 项',
    filePath: '个人空间/项目文件',
    deleteTime: '2020-04-20 14:37',
    fileSize: '22 MB'
  },
  {
    id: 6,
    fileName: '交付材料',
    fileType: 'folder',
    subCount: '127 项',
    filePath: '个人空间/项目文件',
    deleteTime: '2020-04-20 14:37',
    fileSize: '1.3 GB'
  }
])

// 获取文件图标
const getFileIcon = (type) => {
  const iconMap = {
    zip: Box,
    image: Picture,
    doc: Document,
    excel: DocumentCopy,
    folder: Folder,
    video: Film,
    audio: Headset
  }
  return iconMap[type] || Document
}

// 获取文件图标背景色
const getFileIconBg = (type) => {
  const bgMap = {
    folder: 'bg-blue-100',
    doc: 'bg-blue-100',
    excel: 'bg-green-100',
    image: 'bg-purple-100',
    pdf: 'bg-red-100',
    zip: 'bg-yellow-100',
    video: 'bg-red-100',
    audio: 'bg-pink-100'
  }
  return bgMap[type] || 'bg-slate-100'
}

// 获取文件图标颜色
const getFileIconColor = (type) => {
  const colorMap = {
    folder: 'text-blue-600',
    doc: 'text-blue-600',
    excel: 'text-green-600',
    image: 'text-purple-600',
    pdf: 'text-red-600',
    zip: 'text-yellow-600',
    video: 'text-red-600',
    audio: 'text-pink-600'
  }
  return colorMap[type] || 'text-slate-600'
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 还原单个
const handleRestore = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定要还原文件 "${row.fileName}" 吗？`,
        '确认还原',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info',
          customClass: 'custom-message-box'
        }
    )

    const index = tableData.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      tableData.value.splice(index, 1)
      total.value--
    }

    ElMessage.success('还原成功')
  } catch (error) {
    // 取消操作
  }
}

// 彻底删除单个
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定要彻底删除 "${row.fileName}" 吗？此操作不可恢复！`,
        '确认彻底删除',
        {
          confirmButtonText: '彻底删除',
          cancelButtonText: '取消',
          type: 'error',
          customClass: 'custom-message-box'
        }
    )

    const index = tableData.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      tableData.value.splice(index, 1)
      total.value--
    }

    ElMessage.success('已彻底删除')
  } catch (error) {
    // 取消操作
  }
}

// 批量还原
const handleBatchRestore = async () => {
  if (selectedRows.value.length === 0) return

  const isMulti = selectedRows.value.length > 1

  try {
    await ElMessageBox.confirm(
        isMulti
            ? `确定要还原选中的 ${selectedRows.value.length} 个文件吗？`
            : `确定要还原 "${selectedRows.value[0].fileName}" 吗？`,
        '确认还原',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info',
          customClass: 'custom-message-box'
        }
    )

    const ids = selectedRows.value.map(row => row.id)
    tableData.value = tableData.value.filter(item => !ids.includes(item.id))
    total.value -= ids.length
    selectedRows.value = []
    tableRef.value?.clearSelection()

    ElMessage.success('还原成功')
  } catch (error) {
    // 取消操作
  }
}

// 批量彻底删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return

  const isMulti = selectedRows.value.length > 1

  try {
    await ElMessageBox.confirm(
        isMulti
            ? `确定要彻底删除选中的 ${selectedRows.value.length} 个文件吗？此操作不可恢复！`
            : `确定要彻底删除 "${selectedRows.value[0].fileName}" 吗？此操作不可恢复！`,
        '确认彻底删除',
        {
          confirmButtonText: '彻底删除',
          cancelButtonText: '取消',
          type: 'error',
          customClass: 'custom-message-box'
        }
    )

    const ids = selectedRows.value.map(row => row.id)
    tableData.value = tableData.value.filter(item => !ids.includes(item.id))
    total.value -= ids.length
    selectedRows.value = []
    tableRef.value?.clearSelection()

    ElMessage.success('已彻底删除')
  } catch (error) {
    // 取消操作
  }
}

// 清空所有
const handleClearAll = async () => {
  if (tableData.value.length === 0) {
    ElMessage.info('回收站已是空的')
    return
  }

  try {
    await ElMessageBox.confirm(
        '确定要清空回收站吗？所有文件将被彻底删除且不可恢复！',
        '确认清空回收站',
        {
          confirmButtonText: '清空回收站',
          cancelButtonText: '取消',
          type: 'error',
          customClass: 'custom-message-box'
        }
    )

    tableData.value = []
    total.value = 0
    selectedRows.value = []
    tableRef.value?.clearSelection()

    ElMessage.success('回收站已清空')
  } catch (error) {
    // 取消操作
  }
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
/* 表格样式优化 - 参考安全外链页面 */
:deep(.recovery-table) {
  --el-table-header-bg-color: #f8fafc;
  --el-table-header-text-color: #475569;
  --el-table-row-hover-bg-color: #f1f5f9;
  --el-table-border-color: transparent;
  --el-table-text-color: #334155;
}

:deep(.recovery-table .el-table__header th) {
  font-weight: 600;
  font-size: 0.875rem;
  height: 48px;
  border-bottom: 1px solid #e2e8f0;
  background-color: #f8fafc;
}

:deep(.recovery-table .el-table__row) {
  transition: all 0.2s ease;
}

:deep(.recovery-table .el-table__row td) {
  border-bottom: 1px solid #f1f5f9;
  padding: 12px 0;
  vertical-align: middle;
}

:deep(.recovery-table .el-table__row:hover td) {
  background-color: #f8fafc;
}

/* 修复 checkbox 对齐问题 */
:deep(.recovery-table .el-table-column--selection .cell) {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 0;
}

:deep(.recovery-table .el-checkbox) {
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.recovery-table .el-checkbox__inner) {
  border-color: #cbd5e1;
  border-radius: 4px;
  width: 18px;
  height: 18px;
  margin: 0;
}

:deep(.recovery-table .el-checkbox__input) {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.recovery-table .el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #2563eb;
  border-color: #2563eb;
}

/* 分页样式优化 */
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #2563eb;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
  color: #2563eb;
}

/* 消息框样式 */
:deep(.custom-message-box) {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1);
}

:deep(.custom-message-box .el-message-box__header) {
  padding: 20px 20px 0;
}

:deep(.custom-message-box .el-message-box__title) {
  font-weight: 600;
  color: #0f172a;
  font-size: 16px;
}

:deep(.custom-message-box .el-message-box__content) {
  padding: 20px;
  color: #475569;
}

:deep(.custom-message-box .el-message-box__btns) {
  padding: 0 20px 20px;
}

:deep(.custom-message-box .el-button) {
  border-radius: 6px;
  padding: 8px 16px;
  font-weight: 500;
  transition: all 0.2s;
}

:deep(.custom-message-box .el-button--default) {
  border-color: #e2e8f0;
  color: #475569;
}

:deep(.custom-message-box .el-button--default:hover) {
  border-color: #cbd5e1;
  color: #0f172a;
  background: #f8fafc;
}

:deep(.custom-message-box .el-button--primary) {
  background: #2563eb;
  border-color: #2563eb;
}

:deep(.custom-message-box .el-button--primary:hover) {
  background: #1d4ed8;
  border-color: #1d4ed8;
}
</style>