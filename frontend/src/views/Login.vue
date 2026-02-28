<template>
  <div class="min-h-screen w-full flex bg-background">
    <!-- 左侧品牌展示区 -->
    <div class="hidden lg:flex lg:w-1/2 xl:w-3/5 relative overflow-hidden bg-slate-950">
      <!-- 装饰性背景元素 -->
      <div class="absolute inset-0 bg-gradient-to-br from-slate-900 via-slate-950 to-slate-900" />
      <div class="absolute top-0 left-0 w-full h-full overflow-hidden">
        <div class="absolute -top-1/2 -left-1/2 w-full h-full bg-purple-500/10 rounded-full blur-3xl animate-pulse" />
        <div class="absolute -bottom-1/2 -right-1/2 w-full h-full bg-blue-500/10 rounded-full blur-3xl animate-pulse delay-1000" />
      </div>

      <!-- 内容 -->
      <div class="relative z-10 flex flex-col justify-between p-12 w-full">
        <div class="flex items-center gap-2 text-white">
          <div class="w-8 h-8 bg-white rounded-lg flex items-center justify-center">
            <svg class="w-5 h-5 text-slate-900" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 15a4 4 0 004 4h9a5 5 0 10-.1-9.999 5.002 5.002 0 10-9.78 2.096A4.001 4.001 0 003 15z" />
            </svg>
          </div>
          <span class="text-xl font-bold tracking-tight">Enterprise Cloud</span>
        </div>

        <div class="space-y-6 max-w-lg">
          <h1 class="text-4xl xl:text-5xl font-bold text-white tracking-tight leading-tight">
            安全、高效的企业<br/>文件协作平台
          </h1>
          <p class="text-slate-400 text-lg leading-relaxed">
            为现代企业打造的智能云存储解决方案，提供银行级加密、团队协作和无缝同步体验。
          </p>

          <!-- 特性列表 -->
          <div class="space-y-4 pt-4">
            <div class="flex items-center gap-3 text-slate-300">
              <div class="w-6 h-6 rounded-full bg-green-500/20 flex items-center justify-center">
                <svg class="w-4 h-4 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <span class="text-sm font-medium">端到端加密传输与存储</span>
            </div>
            <div class="flex items-center gap-3 text-slate-300">
              <div class="w-6 h-6 rounded-full bg-blue-500/20 flex items-center justify-center">
                <svg class="w-4 h-4 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <span class="text-sm font-medium">支持 500+ 人实时协作编辑</span>
            </div>
            <div class="flex items-center gap-3 text-slate-300">
              <div class="w-6 h-6 rounded-full bg-purple-500/20 flex items-center justify-center">
                <svg class="w-4 h-4 text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <span class="text-sm font-medium">99.99% SLA 服务可用性保障</span>
            </div>
          </div>
        </div>

        <div class="text-slate-500 text-sm">
          © 2026 Enterprise Cloud. 保留所有权利。
        </div>
      </div>
    </div>

    <!-- 右侧登录表单区 -->
    <div class="w-full lg:w-1/2 xl:w-2/5 flex items-center justify-center p-6 sm:p-8 lg:p-12 bg-slate-50">
      <div class="w-full max-w-sm">
        <!-- 移动端 Logo -->
        <div class="lg:hidden flex items-center justify-center gap-2 mb-8">
          <div class="w-8 h-8 bg-slate-900 rounded-lg flex items-center justify-center">
            <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 15a4 4 0 004 4h9a5 5 0 10-.1-9.999 5.002 5.002 0 10-9.78 2.096A4.001 4.001 0 003 15z" />
            </svg>
          </div>
          <span class="text-xl font-bold text-slate-900 tracking-tight">Enterprise Cloud</span>
        </div>

        <!-- Card 卡片容器 -->
        <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-8 space-y-6">
          <!-- 头部标题 -->
          <div class="space-y-2 text-center">
            <h2 class="text-2xl font-semibold tracking-tight text-slate-900">欢迎回来</h2>
            <p class="text-sm text-slate-500">
              请输入您的企业账号以继续访问
            </p>
          </div>

          <!-- 用户/管理中心切换 - shadcn 风格 Segmented Control -->
          <div class="flex justify-center">
            <div class="w-full bg-slate-100 p-1 rounded-lg flex gap-1">
              <button
                  @click="loginType = 'user'"
                  :class="[
                  'flex-1 px-4 py-2 text-sm font-medium rounded-md transition-all duration-200',
                  loginType === 'user'
                    ? 'bg-white text-slate-900 shadow-sm'
                    : 'text-slate-600 hover:text-slate-900 hover:bg-slate-200/50'
                ]"
              >
                用户登录
              </button>
              <button
                  @click="loginType = 'admin'"
                  :class="[
                  'flex-1 px-4 py-2 text-sm font-medium rounded-md transition-all duration-200',
                  loginType === 'admin'
                    ? 'bg-white text-slate-900 shadow-sm'
                    : 'text-slate-600 hover:text-slate-900 hover:bg-slate-200/50'
                ]"
              >
                管理中心
              </button>
            </div>
          </div>

          <!-- 登录表单 -->
          <el-form
              ref="loginFormRef"
              :model="loginForm"
              :rules="loginRules"
              class="space-y-4"
              @keyup.enter="handleLogin"
          >
            <el-form-item prop="username" class="!mb-7">
              <el-input
                  v-model="loginForm.username"
                  placeholder="请输入用户名"
                  size="large"
                  class="!h-11"
              >
                <template #prefix>
                  <svg class="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="password" class="!mb-7">
              <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="••••••••"
                  size="large"
                  show-password
                  class="!h-11"
              >
                <template #prefix>
                  <svg class="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </template>
              </el-input>
            </el-form-item>

            <div class="flex items-center justify-end pt-1">
              <button
                  type="button"
                  class="text-sm font-medium text-slate-900 hover:text-slate-700 transition-colors"
                  @click="handleForgotPassword"
              >
                忘记密码？
              </button>
            </div>

            <el-button
                type="primary"
                size="large"
                class="!w-full !h-11 !rounded-lg !bg-slate-900 !border-slate-900 hover:!bg-slate-800 transition-all duration-200 !text-sm !font-medium !mt-2"
                :loading="loading"
                @click="handleLogin"
            >
              {{ loginType === 'admin' ? '进入管理中心' : '登录' }}
            </el-button>
          </el-form>

          <div class="text-center text-sm text-slate-500 pt-2">
            还没有账号？
            <a href="#" class="font-semibold text-slate-900 hover:text-slate-700 transition-colors">
              联系管理员开通
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from "axios";

