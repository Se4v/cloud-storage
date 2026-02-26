<template>
  <div class="h-full overflow-auto bg-slate-50/50 p-4 md:p-8">
    <div class="max-w-5xl mx-auto space-y-6">
      <!-- 页面标题 -->
      <div class="mb-8">
        <h1 class="text-2xl font-bold text-slate-900 tracking-tight">个人中心</h1>
        <p class="text-slate-500 mt-1 text-sm">管理您的账户信息、头像和安全设置</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 左侧：个人资料 -->
        <div class="lg:col-span-2 space-y-6">
          <!-- 基本信息卡片 -->
          <div class="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
            <div class="px-6 py-4 border-b border-slate-100 bg-slate-50/50">
              <h3 class="text-sm font-semibold text-slate-900 uppercase tracking-wider">基本信息</h3>
            </div>

            <div class="p-6 space-y-6">
              <!-- 头像上传区域 -->
              <div class="flex items-center gap-6">
                <div class="relative group cursor-pointer" @click="triggerUpload">
                  <div
                      class="w-24 h-24 rounded-full overflow-hidden bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center text-white text-2xl font-bold shadow-md ring-4 ring-white"
                      v-if="!userInfo.avatar"
                  >
                    {{ userInfo.realName.charAt(0) }}
                  </div>
                  <img
                      v-else
                      :src="userInfo.avatar"
                      class="w-24 h-24 rounded-full object-cover ring-4 ring-white shadow-md"
                      alt="avatar"
                  />
                  <!-- 悬停遮罩 -->
                  <div class="absolute inset-0 rounded-full bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                    <el-icon class="text-white" :size="24"><Camera /></el-icon>
                  </div>
                  <!-- 上传按钮 -->
                  <el-upload
                      ref="uploadRef"
                      class="hidden"
                      action="#"
                      :auto-upload="false"
                      :show-file-list="false"
                      :on-change="handleAvatarChange"
                      accept="image/*"
                  >
                    <template #trigger>
                      <div class="hidden"></div>
                    </template>
                  </el-upload>
                </div>
                <div>
                  <h4 class="text-lg font-semibold text-slate-900">{{ userInfo.realName }}</h4>
                  <p class="text-sm text-slate-500 mt-0.5">{{ userInfo.username }}</p>
                  <p class="text-xs text-slate-400 mt-2">点击头像更换，支持 JPG、PNG 格式</p>
                </div>
              </div>

              <div class="h-px bg-slate-100"></div>

              <!-- 信息表单 -->
              <div class="space-y-5">
                <!-- 用户名 - 不可修改 -->
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4 items-center">
                  <label class="text-sm font-medium text-slate-700">用户名</label>
                  <div class="md:col-span-2">
                    <el-input
                        v-model="userInfo.username"
                        disabled
                        class="w-full"
                        :prefix-icon="User"
                    >
                      <template #suffix>
                        <span class="text-xs text-slate-400 px-2">不可修改</span>
                      </template>
                    </el-input>
                  </div>
                </div>

                <!-- 真实姓名 - 不可修改 -->
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4 items-center">
                  <label class="text-sm font-medium text-slate-700">真实姓名</label>
                  <div class="md:col-span-2">
                    <el-input
                        v-model="userInfo.realName"
                        disabled
                        class="w-full"
                    >
                      <template #suffix>
                        <span class="text-xs text-slate-400 px-2">不可修改</span>
                      </template>
                    </el-input>
                  </div>
                </div>

                <!-- 邮箱 - 可编辑 -->
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4 items-center">
                  <label class="text-sm font-medium text-slate-700">邮箱</label>
                  <div class="md:col-span-2">
                    <div v-if="editingField !== 'email'" class="flex items-center gap-3">
                      <el-input
                          v-model="userInfo.email"
                          readonly
                          class="w-full"
                          :prefix-icon="Message"
                      />
                      <el-button
                          text
                          type="primary"
                          class="!rounded-md"
                          @click="startEdit('email')"
                      >
                        <el-icon class="mr-1"><Edit /></el-icon>
                        修改
                      </el-button>
                    </div>
                    <div v-else class="flex items-center gap-3">
                      <el-input
                          v-model="tempValue"
                          placeholder="请输入新邮箱"
                          class="w-full"
                          :prefix-icon="Message"
                          ref="emailInputRef"
                      />
                      <el-button
                          type="primary"
                          class="!rounded-md"
                          :loading="saving"
                          @click="saveField('email')"
                      >
                        <el-icon><Check /></el-icon>
                      </el-button>
                      <el-button
                          text
                          class="!rounded-md"
                          @click="cancelEdit"
                      >
                        <el-icon><Close /></el-icon>
                      </el-button>
                    </div>
                  </div>
                </div>

                <!-- 手机号 - 可编辑 -->
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4 items-center">
                  <label class="text-sm font-medium text-slate-700">手机号</label>
                  <div class="md:col-span-2">
                    <div v-if="editingField !== 'phone'" class="flex items-center gap-3">
                      <el-input
                          v-model="userInfo.phone"
                          readonly
                          class="w-full"
                          :prefix-icon="Phone"
                      />
                      <el-button
                          text
                          type="primary"
                          class="!rounded-md"
                          @click="startEdit('phone')"
                      >
                        <el-icon class="mr-1"><Edit /></el-icon>
                        修改
                      </el-button>
                    </div>
                    <div v-else class="flex items-center gap-3">
                      <el-input
                          v-model="tempValue"
                          placeholder="请输入新手机号"
                          class="w-full"
                          :prefix-icon="Phone"
                          ref="phoneInputRef"
                      />
                      <el-button
                          type="primary"
                          class="!rounded-md"
                          :loading="saving"
                          @click="saveField('phone')"
                      >
                        <el-icon><Check /></el-icon>
                      </el-button>
                      <el-button
                          text
                          class="!rounded-md"
                          @click="cancelEdit"
                      >
                        <el-icon><Close /></el-icon>
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：安全设置 -->
        <div class="lg:col-span-1">
          <div class="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden sticky top-6">
            <div class="px-6 py-4 border-b border-slate-100 bg-slate-50/50">
              <h3 class="text-sm font-semibold text-slate-900 uppercase tracking-wider">安全设置</h3>
            </div>

            <div class="p-6">
              <h4 class="text-base font-medium text-slate-900 mb-1">修改密码</h4>
              <p class="text-xs text-slate-500 mb-6">定期更换密码可以保护账户安全</p>

              <el-form
                  ref="passwordFormRef"
                  :model="passwordForm"
                  :rules="passwordRules"
                  label-position="top"
                  size="default"
                  class="space-y-4"
              >
                <el-form-item label="旧密码" prop="oldPassword" class="!mb-4">
                  <el-input
                      v-model="passwordForm.oldPassword"
                      type="password"
                      show-password
                      placeholder="请输入当前密码"
                      :prefix-icon="Lock"
                      class="!rounded-md"
                  />
                </el-form-item>

                <el-form-item label="新密码" prop="newPassword" class="!mb-4">
                  <el-input
                      v-model="passwordForm.newPassword"
                      type="password"
                      show-password
                      placeholder="请输入新密码（6-20位）"
                      :prefix-icon="Lock"
                      class="!rounded-md"
                  />
                </el-form-item>

                <el-form-item label="确认密码" prop="confirmPassword" class="!mb-6">
                  <el-input
                      v-model="passwordForm.confirmPassword"
                      type="password"
                      show-password
                      placeholder="请再次输入新密码"
                      :prefix-icon="Lock"
                      class="!rounded-md"
                  />
                </el-form-item>

                <el-button
                    type="primary"
                    class="w-full !rounded-md !h-10 font-medium"
                    :loading="passwordLoading"
                    @click="handleUpdatePassword"
                >
                  更新密码
                </el-button>
              </el-form>

              <div class="mt-6 pt-6 border-t border-slate-100">
                <div class="flex items-center gap-3 text-sm text-slate-600">
                  <div class="w-8 h-8 rounded-full bg-green-50 flex items-center justify-center flex-shrink-0">
                    <el-icon class="text-green-600" :size="16"><CircleCheck /></el-icon>
                  </div>
                  <div>
                    <p class="font-medium text-slate-900">账户安全</p>
                    <p class="text-xs text-slate-500 mt-0.5">上次登录：2024-01-15 14:30</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User,
  Message,
  Phone,
  Lock,
  Camera,
  Edit,
  Check,
  Close,
  CircleCheck
} from '@element-plus/icons-vue'

