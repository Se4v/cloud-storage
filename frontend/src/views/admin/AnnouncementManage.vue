<template>
  <div class="p-6">
    <!-- 控制栏：左侧操作按钮，右侧搜索 -->
    <div class="mb-6 flex flex-col lg:flex-row lg:items-center justify-between gap-4 bg-white rounded-xl border border-slate-200 shadow-sm p-4">
      <!-- 左侧：操作按钮 -->
      <div class="flex items-center gap-3">
        <button
            @click="handleCreate"
            class="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-all duration-200 shadow-sm hover:shadow-md active:scale-95"
        >
          <el-icon :size="16"><Plus /></el-icon>
          新建公告
        </button>

        <button
            @click="handleBatchDelete"
            :disabled="selectedAnnouncements.length === 0"
            class="inline-flex items-center gap-2 px-4 py-2 bg-white text-slate-700 border border-slate-200 text-sm font-medium rounded-lg hover:bg-slate-50 hover:border-slate-300 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <el-icon :size="16"><Delete /></el-icon>
          删除
        </button>
      </div>

      <!-- 右侧：搜索 -->
      <div class="flex items-center gap-3">
        <el-input
            v-model="searchQuery"
            placeholder="搜索公告标题或内容..."
            clearable
            class="w-64 !rounded-lg"
            @input="handleSearch"
            @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon class="text-slate-400"><Search /></el-icon>
          </template>
        </el-input>
        <button
          @click="handleSearch"
          class="inline-flex items-center justify-center gap-2 h-9 px-4 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors whitespace-nowrap"
        >
          <el-icon :size="16"><Search /></el-icon>
          <span>搜索</span>
        </button>
      </div>
    </div>

    <!-- 公告列表 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <el-table
          v-loading="loading"
          :data="filteredAnnouncementList"
          row-key="id"
          @selection-change="handleSelectionChange"
          class="announcement-table"
          header-cell-class-name="!bg-slate-50 !text-slate-700 !font-semibold !border-b !border-slate-200"
          :cell-style="{
          fontSize: '14px',
          color: '#334155',
          borderBottom: '1px solid #f1f5f9'
        }"
      >
        <el-table-column type="selection" width="48" align="center" />

        <el-table-column label="标题" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <span class="font-medium text-slate-900">{{ row.title }}</span>
              <el-tag
                  v-if="isExpired(row.expireTime)"
                  size="small"
                  type="info"
                  class="!rounded-md !bg-slate-100 !text-slate-500 !border-0"
              >
                已过期
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="内容" min-width="400" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="text-slate-600 line-clamp-1">{{ row.content }}</span>
          </template>
        </el-table-column>

        <el-table-column label="过期时间" width="220" align="center">
          <template #default="{ row }">
            <div class="flex flex-col items-center">
              <span class="text-sm text-slate-700">{{ row.expireTime }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center justify-center gap-1">
              <el-tooltip content="编辑信息" placement="top" :show-after="500">
                <button
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:text-blue-600 hover:bg-blue-50 transition-colors"
                    @click.stop="handleEdit(row)"
                >
                  <el-icon :size="16"><Edit /></el-icon>
                </button>
              </el-tooltip>

              <el-tooltip content="删除" placement="top" :show-after="500">
                <button
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:text-red-600 hover:bg-red-50 transition-colors"
                    @click.stop="handleDelete(row)"
                >
                  <el-icon :size="16"><Delete /></el-icon>
                </button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>

        <!-- 空状态 -->
        <template #empty>
          <div class="py-12 flex flex-col items-center justify-center text-center">
            <div class="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center mb-4">
              <el-icon :size="32" class="text-slate-400"><Bell /></el-icon>
            </div>
            <h3 class="text-sm font-medium text-slate-900 mb-1">暂无公告数据</h3>
            <p class="text-sm text-slate-500 mb-4">开始创建你的第一条公告</p>
            <button
                @click="handleCreate"
                class="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-all duration-200 shadow-sm hover:shadow-md active:scale-95"
            >
              <el-icon :size="16"><Plus /></el-icon>
              新建公告
            </button>
          </div>
        </template>
      </el-table>

      <!-- 分页 -->
      <div class="px-6 py-4 border-t border-slate-200 bg-slate-50/50 flex items-center justify-between">
        <span class="text-sm text-slate-500">
          共 <span class="font-medium text-slate-900">{{ total }}</span> 条记录
        </span>
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="prev, pager, next, sizes"
            background
            class="!gap-2"
        />
      </div>
    </div>

    <!-- 新建/编辑对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑公告' : '新建公告'"
        width="560px"
        destroy-on-close
        class="!rounded-xl"
        :close-on-click-modal="false"
    >
      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="mt-2"
      >
        <el-form-item label="公告标题" prop="title">
          <el-input
              v-model="form.title"
              placeholder="请输入公告标题"
              class="!h-10 !rounded-lg"
              maxlength="100"
              show-word-limit
          />
        </el-form-item>

        <el-form-item label="公告内容" prop="content">
          <el-input
              v-model="form.content"
              type="textarea"
              :rows="5"
              placeholder="请输入公告内容，支持 Markdown 格式"
              class="!rounded-lg"
              maxlength="2000"
              show-word-limit
          />
        </el-form-item>

        <el-form-item label="过期时间" prop="expireTime">
          <el-date-picker
              v-model="form.expireTime"
              type="datetime"
              placeholder="选择公告过期时间"
              class="!w-full !h-10"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <button
              @click="dialogVisible = false"
              class="px-4 py-2 text-sm font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors duration-200"
          >
            取消
          </button>
          <button
              @click="handleSubmit"
              class="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-all duration-200 shadow-sm hover:shadow-md active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ isEdit ? '保存修改' : '立即创建' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { Plus, Delete, Edit, Bell, Search } from '@element-plus/icons-vue'

const API_BASE_URL = 'http://localhost:8080'

// 获取认证配置
const getAuthConfig = () => {
  const token = localStorage.getItem('token')
  return {
    headers: {
      'Authorization': token ? `Bearer ${token}` : '',
      'Content-Type': 'application/json'
    }
  }
}

const loading = ref(false)
const announcementList = ref([])
const selectedAnnouncements = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const searchQuery = ref('')

// 过滤后的公告列表
const filteredAnnouncementList = computed(() => {
  if (!searchQuery.value) return announcementList.value
  const query = searchQuery.value.toLowerCase()
  return announcementList.value.filter(item =>
      item.title.toLowerCase().includes(query) ||
      item.content.toLowerCase().includes(query)
  )
})

const handleSearch = () => {
  currentPage.value = 1
}

const form = reactive({
  id: null,
  title: '',
  content: '',
  expireTime: ''
})

const rules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度应在 2-100 个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
    { min: 10, max: 2000, message: '内容长度应在 10-2000 个字符之间', trigger: 'blur' }
  ],
  expireTime: [
    { required: true, message: '请选择过期时间', trigger: 'change' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await axios.get(`${API_BASE_URL}/api/announcement/all`, getAuthConfig())
    if (res.data.code === 200) {
      announcementList.value = res.data.data || []
      total.value = announcementList.value.length
    } else {
      ElMessage.error(res.data.msg || '加载数据失败')
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error(error.response?.data?.msg || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const isExpired = (expireTime) => {
  return new Date(expireTime) < new Date()
}

const handleSelectionChange = (selection) => {
  selectedAnnouncements.value = selection
}

const handleCreate = () => {
  isEdit.value = false
  form.id = null
  form.title = ''
  form.content = ''
  form.expireTime = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.title = row.title
  form.content = row.content
  form.expireTime = row.expireTime
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定要删除公告 "${row.title}" 吗？此操作不可恢复。`,
        '确认删除',
        {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'warning',
          confirmButtonClass: '!bg-red-600 !border-red-600 !rounded-lg',
          cancelButtonClass: '!rounded-lg',
          customClass: '!rounded-xl'
        }
    )
    const res = await axios.post(`${API_BASE_URL}/api/announcement/delete`, { announcementIds: [row.id] }, getAuthConfig())
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      await loadData()
    } else {
      ElMessage.error(res.data.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.msg || '删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedAnnouncements.value.length === 0) {
    ElMessage.warning('请先选择要删除的公告')
    return
  }
  try {
    await ElMessageBox.confirm(
        `确定要删除选中的 ${selectedAnnouncements.value.length} 条公告吗？此操作不可恢复。`,
        '确认批量删除',
        {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'warning',
          confirmButtonClass: '!bg-red-600 !border-red-600 !rounded-lg',
          cancelButtonClass: '!rounded-lg',
          customClass: '!rounded-xl'
        }
    )
    const ids = selectedAnnouncements.value.map(item => item.id)
    const res = await axios.post(`${API_BASE_URL}/api/announcement/delete`, { ids }, getAuthConfig())
    if (res.data.code === 200) {
      ElMessage.success('批量删除成功')
      selectedAnnouncements.value = []
      await loadData()
    } else {
      ElMessage.error(res.data.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.msg || '删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          const submitData = {
            id: form.id,
            title: form.title,
            content: form.content,
            expireTime: form.expireTime
          }
          const res = await axios.post(`${API_BASE_URL}/api/announcement/update`, submitData, getAuthConfig())
          if (res.data.code === 200) {
            ElMessage.success('修改成功')
            dialogVisible.value = false
            await loadData()
          } else {
            ElMessage.error(res.data.msg || '修改失败')
          }
        } else {
          const submitData = {
            title: form.title,
            content: form.content,
            expireTime: form.expireTime
          }
          const res = await axios.post(`${API_BASE_URL}/api/announcement/create`, submitData, getAuthConfig())
          if (res.data.code === 200) {
            ElMessage.success('创建成功')
            dialogVisible.value = false
            await loadData()
          } else {
            ElMessage.error(res.data.msg || '创建失败')
          }
        }
      } catch (error) {
        console.error(isEdit.value ? '修改失败:' : '创建失败:', error)
        ElMessage.error(error.response?.data?.msg || (isEdit.value ? '修改失败' : '创建失败'))
      }
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
/* 表格行高 */
:deep(.el-table__row) {
  height: 64px;
}

/* 对话框样式 */
:deep(.el-dialog__header) {
  margin-right: 0;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}

:deep(.el-dialog__body) {
  padding: 20px 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #e2e8f0;
}

:deep(.el-dialog__title) {
  font-weight: 600;
  color: #0f172a;
}

/* 表单项标签 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #374151;
  padding-bottom: 4px;
}

/* 分页器样式 */
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #2563eb;
  border-radius: 6px;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
  color: #2563eb;
}
</style>