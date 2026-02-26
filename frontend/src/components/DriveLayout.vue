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
          <span class="font-bold text-lg tracking-tight text-slate-800">企业云盘</span>
        </div>
      </div>

      <!-- 导航菜单 -->
      <nav class="flex-1 p-4 overflow-hidden flex flex-col">
        <!-- 存储空间 -->
        <div class="mb-6 flex-shrink-0">
          <h3 class="px-3 mb-2 text-xs font-semibold text-slate-400 uppercase tracking-wider">
            存储空间
          </h3>
          <div class="space-y-1">
            <!-- 企业空间（带展开按钮） -->
            <div class="space-y-1">
              <button
                  @click="toggleEnterprise"
                  :class="[
                  'w-full flex items-center justify-between px-3 py-2.5 rounded-md text-sm font-medium transition-all duration-200',
                  isEnterpriseActive
                    ? 'bg-slate-100 text-slate-900 shadow-sm ring-1 ring-slate-200'
                    : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
                ]"
              >
                <div class="flex items-center gap-3">
                  <el-icon :size="18" :class="isEnterpriseActive ? 'text-blue-600' : 'text-slate-500'">
                    <FolderOpened />
                  </el-icon>
                  <span>企业空间</span>
                </div>
                <el-icon
                    :size="14"
                    class="transition-transform duration-200"
                    :class="isEnterpriseExpanded ? 'rotate-180' : ''"
                >
                  <ArrowDown />
                </el-icon>
              </button>

              <!-- 组织架构树（固定高度，带滚动条） -->
              <div
                  v-show="isEnterpriseExpanded"
                  class="mt-1 bg-slate-50/50 rounded-lg border border-slate-100 overflow-auto custom-scrollbar"
                  style="max-height: 300px;"
              >
                <div class="p-2 inline-block min-w-full">
                  <el-tree
                      ref="treeRef"
                      :data="orgTree"
                      :props="treeProps"
                      node-key="id"
                      :default-expanded-keys="expandedKeys"
                      :current-node-key="currentNodeId"
                      highlight-current
                      :expand-on-click-node="true"
                      @node-click="handleNodeClick"
                      @node-expand="handleNodeExpand"
                      @node-collapse="handleNodeCollapse"
                      class="org-tree"
                  >
                    <template #default="{ node, data }">
                      <div
                          class="flex items-center gap-3 py-2.5 px-1 w-full min-w-0"
                          :title="node.label"
                      >
                        <el-icon
                            v-if="data.type === 'company'"
                            class="text-blue-600 flex-shrink-0"
                            :size="18"
                        >
                          <OfficeBuilding />
                        </el-icon>
                        <el-icon
                            v-else-if="data.type === 'dept'"
                            class="text-slate-400 flex-shrink-0"
                            :size="16"
                        >
                          <Folder />
                        </el-icon>
                        <span
                            class="text-sm flex-1 select-none whitespace-nowrap"
                            :class="[
                            node.isCurrent ? 'text-blue-600 font-semibold' : 'text-slate-700',
                            data.type === 'company' && 'font-semibold text-slate-900'
                          ]"
                        >
                          {{ node.label }}
                        </span>
                      </div>
                    </template>
                  </el-tree>
                </div>
              </div>
            </div>

            <!-- 个人空间 -->
            <button
                @click="handleMenuClick(storageMenu[0])"
                :class="[
                'w-full flex items-center gap-3 px-3 py-2.5 rounded-md text-sm font-medium transition-all duration-200',
                currentMenu === 'personal'
                  ? 'bg-slate-100 text-slate-900 shadow-sm ring-1 ring-slate-200'
                  : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900 hover:translate-x-0.5'
              ]"
            >
              <el-icon :size="18" :class="currentMenu === 'personal' ? 'text-blue-600' : 'text-slate-500'">
                <component :is="storageMenu[0].icon" />
              </el-icon>
              {{ storageMenu[0].label }}
            </button>

          </div>
        </div>

        <!-- 文件管理 -->
        <div class="flex-shrink-0">
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

      <!-- 用户信息 & 存储空间（始终固定在底部） -->
      <div class="p-4 border-t border-slate-200 bg-slate-50/50 flex-shrink-0 mt-auto">
        <!-- 存储空间 -->
        <div class="mb-4 p-4 bg-white rounded-xl border border-slate-200 shadow-sm">
          <div class="flex justify-between items-center mb-2">
            <span class="text-sm font-semibold text-slate-700">
              {{ isEnterpriseActive ? '企业存储' : '存储空间' }}
            </span>
            <span class="text-xs font-medium text-slate-500 tabular-nums">
              {{ isEnterpriseActive ? '2.4G / 10G' : '40M / 1G' }}
            </span>
          </div>
          <div class="h-2.5 bg-slate-100 rounded-full overflow-hidden">
            <div
                class="h-full bg-gradient-to-r from-blue-500 to-blue-600 rounded-full transition-all duration-500 shadow-sm"
                :style="{ width: isEnterpriseActive ? '24%' : '4%' }"
            ></div>
          </div>
        </div>

        <!-- 用户头像 -->
        <div
            class="flex items-center gap-3 p-2 rounded-lg hover:bg-white hover:shadow-sm transition-all
            cursor-pointer border border-transparent hover:border-slate-200"
            @click="handleMenuClick(accountMenu[0])"
        >
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
          <div class="w-px h-5 bg-slate-200 mx-1"></div>
          <button class="p-2 text-slate-600 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-all" title="上传">
            <el-icon :size="20"><Upload /></el-icon>
          </button>
          <button class="p-2 text-slate-600 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-all" title="下载">
            <el-icon :size="20"><Download /></el-icon>
          </button>
          <button class="p-2 text-slate-500 hover:text-slate-900 hover:bg-slate-100 rounded-md transition-all relative" title="消息">
            <el-icon :size="20"><Message /></el-icon>
            <span class="absolute top-1.5 right-1.5 w-2 h-2 bg-red-500 rounded-full border-2 border-white"></span>
          </button>
          <button class="p-2 text-slate-500 hover:text-slate-900 hover:bg-slate-100 rounded-md transition-all relative" title="公告">
            <el-icon :size="20"><Notification /></el-icon>
          </button>
        </div>
      </header>

      <!-- 内容区域 -->
      <div class="flex-1 overflow-auto bg-slate-50/50">
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
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Folder,
  FolderOpened,
  Upload,
  Download,
  Link,
  Delete,
  ArrowRight,
  ArrowDown,
  OfficeBuilding,
  Message,
  Notification,
  User
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const currentMenu = ref('personal')
const currentNodeId = ref('dept_2_2')
const isEnterpriseExpanded = ref(true) // 默认展开，与图片一致
const expandedKeys = ref(['root', 'dept_2']) // 默认展开的节点
const treeRef = ref(null)

