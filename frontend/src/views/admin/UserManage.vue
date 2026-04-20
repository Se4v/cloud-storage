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
            <option value="1">启用</option>
            <option value="0">禁用</option>
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
      <el-table
        v-loading="loading"
        :data="paginatedTableData"
        row-key="id"
        @selection-change="handleSelectionChange"
        class="w-full"
        header-cell-class-name="!bg-slate-50 !text-slate-700 !font-semibold !border-b !border-slate-200"
        row-class-name="hover:bg-slate-50/50 transition-colors"
      >
        <el-table-column type="selection" width="48" align="center" />

        <el-table-column label="用户" min-width="160">
          <template #default="{ row }">
            <div class="flex flex-col">
              <span class="font-medium text-slate-900">{{ row.username }}</span>
              <span class="text-xs text-slate-500 mt-0.5">{{ row.realName || '-' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="联系方式" min-width="160">
          <template #default="{ row }">
            <div class="flex flex-col">
              <span class="text-slate-900">{{ row.mobile || '-' }}</span>
              <span class="text-xs text-slate-500 mt-0.5">{{ row.email || '-' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="空间配额" min-width="100">
          <template #default="{ row }">
            <span class="text-slate-700 font-medium">{{ formatStorage(row.storageQuota) }}</span>
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

        <el-table-column label="操作" width="140" fixed="right" align="right">
          <template #default="{ row }">
            <div class="flex items-center justify-end gap-1">
              <button
                @click="handleEdit(row)"
                class="inline-flex items-center justify-center w-8 h-8 rounded-md text-slate-600 hover:text-blue-600 hover:bg-blue-50 transition-colors"
                title="编辑"
              >
                <el-icon :size="16"><Edit /></el-icon>
              </button>
              <button
                @click="openRoleDrawer(row)"
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
                    <el-dropdown-item @click="handleResetPassword(row)" class="text-slate-700">
                      <el-icon class="mr-2"><Lock /></el-icon>
                      重置密码
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="selectedUsers = [row]; handleBatchDelete()" class="text-red-600">
                      <el-icon class="mr-2"><Delete /></el-icon>
                      删除用户
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>

        <!-- 空状态 -->
        <template #empty>
          <div class="flex flex-col items-center justify-center text-slate-400 py-12">
            <el-icon :size="48" class="mb-2 opacity-50"><User /></el-icon>
            <p class="text-sm">暂无用户数据</p>
            <button
              @click="handleAddUser"
              class="mt-3 text-blue-600 hover:text-blue-700 text-sm font-medium"
            >
              添加用户
            </button>
          </div>
        </template>
      </el-table>

      <!-- 分页 -->
      <div class="px-6 py-4 border-t border-slate-200 bg-slate-50/50 flex items-center justify-between">
        <span class="text-sm text-slate-500">
          共 <span class="font-medium text-slate-900">{{ filteredTableData.length }}</span> 条记录
        </span>
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="10"
          :total="total"
          layout="prev, pager, next"
          background
          class="!gap-2"
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

        <div v-if="isEdit">
          <label class="block text-sm font-medium text-slate-700 mb-1.5">邮箱</label>
          <input
              v-model="userForm.email"
              type="email"
              class="w-full h-10 px-3 rounded-md border border-slate-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
              placeholder="请输入邮箱"
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
                  v-model="userForm.isEnabled"
                  :value="1"
                  class="text-blue-600 focus:ring-blue-500 border-slate-300"
              />
              <span class="text-sm text-slate-700">启用</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer">
              <input
                  type="radio"
                  v-model="userForm.isEnabled"
                  :value="0"
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
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
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
import {useUserStore} from "@/stores/user.js";

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

// 搜索与筛选
const searchQuery = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedUsers = ref([])

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 加载用户列表
const loadUserList = async () => {
  loading.value = true
  try {
    const { data: res } = await axios.get(`${API_BASE_URL}/api/user/all`, getAuthConfig())
    if (res.code !== 200) {
      ElMessage.error(res.msg || '加载用户列表失败')
      return
    }
    tableData.value = res.data || []
    total.value = tableData.value.length
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error(error.message || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 全选逻辑（仅选中当前页）
const isAllSelected = computed(() => {
  const selectedIds = selectedUsers.value.map(user => user.id)
  return paginatedTableData.value.length > 0 &&
      paginatedTableData.value.every(user => selectedIds.includes(user.id))
})

// 处理表格选择变化
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
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

// 搜索（前端筛选）
const filteredTableData = computed(() => {
  let data = tableData.value
  
  // 按关键词搜索
  if (searchQuery.value) {
    const keyword = searchQuery.value.toLowerCase()
    data = data.filter(item => 
      item.username?.toLowerCase().includes(keyword) ||
      item.realName?.toLowerCase().includes(keyword) ||
      item.mobile?.includes(keyword)
    )
  }
  
  // 按状态筛选
  if (filterStatus.value !== '') {
    const statusValue = parseInt(filterStatus.value)
    data = data.filter(item => item.isEnabled === statusValue)
  }
  
  return data
})

// 分页后的数据
const paginatedTableData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredTableData.value.slice(start, end)
})

// 搜索
const handleSearch = () => {
  currentPage.value = 1
}

const handleReset = () => {
  searchQuery.value = ''
  filterStatus.value = ''
  currentPage.value = 1
  handleSearch()
}

// 分页
const handleCurrentChange = (val) => {
  currentPage.value = val
  handleSearch()
}

// 批量删除
const handleBatchDelete = async () => {
  if (!selectedUsers.value.length) return
  try {
    const selectedNames = selectedUsers.value.map(row => row.username)
    const msg = `确定要删除${selectedNames.length > 1 ? `选中的${selectedNames.length}个用户`
            : `用户"${selectedNames[0]}"`}吗？此操作不可恢复`
    await ElMessageBox.confirm(msg, '批量删除', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'error',
        dangerouslyUseHTMLString: true
      }
    )
    const { data: res } = await axios.post(`${API_BASE_URL}/api/user/delete`, {
      userIds: selectedUsers.value.map(row => row.id)
    }, getAuthConfig())
    if (res.code !== 200) {
      ElMessage.error(res.data.msg || '批量删除失败')
      return
    }
    ElMessage.success('批量删除成功')
    selectedUsers.value = []
    await loadUserList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error(error.message || '批量删除失败')
    }
  }
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
  email: '',
  storageQuota: 10,
  isEnabled: 1
})

const handleAddUser = () => {
  isEdit.value = false
  Object.assign(userForm, {
    id: null,
    username: '',
    realName: '',
    mobile: '',
    email: '',
    storageQuota: 10,
    isEnabled: 1
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
    email: row.email || '',
    storageQuota: Math.round(row.storageQuota / 1024 / 1024 / 1024),
    isEnabled: row.isEnabled
  })
  userDialogVisible.value = true
}

const handleSaveUser = async () => {
  if (!userForm.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  saving.value = true
  try {
    // 将GB转换为字节
    const storageQuotaBytes = userForm.storageQuota > 0 ? userForm.storageQuota * 1024 * 1024 * 1024 : 0
    
    if (isEdit.value) {
      // 编辑用户
      const submitData = {
        id: userForm.id,
        realName: userForm.realName,
        mobile: userForm.mobile,
        email: userForm.email || '',
        storageQuota: storageQuotaBytes,
        isEnabled: userForm.isEnabled
      }
      const res = await axios.post(`${API_BASE_URL}/api/user/update`, submitData, getAuthConfig())
      if (res.data.code !== 200) {
        ElMessage.error(res.msg || '修改失败')
        return
      }
      ElMessage.success('修改成功')
      userDialogVisible.value = false
      await loadUserList()
    } else {
      // 创建用户
      const submitData = {
        username: userForm.username,
        realName: userForm.realName,
        mobile: userForm.mobile,
        storageQuota: storageQuotaBytes
      }
      const { data: res } = await axios.post(`${API_BASE_URL}/api/user/create`, submitData, getAuthConfig())
      if (res.code !== 200) {
        ElMessage.error(res.msg || '创建失败')
        return
      }
      ElMessage.success('创建成功')
      userDialogVisible.value = false
      await loadUserList()
    }
  } catch (error) {
    console.error('保存用户失败:', error)
    ElMessage.error(error.message || '保存用户失败')
  } finally {
    saving.value = false
  }
}

// 角色分配抽屉
const roleDrawerVisible = ref(false)
const currentUser = ref(null)
const selectedRoles = ref([])
const roleSaving = ref(false)
const allRoles = ref([])
const rolesLoaded = ref(false)

// 加载角色列表
const loadRoleList = async () => {
  if (rolesLoaded.value) return
  try {
    const { data: res } = await axios.get(`${API_BASE_URL}/api/user/role`, getAuthConfig())
    if (res.code !== 200) {
      ElMessage.error(res.msg || '加载角色列表失败')
      return
    }
    allRoles.value = res.data || []
    rolesLoaded.value = true
  } catch (error) {
    console.error('加载角色列表失败:', error)
    ElMessage.error(error.message || '加载角色列表失败')
  }
}

const openRoleDrawer = async (row) => {
  currentUser.value = row
  // 后端返回的是 roleId 数组
  selectedRoles.value = [...(row.roles || [])]
  // 加载角色列表
  await loadRoleList()
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
  if (!currentUser.value) return
  roleSaving.value = true
  try {
    const { data: res } = await axios.post(`${API_BASE_URL}/api/user/assign`, {
      userId: currentUser.value.id,
      roleIds: selectedRoles.value
    }, getAuthConfig())
    if (res.code !== 200) {
      ElMessage.error(res.msg || '角色分配失败')
      return
    }
    currentUser.value.roles = [...selectedRoles.value]
    ElMessage.success('角色分配成功')
    roleDrawerVisible.value = false
  } catch (error) {
    console.error('角色分配失败:', error)
    ElMessage.error(error.message || '角色分配失败')
  } finally {
    roleSaving.value = false
  }
}

// 其他操作
const handleResetPassword = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户 "${row.username}" 的密码吗？重置后的默认密码将发送至用户手机`,
      '重置密码',
      {
        confirmButtonText: '确定重置',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }
    )
    const { data: res } = await axios.post(`${API_BASE_URL}/api/user/reset`, { userId: row.id }, getAuthConfig())
    if (res.code !== 200) {
      ElMessage.error(res.msg || '重置密码失败')
      return
    }
    ElMessage.success('密码重置成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      ElMessage.error(error.message || '重置密码失败')
    }
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadUserList()
})
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