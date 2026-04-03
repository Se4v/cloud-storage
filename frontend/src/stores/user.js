import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
    state: () => ({
        token: '',
        personalDriveId: null,
        orgId: null
    }),

    persist: {
        storage: localStorage,
        paths: ['token', 'personalDriveId']
    }
})