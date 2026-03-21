import { createRouter, createWebHistory } from 'vue-router';
import Login from '@/views/login.vue';
import DriveLayout from '@/components/DriveLayout.vue';
import PersonalDrive from "@/views/PersonalDrive.vue";
import EnterpriseDrive from "@/views/EnterpriseDrive.vue";
import Links from "@/views/Links.vue";
import Profile from '@/views/Profile.vue';
import Recovery from "@/views/Recovery.vue";
import AdminLayout from "@/components/AdminLayout.vue";
import AnnouncementManage from "@/views/admin/AnnouncementManage.vue";
import UserManage from "@/views/admin/UserManage.vue";
import PermissionManage from "@/views/admin/PermissionManage.vue";
import RoleManage from "@/views/admin/RoleManage.vue";
import OrgManage from "@/views/admin/OrgManage.vue";
import StorageManage from "@/views/admin/StorageManage.vue";
import LogManage from "@/views/admin/LogManage.vue";
import SystemManage from "@/views/admin/SystemManage.vue";
import UserStat from "@/views/admin/UserStat.vue";
import StorageStat from "@/views/admin/StorageStat.vue";
import TrafficStat from "@/views/admin/TrafficStat.vue";
import MemberManage from "@/views/admin/MemberManage.vue";

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
                path: 'personal/:driveId?',
                name: 'PersonalDrive',
                component: PersonalDrive
            },
            {
                path: 'enterprise/:driveId?',
                name: 'EnterpriseDrive',
                component: EnterpriseDrive
            },
            {
                path: 'links',
                name: 'Links',
                component: Links
            },
            {
                path: 'recovery',
                name: 'Recovery',
                component: Recovery
            },
            {
                path: 'profile',
                name: 'Profile',
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
                path: 'announcement',
                name: 'AnnouncementManage',
                component: AnnouncementManage
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
                path: 'member',
                name: 'MemberManage',
                component: MemberManage
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
            },
            {
                path: 'space-stats',
                name: 'StorageStat',
                component: StorageStat
            },
            {
                path: 'traffic-stats',
                name: 'TrafficStat',
                component: TrafficStat
            }
        ]
    }

]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router