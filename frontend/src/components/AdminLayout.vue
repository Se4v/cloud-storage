<template>
  <div class="flex h-screen bg-slate-50 font-sans text-slate-900 antialiased">
    <!-- 左侧边栏 -->
    <aside class="w-64 bg-white border-r border-slate-200 flex flex-col shadow-sm z-10 flex-shrink-0">
      <!-- Logo - 仅保留"管理中心" -->
      <div class="h-16 flex items-center px-6 border-b border-slate-100 flex-shrink-0">
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 bg-gradient-to-br from-blue-600 to-blue-700 rounded-lg flex items-center justify-center text-white shadow-sm flex-shrink-0">
            <el-icon :size="18"><Setting /></el-icon>
          </div>
          <span class="font-bold text-lg tracking-tight text-slate-800">管理中心</span>
        </div>
      </div>

      <!-- 导航菜单 -->
      <nav class="flex-1 py-4 overflow-y-auto overflow-x-hidden custom-scrollbar">
        <!-- 数据报表 -->
        <div class="mb-6">
          <h3 class="px-4 mb-2 text-xs font-semibold text-slate-400 uppercase tracking-wider">
            数据报表
          </h3>
          <div class="space-y-1 px-2">
            <button
                v-for="item in reportMenu"
                :key="item.key"
                @click="handleMenuClick(item)"
                :class="[
                'w-full flex items-center gap-3 px-3 py-2.5 rounded-md text-sm font-medium transition-all duration-200',
                currentMenu === item.key
                  ? 'bg-slate-100 text-slate-900 shadow-sm ring-1 ring-slate-200'
                  : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
              ]"
            >
              <el-icon :size="18" :class="currentMenu === item.key ? 'text-blue-600' : 'text-slate-500'">
                <component :is="item.icon" />
              </el-icon>
              <span class="truncate">{{ item.label }}</span>
            </button>
          </div>
        </div>

        <!-- 用户与团队管理 -->
        <div class="mb-6">
          <h3 class="px-4 mb-2 text-xs font-semibold text-slate-400 uppercase tracking-wider">
            用户与团队管理
          </h3>
          <div class="space-y-1 px-2">
            <button
                v-for="item in managementMenu"
                :key="item.key"
                @click="handleMenuClick(item)"
                :class="[
                'w-full flex items-center gap-3 px-3 py-2.5 rounded-md text-sm font-medium transition-all duration-200',
                currentMenu === item.key
                  ? 'bg-slate-100 text-slate-900 shadow-sm ring-1 ring-slate-200'
                  : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
              ]"
            >
              <el-icon :size="18" :class="currentMenu === item.key ? 'text-blue-600' : 'text-slate-500'">
                <component :is="item.icon" />
              </el-icon>
              <span class="truncate">{{ item.label }}</span>
            </button>
          </div>
        </div>

        <!-- 网盘配置 -->
        <div class="mb-6">
          <h3 class="px-4 mb-2 text-xs font-semibold text-slate-400 uppercase tracking-wider">
            网盘配置
          </h3>
          <div class="space-y-1 px-2">
            <button
                v-for="item in configMenu"
                :key="item.key"
                @click="handleMenuClick(item)"
                :class="[
                'w-full flex items-center gap-3 px-3 py-2.5 rounded-md text-sm font-medium transition-all duration-200',
                currentMenu === item.key
                  ? 'bg-slate-100 text-slate-900 shadow-sm ring-1 ring-slate-200'
                  : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
              ]"
            >
              <el-icon :size="18" :class="currentMenu === item.key ? 'text-blue-600' : 'text-slate-500'">
                <component :is="item.icon" />
              </el-icon>
              <span class="truncate">{{ item.label }}</span>
            </button>
          </div>
        </div>

        <!-- 安全与审计 -->
        <div class="mb-6">
          <h3 class="px-4 mb-2 text-xs font-semibold text-slate-400 uppercase tracking-wider">
            安全与审计
          </h3>
          <div class="space-y-1 px-2">
            <button
                v-for="item in securityMenu"
                :key="item.key"
                @click="handleMenuClick(item)"
                :class="[
                'w-full flex items-center gap-3 px-3 py-2.5 rounded-md text-sm font-medium transition-all duration-200',
                currentMenu === item.key
                  ? 'bg-slate-100 text-slate-900 shadow-sm ring-1 ring-slate-200'
                  : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
              ]"
            >
              <el-icon :size="18" :class="currentMenu === item.key ? 'text-blue-600' : 'text-slate-500'">
                <component :is="item.icon" />
              </el-icon>
              <span class="truncate">{{ item.label }}</span>
            </button>
          </div>
        </div>
      </nav>

      <!-- 底部用户信息区域 - 卡片包裹，退出按钮在上 -->
      <div class="p-4 border-t border-slate-200 bg-slate-50/50 flex-shrink-0">
        <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-4">
          <!-- 退出按钮在上 -->
          <button
              @click="handleLogout"
              class="w-full flex items-center gap-2 text-sm text-slate-600 hover:text-red-600 transition-colors mb-3 pb-3 border-b border-slate-100"
          >
            <el-icon :size="16"><SwitchButton /></el-icon>
            <span class="font-medium">退出管理中心</span>
          </button>

          <!-- 用户信息在下 -->
          <div class="flex items-center gap-3">
            <div class="w-9 h-9 rounded-full bg-emerald-500 flex items-center justify-center text-white text-sm font-semibold shadow-sm flex-shrink-0">
              管
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-semibold text-slate-900 truncate">系统管理员</p>
              <p class="text-xs text-slate-500 truncate">已使用 45.58M</p>
            </div>
          </div>
        </div>
      </div>
    </aside>

    <!-- 主内容区 - 无标题，直接显示内容 -->
    <main class="flex-1 flex flex-col overflow-hidden min-w-0 bg-slate-50/50">
      <div class="flex-1 overflow-auto custom-scrollbar">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  Setting,
  DataLine,
  User,
  OfficeBuilding,
  Link,
  SetUp,
  Histogram,
  List,
  SwitchButton
} from '@element-plus/icons-vue'

const router = useRouter()
const currentMenu = ref('space-stats')

// 数据报表菜单
const reportMenu = [
  { key: 'space-stats', label: '空间统计', icon: DataLine, route: '/admin/space-stats' },
  { key: 'user-stats', label: '用户统计', icon: Histogram, route: '/admin/user-stats' },
  { key: 'traffic-stats', label: '流量统计', icon: DataLine, route: '/admin/traffic-stats' }
]

// 用户与团队管理菜单
const managementMenu = [
  { key: 'user-mgmt', label: '用户管理', icon: User, route: '/admin/users' },
  { key: 'team-mgmt', label: '团队管理', icon: OfficeBuilding, route: '/admin/teams' }
]

// 网盘配置菜单
const configMenu = [
  { key: 'init-config', label: '初始化设置', icon: SetUp, route: '/admin/init' },
  { key: 'link-config', label: '外链设置', icon: Link, route: '/admin/links' }
]

// 安全与审计菜单
const securityMenu = [
  { key: 'logs', label: '日志查询', icon: List, route: '/admin/logs' }
]

// 处理菜单点击
const handleMenuClick = (item) => {
  currentMenu.value = item.key
  if (item.route) {
    router.push(item.route)
  }
}

// 退出登录
const handleLogout = () => {
  console.log('退出登录')
  // router.push('/login')
}
</script>

<style scoped>
/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(4px);
}

/* 自定义滚动条样式 */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>