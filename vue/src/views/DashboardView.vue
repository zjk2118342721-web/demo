<template>
  <div v-loading="loading">
    <div class="page-head">
      <div>
        <h2 class="page-title">数据概览</h2>
        <p class="page-sub">{{ todayText }} · 味食香门店实时经营数据</p>
      </div>
      <div class="head-chips">
        <div class="chip">
          <span>在售菜品</span>
          <b>{{ summary.onlineDishes }}</b>
        </div>
        <div class="chip">
          <span>平均评分</span>
          <b>{{ Number(summary.avgRating || 0).toFixed(1) }}</b>
        </div>
      </div>
    </div>

    <div v-if="summary.pendingOrders > 0" class="pending-strip">
      <div class="pending-left">
        <div class="pending-icon">
          <el-icon><Bell /></el-icon>
        </div>
        <div>
          <b>有 {{ summary.pendingOrders }} 笔新订单等待接单</b>
          <p>顾客已支付完成，请尽快确认并通知后厨制作</p>
        </div>
      </div>
      <el-button type="primary" round @click="$router.push('/orders')">去处理</el-button>
    </div>

    <div class="stat-grid">
      <div class="stat-card theme-orange">
        <div class="stat-top">
          <div class="stat-icon"><el-icon><Tickets /></el-icon></div>
          <span class="stat-label">今日订单</span>
        </div>
        <div class="stat-value">{{ summary.todayOrders }}<small>笔</small></div>
        <div class="stat-foot">今日有效订单实时汇总</div>
      </div>

      <div class="stat-card theme-green">
        <div class="stat-top">
          <div class="stat-icon"><el-icon><Wallet /></el-icon></div>
          <span class="stat-label">今日营业额</span>
        </div>
        <div class="stat-value"><small class="yen">¥</small>{{ money(summary.todayRevenue) }}</div>
        <div class="stat-foot">已支付订单金额合计</div>
      </div>

      <div class="stat-card theme-red">
        <div class="stat-top">
          <div class="stat-icon"><el-icon><Bell /></el-icon></div>
          <span class="stat-label">待接单</span>
        </div>
        <div class="stat-value">{{ summary.pendingOrders }}<small>笔</small></div>
        <div class="stat-foot">需要后厨尽快确认的订单</div>
      </div>

      <div class="stat-card theme-blue">
        <div class="stat-top">
          <div class="stat-icon"><el-icon><User /></el-icon></div>
          <span class="stat-label">会员总数</span>
        </div>
        <div class="stat-value">{{ summary.totalUsers }}<small>人</small></div>
        <div class="stat-foot">小程序累计注册顾客</div>
      </div>
    </div>

    <div class="chart-grid">
      <div class="page-card chart-card wide">
        <div class="chart-head">
          <div>
            <h3>近 7 日订单与营业额</h3>
            <p>按支付成功订单统计</p>
          </div>
          <div class="legend-dots">
            <span><i class="dot orange"></i>订单量</span>
            <span><i class="dot green"></i>营业额</span>
          </div>
        </div>
        <div ref="trendRef" class="chart-box"></div>
      </div>

      <div class="page-card chart-card">
        <div class="chart-head">
          <div>
            <h3>分类销量占比</h3>
            <p>全部有效订单菜品销量</p>
          </div>
        </div>
        <div ref="pieRef" class="chart-box"></div>
      </div>
    </div>

    <div class="bottom-grid">
      <div class="page-card chart-card">
        <div class="chart-head">
          <div>
            <h3>热销菜品 TOP 8</h3>
            <p>按售出份数排名</p>
          </div>
        </div>
        <div ref="barRef" class="chart-box small"></div>
      </div>

      <div class="page-card recent-card">
        <div class="chart-head">
          <div>
            <h3>最新订单</h3>
            <p>最近产生的顾客订单</p>
          </div>
          <el-button text type="primary" @click="$router.push('/orders')">查看全部</el-button>
        </div>

        <el-table :data="summary.recentOrders" size="small" class="recent-table">
          <el-table-column prop="orderNo" label="订单号" min-width="160" show-overflow-tooltip />
          <el-table-column prop="tableNo" label="桌号" width="70" align="center" />
          <el-table-column label="金额" width="92" align="right">
            <template #default="{ row }">¥{{ money(row.totalAmount) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="82" align="center">
            <template #default="{ row }">
              <el-tag :type="orderStatusType(row.status)" size="small" effect="light">
                {{ orderStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="下单时间" width="150" show-overflow-tooltip />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { adminApi } from '../api'
import { money, orderStatusText, orderStatusType } from '../utils/format'

const loading = ref(false)
const trendRef = ref()
const pieRef = ref()
const barRef = ref()

const summary = reactive({
  todayOrders: 0,
  todayRevenue: 0,
  totalUsers: 0,
  pendingOrders: 0,
  onlineDishes: 0,
  avgRating: 0,
  trend: [],
  categorySales: [],
  topDishes: [],
  recentOrders: []
})

const todayText = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long'
})

