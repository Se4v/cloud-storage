<template>
  <div class="h-full flex flex-col p-6 bg-slate-50/50">
    <!-- 顶部操作栏 -->
    <div class="mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
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

      <div class="flex items-center gap-3">
        <el-input
          v-model="searchQuery"
          placeholder="搜索用户名或姓名..."
          clearable
          class="w-64 !rounded-lg"
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon class="text-slate-400"><Search /></el-icon>
          </template>
        </el-input>
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
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center text-white text-sm font-medium">
                {{ row.username.charAt(0).toUpperCase() }}
              </div>
              <div class="flex flex-col">
                <span class="font-medium text-slate-900">{{ row.username }}</span>
                <span class="text-xs text-slate-500">{{ row.realName }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="部门" min-width="180">
          <template #default="{ row }">
            <div class="flex items-center gap-2 text-slate-600">
              <el-icon class="text-slate-400"><OfficeBuilding /></el-icon>
              <span>{{ row.departmentName || '未分配' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="角色" width="160">
          <template #default="{ row }">
            <el-tag
              :type="getRoleType(row.role)"
              effect="light"
              class="!rounded-md !border-0 !font-medium"
              :class="getRoleClass(row.role)"
            >
              {{ row.roleName || '未分配' }}
            </el-tag>
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
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="prev, pager, next, sizes"
          background
          class="!gap-2"
        />
      </div>
    </div>

    <!-- 新建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑成员信息' : '添加成员'"
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
            :disabled="isEdit"
          />
        </el-form-item>

        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码"
            class="!rounded-lg"
            show-password
          />
        </el-form-item>

        <el-form-item label="姓名" prop="realName">
          <el-input
            v-model="formData.realName"
            placeholder="请输入真实姓名"
            class="!rounded-lg"
          />
        </el-form-item>

        <el-form-item label="部门" prop="departmentId">
          <el-select
            v-model="formData.departmentId"
            placeholder="请选择部门"
            class="w-full !rounded-lg"
            clearable
          >
            <el-option
              v-for="dept in departmentList"
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
            >
              <div class="flex items-center gap-2">
                <span
                  class="w-2 h-2 rounded-full"
                  :class="getRoleDotClass(role.type)"
                ></span>
                <span>{{ role.name }}</span>
              </div>
            </el-option>
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
import { ref, computed, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Delete,
  Search,
  Edit,
  OfficeBuilding
} from '@element-plus/icons-vue'

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
  username: '',
  password: '',
  realName: '',
  departmentId: null,
  roleId: null
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

// 模拟成员数据
const memberList = ref([
  { id: 1, username: 'zhangsan', realName: '张三', departmentId: 2, departmentName: '技术研发中心', role: 'admin', roleName: '管理员' },
  { id: 2, username: 'lisi', realName: '李四', departmentId: 2, departmentName: '技术研发中心', role: 'manager', roleName: '部门经理' },
  { id: 3, username: 'wangwu', realName: '王五', departmentId: 3, departmentName: '前端开发部', role: 'member', roleName: '普通成员' },
  { id: 4, username: 'zhaoliu', realName: '赵六', departmentId: 4, departmentName: '后端开发部', role: 'member', roleName: '普通成员' },
  { id: 5, username: 'qianqi', realName: '钱七', departmentId: 5, departmentName: '产品设计部', role: 'manager', roleName: '部门经理' },
  { id: 6, username: 'sunba', realName: '孙八', departmentId: 6, departmentName: 'UI设计组', role: 'member', roleName: '普通成员' },
  { id: 7, username: 'zhoujiu', realName: '周九', departmentId: 7, departmentName: '用户体验组', role: 'member', roleName: '普通成员' },
  { id: 8, username: 'wushi', realName: '吴十', departmentId: 8, departmentName: '市场运营部', role: 'admin', roleName: '管理员' }
])

// 部门列表（用于下拉选择）
const departmentList = ref([
  { id: 1, name: '总经办' },
  { id: 2, name: '技术研发中心' },
  { id: 3, name: '前端开发部' },
  { id: 4, name: '后端开发部' },
  { id: 5, name: '产品设计部' },
  { id: 6, name: 'UI设计组' },
  { id: 7, name: '用户体验组' },
  { id: 8, name: '市场运营部' }
])

// 角色列表（用于下拉选择）
const roleList = ref([
  { id: 1, name: '管理员', type: 'admin' },
  { id: 2, name: '部门经理', type: 'manager' },
  { id: 3, name: '普通成员', type: 'member' },
  { id: 4, name: '访客', type: 'guest' }
])

// 过滤后的列表
const filteredList = computed(() => {
  if (!searchQuery.value) return memberList.value
  const query = searchQuery.value.toLowerCase()
  return memberList.value.filter(item =>
    item.username.toLowerCase().includes(query) ||
    item.realName.toLowerCase().includes(query)
  )
})

// 角色标签样式
const getRoleType = (role) => {
  const map = { admin: 'danger', manager: 'warning', member: 'primary', guest: 'info' }
  return map[role] || 'info'
}

const getRoleClass = (role) => {
  const map = {
    admin: '!bg-red-50 !text-red-700',
    manager: '!bg-amber-50 !text-amber-700',
    member: '!bg-blue-50 !text-blue-700',
    guest: '!bg-slate-100 !text-slate-700'
  }
  return map[role] || '!bg-slate-100 !text-slate-700'
}

// 角色圆点样式
const getRoleDotClass = (type) => {
  const map = {
    admin: 'bg-red-500',
    manager: 'bg-amber-500',
    member: 'bg-blue-500',
    guest: 'bg-slate-400'
  }
  return map[type] || 'bg-slate-400'
}

// 表格选择
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
}

// 打开创建对话框
const handleCreate = () => {
  isEdit.value = false
  Object.assign(formData, {
    id: null,
    username: '',
    password: '',
    realName: '',
    departmentId: null,
    roleId: null
  })
  dialogVisible.value = true
}

// 打开编辑对话框
const handleEdit = (row) => {
  isEdit.value = true
  // 查找对应的部门和角色ID
  const dept = departmentList.value.find(d => d.name === row.departmentName)
  const role = roleList.value.find(r => r.name === row.roleName)
  Object.assign(formData, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    departmentId: dept?.id || null,
    roleId: role?.id || null
  })
  dialogVisible.value = true
}

// 删除单个成员
const handleDelete = (row) => {
  const index = memberList.value.findIndex(item => item.id === row.id)
  if (index > -1) {
    memberList.value.splice(index, 1)
    ElMessage.success('删除成功')
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
    memberList.value = memberList.value.filter(item => !ids.includes(item.id))
    selectedRows.value = []
    ElMessage.success('批量删除成功')
  } catch {
    // 取消删除
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (valid) {
      const dept = departmentList.value.find(d => d.id === formData.departmentId)
      const role = roleList.value.find(r => r.id === formData.roleId)
      
      if (isEdit.value) {
        const index = memberList.value.findIndex(item => item.id === formData.id)
        if (index > -1) {
          memberList.value[index] = {
            ...memberList.value[index],
            realName: formData.realName,
            departmentId: formData.departmentId,
            departmentName: dept?.name || '',
            role: role?.type || '',
            roleName: role?.name || ''
          }
          ElMessage.success('更新成功')
        }
      } else {
        const newId = Math.max(...memberList.value.map(item => item.id)) + 1
        memberList.value.push({
          id: newId,
          username: formData.username,
          realName: formData.realName,
          departmentId: formData.departmentId,
          departmentName: dept?.name || '',
          role: role?.type || '',
          roleName: role?.name || ''
        })
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
    }
  })
}

// 初始化加载数据
const loadData = () => {
  loading.value = true
  setTimeout(() => {
    total.value = memberList.value.length
    loading.value = false
  }, 500)
}

loadData()
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
