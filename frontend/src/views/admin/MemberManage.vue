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
          添加成员
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
        <div class="relative w-full sm:w-64">
          <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <el-icon class="text-slate-400" :size="16">
              <Search />
            </el-icon>
          </div>
          <input
              v-model="searchQuery"
              type="text"
              placeholder="搜索用户名或姓名..."
              class="w-full h-9 pl-9 pr-4 rounded-md border border-slate-200 bg-white text-sm placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
              @input="handleSearch"
              @keyup.enter="handleSearch"
          />
        </div>
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
    <div class="flex-1 bg-white rounded-xl border border-slate-200 shadow-sm flex flex-col overflow-hidden">
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

        <el-table-column label="用户名" min-width="200">
          <template #default="{ row }">
            <div class="flex flex-col">
              <span class="font-medium text-slate-900">{{ row.username }}</span>
              <span class="text-xs text-slate-500">{{ row.realName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="部门" min-width="180">
          <template #default="{ row }">
            <div class="flex items-center gap-2 text-slate-600">
              <el-icon class="text-slate-400"><OfficeBuilding /></el-icon>
              <span>{{ row.nodeName || '未分配' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="角色" width="160">
          <template #default="{ row }">
            <span class="text-slate-700">{{ row.roleName || '未分配' }}</span>
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
                title="确认删除该成员？"
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
          :page-size="10"
          :total="total"
          layout="prev, pager, next"
          background
          class="!gap-2"
        />
      </div>
    </div>

    <!-- 添加成员对话框 -->
    <el-dialog
      v-if="!isEdit"
      v-model="dialogVisible"
      title="添加成员"
      width="500px"
      class="!rounded-xl"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
        class="mt-4"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="formData.username"
            placeholder="请输入用户名"
            class="!rounded-lg"
          />
        </el-form-item>

        <el-form-item label="部门" prop="nodeId">
          <el-select
            v-model="formData.nodeId"
            placeholder="请选择部门"
            class="w-full !rounded-lg"
            clearable
          >
            <el-option
              v-for="dept in orgNodeList"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="角色" prop="roleId">
          <el-select
            v-model="formData.roleId"
            placeholder="请选择角色"
            class="w-full !rounded-lg"
            clearable
          >
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            />
          </el-select>
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
            class="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-all duration-200 shadow-sm hover:shadow-md active:scale-95"
          >
            确认
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 编辑成员对话框 -->
    <el-dialog
      v-if="isEdit"
      v-model="dialogVisible"
      title="编辑成员信息"
      width="500px"
      class="!rounded-xl"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
        class="mt-4"
      >
        <el-form-item label="部门" prop="nodeId">
          <el-select
            v-model="formData.nodeId"
            placeholder="请选择部门"
            class="w-full !rounded-lg"
            clearable
          >
            <el-option
              v-for="dept in orgNodeList"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="角色" prop="roleId">
          <el-select
            v-model="formData.roleId"
            placeholder="请选择角色"
            class="w-full !rounded-lg"
            clearable
          >
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            />
          </el-select>
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
            class="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-all duration-200 shadow-sm hover:shadow-md active:scale-95"
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
  OfficeBuilding
} from '@element-plus/icons-vue'
import { useUserStore } from "@/stores/user.js";

const userStore = useUserStore()
const API_BASE_URL = 'http://localhost:8080'

// 获取认证配置
const getAuthConfig = () => {
  const token = userStore.token
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
const total = ref(0)
const selectedRows = ref([])

// 对话框状态
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: null,
  username: '',
  nodeId: null,
  roleId: null
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nodeId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

// 成员列表
const memberList = ref([])

// 部门列表（用于下拉选择）
const orgNodeList = ref([])

// 角色列表（用于下拉选择）
const roleList = ref([])

// 加载成员列表
const loadMemberList = async () => {
  try {
    const res = await axios.get(`${API_BASE_URL}/api/member/all`, getAuthConfig())
    if (res.data.code === 200) {
      memberList.value = res.data.data || []
      total.value = memberList.value.length
    } else {
      ElMessage.error(res.data.msg || '获取成员列表失败')
    }
  } catch (error) {
    console.error('获取成员列表失败:', error)
    ElMessage.error(error.response?.data?.msg || '获取成员列表失败')
  }
}

// 加载部门列表
const loadOrgNodeList = async () => {
  try {
    const res = await axios.get(`${API_BASE_URL}/api/member/org`, getAuthConfig())
    if (res.data.code === 200) {
      orgNodeList.value = res.data.data || []
    } else {
      ElMessage.error(res.data.msg || '获取部门列表失败')
    }
  } catch (error) {
    console.error('获取部门列表失败:', error)
    ElMessage.error(error.response?.data?.msg || '获取部门列表失败')
  }
}

// 加载角色列表
const loadRoleList = async () => {
  try {
    const res = await axios.get(`${API_BASE_URL}/api/member/role`, getAuthConfig())
    if (res.data.code === 200) {
      roleList.value = res.data.data || []
    } else {
      ElMessage.error(res.data.msg || '获取角色列表失败')
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error(error.response?.data?.msg || '获取角色列表失败')
  }
}

// 过滤后的列表
const filteredList = computed(() => {
  if (!searchQuery.value) return memberList.value
  const query = searchQuery.value.toLowerCase()
  return memberList.value.filter(item =>
    item.username.toLowerCase().includes(query) ||
    item.realName.toLowerCase().includes(query)
  )
})

// 表格选择
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
}

// 打开创建对话框
const handleCreate = async () => {
  isEdit.value = false
  Object.assign(formData, {
    id: null,
    username: '',
    nodeId: null,
    roleId: null
  })
  // 加载部门和角色列表
  await Promise.all([loadOrgNodeList(), loadRoleList()])
  dialogVisible.value = true
}

// 打开编辑对话框
const handleEdit = async (row) => {
  isEdit.value = true
  // 加载部门和角色列表
  await Promise.all([loadOrgNodeList(), loadRoleList()])
  // 查找对应的部门和角色ID
  const dept = orgNodeList.value.find(d => d.name === row.nodeName)
  const role = roleList.value.find(r => r.name === row.roleName)
  Object.assign(formData, {
    id: row.id,
    nodeId: dept?.id || null,
    roleId: role?.id || null
  })
  dialogVisible.value = true
}

// 删除单个成员
const handleDelete = async (row) => {
  try {
    const res = await axios.post(`${API_BASE_URL}/api/member/delete`, { memberIds: [row.id] }, getAuthConfig())
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      await loadMemberList()
    } else {
      ElMessage.error(res.data.msg || '删除失败')
    }
  } catch (error) {
    console.error('删除成员失败:', error)
    ElMessage.error(error.response?.data?.msg || '删除失败')
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 个成员吗？`,
      '批量删除',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: '!bg-red-600 !border-red-600'
      }
    )
    const ids = selectedRows.value.map(row => row.id)
    const res = await axios.post(`${API_BASE_URL}/api/member/delete`, { memberIds: ids }, getAuthConfig())
    if (res.data.code === 200) {
      ElMessage.success('批量删除成功')
      selectedRows.value = []
      await loadMemberList()
    } else {
      ElMessage.error(res.data.msg || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error(error.response?.data?.msg || '批量删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          // 更新成员
          const submitData = {
            memberId: formData.id,
            nodeId: formData.nodeId,
            roleId: formData.roleId
          }
          const res = await axios.post(`${API_BASE_URL}/api/member/update`, submitData, getAuthConfig())
          if (res.data.code === 200) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            await loadMemberList()
          } else {
            ElMessage.error(res.data.msg || '更新失败')
          }
        } else {
          // 创建成员
          const submitData = {
            username: formData.username,
            nodeId: formData.nodeId,
            roleId: formData.roleId
          }
          const res = await axios.post(`${API_BASE_URL}/api/member/create`, submitData, getAuthConfig())
          if (res.data.code === 200) {
            ElMessage.success('创建成功')
            dialogVisible.value = false
            await loadMemberList()
          } else {
            ElMessage.error(res.data.msg || '创建失败')
          }
        }
      } catch (error) {
        console.error(isEdit.value ? '更新成员失败:' : '创建成员失败:', error)
        ElMessage.error(error.response?.data?.msg || (isEdit.value ? '更新失败' : '创建失败'))
      }
    }
  })
}

// 初始化加载数据
const loadData = async () => {
  loading.value = true
  await loadMemberList()
  loading.value = false
}

// 页面加载时获取数据
onMounted(() => {
  loadData()
})
</script>

<style scoped>
/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* Element Plus 样式覆盖，贴近 shadcn 风格 */
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
