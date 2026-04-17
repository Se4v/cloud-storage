<template>
  <div class="h-full flex flex-col bg-slate-50">
    <!-- 顶部工具栏 - 白色背景 -->
    <div class="bg-white px-8 py-4 border-b border-slate-200">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <button
              @click="handleBatchDelete"
              :disabled="selectedLinks.length === 0"
              class="inline-flex items-center gap-2 px-4 py-2 bg-white text-slate-700 border border-slate-200 text-sm font-medium rounded-lg hover:bg-slate-50 hover:border-slate-300 active:scale-95 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <el-icon><Delete /></el-icon>
            删除外链
          </button>
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
                <div class="w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0 text-white bg-slate-400">
                  <el-icon :size="20">
                    <Document v-if="row.fileType === 1" />
                    <FolderOpened v-else />
                  </el-icon>
                </div>
                <div class="min-w-0 flex-1">
                  <!-- 第一行：文件名 + 标签并列 -->
                  <div class="flex items-center gap-2 flex-wrap">
                    <span class="text-sm font-medium text-slate-900 truncate" :title="row.linkName">
                      {{ row.linkName }}
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
                        v-if="row.linkType === 2"
                        class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium border border-amber-200 bg-amber-50 text-amber-700 flex-shrink-0"
                    >
                      <el-icon :size="10"><Lock /></el-icon>
                      <span>有密码</span>
                    </span>
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
                      @click="selectedLinks = [row]; handleBatchDelete()"
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

    <!-- 底部状态栏 -->
    <div class="bg-white border-t border-slate-200 px-6 py-3 flex items-center justify-between text-sm text-slate-500">
      <div class="flex items-center gap-4">
        <span>共 {{ total }} 项数据</span>
        <span v-if="selectedLinks.length > 0" class="text-blue-600 font-medium">
          已选中 {{ selectedLinks.length }} 项
        </span>
      </div>

      <el-pagination
          v-model:current-page="currentPage"
          :page-size="10"
          :total="total"
          layout="total, prev, pager, next"
          background
          class="custom-pagination"
      />
    </div>

    <!-- 编辑对话框 -->
    <el-dialog
        v-model="editDialogVisible"
        title="编辑分享链接"
        width="500px"
        :close-on-click-modal="false"
    >
      <el-form :model="editForm" label-width="100px" class="mt-4">
        <el-form-item label="链接名称">
          <el-input v-model="editForm.linkName" placeholder="请输入链接名称" />
        </el-form-item>
        <el-form-item label="分享类型">
          <el-radio-group v-model="editForm.linkType">
            <el-radio :label="1">公开链接</el-radio>
            <el-radio :label="2">加密链接</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="提取码" v-if="editForm.linkType === 2">
          <el-input v-model="editForm.accessCode" placeholder="请输入提取码" maxlength="6" show-word-limit />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker
              v-model="editForm.expiredAt"
              type="datetime"
              placeholder="选择过期时间"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-2">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveEdit" :loading="saving">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
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
import axios from 'axios'
import { useUserStore } from "@/stores/user.js";

const userStore = useUserStore()
const API_BASE_URL = 'http://localhost:8080'

// 获取请求配置（包含认证头）
const getAuthConfig = () => {
  const token = userStore.token
  return {
    headers: {
      'Authorization': token ? `Bearer ${token}` : '',
      'Content-Type': 'application/json'
    }
  }
}

const loading = ref(false)
const saving = ref(false)
const currentPage = ref(1)
const total = ref(0)
const selectedLinks = ref([])
const tableRef = ref(null)
const linkList = ref([])

// 编辑对话框
const editDialogVisible = ref(false)
const editForm = ref({
  id: null,
  linkName: '',
  linkType: 1,
  accessCode: '',
  expireTime: null
})

// 获取分享链接列表
const loadLinkList = async () => {
  loading.value = true
  try {
    const { data: res} = await axios.get(`${API_BASE_URL}/api/share`, getAuthConfig())
    if (res.code !== 200) {
      ElMessage.error(res.msg || '获取列表失败')
      return
    }
    linkList.value = res.data || []
    total.value = linkList.value.length
  } catch (error) {
    console.error(error)
    ElMessage.error(error.message || '获取分享链接列表失败')
  } finally {
    loading.value = false
  }
}

// 获取过期状态
const getExpireStatus = (row) => {
  const statusMap = {
    expired: { label: '已过期', class: 'bg-red-50 text-red-700 border-red-200' },
    warning: { label: '即将过期', class: 'bg-amber-50 text-amber-700 border-amber-200' },
    active: { label: '有效', class: 'bg-emerald-50 text-emerald-700 border-emerald-200' }
  }

  const now = new Date().getTime()
  const expireTime = new Date(row.expireTime).getTime()
  const sevenDays = 7 * 24 * 60 * 60 * 1000 // 7天的毫秒数

  if (now > expireTime) {
    return statusMap.expired
  } else if (expireTime - now < sevenDays) {
    return statusMap.warning
  } else {
    return statusMap.active
  }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedLinks.value = selection
}

// 编辑
const handleEdit = (row) => {
  editForm.value = {
    id: parseInt(row.id),
    linkName: row.linkName,
    linkType: row.linkType,
    accessCode: row.linkType === 2 ? (row.accessCode || '******') : '',
    expireTime: row.expireTime
  }
  editDialogVisible.value = true
}

// 保存编辑
const handleSaveEdit = async () => {
  saving.value = true
  try {
    const updateData = {
      id: editForm.value.id,
      linkName: editForm.value.linkName,
      linkType: editForm.value.linkType,
      accessCode: editForm.value.accessCode,
      expireTime: editForm.value.expireTime
    }
    const { data: res } = await axios.post(`${API_BASE_URL}/api/share/update`, updateData, getAuthConfig())
    if (res.code !== 200) {
      ElMessage.error(msg || '更新失败')
      return
    }
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    await loadLinkList()
  } catch (error) {
    console.error(error)
    ElMessage.error(error.message || '更新失败')
  } finally {
    saving.value = false
  }
}

// 复制链接
const handleCopyLink = async (row) => {
  const linkUrl = `http://localhost:5173/s/${row.linkKey}`
  try {
    await navigator.clipboard.writeText(linkUrl)
    ElMessage.success(`已复制分享链接`)
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (!selectedLinks.value.length) return

  try {
    const selectedNames = selectedLinks.value.map(row => row.linkName)
    const msg = `确定要删除${selectedNames.length > 1 ? `选中的${ selectedNames.value.length }个外链吗？`
        : `${ selectedNames[0] }"`}吗？删除后链接将立即失效。`
    await ElMessageBox.confirm(msg, '确认删除', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: '!bg-red-600 !border-red-600 hover:!bg-red-700'
      }
    )

    const { data: res } = await axios.post(`${API_BASE_URL}/api/share/delete`, {
      linkIds: selectedLinks.value.map(row => row.id)
    }, getAuthConfig())
    if (res.code !== 200) {
      ElMessage.error(res.msg || '删除失败')
      return
    }
    ElMessage.success('批量删除成功')
    selectedLinks.value = []
    await loadLinkList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadLinkList()
})
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
  border-radius: 4px;
  border-color: #cbd5e1;
}

:deep(.link-table .el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #2563eb;
  border-color: #2563eb;
}

/* 分页样式优化 */
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
</style>
