<template>
  <div class="p-6 max-w-[1600px] mx-auto space-y-6">
    <!-- 控制栏：左侧操作按钮，右侧搜索 -->
    <div class="flex flex-col lg:flex-row lg:items-center justify-between gap-4 bg-white rounded-xl border border-slate-200 shadow-sm p-4">
      <!-- 左侧：操作按钮 -->
      <div class="flex items-center gap-3">
        <button
            @click="handleAddUser"
            class="inline-flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-700 text-white h-9 px-4 rounded-md text-sm font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
        >
          <el-icon :size="16"><Plus /></el-icon>
          添加用户
        </button>

        <button
            @click="handleBatchDelete"
            :disabled="selectedUsers.length === 0"
            :class="[
            'inline-flex items-center justify-center gap-2 h-9 px-4 rounded-md text-sm font-medium transition-colors border',
            selectedUsers.length > 0
              ? 'border-red-200 text-red-600 hover:bg-red-50 bg-white'
              : 'border-slate-200 text-slate-400 cursor-not-allowed bg-slate-50'
          ]"
        >
          <el-icon :size="16"><Delete /></el-icon>
          批量删除
        </button>
      </div>

      <!-- 右侧：搜索筛选 -->
      <div class="flex flex-col sm:flex-row gap-3">
        <div class="relative w-full sm:w-64">
          <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <el-icon class="text-slate-400" :size="16">
              <Search />
            </el-icon>
          </div>
          <input
              v-model="searchQuery"
              type="text"
              placeholder="搜索用户名、姓名或手机号..."
              class="w-full h-9 pl-9 pr-4 rounded-md border border-slate-200 bg-white text-sm placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
              @keyup.enter="handleSearch"
          />
        </div>

        <div class="flex gap-2">
          <select
              v-model="filterStatus"
              class="h-9 px-3 rounded-md border border-slate-200 bg-white text-sm text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent cursor-pointer"
          >
            <option value="">全部状态</option>
            <option value="active">正常</option>
            <option value="disabled">禁用</option>
          </select>

          <button
              @click="handleSearch"
              class="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-all duration-200 shadow-sm hover:shadow-md active:scale-95"
          >
            <el-icon :size="16"><Search /></el-icon>
            查询
          </button>
          <button
              @click="handleReset"
              class="h-9 px-4 border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 rounded-md text-sm font-medium transition-colors"
          >
            重置
          </button>
        </div>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-sm text-left">
          <thead class="bg-slate-50 border-b border-slate-200">
          <tr>
            <th class="px-6 py-3.5 font-semibold text-slate-700 w-10">
              <input
                  type="checkbox"
                  :checked="isAllSelected"
                  @change="toggleSelectAll"
                  class="rounded border-slate-300 text-blue-600 focus:ring-blue-500"
              />
            </th>
            <th class="px-6 py-3.5 font-semibold text-slate-700">用户</th>
            <th class="px-6 py-3.5 font-semibold text-slate-700">联系方式</th>
            <th class="px-6 py-3.5 font-semibold text-slate-700">空间配额</th>
            <th class="px-6 py-3.5 font-semibold text-slate-700">状态</th>
            <th class="px-6 py-3.5 font-semibold text-slate-700 text-right">操作</th>
          </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
          <tr
              v-for="user in tableData"
              :key="user.id"
              class="hover:bg-slate-50/50 transition-colors"
          >
            <td class="px-6 py-4">
              <input
                  type="checkbox"
                  v-model="selectedUsers"
                  :value="user.id"
                  class="rounded border-slate-300 text-blue-600 focus:ring-blue-500"
              />
            </td>
            <td class="px-6 py-4">
              <div class="flex flex-col">
                <span class="font-medium text-slate-900">{{ user.username }}</span>
                <span class="text-xs text-slate-500 mt-0.5">{{ user.realName || '-' }}</span>
              </div>
            </td>
            <td class="px-6 py-4">
              <div class="flex flex-col">
                <span class="text-slate-900">{{ user.mobile || '-' }}</span>
                <span class="text-xs text-slate-500 mt-0.5">{{ user.email || '-' }}</span>
              </div>
            </td>
            <td class="px-6 py-4">
              <span class="text-slate-700 font-medium">{{ formatStorage(user.storageQuota) }}</span>
            </td>
            <td class="px-6 py-4">
                <span
                    :class="[
                    'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border',
                    user.status === 'active'
                      ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                      : 'bg-red-50 text-red-700 border-red-200'
                  ]"
                >
                  <span
                      :class="[
                      'w-1.5 h-1.5 rounded-full mr-1.5',
                      user.status === 'active' ? 'bg-emerald-500' : 'bg-red-500'
                    ]"
                  ></span>
                  {{ user.status === 'active' ? '正常' : '禁用' }}
                </span>
            </td>
            <td class="px-6 py-4 text-right">
              <div class="flex items-center justify-end gap-1">
                <button
                    @click="handleEdit(user)"
                    class="inline-flex items-center justify-center w-8 h-8 rounded-md text-slate-600 hover:text-blue-600 hover:bg-blue-50 transition-colors"
                    title="编辑"
                >
                  <el-icon :size="16"><Edit /></el-icon>
                </button>
                <button
                    @click="openRoleDrawer(user)"
                    class="inline-flex items-center justify-center w-8 h-8 rounded-md text-slate-600 hover:text-purple-600 hover:bg-purple-50 transition-colors"
                    title="分配角色"
                >
                  <el-icon :size="16"><UserFilled /></el-icon>
                </button>
                <el-dropdown trigger="click">
                  <button
                      class="inline-flex items-center justify-center w-8 h-8 rounded-md text-slate-600 hover:text-slate-900 hover:bg-slate-100 transition-colors"
                  >
                    <el-icon :size="16"><MoreFilled /></el-icon>
                  </button>
                  <template #dropdown>
                    <el-dropdown-menu class="min-w-[120px]">
                      <el-dropdown-item @click="handleResetPassword(user)" class="text-slate-700">
                        <el-icon class="mr-2"><Lock /></el-icon>
                        重置密码
                      </el-dropdown-item>
                      <el-dropdown-item divided @click="handleDelete(user)" class="text-red-600">
                        <el-icon class="mr-2"><Delete /></el-icon>
                        删除用户
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </td>
          </tr>

          <!-- 空状态 -->
          <tr v-if="tableData.length === 0">
            <td colspan="6" class="px-6 py-12 text-center">
              <div class="flex flex-col items-center justify-center text-slate-400">
                <el-icon :size="48" class="mb-2 opacity-50"><User /></el-icon>
                <p class="text-sm">暂无用户数据</p>
                <button
                    @click="handleAddUser"
                    class="mt-3 text-blue-600 hover:text-blue-700 text-sm font-medium"
                >
                  添加用户
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

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

    <!-- 角色分配抽屉 -->
    <el-drawer
        v-model="roleDrawerVisible"
        :title="`分配角色 - ${currentUser?.username || ''}`"
        size="420px"
        :destroy-on-close="true"
        class="role-drawer"
    >
      <div class="h-full flex flex-col bg-white">
        <!-- 用户信息摘要 -->
        <div class="px-6 py-4 bg-slate-50 border-b border-slate-100">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-blue-100 text-blue-600 flex items-center justify-center font-semibold text-lg">
              {{ currentUser?.username?.charAt(0)?.toUpperCase() || 'U' }}
            </div>
            <div>
              <div class="font-medium text-slate-900">{{ currentUser?.realName || currentUser?.username }}</div>
              <div class="text-xs text-slate-500">{{ currentUser?.mobile || '暂无手机号' }}</div>
            </div>
          </div>
        </div>

        <!-- 角色选择区 -->
        <div class="flex-1 overflow-y-auto p-6">
          <div class="mb-4">
            <h3 class="text-sm font-semibold text-slate-900 mb-1">选择全局角色</h3>
            <p class="text-xs text-slate-500">为用户分配一个或多个角色，权限将实时生效</p>
          </div>

          <div class="space-y-2">
            <div
                v-for="role in allRoles"
                :key="role.id"
                @click="toggleRole(role.id)"
                :class="[
                'group relative flex items-center gap-3 p-3 rounded-lg border cursor-pointer transition-all',
                selectedRoles.includes(role.id)
                  ? 'border-blue-500 bg-blue-50/30 shadow-sm'
                  : 'border-slate-200 hover:border-slate-300 hover:bg-slate-50'
              ]"
            >
              <div
                  :class="[
                  'w-5 h-5 rounded border flex items-center justify-center transition-colors',
                  selectedRoles.includes(role.id)
                    ? 'bg-blue-600 border-blue-600'
                    : 'border-slate-300 bg-white'
                ]"
              >
                <el-icon v-if="selectedRoles.includes(role.id)" color="white" :size="12"><Check /></el-icon>
              </div>

              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2">
                  <span class="text-sm font-medium text-slate-900">{{ role.name }}</span>
                </div>
                <p class="mt-0.5 text-xs text-slate-500 leading-relaxed truncate">
                  {{ role.description }}
                </p>
              </div>
            </div>
          </div>


        </div>

        <!-- 底部操作 -->
        <div class="border-t border-slate-200 p-4 bg-slate-50 flex justify-end gap-3">
          <button
              @click="roleDrawerVisible = false"
              class="px-4 py-2 rounded-md border border-slate-200 bg-white text-slate-700 text-sm font-medium hover:bg-slate-50 transition-colors"
          >
            取消
          </button>
          <button
              @click="saveRoles"
              :disabled="roleSaving"
              class="px-4 py-2 rounded-md bg-blue-600 text-white text-sm font-medium hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center gap-2"
          >
            <el-icon v-if="roleSaving" class="animate-spin"><Loading /></el-icon>
            {{ roleSaving ? '保存中...' : '确认分配' }}
          </button>
        </div>
      </div>
    </el-drawer>

    <!-- 编辑/新增用户弹窗 -->
    <el-dialog
        v-model="userDialogVisible"
        :title="isEdit ? '编辑用户' : '添加用户'"
        width="500px"
        destroy-on-close
        class="user-dialog"
    >
      <form @submit.prevent="handleSaveUser" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1.5">用户名 <span class="text-red-500">*</span></label>
          <input
              v-model="userForm.username"
              type="text"
              :disabled="isEdit"
              class="w-full h-10 px-3 rounded-md border border-slate-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm disabled:bg-slate-50 disabled:text-slate-500"
              placeholder="请输入用户名"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1.5">姓名</label>
          <input
              v-model="userForm.realName"
              type="text"
              class="w-full h-10 px-3 rounded-md border border-slate-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
              placeholder="请输入真实姓名"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1.5">手机号</label>
          <input
              v-model="userForm.mobile"
              type="tel"
              class="w-full h-10 px-3 rounded-md border border-slate-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
              placeholder="请输入手机号"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1.5">空间配额 <span class="text-red-500">*</span></label>
          <div class="relative">
            <input
                v-model.number="userForm.storageQuota"
                type="number"
                min="0"
                class="w-full h-10 px-3 pr-12 rounded-md border border-slate-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                placeholder="请输入空间配额"
            />
            <span class="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-slate-500">GB</span>
          </div>
          <p class="mt-1 text-xs text-slate-500">0 表示无限制</p>
        </div>

        <div v-if="isEdit">
          <label class="block text-sm font-medium text-slate-700 mb-1.5">状态</label>
          <div class="flex gap-4">
            <label class="flex items-center gap-2 cursor-pointer">
              <input
                  type="radio"
                  v-model="userForm.status"
                  value="active"
                  class="text-blue-600 focus:ring-blue-500 border-slate-300"
              />
              <span class="text-sm text-slate-700">正常</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer">
              <input
                  type="radio"
                  v-model="userForm.status"
                  value="disabled"
                  class="text-blue-600 focus:ring-blue-500 border-slate-300"
              />
              <span class="text-sm text-slate-700">禁用</span>
            </label>
          </div>
        </div>
      </form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <button
              @click="userDialogVisible = false"
              class="px-4 py-2 rounded-md border border-slate-200 text-slate-700 text-sm font-medium hover:bg-slate-50 transition-colors"
          >
            取消
          </button>
          <button
              @click="handleSaveUser"
              :disabled="saving"
              class="px-4 py-2 rounded-md bg-blue-600 text-white text-sm font-medium hover:bg-blue-700 disabled:opacity-50 transition-colors"
          >
            {{ saving ? '保存中...' : '保存' }}
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
  Search,
  Edit,
  Delete,
  UserFilled,
  MoreFilled,
  Lock,
  User,
  Loading,
  Check
} from '@element-plus/icons-vue'

