<template>
  <div class="p-6 max-w-[1600px] mx-auto space-y-6">
    <!-- 查询区域 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-4 space-y-4">
      <!-- 第一行：用户名、操作类型、操作状态 -->
      <div class="flex flex-col lg:flex-row lg:items-end gap-4">
        <!-- 用户名搜索 -->
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-slate-700 mb-1.5">用户名</label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <el-icon class="text-slate-400" :size="16">
                <User />
              </el-icon>
            </div>
            <input
              v-model="queryForm.username"
              type="text"
              placeholder="请输入用户名"
              class="w-full h-9 pl-9 pr-4 rounded-md border border-slate-200 bg-white text-sm placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
            />
          </div>
        </div>

        <!-- 操作类型筛选 -->
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-slate-700 mb-1.5">操作类型</label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <el-icon class="text-slate-400" :size="16">
                <Operation />
              </el-icon>
            </div>
            <select
              v-model="queryForm.operationType"
              class="w-full h-9 pl-9 pr-8 rounded-md border border-slate-200 bg-white text-sm text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent cursor-pointer appearance-none"
            >
              <option value="">全部类型</option>
              <option value="LOGIN">登录</option>
              <option value="LOGOUT">登出</option>
              <option value="CREATE">创建</option>
              <option value="UPDATE">更新</option>
              <option value="DELETE">删除</option>
              <option value="UPLOAD">上传</option>
              <option value="DOWNLOAD">下载</option>
              <option value="SHARE">分享</option>
            </select>
            <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
              <el-icon class="text-slate-400" :size="14"><ArrowDown /></el-icon>
            </div>
          </div>
        </div>

        <!-- 操作状态筛选 -->
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-slate-700 mb-1.5">操作状态</label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <el-icon class="text-slate-400" :size="16">
                <CircleCheck />
              </el-icon>
            </div>
            <select
              v-model="queryForm.success"
              class="w-full h-9 pl-9 pr-8 rounded-md border border-slate-200 bg-white text-sm text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent cursor-pointer appearance-none"
            >
              <option value="">全部状态</option>
              <option :value="true">成功</option>
              <option :value="false">失败</option>
            </select>
            <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
              <el-icon class="text-slate-400" :size="14"><ArrowDown /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- 第二行：操作时间、按钮 -->
      <div class="flex flex-col lg:flex-row lg:items-end gap-4">
        <!-- 时间范围筛选 -->
        <div class="flex-1 min-w-[280px] lg:max-w-[calc(33.333%-11px)]">
          <label class="block text-sm font-medium text-slate-700 mb-1.5">操作时间</label>
          <el-date-picker
            v-model="queryForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            class="w-full"
          />
        </div>

        <!-- 操作按钮 -->
        <div class="flex gap-2 flex-shrink-0 lg:ml-auto">
          <button
            @click="handleSearch"
            class="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-all duration-200 shadow-sm hover:shadow-md active:scale-95"
          >
            <el-icon :size="16"><Search /></el-icon>
            查询
          </button>
          <button
            @click="handleReset"
            class="h-9 px-5 border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 rounded-md text-sm font-medium transition-colors"
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
              <th class="px-6 py-3.5 font-semibold text-slate-700">用户名</th>
              <th class="px-6 py-3.5 font-semibold text-slate-700">操作类型</th>
              <th class="px-6 py-3.5 font-semibold text-slate-700">操作详情</th>
              <th class="px-6 py-3.5 font-semibold text-slate-700">操作时间</th>
              <th class="px-6 py-3.5 font-semibold text-slate-700">状态</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr
              v-for="log in tableData"
              :key="log.id"
              class="hover:bg-slate-50/50 transition-colors"
            >
              <td class="px-6 py-4">
                <span class="font-medium text-slate-900">{{ log.username }}</span>
              </td>
              <td class="px-6 py-4">
                <span
                  :class="[
                    'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border',
                    getOperationTypeClass(log.operationType)
                  ]"
                >
                  {{ getOperationTypeLabel(log.operationType) }}
                </span>
              </td>
              <td class="px-6 py-4">
                <div class="max-w-md">
                  <p class="text-slate-700 truncate" :title="log.detail">{{ log.detail }}</p>
                </div>
              </td>
              <td class="px-6 py-4">
                <div class="flex flex-col">
                  <span class="text-slate-900">{{ log.operationTime }}</span>
                </div>
              </td>
              <td class="px-6 py-4">
                <span
                  :class="[
                    'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border',
                    log.success
                      ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                      : 'bg-red-50 text-red-700 border-red-200'
                  ]"
                >
                  <span
                    :class="[
                      'w-1.5 h-1.5 rounded-full mr-1.5',
                      log.success ? 'bg-emerald-500' : 'bg-red-500'
                    ]"
                  ></span>
                  {{ log.success ? '成功' : '失败' }}
                </span>
              </td>
            </tr>

            <!-- 空状态 -->
            <tr v-if="tableData.length === 0">
              <td colspan="5" class="px-6 py-12 text-center">
                <div class="flex flex-col items-center justify-center text-slate-400">
                  <el-icon :size="48" class="mb-2 opacity-50"><Document /></el-icon>
                  <p class="text-sm">暂无日志数据</p>
                  <button
                    @click="handleReset"
                    class="mt-3 text-blue-600 hover:text-blue-700 text-sm font-medium"
                  >
                    重置筛选条件
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import {
  Search,
  User,
  Operation,
  CircleCheck,
  ArrowDown,
  Document
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

// 查询表单
const queryForm = reactive({
  username: '',
  operationType: '',
  success: '',
  timeRange: null
})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 操作类型映射
const operationTypeMap = {
  'LOGIN': { label: '登录', class: 'bg-blue-50 text-blue-700 border-blue-200' },
  'LOGOUT': { label: '登出', class: 'bg-slate-50 text-slate-700 border-slate-200' },
  'CREATE': { label: '创建', class: 'bg-emerald-50 text-emerald-700 border-emerald-200' },
  'UPDATE': { label: '更新', class: 'bg-amber-50 text-amber-700 border-amber-200' },
  'DELETE': { label: '删除', class: 'bg-red-50 text-red-700 border-red-200' },
  'UPLOAD': { label: '上传', class: 'bg-purple-50 text-purple-700 border-purple-200' },
  'DOWNLOAD': { label: '下载', class: 'bg-cyan-50 text-cyan-700 border-cyan-200' },
  'SHARE': { label: '分享', class: 'bg-pink-50 text-pink-700 border-pink-200' }
}

// 获取操作类型标签
const getOperationTypeLabel = (type) => {
  return operationTypeMap[type]?.label || type
}

// 获取操作类型样式类
const getOperationTypeClass = (type) => {
  return operationTypeMap[type]?.class || 'bg-slate-50 text-slate-700 border-slate-200'
}

// 表格数据
const tableData = ref([])

// 加载数据
const loadData = async () => {
  try {
    // 构建查询参数
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    if (queryForm.username) {
      params.username = queryForm.username
    }
    
    if (queryForm.operationType) {
      params.operationType = queryForm.operationType
    }
    
    if (queryForm.success !== '') {
      params.success = queryForm.success
    }
    
    if (queryForm.timeRange && queryForm.timeRange.length === 2) {
      params.startTime = queryForm.timeRange[0]
      params.endTime = queryForm.timeRange[1]
    }
    
    const res = await axios.get(`${API_BASE_URL}/api/log/all`, {
      ...getAuthConfig(),
      params
    })
    
    if (res.data.code === 200) {
      tableData.value = res.data.data || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.data.msg || '获取日志列表失败')
    }
  } catch (error) {
    console.error('获取日志列表失败:', error)
    ElMessage.error(error.response?.data?.msg || '获取日志列表失败')
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadData()
})

// 搜索
const handleSearch = async () => {
  currentPage.value = 1
  await loadData()
}

// 重置
const handleReset = async () => {
  queryForm.username = ''
  queryForm.operationType = ''
  queryForm.success = ''
  queryForm.timeRange = null
  currentPage.value = 1
  await loadData()
}

// 分页大小变化
const handleSizeChange = async (val) => {
  pageSize.value = val
  await loadData()
}

// 页码变化
const handleCurrentChange = async (val) => {
  currentPage.value = val
  await loadData()
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

/* 日期选择器样式覆盖 */
:deep(.el-date-editor.el-input__wrapper) {
  box-shadow: 0 0 0 1px rgb(226 232 240) inset;
  border-radius: 6px;
  height: 36px;
}

:deep(.el-date-editor.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgb(148 163 184) inset;
}

:deep(.el-date-editor.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgb(59 130 246) inset;
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

/* select 箭头样式 */
select {
  background-image: none;
}
</style>
