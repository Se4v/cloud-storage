<template>
  <div class="h-full flex flex-col p-6 bg-slate-50/50">
    <!-- 顶部操作栏 -->
    <div class="mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div class="flex items-center gap-3">
        <el-button
            type="primary"
            class="!bg-slate-900 !border-slate-900 !text-white hover:!bg-slate-800 shadow-sm"
            @click="handleCreate"
        >
          <el-icon class="mr-1"><Plus /></el-icon>
          新建节点
        </el-button>
        <el-button
            :disabled="!selectedRows.length"
            class="!border-slate-200 !text-slate-700 hover:!bg-slate-50 disabled:!opacity-50"
            @click="handleBatchDelete"
        >
          <el-icon class="mr-1"><Delete /></el-icon>
          批量删除
          <span v-if="selectedRows.length" class="ml-1 text-xs bg-slate-100 text-slate-600 px-2 py-0.5 rounded-full">
            {{ selectedRows.length }}
          </span>
        </el-button>
      </div>

      <div class="flex items-center gap-3">
        <el-input
            v-model="searchQuery"
            placeholder="搜索组织名称..."
            clearable
            class="w-64 !rounded-lg"
            @input="handleSearch"
        >
          <template #prefix>
            <el-icon class="text-slate-400"><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="flex-1 flex gap-6 min-h-0">
      <!-- 左侧：组织列表 -->
      <div class="flex-1 bg-white rounded-xl border border-slate-200 shadow-sm flex flex-col overflow-hidden">
        <el-table
            v-loading="loading"
            :data="filteredList"
            row-key="id"
            @selection-change="handleSelectionChange"
            class="flex-1"
            header-cell-class-name="!bg-slate-50 !text-slate-700 !font-semibold !border-b !border-slate-200"
            cell-class-name="!border-b !border-slate-100"
        >
          <el-table-column type="selection" width="50" align="center" />

          <el-table-column label="组织名称" min-width="200">
            <template #default="{ row }">
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-lg bg-slate-100 flex items-center justify-center text-slate-600">
                  <el-icon :size="16"><OfficeBuilding v-if="row.type === 'company'" /><House v-if="row.type === 'dept'" /><UserFilled v-if="row.type === 'group'" /></el-icon>
                </div>
                <span class="font-medium text-slate-900">{{ row.name }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="类型" width="120">
            <template #default="{ row }">
              <el-tag
                  :type="getTypeType(row.type)"
                  effect="light"
                  class="!rounded-md !border-0 !font-medium"
                  :class="getTypeClass(row.type)"
              >
                {{ getTypeLabel(row.type) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="父节点" min-width="180">
            <template #default="{ row }">
              <div class="flex items-center gap-2 text-slate-600">
                <el-icon class="text-slate-400"><Link /></el-icon>
                <span>{{ row.parentName || '根节点' }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="创建时间" width="180">
            <template #default="{ row }">
              <span class="text-slate-500 text-sm">{{ row.createTime }}</span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="flex items-center gap-2">
                <el-button
                    link
                    type="primary"
                    class="!text-slate-600 hover:!text-blue-600"
                    @click="handleEdit(row)"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-popconfirm
                    title="确认删除该组织节点？"
                    confirm-button-text="确认"
                    cancel-button-text="取消"
                    confirm-button-class="!bg-red-600 !border-red-600"
                    @confirm="handleDelete(row)"
                >
                  <template #reference>
                    <el-button
                        link
                        type="danger"
                        class="!text-slate-600 hover:!text-red-600"
                    >
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="px-4 py-3 border-t border-slate-200 flex justify-end">
          <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50]"
              :total="total"
              layout="total, sizes, prev, pager, next"
              class="!font-sans"
          />
        </div>
      </div>

      <!-- 右侧：组织架构树 -->
      <div class="w-80 bg-white rounded-xl border border-slate-200 shadow-sm flex flex-col overflow-hidden flex-shrink-0">
        <div class="px-4 py-3 border-b border-slate-200 flex items-center justify-between bg-slate-50/50">
          <span class="font-semibold text-slate-800">组织架构树</span>
          <el-button link type="primary" class="!text-xs" @click="expandAll">
            {{ isExpandAll ? '收起全部' : '展开全部' }}
          </el-button>
        </div>

        <div class="flex-1 overflow-auto p-2 custom-scrollbar">
          <el-tree
              ref="treeRef"
              :data="treeData"
              node-key="id"
              :props="defaultProps"
              :expand-on-click-node="false"
              :default-expanded-keys="expandedKeys"
              class="!bg-transparent"
          >
            <template #default="{ node, data }">
              <div
                  class="flex items-center gap-2 py-1 px-2 rounded-md hover:bg-slate-100 cursor-pointer transition-colors group w-full"
                  @click="handleTreeNodeClick(data)"
              >
                <el-icon class="text-slate-400 group-hover:text-slate-600">
                  <FolderOpened v-if="node.expanded" />
                  <Folder v-else />
                </el-icon>
                <span class="text-sm text-slate-700 font-medium truncate">{{ node.label }}</span>
                <span class="text-xs text-slate-400 ml-auto">{{ data.children?.length || 0 }}</span>
              </div>
            </template>
          </el-tree>
        </div>
      </div>
    </div>

    <!-- 新建/编辑对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑组织节点' : '新建组织节点'"
        width="500px"
        class="!rounded-xl"
        :close-on-click-modal="false"
    >
      <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="80px"
          class="mt-4"
      >
        <el-form-item label="组织名称" prop="name">
          <el-input
              v-model="formData.name"
              placeholder="请输入组织名称"
              class="!rounded-lg"
          />
        </el-form-item>

        <el-form-item label="组织类型" prop="type">
          <el-select
              v-model="formData.type"
              placeholder="请选择类型"
              class="w-full !rounded-lg"
          >
            <el-option label="公司" value="company" />
            <el-option label="部门" value="dept" />
            <el-option label="小组" value="group" />
          </el-select>
        </el-form-item>

        <el-form-item label="父节点" prop="parentId">
          <el-tree-select
              v-model="formData.parentId"
              :data="treeData"
              :props="defaultProps"
              check-strictly
              :render-after-expand="false"
              placeholder="请选择父节点（不选则为根节点）"
              class="w-full !rounded-lg"
              clearable
          />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number
              v-model="formData.sort"
              :min="0"
              class="!rounded-lg"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false" class="!rounded-lg">取消</el-button>
          <el-button
              type="primary"
              @click="handleSubmit"
              class="!bg-slate-900 !border-slate-900 !text-white hover:!bg-slate-800 !rounded-lg"
          >
            确认
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Delete,
  Search,
  Edit,
  OfficeBuilding,
  House,
  UserFilled,
  Link,
  Folder,
  FolderOpened
} from '@element-plus/icons-vue'

// 数据加载状态
const loading = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref([])
const treeRef = ref(null)
const isExpandAll = ref(false)
const expandedKeys = ref([])

// 对话框状态
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: null,
  name: '',
  type: 'dept',
  parentId: null,
  sort: 0
})

