import { createRouter, createWebHistory } from 'vue-router';

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue')
    },
    {
        path: '/drive',
        name: 'UserLayout',
        component: () => import('@/components/UserLayout.vue'),
        children: [
            {
                path: 'personal/:driveId?',
                name: 'PersonalDrive',
                component: () => import('@/views/PersonalDrive.vue')
            },
            {
                path: 'enterprise/:driveId?',
                name: 'EnterpriseDrive',
                component: () => import('@/views/EnterpriseDrive.vue')
            },
            {
                path: 'share',
                name: 'ShareManage',
                component: () => import('@/views/ShareManage.vue')
            },
            {
                path: 'recycle',
                name: 'Recycle',
                component: () => import('@/views/Recycle.vue')
            },
            {
                path: 'notice',
                name: 'Notice',
                component: () => import('@/views/Notice.vue')
            },
            {
                path: 'profile',
                name: 'UserProfile',
                component: () => import('@/views/Profile.vue')
            }
        ]
    },
    {
        path: '/admin',
        name: 'AdminLayout',
        component: () => import('@/components/AdminLayout.vue'),
        children: [
            {
                path: 'user',
                name: 'UserManagement',
                component: () => import('@/views/admin/UserManage.vue')
            },
            {
                path: 'permission',
                name: 'PermissionManagement',
                component: () => import('@/views/admin/PermissionManage.vue')
            },
            {
                path: 'role',
                name: 'RoleManagement',
                component: () => import('@/views/admin/RoleManage.vue')
            },
            {
                path: 'org',
                name: 'OrgManagement',
                component: () => import('@/views/admin/OrgManage.vue')
            },
            {
                path: 'member',
                name: 'MemberManagement',
                component: () => import('@/views/admin/MemberManage.vue')
            },
            {
                path: 'log',
                name: 'LogManagement',
                component: () => import('@/views/admin/LogManage.vue')
            },
            {
                path: 'system',
                name: 'SystemManagement',
                component: () => import('@/views/admin/SystemManage.vue')
            },
            {
                path: 'drive-stats',
                name: 'DriveStat',
                component: () => import('@/views/admin/DriveStat.vue')
            },
            {
                path: 'traffic-stats',
                name: 'TrafficStat',
                component: () => import('@/views/admin/TrafficStat.vue')
            },
            {
                path: 'profile',
                name: 'AdminProfile',
                component: () => import('@/views/Profile.vue')
            }
        ]
    },
    {
        path: '/s/:linkKey',
        name: 'SharedFile',
        component: () => import('@/views/SharedFile.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router