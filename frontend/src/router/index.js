import { createRouter, createWebHistory } from 'vue-router';
import Login from '@/views/login.vue';
import DriveLayout from '@/components/DriveLayout.vue';
import PersonalDrive from "@/views/PersonalDrive.vue";
import EnterpriseDrive from "@/views/EnterpriseDrive.vue";
import Links from "@/views/Links.vue";
import Profile from '@/views/Profile.vue';
import Recovery from "@/views/Recovery.vue";
import AdminLayout from "@/components/AdminLayout.vue";
import UserManage from "@/views/admin/UserManage.vue";
import PermissionManage from "@/views/admin/PermissionManage.vue";
import RoleManage from "@/views/admin/RoleManage.vue";
import OrgManage from "@/views/admin/OrgManage.vue";
import LogManage from "@/views/admin/LogManage.vue";
import SystemManage from "@/views/admin/SystemManage.vue";
import DriveStat from "@/views/admin/DriveStat.vue";
import TrafficStat from "@/views/admin/TrafficStat.vue";
import MemberManage from "@/views/admin/MemberManage.vue";
import Notice from "@/views/Notice.vue";
import SharedFile from "@/views/SharedFile.vue";

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: Login
    },
    {
        path: '/s/:linkKey?',
        name: 'shareFile',
        component: SharedFile
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
                path: 'notice',
                name: 'Notice',
                component: Notice
            },
            {
                path: 'profile',
                name: 'DriveProfile',
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
                path: 'space-stats',
                name: 'DriveStat',
                component: DriveStat
            },
            {
                path: 'traffic-stats',
                name: 'TrafficStat',
                component: TrafficStat
            },
            {
                path: 'profile',
                name: 'AdminProfile',
                component: Profile
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router