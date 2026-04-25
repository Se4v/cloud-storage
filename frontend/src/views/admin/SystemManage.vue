<template>
  <div class="p-6 max-w-[1200px] mx-auto">
    <!-- 设置表单 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm">
      <form @submit.prevent="handleSave" class="p-6 space-y-8">
        
        <!-- 系统设置组 -->
        <div class="space-y-6">
          <div class="flex items-center gap-2 pb-2 border-b border-slate-100">
            <div class="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center">
              <el-icon class="text-blue-600" :size="18"><Lock /></el-icon>
            </div>
            <div>
              <h2 class="text-base font-semibold text-slate-900">系统设置</h2>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6 pl-0 md:pl-10">
            <!-- 重置后的默认密码 -->
            <div class="space-y-2">
              <label class="block text-sm font-medium text-slate-700">
                重置后的默认密码
                <span class="text-red-500">*</span>
              </label>
              <div class="relative">
                <input
                  v-model="form.defaultPassword"
                  :type="showPassword ? 'text' : 'password'"
                  class="w-full h-10 px-3 pr-10 rounded-md border border-slate-200 bg-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                  placeholder="请输入默认密码"
                />
                <button
                  type="button"
                  @click="showPassword = !showPassword"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
                >
                  <el-icon :size="16">
                    <View v-if="!showPassword" />
                    <Hide v-else />
                  </el-icon>
                </button>
              </div>
              <p class="text-xs text-slate-500">管理员重置用户密码后的默认密码</p>
            </div>

            <!-- 默认空间存储容量大小 -->
            <div class="space-y-2">
              <label class="block text-sm font-medium text-slate-700">
                默认空间存储容量
                <span class="text-red-500">*</span>
              </label>
              <div class="relative">
                <input
                    v-model.number="displayStorageQuota"
                    type="number"
                    min="1"
                    class="w-full h-10 px-3 pr-16 rounded-md border border-slate-200 bg-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                    placeholder="请输入容量"
                />
                <span class="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-slate-500">GB</span>
              </div>
              <p class="text-xs text-slate-500">新用户注册时默认分配的存储空间</p>
            </div>

            <!-- 单文件最大上传限制 -->
            <div class="space-y-2">
              <label class="block text-sm font-medium text-slate-700">
                单文件最大上传限制
                <span class="text-red-500">*</span>
              </label>
              <div class="relative">
                <input
                    v-model.number="form.maxFileSize"
                    type="number"
                    min="1"
                    class="w-full h-10 px-3 pr-16 rounded-md border border-slate-200 bg-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                    placeholder="请输入大小"
                />
                <span class="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-slate-500">MB</span>
              </div>
              <p class="text-xs text-slate-500">单个文件允许上传的最大大小</p>
            </div>

            <!-- 存储空间预警阈值 -->
            <div class="space-y-2">
              <label class="block text-sm font-medium text-slate-700">
                存储空间预警阈值
                <span class="text-red-500">*</span>
              </label>
              <div class="relative">
                <input
                    v-model.number="form.storageWarningThreshold"
                    type="number"
                    min="1"
                    max="100"
                    class="w-full h-10 px-3 pr-16 rounded-md border border-slate-200 bg-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                    placeholder="请输入百分比"
                />
                <span class="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-slate-500">%</span>
              </div>
              <p class="text-xs text-slate-500">用户存储空间使用达到此百分比时发送预警通知</p>
            </div>
          </div>

          <div class="pl-0 md:pl-10">
            <!-- 文件类型黑名单 -->
            <div class="space-y-3">
              <label class="block text-sm font-medium text-slate-700">
                文件类型黑名单
              </label>

              <!-- 标签展示 -->
              <div class="flex flex-wrap gap-2 min-h-[40px] p-3 rounded-md border border-slate-200 bg-slate-50/50">
                <span
                    v-for="(ext, index) in form.fileTypeBlacklist"
                    :key="index"
                    class="inline-flex items-center gap-1 px-2.5 py-1 rounded-md bg-white border border-slate-200 text-sm text-slate-700 shadow-sm"
                >
                  {{ ext }}
                  <button
                      type="button"
                      @click="removeFileExt(index)"
                      class="text-slate-400 hover:text-red-500 transition-colors"
                  >
                    <el-icon :size="14"><Close /></el-icon>
                  </button>
                </span>
                <span v-if="form.fileTypeBlacklist.length === 0" class="text-sm text-slate-400 py-1">
                  暂无限制的文件类型
                </span>
              </div>

              <!-- 添加输入框 -->
              <div class="flex gap-2">
                <div class="relative flex-1 max-w-[300px]">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <span class="text-slate-400 text-sm">.</span>
                  </div>
                  <input
                      v-model="newFileExt"
                      type="text"
                      class="w-full h-10 pl-6 pr-4 rounded-md border border-slate-200 bg-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                      placeholder="输入扩展名，如：exe"
                      @keyup.enter="addFileExt"
                  />
                </div>
                <button
                    type="button"
                    @click="addFileExt"
                    :disabled="!newFileExt.trim()"
                    class="h-10 px-4 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 disabled:cursor-not-allowed text-white rounded-md text-sm font-medium transition-colors inline-flex items-center gap-1"
                >
                  <el-icon :size="16"><Plus /></el-icon>
                  添加
                </button>
              </div>

              <!-- 快捷添加常用类型 -->
              <div class="flex items-center gap-2 flex-wrap">
                <span class="text-xs text-slate-500">快捷添加：</span>
                <button
                    v-for="ext in commonFileExts"
                    :key="ext"
                    type="button"
                    @click="quickAddExt(ext)"
                    :disabled="form.fileTypeBlacklist.includes('.' + ext)"
                    class="px-2 py-1 text-xs rounded-md border border-slate-200 bg-white hover:bg-slate-50 disabled:bg-slate-100 disabled:text-slate-400 disabled:cursor-not-allowed transition-colors"
                >
                  .{{ ext }}
                </button>
              </div>

              <p class="text-xs text-slate-500">被列入黑名单的文件类型将无法上传，支持扩展名格式（如：.exe）</p>
            </div>
          </div>
        </div>

        <!-- 底部操作按钮 -->
        <div class="pt-6 border-t border-slate-200 flex items-center justify-end gap-3">
          <button
            type="submit"
            :disabled="saving"
            class="h-10 px-5 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 disabled:cursor-not-allowed text-white rounded-md text-sm font-medium transition-colors inline-flex items-center gap-2"
          >
            <el-icon v-if="saving" class="animate-spin"><Loading /></el-icon>
            <el-icon v-else :size="16"><Check /></el-icon>
            {{ saving ? '保存中...' : '保存设置' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import {
  Lock,
  Plus,
  Close,
  View,
  Hide,
  Loading,
  Check
} from '@element-plus/icons-vue'
import { useUserStore } from "@/stores/user.js";

const userStore = useUserStore()
const API_BASE_URL = 'http://localhost:8080'

// 获取认证配置
const getAuthConfig = () => {
  const token = userStore.token
  return {
    headers: {
      'Authorization': token ? `Bearer ${token}` : '',
      'Content-Type': 'application/json'
    }
  }
}

// 表单数据
const form = reactive({
  defaultPassword: '123456',
  totalQuota: 10737418240, // 10 GB in bytes (10 * 1024 * 1024 * 1024)
  maxFileSize: 500,
  storageWarningThreshold: 80,
  fileTypeBlacklist: ['.exe', '.bat', '.sh', '.php']
})

// 显示用的存储容量（GB）
const displayStorageQuota = computed({
  get: () => {
    // 将字节转换为 GB 显示
    return Math.round(form.totalQuota / 1024 / 1024 / 1024)
  },
  set: (value) => {
    // 将 GB 转换为字节存储
    form.totalQuota = value * 1024 * 1024 * 1024
  }
})

// 常用危险文件扩展名
const commonFileExts = ['exe', 'bat', 'sh', 'php', 'jsp', 'asp', 'dll', 'bin', 'cmd']

// 状态
const saving = ref(false)
const showPassword = ref(false)
const newFileExt = ref('')

// 加载系统设置
const loadSystemConfig = async () => {
  try {
    const { data: res } = await axios.get(`${API_BASE_URL}/api/system`, getAuthConfig())
    if (res.code !== 200) {
      ElMessage.error(res.msg || '获取系统设置失败')
      return
    }
    const data = res.data
    // 后端返回的都是 String 类型，需要转换
    form.defaultPassword = data.defaultPassword || '123456'
    form.totalQuota = parseInt(data.totalQuota) || 10737418240
    form.maxFileSize = parseInt(data.maxFileSize) || 500
    form.storageWarningThreshold = parseInt(data.storageWarningThreshold) || 80
    form.fileTypeBlacklist = data.fileTypeBlacklist || ['.exe', '.bat', '.sh', '.php']
  } catch (error) {
    console.error('获取系统设置失败:', error)
    ElMessage.error(error.message || '获取系统设置失败')
  }
}

// 添加文件扩展名
const addFileExt = () => {
  const ext = newFileExt.value.trim().toLowerCase()
  if (!ext) return
  
  // 自动添加点号
  const formattedExt = ext.startsWith('.') ? ext : '.' + ext
  
  if (form.fileTypeBlacklist.includes(formattedExt)) {
    ElMessage.warning('该文件类型已在黑名单中')
    return
  }
  
  form.fileTypeBlacklist.push(formattedExt)
  newFileExt.value = ''
  ElMessage.success('添加成功')
}

// 快捷添加
const quickAddExt = (ext) => {
  const formattedExt = '.' + ext.toLowerCase()
  if (!form.fileTypeBlacklist.includes(formattedExt)) {
    form.fileTypeBlacklist.push(formattedExt)
  }
}

// 移除文件扩展名
const removeFileExt = (index) => {
  form.fileTypeBlacklist.splice(index, 1)
}

// 保存设置
const handleSave = async () => {
  // 表单验证
  
  if (!form.defaultPassword) {
    ElMessage.error('请输入重置后的默认密码')
    return
  }
  
  if (!displayStorageQuota.value || displayStorageQuota.value < 1) {
    ElMessage.error('请输入有效的默认空间存储容量')
    return
  }
  
  if (!form.maxFileSize || form.maxFileSize < 1) {
    ElMessage.error('请输入有效的单文件最大上传限制')
    return
  }
  
  if (!form.storageWarningThreshold || form.storageWarningThreshold < 1 || form.storageWarningThreshold > 100) {
    ElMessage.error('请输入有效的存储空间预警阈值（1-100）')
    return
  }
  
  saving.value = true
  
  try {
    // 构造提交数据（后端要求 String 类型）
    const submitData = {
      defaultPassword: form.defaultPassword,
      totalQuota: String(form.totalQuota),
      maxFileSize: String(form.maxFileSize),
      storageWarningThreshold: String(form.storageWarningThreshold),
      fileTypeBlacklist: form.fileTypeBlacklist
    }
    
    const { data: res } = await axios.post(`${API_BASE_URL}/api/system/update`, submitData, getAuthConfig())
    if (res.code !== 200) {
      ElMessage.error(res.msg || '保存失败')
      return
    }
    ElMessage.success('系统设置保存成功')
  } catch (error) {
    console.error('保存系统设置失败:', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 页面加载时获取系统设置
onMounted(() => {
  loadSystemConfig()
})
</script>

<style scoped>
/* 输入框数字类型去除箭头 */
input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

input[type="number"] {
  -moz-appearance: textfield;
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
</style>
