<template>
  <div>
    <div class="page-head">
      <div>
        <h2 class="page-title">评价管理</h2>
        <p class="page-sub">查看顾客用餐评价，及时回复反馈，维护门店口碑</p>
      </div>
      <div class="rating-summary">
        <div class="score">{{ avgScore }}</div>
        <div class="score-meta">
          <el-rate :model-value="Math.round(Number(avgScore))" disabled size="small" />
          <span>当前筛选评价平均分</span>
        </div>
      </div>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-select v-model="query.rating" placeholder="全部评分" clearable style="width: 140px" @change="search">
          <el-option label="5 星好评" :value="5" />
          <el-option label="4 星" :value="4" />
          <el-option label="3 星及以下" :value="3" />
          <el-option label="2 星" :value="2" />
          <el-option label="1 星" :value="1" />
        </el-select>
        <el-input
          v-model="query.keyword"
          placeholder="搜索评价内容"
          clearable
          style="width: 240px"
          :prefix-icon="Search"
          @keyup.enter="search"
          @clear="search"
        />
        <el-button type="primary" plain @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
        <div class="toolbar-spacer"></div>
        <span class="total-text">共 {{ total }} 条评价</span>
      </div>

      <div v-loading="loading" class="review-list">
        <div v-for="row in list" :key="row.id" class="review-card">
          <div class="review-head">
            <div class="review-user">
              <el-avatar :src="row.userAvatar" :size="42" class="review-avatar">
                {{ (row.userNickname || '顾').slice(0, 1) }}
              </el-avatar>
              <div class="user-meta">
                <b>{{ row.userNickname || '顾客' }}</b>
                <div class="rate-line">
                  <el-rate :model-value="row.rating" disabled size="small" />
                  <span class="review-time">{{ row.createTime }}</span>
                </div>
              </div>
            </div>
            <div class="review-actions">
              <el-tag v-if="row.reply" type="success" effect="plain" size="small">已回复</el-tag>
              <el-tag v-else type="warning" effect="plain" size="small">待回复</el-tag>
              <el-button link type="primary" @click="openReply(row)">{{ row.reply ? '修改回复' : '回复' }}</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </div>
          </div>

          <div class="review-order">
            <span class="order-chip">订单 {{ row.orderNo || '—' }}</span>
            <span class="dish-chip">{{ row.dishNames || '菜品信息缺失' }}</span>
          </div>

          <p class="review-content">{{ row.content || '顾客未填写评价内容' }}</p>

          <div v-if="imageList(row.images).length" class="review-images">
            <el-image
              v-for="(img, idx) in imageList(row.images)"
              :key="idx"
              :src="img"
              :preview-src-list="imageList(row.images)"
              :initial-index="idx"
              fit="cover"
              class="review-image"
            />
          </div>

          <div v-if="row.reply" class="reply-box">
            <span class="reply-label">商家回复</span>
            <p>{{ row.reply }}</p>
          </div>
        </div>

        <el-empty v-if="!loading && !list.length" description="暂无评价数据" />
      </div>

      <div class="table-pagination">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :page-sizes="[5, 10, 20]"
          :total="total"
          background
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="search"
        />
      </div>
    </div>

    <el-dialog v-model="replyVisible" title="回复顾客评价" width="480px">
      <div v-if="currentReview" class="reply-target">
        <div class="reply-target-head">
          <b>{{ currentReview.userNickname }}</b>
          <el-rate :model-value="currentReview.rating" disabled size="small" />
        </div>
        <p>{{ currentReview.content || '顾客未填写评价内容' }}</p>
      </div>
      <el-input
        v-model="replyText"
        type="textarea"
        :rows="4"
        maxlength="500"
        show-word-limit
        placeholder="输入温暖的回复内容，顾客将在小程序评价中看到"
      />
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitReply">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { adminApi } from '../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const replyVisible = ref(false)
const replyText = ref('')
const currentReview = ref(null)

const query = reactive({
  page: 1,
  size: 10,
  rating: null,
  keyword: ''
})

