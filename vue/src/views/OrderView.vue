<template>
  <div>
    <div class="page-head">
      <div>
        <h2 class="page-title">订单管理</h2>
        <p class="page-sub">接单、出餐、完成与取消，全流程跟踪顾客点餐订单</p>
      </div>
      <div class="head-actions">
        <el-button :icon="Refresh" circle @click="load" />
        <el-button type="primary" plain :icon="Search" @click="load">刷新订单</el-button>
      </div>
    </div>

    <div class="page-card">
      <div class="status-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.label"
          class="status-tab"
          :class="{ active: query.status === tab.value }"
          @click="changeTab(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索订单号 / 桌号"
          clearable
          style="width: 250px"
          :prefix-icon="Search"
          @keyup.enter="search"
          @clear="search"
        />
        <el-button type="primary" plain @click="search">查询</el-button>
        <div class="toolbar-spacer"></div>
        <span class="total-text">共 {{ total }} 笔订单</span>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-box">
              <div class="expand-title">订单菜品明细</div>
              <div class="expand-list">
                <div v-for="item in row.items || []" :key="item.id" class="expand-item">
                  <el-image :src="item.dishImage" fit="cover" class="expand-thumb">
                    <template #error>
                      <div class="thumb-fallback">味</div>
                    </template>
                  </el-image>
                  <div class="expand-meta">
                    <b>{{ item.dishName }}</b>
                    <span>¥{{ money(item.price) }} × {{ item.quantity }}</span>
                  </div>
                  <div class="expand-subtotal">¥{{ money(Number(item.price) * Number(item.quantity)) }}</div>
                </div>
                <div v-if="!(row.items || []).length" class="expand-empty">暂无菜品明细</div>
              </div>
              <div v-if="row.remark" class="expand-remark">
                <span>备注</span>
                <p>{{ row.remark }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单号" min-width="180">
          <template #default="{ row }">
            <div class="order-no">{{ row.orderNo }}</div>
          </template>
        </el-table-column>
        <el-table-column label="桌号" width="80" align="center">
          <template #default="{ row }">
            <span class="table-badge">{{ row.tableNo || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="userNickname" label="顾客" width="110" show-overflow-tooltip />
        <el-table-column label="菜品" min-width="180">
          <template #default="{ row }">
            <span class="items-summary">{{ itemsSummary(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="100" align="right">
          <template #default="{ row }">
            <b class="amount">¥{{ money(row.totalAmount) }}</b>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="96" align="center">
          <template #default="{ row }">
            <el-tag :type="orderStatusType(row.status)" effect="light" size="small">
              {{ orderStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="160" />
        <el-table-column label="操作" width="190" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 1" link type="success" @click="changeStatus(row, 2)">接单</el-button>
            <el-button v-if="row.status === 2" link type="success" @click="changeStatus(row, 3)">出餐</el-button>
            <el-button v-if="row.status === 3" link type="success" @click="changeStatus(row, 4)">完成</el-button>
            <el-button
              v-if="row.status === 0 || row.status === 1 || row.status === 2"
              link
              type="danger"
              @click="changeStatus(row, 5)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !list.length" description="当前筛选条件下暂无订单" />

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

    <el-drawer v-model="drawerVisible" title="订单详情" size="430px">
      <div v-if="current" class="detail-wrap">
        <div class="detail-head">
          <div>
            <span class="detail-label">订单号</span>
            <b class="detail-order">{{ current.orderNo }}</b>
          </div>
          <el-tag :type="orderStatusType(current.status)" effect="light">
            {{ orderStatusText(current.status) }}
          </el-tag>
        </div>

        <div class="detail-grid">
          <div><span>桌号</span><b>{{ current.tableNo || '—' }}</b></div>
          <div><span>顾客</span><b>{{ current.userNickname }}</b></div>
          <div><span>下单时间</span><b>{{ current.createTime }}</b></div>
          <div><span>支付时间</span><b>{{ current.payTime || '未支付' }}</b></div>
        </div>

        <div class="detail-section">菜品明细</div>
        <div class="detail-items">
          <div v-for="item in current.items || []" :key="item.id" class="detail-item">
            <el-image :src="item.dishImage" fit="cover" class="detail-thumb">
              <template #error>
                <div class="thumb-fallback">味</div>
              </template>
            </el-image>
            <div class="detail-meta">
              <b>{{ item.dishName }}</b>
              <span>¥{{ money(item.price) }}</span>
            </div>
            <span class="detail-qty">× {{ item.quantity }}</span>
            <b class="detail-subtotal">¥{{ money(Number(item.price) * Number(item.quantity)) }}</b>
          </div>
        </div>

        <div v-if="current.remark" class="detail-section">顾客备注</div>
        <div v-if="current.remark" class="detail-remark">{{ current.remark }}</div>

        <div class="detail-total">
          <span>订单合计</span>
          <b>¥{{ money(current.totalAmount) }}</b>
        </div>

        <div class="drawer-actions">
          <el-button v-if="current.status === 1" type="success" @click="changeStatus(current, 2)">确认接单</el-button>
          <el-button v-if="current.status === 2" type="success" @click="changeStatus(current, 3)">确认出餐</el-button>
          <el-button v-if="current.status === 3" type="success" @click="changeStatus(current, 4)">完成订单</el-button>
          <el-button
            v-if="current.status <= 2"
            type="danger"
            plain
            @click="changeStatus(current, 5)"
          >
            取消订单
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { adminApi } from '../api'
import { money, orderStatusText, orderStatusType } from '../utils/format'

const tabs = [
  { label: '全部', value: null },
  { label: '待付款', value: 0 },
  { label: '待接单', value: 1 },
  { label: '制作中', value: 2 },
  { label: '待上菜', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已取消', value: 5 }
]

const list = ref([])
const total = ref(0)
const loading = ref(false)
const drawerVisible = ref(false)
const current = ref(null)

const query = reactive({
  page: 1,
  size: 10,
  status: null,
  keyword: ''
})

const load = async () => {
  loading.value = true
  try {
    const data = await adminApi.orders(query)
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

const changeTab = (value) => {
  query.status = value
  query.page = 1
  load()
}

const itemsSummary = (row) => {
  const items = row.items || []
  if (!items.length) return '—'
  const names = items.slice(0, 2).map((i) => `${i.dishName} ×${i.quantity}`).join('、')
  return items.length > 2 ? `${names} 等${items.length}项` : names
}

const openDetail = async (row) => {
  try {
    current.value = await adminApi.orderDetail(row.id)
    drawerVisible.value = true
  } catch (e) {
    // 已统一提示
  }
}

const changeStatus = async (row, status) => {
  const actions = {
    2: { title: '确认接单', text: '确认接单并通知后厨开始制作吗？' },
    3: { title: '确认出餐', text: '确认菜品已制作完成并出餐吗？' },
    4: { title: '完成订单', text: '确认顾客已用餐完毕，完成该订单吗？' },
    5: { title: '取消订单', text: '确定取消该订单吗？已支付订单将回退菜品销量。', danger: true }
  }
  const action = actions[status]
  try {
    await ElMessageBox.confirm(action.text, action.title, {
      type: action.danger ? 'warning' : 'info',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    await adminApi.updateOrderStatus(row.id, status)
    ElMessage.success(`${action.title}成功`)
    drawerVisible.value = false
    load()
  } catch (e) {
    // 取消或接口异常
  }
}

onMounted(load)
</script>

<style scoped>
.head-actions {
  display: flex;
  gap: 8px;
}

.status-tabs {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 16px;
  padding: 5px;
  border-radius: 12px;
  background: #fbf6f0;
  width: fit-content;
}

.status-tab {
  border: none;
  background: transparent;
  padding: 8px 16px;
  border-radius: 9px;
  font-size: 12.5px;
  color: #8b7668;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.status-tab:hover {
  color: #d9571f;
}

.status-tab.active {
  background: #fff;
  color: #d9571f;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(88, 55, 22, 0.08);
}

.toolbar-spacer {
  flex: 1;
}

.total-text {
  font-size: 12.5px;
  color: #a08b7c;
}

.order-no {
  font-family: "SFMono-Regular", Consolas, monospace;
  font-size: 12px;
  color: #57443a;
  letter-spacing: 0.2px;
}

.table-badge {
  display: inline-block;
  padding: 3px 9px;
  border-radius: 7px;
  background: #f4ede4;
  color: #8a6a53;
  font-size: 12px;
  font-weight: 600;
}

.items-summary {
  font-size: 12px;
  color: #7f6a5b;
}

.amount {
  color: #e25422;
  font-size: 14px;
}

.expand-box {
  padding: 12px 20px 16px 48px;
  background: #fffcf8;
}

.expand-title {
  font-size: 12px;
  font-weight: 600;
  color: #8a7262;
  margin-bottom: 10px;
}

.expand-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 640px;
}

.expand-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.expand-thumb {
  width: 38px;
  height: 38px;
  border-radius: 8px;
  flex-shrink: 0;
}

.thumb-fallback {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8efe6;
  color: #d9b28c;
  font-size: 15px;
}

.expand-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  line-height: 1.35;
}

.expand-meta b {
  font-size: 12.5px;
  color: #4b392e;
}

.expand-meta span {
  font-size: 11px;
  color: #a08b7c;
}

.expand-subtotal {
  font-size: 12.5px;
  color: #7f6a5b;
}

.expand-empty {
  font-size: 12px;
  color: #b09b8c;
}

.expand-remark {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #8a7262;
}

.expand-remark span {
  color: #b09b8c;
}

.expand-remark p {
  margin: 0;
  color: #6f5a4c;
}

.detail-wrap {
  padding: 0 4px;
}

.detail-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1e8de;
}

.detail-label {
  display: block;
  font-size: 11px;
  color: #a08b7c;
  margin-bottom: 5px;
}

.detail-order {
  font-family: "SFMono-Regular", Consolas, monospace;
  font-size: 16px;
  color: #3b2b22;
  letter-spacing: 0.3px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px 10px;
  padding: 16px 0;
  border-bottom: 1px solid #f1e8de;
}

.detail-grid div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-grid span {
  font-size: 11px;
  color: #a08b7c;
}

.detail-grid b {
  font-size: 12.5px;
  color: #4b392e;
  font-weight: 500;
}

.detail-section {
  margin: 16px 0 10px;
  font-size: 12.5px;
  font-weight: 600;
  color: #6f5a4c;
}

.detail-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 11px;
  background: #fcf8f3;
}

.detail-thumb {
  width: 44px;
  height: 44px;
  border-radius: 9px;
  flex-shrink: 0;
}

.detail-thumb :deep(.thumb-fallback) {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f2e7da;
  color: #d9b28c;
}

.detail-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.detail-meta b {
  font-size: 13px;
  color: #3b2b22;
}

.detail-meta span {
  font-size: 11.5px;
  color: #a08b7c;
}

.detail-qty {
  font-size: 12px;
  color: #8b7668;
}

.detail-subtotal {
  font-size: 13px;
  color: #3b2b22;
  min-width: 62px;
  text-align: right;
}

.detail-remark {
  padding: 10px 12px;
  border-radius: 9px;
  background: #fff6ed;
  font-size: 12.5px;
  color: #8a5a35;
  line-height: 1.7;
}

.detail-total {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px dashed #eaded2;
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.detail-total span {
  font-size: 13px;
  color: #8b7668;
}

.detail-total b {
  font-size: 24px;
  color: #e25422;
}

.drawer-actions {
  margin-top: 22px;
  display: flex;
  gap: 10px;
}

.drawer-actions .el-button {
  flex: 1;
}
</style>
