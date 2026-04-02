<template>
  <div class="p-6 max-w-[1400px] mx-auto">
    <!-- 统计卡片 - 扁平化 -->
    <div class="flex border-b border-slate-200">
      <!-- 总存储 -->
      <div class="flex-1 p-6 border-r border-slate-200">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-sm font-medium text-slate-500">总存储</p>
            <p class="text-3xl font-bold text-slate-900 mt-2">{{ formatStorage(stats.totalQuota) }}</p>
          </div>
          <div class="w-12 h-12 rounded-xl bg-blue-50 flex items-center justify-center">
            <el-icon class="text-blue-600" :size="24"><Box /></el-icon>
          </div>
        </div>
        <div class="mt-4 flex items-center gap-2">
          <div class="flex-1 h-1.5 bg-slate-100 rounded-full overflow-hidden">
            <div class="h-full bg-blue-500 rounded-full" :style="{ width: usagePercent + '%' }"></div>
          </div>
          <span class="text-xs text-slate-500 whitespace-nowrap">{{ usagePercent }}% 已使用</span>
        </div>
      </div>

      <!-- 今日上传量 -->
      <div class="flex-1 p-6 border-r border-slate-200">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-sm font-medium text-slate-500">今日上传量</p>
            <p class="text-3xl font-bold text-slate-900 mt-2">{{ formatStorage(stats.todayUpload) }}</p>
          </div>
          <div class="w-12 h-12 rounded-xl bg-emerald-50 flex items-center justify-center">
            <el-icon class="text-emerald-600" :size="24"><Upload /></el-icon>
          </div>
        </div>
        <div class="mt-4 flex items-center gap-2">
          <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 text-xs font-medium">
            <el-icon :size="12"><ArrowUp /></el-icon>
            23.5%
          </span>
          <span class="text-xs text-slate-500">较昨日</span>
        </div>
      </div>

      <!-- 今日下载量 -->
      <div class="flex-1 p-6">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-sm font-medium text-slate-500">今日下载量</p>
            <p class="text-3xl font-bold text-slate-900 mt-2">{{ formatStorage(stats.todayDownload) }}</p>
          </div>
          <div class="w-12 h-12 rounded-xl bg-purple-50 flex items-center justify-center">
            <el-icon class="text-purple-600" :size="24"><Download /></el-icon>
          </div>
        </div>
        <div class="mt-4 flex items-center gap-2">
          <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-red-50 text-red-700 text-xs font-medium">
            <el-icon :size="12"><ArrowDown /></el-icon>
            5.2%
          </span>
          <span class="text-xs text-slate-500">较昨日</span>
        </div>
      </div>
    </div>

    <!-- 下方两列布局 - 扁平化 -->
    <div class="flex flex-col lg:flex-row border-b border-slate-200">
      <!-- 左侧：近7天流量趋势 -->
      <div class="flex-[2] p-6 lg:border-r border-slate-200">
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-cyan-50 flex items-center justify-center">
              <el-icon class="text-cyan-600" :size="20"><TrendCharts /></el-icon>
            </div>
            <div>
              <h2 class="text-base font-semibold text-slate-900">近7天流量趋势</h2>
              <p class="text-xs text-slate-500">上传与下载流量统计</p>
            </div>
          </div>
          <!-- 图例 -->
          <div class="flex items-center gap-4">
            <div class="flex items-center gap-2">
              <span class="w-3 h-3 rounded-full bg-emerald-500"></span>
              <span class="text-sm text-slate-600">上传</span>
            </div>
            <div class="flex items-center gap-2">
              <span class="w-3 h-3 rounded-full bg-purple-500"></span>
              <span class="text-sm text-slate-600">下载</span>
            </div>
          </div>
        </div>

        <!-- ECharts 折线图 -->
        <div class="h-[320px]">
          <v-chart 
            :option="trendChartOption" 
            autoresize 
            class="w-full h-full"
          />
        </div>

        <!-- 统计摘要 -->
        <div class="mt-4 pt-4 border-t border-slate-100 grid grid-cols-4 gap-4">
          <div class="text-center">
            <p class="text-xs text-slate-500">本周上传总量</p>
            <p class="text-lg font-semibold text-emerald-600">{{ weeklyUploadTotal }}</p>
          </div>
          <div class="text-center border-x border-slate-100">
            <p class="text-xs text-slate-500">本周下载总量</p>
            <p class="text-lg font-semibold text-purple-600">{{ weeklyDownloadTotal }}</p>
          </div>
          <div class="text-center border-r border-slate-100">
            <p class="text-xs text-slate-500">上传峰值</p>
            <p class="text-lg font-semibold text-slate-900">{{ uploadPeak }}</p>
          </div>
          <div class="text-center">
            <p class="text-xs text-slate-500">下载峰值</p>
            <p class="text-lg font-semibold text-slate-900">{{ downloadPeak }}</p>
          </div>
        </div>
      </div>

      <!-- 右侧：文件类型占比 -->
      <div class="flex-1 p-6 border-t lg:border-t-0 border-slate-200">
        <div class="flex items-center gap-3 mb-6">
          <div class="w-10 h-10 rounded-lg bg-amber-50 flex items-center justify-center">
            <el-icon class="text-amber-600" :size="20"><Document /></el-icon>
          </div>
          <div>
            <h2 class="text-base font-semibold text-slate-900">文件类型占比</h2>
            <p class="text-xs text-slate-500">按存储容量分布</p>
          </div>
        </div>

        <!-- ECharts 饼图 -->
        <div class="h-[240px]">
          <v-chart 
            :option="pieChartOption" 
            autoresize 
            class="w-full h-full"
          />
        </div>

        <!-- 图例列表 -->
        <div class="mt-4 space-y-2 max-h-[160px] overflow-y-auto custom-scrollbar">
          <div 
            v-for="item in fileTypeData" 
            :key="item.name"
            class="flex items-center justify-between py-2 px-3 rounded-lg hover:bg-slate-50 transition-colors"
          >
            <div class="flex items-center gap-2">
              <span 
                class="w-3 h-3 rounded-full flex-shrink-0"
                :style="{ backgroundColor: item.color }"
              ></span>
              <span class="text-sm text-slate-700">{{ item.name }}</span>
            </div>
            <div class="text-right">
              <span class="text-sm font-medium text-slate-900">{{ item.size }}</span>
              <span class="text-xs text-slate-500 ml-1">({{ item.percent }}%)</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  DataZoomComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import {
  Box,
  Upload,
  Download,
  ArrowUp,
  ArrowDown,
  TrendCharts,
  Document
} from '@element-plus/icons-vue'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  LineChart,
  PieChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  DataZoomComponent
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

