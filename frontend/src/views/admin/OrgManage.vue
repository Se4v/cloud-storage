<template>
  <div class="h-full flex flex-col p-6 bg-slate-50/50">
    <!-- 控制栏：左侧操作按钮，右侧搜索 -->
    <div class="mb-6 flex flex-col lg:flex-row lg:items-center justify-between gap-4 bg-white rounded-xl border border-slate-200 shadow-sm p-4">
      <!-- 左侧：操作按钮 -->
      <div class="flex items-center gap-3">
        <button
            @click="handleCreate"
            class="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-all duration-200 shadow-sm hover:shadow-md active:scale-95"
        >
          <el-icon :size="16"><Plus /></el-icon>
          新建节点
        </button>
        <button
            @click="handleBatchDelete"
            :disabled="!selectedRows.length"
            class="inline-flex items-center gap-2 px-4 py-2 bg-white text-slate-700 border border-slate-200 text-sm font-medium rounded-lg hover:bg-slate-50 hover:border-slate-300 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <el-icon :size="16"><Delete /></el-icon>
          批量删除
          <span v-if="selectedRows.length" class="ml-1 text-xs bg-slate-100 text-slate-600 px-2 py-0.5 rounded-full">
            {{ selectedRows.length }}
          </span>
        </button>
      </div>

      <!-- 右侧：搜索 -->
      <div class="flex items-center gap-3">
        <el-input
            v-model="searchQuery"
            placeholder="搜索组织名称..."
            clearable
            class="w-64 !rounded-lg"
            @input="handleSearch"
            @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon class="text-slate-400"><Search /></el-icon>
          </template>
        </el-input>
        <button
          @click="handleSearch"
          class="inline-flex items-center justify-center gap-2 h-9 px-4 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors whitespace-nowrap"
        >
          <el-icon :size="16"><Search /></el-icon>
          <span>搜索</span>
        </button>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="flex-1 min-h-0">
      <div class="h-full bg-white rounded-xl border border-slate-200 shadow-sm flex flex-col overflow-hidden">
        <el-table
            v-loading="loading"
            :data="filteredList"
            row-key="id"
            @selection-change="handleSelectionChange"
            class="flex-1"
            header-cell-class-name="!bg-slate-50 !text-slate-700 !font-semibold !border-b !border-slate-200"
            cell-class-name="!border-b !border-slate-100"
        >
          <el-table-column type="selection" width="50" align="center" />

          <el-table-column label="组织名称" min-width="160" flex="2">
            <template #default="{ row }">
              <span class="font-medium text-slate-900">{{ row.name }}</span>
            </template>
          </el-table-column>

          <el-table-column label="类型" min-width="100">
            <template #default="{ row }">
              <el-tag
                  :type="getTypeType(row.type)"
                  effect="light"
                  class="!rounded-md !border-0 !font-medium"
                  :class="getTypeClass(row.type)"
              >
                {{ getTypeLabel(row.type) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="父节点" min-width="160" flex="2">
            <template #default="{ row }">
              <div class="flex items-center gap-2 text-slate-600">
                <el-icon class="text-slate-400"><Link /></el-icon>
                <span>{{ row.parentName || (row.parentId === 0 ? '根节点' : '-') }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="空间配额" min-width="120">
            <template #default="{ row }">
              <span class="text-slate-600 text-sm font-medium">{{ formatStorageQuota(row.storageQuota) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="状态" min-width="100">
            <template #default="{ row }">
              <span
                :class="[
                  'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border',
                  row.isEnabled === 1
                    ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                    : 'bg-red-50 text-red-700 border-red-200'
                ]"
              >
                <span
                  :class="[
                    'w-1.5 h-1.5 rounded-full mr-1.5',
                    row.isEnabled === 1 ? 'bg-emerald-500' : 'bg-red-500'
                  ]"
                ></span>
                {{ row.isEnabled === 1 ? '启用' : '禁用' }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="flex items-center gap-2">
                <el-button
                    link
                    type="primary"
                    class="!text-slate-600 hover:!text-blue-600"
                    @click="handleEdit(row)"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-popconfirm
                    title="确认删除该组织节点？"
                    confirm-button-text="确认"
                    cancel-button-text="取消"
                    confirm-button-class="!bg-red-600 !border-red-600"
                    @confirm="handleDelete(row)"
                >
                  <template #reference>
                    <el-button
                        link
                        type="danger"
                        class="!text-slate-600 hover:!text-red-600"
                    >
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="px-6 py-4 border-t border-slate-200 bg-slate-50/50 flex items-center justify-between">
          <span class="text-sm text-slate-500">
            共 <span class="font-medium text-slate-900">{{ total }}</span> 条记录
          </span>
          <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50]"
              :total="total"
              layout="prev, pager, next, sizes"
              background
              class="!gap-2"
          />
        </div>
      </div>
    </div>

    <!-- 新建/编辑对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑组织节点' : '新建组织节点'"
        width="600px"
        class="!rounded-xl"
        :close-on-click-modal="false"
    >
      <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="100px"
          class="mt-4"
      >
        <el-form-item label="组织名称" prop="name">
          <el-input
              v-model="formData.name"
              placeholder="请输入组织名称"
              class="!rounded-lg"
          />
        </el-form-item>

        <el-form-item label="组织类型" prop="type">
          <el-select
              v-model="formData.type"
              placeholder="请选择类型"
              class="w-full !rounded-lg"
          >
            <el-option label="公司" :value="1" />
            <el-option label="部门" :value="2" />
            <el-option label="小组" :value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="父节点" prop="parentId">
          <el-select
              v-model="formData.parentId"
              placeholder="请选择父节点（不选则为根节点）"
              class="w-full !rounded-lg"
              clearable
          >
            <el-option label="根节点" :value="0" />
            <el-option
                v-for="org in orgList"
                :key="org.id"
                :label="org.name"
                :value="org.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="空间配额" prop="storageQuota">
          <el-input
              v-model="storageQuotaGB"
              placeholder="请输入空间配额（GB）"
              class="!rounded-lg"
          >
            <template #append>GB</template>
          </el-input>
        </el-form-item>

        <el-form-item label="组织管理员" prop="adminName">
          <el-input
              v-model="formData.adminUsername"
              placeholder="请输入管理员用户名"
              class="!rounded-lg"
          />
        </el-form-item>

        <el-form-item label="状态" prop="isEnabled">
          <el-radio-group v-model="formData.isEnabled">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <button
              @click="dialogVisible = false"
              class="px-4 py-2 text-sm font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors duration-200"
          >
            取消
          </button>
          <button
              @click="handleSubmit"
              class="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-all duration-200 shadow-sm hover:shadow-md active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            确认
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import {
  Plus,
  Delete,
  Search,
  Edit,
  Link
} from '@element-plus/icons-vue'

const API_BASE_URL = 'http://localhost:8080'

// 获取认证配置
const getAuthConfig = () => {
  const token = localStorage.getItem('token')
  return {
    headers: {
      'Authorization': token ? `Bearer ${token}` : '',
      'Content-Type': 'application/json'
    }
  }
}

// 数据加载状态
const loading = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref([])

// 对话框状态
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: null,
  name: '',
  type: 2,
  parentId: 0,
  storageQuota: 10737418240, // 10GB in bytes
  adminUsername: '',
  isEnabled: 1
})

// 用于对话框显示的GB值
const storageQuotaGB = ref(10)

const formRules = {
  name: [{ required: true, message: '请输入组织名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择组织类型', trigger: 'change' }]
}

// 组织列表数据
const orgList = ref([])

// 过滤后的列表
const filteredList = computed(() => {
  if (!searchQuery.value) return orgList.value
  const query = searchQuery.value.toLowerCase()
  return orgList.value.filter(item =>
      item.name.toLowerCase().includes(query) ||
      item.parentName?.toLowerCase().includes(query)
  )
})

// 格式化存储配额显示
const formatStorageQuota = (quota) => {
  if (quota === 0) return '0 B'
  if (!quota) return '-'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(quota) / Math.log(k))
  return parseFloat((quota / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 类型标签映射
const getTypeLabel = (type) => {
  const map = { 1: '公司', 2: '部门', 3: '小组' }
  return map[type] || type
}

const getTypeType = (type) => {
  const map = { 1: 'primary', 2: 'success', 3: 'info' }
  return map[type] || ''
}

const getTypeClass = (type) => {
  const map = {
    1: '!bg-blue-50 !text-blue-700',
    2: '!bg-emerald-50 !text-emerald-700',
    3: '!bg-slate-100 !text-slate-700'
  }
  return map[type] || ''
}

// 表格选择
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
}

// 增删改查操作
const GB = 1024 * 1024 * 1024

const handleCreate = () => {
  isEdit.value = false
  Object.assign(formData, {
    id: null,
    name: '',
    type: 2,
    parentId: 0,
    sort: 0,
    storageQuota: 10 * GB,
    adminUsername: '',
    isEnabled: 1
  })
  storageQuotaGB.value = 10
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, row)
  storageQuotaGB.value = Math.round(row.storageQuota / GB)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定要删除组织节点 "${row.name}" 吗？此操作不可恢复。`,
        '确认删除',
        {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'warning',
          confirmButtonClass: '!bg-red-600 !border-red-600 !rounded-lg',
          cancelButtonClass: '!rounded-lg',
          customClass: '!rounded-xl'
        }
    )
    const res = await axios.post(`${API_BASE_URL}/api/org/delete`, { nodeIds: [row.id] }, getAuthConfig())
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      await loadData()
    } else {
      ElMessage.error(res.data.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.msg || '删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的组织节点')
    return
  }
  try {
    await ElMessageBox.confirm(
        `确定要删除选中的 ${selectedRows.value.length} 个组织节点吗？此操作不可恢复。`,
        '批量删除',
        {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'warning',
          confirmButtonClass: '!bg-red-600 !border-red-600 !rounded-lg',
          cancelButtonClass: '!rounded-lg',
          customClass: '!rounded-xl'
        }
    )
    const ids = selectedRows.value.map(row => row.id)
    const res = await axios.post(`${API_BASE_URL}/api/org/delete`, { nodeIds: ids }, getAuthConfig())
    if (res.data.code === 200) {
      ElMessage.success('批量删除成功')
      selectedRows.value = []
      await loadData()
    } else {
      ElMessage.error(res.data.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.msg || '删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 将GB转换为字节
        const storageQuotaBytes = Number(storageQuotaGB.value) * GB
        if (isEdit.value) {
          const submitData = {
            id: formData.id,
            name: formData.name,
            type: formData.type,
            parentId: formData.parentId,
            storageQuota: storageQuotaBytes,
            isEnabled: formData.isEnabled,
            adminUsername: formData.adminUsername
          }
          const res = await axios.post(`${API_BASE_URL}/api/org/update`, submitData, getAuthConfig())
          if (res.data.code === 200) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            await loadData()
          } else {
            ElMessage.error(res.data.msg || '更新失败')
          }
        } else {
          const submitData = {
            name: formData.name,
            type: formData.type,
            parentId: formData.parentId,
            storageQuota: storageQuotaBytes,
            adminUsername: formData.adminUsername
          }
          const res = await axios.post(`${API_BASE_URL}/api/org/create`, submitData, getAuthConfig())
          if (res.data.code === 200) {
            ElMessage.success('创建成功')
            dialogVisible.value = false
            await loadData()
          } else {
            ElMessage.error(res.data.msg || '创建失败')
          }
        }
      } catch (error) {
        console.error(isEdit.value ? '更新失败:' : '创建失败:', error)
        ElMessage.error(error.response?.data?.msg || (isEdit.value ? '更新失败' : '创建失败'))
      }
    }
  })
}

// 加载组织列表
const loadData = async () => {
  loading.value = true
  try {
    const res = await axios.get(`${API_BASE_URL}/api/org/all`, getAuthConfig())
    if (res.data.code === 200) {
      orgList.value = res.data.data || []
      total.value = orgList.value.length
    } else {
      ElMessage.error(res.data.msg || '加载数据失败')
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error(error.response?.data?.msg || '加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
/* 自定义滚动条 */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* Element Plus 样式覆盖，贴近 shadcn 风格 */
:deep(.el-button--primary) {
  --el-button-bg-color: #0f172a;
  --el-button-border-color: #0f172a;
  --el-button-hover-bg-color: #1e293b;
  --el-button-hover-border-color: #1e293b;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  border-radius: 0.5rem;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #0f172a inset;
}

:deep(.el-dialog) {
  border-radius: 0.75rem;
  box-shadow: 0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1);
}

:deep(.el-tag) {
  border-radius: 0.375rem;
  padding: 0 0.75rem;
  height: 1.5rem;
}

:deep(.el-tree-node__content) {
  border-radius: 0.375rem;
  margin: 2px 0;
}

:deep(.el-table) {
  --el-table-header-bg-color: #f8fafc;
  --el-table-row-hover-bg-color: #f8fafc;
  --el-table-border-color: #e2e8f0;
}

/* 分页器样式 */
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #2563eb;
  border-radius: 6px;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
  color: #2563eb;
}
</style>