import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
    state: () => ({
        token: '',
        personalDriveId: null
    }),

    persist: {
        storage: localStorage,
        paths: ['token', 'personalDriveId']
    }
})