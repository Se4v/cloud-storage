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
            <span class="text-4xl font-bold text-slate-900">{{ remainingStorage }}</span>
            <span class="text-lg text-slate-500">GB</span>
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
            总容量：<span class="font-medium text-slate-900">{{ totalStorage }} GB</span>
          </span>
          <span class="flex items-center gap-1.5 text-slate-600">
            <span class="w-2 h-2 rounded-full bg-blue-500"></span>
            已使用：<span class="font-medium text-slate-900">{{ usedStorage }}</span>
          </span>
          <span class="flex items-center gap-1.5 text-slate-600">
            <span class="w-2 h-2 rounded-full bg-emerald-500"></span>
            剩余已分配容量：<span class="font-medium text-slate-900">{{ allocatedStorage }}</span>
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

      <!-- 数据表格 - 扁平化 -->
      <div class="overflow-x-auto bg-white">
        <table class="w-full text-sm text-left">
          <thead>
            <tr class="border-b border-slate-200">
              <th 
                v-for="col in columns" 
                :key="col.key"
                class="px-6 py-3 text-xs font-medium text-slate-500 cursor-pointer hover:text-slate-700 transition-colors"
                @click="handleSort(col.key)"
              >
                <div class="flex items-center gap-1">
                  {{ col.label }}
                  <el-icon v-if="col.sortable" :size="12" class="text-slate-400">
                    <Sort />
                  </el-icon>
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="row in tableData"
              :key="row.id"
              class="border-b border-slate-100 hover:bg-slate-50/50 transition-colors"
            >
              <td class="px-6 py-4">
                <div class="flex flex-col">
                  <span class="font-medium text-slate-900">{{ row.name }}</span>
                  <span class="text-xs text-slate-500">{{ row.id }}</span>
                </div>
              </td>
              <td class="px-6 py-4 text-slate-700">
                {{ row.allocated }}
              </td>
              <td class="px-6 py-4 text-slate-700">
                {{ row.used }}
              </td>
              <td class="px-6 py-4 text-slate-700">
                {{ row.remaining }}
              </td>
              <td class="px-6 py-4 text-slate-700">
                {{ row.members }}
              </td>
            </tr>

            <!-- 空状态 -->
            <tr v-if="tableData.length === 0">
              <td colspan="5" class="px-6 py-12 text-center">
                <div class="flex flex-col items-center justify-center text-slate-400">
                  <el-icon :size="48" class="mb-2 opacity-50"><Box /></el-icon>
                  <p class="text-sm">暂无数据</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 - 扁平化 -->
      <div class="py-4 border-t border-slate-200 bg-white flex items-center justify-between">
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
import { ref, computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart } from 'echarts/charts'
import {
  TooltipComponent,
  LegendComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { ElMessage } from 'element-plus'
import {
  Search,
  Box,
  Sort
} from '@element-plus/icons-vue'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  PieChart,
  TooltipComponent,
  LegendComponent
])

// 存储概览数据
const totalStorage = ref(50)
const usedStorageBytes = ref(91.18 * 1024 * 1024) // 91.18 MB
const allocatedStorageBytes = ref(0)

// 格式化存储
const formatBytes = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 计算属性
const usedStorage = computed(() => formatBytes(usedStorageBytes.value))
const allocatedStorage = computed(() => formatBytes(allocatedStorageBytes.value))
const remainingStorage = computed(() => {
  const totalBytes = totalStorage.value * 1024 * 1024 * 1024
  const remaining = totalBytes - usedStorageBytes.value
  return (remaining / (1024 * 1024 * 1024)).toFixed(2)
})
const usagePercent = computed(() => {
  return ((usedStorageBytes.value / (totalStorage.value * 1024 * 1024 * 1024)) * 100).toFixed(2)
})

// 环形图数据
const storageData = [
  { value: 45.6, name: '企业空间', itemStyle: { color: '#f97316' } },
  { value: 45.58, name: '个人空间', itemStyle: { color: '#3b82f6' } }
]

const storageLegend = [
  { name: '企业空间', value: '45.6 MB', color: '#f97316' },
  { name: '个人空间', value: '45.58 MB', color: '#3b82f6' }
]

// 环形图配置
const pieOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    borderColor: '#e2e8f0',
    borderWidth: 1,
    textStyle: {
      color: '#1e293b',
      fontSize: 13
    },
    formatter: '{b}: {c} MB ({d}%)'
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
          return `{title|使用总量 (MB)}\n{value|${(storageData[0].value + storageData[1].value).toFixed(2)}}`
        },
        rich: {
          title: {
            fontSize: 12,
            color: '#64748b',
            lineHeight: 20
          },
          value: {
            fontSize: 20,
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
      data: storageData
    }
  ]
}))

// Tab 切换
const tabs = [
  { key: 'enterprise', label: '企业空间' },
  { key: 'personal', label: '个人空间' }
]
const currentTab = ref('enterprise')

// 表格列
const columns = [
  { key: 'name', label: '团队名称', sortable: true },
  { key: 'allocated', label: '分配容量', sortable: true },
  { key: 'used', label: '已使用容量', sortable: true },
  { key: 'remaining', label: '剩余容量', sortable: true },
  { key: 'members', label: '团队成员', sortable: false }
]

// 表格数据
const tableData = ref([
  {
    id: 't2c93e7e',
    name: '我的企业',
    allocated: '49.91 GB',
    used: '45.6 MB',
    remaining: '49.91 GB',
    members: 1,
    isShared: true
  },
  {
    id: '1121',
    name: '1121',
    allocated: '49.91 GB',
    used: '0 B',
    remaining: '49.91 GB',
    members: 0,
    isShared: true
  }
])

// 分页
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(2)
const searchQuery = ref('')

// 方法
const handleExpand = () => {
  ElMessage.info('扩容功能开发中...')
}

const handleSearch = () => {
  ElMessage.success('搜索: ' + searchQuery.value)
}

const handleDownload = () => {
  ElMessage.success('开始下载数据')
}

const handleSort = (key) => {
  ElMessage.info('按 ' + key + ' 排序')
}

const handleSizeChange = (val) => {
  pageSize.value = val
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
