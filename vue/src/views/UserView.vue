<template>
  <div>
    <div class="page-head">
      <div>
        <h2 class="page-title">顾客管理</h2>
        <p class="page-sub">查看通过微信小程序登录的顾客信息与注册时间</p>
      </div>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索顾客昵称 / 手机号"
          clearable
          style="width: 260px"
          :prefix-icon="Search"
          @keyup.enter="search"
          @clear="search"
        />
        <el-button type="primary" plain @click="search">查询</el-button>
        <div class="toolbar-spacer"></div>
        <span class="total-text">共 {{ total }} 位顾客</span>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="顾客" min-width="220">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :src="row.avatar" :size="42" class="user-avatar">
                {{ (row.nickname || '顾').slice(0, 1) }}
              </el-avatar>
              <div class="user-meta">
                <b>{{ row.nickname || '微信用户' }}</b>
                <span>{{ row.phone || '未绑定手机号' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="openid" label="微信 OpenID" min-width="220">
          <template #default="{ row }">
            <code class="openid">{{ row.openid }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140">
          <template #default="{ row }">{{ row.phone || '—' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
      </el-table>

      <el-empty v-if="!loading && !list.length" description="暂无顾客数据" />

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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { adminApi } from '../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({
  page: 1,
  size: 10,
  keyword: ''
})

const load = async () => {
  loading.value = true
  try {
    const data = await adminApi.users(query)
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

onMounted(load)
</script>

<style scoped>
.toolbar-spacer {
  flex: 1;
}

.total-text {
  font-size: 12.5px;
  color: #a08b7c;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  background: linear-gradient(135deg, #ffb28b, #f0703b);
  color: #fff;
  font-weight: 600;
  flex-shrink: 0;
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.user-meta b {
  font-size: 13.5px;
  color: #3b2b22;
}

.user-meta span {
  font-size: 11.5px;
  color: #a08b7c;
}

.openid {
  font-family: "SFMono-Regular", Consolas, monospace;
  font-size: 11.5px;
  color: #8a7262;
  background: #f8f3ec;
  padding: 3px 8px;
  border-radius: 6px;
}
</style>
