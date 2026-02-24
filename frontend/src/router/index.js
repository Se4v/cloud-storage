import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/login.vue'  // 确保路径正确
import DriveLayout from '@/components/DriveLayout.vue'
import PersonalDrive from "@/views/PersonalDrive.vue";

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: Login
    },
    {
        path: '/drive',
        name: 'r',
        component: DriveLayout,
        children: [
            {
                path: 'personal',
                name: 'PersonalDrive',
                component: PersonalDrive
            }
        ]
    },

]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router