const avgScore = computed(() => {
  if (!list.value.length) return '0.0'
  const sum = list.value.reduce((acc, item) => acc + Number(item.rating || 0), 0)
  return (sum / list.value.length).toFixed(1)
})

const imageList = (images) => {
  if (!images) return []
  return images.split(',').filter(Boolean)
}

const load = async () => {
  loading.value = true
  try {
    const data = await adminApi.reviews(query)
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
  query.rating = null
  query.keyword = ''
  search()
}

const openReply = (row) => {
  currentReview.value = row
  replyText.value = row.reply || ''
  replyVisible.value = true
}

const submitReply = async () => {
  if (!replyText.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  saving.value = true
  try {
    await adminApi.replyReview(currentReview.value.id, replyText.value.trim())
    ElMessage.success('回复已提交')
    replyVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除这条顾客评价吗？删除后不可恢复。', '删除评价', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await adminApi.deleteReview(row.id)
    ElMessage.success('评价已删除')
    load()
  } catch (e) {
    // 取消或接口异常
  }
}

onMounted(load)
</script>

<style scoped>
.rating-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 18px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid var(--wsx-border);
  box-shadow: 0 6px 16px rgba(88, 55, 22, 0.04);
}

.score {
  font-size: 28px;
  font-weight: 700;
  color: #e25422;
  line-height: 1;
}

.score-meta {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.score-meta span {
  font-size: 10.5px;
  color: #a08b7c;
}

.toolbar-spacer {
  flex: 1;
}

.total-text {
  font-size: 12.5px;
  color: #a08b7c;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 120px;
}

.review-card {
  padding: 18px 20px;
  border-radius: 14px;
  border: 1px solid #f1e6da;
  background: linear-gradient(180deg, #fffdfb, #fff);
  transition: border-color 0.2s, box-shadow 0.2s;
}

.review-card:hover {
  border-color: #ffd2bd;
  box-shadow: 0 8px 24px rgba(88, 55, 22, 0.07);
}

.review-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}

.review-user {
  display: flex;
  gap: 11px;
  align-items: center;
}

.review-avatar {
  background: linear-gradient(135deg, #ffb28b, #f0703b);
  color: #fff;
  font-weight: 600;
  flex-shrink: 0;
}

.user-meta b {
  font-size: 13.5px;
  color: #3b2b22;
}

.rate-line {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 3px;
}

.review-time {
  font-size: 11px;
  color: #ab9687;
}

.review-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.review-order {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin: 12px 0 8px;
}

.order-chip {
  font-size: 11px;
  padding: 3px 9px;
  border-radius: 6px;
  background: #f6f0e8;
  color: #8a7262;
  font-family: "SFMono-Regular", Consolas, monospace;
}

.dish-chip {
  font-size: 11px;
  padding: 3px 9px;
  border-radius: 6px;
  background: #fff2ea;
  color: #b4673c;
}

.review-content {
  margin: 0;
  font-size: 13px;
  line-height: 1.85;
  color: #57443a;
}

.review-images {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.review-image {
  width: 82px;
  height: 82px;
  border-radius: 10px;
  cursor: pointer;
}

.reply-box {
  margin-top: 13px;
  padding: 11px 14px;
  border-radius: 10px;
  background: #f7f4ed;
  border-left: 3px solid #d9b475;
}

.reply-label {
  font-size: 11px;
  color: #a8854f;
  font-weight: 600;
}

.reply-box p {
  margin: 5px 0 0;
  font-size: 12.5px;
  color: #6f5a4c;
  line-height: 1.7;
}

.reply-target {
  margin-bottom: 14px;
  padding: 13px 15px;
  border-radius: 11px;
  background: #fcf7f1;
}

.reply-target-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.reply-target-head b {
  font-size: 13px;
  color: #3b2b22;
}

.reply-target p {
  margin: 7px 0 0;
  font-size: 12.5px;
  color: #7f6a5b;
  line-height: 1.7;
}
</style>
