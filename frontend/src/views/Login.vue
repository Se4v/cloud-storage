<template>
  <div class="login-container">
    <!-- 左侧品牌展示区 - 全新设计 -->
    <div class="brand-section">
      <div class="brand-content">
        <div class="logo-wrapper">
          <div class="logo-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
            </svg>
          </div>
          <h1 class="brand-title">SecureVault</h1>
        </div>
        <p class="brand-description">企业级智能云存储平台，让文件管理更安全、协作更高效</p>

        <div class="feature-list">
          <div class="feature-item">
            <div class="feature-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
            </div>
            <span>端到端加密存储</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
            </div>
            <span>千人级团队协作</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/>
                <polyline points="17 6 23 6 23 12"/>
              </svg>
            </div>
            <span>实时同步与备份</span>
          </div>
        </div>
      </div>

      <!-- 全新背景装饰：极光流动效果 + 浮动几何 -->
      <div class="aurora-bg">
        <div class="aurora aurora-1"></div>
        <div class="aurora aurora-2"></div>
        <div class="aurora aurora-3"></div>
      </div>
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>
      <div class="noise-overlay"></div>
    </div>

    <!-- 右侧登录表单区（保持不变） -->
    <div class="form-section">
      <div class="form-wrapper">
        <div class="form-header">
          <h2 class="form-title">欢迎回来</h2>
          <p class="form-subtitle">请输入您的账号密码以继续访问</p>
        </div>

        <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
                v-model="loginForm.username"
                placeholder="请输入企业邮箱或用户名"
                size="large"
                :prefix-icon="User"
                class="custom-input"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                class="custom-input"
            />
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="loginForm.remember" class="custom-checkbox">
              记住我
            </el-checkbox>
            <el-button
                link
                type="primary"
                class="forgot-link"
                @click="handleForgotPassword"
            >
              忘记密码？
            </el-button>
          </div>

          <el-form-item>
            <el-button
                type="primary"
                size="large"
                class="login-button"
                :loading="loading"
                @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="divider">
          <span class="divider-text">或使用以下方式登录</span>
        </div>

        <div class="social-login">
          <el-button class="social-btn" @click="handleSSO">
            <svg class="sso-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <line x1="3" y1="9" x2="21" y2="9"/>
              <line x1="9" y1="21" x2="9" y2="9"/>
            </svg>
            企业微信登录
          </el-button>
          <el-button class="social-btn" @click="handleSSO">
            <svg class="sso-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/>
            </svg>
            钉钉登录
          </el-button>
        </div>

        <div class="form-footer">
          <span class="footer-text">还没有账号？</span>
          <el-button link type="primary" class="register-link" @click="handleRegister">
            联系管理员开通
          </el-button>
        </div>
      </div>

      <div class="copyright">
        <p>© 2024 SecureVault Enterprise. 企业数据安全保障</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true

    setTimeout(() => {
      loading.value = false
      ElMessage.success('登录成功')
    }, 1500)
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleForgotPassword = () => {
  ElMessage.info('请联系IT管理员重置密码')
}

const handleRegister = () => {
  ElMessage.info('请联系管理员开通企业账号')
}

const handleSSO = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('SSO登录成功')
  }, 1000)
}
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  width: 100%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* 左侧品牌区域 - 全新 Aurora 极光背景 */
.brand-section {
  position: relative;
  display: none;
  flex: 1;
  flex-direction: column;
  justify-content: center;
  padding: 3rem;
  background: #0f172a;
  color: white;
  overflow: hidden;
}

@media (min-width: 1024px) {
  .brand-section {
    display: flex;
  }
}

/* Aurora 极光背景效果 */
.aurora-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.aurora {
  position: absolute;
  width: 200%;
  height: 200%;
  top: -50%;
  left: -50%;
  opacity: 0.4;
  filter: blur(80px);
  animation: aurora-flow 20s ease-in-out infinite;
}

.aurora-1 {
  background: radial-gradient(circle at 50% 50%, rgba(56, 189, 248, 0.4) 0%, transparent 50%);
  animation-delay: 0s;
}

.aurora-2 {
  background: radial-gradient(circle at 30% 70%, rgba(139, 92, 246, 0.3) 0%, transparent 50%);
  animation-delay: -7s;
}

.aurora-3 {
  background: radial-gradient(circle at 70% 30%, rgba(14, 165, 233, 0.3) 0%, transparent 50%);
  animation-delay: -14s;
}

@keyframes aurora-flow {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg) scale(1);
  }
  33% {
    transform: translate(5%, -5%) rotate(5deg) scale(1.1);
  }
  66% {
    transform: translate(-5%, 5%) rotate(-5deg) scale(0.9);
  }
}

/* 浮动几何形状 */
.floating-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.1);
}

.shape-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -100px;
  animation: float 25s infinite ease-in-out;
}

