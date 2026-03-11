import { createRouter, createWebHistory } from 'vue-router';
import Login from '@/views/login.vue';
import DriveLayout from '@/components/DriveLayout.vue';
import PersonalDrive from "@/views/PersonalDrive.vue";
import EnterpriseDrive from "@/views/EnterpriseDrive.vue";
import SafeExternalLinks from "@/views/SafeExternalLinks.vue";
import Profile from '@/views/Profile.vue';
import Recovery from "@/views/Recovery.vue";
import AdminLayout from "@/components/AdminLayout.vue";
import NoticeManage from "@/views/admin/NoticeManage.vue";
import UserManage from "@/views/admin/UserManage.vue";
import PermissionManage from "@/views/admin/PermissionManage.vue";
import RoleManage from "@/views/admin/RoleManage.vue";
import OrgManage from "@/views/admin/OrgManage.vue";
import StorageManage from "@/views/admin/StorageManage.vue";
import LogManage from "@/views/admin/LogManage.vue";
import SystemManage from "@/views/admin/SystemManage.vue";
import UserStat from "@/views/admin/UserStat.vue";

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
                name: 'NoticeManage',
                component: NoticeManage
            },
            {
                path: 'user',
                name: 'UserManage',
                component: UserManage
            },
            {
                path: 'permission',
                name: 'PermissionManage',
                component: PermissionManage
            },
            {
                path: 'role',
                name: 'RoleManage',
                component: RoleManage
            },
            {
                path: 'org',
                name: 'OrgManage',
                component: OrgManage
            },
            {
                path: 'storage',
                name: 'StorageManage',
                component: StorageManage
            },
            {
                path: 'log',
                name: 'LogManage',
                component: LogManage
            },
            {
                path: 'settings',
                name: 'SystemManage',
                component: SystemManage
            },
            {
                path: 'user-stats',
                name: 'UserStat',
                component: UserStat
            }
        ]
    }

]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router