// 组织架构树数据
const orgTree = [
  {
    id: 'root',
    label: '小明电器有限公司111111111111111111111',
    type: 'company',
    children: [
      {
        id: 'dept_1',
        label: '总办',
        type: 'dept',
        children: []
      },
      {
        id: 'dept_2',
        label: '家电产品部',
        type: 'dept',
        children: [
          {
            id: 'dept_2_1',
            label: '家电产品一部',
            type: 'dept',
            children: []
          },
          {
            id: 'dept_2_2',
            label: '家电产品二部',
            type: 'dept',
            children: []
          },
          {
            id: 'dept_2_3',
            label: '家电产品三部',
            type: 'dept',
            children: []
          }
        ]
      },
      {
        id: 'dept_3',
        label: '财务部',
        type: 'dept',
        children: []
      },
      {
        id: 'dept_4',
        label: '人力资源部',
        type: 'dept',
        children: []
      },
      {
        id: 'dept_4',
        label: '人力资源部',
        type: 'dept',
        children: []
      },
      {
        id: 'dept_4',
        label: '人力资源部',
        type: 'dept',
        children: []
      }
    ]
  }
]

const treeProps = {
  label: 'label',
  children: 'children'
}

// 分组的菜单项
const storageMenu = [
  { key: 'personal', label: '个人空间', icon: User, title: '个人空间', route: '/drive/personal' }
]