.shape-2 {
  width: 200px;
  height: 200px;
  bottom: 10%;
  left: -50px;
  animation: float 30s infinite ease-in-out reverse;
}

.shape-3 {
  width: 150px;
  height: 150px;
  top: 40%;
  right: 15%;
  animation: float 20s infinite ease-in-out;
  animation-delay: -10s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  50% {
    transform: translate(30px, -30px) rotate(10deg);
  }
}

/* 噪点纹理覆盖 */
.noise-overlay {
  position: absolute;
  inset: 0;
  opacity: 0.03;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E");
  pointer-events: none;
}

.brand-content {
  position: relative;
  z-index: 10;
  max-width: 480px;
  margin: 0 auto;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 3.5rem;
  height: 3.5rem;
  background: linear-gradient(135deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0.05) 100%);
  border-radius: 1rem;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.2);
  color: white;
  box-shadow: 0 8px 32px rgba(0,0,0,0.2);
  transform: rotate(-3deg);
  transition: transform 0.3s ease;
}

.logo-icon:hover {
  transform: rotate(0deg) scale(1.05);
}

.brand-title {
  font-size: 2.25rem;
  font-weight: 800;
  margin: 0;
  background: linear-gradient(to right, #fff, #cbd5e1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.02em;
}

.brand-description {
  font-size: 1.125rem;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.65);
  margin-bottom: 3rem;
  font-weight: 400;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 0.875rem;
  font-size: 0.9375rem;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 500;
  transition: transform 0.2s;
}

.feature-item:hover {
  transform: translateX(5px);
}

.feature-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.25rem;
  height: 2.25rem;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 0.625rem;
  color: #38bdf8;
  border: 1px solid rgba(255,255,255,0.1);
  transition: all 0.3s;
}

.feature-item:hover .feature-icon {
  background: rgba(56, 189, 248, 0.2);
  color: #7dd3fc;
  transform: scale(1.1);
}

/* 右侧表单区域（保持原样） */
.form-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 1.5rem;
  background: #fafafa;
  position: relative;
}

.form-wrapper {
  width: 100%;
  max-width: 400px;
  background: white;
  padding: 2.5rem;
  border-radius: 0.75rem;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
}

.form-header {
  text-align: center;
  margin-bottom: 2rem;
}

.form-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #0f172a;
  margin: 0 0 0.5rem 0;
  letter-spacing: -0.025em;
}

.form-subtitle {
  font-size: 0.875rem;
  color: #64748b;
  margin: 0;
}

.login-form {
  margin-top: 1.5rem;
}

:deep(.custom-input .el-input__wrapper) {
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  border-radius: 0.5rem;
  padding: 0.25rem 0.75rem;
  background: white;
  transition: all 0.2s;
}

:deep(.custom-input .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #cbd5e1 inset;
}

:deep(.custom-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #0f172a inset;
}

:deep(.custom-input .el-input__inner) {
  height: 2.5rem;
  font-size: 0.875rem;
  color: #0f172a;
}

:deep(.custom-input .el-input__icon) {
  color: #94a3b8;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

:deep(.custom-checkbox .el-checkbox__label) {
  font-size: 0.875rem;
  color: #475569;
}

:deep(.custom-checkbox .el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #0f172a;
  border-color: #0f172a;
}

.forgot-link {
  font-size: 0.875rem;
  color: #0f172a;
  font-weight: 500;
}

.forgot-link:hover {
  color: #334155;
}

.login-button {
  width: 100%;
  height: 2.75rem;
  font-size: 0.875rem;
  font-weight: 500;
  background: #0f172a;
  border: none;
  border-radius: 0.5rem;
  transition: all 0.2s;
}

.login-button:hover {
  background: #1e293b;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.15);
}

.divider {
  position: relative;
  margin: 1.5rem 0;
  text-align: center;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e2e8f0;
}

.divider-text {
  position: relative;
  display: inline-block;
  padding: 0 0.75rem;
  font-size: 0.75rem;
  color: #94a3b8;
  background: white;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.social-login {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.social-btn {
  height: 2.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
  background: white;
  color: #475569;
  font-size: 0.875rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.2s;
}

.social-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  color: #0f172a;
}

.sso-icon {
  width: 1rem;
  height: 1rem;
}

.form-footer {
  text-align: center;
  font-size: 0.875rem;
  color: #64748b;
}

.footer-text {
  margin-right: 0.25rem;
}

.register-link {
  font-weight: 500;
  color: #0f172a;
}

.copyright {
  position: absolute;
  bottom: 1.5rem;
  font-size: 0.75rem;
  color: #94a3b8;
  text-align: center;
}

@media (max-width: 640px) {
  .form-wrapper {
    padding: 1.5rem;
    border: none;
    box-shadow: none;
    background: transparent;
  }

  .form-section {
    background: white;
  }
}
</style>