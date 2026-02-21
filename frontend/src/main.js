import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import router from './router'  // 添加这行
import 'element-plus/dist/index.css'
import './assets/tailwind.css'

const app = createApp(App)
app.use(router)              // 添加这行
app.use(ElementPlus)
app.mount('#app')
