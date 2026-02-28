import { createRouter, createWebHistory } from 'vue-router';
import Login from '@/views/login.vue';
import DriveLayout from '@/components/DriveLayout.vue';
import PersonalDrive from "@/views/PersonalDrive.vue";
import EnterpriseDrive from "@/views/EnterpriseDrive.vue";
import SafeExternalLinks from "@/views/SafeExternalLinks.vue";
import Profile from '@/views/Profile.vue';
import Recovery from "@/views/Recovery.vue";
import AdminLayout from "@/components/AdminLayout.vue";

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: Login
    },
    {
        path: '/drive',
        name: 'drive',
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
            },
            {
                path: 'recovery',
                name: 'recovery',
                component: Recovery
            },
            {
                path: 'profile',
                name: 'profile',
                component: Profile
            }
        ]
    },
    {
        path: '/admin',
        name: 'admin',
        component: AdminLayout,
        children: [

        ]
    }

]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router