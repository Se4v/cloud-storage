<template>
  <div class="p-8 max-w-7xl mx-auto">
    <!-- 筛选区 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-5 mb-6">
      <div class="flex flex-wrap items-center gap-4">
        <!-- 搜索框 -->
        <div class="flex-1 min-w-[300px] max-w-md">
          <div class="relative w-full">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <el-icon :size="16" class="text-slate-400">
                <Search />
              </el-icon>
            </div>
            <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索权限名称或代码..."
                class="w-full pl-10 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-slate-900/10 focus:border-slate-900/30 transition-all"
            />
          </div>
        </div>

        <!-- 类型筛选 -->
        <div class="flex items-center gap-2">
          <span class="text-sm text-slate-500">类型：</span>
          <el-select
              v-model="currentType"
              placeholder="全部类型"
              class="w-28"
              size="default"
          >
            <el-option
                v-for="type in typeFilters"
                :key="type.value"
                :label="type.label"
                :value="type.value"
            />
          </el-select>
        </div>

        <!-- 查询按钮 -->
        <button
            @click="handleSearch"
            class="inline-flex items-center gap-2 px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white text-sm font-medium rounded-lg transition-all duration-200 shadow-sm hover:shadow-md active:scale-95"
        >
          <el-icon :size="16"><Search /></el-icon>
          <span>查询</span>
        </button>
      </div>
    </div>

    <!-- 数据列表 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-left">
          <thead>
          <tr class="border-b border-slate-200 bg-slate-50/50">
            <th class="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">
              权限信息
            </th>
            <th class="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">
              权限代码
            </th>
            <th class="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">
              类型
            </th>
          </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
          <tr
              v-for="permission in filteredPermissions"
              :key="permission.id"
              class="hover:bg-slate-50/80 transition-colors"
          >
            <!-- 名称 -->
            <td class="px-6 py-4">
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-lg bg-slate-100 flex items-center justify-center text-slate-600">
                  <el-icon :size="18"><Key /></el-icon>
                </div>
                <div>
                  <div class="text-sm font-medium text-slate-900">{{ permission.name }}</div>
                </div>
              </div>
            </td>

            <!-- 代码 -->
            <td class="px-6 py-4">
              <code class="px-2 py-1 bg-slate-100 text-slate-700 text-xs font-mono rounded border border-slate-200">
                {{ permission.code }}
              </code>
            </td>

            <!-- 类型 -->
            <td class="px-6 py-4">
                <span :class="[
                  'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                  getTypeStyle(permission.type)
                ]">
                  {{ getTypeLabel(permission.type) }}
                </span>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredPermissions.length === 0" class="py-16 text-center">
        <div class="w-16 h-16 mx-auto mb-4 rounded-full bg-slate-100 flex items-center justify-center text-slate-400">
          <el-icon :size="24"><Search /></el-icon>
        </div>
        <h3 class="text-sm font-medium text-slate-900 mb-1">未找到权限</h3>
        <p class="text-sm text-slate-500">尝试调整搜索关键词或筛选条件</p>
      </div>

      <!-- 分页 -->
      <div v-if="filteredPermissions.length > 0" class="px-6 py-4 border-t border-slate-200 bg-slate-50/50 flex items-center justify-between">
        <span class="text-sm text-slate-500">
          共 <span class="font-medium text-slate-900">{{ filteredPermissions.length }}</span> 条记录
        </span>
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="filteredPermissions.length"
            layout="prev, pager, next, sizes"
            background
            class="!gap-2"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Search, Key } from '@element-plus/icons-vue'

// 权限类型定义
const permissionTypes = [
  { value: 'menu', label: '菜单' },
  { value: 'button', label: '按钮' },
  { value: 'api', label: '接口' },
  { value: 'data', label: '数据' }
]

const typeFilters = [
  { value: 'all', label: '全部' },
  ...permissionTypes
]

// 模拟数据
const permissions = ref([
  {
    id: 1,
    name: '用户查看',
    code: 'user:view',
    type: 'menu',
    status: 'enabled'
  },
  {
    id: 2,
    name: '用户创建',
    code: 'user:create',
    type: 'button',
    status: 'enabled'
  },
  {
    id: 3,
    name: '文件上传',
    code: 'file:upload',
    type: 'api',
    status: 'enabled'
  },
  {
    id: 4,
    name: '团队管理',
    code: 'team:manage',
    type: 'menu',
    status: 'disabled'
  },
  {
    id: 5,
    name: '数据导出',
    code: 'data:export',
    type: 'data',
    status: 'enabled'
  },
  {
    id: 6,
    name: '角色分配',
    code: 'role:assign',
    type: 'button',
    status: 'enabled'
  },
  {
    id: 7,
    name: '日志查看',
    code: 'log:view',
    type: 'menu',
    status: 'enabled'
  },
  {
    id: 8,
    name: '系统配置',
    code: 'system:config',
    type: 'api',
    status: 'disabled'
  }
])

// 状态管理
const searchQuery = ref('')
const currentType = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)

// 类型样式映射
const getTypeStyle = (type) => {
  const styles = {
    menu: 'bg-blue-50 text-blue-700 border border-blue-200',
    button: 'bg-purple-50 text-purple-700 border border-purple-200',
    api: 'bg-amber-50 text-amber-700 border border-amber-200',
    data: 'bg-emerald-50 text-emerald-700 border border-emerald-200'
  }
  return styles[type] || 'bg-slate-100 text-slate-700'
}

const getTypeLabel = (type) => {
  return permissionTypes.find(t => t.value === type)?.label || type
}

// 筛选逻辑
const filteredPermissions = computed(() => {
  let result = permissions.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(p =>
        p.name.toLowerCase().includes(query) ||
        p.code.toLowerCase().includes(query)
    )
  }

  if (currentType.value !== 'all') {
    result = result.filter(p => p.type === currentType.value)
  }

  return result
})

// 查询方法
const handleSearch = () => {
  currentPage.value = 1
}
</script>

<style scoped>
/* 分页器样式覆盖 */
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #0f172a;
  border-radius: 6px;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
  color: #0f172a;
}

/* 下拉框样式调整 */
:deep(.el-select) {
  width: 7rem !important;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 0.5rem;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
}

:deep(.el-select .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #0f172a inset;
}

:deep(.el-select .el-input__inner) {
  font-size: 0.875rem;
}
</style>