let trendChart = null
let pieChart = null
let barChart = null

const initTrend = () => {
  if (!trendRef.value) return
  trendChart = echarts.init(trendRef.value)
  const trend = summary.trend || []
  trendChart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(47,36,29,.92)', borderWidth: 0, textStyle: { color: '#fff', fontSize: 12 } },
    grid: { left: 8, right: 14, top: 24, bottom: 6, containLabel: true },
    xAxis: {
      type: 'category',
      data: trend.map((i) => i.date),
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#e9ddd2' } },
      axisLabel: { color: '#9a8577', fontSize: 11.5 }
    },
    yAxis: [
      {
        type: 'value',
        name: '单',
        nameTextStyle: { color: '#b3a091', fontSize: 10, padding: [0, 0, 0, -20] },
        splitLine: { lineStyle: { color: '#f4ece4' } },
        axisLabel: { color: '#9a8577', fontSize: 11 }
      },
      {
        type: 'value',
        name: '元',
        nameTextStyle: { color: '#b3a091', fontSize: 10 },
        splitLine: { show: false },
        axisLabel: { color: '#9a8577', fontSize: 11 }
      }
    ],
    series: [
      {
        name: '订单量',
        type: 'bar',
        data: trend.map((i) => Number(i.orderCount || 0)),
        barWidth: 18,
        itemStyle: {
          borderRadius: [7, 7, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#ff9a68' },
            { offset: 1, color: '#ffd9c8' }
          ])
        }
      },
      {
        name: '营业额',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        data: trend.map((i) => Number(i.amount || 0)),
        lineStyle: { width: 3, color: '#4c9f70' },
        itemStyle: { color: '#4c9f70', borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(76,159,112,.22)' },
            { offset: 1, color: 'rgba(76,159,112,0)' }
          ])
        }
      }
    ]
  })
}

const initPie = () => {
  if (!pieRef.value) return
  pieChart = echarts.init(pieRef.value)
  const data = (summary.categorySales || []).map((i) => ({ name: i.name, value: Number(i.value || 0) }))
  const total = data.reduce((sum, item) => sum + item.value, 0)
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}<br/>销量 {c} 份（{d}%）' },
    color: ['#ff6b35', '#ffa069', '#ffc9a3', '#4c9f70', '#7fc4a0', '#e9b94f'],
    title: {
      text: `${total}`,
      subtext: '总销量 / 份',
      left: 'center',
      top: '34%',
      textStyle: { fontSize: 22, fontWeight: 700, color: '#3b2b22' },
      subtextStyle: { fontSize: 10.5, color: '#a49080' }
    },
    legend: {
      orient: 'vertical',
      right: 6,
      top: 'middle',
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      itemGap: 12,
      textStyle: { fontSize: 11.5, color: '#7d6a5c' }
    },
    series: [
      {
        type: 'pie',
        radius: ['50%', '72%'],
        center: ['38%', '48%'],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#fff', borderWidth: 3, borderRadius: 6 },
        label: { show: false },
        emphasis: { scale: true, scaleSize: 6 },
        data
      }
    ]
  })
}

const initBar = () => {
  if (!barRef.value) return
  barChart = echarts.init(barRef.value)
  const data = (summary.topDishes || []).map((i) => ({ name: i.name, value: Number(i.value || 0) })).reverse()
  barChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: '{b}<br/>售出 {c} 份' },
    grid: { left: 8, right: 34, top: 10, bottom: 6, containLabel: true },
    xAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f4ece4' } },
      axisLabel: { color: '#9a8577', fontSize: 11 }
    },
    yAxis: {
      type: 'category',
      data: data.map((i) => i.name),
      axisTick: { show: false },
      axisLine: { show: false },
      axisLabel: { color: '#6f5a4c', fontSize: 11.5 }
    },
    series: [
      {
        type: 'bar',
        data: data.map((i) => i.value),
        barWidth: 12,
        itemStyle: {
          borderRadius: [0, 8, 8, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#ffcaa8' },
            { offset: 1, color: '#ff6b35' }
          ])
        },
        label: { show: true, position: 'right', color: '#a0704f', fontSize: 11 }
      }
    ]
  })
}

