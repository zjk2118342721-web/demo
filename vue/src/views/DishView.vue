<template>
  <div>
    <div class="page-head">
      <div>
        <h2 class="page-title">菜品管理</h2>
        <p class="page-sub">维护小程序菜单、价格与上下架状态，图片支持本地上传</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增菜品</el-button>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 150px" @change="search">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px" @change="search">
          <el-option label="已上架" :value="1" />
          <el-option label="已下架" :value="0" />
        </el-select>
        <el-input
          v-model="query.keyword"
          placeholder="搜索菜品名称 / 描述"
          clearable
          style="width: 240px"
          :prefix-icon="Search"
          @keyup.enter="search"
          @clear="search"
        />
        <el-button type="primary" plain @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
        <div class="toolbar-spacer"></div>
        <span class="total-text">共 {{ total }} 道菜品</span>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="菜品" min-width="270">
          <template #default="{ row }">
            <div class="dish-cell">
              <el-image :src="row.image" fit="cover" class="dish-thumb">
                <template #error>
                  <div class="thumb-fallback">味</div>
                </template>
              </el-image>
              <div class="dish-meta">
                <b>{{ row.name }}</b>
                <p>{{ row.description || '暂无菜品描述' }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="110">
          <template #default="{ row }">
            <span class="category-tag">{{ row.categoryName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="售价" width="110" align="right">
          <template #default="{ row }">
            <div class="price-cell">
              <b>¥{{ money(row.price) }}</b>
              <span v-if="Number(row.originalPrice) > Number(row.price)" class="origin-price">
                ¥{{ money(row.originalPrice) }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sales" label="销量" width="86" align="center" />
        <el-table-column prop="sort" label="排序" width="76" align="center" />
        <el-table-column label="状态" width="108" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              inactive-text=""
              @change="(val) => toggleStatus(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !list.length" description="暂无菜品数据" />

      <div class="table-pagination">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          background
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="search"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑菜品' : '新增菜品'" width="640px" top="6vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px" class="dish-form">
        <div class="form-row">
          <el-form-item label="菜品图片" prop="image" class="image-item">
            <div class="upload-wrap">
              <el-upload
                class="image-uploader"
                action="/api/admin/upload"
                name="file"
                :headers="uploadHeaders"
                :show-file-list="false"
                :before-upload="beforeUpload"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
              >
                <div v-if="form.image" class="image-preview">
                  <img :src="form.image" alt="菜品图片" />
                  <div class="image-mask">
                    <el-icon><Camera /></el-icon>
                    更换图片
                  </div>
                </div>
                <div v-else class="upload-placeholder">
                  <el-icon><Plus /></el-icon>
                  <span>上传图片</span>
                  <small>建议 1:1，jpg/png，≤10MB</small>
                </div>
              </el-upload>
            </div>
          </el-form-item>

          <div class="form-right">
            <el-form-item label="菜品名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入菜品名称" maxlength="80" show-word-limit />
            </el-form-item>
            <el-form-item label="所属分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <div class="price-row">
              <el-form-item label="售价（元）" prop="price">
                <el-input-number v-model="form.price" :min="0.01" :max="9999" :precision="2" :step="1" controls-position="right" style="width: 150px" />
              </el-form-item>
              <el-form-item label="原价" prop="originalPrice" label-width="62px">
                <el-input-number v-model="form.originalPrice" :min="0" :max="9999" :precision="2" :step="1" controls-position="right" style="width: 150px" />
              </el-form-item>
            </div>
          </div>
        </div>

        <el-form-item label="菜品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="介绍菜品口味、食材与特色，展示在小程序详情页"
          />
        </el-form-item>

        <div class="form-bottom">
          <el-form-item label="展示排序">
            <el-input-number v-model="form.sort" :min="0" :max="999" controls-position="right" />
            <span class="form-tip">数字越小越靠前</span>
          </el-form-item>
          <el-form-item label="上架状态">
            <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存菜品</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Camera, Plus, Search } from '@element-plus/icons-vue'
import { adminApi } from '../api'
import { money } from '../utils/format'

const categories = ref([])
const list = ref([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const query = reactive({
  page: 1,
  size: 10,
  categoryId: null,
  keyword: '',
  status: null
})

const form = reactive({
  id: null,
  name: '',
  categoryId: null,
  description: '',
  image: '',
  price: 0,
  originalPrice: 0,
  sort: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择所属分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }],
  image: [{ required: true, message: '请上传菜品图片', trigger: 'change' }]
}

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('wsx_admin_token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const loadCategories = async () => {
  categories.value = await adminApi.categories()
}

const load = async () => {
  loading.value = true
  try {
    const data = await adminApi.dishes(query)
    list.value = data.records || []
    total.value = Number(data.total || 0)
  } finally {
    loading.value = false
  }
}

const search = () => {
  query.page = 1
  load()
}

const reset = () => {
  query.categoryId = null
  query.keyword = ''
  query.status = null
  search()
}

const openDialog = async (row) => {
  form.id = row?.id || null
  form.name = row?.name || ''
  form.categoryId = row?.categoryId || null
  form.description = row?.description || ''
  form.image = row?.image || ''
  form.price = Number(row?.price ?? 0)
  form.originalPrice = Number(row?.originalPrice ?? 0)
  form.sort = row?.sort ?? 0
  form.status = row?.status ?? 1
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLt10M) ElMessage.error('图片大小不能超过 10MB')
  return isImage && isLt10M
}

const handleUploadSuccess = (res) => {
  if (res.code === 200) {
    form.image = res.data
    formRef.value?.validateField('image')
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(res.msg || '图片上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败，请检查登录状态或网络')
}

const save = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  saving.value = true
  try {
    const payload = { ...form, price: Number(form.price), originalPrice: Number(form.originalPrice) }
    if (payload.id) {
      await adminApi.updateDish(payload)
      ElMessage.success('菜品修改成功')
    } else {
      await adminApi.createDish(payload)
      ElMessage.success('菜品新增成功')
    }
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row, val) => {
  try {
    await adminApi.changeDishStatus(row.id, val ? 1 : 0)
    row.status = val ? 1 : 0
    ElMessage.success(val ? '菜品已上架' : '菜品已下架')
  } catch (e) {
    load()
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除菜品「${row.name}」吗？`, '删除菜品', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await adminApi.deleteDish(row.id)
    ElMessage.success('菜品已删除')
    if (list.value.length === 1 && query.page > 1) query.page -= 1
    load()
  } catch (e) {
    // 取消或接口异常
  }
}

onMounted(() => {
  loadCategories()
  load()
})
</script>

<style scoped>
.toolbar-spacer {
  flex: 1;
}

.total-text {
  font-size: 12.5px;
  color: #a08b7c;
}

.dish-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}

.dish-thumb {
  width: 54px;
  height: 54px;
  border-radius: 10px;
  flex-shrink: 0;
  background: #f5ece2;
  box-shadow: 0 4px 10px rgba(88, 55, 22, 0.08);
}

.thumb-fallback {
  width: 54px;
  height: 54px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #d9b28c;
  font-size: 20px;
  background: #f8efe6;
}

.dish-meta {
  min-width: 0;
}

.dish-meta b {
  font-size: 13.5px;
  color: #3c2c23;
}

.dish-meta p {
  margin: 4px 0 0;
  font-size: 11.5px;
  color: #a38f81;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.category-tag {
  display: inline-block;
  padding: 2px 9px;
  border-radius: 6px;
  font-size: 11.5px;
  color: #b0764c;
  background: #fbf2e8;
}

.price-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  line-height: 1.3;
}

.price-cell b {
  color: #e25422;
  font-size: 14.5px;
}

.origin-price {
  font-size: 11px;
  color: #b9a495;
  text-decoration: line-through;
}

.dish-form .form-row {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.image-item :deep(.el-form-item__label) {
  padding-bottom: 6px;
}

.upload-wrap {
  width: 100%;
}

.image-uploader :deep(.el-upload) {
  border-radius: 12px;
  overflow: hidden;
  border: 1px dashed #e3d3c3;
  background: #fcf8f3;
  transition: border-color 0.2s;
}

.image-uploader :deep(.el-upload:hover) {
  border-color: #ff8a5c;
}

.image-preview {
  position: relative;
  width: 150px;
  height: 150px;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.image-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  background: rgba(44, 27, 17, 0.55);
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.2s;
}

.image-preview:hover .image-mask {
  opacity: 1;
}

.upload-placeholder {
  width: 150px;
  height: 150px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #a98e79;
}

.upload-placeholder .el-icon {
  font-size: 26px;
  color: #d9b28c;
}

.upload-placeholder span {
  font-size: 12.5px;
}

.upload-placeholder small {
  font-size: 10.5px;
  color: #bbaa9c;
}

.form-right {
  flex: 1;
  min-width: 0;
}

.price-row {
  display: flex;
  gap: 10px;
}

.form-bottom {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.form-tip {
  margin-left: 8px;
  font-size: 11.5px;
  color: #a08b7c;
}

@media (max-width: 760px) {
  .dish-form .form-row {
    display: block;
  }
  .price-row {
    display: block;
  }
}
</style>
