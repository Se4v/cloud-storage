<template>
  <div class="p-6 max-w-[1200px] mx-auto">
    <!-- 统计卡片 - 扁平化 -->
    <div class="flex border-b border-slate-200">
      <!-- 用户总数 -->
      <div class="flex-1 p-6 border-r border-slate-200">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-sm font-medium text-slate-500">用户总数</p>
            <p class="text-3xl font-bold text-slate-900 mt-2">{{ formatNumber(stats.totalUsers) }}</p>
          </div>
          <div class="w-12 h-12 rounded-xl bg-blue-50 flex items-center justify-center">
            <el-icon class="text-blue-600" :size="24"><User /></el-icon>
          </div>
        </div>
        <div class="mt-4 flex items-center gap-2">
          <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 text-xs font-medium">
            <el-icon :size="12"><ArrowUp /></el-icon>
            12.5%
          </span>
          <span class="text-xs text-slate-500">较上月</span>
        </div>
      </div>

      <!-- 今日新增 -->
      <div class="flex-1 p-6 border-r border-slate-200">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-sm font-medium text-slate-500">今日新增</p>
            <p class="text-3xl font-bold text-slate-900 mt-2">{{ formatNumber(stats.todayNew) }}</p>
          </div>
          <div class="w-12 h-12 rounded-xl bg-emerald-50 flex items-center justify-center">
            <el-icon class="text-emerald-600" :size="24"><User /></el-icon>
          </div>
        </div>
        <div class="mt-4 flex items-center gap-2">
          <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 text-xs font-medium">
            <el-icon :size="12"><ArrowUp /></el-icon>
            8.2%
          </span>
          <span class="text-xs text-slate-500">较昨日</span>
        </div>
      </div>

      <!-- 活跃用户数 -->
      <div class="flex-1 p-6">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-sm font-medium text-slate-500">活跃用户数</p>
            <p class="text-3xl font-bold text-slate-900 mt-2">{{ formatNumber(stats.activeUsers) }}</p>
          </div>
          <div class="w-12 h-12 rounded-xl bg-amber-50 flex items-center justify-center">
            <el-icon class="text-amber-600" :size="24"><UserFilled /></el-icon>
          </div>
        </div>
        <div class="mt-4 flex items-center gap-2">
          <div class="flex-1 h-1.5 bg-slate-100 rounded-full overflow-hidden">
            <div 
              class="h-full bg-amber-500 rounded-full"
              :style="{ width: activeRate + '%' }"
            ></div>
          </div>
          <span class="text-xs text-slate-500 whitespace-nowrap">{{ activeRate }}% 活跃度</span>
        </div>
      </div>
    </div>

    <!-- 下方两列布局 - 扁平化 -->
    <div class="flex flex-col lg:flex-row border-b border-slate-200">
      <!-- 存储空间排行榜 -->
      <div class="flex-1 p-6 lg:border-r border-slate-200">
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-purple-50 flex items-center justify-center">
              <el-icon class="text-purple-600" :size="20"><Trophy /></el-icon>
            </div>
            <div>
              <h2 class="text-base font-semibold text-slate-900">存储空间 TOP5</h2>
              <p class="text-xs text-slate-500">占用存储空间最多的用户</p>
            </div>
          </div>
        </div>

        <!-- 横向条形图 -->
        <div class="space-y-3">
          <div 
            v-for="(user, index) in topStorageUsers" 
            :key="user.id"
            class="group"
          >
            <div class="flex items-center gap-3">
              <!-- 排名 -->
              <div class="w-6 flex-shrink-0">
                <div 
                  class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold"
                  :class="getRankClass(index)"
                >
                  {{ index + 1 }}
                </div>
              </div>

              <!-- 用户信息 -->
              <div class="w-24 flex-shrink-0">
                <p class="text-sm font-medium text-slate-900 truncate">{{ user.username }}</p>
              </div>

              <!-- 进度条 -->
              <div class="flex-1 min-w-0">
                <div class="h-5 bg-slate-100 rounded-md overflow-hidden relative">
                  <div 
                    class="h-full rounded-md transition-all duration-500 ease-out flex items-center justify-end pr-2"
                    :class="getBarColor(index)"
                    :style="{ width: getPercentage(user.storageUsed) + '%' }"
                  >
                    <span class="text-xs font-medium text-white drop-shadow-sm">
                      {{ formatStorage(user.storageUsed) }}
                    </span>
                  </div>
                  <span 
                    v-if="getPercentage(user.storageUsed) < 25"
                    class="absolute right-2 top-1/2 -translate-y-1/2 text-xs text-slate-500"
                  >
                    {{ formatStorage(user.storageUsed) }}
                  </span>
                </div>
              </div>

              <!-- 使用率 -->
              <div class="w-14 flex-shrink-0 text-right">
                <span class="text-xs font-medium" :class="getUsageRateColor(getUsageRate(user.storageUsed, user.storageQuota))">
                  {{ getUsageRate(user.storageUsed, user.storageQuota) }}%
                </span>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="topStorageUsers.length === 0" class="py-12 text-center">
            <div class="flex flex-col items-center justify-center text-slate-400">
              <el-icon :size="48" class="mb-2 opacity-50"><Box /></el-icon>
              <p class="text-sm">暂无数据</p>
            </div>
          </div>
        </div>

        <!-- 底部统计 -->
        <div class="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-sm">
          <span class="text-slate-500">
            共占用 <span class="font-medium text-slate-900">{{ formatStorage(totalTop5Storage) }}</span>
          </span>
          <span class="text-slate-500">
            占系统存储 <span class="font-medium text-slate-900">{{ totalStorageRate }}%</span>
          </span>
        </div>
      </div>

      <!-- 用户增长趋势折线图 -->
      <div class="flex-1 p-6 border-t lg:border-t-0 border-slate-200">
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-cyan-50 flex items-center justify-center">
              <el-icon class="text-cyan-600" :size="20"><TrendCharts /></el-icon>
            </div>
            <div>
              <h2 class="text-base font-semibold text-slate-900">用户增长趋势</h2>
              <p class="text-xs text-slate-500">近7天新增用户变化</p>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-2xl font-bold text-slate-900">{{ weeklyTotal }}</span>
            <span class="text-xs text-slate-500">新增</span>
          </div>
        </div>

        <!-- ECharts 折线图 -->
        <div class="h-[280px]">
          <v-chart 
            :option="chartOption" 
            autoresize 
            class="w-full h-full"
          />
        </div>

        <!-- 统计摘要 -->
        <div class="mt-4 pt-4 border-t border-slate-100 grid grid-cols-3 gap-4">
          <div class="text-center">
            <p class="text-xs text-slate-500">平均日增</p>
            <p class="text-lg font-semibold text-slate-900">{{ avgDaily }}</p>
          </div>
          <div class="text-center border-x border-slate-100">
            <p class="text-xs text-slate-500">最高日增</p>
            <p class="text-lg font-semibold text-emerald-600">{{ maxDaily }}</p>
          </div>
          <div class="text-center">
            <p class="text-xs text-slate-500">最低日增</p>
            <p class="text-lg font-semibold text-blue-600">{{ minDaily }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  DataZoomComponent
} from 'echarts/components'
import VChart from 'vue-echarts'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  LineChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  DataZoomComponent
])

