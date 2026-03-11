<template>
  <div class="p-6 max-w-7xl mx-auto space-y-6">
    <!-- 操作栏卡片 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-4">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <!-- 左侧操作按钮 -->
        <div class="flex items-center gap-3">
          <button
              @click="handleCreate"
              class="inline-flex items-center gap-2 px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white text-sm font-medium rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-slate-900 focus:ring-offset-2"
          >
            <el-icon :size="16"><Plus /></el-icon>
            新建角色
          </button>

          <button
              @click="handleBatchDelete"
              :disabled="selectedRoles.length === 0"
              :class="[
              'inline-flex items-center gap-2 px-4 py-2 text-sm font-medium rounded-lg border transition-all duration-200',
              selectedRoles.length === 0
                ? 'border-slate-200 text-slate-400 cursor-not-allowed bg-slate-50'
                : 'border-red-200 text-red-600 hover:bg-red-50 hover:border-red-300 bg-white'
            ]"
          >
            <el-icon :size="16"><Delete /></el-icon>
            批量删除
          </button>
        </div>

        <!-- 右侧搜索 -->
        <div class="flex items-center gap-3">
          <div class="relative">
            <el-input
                v-model="searchQuery"
                placeholder="搜索角色名称或代码..."
                clearable
                class="w-64"
                @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon class="text-slate-400"><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据表格卡片 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <el-table
          v-loading="loading"
          :data="roleList"
          @selection-change="handleSelectionChange"
          class="w-full"
          header-cell-class-name="bg-slate-50 text-slate-700 font-semibold text-xs uppercase tracking-wider"
          row-class-name="hover:bg-slate-50/50 transition-colors duration-150"
      >
        <el-table-column type="selection" width="48" align="center" />

        <el-table-column label="角色名称" min-width="180">
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center text-white shadow-sm">
                <el-icon :size="18"><UserFilled /></el-icon>
              </div>
              <div>
                <div class="font-medium text-slate-900">{{ row.name }}</div>
                <div class="text-xs text-slate-500 mt-0.5">{{ row.description || '暂无描述' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="角色代码" width="180">
          <template #default="{ row }">
            <code class="px-2 py-1 bg-slate-100 text-slate-700 rounded text-xs font-mono border border-slate-200">
              {{ row.code }}
            </code>
          </template>
        </el-table-column>

        <el-table-column label="类型" width="120" align="center">
          <template #default="{ row }">
            <span
                :class="[
                'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border',
                row.type === 'system'
                  ? 'bg-amber-50 text-amber-700 border-amber-200'
                  : 'bg-emerald-50 text-emerald-700 border-emerald-200'
              ]"
            >
              {{ row.type === 'system' ? '系统角色' : '自定义' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            <span class="text-sm text-slate-600">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="140" align="right" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center justify-end gap-2">
              <button
                  @click="handleEdit(row)"
                  class="p-2 text-slate-600 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors duration-200"
                  title="编辑"
              >
                <el-icon :size="16"><Edit /></el-icon>
              </button>
              <button
                  @click="handleDelete(row)"
                  :disabled="row.type === 'system'"
                  :class="[
                  'p-2 rounded-lg transition-colors duration-200',
                  row.type === 'system'
                    ? 'text-slate-300 cursor-not-allowed'
                    : 'text-slate-600 hover:text-red-600 hover:bg-red-50'
                ]"
                  title="删除"
              >
                <el-icon :size="16"><Delete /></el-icon>
              </button>
            </div>
          </template>
        </el-table-column>

        <!-- 空状态 -->
        <template #empty>
          <div class="py-12 flex flex-col items-center justify-center text-center">
            <div class="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center mb-4">
              <el-icon :size="32" class="text-slate-400"><User /></el-icon>
            </div>
            <h3 class="text-sm font-medium text-slate-900 mb-1">暂无角色数据</h3>
            <p class="text-sm text-slate-500 mb-4">开始创建你的第一个角色</p>
            <button
                @click="handleCreate"
                class="inline-flex items-center gap-2 px-4 py-2 bg-slate-900 text-white text-sm font-medium rounded-lg hover:bg-slate-800 transition-colors"
            >
              <el-icon :size="16"><Plus /></el-icon>
              新建角色
            </button>
          </div>
        </template>
      </el-table>

      <!-- 分页 -->
      <div class="border-t border-slate-200 px-4 py-4 flex items-center justify-between">
        <div class="text-sm text-slate-500 flex items-center gap-4">
          <span>共 <span class="font-medium text-slate-900">{{ total }}</span> 条记录</span>
          <span v-if="selectedRoles.length > 0" class="text-blue-600 font-medium">
            已选择 {{ selectedRoles.length }} 项数据
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
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 新建/编辑角色弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="isEditing ? '编辑角色' : '新建角色'"
        width="500px"
        destroy-on-close
        class="custom-dialog"
        :close-on-click-modal="false"
    >
      <div class="p-2">
        <el-form
            ref="formRef"
            :model="form"
            :rules="formRules"
            label-position="top"
            class="space-y-4"
        >
          <el-form-item label="角色名称" prop="name">
            <el-input
                v-model="form.name"
                placeholder="请输入角色名称，如：普通员工"
                size="large"
                class="custom-input"
            />
          </el-form-item>

          <el-form-item label="角色代码" prop="code">
            <el-input
                v-model="form.code"
                placeholder="请输入角色代码，如：ROLE_USER"
                size="large"
                class="custom-input"
                :disabled="isEditing && form.type === 'system'"
            >
              <template #prefix>
                <span class="text-slate-400 text-sm">ROLE_</span>
              </template>
            </el-input>
            <p class="mt-1.5 text-xs text-slate-500">代码用于系统识别，创建后不可修改</p>
          </el-form-item>

          <el-form-item label="角色类型" prop="type">
            <el-select
                v-model="form.type"
                placeholder="选择角色类型"
                size="large"
                class="w-full custom-select"
                :disabled="isEditing"
            >
              <el-option label="自定义角色" value="custom">
                <div class="flex items-center gap-2">
                  <div class="w-2 h-2 rounded-full bg-emerald-500"></div>
                  <span>自定义角色</span>
                  <span class="text-xs text-slate-400 ml-auto">可删除修改</span>
                </div>
              </el-option>
              <el-option label="系统角色" value="system">
                <div class="flex items-center gap-2">
                  <div class="w-2 h-2 rounded-full bg-amber-500"></div>
                  <span>系统角色</span>
                  <span class="text-xs text-slate-400 ml-auto">不可删除</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="flex justify-end gap-3 pt-4 border-t border-slate-100">
          <button
              @click="dialogVisible = false"
              class="px-4 py-2 text-sm font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 hover:text-slate-900 transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-slate-200"
          >
            取消
          </button>
          <button
              @click="handleSubmit"
              :loading="submitLoading"
              class="px-4 py-2 text-sm font-medium text-white bg-slate-900 rounded-lg hover:bg-slate-800 transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-slate-900 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ submitLoading ? '保存中...' : '确认保存' }}
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 删除确认弹窗 -->
    <el-dialog
        v-model="deleteDialogVisible"
        title="确认删除"
        width="400px"
        class="custom-dialog"
        :show-close="false"
    >
      <div class="flex items-start gap-4">
        <div class="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center flex-shrink-0">
          <el-icon :size="20" class="text-red-600"><Warning /></el-icon>
        </div>
        <div>
          <h3 class="text-base font-semibold text-slate-900 mb-1">
            {{ isBatchDelete ? `确定要删除选中的 ${selectedRoles.length} 个角色吗？` : '确定要删除该角色吗？' }}
          </h3>
          <p class="text-sm text-slate-500 leading-relaxed">
            此操作不可撤销。删除后，关联该角色的用户将失去相应权限，请谨慎操作。
          </p>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-3 pt-4 border-t border-slate-100">
          <button
              @click="deleteDialogVisible = false"
              class="px-4 py-2 text-sm font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors duration-200"
          >
            取消
          </button>
          <button
              @click="confirmDelete"
              :loading="deleteLoading"
              class="px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-lg hover:bg-red-700 transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-red-600 focus:ring-offset-2"
          >
            {{ deleteLoading ? '删除中...' : '确认删除' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Plus,
  Delete,
  Search,
  Edit,
  UserFilled,
  User,
  Warning
} from '@element-plus/icons-vue'

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)
const deleteLoading = ref(false)

// 搜索和分页
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表格数据
const roleList = ref([])
const selectedRoles = ref([])

// 弹窗控制
const dialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const isEditing = ref(false)
const isBatchDelete = ref(false)
const currentRow = ref(null)

// 表单引用
const formRef = ref(null)

// 表单数据
const form = reactive({
  id: null,
  name: '',
  code: '',
  type: 'custom',
  description: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入角色代码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '代码只能包含大写字母和下划线', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择角色类型', trigger: 'change' }
  ]
}

// 模拟数据（实际项目中应该从 API 获取）
const mockData = [
  {
    id: 1,
    name: '超级管理员',
    code: 'ROLE_ADMIN',
    type: 'system',
    description: '系统最高权限，可管理所有资源',
    createTime: '2024-01-15T08:30:00'
  },
  {
    id: 2,
    name: '普通用户',
    code: 'ROLE_USER',
    type: 'system',
    description: '普通员工权限，可访问基础功能',
    createTime: '2024-01-15T08:30:00'
  },
  {
    id: 3,
    name: '部门经理',
    code: 'ROLE_MANAGER',
    type: 'custom',
    description: '部门管理权限，可查看部门数据',
    createTime: '2024-02-20T14:22:00'
  },
  {
    id: 4,
    name: '审计员',
    code: 'ROLE_AUDITOR',
    type: 'custom',
    description: '只读权限，用于安全审计',
    createTime: '2024-03-01T09:15:00'
  }
]

// 初始化
onMounted(() => {
  fetchRoleList()
})

// 获取角色列表
const fetchRoleList = async () => {
  loading.value = true
  try {
    // 模拟 API 请求
    await new Promise(resolve => setTimeout(resolve, 500))

    let filtered = [...mockData]

    // 搜索过滤
    if (searchQuery.value) {
      const query = searchQuery.value.toLowerCase()
      filtered = filtered.filter(item =>
          item.name.toLowerCase().includes(query) ||
          item.code.toLowerCase().includes(query)
      )
    }

    total.value = filtered.length
    roleList.value = filtered.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value)
  } catch (error) {
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchRoleList()
}

// 多选变化
const handleSelectionChange = (selection) => {
  selectedRoles.value = selection
}

// 创建角色
const handleCreate = () => {
  isEditing.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑角色
const handleEdit = (row) => {
  isEditing.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 模拟 API 请求
        await new Promise(resolve => setTimeout(resolve, 800))

        if (isEditing.value) {
          const index = mockData.findIndex(item => item.id === form.id)
          if (index !== -1) {
            mockData[index] = { ...mockData[index], ...form }
          }
          ElMessage.success('角色更新成功')
        } else {
          const newRole = {
            ...form,
            id: Date.now(),
            createTime: new Date().toISOString()
          }
          mockData.unshift(newRole)
          ElMessage.success('角色创建成功')
        }

        dialogVisible.value = false
        fetchRoleList()
      } catch (error) {
        ElMessage.error(isEditing.value ? '更新失败' : '创建失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 删除角色
const handleDelete = (row) => {
  if (row.type === 'system') {
    ElMessage.warning('系统角色不可删除')
    return
  }
  isBatchDelete.value = false
  currentRow.value = row
  deleteDialogVisible.value = true
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedRoles.value.length === 0) return
  isBatchDelete.value = true
  deleteDialogVisible.value = true
}

// 确认删除
const confirmDelete = async () => {
  deleteLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 600))

    if (isBatchDelete.value) {
      // 过滤掉系统角色
      const deletableIds = selectedRoles.value
          .filter(item => item.type !== 'system')
          .map(item => item.id)

      const indices = mockData.map((item, index) => deletableIds.includes(item.id) ? index : -1).filter(i => i !== -1)
      indices.reverse().forEach(index => mockData.splice(index, 1))

      const actualDeleted = deletableIds.length
      ElMessage.success(`成功删除 ${actualDeleted} 个角色`)
      selectedRoles.value = []
    } else {
      const index = mockData.findIndex(item => item.id === currentRow.value.id)
      if (index !== -1) {
        mockData.splice(index, 1)
        ElMessage.success('角色删除成功')
      }
    }

    deleteDialogVisible.value = false
    fetchRoleList()
  } catch (error) {
    ElMessage.error('删除失败')
  } finally {
    deleteLoading.value = false
  }
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchRoleList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchRoleList()
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.name = ''
  form.code = ''
  form.type = 'custom'
  form.description = ''
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}
</script>

<style scoped>
/* 自定义 Element Plus 样式以匹配 shadcn 风格 */
:deep(.el-table) {
  --el-table-border-color: #e2e8f0;
  --el-table-header-bg-color: #f8fafc;
  --el-table-row-hover-bg-color: #f1f5f9;
}

:deep(.el-table th) {
  font-weight: 600;
  height: 48px;
  color: #475569;
}

:deep(.el-table td) {
  padding: 12px 0;
  color: #334155;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  border-radius: 0.5rem;
  padding: 0 12px;
  height: 40px;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #0f172a inset;
}

:deep(.el-textarea__inner) {
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  border-radius: 0.5rem;
  padding: 8px 12px;
}

:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 2px #0f172a inset;
}

:deep(.el-select .el-input__wrapper) {
  height: 40px;
}

:deep(.custom-pagination .el-pagination__sizes) {
  margin-right: 0;
}

:deep(.el-dialog__header) {
  margin: 0;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}

:deep(.el-dialog__title) {
  font-weight: 600;
  font-size: 1.125rem;
  color: #0f172a;
}

:deep(.el-dialog__body) {
  padding: 20px 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #e2e8f0;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #334155;
  padding-bottom: 6px;
}

/* 滚动条样式 */
:deep(.el-table__body-wrapper::-webkit-scrollbar) {
  width: 6px;
  height: 6px;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-thumb) {
  background: #cbd5e1;
  border-radius: 3px;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-thumb:hover) {
  background: #94a3b8;
}
</style>