const formRules = {
  name: [{ required: true, message: '请输入组织名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择组织类型', trigger: 'change' }]
}

const defaultProps = {
  children: 'children',
  label: 'name',
  value: 'id'
}

// 模拟数据
const orgList = ref([
  { id: 1, name: '总经办', type: 'company', parentId: null, parentName: null, createTime: '2024-01-15 10:00:00', sort: 1 },
  { id: 2, name: '技术研发中心', type: 'dept', parentId: 1, parentName: '总经办', createTime: '2024-01-15 10:30:00', sort: 2 },
  { id: 3, name: '前端开发部', type: 'group', parentId: 2, parentName: '技术研发中心', createTime: '2024-01-16 09:00:00', sort: 1 },
  { id: 4, name: '后端开发部', type: 'group', parentId: 2, parentName: '技术研发中心', createTime: '2024-01-16 09:30:00', sort: 2 },
  { id: 5, name: '产品设计部', type: 'dept', parentId: 1, parentName: '总经办', createTime: '2024-01-17 14:00:00', sort: 3 },
  { id: 6, name: 'UI设计组', type: 'group', parentId: 5, parentName: '产品设计部', createTime: '2024-01-18 10:00:00', sort: 1 },
  { id: 7, name: '用户体验组', type: 'group', parentId: 5, parentName: '产品设计部', createTime: '2024-01-18 11:00:00', sort: 2 },
  { id: 8, name: '市场运营部', type: 'dept', parentId: 1, parentName: '总经办', createTime: '2024-01-20 09:00:00', sort: 4 },
])

const treeData = ref([
  {
    id: 1,
    name: '总经办',
    children: [
      {
        id: 2,
        name: '技术研发中心',
        children: [
          { id: 3, name: '前端开发部' },
          { id: 4, name: '后端开发部' }
        ]
      },
      {
        id: 5,
        name: '产品设计部',
        children: [
          { id: 6, name: 'UI设计组' },
          { id: 7, name: '用户体验组' }
        ]
      },
      {
        id: 8,
        name: '市场运营部'
      }
    ]
  }
])