// 搜索与筛选
const searchQuery = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const selectedUsers = ref([])

// 表格数据
const tableData = ref([
  {
    id: 1,
    username: 'zhangsan',
    realName: '张三',
    mobile: '13800138000',
    email: 'zhangsan@company.com',
    status: 'active',
    storageQuota: 10737418240, // 10GB in bytes
    roles: [1, 3]
  },
  {
    id: 2,
    username: 'lisi',
    realName: '李四',
    mobile: '13900139000',
    email: 'lisi@company.com',
    status: 'active',
    storageQuota: 5368709120,  // 5GB
    roles: [2]
  },
  {
    id: 3,
    username: 'wangwu',
    realName: '王五',
    mobile: '13700137000',
    email: 'wangwu@company.com',
    status: 'disabled',
    storageQuota: 21474836480, // 20GB
    roles: []
  }
])

// 全选逻辑
const isAllSelected = computed(() => {
  return tableData.value.length > 0 && selectedUsers.value.length === tableData.value.length
})

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedUsers.value = []
  } else {
    selectedUsers.value = tableData.value.map(item => item.id)
  }
}

// 格式化存储空间
const formatStorage = (bytes) => {
  if (bytes === 0) return '0 B'
  if (!bytes) return '-'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  // TODO: 调用API
  ElMessage.success('搜索: ' + searchQuery.value)
}

