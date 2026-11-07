<template>
  <div>
    <div class="page-head">
      <div>
        <h2 class="page-title">餐桌管理</h2>
        <p class="page-sub">维护门店桌号与座位数，顾客下单时可直接选择对应桌号</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增餐桌</el-button>
    </div>

    <div class="page-card">
      <div class="table-overview">
        <div class="overview-item">
          <span class="overview-dot free"></span>
          <div><b>{{ freeCount }}</b><small>空闲餐桌</small></div>
        </div>
        <div class="overview-item">
          <span class="overview-dot busy"></span>
          <div><b>{{ busyCount }}</b><small>使用中</small></div>
        </div>
        <div class="overview-item">
          <span class="overview-dot total"></span>
          <div><b>{{ list.length }}</b><small>餐桌总数</small></div>
        </div>
      </div>

      <div v-loading="loading" class="table-grid">
        <div v-for="row in list" :key="row.id" class="table-card" :class="{ busy: row.status === 1 }">
          <div class="table-top">
            <div class="table-number">{{ row.tableNo }}</div>
            <el-tag :type="row.status === 1 ? 'danger' : 'success'" effect="light" size="small">
              {{ row.status === 1 ? '使用中' : '空闲' }}
            </el-tag>
          </div>
          <div class="table-seats">
            <el-icon><User /></el-icon>
            <span>{{ row.seats }} 人座</span>
          </div>
          <div class="table-foot">
            <el-switch
              :model-value="row.status === 1"
              active-text="占用"
              inactive-text="空闲"
              @change="(val) => toggleStatus(row, val)"
            />
            <div class="table-actions">
              <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && !list.length" description="暂无餐桌，点击右上角新增" />
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑餐桌' : '新增餐桌'" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="84px">
        <el-form-item label="桌号" prop="tableNo">
          <el-input v-model="form.tableNo" placeholder="例如 A01 / B06" maxlength="20" />
        </el-form-item>
        <el-form-item label="座位数" prop="seats">
          <el-input-number v-model="form.seats" :min="1" :max="30" controls-position="right" />
          <span class="form-tip">用于前台展示餐桌容量</span>
        </el-form-item>
        <el-form-item label="使用状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">空闲</el-radio>
            <el-radio :value="1">使用中</el-radio>
          </el-radio-group>
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
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { adminApi } from '../api'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  tableNo: '',
  seats: 4,
  status: 0
})

const rules = {
  tableNo: [{ required: true, message: '请输入桌号', trigger: 'blur' }],
  seats: [{ required: true, message: '请输入座位数', trigger: 'blur' }]
}

const freeCount = computed(() => list.value.filter((i) => i.status === 0).length)
const busyCount = computed(() => list.value.filter((i) => i.status === 1).length)

const load = async () => {
  loading.value = true
  try {
    list.value = await adminApi.tables()
  } finally {
    loading.value = false
  }
}

const openDialog = async (row) => {
  form.id = row?.id || null
  form.tableNo = row?.tableNo || ''
  form.seats = row?.seats ?? 4
  form.status = row?.status ?? 0
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
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
      await adminApi.updateTable({ ...form })
      ElMessage.success('餐桌修改成功')
    } else {
      await adminApi.createTable({ ...form })
      ElMessage.success('餐桌新增成功')
    }
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row, val) => {
  try {
    await adminApi.updateTable({ ...row, status: val ? 1 : 0 })
    row.status = val ? 1 : 0
    ElMessage.success(val ? '餐桌已设为使用中' : '餐桌已设为空闲')
  } catch (e) {
    load()
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除餐桌「${row.tableNo}」吗？`, '删除餐桌', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await adminApi.deleteTable(row.id)
    ElMessage.success('餐桌已删除')
    load()
  } catch (e) {
    // 取消或接口异常
  }
}

onMounted(load)
</script>

<style scoped>
.table-overview {
  display: flex;
  gap: 30px;
  padding: 6px 4px 20px;
  border-bottom: 1px solid #f3ebe2;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.overview-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.overview-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.overview-dot.free { background: #52a878; }
.overview-dot.busy { background: #e06060; }
.overview-dot.total { background: #d9b28c; }

.overview-item b {
  font-size: 20px;
  color: #3b2b22;
}

.overview-item small {
  display: block;
  font-size: 11px;
  color: #a08b7c;
  margin-top: 1px;
}

.table-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 14px;
  min-height: 120px;
}

.table-card {
  padding: 16px 18px 12px;
  border-radius: 15px;
  border: 1px solid #f0e5d9;
  background: linear-gradient(180deg, #fffdfb, #fff);
  transition: all 0.2s;
}

.table-card:hover {
  box-shadow: 0 10px 26px rgba(88, 55, 22, 0.08);
  transform: translateY(-2px);
}

.table-card.busy {
  border-color: #f6d2cd;
  background: linear-gradient(180deg, #fffaf9, #fff);
}

.table-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-number {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #3b2b22;
}

.table-seats {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  font-size: 12px;
  color: #a08b7c;
}

.table-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 14px;
  padding-top: 10px;
  border-top: 1px dashed #f0e6db;
}

.table-foot :deep(.el-switch) {
  --el-switch-on-color: #e06060;
  --el-switch-off-color: #52a878;
}

.table-actions {
  display: flex;
  align-items: center;
}

.form-tip {
  margin-left: 8px;
  font-size: 11.5px;
  color: #a08b7c;
}
</style>