const loginFormRef = ref(null)
const loading = ref(false)
const loginType = ref('user') // 'user' 或 'admin'
const router = useRouter()

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    // 调用后端登录接口
    const response = await axios.post('http://localhost:8080/api/auth/login', {
      username: loginForm.username,
      password: loginForm.password
    })

    // 后端返回 Result<String>，token 在 data 中
    const { code, data: token, msg } = response.data

    if (code === 200 && token) {
      // 保存 token
      localStorage.setItem('token', token)

      // 根据登录类型跳转到不同页面
      if (loginType.value === 'admin') {
        ElMessage.success('管理中心登录成功，正在跳转...')
        await router.push('/admin')
      } else {
        ElMessage.success('用户端登录成功，正在跳转...')
        await router.push('/user')
      }
    } else {
      ElMessage.error(msg || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    const errorMsg = error.response?.data?.msg || error.message || '登录失败，请检查网络连接'
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}

const handleForgotPassword = () => {
  ElMessageBox.alert(
      '请联系您的企业管理员重置密码。\n\n管理员联系方式：\n• 内部办公系统：搜索"IT服务台"\n• 邮箱：admin@company.com\n• 分机：8888',
      '密码重置提示',
      {
        confirmButtonText: '我知道了',
        type: 'info',
        customClass: 'forgot-password-dialog'
      }
  )
}
</script>

<style scoped>
/* 覆盖 Element Plus 默认样式以匹配 shadcn 美学 */
:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e2e8f0 !important;
  border-radius: 0.5rem;
  background-color: white;
  padding: 0 12px;
  transition: all 0.2s;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #0f172a !important;
  border-color: #0f172a;
}

:deep(.el-input__inner) {
  color: #0f172a;
  font-size: 0.875rem;
  height: 2.75rem;
  font-weight: 400;
}

:deep(.el-input__inner::placeholder) {
  color: #94a3b8;
}

:deep(.el-form-item__error) {
  color: #ef4444;
  font-size: 0.75rem;
  margin-top: 0.25rem;
  padding-left: 0.25rem;
}

/* 忘记密码弹窗自定义样式 */
:global(.forgot-password-dialog .el-message-box__content) {
  white-space: pre-line;
  line-height: 1.6;
  color: #334155;
  font-size: 0.875rem;
}

:global(.forgot-password-dialog .el-message-box__title) {
  color: #0f172a;
  font-weight: 600;
}

:global(.forgot-password-dialog .el-button--primary) {
  background-color: #0f172a;
  border-color: #0f172a;
}

/* 动画效果 */
@keyframes pulse {
  0%, 100% {
    opacity: 0.4;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(1.05);
  }
}

.animate-pulse {
  animation: pulse 4s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.delay-1000 {
  animation-delay: 1s;
}
</style>