// 上传组件引用
const uploadRef = ref(null)
const emailInputRef = ref(null)
const phoneInputRef = ref(null)
const passwordFormRef = ref(null)

// 用户信息
const userInfo = reactive({
  username: 'zhangsan',
  realName: '张三',
  email: 'zhangsan@company.com',
  phone: '13800138000',
  avatar: '' // 空则显示文字头像
})

// 编辑状态
const editingField = ref('') // 当前编辑的字段：'email' | 'phone' | ''
const tempValue = ref('')
const saving = ref(false)

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const passwordLoading = ref(false)

// 自定义验证：确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 密码验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在 6 到 20 个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 触发头像上传
const triggerUpload = () => {
  // 模拟点击 upload 组件的触发器
  const input = document.querySelector('.el-upload input')
  if (input) input.click()
}

// 处理头像变更
const handleAvatarChange = (file) => {
  // 前端预览（实际项目中应上传至服务器）
  const isJPG = file.raw.type === 'image/jpeg'
  const isPNG = file.raw.type === 'image/png'
  const isLt2M = file.raw.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
    return
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return
  }

  // 本地预览
  const reader = new FileReader()
  reader.readAsDataURL(file.raw)
  reader.onload = () => {
    userInfo.avatar = reader.result
    ElMessage.success('头像更新成功')
    // 实际项目中这里调用 API 上传文件
  }
}