// 辅助函数：将后端返回的字符串转换为BigInt
const toBigInt = (value) => {
  if (value === null || value === undefined) return 0n
  if (typeof value === 'bigint') return value
  if (typeof value === 'number') return BigInt(Math.floor(value))
  if (typeof value === 'string') {
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

// 格式化存储大小（BigInt版本）
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

// 统计数据
const stats = ref({
  totalQuota: 0n,
  usedQuota: 0n,
  todayUpload: 0n,
  todayDownload: 0n
})

// 计算使用百分比
const usagePercent = computed(() => {
  if (stats.value.totalQuota === 0n) return 0
  return Math.round(Number((stats.value.usedQuota * 100n) / stats.value.totalQuota))
})

// 格式化存储
const formatStorage = (bytes) => {
  return formatBytes(bytes)
}

// 近7天流量数据（MB）
const trafficData = ref([])

// 趋势统计
const weeklyUploadTotal = computed(() => {
  if (!trafficData.value || trafficData.value.length === 0) return '0 GB'
  const total = trafficData.value.reduce((sum, d) => sum + (d.upload || 0), 0)
  return (total / 1024).toFixed(2) + ' GB'
})

const weeklyDownloadTotal = computed(() => {
  if (!trafficData.value || trafficData.value.length === 0) return '0 GB'
  const total = trafficData.value.reduce((sum, d) => sum + (d.download || 0), 0)
  return (total / 1024).toFixed(2) + ' GB'
})

const uploadPeak = computed(() => {
  if (!trafficData.value || trafficData.value.length === 0) return '0 MB'
  const max = Math.max(...trafficData.value.map(d => d.upload || 0))
  return max + ' MB'
})

const downloadPeak = computed(() => {
  if (!trafficData.value || trafficData.value.length === 0) return '0 MB'
  const max = Math.max(...trafficData.value.map(d => d.download || 0))
  return max + ' MB'
})

// 折线图配置
const trendChartOption = computed(() => ({
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: '10%',
    containLabel: true
  },
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    borderColor: '#e2e8f0',
    borderWidth: 1,
    textStyle: {
      color: '#1e293b',
      fontSize: 13
    },
    padding: [12, 16],
    formatter: (params) => {
      const data = trafficData.value[params[0].dataIndex]
      let html = `<div class="font-medium text-slate-700 mb-2">${data.fullDate}</div>`
      params.forEach(param => {
        const color = param.seriesName === '上传' ? '#10b981' : '#a855f7'
        html += `
          <div class="flex items-center gap-2 mb-1">
            <span class="w-2 h-2 rounded-full" style="background-color: ${color}"></span>
            <span class="text-slate-600">${param.seriesName}：</span>
            <span class="font-semibold text-slate-900">${param.value} MB</span>
          </div>
        `
      })
      return html
    }
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: trafficData.value.map(d => d.date),
    axisLine: {
      lineStyle: {
        color: '#e2e8f0'
      }
    },
    axisTick: {
      show: false
    },
    axisLabel: {
      color: '#64748b',
      fontSize: 12,
      margin: 12
    }
  },
  yAxis: {
    type: 'value',
    minInterval: 100,
    splitLine: {
      lineStyle: {
        color: '#f1f5f9',
        type: 'dashed'
      }
    },
    axisLabel: {
      color: '#64748b',
      fontSize: 12,
      formatter: '{value} MB'
    }
  },
  series: [
    {
      name: '上传',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      showSymbol: false,
      data: trafficData.value.map(d => d.upload),
      itemStyle: {
        color: '#10b981'
      },
      lineStyle: {
        width: 3,
        color: '#10b981'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(16, 185, 129, 0.2)' },
            { offset: 1, color: 'rgba(16, 185, 129, 0.01)' }
          ]
        }
      },
      emphasis: {
        showSymbol: true,
        scale: 1.5
      }
    },
    {
      name: '下载',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      showSymbol: false,
      data: trafficData.value.map(d => d.download),
      itemStyle: {
        color: '#a855f7'
      },
      lineStyle: {
        width: 3,
        color: '#a855f7'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(168, 85, 247, 0.2)' },
            { offset: 1, color: 'rgba(168, 85, 247, 0.01)' }
          ]
        }
      },
      emphasis: {
        showSymbol: true,
        scale: 1.5
      }
    }
  ]
}))

