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
      <div class="w-full max-w-sm space-y-6">
        <!-- 移动端 Logo -->
        <div class="lg:hidden flex items-center justify-center gap-2 mb-8">
          <div class="w-8 h-8 bg-slate-900 rounded-lg flex items-center justify-center">
            <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 15a4 4 0 004 4h9a5 5 0 10-.1-9.999 5.002 5.002 0 10-9.78 2.096A4.001 4.001 0 003 15z" />
            </svg>
          </div>
          <span class="text-xl font-bold text-slate-900 tracking-tight">Enterprise Cloud</span>
        </div>

        <div class="space-y-2 text-center lg:text-left">
          <h2 class="text-2xl font-semibold tracking-tight text-slate-900">欢迎回来</h2>
          <p class="text-sm text-slate-500">
            请输入您的企业账号以继续访问
          </p>
        </div>

        <!-- 登录表单 -->
        <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="space-y-4"
            @keyup.enter="handleLogin"
        >
          <el-form-item prop="email" class="!mb-4">
            <el-input
                v-model="loginForm.email"
                placeholder="name@company.com"
                size="large"
                class="!h-11 !rounded-lg"
            >
              <template #prefix>
                <svg class="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" />
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password" class="!mb-4">
            <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="••••••••"
                size="large"
                show-password
                class="!h-11 !rounded-lg"
            >
              <template #prefix>
                <svg class="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <div class="flex items-center justify-between">
            <el-checkbox v-model="loginForm.remember" class="!text-sm !text-slate-600">
              记住我
            </el-checkbox>
            <a href="#" class="text-sm font-medium text-slate-900 hover:text-slate-700 transition-colors">
              忘记密码？
            </a>
          </div>

          <el-button
              type="primary"
              size="large"
              class="!w-full !h-11 !rounded-lg !bg-slate-900 !border-slate-900 hover:!bg-slate-800 transition-all duration-200 !text-sm !font-medium"
              :loading="loading"
              @click="handleLogin"
          >
            登录
          </el-button>
        </el-form>

        <!-- 分隔线 -->
        <div class="relative">
          <div class="absolute inset-0 flex items-center">
            <div class="w-full border-t border-slate-200" />
          </div>
          <div class="relative flex justify-center text-xs uppercase">
            <span class="bg-slate-50 px-2 text-slate-500 font-medium">或通过以下方式登录</span>
          </div>
        </div>

        <!-- SSO 按钮 -->
        <div class="grid grid-cols-2 gap-3">
          <button
              type="button"
              class="flex items-center justify-center gap-2 h-10 px-4 border border-slate-200 rounded-lg bg-white text-sm font-medium text-slate-700 hover:bg-slate-50 hover:border-slate-300 transition-all duration-200"
          >
            <svg class="w-4 h-4" viewBox="0 0 24 24">
              <path fill="currentColor" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path fill="currentColor" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="currentColor" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
              <path fill="currentColor" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
            </svg>
            Google
          </button>
          <button
              type="button"
              class="flex items-center justify-center gap-2 h-10 px-4 border border-slate-200 rounded-lg bg-white text-sm font-medium text-slate-700 hover:bg-slate-50 hover:border-slate-300 transition-all duration-200"
          >
            <svg class="w-4 h-4" viewBox="0 0 24 24">
              <path fill="currentColor" d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm3.5-9c.83 0 1.5-.67 1.5-1.5S16.33 8 15.5 8 14 8.67 14 9.5s.67 1.5 1.5 1.5zm-7 0c.83 0 1.5-.67 1.5-1.5S9.33 8 8.5 8 7 8.67 7 9.5 7.67 11 8.5 11zm3.5 6.5c2.33 0 4.31-1.46 5.11-3.5H6.89c.8 2.04 2.78 3.5 5.11 3.5z"/>
            </svg>
            Microsoft
          </button>
        </div>

        <div class="text-center text-sm text-slate-500">
          还没有账号？
          <a href="#" class="font-semibold text-slate-900 hover:text-slate-700 transition-colors">
            联系管理员开通
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  email: '',
  password: '',
  remember: false
})

const loginRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate((valid) => {
      if (valid) {
        loading.value = true
        // 模拟登录请求
        setTimeout(() => {
          loading.value = false
          ElMessage.success('登录成功，正在跳转...')
          // 这里添加实际的路由跳转逻辑
        }, 1500)
      }
    })
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}
</script>

<style scoped>
/* 覆盖 Element Plus 默认样式以匹配 shadcn 美学 */
:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e2e8f0 !important;
  border-radius: 0.5rem;
  background-color: white;
  padding: 0 12px;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #0f172a !important;
}

:deep(.el-input__inner) {
  color: #0f172a;
  font-size: 0.875rem;
  height: 2.75rem;
}

:deep(.el-input__inner::placeholder) {
  color: #94a3b8;
}

:deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #0f172a;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #0f172a;
  border-color: #0f172a;
}

:deep(.el-form-item__error) {
  color: #ef4444;
  font-size: 0.75rem;
  margin-top: 0.25rem;
  padding-left: 0.25rem;
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