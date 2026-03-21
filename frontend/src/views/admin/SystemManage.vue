<template>
  <div class="p-6 max-w-[1200px] mx-auto">
    <!-- 设置表单 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm">
      <form @submit.prevent="handleSave" class="p-6 space-y-8">
        
        <!-- 安全设置组 -->
        <div class="space-y-6">
          <div class="flex items-center gap-2 pb-2 border-b border-slate-100">
            <div class="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center">
              <el-icon class="text-blue-600" :size="18"><Lock /></el-icon>
            </div>
            <div>
              <h2 class="text-base font-semibold text-slate-900">安全设置</h2>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6 pl-0 md:pl-10">
            <!-- 登录失败次数阈值 -->
            <div class="space-y-2">
              <label class="block text-sm font-medium text-slate-700">
                登录失败次数阈值
                <span class="text-red-500">*</span>
              </label>
              <div class="relative">
                <input
                  v-model.number="form.loginFailThreshold"
                  type="number"
                  min="1"
                  max="10"
                  class="w-full h-10 px-3 pr-16 rounded-md border border-slate-200 bg-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                  placeholder="请输入次数"
                />
                <span class="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-slate-500">次</span>
              </div>
              <p class="text-xs text-slate-500">连续登录失败超过此次数将锁定账号</p>
            </div>

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
          </div>
        </div>

        <!-- 存储设置组 -->
        <div class="space-y-6">
          <div class="flex items-center gap-2 pb-2 border-b border-slate-100">
            <div class="w-8 h-8 rounded-lg bg-emerald-50 flex items-center justify-center">
              <el-icon class="text-emerald-600" :size="18"><Box /></el-icon>
            </div>
            <div>
              <h2 class="text-base font-semibold text-slate-900">存储设置</h2>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6 pl-0 md:pl-10">
            <!-- 默认空间存储容量大小 -->
            <div class="space-y-2">
              <label class="block text-sm font-medium text-slate-700">
                默认空间存储容量
                <span class="text-red-500">*</span>
              </label>
              <div class="relative">
                <input
                  v-model.number="form.defaultStorageQuota"
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
            <div class="space-y-2 md:col-span-2">
              <label class="block text-sm font-medium text-slate-700">
                存储空间预警阈值
                <span class="text-red-500">*</span>
              </label>
              <div class="flex items-center gap-4">
                <div class="relative flex-1 max-w-[200px]">
                  <input
                    v-model.number="form.storageWarningThreshold"
                    type="number"
                    min="1"
                    max="100"
                    class="w-full h-10 px-3 pr-12 rounded-md border border-slate-200 bg-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                    placeholder="请输入百分比"
                  />
                  <span class="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-slate-500">%</span>
                </div>
                <!-- 可视化进度条 -->
                <div class="flex-1 max-w-[300px] h-2 bg-slate-100 rounded-full overflow-hidden">
                  <div 
                    class="h-full rounded-full transition-all duration-300"
                    :class="getThresholdColor(form.storageWarningThreshold)"
                    :style="{ width: form.storageWarningThreshold + '%' }"
                  ></div>
                </div>
                <span class="text-sm text-slate-600 w-28">
                  超过 {{ form.storageWarningThreshold }}% 时预警
                </span>
              </div>
              <p class="text-xs text-slate-500">用户存储空间使用达到此百分比时发送预警通知</p>
            </div>
          </div>
        </div>

        <!-- 文件类型设置组 -->
        <div class="space-y-6">
          <div class="flex items-center gap-2 pb-2 border-b border-slate-100">
            <div class="w-8 h-8 rounded-lg bg-amber-50 flex items-center justify-center">
              <el-icon class="text-amber-600" :size="18"><Document /></el-icon>
            </div>
            <div>
              <h2 class="text-base font-semibold text-slate-900">文件类型设置</h2>
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
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Lock,
  Box,
  Document,
  Plus,
  Close,
  View,
  Hide,
  Loading,
  Check
} from '@element-plus/icons-vue'

// 表单数据
const form = reactive({
  loginFailThreshold: 5,
  defaultPassword: '123456',
  defaultStorageQuota: 10,
  maxFileSize: 500,
  storageWarningThreshold: 80,
  fileTypeBlacklist: ['.exe', '.bat', '.sh', '.php']
})

// 常用危险文件扩展名
const commonFileExts = ['exe', 'bat', 'sh', 'php', 'jsp', 'asp', 'dll', 'bin', 'cmd']

// 状态
const saving = ref(false)
const showPassword = ref(false)
const newFileExt = ref('')

// 获取阈值颜色
const getThresholdColor = (threshold) => {
  if (threshold >= 90) return 'bg-red-500'
  if (threshold >= 70) return 'bg-amber-500'
  return 'bg-emerald-500'
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
  if (!form.loginFailThreshold || form.loginFailThreshold < 1) {
    ElMessage.error('请输入有效的登录失败次数阈值')
    return
  }
  
  if (!form.defaultPassword) {
    ElMessage.error('请输入重置后的默认密码')
    return
  }
  
  if (!form.defaultStorageQuota || form.defaultStorageQuota < 1) {
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
  
  // 模拟API调用
  setTimeout(() => {
    saving.value = false
    ElMessage.success('系统设置保存成功')
  }, 800)
}
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
