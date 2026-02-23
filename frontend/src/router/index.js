import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/login.vue'  // 确保路径正确
import DriveLayout from '@/components/DriveLayout.vue'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: Login  // 或者使用 () => import('@/views/login.vue')
    },
    {
        path: '/DriveLayout',
        name: 'r',
        component: DriveLayout  // 或者使用 () => import('@/views/login.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router