import {
  User,
  UserFilled,
  Trophy,
  ArrowUp,
  Box,
  TrendCharts
} from '@element-plus/icons-vue'

// 统计数据
const stats = ref({
  totalUsers: 1258,
  todayNew: 23,
  activeUsers: 892
})

// 计算活跃率
const activeRate = computed(() => {
  return Math.round((stats.value.activeUsers / stats.value.totalUsers) * 100)
})

// TOP5存储用户
const topStorageUsers = ref([
  {
    id: 1,
    username: 'zhangsan',
    realName: '张三',
    storageUsed: 16106127360,  // 15 GB
    storageQuota: 21474836480  // 20 GB
  },
  {
    id: 2,
    username: 'lisi',
    realName: '李四',
    storageUsed: 12884901888,  // 12 GB
    storageQuota: 21474836480
  },
  {
    id: 3,
    username: 'wangwu',
    realName: '王五',
    storageUsed: 9663676416,   // 9 GB
    storageQuota: 10737418240  // 10 GB
  },
  {
    id: 4,
    username: 'zhaoliu',
    realName: '赵六',
    storageUsed: 6442450944,   // 6 GB
    storageQuota: 10737418240
  },
  {
    id: 5,
    username: 'qianqi',
    realName: '钱七',
    storageUsed: 4294967296,   // 4 GB
    storageQuota: 5368709120   // 5 GB
  }
])

// 最大存储值（用于计算百分比）
const maxStorage = computed(() => {
  if (topStorageUsers.value.length === 0) return 0
  return Math.max(...topStorageUsers.value.map(u => u.storageUsed))
})

// TOP5总存储
const totalTop5Storage = computed(() => {
  return topStorageUsers.value.reduce((sum, u) => sum + u.storageUsed, 0)
})

// 假设总存储为50GB
const totalSystemStorage = 50 * 1024 * 1024 * 1024
const totalStorageRate = computed(() => {
  return ((totalTop5Storage.value / totalSystemStorage) * 100).toFixed(1)
})