// 预定义颜色列表
const colorPalette = [
  '#3b82f6', // blue
  '#10b981', // emerald
  '#f59e0b', // amber
  '#8b5cf6', // violet
  '#64748b'  // slate
]

// 文件类型数据
const fileTypeData = ref([])

// 饼图配置
const pieChartOption = computed(() => ({
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
      const item = fileTypeData.value.find(d => d.name === params.name)
      return `
        <div class="font-medium text-slate-700 mb-1">${params.name}</div>
        <div class="text-slate-600">占比：${params.percent}%</div>
        <div class="text-slate-600">大小：${item?.size || ''}</div>
      `
    }
  },
  series: [
    {
      name: '文件类型',
      type: 'pie',
      radius: ['45%', '75%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold',
          color: '#1e293b'
        },
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.1)'
        }
      },
      labelLine: {
        show: false
      },
      data: fileTypeData.value.map(item => ({
        value: item.percent,
        name: item.name,
        itemStyle: { color: item.color }
      }))
    }
  ]
}))
// 加载统计数据
const loadTrafficOverview = async () => {
  try {
    const res = await axios.get(`${API_BASE_URL}/api/traffic/overview`, getAuthConfig())
    if (res.data.code === 200) {
      const data = res.data.data
      stats.value = {
        totalQuota: toBigInt(data.totalQuota),
        usedQuota: toBigInt(data.usedQuota),
        todayUpload: toBigInt(data.todayUpload),
        todayDownload: toBigInt(data.todayDownload)
      }
    } else {
      ElMessage.error(res.data.msg || '加载流量概览失败')
    }
  } catch (error) {
    console.error('加载流量概览失败:', error)
    ElMessage.error(error.response?.data?.msg || '加载流量概览失败')
  }
}

// 加载趋势数据
const loadTrendData = async () => {
  try {
    const res = await axios.get(`${API_BASE_URL}/api/traffic/trend`, getAuthConfig())
    if (res.data.code === 200) {
      trafficData.value = res.data.data || []
    } else {
      ElMessage.error(res.data.msg || '加载趋势数据失败')
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error)
    ElMessage.error(error.response?.data?.msg || '加载趋势数据失败')
  }
}

// 加载文件类型分布数据
const loadFileTypeDistribution = async () => {
  try {
    const res = await axios.get(`${API_BASE_URL}/api/traffic/distribution`, getAuthConfig())
    if (res.data.code === 200) {
      const data = res.data.data || []
      fileTypeData.value = data.map((item, index) => ({
        name: item.name,
        percent: item.percent,
        size: formatBytes(toBigInt(item.size)),
        color: colorPalette[index % colorPalette.length]
      }))
    } else {
      ElMessage.error(res.data.msg || '加载文件类型分布失败')
    }
  } catch (error) {
    console.error('加载文件类型分布失败:', error)
    ElMessage.error(error.response?.data?.msg || '加载文件类型分布失败')
  }
}

// 初始化加载
onMounted(() => {
  loadTrafficOverview()
  loadTrendData()
  loadFileTypeDistribution()
})
</script>

<style scoped>
/* 平滑过渡 */
.transition-all {
  transition-property: all;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 300ms;
}

/* 自定义滚动条 */
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgb(203 213 225);
  border-radius: 2px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgb(148 163 184);
}

/* 数字字体 */
.text-3xl {
  font-variant-numeric: tabular-nums;
}
</style>
