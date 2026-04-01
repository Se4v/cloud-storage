<template>
  <div class="p-6 max-w-[1400px] mx-auto">
    <!-- 顶部区域：存储概览 + 环形图 - 扁平化设计 -->
    <div class="flex flex-col lg:flex-row border-b border-slate-200">
      <!-- 左侧：存储空间概览 -->
      <div class="flex-1 p-6 lg:border-r border-slate-200">
        <h2 class="text-base font-semibold text-slate-900 mb-4">存储空间</h2>

        <!-- 剩余容量 -->
        <div class="mb-4">
          <p class="text-sm text-slate-500 mb-1">剩余容量</p>
          <div class="flex items-baseline gap-1">
            <span class="text-4xl font-bold text-slate-900">{{ formattedRemainingQuota }}</span>
          </div>
        </div>

        <!-- 进度条 -->
        <div class="mb-4">
          <div class="h-2 bg-slate-100 rounded-full overflow-hidden">
            <div 
              class="h-full bg-blue-500 rounded-full transition-all duration-500"
              :style="{ width: usagePercent + '%' }"
            ></div>
          </div>
          <div class="flex justify-end mt-1">
            <span class="text-sm text-slate-500">{{ usagePercent }}%</span>
          </div>
        </div>

        <!-- 统计信息 -->
        <div class="flex flex-wrap items-center gap-4 text-sm">
          <span class="text-slate-600">
            总容量：<span class="font-medium text-slate-900">{{ formattedTotalQuota }}</span>
          </span>
          <span class="flex items-center gap-1.5 text-slate-600">
            <span class="w-2 h-2 rounded-full bg-blue-500"></span>
            已使用：<span class="font-medium text-slate-900">{{ formattedUsedQuota }}</span>
          </span>
          <span class="flex items-center gap-1.5 text-slate-600">
            <span class="w-2 h-2 rounded-full bg-emerald-500"></span>
            剩余已分配容量：<span class="font-medium text-slate-900">{{ formattedAllocatedQuota }}</span>
          </span>
        </div>
      </div>

      <!-- 右侧：容量使用占比环形图 -->
      <div class="flex-1 p-6 border-t lg:border-t-0 border-slate-200">
        <h2 class="text-base font-semibold text-slate-900 mb-4">容量使用占比</h2>
        <div class="flex items-center">
          <!-- 环形图 -->
          <div class="w-48 h-48 flex-shrink-0">
            <v-chart :option="pieOption" autoresize class="w-full h-full" />
          </div>
          <!-- 图例 -->
          <div class="flex-1 ml-8 space-y-3">
            <div 
              v-for="item in storageLegend" 
              :key="item.name"
              class="flex items-center justify-between"
            >
              <div class="flex items-center gap-2">
                <span 
                  class="w-3 h-3 rounded-full"
                  :style="{ backgroundColor: item.color }"
                ></span>
                <span class="text-sm text-slate-600">{{ item.name }}</span>
              </div>
              <span class="text-sm font-medium text-slate-900">{{ item.value }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部：容量使用详情 -->
    <div>
      <!-- 标题和工具栏 -->
      <div class="py-4 border-b border-slate-200">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div class="flex items-center gap-6">
            <h2 class="text-base font-semibold text-slate-900">容量使用详情</h2>
            <!-- Tab 切换 - 扁平化 -->
            <div class="flex items-center gap-1">
              <button
                v-for="tab in tabs"
                :key="tab.key"
                @click="currentTab = tab.key"
                :class="[
                  'px-3 py-1.5 text-sm font-medium transition-colors border-b-2',
                  currentTab === tab.key
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-slate-600 hover:text-slate-900'
                ]"
              >
                {{ tab.label }}
              </button>
            </div>
          </div>
          
          <div class="flex items-center gap-3">
            <!-- 搜索框 - 扁平化 -->
            <div class="relative">
              <input
                v-model="searchQuery"
                type="text"
                placeholder="请输入团队名称搜索团队"
                class="w-64 h-9 pl-3 pr-10 rounded border border-slate-200 text-sm placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
              />
              <button 
                @click="handleSearch"
                class="absolute right-0 top-0 h-full px-3 text-slate-400 hover:text-slate-600"
              >
                <el-icon :size="16"><Search /></el-icon>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 数据表格 - element-plus 风格 -->
      <div class="bg-white border border-slate-200">
        <el-table
          :data="filteredTableData"
          style="width: 100%"
          :header-cell-style="{
            background: '#f8fafc',
            color: '#475569',
            fontWeight: 500,
            fontSize: '12px',
            padding: '12px 16px',
            borderBottom: '1px solid #e2e8f0'
          }"
          :cell-style="{
            padding: '12px 16px',
            color: '#334155',
            fontSize: '13px',
            borderBottom: '1px solid #f1f5f9'
          }"
          stripe
          highlight-current-row
        >
          <el-table-column prop="name" label="空间名称" min-width="180" sortable>
            <template #default="{ row }">
              <span class="font-medium text-slate-900">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="allocatedQuota" label="分配容量" min-width="150" sortable>
            <template #default="{ row }">
              {{ formatBytes(row.allocatedQuota) }}
            </template>
          </el-table-column>
          <el-table-column prop="usedQuota" label="已使用容量" min-width="150" sortable>
            <template #default="{ row }">
              {{ formatBytes(row.usedQuota) }}
            </template>
          </el-table-column>
          <el-table-column prop="remainingQuota" label="剩余容量" min-width="150" sortable>
            <template #default="{ row }">
              {{ formatBytes(row.remainingQuota) }}
            </template>
          </el-table-column>
          
          <!-- 空状态 -->
          <template #empty>
            <div class="flex flex-col items-center justify-center py-12 text-slate-400">
              <el-icon :size="48" class="mb-2 opacity-50"><Box /></el-icon>
              <p class="text-sm">暂无数据</p>
            </div>
          </template>
        </el-table>
      </div>

      <!-- 分页 - 扁平化 -->
      <div class="py-4 border-t border-slate-200 bg-white flex items-center justify-between">
        <span class="text-sm text-slate-500">
          共 <span class="font-medium text-slate-900">{{ total }}</span> 条记录
        </span>
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          class="!gap-2"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart } from 'echarts/charts'
import {
  TooltipComponent,
  LegendComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import {
  Search,
  Box
} from '@element-plus/icons-vue'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  PieChart,
  TooltipComponent,
  LegendComponent
])

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