// 近7天趋势数据
const weeklyTrend = ref([
  { date: '03/05', count: 12, fullDate: '03月05日' },
  { date: '03/06', count: 18, fullDate: '03月06日' },
  { date: '03/07', count: 15, fullDate: '03月07日' },
  { date: '03/08', count: 25, fullDate: '03月08日' },
  { date: '03/09', count: 22, fullDate: '03月09日' },
  { date: '03/10', count: 30, fullDate: '03月10日' },
  { date: '03/11', count: 23, fullDate: '03月11日' }
])

// 趋势统计
const weeklyTotal = computed(() => {
  return weeklyTrend.value.reduce((sum, d) => sum + d.count, 0)
})

const avgDaily = computed(() => {
  return Math.round(weeklyTotal.value / weeklyTrend.value.length)
})

const maxDaily = computed(() => {
  return Math.max(...weeklyTrend.value.map(d => d.count))
})

const minDaily = computed(() => {
  return Math.min(...weeklyTrend.value.map(d => d.count))
})

// ECharts 配置
const chartOption = computed(() => ({
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
      const data = params[0]
      const trendData = weeklyTrend.value[data.dataIndex]
      return `
        <div class="font-medium text-slate-700 mb-1">${trendData.fullDate}</div>
        <div class="flex items-center gap-2">
          <span class="w-2 h-2 rounded-full bg-cyan-500"></span>
          <span class="text-slate-600">新增用户：</span>
          <span class="font-semibold text-slate-900">${data.value} 人</span>
        </div>
      `
    }
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: weeklyTrend.value.map(d => d.date),
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
    minInterval: 1,
    splitLine: {
      lineStyle: {
        color: '#f1f5f9',
        type: 'dashed'
      }
    },
    axisLabel: {
      color: '#64748b',
      fontSize: 12
    }
  },
  series: [
    {
      name: '新增用户',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      showSymbol: false,
      data: weeklyTrend.value.map(d => d.count),
      itemStyle: {
        color: '#06b6d4',
        borderWidth: 2,
        borderColor: '#fff'
      },
      lineStyle: {
        width: 3,
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 1,
          y2: 0,
          colorStops: [
            { offset: 0, color: '#22d3ee' },
            { offset: 1, color: '#06b6d4' }
          ]
        }
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(6, 182, 212, 0.2)' },
            { offset: 1, color: 'rgba(6, 182, 212, 0.01)' }
          ]
        }
      },
      emphasis: {
        showSymbol: true,
        scale: 1.5,
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(6, 182, 212, 0.5)'
        }
      }
    }
  ]
}))

// 格式化数字
const formatNumber = (num) => {
  return num.toLocaleString('zh-CN')
}

// 格式化存储大小
const formatStorage = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

// 计算百分比
const getPercentage = (used) => {
  if (maxStorage.value === 0) return 0
  return Math.round((used / maxStorage.value) * 100)
}

// 计算使用率
const getUsageRate = (used, quota) => {
  return Math.round((used / quota) * 100)
}

// 获取使用率颜色
const getUsageRateColor = (rate) => {
  if (rate >= 90) return 'text-red-600'
  if (rate >= 70) return 'text-amber-600'
  return 'text-emerald-600'
}

// 获取排名样式
const getRankClass = (index) => {
  const classes = [
    'bg-amber-100 text-amber-700 border border-amber-200',  // 1st
    'bg-slate-100 text-slate-700 border border-slate-200',  // 2nd
    'bg-orange-100 text-orange-700 border border-orange-200', // 3rd
    'bg-slate-50 text-slate-500 border border-slate-200',   // 4th
    'bg-slate-50 text-slate-500 border border-slate-200'    // 5th
  ]
  return classes[index] || classes[4]
}

// 获取条形图颜色
const getBarColor = (index) => {
  const colors = [
    'bg-gradient-to-r from-amber-500 to-amber-400',    // 1st
    'bg-gradient-to-r from-slate-500 to-slate-400',    // 2nd
    'bg-gradient-to-r from-orange-500 to-orange-400',  // 3rd
    'bg-gradient-to-r from-blue-500 to-blue-400',      // 4th
    'bg-gradient-to-r from-cyan-500 to-cyan-400'       // 5th
  ]
  return colors[index] || colors[4]
}
</script>

<style scoped>
/* 平滑过渡 */
.transition-all {
  transition-property: all;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 300ms;
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgb(203 213 225);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgb(148 163 184);
}

/* 数字字体 */
.text-3xl {
  font-variant-numeric: tabular-nums;
}
</style>
