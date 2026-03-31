import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUploadStore = defineStore('upload', () => {
  // 上传任务列表
  const uploadTasks = ref([])
  
  // 是否显示上传进度面板
  const isUploadPanelVisible = ref(false)
  
  // 获取活跃任务数量（上传中或等待中）
  const activeTaskCount = computed(() => {
    return uploadTasks.value.filter(task => 
      task.status === 'uploading' || task.status === 'waiting'
    ).length
  })
  
  // 获取已完成任务数量
  const completedTaskCount = computed(() => {
    return uploadTasks.value.filter(task => 
      task.status === 'completed'
    ).length
  })
  
  // 添加上传任务
  const addTask = (file) => {
    const taskId = `${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    const task = {
      id: taskId,
      name: file.name,
      size: file.size,
      progress: 0,
      status: 'waiting', // waiting, uploading, completed, error, paused
      speed: 0,
      loaded: 0,
      startTime: null,
      errorMessage: '',
      sha256: null, // 用于关联后端
      isSkip: false, // 是否秒传
      totalChunks: 0, // 总分片数（大文件）
      uploadedChunks: 0 // 已上传分片数
    }
    uploadTasks.value.push(task)
    return taskId
  }
  
  // 开始上传任务
  const startTask = (taskId, sha256 = null) => {
    const task = uploadTasks.value.find(t => t.id === taskId)
    if (task) {
      task.status = 'uploading'
      task.startTime = Date.now()
      if (sha256) {
        task.sha256 = sha256
      }
    }
  }
  
  // 更新上传进度（直传使用）
  const updateProgress = (taskId, loaded, total) => {
    const task = uploadTasks.value.find(t => t.id === taskId)
    if (task && task.status === 'uploading') {
      task.loaded = loaded
      task.progress = Math.round((loaded * 100) / total)
      
      // 计算速度
      if (task.startTime) {
        const elapsed = (Date.now() - task.startTime) / 1000
        if (elapsed > 0) {
          task.speed = Math.round(loaded / elapsed)
        }
      }
    }
  }
  
  // 更新分片进度（分片上传使用）
  const updateChunkProgress = (taskId, uploadedChunks, totalChunks) => {
    const task = uploadTasks.value.find(t => t.id === taskId)
    if (task && task.status === 'uploading') {
      task.uploadedChunks = uploadedChunks
      task.totalChunks = totalChunks
      task.progress = Math.round((uploadedChunks * 100) / totalChunks)
      
      // 估算已上传大小
      const avgChunkSize = task.size / totalChunks
      task.loaded = Math.round(uploadedChunks * avgChunkSize)
      
      // 计算速度
      if (task.startTime) {
        const elapsed = (Date.now() - task.startTime) / 1000
        if (elapsed > 0) {
          task.speed = Math.round(task.loaded / elapsed)
        }
      }
    }
  }
  
  // 标记任务为秒传成功
  const markAsSkipped = (taskId) => {
    const task = uploadTasks.value.find(t => t.id === taskId)
    if (task) {
      task.status = 'completed'
      task.progress = 100
      task.loaded = task.size
      task.isSkip = true
      task.speed = 0
    }
  }
  
  // 完成任务
  const completeTask = (taskId) => {
    const task = uploadTasks.value.find(t => t.id === taskId)
    if (task) {
      task.status = 'completed'
      task.progress = 100
      task.loaded = task.size
      task.speed = 0
    }
  }
  
  // 标记任务失败
  const failTask = (taskId, errorMessage) => {
    const task = uploadTasks.value.find(t => t.id === taskId)
    if (task) {
      task.status = 'error'
      task.errorMessage = errorMessage || '上传失败'
      task.speed = 0
    }
  }
  
  // 暂停任务
  const pauseTask = (taskId) => {
    const task = uploadTasks.value.find(t => t.id === taskId)
    if (task && task.status === 'uploading') {
      task.status = 'paused'
      task.speed = 0
    }
  }
  
  // 恢复任务
  const resumeTask = (taskId) => {
    const task = uploadTasks.value.find(t => t.id === taskId)
    if (task && task.status === 'paused') {
      task.status = 'uploading'
      task.startTime = Date.now() - (task.loaded / (task.speed || 1)) * 1000
    }
  }
  
  // 移除任务
  const removeTask = (taskId) => {
    const index = uploadTasks.value.findIndex(t => t.id === taskId)
    if (index > -1) {
      uploadTasks.value.splice(index, 1)
    }
  }
  
  // 清空已完成的任务
  const clearCompletedTasks = () => {
    uploadTasks.value = uploadTasks.value.filter(task => 
      task.status !== 'completed'
    )
  }
  
  // 显示上传面板
  const showUploadPanel = () => {
    isUploadPanelVisible.value = true
  }
  
  // 隐藏上传面板
  const hideUploadPanel = () => {
    isUploadPanelVisible.value = false
  }
  
  // 切换上传面板显示状态
  const toggleUploadPanel = () => {
    isUploadPanelVisible.value = !isUploadPanelVisible.value
  }
  
  // 格式化文件大小
  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
  }
  
  // 格式化速度
  const formatSpeed = (bytesPerSecond) => {
    if (bytesPerSecond === 0) return '0 B/s'
    return formatFileSize(bytesPerSecond) + '/s'
  }
  
  // 格式化剩余时间
  const formatRemainingTime = (task) => {
    if (task.status !== 'uploading' || task.speed === 0) return ''
    const remaining = task.size - task.loaded
    const seconds = Math.ceil(remaining / task.speed)
    if (seconds < 60) return `${seconds}秒`
    if (seconds < 3600) return `${Math.floor(seconds / 60)}分${seconds % 60}秒`
    return `${Math.floor(seconds / 3600)}时${Math.floor((seconds % 3600) / 60)}分`
  }

  return {
    uploadTasks,
    isUploadPanelVisible,
    activeTaskCount,
    completedTaskCount,
    addTask,
    startTask,
    updateProgress,
    updateChunkProgress,
    markAsSkipped,
    completeTask,
    failTask,
    pauseTask,
    resumeTask,
    removeTask,
    clearCompletedTasks,
    showUploadPanel,
    hideUploadPanel,
    toggleUploadPanel,
    formatFileSize,
    formatSpeed,
    formatRemainingTime
  }
})