const handleReset = () => {
  searchQuery.value = ''
  filterStatus.value = ''
  currentPage.value = 1
  handleSearch()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  handleSearch()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  handleSearch()
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedUsers.value.length === 0) return
  ElMessageBox.confirm(
      `确定要删除选中的 ${selectedUsers.value.length} 个用户吗？<br><span class="text-xs text-slate-500">此操作不可恢复</span>`,
      '批量删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'error',
        dangerouslyUseHTMLString: true
      }
  ).then(() => {
    tableData.value = tableData.value.filter(item => !selectedUsers.value.includes(item.id))
    selectedUsers.value = []
    ElMessage.success('批量删除成功')
  }).catch(() => {})
}

// 用户编辑/新增
const userDialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const userForm = reactive({
  id: null,
  username: '',
  realName: '',
  mobile: '',
  storageQuota: 10,
  status: 'active'
})

const handleAddUser = () => {
  isEdit.value = false
  Object.assign(userForm, {
    id: null,
    username: '',
    realName: '',
    mobile: '',
    storageQuota: 10,
    status: 'active'
  })
  userDialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(userForm, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    mobile: row.mobile,
    storageQuota: Math.round(row.storageQuota / 1024 / 1024 / 1024),
    status: row.status
  })
  userDialogVisible.value = true
}

