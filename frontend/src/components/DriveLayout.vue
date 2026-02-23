<template>
  <div class="flex h-screen bg-slate-50 font-sans text-slate-900 antialiased">
    <!-- 左侧边栏 -->
    <aside class="w-64 bg-white border-r border-slate-200 flex flex-col shadow-sm z-10">
      <!-- Logo -->
      <div class="h-16 flex items-center px-6 border-b border-slate-100 flex-shrink-0">
        <div class="flex items-center gap-2.5">
          <div class="w-8 h-8 bg-gradient-to-br from-blue-500 to-blue-600 rounded-lg flex items-center justify-center text-white shadow-sm">
            <el-icon :size="20"><FolderOpened /></el-icon>
          </div>
          <span class="font-bold text-lg tracking-tight text-slate-800">腾讯云盘</span>
        </div>
      </div>

      <!-- 导航菜单 - 分组显示 -->
      <nav class="flex-1 p-4 overflow-auto mt-2">
        <!-- 第一组：存储空间 -->
        <div class="mb-6">
          <h3 class="px-3 mb-2 text-xs font-semibold text-slate-400 uppercase tracking-wider">
            存储空间
          </h3>
          <div class="space-y-1">
            <button
                v-for="item in storageMenu"
                :key="item.key"
                @click="handleMenuClick(item)"
                :class="[
                'w-full flex items-center gap-3 px-3 py-2.5 rounded-md text-sm font-medium transition-all duration-200',
                currentMenu === item.key
                  ? 'bg-slate-100 text-slate-900 shadow-sm ring-1 ring-slate-200'
                  : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900 hover:translate-x-0.5'
              ]"
            >
              <el-icon :size="18" :class="currentMenu === item.key ? 'text-blue-600' : 'text-slate-500'">
                <component :is="item.icon" />
              </el-icon>
              {{ item.label }}
            </button>
          </div>
        </div>

        <!-- 第二组：文件管理 -->
        <div>
          <h3 class="px-3 mb-2 text-xs font-semibold text-slate-400 uppercase tracking-wider">
            文件管理
          </h3>
          <div class="space-y-1">
            <button
                v-for="item in fileMenu"
                :key="item.key"
                @click="handleMenuClick(item)"
                :class="[
                'w-full flex items-center gap-3 px-3 py-2.5 rounded-md text-sm font-medium transition-all duration-200',
                currentMenu === item.key
                  ? 'bg-slate-100 text-slate-900 shadow-sm ring-1 ring-slate-200'
                  : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900 hover:translate-x-0.5'
              ]"
            >
              <el-icon :size="18" :class="currentMenu === item.key ? 'text-blue-600' : 'text-slate-500'">
                <component :is="item.icon" />
              </el-icon>
              {{ item.label }}
            </button>
          </div>
        </div>
      </nav>

      <!-- 用户信息 & 存储空间 -->
      <div class="p-4 border-t border-slate-200 bg-slate-50/50 flex-shrink-0">
        <!-- 存储空间 - 优化后 -->
        <div class="mb-4 p-4 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div class="flex justify-between items-center mb-2">
            <span class="text-sm font-semibold text-slate-700">存储空间</span>
            <span class="text-xs font-medium text-slate-500 tabular-nums">40M / 1G</span>
          </div>
          <div class="h-2.5 bg-slate-100 rounded-full overflow-hidden">
            <div
                class="h-full bg-gradient-to-r from-blue-500 to-blue-600 rounded-full transition-all duration-500 shadow-sm"
                style="width: 4%"
            ></div>
          </div>
        </div>

        <!-- 用户头像 -->
        <div class="flex items-center gap-3 p-2 rounded-lg hover:bg-white hover:shadow-sm transition-all cursor-pointer border border-transparent hover:border-slate-200">
          <div class="w-9 h-9 rounded-full bg-gradient-to-br from-slate-700 to-slate-800 flex items-center justify-center text-white text-sm font-semibold shadow-sm">
            张
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-semibold text-slate-900 truncate">张三</p>
            <p class="text-xs text-slate-500 truncate">zhangsan@company.com</p>
          </div>
          <el-icon class="text-slate-400" :size="16"><ArrowRight /></el-icon>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="flex-1 flex flex-col overflow-hidden min-w-0">
      <!-- 顶部栏 -->
      <header class="h-16 bg-white/80 backdrop-blur-sm border-b border-slate-200 flex items-center justify-between px-8 sticky top-0 z-20 flex-shrink-0">
        <div class="flex items-center gap-4">
          <h1 class="text-xl font-bold text-slate-900 tracking-tight">{{ currentTitle }}</h1>
        </div>

        <div class="flex items-center gap-2">
          <button class="p-2 text-slate-600 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-all" title="上传">
            <el-icon :size="20"><Upload /></el-icon>
          </button>
          <button class="p-2 text-slate-600 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-all" title="下载">
            <el-icon :size="20"><Download /></el-icon>
          </button>
          <div class="w-px h-5 bg-slate-200 mx-1"></div>
          <button class="p-2 text-slate-500 hover:text-slate-900 hover:bg-slate-100 rounded-md transition-all relative" title="消息通知">
            <el-icon :size="20"><Bell /></el-icon>
            <span class="absolute top-1.5 right-1.5 w-2 h-2 bg-red-500 rounded-full border-2 border-white"></span>
          </button>
        </div>
      </header>

      <!-- 内容区域 - 使用 router-view 或 slot -->
      <div class="flex-1 overflow-auto bg-slate-50/50">
        <!-- 方式1：使用 Vue Router -->
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>

        <!-- 方式2：使用 Slot（如果不使用路由） -->
        <!-- <slot></slot> -->
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  Folder,
  FolderOpened,
  Upload,
  Download,
  Bell,
  Link,
  Delete,
  ArrowRight
} from '@element-plus/icons-vue'

const router = useRouter()
const currentMenu = ref('personal')

// 分组的菜单项
const storageMenu = [
  { key: 'personal', label: '个人空间', icon: Folder, title: '个人空间', route: '/drive/personal' },
  { key: 'enterprise', label: '企业空间', icon: FolderOpened, title: '企业空间', route: '/drive/enterprise' }
]

const fileMenu = [
  { key: 'link', label: '安全外链', icon: Link, title: '安全外链管理', route: '/drive/links' },
  { key: 'recovery', label: '误删恢复', icon: Delete, title: '误删文件恢复', route: '/drive/recovery' }
]

// 合并所有菜单用于查找
const allMenus = [...storageMenu, ...fileMenu]

// 当前页面标题
const currentTitle = computed(() => {
  const item = allMenus.find(m => m.key === currentMenu.value)
  return item?.title || '个人空间'
})

// 处理菜单点击
const handleMenuClick = (item) => {
  currentMenu.value = item.key
  // 如果使用 Vue Router，这里进行路由跳转
  if (item.route) {
    router.push(item.route)
  }
}
</script>

<style>
/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
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
  background: #cbd5e1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>