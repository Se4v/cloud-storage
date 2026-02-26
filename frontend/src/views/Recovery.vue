<template>
  <div class="h-full flex flex-col bg-slate-50 p-6">
    <!-- 操作栏 - 白色背景卡片 -->
    <div class="mb-4 bg-white rounded-lg border border-slate-200 p-4 flex items-center justify-between shadow-sm">
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
            class="inline-flex items-center gap-2 px-4 py-2 border border-slate-300 bg-white text-slate-700 text-sm font-medium rounded-md hover:bg-slate-50 hover:text-red-600 hover:border-red-300 transition-colors"
        >
          <el-icon :size="16"><DeleteFilled /></el-icon>
          清空
        </button>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="flex-1 bg-white rounded-lg border border-slate-200 shadow-sm overflow-hidden">
      <el-table
          ref="tableRef"
          :data="tableData"
          style="width: 100%; height: 100%"
          @selection-change="handleSelectionChange"
          row-key="id"
          class="recovery-table"
          :header-cell-style="{
          background: '#ffffff',
          color: '#64748b',
          fontWeight: 600,
          fontSize: '14px',
          borderBottom: '1px solid #e2e8f0',
          height: '48px'
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
            min-width="300"
        >
          <template #default="{ row }">
            <div class="flex items-center gap-4 py-2">
              <div
                  class="w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0"
                  :class="getFileIconBg(row.fileType)"
              >
                <el-icon :size="22" :class="getFileIconColor(row.fileType)">
                  <component :is="getFileIcon(row.fileType)" />
                </el-icon>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="font-medium text-slate-900 truncate text-[15px]">{{ row.fileName }}</span>
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
            <span class="text-slate-600 text-[14px]">{{ row.filePath }}</span>
          </template>
        </el-table-column>

        <el-table-column
            prop="deleteTime"
            label="删除时间"
            width="180"
            sortable
        >
          <template #default="{ row }">
            <div class="flex items-center gap-1.5 text-slate-600 text-[14px]">
              <el-icon :size="14" class="text-slate-400"><Clock /></el-icon>
              <span>{{ row.deleteTime }}</span>
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
            <span class="text-slate-600 text-[14px] tabular-nums">{{ row.fileSize }}</span>
          </template>
        </el-table-column>

        <el-table-column
            label="操作"
            width="100"
            align="center"
            fixed="right"
        >
          <template #default="{ row }">
            <div class="flex items-center justify-center gap-1">
              <button
                  @click="handleRestore(row)"
                  class="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-full transition-colors"
                  title="还原"
              >
                <el-icon :size="18"><RefreshLeft /></el-icon>
              </button>
              <button
                  @click="handleDelete(row)"
                  class="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-full transition-colors"
                  title="彻底删除"
              >
                <el-icon :size="18"><Delete /></el-icon>
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 底部栏 - 白色背景 -->
    <div class="mt-4 bg-white rounded-lg border border-slate-200 px-4 py-3 flex items-center justify-between shadow-sm">
      <div class="flex items-center gap-6 text-sm">
        <span class="text-slate-600">
          共 <span class="font-semibold text-slate-900">{{ total }}</span> 项文件
        </span>
        <span v-if="selectedRows.length > 0" class="text-blue-600 font-medium">
          已选中 {{ selectedRows.length }} 项
        </span>
      </div>

      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="prev, pager, next, sizes"
          background
          class="custom-pagination"
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

// 模拟数据 - 参考图片中的数据格式
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
</script>

<style scoped>
/* 表格样式优化 */
:deep(.recovery-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: #ffffff;
  --el-table-row-hover-bg-color: #f8fafc;
}

:deep(.recovery-table .el-table__header-wrapper th) {
  font-weight: 600;
  color: #64748b;
  border-bottom: 1px solid #e2e8f0;
  padding: 12px 16px;
}

:deep(.recovery-table .el-table__row) {
  transition: background-color 0.2s;
}

:deep(.recovery-table .el-table__row:hover) {
  background-color: #f8fafc;
}

:deep(.recovery-table .el-table__cell) {
  padding: 8px 16px;
}

:deep(.recovery-table .el-checkbox__inner) {
  border-color: #cbd5e1;
  border-radius: 4px;
  width: 16px;
  height: 16px;
}

:deep(.recovery-table .el-checkbox__inner::after) {
  left: 5px;
  top: 2px;
}

:deep(.recovery-table .el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #2563eb;
  border-color: #2563eb;
}

/* 分页样式优化 - 更简洁 */
:deep(.custom-pagination .el-pagination__total) {
  display: none;
}

:deep(.custom-pagination .el-pager li) {
  background: transparent;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  margin: 0 4px;
  color: #475569;
  font-weight: 500;
  min-width: 32px;
  height: 32px;
  line-height: 32px;
  transition: all 0.2s;
}

:deep(.custom-pagination .el-pager li:hover) {
  border-color: #cbd5e1;
  color: #0f172a;
}

:deep(.custom-pagination .el-pager li.is-active) {
  background: #2563eb;
  border-color: #2563eb;
  color: white;
}

:deep(.custom-pagination .el-pagination__sizes .el-input__wrapper) {
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  border-radius: 6px;
}

:deep(.custom-pagination .btn-prev),
:deep(.custom-pagination .btn-next) {
  background: transparent;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  color: #475569;
  min-width: 32px;
  height: 32px;
  padding: 0;
}

:deep(.custom-pagination .btn-prev:hover),
:deep(.custom-pagination .btn-next:hover) {
  color: #0f172a;
  border-color: #cbd5e1;
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