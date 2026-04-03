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
                          :title="data.name"
                      >
                        <el-icon
                            v-if="data.type === 1"
                            class="text-blue-600 flex-shrink-0"
                            :size="18"
                        >
                          <OfficeBuilding />
                        </el-icon>
                        <el-icon
                            v-else-if="data.type === 2"
                            class="text-slate-400 flex-shrink-0"
                            :size="16"
                        >
                          <Folder />
                        </el-icon>
                        <el-icon
                            v-else-if="data.type === 3"
                            class="text-green-500 flex-shrink-0"
                            :size="16"
                        >
                          <User />
                        </el-icon>
                        <span
                            class="text-sm flex-1 select-none whitespace-nowrap"
                            :class="[
                            node.isCurrent ? 'text-blue-600 font-semibold' : 'text-slate-700',
                            data.type === 1 && 'font-semibold text-slate-900'
                          ]"
                        >
                          {{ data.name }}
                        </span>
                      </div>
                    </template>
                  </el-tree>
                </div>
              </div>
            </div>

            <!-- 个人空间 -->
            <button
                @click="handlePersonalSpaceClick"
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
        <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-4">
          <!-- 用户头像和名称 -->
          <div
              class="flex items-center gap-3 pb-3 border-b border-slate-100 cursor-pointer"
              @click="handleMenuClick(accountMenu[0])"
          >
            <!-- 头像 -->
            <div
                v-if="!userAvatar"
                class="w-10 h-10 rounded-full bg-gradient-to-br from-slate-700 to-slate-800 flex items-center justify-center text-white text-sm font-semibold shadow-sm flex-shrink-0"
            >
              ?
            </div>
            <img
                v-else
                :src="userAvatar"
                class="w-10 h-10 rounded-full object-cover shadow-sm flex-shrink-0"
                alt="avatar"
            />
            <div class="flex-1 min-w-0">
              <p class="text-sm font-semibold text-slate-900 truncate">个人中心</p>
            </div>
          </div>

          <!-- 退出管理中心按钮 -->
          <button
              @click="handleLogout"
              class="w-full flex items-center gap-2 mt-3 text-sm text-slate-600 hover:text-red-600 transition-colors"
          >
            <el-icon :size="16"><SwitchButton /></el-icon>
            <span class="font-medium">退出管理中心</span>
          </button>
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
          <button 
              class="p-2 text-slate-600 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-all relative" 
              title="上传"
              @click="uploadStore.toggleUploadPanel"
          >
            <el-icon :size="20"><Upload /></el-icon>
            <span 
              v-if="uploadStore.activeTaskCount > 0" 
              class="absolute -top-1 -right-1 min-w-[18px] h-[18px] px-1 bg-red-500 text-white text-xs font-medium rounded-full flex items-center justify-center border-2 border-white"
            >
              {{ uploadStore.activeTaskCount }}
            </span>
          </button>
          <div class="relative">
            <button 
              @click="toggleMessagePanel"
              class="p-2 text-slate-500 hover:text-slate-900 hover:bg-slate-100 rounded-md transition-all relative" 
              title="消息"
            >
              <el-icon :size="20"><Message /></el-icon>
              <span v-if="messageList.length > 0" class="absolute top-1.5 right-1.5 w-2 h-2 bg-red-500 rounded-full border-2 border-white"></span>
            </button>
            
            <!-- 消息列表面板 -->
            <div 
              v-show="isMessagePanelVisible"
              class="absolute top-full right-0 mt-2 w-[360px] bg-white rounded-lg shadow-lg border border-slate-200 flex flex-col z-50 overflow-hidden"
            >
              <!-- 消息列表 -->
              <div class="flex-1 overflow-y-auto max-h-[400px]">
                <div v-if="messageList.length === 0" class="py-12 text-center text-slate-500">
                  <el-icon :size="40" class="mb-2 opacity-40"><Message /></el-icon>
                  <p class="text-sm">暂无消息</p>
                </div>
                
                <div 
                  v-for="message in messageList" 
                  :key="message.id"
                  class="flex items-start gap-3 px-4 py-3 hover:bg-slate-50 cursor-pointer transition-colors border-b border-slate-100 last:border-b-0"
                  :class="{ 'bg-slate-50/80': !message.isRead }"
                >
                  <!-- 消息图标 -->
                  <div 
                    class="w-9 h-9 rounded-full flex items-center justify-center flex-shrink-0"
                    :class="getMessageIconClass(message.type)"
                  >
                    <el-icon :size="16">
                      <component :is="getMessageIcon(message.type)" />
                    </el-icon>
                  </div>
                  
                  <!-- 消息内容 -->
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-medium text-slate-900 truncate">{{ message.title }}</p>
                    <p class="text-sm text-slate-600 line-clamp-2 mt-0.5">{{ message.content }}</p>
                    <p class="text-xs text-slate-400 mt-1">{{ message.createTime }}</p>
                  </div>
                </div>
              </div>
              
              <!-- 面板底部 -->
              <div class="flex items-center border-t border-slate-200">
                <button 
                  @click="markAllAsRead"
                  class="flex-1 px-4 py-2.5 text-sm text-slate-600 hover:text-slate-900 hover:bg-slate-50 transition-colors text-center"
                >
                  全部已读
                </button>
                <div class="w-px h-6 bg-slate-200"></div>
                <button 
                  @click="viewAllMessages"
                  class="flex-1 px-4 py-2.5 text-sm text-slate-600 hover:text-slate-900 hover:bg-slate-50 transition-colors text-center"
                >
                  查看更多
                </button>
              </div>
            </div>
          </div>
        </div>
      </header>

      <!-- 内容区域 -->
      <div class="flex-1 overflow-auto bg-slate-50/50 relative">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
        
        <!-- 上传进度面板 -->
        <div 
          v-show="uploadStore.isUploadPanelVisible"
          class="absolute bottom-4 right-4 w-[480px] max-h-[600px] bg-white rounded-xl shadow-2xl border border-slate-200 flex flex-col z-50 overflow-hidden"
        >
          <!-- 面板头部 -->
          <div class="flex items-center justify-between px-4 py-3 border-b border-slate-200 bg-slate-50/80">
            <div class="flex items-center gap-2">
              <el-icon class="text-blue-600" :size="18"><Upload /></el-icon>
              <span class="font-semibold text-slate-800">上传任务</span>
              <span v-if="uploadStore.activeTaskCount > 0" class="text-sm text-blue-600 font-medium">
                ({{ uploadStore.activeTaskCount }} 个进行中)
              </span>
            </div>
            <div class="flex items-center gap-1">
              <button 
                v-if="uploadStore.completedTaskCount > 0"
                @click="uploadStore.clearCompletedTasks"
                class="px-3 py-1.5 text-xs text-slate-600 hover:text-slate-900 hover:bg-slate-200 rounded-md transition-colors"
              >
                清除已完成
              </button>
              <button 
                @click="uploadStore.hideUploadPanel"
                class="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-200 rounded-md transition-colors"
              >
                <el-icon :size="16"><Close /></el-icon>
              </button>
            </div>
          </div>
          
          <!-- 任务列表 -->
          <div class="flex-1 overflow-y-auto max-h-[400px] p-2">
            <div v-if="uploadStore.uploadTasks.length === 0" class="py-12 text-center text-slate-400">
              <el-icon :size="48" class="mb-3 opacity-30"><Upload /></el-icon>
              <p class="text-sm">暂无上传任务</p>
            </div>
            
            <div 
              v-for="task in uploadStore.uploadTasks" 
              :key="task.id"
              class="flex flex-col gap-2 p-3 mb-2 rounded-lg border border-slate-100 hover:border-slate-200 hover:bg-slate-50/50 transition-all"
              :class="{ 'bg-blue-50/30 border-blue-100': task.status === 'uploading' }"
            >
              <!-- 文件信息 -->
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-lg bg-blue-100 flex items-center justify-center flex-shrink-0">
                  <el-icon class="text-blue-600" :size="18"><Document /></el-icon>
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium text-slate-800 truncate" :title="task.name">
                    {{ task.name }}
                  </p>
                  <p class="text-xs text-slate-500">
                    <span v-if="task.isSkip" class="text-green-600 font-medium">秒传成功</span>
                    <span v-else-if="task.status === 'completed'" class="text-green-600 font-medium">上传完成</span>
                    <span v-else-if="task.status === 'error'" class="text-red-600">{{ task.errorMessage || '上传失败' }}</span>
                    <span v-else-if="task.status === 'waiting'">等待中</span>
                    <span v-else-if="task.status === 'paused'">已暂停</span>
                    <span v-else-if="task.status === 'uploading' && task.totalChunks > 0">
                      分片 {{ task.uploadedChunks }}/{{ task.totalChunks }}
                    </span>
                    <span v-else-if="task.status === 'uploading'">
                      {{ uploadStore.formatFileSize(task.loaded) }} / {{ uploadStore.formatFileSize(task.size) }}
                    </span>
                    <span v-else>{{ uploadStore.formatFileSize(task.size) }}</span>
                  </p>
                </div>
                <!-- 状态图标 -->
                <div class="flex-shrink-0">
                  <el-icon v-if="task.status === 'completed'" class="text-green-500" :size="20"><CircleCheckFilled /></el-icon>
                  <el-icon v-else-if="task.status === 'error'" class="text-red-500" :size="20"><CircleCloseFilled /></el-icon>
                  <el-icon v-else-if="task.status === 'waiting'" class="text-slate-400" :size="20"><Timer /></el-icon>
                  <button 
                    v-else-if="task.status === 'paused'"
                    @click="uploadStore.resumeTask(task.id)"
                    class="p-1.5 text-blue-600 hover:bg-blue-100 rounded-md transition-colors"
                  >
                    <el-icon :size="18"><VideoPlay /></el-icon>
                  </button>
                  <button 
                    v-else-if="task.status === 'uploading'"
                    @click="uploadStore.pauseTask(task.id)"
                    class="p-1.5 text-slate-500 hover:bg-slate-200 rounded-md transition-colors"
                  >
                    <el-icon :size="18"><VideoPause /></el-icon>
                  </button>
                </div>
                <!-- 删除按钮 -->
                <button 
                  @click="uploadStore.removeTask(task.id)"
                  class="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-md transition-colors"
                >
                  <el-icon :size="16"><Close /></el-icon>
                </button>
              </div>
              
              <!-- 进度条 -->
              <div v-if="task.status !== 'waiting'" class="flex items-center gap-3">
                <div class="flex-1 h-1.5 bg-slate-200 rounded-full overflow-hidden">
                  <div 
                    class="h-full bg-blue-600 rounded-full transition-all duration-300"
                    :class="{ 'bg-green-500': task.status === 'completed', 'bg-red-500': task.status === 'error' }"
                    :style="{ width: `${task.progress}%` }"
                  ></div>
                </div>
                <span class="text-xs font-medium text-slate-600 w-10 text-right">{{ task.progress }}%</span>
              </div>
              
              <!-- 速度信息 -->
              <div v-if="task.status === 'uploading' && task.speed > 0 && !task.isSkip" class="flex items-center justify-between text-xs text-slate-500">
                <span>{{ uploadStore.formatSpeed(task.speed) }}</span>
                <span>剩余 {{ uploadStore.formatRemainingTime(task) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Folder,
  FolderOpened,
  Upload,
  Link,
  Delete,
  ArrowDown,
  OfficeBuilding,
  Message,
  User,
  Close,
  Document,
  CircleCheckFilled,
  CircleCloseFilled,
  Timer,
  VideoPlay,
  VideoPause,
  InfoFilled,
  WarningFilled,
  SuccessFilled,
  Bell,
  SwitchButton
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user.js'
import { useUploadStore } from '@/stores/upload.js'
import axios from "axios";
import {ElMessage} from "element-plus";

const userStore = useUserStore()
const uploadStore = useUploadStore()

// 用户头像和存储信息
const userAvatar = ref('')

// 加载用户头像
const loadUserAvatar = async () => {
  try {
    const avatarRes = await axios.get(`${API_BASE_URL}/api/profile/avatar`, getAuthConfig())
    if (avatarRes.data.code === 200) {
      userAvatar.value = avatarRes.data.data
    } else {
      userAvatar.value = ''
    }
  } catch (avatarError) {
    console.error('获取头像链接失败:', avatarError)
    userAvatar.value = ''
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    const res = await axios.post(`${API_BASE_URL}/api/auth/logout`, getAuthConfig())
    if (res.data.code === 200) {
      ElMessage.success("登出成功")
    } else {
      ElMessage.warning('登出失败')
    }
  } catch (error) {
    console.error('登出异常:', error)
  } finally {
    userStore.$reset()
    await router.push('/login')
  }
}

// API 基础配置
const API_BASE_URL = 'http://localhost:8080'

// 获取请求配置（包含认证头）
const getAuthConfig = () => {
  const token = userStore.token
  return {
    headers: {
      'Authorization': token ? `Bearer ${token}` : '',
      'Content-Type': 'application/json'
    }
  }
}

const router = useRouter()
const route = useRoute()
const currentMenu = ref('personal')
const currentNodeId = ref('dept_2_2')
const isEnterpriseExpanded = ref(true) // 默认展开，与图片一致
const expandedKeys = ref(['root', 'dept_2']) // 默认展开的节点
const treeRef = ref(null)

// 消息面板相关
const isMessagePanelVisible = ref(false)
const messageList = ref([])

// 加载未读消息列表
const loadUnreadMessages = async () => {
  try {
    const res = await axios.get(`${API_BASE_URL}/api/notice/unread`, getAuthConfig())
    if (res.data.code === 200) {
      messageList.value = res.data.data || []
    } else {
      console.error('加载消息列表失败:', res.data.msg)
    }
  } catch (error) {
    console.error('加载消息列表失败:', error)
  }
}

// 切换消息面板显示/隐藏
const toggleMessagePanel = () => {
  isMessagePanelVisible.value = !isMessagePanelVisible.value
}

// 标记所有消息为已读
const markAllAsRead = async () => {
  if (messageList.value.length === 0) return
  
  try {
    const ids = messageList.value.map(msg => msg.id)
    const res = await axios.post(`${API_BASE_URL}/api/notice/read`, { ids }, getAuthConfig())
    if (res.data.code === 200) {
      messageList.value = []
    } else {
      console.error('标记已读失败:', res.data.msg)
    }
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// 查看全部消息
const viewAllMessages = () => {
  isMessagePanelVisible.value = false
  router.push('/drive/notice')
}

// 获取消息图标
const getMessageIcon = (type) => {
  const iconMap = {
    1: InfoFilled,
    2: WarningFilled,
    3: SuccessFilled
  }
  return iconMap[type] || InfoFilled
}

// 获取消息图标样式
const getMessageIconClass = (type) => {
  const classMap = {
    1: 'bg-red-100 text-red-500',
    2: 'bg-cyan-100 text-cyan-500',
    3: 'bg-yellow-100 text-yellow-500'
  }
  return classMap[type] || 'bg-slate-100 text-slate-600'
}

// 组织架构树数据
const orgTree = ref([])

// 从后端获取组织架构树
const loadOrgTree = async () => {
  try {
    const res = await axios.get(`${API_BASE_URL}/api/enterprise/org`, getAuthConfig())
    if (res.data.code === 200) {
      orgTree.value = res.data.data || []
    } else {
      console.error('加载组织架构树失败:', res.data.msg)
      orgTree.value = []
    }
  } catch (error) {
    console.error('加载组织架构树失败:', error)
    orgTree.value = []
  }
}

const treeProps = {
  label: 'name',
  children: 'children'
}

// 分组的菜单项
const storageMenu = [
  { key: 'personal', label: '个人空间', icon: User, title: '个人空间' }
]

const fileMenu = [
  { key: 'link', label: '安全外链', icon: Link, title: '安全外链', route: '/drive/links' },
  { key: 'recovery', label: '误删恢复', icon: Delete, title: '误删恢复', route: '/drive/recovery' },
  { key: 'notice', label: '消息通知', icon: Bell, title: '消息通知', route: '/drive/notice' }
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
      return [...path, node.name]
    }
    if (node.children && node.children.length > 0) {
      const result = findNodePath(node.children, id, [...path, node.name])
      if (result) return result
    }
  }
  return null
}

// 当前页面标题
const currentTitle = computed(() => {
  if (isEnterpriseActive.value) {
    const path = findNodePath(orgTree.value, currentNodeId.value)
    return path ? path[path.length - 1] : '企业空间'
  }
  const item = [...storageMenu, ...fileMenu, ...accountMenu].find(m => m.key === currentMenu.value)
  return item?.title || '个人空间'
})

// 切换企业空间展开/收起
const toggleEnterprise = () => {
  isEnterpriseExpanded.value = !isEnterpriseExpanded.value
}

// 页面加载时获取组织架构树、未读消息、用户头像和存储信息
onMounted(() => {
  loadOrgTree()
  loadUnreadMessages()
  loadUserAvatar()
})

// 处理个人空间点击
const handlePersonalSpaceClick = async () => {
  currentMenu.value = 'personal'
  
  // 如果 Pinia 中没有 personalDriveId，先获取
  if (!userStore.personalDriveId) {
    const response = await axios.get(`${API_BASE_URL}/api/personal/id`, getAuthConfig())
    userStore.personalDriveId = response.data.data
  }
  
  // 使用 router.push 跳转到个人空间
  await router.push({
    name: 'PersonalDrive',
    params: {
      driveId: userStore.personalDriveId
    }
  })
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
  
  // 将节点的id赋值给userStore的orgId字段
  userStore.orgId = data.id

  // 如果是叶子节点或部门，跳转路由
  router.push({
    name: 'EnterpriseDrive',
    params: {
      driveId: data.driveId
    }
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