const handleSaveUser = async () => {
  if (!userForm.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  saving.value = true
  // 将GB转换为字节
  const storageQuotaBytes = userForm.storageQuota > 0 ? userForm.storageQuota * 1024 * 1024 * 1024 : 0
  const submitData = {
    ...userForm,
    storageQuota: storageQuotaBytes
  }
  // TODO: API调用，使用submitData
  console.log('提交数据:', submitData)
  setTimeout(() => {
    saving.value = false
    userDialogVisible.value = false
    ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
  }, 500)
}

// 角色分配抽屉
const roleDrawerVisible = ref(false)
const currentUser = ref(null)
const selectedRoles = ref([])
const roleSaving = ref(false)
const allRoles = ref([
  { id: 1, name: '系统管理员'},
  { id: 2, name: '普通用户'},
  { id: 3, name: '安全审计员'},
  { id: 4, name: '数据分析师'},
  { id: 5, name: '运维工程师'}
])

const openRoleDrawer = (row) => {
  currentUser.value = row
  // 后端返回的是 roleId 数组
  selectedRoles.value = row.roles || []
  roleDrawerVisible.value = true
}

const toggleRole = (roleId) => {
  const index = selectedRoles.value.indexOf(roleId)
  if (index > -1) {
    selectedRoles.value.splice(index, 1)
  } else {
    selectedRoles.value.push(roleId)
  }
}

const saveRoles = async () => {
  roleSaving.value = true
  // TODO: API调用
  setTimeout(() => {
    // 更新本地数据，只保存 roleId 数组
    if (currentUser.value) {
      currentUser.value.roles = [...selectedRoles.value]
    }
    roleSaving.value = false
    roleDrawerVisible.value = false
    ElMessage.success('角色分配成功')
  }, 600)
}

// 其他操作
const handleResetPassword = (row) => {
  ElMessageBox.confirm(
      `确定要重置用户 "${row.username}" 的密码吗？<br><span class="text-xs text-slate-500">重置后的默认密码将发送至用户手机</span>`,
      '重置密码',
      {
        confirmButtonText: '确定重置',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }
  ).then(() => {
    ElMessage.success('密码重置成功')
  }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
      `确定要删除用户 "${row.username}" 吗？<br><span class="text-xs text-slate-500">此操作将删除该用户的所有数据，且不可恢复</span>`,
      '删除用户',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'error',
        dangerouslyUseHTMLString: true
      }
  ).then(() => {
    const index = tableData.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      tableData.value.splice(index, 1)
    }
    selectedUsers.value = selectedUsers.value.filter(id => id !== row.id)
    ElMessage.success('删除成功')
  }).catch(() => {})
}
</script>

<style scoped>
/* 分页器样式 */
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #2563eb;
  border-radius: 6px;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
  color: #2563eb;
}

:deep(.el-dropdown-menu__item) {
  font-size: 13px;
  padding: 8px 16px;
  line-height: 1.5;
}

:deep(.el-dropdown-menu__item:not(.is-disabled):hover) {
  background-color: rgb(241 245 249);
  color: rgb(15 23 42);
}

:deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 16px 24px;
  border-bottom: 1px solid rgb(226 232 240);
  color: rgb(15 23 42);
  font-weight: 600;
  font-size: 16px;
}

:deep(.el-drawer__body) {
  padding: 0;
}

:deep(.el-dialog__header) {
  margin-right: 0;
  padding: 20px 24px;
  border-bottom: 1px solid rgb(226 232 240);
}

:deep(.el-dialog__title) {
  font-weight: 600;
  color: rgb(15 23 42);
  font-size: 16px;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid rgb(226 232 240);
}

/* 复选框样式优化 */
input[type="checkbox"] {
  cursor: pointer;
}

/* 平滑滚动 */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgb(203 213 225);
  border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgb(148 163 184);
}
</style>