// 存储概览数据
const totalQuota = ref(0n)
const usedQuota = ref(0n)
const allocatedQuota = ref(0n)
const remainingQuota = ref(0n)

// 容量占比数据
const enterpriseQuota = ref(0n)
const personalQuota = ref(0n)

// 辅助函数：将后端返回的字符串转换为BigInt
const toBigInt = (value) => {
  if (value === null || value === undefined) return 0n
  if (typeof value === 'bigint') return value
  if (typeof value === 'number') return BigInt(Math.floor(value))
  if (typeof value === 'string') {
    // 去除可能的引号和空白字符
    const cleanValue = value.replace(/["']/g, '').trim()
    if (cleanValue === '' || cleanValue === 'null') return 0n
    try {
      return BigInt(cleanValue)
    } catch (e) {
      console.error('无法转换为BigInt:', value, e)
      return 0n
    }
  }
  return 0n
}

// 格式化存储大小
const formatBytes = (bytes) => {
  if (bytes === 0n || bytes === 0) return '0 B'
  const k = 1024n
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  let size = BigInt(bytes)
  let i = 0
  while (size >= k && i < sizes.length - 1) {
    size = size / k
    i++
  }
  return `${Number(size).toFixed(2)} ${sizes[i]}`
}

// 计算百分比
const calculatePercent = (used, total) => {
  if (total === 0n) return 0
  return Number((used * 100n) / total)
}

// 格式化配额显示
const formattedTotalQuota = computed(() => formatBytes(totalQuota.value))
const formattedUsedQuota = computed(() => formatBytes(usedQuota.value))
const formattedAllocatedQuota = computed(() => formatBytes(allocatedQuota.value))
const formattedRemainingQuota = computed(() => formatBytes(remainingQuota.value))

const usagePercent = computed(() => {
  return calculatePercent(usedQuota.value, totalQuota.value).toFixed(2)
})

// 环形图数据
const storageData = computed(() => [
  { 
    value: Number(enterpriseQuota.value) / (1024 * 1024), // 转换为MB显示
    name: '企业空间', 
    itemStyle: { color: '#f97316' } 
  },
  { 
    value: Number(personalQuota.value) / (1024 * 1024),
    name: '个人空间', 
    itemStyle: { color: '#3b82f6' } 
  }
])

const storageLegend = computed(() => [
  { name: '企业空间', value: formatBytes(enterpriseQuota.value), color: '#f97316' },
  { name: '个人空间', value: formatBytes(personalQuota.value), color: '#3b82f6' }
])

// 环形图配置
const pieOption = computed(() => {
  const total = storageData.value[0].value + storageData.value[1].value
  return {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e2e8f0',
      borderWidth: 1,
      textStyle: {
        color: '#1e293b',
        fontSize: 13
      },
      formatter: (params) => {
        return `${params.name}: ${formatBytes(BigInt(Math.round(params.value * 1024 * 1024)))} (${params.percent}%)`
      }
    },
    series: [
      {
        name: '容量使用',
        type: 'pie',
        radius: ['60%', '80%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        label: {
          show: true,
          position: 'center',
          formatter: () => {
            return `{title|使用总量}\n{value|${formatBytes(enterpriseQuota.value + personalQuota.value)}}`
          },
          rich: {
            title: {
              fontSize: 12,
              color: '#64748b',
              lineHeight: 20
            },
            value: {
              fontSize: 14,
              fontWeight: 'bold',
              color: '#1e293b'
            }
          }
        },
        emphasis: {
          label: {
            show: true
          }
        },
        labelLine: {
          show: false
        },
        data: storageData.value
      }
    ]
  }
})

// Tab 切换
const tabs = [
  { key: 'enterprise', label: '企业空间' },
  { key: 'personal', label: '个人空间' }
]
const currentTab = ref('enterprise')

// 表格数据
const enterpriseTableData = ref([])
const personalTableData = ref([])

// 计算属性：根据当前Tab获取数据
const filteredTableData = computed(() => {
  const data = currentTab.value === 'enterprise' ? enterpriseTableData.value : personalTableData.value
  if (!searchQuery.value) return data
  return data.filter(item => 
    item.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = computed(() => filteredTableData.value.length)
const searchQuery = ref('')

// 加载数据
const loadOverview = async () => {
  try {
    const res = await axios.get(`${API_BASE_URL}/api/drive-stat/overview`, getAuthConfig())
    if (res.data.code === 200) {
      const data = res.data.data
      totalQuota.value = toBigInt(data.totalQuota)
      allocatedQuota.value = toBigInt(data.allocatedQuota)
      usedQuota.value = toBigInt(data.usedQuota)
      remainingQuota.value = toBigInt(data.remainingQuota)
    } else {
      ElMessage.error(res.data.msg || '加载存储概览失败')
    }
  } catch (error) {
    console.error('加载存储概览失败:', error)
    ElMessage.error(error.response?.data?.msg || '加载存储概览失败')
  }
}

const loadBreakdown = async () => {
  try {
    const res = await axios.get(`${API_BASE_URL}/api/drive-stat/breakdown`, getAuthConfig())
    if (res.data.code === 200) {
      const data = res.data.data
      enterpriseQuota.value = toBigInt(data.enterpriseQuota)
      personalQuota.value = toBigInt(data.personalQuota)
    } else {
      ElMessage.error(res.data.msg || '加载容量占比失败')
    }
  } catch (error) {
    console.error('加载容量占比失败:', error)
    ElMessage.error(error.response?.data?.msg || '加载容量占比失败')
  }
}

// 辅助函数：转换表格数据中的BigInt字段
const convertTableData = (data) => {
  if (!data || !Array.isArray(data)) return []
  return data.map(item => ({
    ...item,
    usedQuota: toBigInt(item.usedQuota),
    totalQuota: toBigInt(item.totalQuota),
    allocatedQuota: toBigInt(item.allocatedQuota),
    remainingQuota: toBigInt(item.remainingQuota)
  }))
}

const loadEnterpriseData = async () => {
  try {
    const res = await axios.get(`${API_BASE_URL}/api/drive-stat/enterprise`, getAuthConfig())
    if (res.data.code === 200) {
      enterpriseTableData.value = convertTableData(res.data.data)
    } else {
      ElMessage.error(res.data.msg || '加载企业空间数据失败')
    }
  } catch (error) {
    console.error('加载企业空间数据失败:', error)
    ElMessage.error(error.response?.data?.msg || '加载企业空间数据失败')
  }
}

const loadPersonalData = async () => {
  try {
    const res = await axios.get(`${API_BASE_URL}/api/drive-stat/personal`, getAuthConfig())
    if (res.data.code === 200) {
      personalTableData.value = convertTableData(res.data.data)
    } else {
      ElMessage.error(res.data.msg || '加载个人空间数据失败')
    }
  } catch (error) {
    console.error('加载个人空间数据失败:', error)
    ElMessage.error(error.response?.data?.msg || '加载个人空间数据失败')
  }
}

// 初始化加载
onMounted(() => {
  loadOverview()
  loadBreakdown()
  loadEnterpriseData()
  loadPersonalData()
})

// 方法
const handleSearch = () => {
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
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
</style>
