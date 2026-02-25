import { createRouter, createWebHistory } from 'vue-router';
import Login from '@/views/login.vue';
import DriveLayout from '@/components/DriveLayout.vue';
import PersonalDrive from "@/views/PersonalDrive.vue";
import EnterpriseDrive from "@/views/EnterpriseDrive.vue";
import SafeExternalLinks from "@/views/SafeExternalLinks.vue";

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
            },
            {
                path: 'enterprise',
                name: 'EnterpriseDrive',
                component: EnterpriseDrive
            },
            {
                path: 'links',
                name: 'SafeExternalLinks',
                component: SafeExternalLinks
            }
        ]
    },

]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router