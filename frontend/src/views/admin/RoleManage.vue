<template>
  <div class="p-6 max-w-7xl mx-auto space-y-6">
    <!-- 操作栏卡片 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-4">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <!-- 左侧操作按钮 -->
        <div class="flex items-center gap-3">
          <button
              @click="handleCreate"
              class="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-all duration-200 shadow-sm hover:shadow-md active:scale-95"
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
          <button
            @click="handleSearch"
            class="inline-flex items-center justify-center gap-2 h-9 px-4 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors whitespace-nowrap"
          >
            <el-icon :size="16"><Search /></el-icon>
            <span>搜索</span>
          </button>
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
          header-cell-class-name="!bg-slate-50 !text-slate-700 !font-semibold !border-b !border-slate-200"
          row-class-name="hover:bg-slate-50/50 transition-colors duration-150"
      >
        <el-table-column type="selection" width="48" align="center" />

        <el-table-column label="角色名称" min-width="160">
          <template #default="{ row }">
            <div>
              <div class="font-medium text-slate-900">{{ row.name }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="角色代码" min-width="160">
          <template #default="{ row }">
            <code class="px-2 py-1 bg-slate-100 text-slate-700 rounded text-xs font-mono border border-slate-200">
              {{ row.code }}
            </code>
          </template>
        </el-table-column>

        <el-table-column label="类型" min-width="120">
          <template #default="{ row }">
            <span
                :class="[
                'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border',
                row.type === 'global'
                  ? 'bg-amber-50 text-amber-700 border-amber-200'
                  : 'bg-emerald-50 text-emerald-700 border-emerald-200'
              ]"
            >
              {{ row.type === 'global' ? '全局角色' : '组织角色' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <span
                :class="[
                'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border',
                row.isEnabled
                  ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                  : 'bg-red-50 text-red-700 border-red-200'
              ]"
            >
              <span
                  :class="[
                  'w-1.5 h-1.5 rounded-full mr-1.5',
                  row.isEnabled ? 'bg-emerald-500' : 'bg-red-500'
                ]"
              ></span>
              {{ row.isEnabled ? '启用' : '禁用' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" min-width="140">
          <template #default="{ row }">
            <span class="text-sm text-slate-600">{{ row.createTime }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" min-width="140" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center justify-end gap-2">
              <button
                  v-if="row.type === 'org'"
                  @click="handlePermissionConfig(row)"
                  class="p-2 text-slate-600 hover:text-purple-600 hover:bg-purple-50 rounded-lg transition-colors duration-200"
                  title="配置权限"
              >
                <el-icon :size="16"><Key /></el-icon>
              </button>
              <button
                  @click="handleEdit(row)"
                  class="p-2 text-slate-600 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors duration-200"
                  title="编辑"
              >
                <el-icon :size="16"><Edit /></el-icon>
              </button>
              <button
                  @click="handleDelete(row)"
                  :disabled="row.type === 'global'"
                  :class="[
                  'p-2 rounded-lg transition-colors duration-200',
                  row.type === 'global'
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
                class="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-all duration-200 shadow-sm hover:shadow-md active:scale-95"
            >
              <el-icon :size="16"><Plus /></el-icon>
              新建角色
            </button>
          </div>
        </template>
      </el-table>

      <!-- 分页 -->
      <div class="px-6 py-4 border-t border-slate-200 bg-slate-50/50 flex items-center justify-between">
        <span class="text-sm text-slate-500">
          共 <span class="font-medium text-slate-900">{{ total }}</span> 条记录
        </span>
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="prev, pager, next, sizes"
            background
            class="!gap-2"
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
                :disabled="isEditing && form.type === 'global'"
            >
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
              <el-option label="组织角色" value="org">
                <div class="flex items-center gap-2">
                  <div class="w-2 h-2 rounded-full bg-emerald-500"></div>
                  <span>组织角色</span>
                  <span class="text-xs text-slate-400 ml-auto">可删除修改</span>
                </div>
              </el-option>
              <el-option label="全局角色" value="global">
                <div class="flex items-center gap-2">
                  <div class="w-2 h-2 rounded-full bg-amber-500"></div>
                  <span>全局角色</span>
                  <span class="text-xs text-slate-400 ml-auto">不可删除</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>

          <!-- 编辑时显示状态 -->
          <el-form-item v-if="isEditing" label="状态">
            <div class="flex gap-4">
              <label class="flex items-center gap-2 cursor-pointer">
                <input
                    type="radio"
                    v-model="form.isEnabled"
                    :value="true"
                    class="text-blue-600 focus:ring-blue-500 border-slate-300"
                />
                <span class="text-sm text-slate-700">启用</span>
              </label>
              <label class="flex items-center gap-2 cursor-pointer">
                <input
                    type="radio"
                    v-model="form.isEnabled"
                    :value="false"
                    class="text-blue-600 focus:ring-blue-500 border-slate-300"
                />
                <span class="text-sm text-slate-700">禁用</span>
              </label>
            </div>
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
              class="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-all duration-200 shadow-sm hover:shadow-md active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
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

    <!-- 权限配置对话框 -->
    <el-dialog
        v-model="permDialogVisible"
        :title="`配置权限 - ${currentRole?.name || ''}`"
        width="500px"
        destroy-on-close
        class="custom-dialog"
        :close-on-click-modal="false"
    >
      <div class="p-2">
        <div class="mb-4 p-3 bg-slate-50 rounded-lg border border-slate-100">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-purple-100 text-purple-600 flex items-center justify-center">
              <el-icon :size="20"><Key /></el-icon>
            </div>
            <div>
              <div class="font-medium text-slate-900">{{ currentRole?.name }}</div>
              <div class="text-xs text-slate-500">{{ currentRole?.code }}</div>
            </div>
          </div>
        </div>

        <div class="border border-slate-200 rounded-lg p-4">
          <div class="text-sm font-medium text-slate-700 mb-3">选择权限</div>
          <div v-if="permissionList.length === 0" class="text-sm text-slate-500 py-4 text-center">
            加载权限列表中...
          </div>
          <el-checkbox-group v-else v-model="selectedPermissions" class="space-y-2">
            <div 
              v-for="perm in permissionList" 
              :key="perm.id"
              class="flex items-center p-2 rounded-lg hover:bg-slate-50"
            >
              <el-checkbox :label="perm.id">
                <div class="ml-2">
                  <div class="text-sm font-medium text-slate-700">{{ perm.name }}</div>
                </div>
              </el-checkbox>
            </div>
          </el-checkbox-group>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-3 pt-4 border-t border-slate-100">
          <button
              @click="permDialogVisible = false"
              class="px-4 py-2 text-sm font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors duration-200"
          >
            取消
          </button>
          <button
              @click="savePermissions"
              :loading="permLoading"
              class="px-4 py-2 text-sm font-medium text-white bg-purple-600 rounded-lg hover:bg-purple-700 transition-colors duration-200 shadow-sm hover:shadow-md"
          >
            {{ permLoading ? '保存中...' : '确认保存' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import {
  Plus,
  Delete,
  Search,
  Edit,
  UserFilled,
  User,
  Warning,
  Key
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

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)
const deleteLoading = ref(false)
const permLoading = ref(false)

// 搜索和分页
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表格数据
const roleList = ref([])
const selectedRoles = ref([])

// 权限配置对话框
const permDialogVisible = ref(false)
const currentRole = ref(null)
const selectedPermissions = ref([])

// 权限列表（从后端获取）
const permissionList = ref([])
const permissionsLoaded = ref(false)

// 加载权限列表
const loadPermissionList = async () => {
  if (permissionsLoaded.value) return
  try {
    const res = await axios.get(`${API_BASE_URL}/api/role/perm`, getAuthConfig())
    if (res.data.code === 200) {
      permissionList.value = res.data.data || []
      permissionsLoaded.value = true
    }
  } catch (error) {
    console.error('加载权限列表失败:', error)
  }
}

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
  type: 'org',
  isEnabled: true
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



// 初始化
onMounted(() => {
  loadRoleList()
})

// 获取角色列表
const loadRoleList = async () => {
  loading.value = true
  try {
    const res = await axios.get(`${API_BASE_URL}/api/role/all`, getAuthConfig())
    if (res.data.code === 200) {
      let filtered = res.data.data || []

      // 搜索过滤
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(item =>
            item.name?.toLowerCase().includes(query) ||
            item.code?.toLowerCase().includes(query)
        )
      }

      total.value = filtered.length
      roleList.value = filtered.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value)
    } else {
      ElMessage.error(res.data.msg || '获取角色列表失败')
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error(error.response?.data?.msg || '获取角色列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadRoleList()
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

// 配置权限
const handlePermissionConfig = async (row) => {
  currentRole.value = row
  // 加载权限列表
  await loadPermissionList()
  // 设置已选中的权限（从后端获取）
  selectedPermissions.value = [...(row.permissionIds || row.permissions || [])]
  permDialogVisible.value = true
}

// 保存权限
const savePermissions = async () => {
  if (!currentRole.value) return
  permLoading.value = true
  try {
    const submitData = {
      roleId: currentRole.value.id,
      permissionIds: selectedPermissions.value
    }
    const res = await axios.post(`${API_BASE_URL}/api/role/assign`, submitData, getAuthConfig())
    if (res.data.code === 200) {
      // 更新本地数据
      currentRole.value.permissions = [...selectedPermissions.value]
      ElMessage.success('权限配置成功')
      permDialogVisible.value = false
    } else {
      ElMessage.error(res.data.msg || '权限配置失败')
    }
  } catch (error) {
    console.error('权限配置失败:', error)
    ElMessage.error(error.response?.data?.msg || '权限配置失败')
  } finally {
    permLoading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEditing.value) {
          // 编辑角色
          const submitData = {
            id: form.id,
            name: form.name,
            code: form.code,
            isEnabled: form.isEnabled
          }
          const res = await axios.post(`${API_BASE_URL}/api/role/update`, submitData, getAuthConfig())
          if (res.data.code === 200) {
            ElMessage.success('角色更新成功')
            dialogVisible.value = false
            loadRoleList()
          } else {
            ElMessage.error(res.data.msg || '更新失败')
          }
        } else {
          // 创建角色
          const submitData = {
            name: form.name,
            code: form.code,
            type: form.type
          }
          const res = await axios.post(`${API_BASE_URL}/api/role/create`, submitData, getAuthConfig())
          if (res.data.code === 200) {
            ElMessage.success('角色创建成功')
            dialogVisible.value = false
            loadRoleList()
          } else {
            ElMessage.error(res.data.msg || '创建失败')
          }
        }
      } catch (error) {
        console.error(isEditing.value ? '更新角色失败:' : '创建角色失败:', error)
        ElMessage.error(error.response?.data?.msg || (isEditing.value ? '更新失败' : '创建失败'))
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 删除角色
const handleDelete = (row) => {
  if (row.type === 'global') {
    ElMessage.warning('全局角色不可删除')
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
    if (isBatchDelete.value) {
      // 过滤掉全局角色
      const deletableIds = selectedRoles.value
          .filter(item => item.type !== 'global')
          .map(item => item.id)

      if (deletableIds.length === 0) {
        ElMessage.warning('选中的角色中包含全局角色，无法删除')
        deleteDialogVisible.value = false
        return
      }

      const res = await axios.post(`${API_BASE_URL}/api/role/delete`, deletableIds, getAuthConfig())
      if (res.data.code === 200) {
        ElMessage.success(`成功删除 ${deletableIds.length} 个角色`)
        selectedRoles.value = []
        deleteDialogVisible.value = false
        loadRoleList()
      } else {
        ElMessage.error(res.data.msg || '删除失败')
      }
    } else {
      const res = await axios.post(`${API_BASE_URL}/api/role/delete`, [currentRow.value.id], getAuthConfig())
      if (res.data.code === 200) {
        ElMessage.success('角色删除成功')
        deleteDialogVisible.value = false
        loadRoleList()
      } else {
        ElMessage.error(res.data.msg || '删除失败')
      }
    }
  } catch (error) {
    console.error('删除角色失败:', error)
    ElMessage.error(error.response?.data?.msg || '删除失败')
  } finally {
    deleteLoading.value = false
  }
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  loadRoleList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadRoleList()
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.name = ''
  form.code = ''
  form.type = 'org'
  form.isEnabled = true
  if (formRef.value) {
    formRef.value.resetFields()
  }
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

/* 分页器样式 */
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #2563eb;
  border-radius: 6px;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
  color: #2563eb;
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