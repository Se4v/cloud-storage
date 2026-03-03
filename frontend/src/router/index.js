import { createRouter, createWebHistory } from 'vue-router';
import Login from '@/views/login.vue';
import DriveLayout from '@/components/DriveLayout.vue';
import PersonalDrive from "@/views/PersonalDrive.vue";
import EnterpriseDrive from "@/views/EnterpriseDrive.vue";
import SafeExternalLinks from "@/views/SafeExternalLinks.vue";
import Profile from '@/views/Profile.vue';
import Recovery from "@/views/Recovery.vue";
import AdminLayout from "@/components/AdminLayout.vue";
import NoticeMange from "@/views/admin/NoticeMange.vue";
import UserMange from "@/views/admin/UserMange.vue";
import PermissionMange from "@/views/admin/PermissionMange.vue";
import RoleMange from "@/views/admin/RoleMange.vue";
import OrgMange from "@/views/admin/OrgMange.vue";

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
            {
                path: 'notice',
                name: 'NoticeMange',
                component: NoticeMange
            },
            {
                path: 'user',
                name: 'UserMange',
                component: UserMange
            },
            {
                path: 'permission',
                name: 'PermissionMange',
                component: PermissionMange
            },
            {
                path: 'role',
                name: 'RoleMange',
                component: RoleMange
            },
            {
                path: 'org',
                name: 'OrgMange',
                component: OrgMange
            }
        ]
    }

]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router