const resizeCharts = () => {
  trendChart?.resize()
  pieChart?.resize()
  barChart?.resize()
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await adminApi.dashboard()
    Object.assign(summary, data)
    initTrend()
    initPie()
    initBar()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  trendChart?.dispose()
  pieChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped>
.head-chips {
  display: flex;
  gap: 10px;
}

.chip {
  display: flex;
  align-items: baseline;
  gap: 8px;
  padding: 9px 16px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid var(--wsx-border);
  box-shadow: 0 6px 16px rgba(88, 55, 22, 0.04);
}

.chip span {
  font-size: 12px;
  color: var(--wsx-muted);
}

.chip b {
  font-size: 17px;
  color: #d9571f;
}

.pending-strip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 15px 20px;
  margin-bottom: 18px;
  border-radius: 15px;
  background: linear-gradient(100deg, #fff4ee, #fffaf6 60%, #fff);
  border: 1px solid #ffddcb;
}

.pending-left {
  display: flex;
  align-items: center;
  gap: 13px;
}

.pending-icon {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff8f61, #ef5a24);
  color: #fff;
  font-size: 18px;
  box-shadow: 0 8px 18px rgba(239, 90, 36, 0.3);
}

.pending-strip b {
  font-size: 14px;
  color: #5c4030;
}

.pending-strip p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #a58672;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 18px;
}

.stat-card {
  padding: 18px 20px 16px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid var(--wsx-border);
  box-shadow: 0 10px 30px rgba(88, 55, 22, 0.05);
  position: relative;
  overflow: hidden;
}

.stat-card::after {
  content: '';
  position: absolute;
  right: -30px;
  top: -30px;
  width: 96px;
  height: 96px;
  border-radius: 50%;
  opacity: 0.08;
  background: currentColor;
}

.theme-orange { color: #f2703c; }
.theme-green { color: #3f9b6a; }
.theme-red { color: #e05252; }
.theme-blue { color: #4f80d4; }

.stat-top {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #fff;
  background: currentColor;
  opacity: 0.92;
}

.stat-label {
  font-size: 13px;
  color: #7f6a5b;
  font-weight: 500;
}

.stat-value {
  margin-top: 14px;
  font-size: 30px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: #33251d;
  line-height: 1.1;
}

.stat-value small {
  font-size: 12px;
  font-weight: 400;
  color: #a08b7c;
  margin-left: 4px;
}

.stat-value .yen {
  font-size: 16px;
  margin-right: 2px;
  color: #7f6a5b;
}

.stat-foot {
  margin-top: 8px;
  font-size: 11.5px;
  color: #a99688;
}

.chart-grid {
  display: grid;
  grid-template-columns: 1.55fr 1fr;
  gap: 16px;
  margin-bottom: 18px;
}

.bottom-grid {
  display: grid;
  grid-template-columns: 1fr 1.15fr;
  gap: 16px;
}

.chart-card {
  padding: 18px 20px 12px;
}

.chart-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.chart-head h3 {
  margin: 0;
  font-size: 15.5px;
  font-weight: 700;
  color: #3b2b22;
  letter-spacing: 0.3px;
}

.chart-head p {
  margin: 4px 0 0;
  font-size: 11.5px;
  color: #a49080;
}

.legend-dots {
  display: flex;
  gap: 14px;
  font-size: 11.5px;
  color: #8b7668;
  padding-top: 2px;
}

.legend-dots span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.dot.orange { background: #ff8d5b; }
.dot.green { background: #4c9f70; }

.chart-box {
  height: 288px;
  margin-top: 6px;
}

.chart-box.small {
  height: 286px;
}

.recent-card {
  padding: 18px 20px 12px;
}

.recent-table {
  margin-top: 8px;
}

.recent-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

@media (max-width: 1200px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .chart-grid,
  .bottom-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
  .head-chips {
    display: none;
  }
}
</style>
