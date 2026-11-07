<template>
  <div>
    <div class="page-head">
      <div>
        <h2 class="page-title">菜品分类</h2>
        <p class="page-sub">维护小程序左侧分类导航，分类停用后将不会在前台展示</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增分类</el-button>
    </div>

    <div class="page-card" v-loading="loading">
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="name" label="分类名称" min-width="180">
          <template #default="{ row }">
            <div class="cat-name">
              <span class="cat-dot"></span>
              <b>{{ row.name }}</b>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="90" align="center" />
        <el-table-column label="菜品数量" width="110" align="center">
          <template #default="{ row }">
            <span class="count-badge">{{ row.dishCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="启用状态" width="120" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val) => toggleStatus(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !list.length" description="暂无分类，点击右上角新增" />
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="440px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="86px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：招牌热菜" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="展示排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" controls-position="right" />
          <span class="form-tip">数字越小越靠前</span>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { adminApi } from '../api'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  name: '',
  sort: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序值', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    list.value = await adminApi.categories()
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  form.id = row?.id || null
  form.name = row?.name || ''
  form.sort = row?.sort ?? 0
  form.status = row?.status ?? 1
  dialogVisible.value = true
}

const save = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  saving.value = true
  try {
    if (form.id) {
      await adminApi.updateCategory({ ...form })
      ElMessage.success('分类修改成功')
    } else {
      await adminApi.createCategory({ ...form })
      ElMessage.success('分类新增成功')
    }
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row, val) => {
  try {
    await adminApi.updateCategory({ ...row, status: val ? 1 : 0 })
    row.status = val ? 1 : 0
    ElMessage.success(val ? '分类已启用' : '分类已停用')
  } catch (e) {
    load()
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除分类「${row.name}」吗？分类下存在菜品时无法删除。`,
      '删除分类',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
    await adminApi.deleteCategory(row.id)
    ElMessage.success('分类已删除')
    load()
  } catch (e) {
    // 取消或接口异常
  }
}

onMounted(load)
</script>

<style scoped>
.cat-name {
  display: flex;
  align-items: center;
  gap: 9px;
}

.cat-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff9a6c, #ef5a24);
  flex-shrink: 0;
}

.count-badge {
  display: inline-block;
  min-width: 32px;
  padding: 2px 10px;
  border-radius: 20px;
  background: #fbf2e9;
  color: #b07a4d;
  font-size: 12px;
  font-weight: 600;
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #a08b7c;
}
</style>