const fileMenu = [
  { key: 'link', label: '安全外链', icon: Link, title: '安全外链', route: '/drive/links' },
  { key: 'recovery', label: '误删恢复', icon: Delete, title: '误删恢复', route: '/drive/recovery' }
]

const accountMenu = [
  { key: 'profile', label: '个人中心', icon: User, title: '个人中心', route: '/drive/profile' }
]

// 是否在企业空间相关页面
const isEnterpriseActive = computed(() => {
  return currentMenu.value === 'enterprise' || currentMenu.value.startsWith('enterprise')
})

// 查找当前节点路径
const findNodePath = (tree, id, path = []) => {
  for (let node of tree) {
    if (node.id === id) {
      return [...path, node.label]
    }
    if (node.children && node.children.length > 0) {
      const result = findNodePath(node.children, id, [...path, node.label])
      if (result) return result
    }
  }
  return null
}

// 当前页面标题
const currentTitle = computed(() => {
  if (isEnterpriseActive.value) {
    const path = findNodePath(orgTree, currentNodeId.value)
    return path ? path[path.length - 1] : '企业空间'
  }
  const item = [...storageMenu, ...fileMenu, ...accountMenu].find(m => m.key === currentMenu.value)
  return item?.title || '个人空间'
})

// 切换企业空间展开/收起
const toggleEnterprise = () => {
  isEnterpriseExpanded.value = !isEnterpriseExpanded.value
  if (isEnterpriseExpanded.value && currentMenu.value !== 'enterprise') {
    currentMenu.value = 'enterprise'
    router.push('/drive/enterprise')
  }
}

// 处理菜单点击
const handleMenuClick = (item) => {
  currentMenu.value = item.key
  if (item.route) {
    router.push(item.route)
  }
}

// 处理树节点点击
const handleNodeClick = (data, node) => {
  currentNodeId.value = data.id
  currentMenu.value = 'enterprise'

  // 如果是叶子节点或部门，跳转路由
  router.push({
    path: '/drive/enterprise',
    query: { dept: data.id }
  })
}

// 处理节点展开/收起，同步 expandedKeys
const handleNodeExpand = (data) => {
  if (!expandedKeys.value.includes(data.id)) {
    expandedKeys.value.push(data.id)
  }
}

const handleNodeCollapse = (data) => {
  const index = expandedKeys.value.indexOf(data.id)
  if (index > -1) {
    expandedKeys.value.splice(index, 1)
  }
}
</script>

<style scoped>
/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 组织架构树样式 - 优化间距和滚动 */
:deep(.org-tree) {
  background: transparent;
  width: 100%;
}

:deep(.org-tree .el-tree-node__content) {
  height: 36px; /* 增加高度 */
  border-radius: 6px;
  margin: 2px 0;
  padding-right: 8px !important;
  transition: all 0.2s ease;
}

:deep(.org-tree .el-tree-node__content:hover) {
  background-color: #e2e8f0;
}

:deep(.org-tree .el-tree-node.is-current > .el-tree-node__content) {
  background-color: #dbeafe; /* 淡蓝色背景 */
  color: #2563eb;
}

:deep(.org-tree .el-tree-node__expand-icon) {
  color: #64748b;
  font-size: 14px;
  padding: 6px; /* 增加点击区域 */
  margin-right: 4px;
}

:deep(.org-tree .el-tree-node__expand-icon.is-leaf) {
  color: transparent;
  cursor: default;
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

/* 确保树节点文字不会溢出 */
:deep(.org-tree .el-tree-node__label) {
  width: 100%;
  overflow: hidden;
}

/* 全局滚动条样式 */
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