// 过滤后的列表
const filteredList = computed(() => {
  if (!searchQuery.value) return orgList.value
  const query = searchQuery.value.toLowerCase()
  return orgList.value.filter(item =>
      item.name.toLowerCase().includes(query) ||
      item.parentName?.toLowerCase().includes(query)
  )
})

// 类型标签映射
const getTypeLabel = (type) => {
  const map = { company: '公司', dept: '部门', group: '小组' }
  return map[type] || type
}

const getTypeType = (type) => {
  const map = { company: 'primary', dept: 'success', group: 'info' }
  return map[type] || ''
}

const getTypeClass = (type) => {
  const map = {
    company: '!bg-blue-50 !text-blue-700',
    dept: '!bg-emerald-50 !text-emerald-700',
    group: '!bg-slate-100 !text-slate-700'
  }
  return map[type] || ''
}

// 表格选择
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
}

// 树操作
const expandAll = () => {
  isExpandAll.value = !isExpandAll.value
  if (isExpandAll.value) {
    expandedKeys.value = getAllIds(treeData.value)
  } else {
    expandedKeys.value = []
  }
}

const getAllIds = (data) => {
  const ids = []
  const traverse = (list) => {
    list.forEach(item => {
      ids.push(item.id)
      if (item.children?.length) traverse(item.children)
    })
  }
  traverse(data)
  return ids
}

const handleTreeNodeClick = (data) => {
  // 点击树节点筛选左侧列表
  searchQuery.value = data.name
  handleSearch()
}

// 增删改查操作
const handleCreate = () => {
  isEdit.value = false
  Object.assign(formData, {
    id: null,
    name: '',
    type: 'dept',
    parentId: null,
    sort: 0
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  const index = orgList.value.findIndex(item => item.id === row.id)
  if (index > -1) {
    orgList.value.splice(index, 1)
    ElMessage.success('删除成功')
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
        `确定要删除选中的 ${selectedRows.value.length} 个组织节点吗？`,
        '批量删除',
        {
          confirmButtonText: '确认删除',
          cancelButtonText: '取消',
          type: 'warning',
          confirmButtonClass: '!bg-red-600 !border-red-600'
        }
    )
    const ids = selectedRows.value.map(row => row.id)
    orgList.value = orgList.value.filter(item => !ids.includes(item.id))
    selectedRows.value = []
    ElMessage.success('批量删除成功')
  } catch {
    // 取消删除
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (valid) {
      if (isEdit.value) {
        const index = orgList.value.findIndex(item => item.id === formData.id)
        if (index > -1) {
          const parent = orgList.value.find(item => item.id === formData.parentId)
          orgList.value[index] = {
            ...orgList.value[index],
            ...formData,
            parentName: parent?.name || null
          }
          ElMessage.success('更新成功')
        }
      } else {
        const parent = orgList.value.find(item => item.id === formData.parentId)
        const newId = Math.max(...orgList.value.map(item => item.id)) + 1
        orgList.value.push({
          ...formData,
          id: newId,
          parentName: parent?.name || null,
          createTime: new Date().toLocaleString()
        })
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
    }
  })
}

// 初始化加载数据
const loadData = () => {
  loading.value = true
  setTimeout(() => {
    total.value = orgList.value.length
    loading.value = false
  }, 500)
}

loadData()
</script>

<style scoped>
/* 自定义滚动条 */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* Element Plus 样式覆盖，贴近 shadcn 风格 */
:deep(.el-button--primary) {
  --el-button-bg-color: #0f172a;
  --el-button-border-color: #0f172a;
  --el-button-hover-bg-color: #1e293b;
  --el-button-hover-border-color: #1e293b;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  border-radius: 0.5rem;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #0f172a inset;
}

:deep(.el-dialog) {
  border-radius: 0.75rem;
  box-shadow: 0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1);
}

:deep(.el-tag) {
  border-radius: 0.375rem;
  padding: 0 0.75rem;
  height: 1.5rem;
}

:deep(.el-tree-node__content) {
  border-radius: 0.375rem;
  margin: 2px 0;
}

:deep(.el-table) {
  --el-table-header-bg-color: #f8fafc;
  --el-table-row-hover-bg-color: #f8fafc;
  --el-table-border-color: #e2e8f0;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #0f172a;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
  color: #0f172a;
}
</style>