// 开始编辑
const startEdit = (field) => {
  editingField.value = field
  tempValue.value = userInfo[field]
  nextTick(() => {
    if (field === 'email' && emailInputRef.value) {
      emailInputRef.value.focus()
    } else if (field === 'phone' && phoneInputRef.value) {
      phoneInputRef.value.focus()
    }
  })
}

// 保存字段
const saveField = async (field) => {
  if (!tempValue.value.trim()) {
    ElMessage.warning('请输入有效内容')
    return
  }

  // 简单验证
  if (field === 'email' && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(tempValue.value)) {
    ElMessage.error('邮箱格式不正确')
    return
  }
  if (field === 'phone' && !/^1[3-9]\d{9}$/.test(tempValue.value)) {
    ElMessage.error('手机号格式不正确')
    return
  }

  saving.value = true
  try {
    // 模拟 API 调用
    await new Promise(resolve => setTimeout(resolve, 800))
    userInfo[field] = tempValue.value
    ElMessage.success('修改成功')
    editingField.value = ''
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

// 取消编辑
const cancelEdit = () => {
  editingField.value = ''
  tempValue.value = ''
}

// 更新密码
const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        // 模拟 API 调用
        await new Promise(resolve => setTimeout(resolve, 1000))
        ElMessage.success('密码修改成功，请重新登录')
        // 清空表单
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
      } catch (error) {
        ElMessage.error('密码修改失败')
      } finally {
        passwordLoading.value = false
      }
    }
  })
}
</script>

<style scoped>
/* Shadcn 风格覆盖 - 使 Element Plus 组件更贴近设计系统 */
:deep(.el-input__wrapper) {
  border-radius: 0.5rem;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  padding: 0 12px;
  transition: all 0.2s;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #cbd5e1 inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2) inset, 0 0 0 1px #3b82f6 inset;
}

:deep(.el-input__inner) {
  height: 40px;
  color: #0f172a;
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: #f8fafc;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
}

:deep(.el-input.is-disabled .el-input__inner) {
  color: #64748b;
  -webkit-text-fill-color: #64748b;
}

:deep(.el-button) {
  font-weight: 500;
  transition: all 0.2s;
}

:deep(.el-button--primary) {
  background-color: #2563eb;
  border-color: #2563eb;
}

:deep(.el-button--primary:hover) {
  background-color: #1d4ed8;
  border-color: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(37, 99, 235, 0.2);
}

:deep(.el-form-item__label) {
  color: #334155;
  font-weight: 500;
  padding-bottom: 6px;
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