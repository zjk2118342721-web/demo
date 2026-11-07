<template>
  <div class="admin-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-logo">味</div>
        <div class="brand-text">
          <h1>味食香</h1>
          <p>商家管理后台</p>
        </div>
      </div>

      <div class="menu-label">经营管理</div>
      <el-menu
        class="side-menu"
        :default-active="$route.path"
        router
        background-color="transparent"
        text-color="#c9b4a4"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><Menu /></el-icon>
          <span>菜品分类</span>
        </el-menu-item>
        <el-menu-item index="/dishes">
          <el-icon><Goods /></el-icon>
          <span>菜品管理</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><Tickets /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/reviews">
          <el-icon><ChatDotRound /></el-icon>
          <span>评价管理</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <span>顾客管理</span>
        </el-menu-item>
        <el-menu-item index="/tables">
          <el-icon><House /></el-icon>
          <span>餐桌管理</span>
        </el-menu-item>
      </el-menu>

      <div class="side-status">
        <div class="status-head">
          <span class="pulse"></span>
          <span>门店营业中</span>
        </div>
        <p>前台小程序与后台数据实时同步</p>
      </div>
    </aside>

    <div class="main">
      <header class="topbar">
        <div class="topbar-left">
          <div class="mini-logo">味</div>
          <span class="sys-name">味食香 · 在线点餐系统</span>
          <span class="divider"></span>
          <span class="date-text">{{ today }}</span>
        </div>

        <div class="topbar-right">
          <el-tooltip content="顾客端小程序已开启" placement="bottom">
            <el-button text circle class="icon-btn">
              <el-icon><Bell /></el-icon>
            </el-button>
          </el-tooltip>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-chip">
              <div class="avatar">{{ initial }}</div>
              <div class="user-info">
                <b>{{ adminName }}</b>
                <span>管理员</span>
              </div>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const adminName = computed(() => auth.admin?.name || '味食香店长')
const initial = computed(() => (adminName.value || '管').slice(0, 1))
const today = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long'
})

const handleCommand = async (command) => {
  if (command !== 'logout') return
  try {
    await ElMessageBox.confirm('确定退出当前管理账号吗？', '退出登录', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
    auth.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (e) {
    // 取消操作
  }
}
</script>

<style scoped>
.admin-shell {
  display: flex;
  min-height: 100vh;
  background: var(--wsx-bg);
}

.sidebar {
  position: relative;
  width: 238px;
  flex-shrink: 0;
  padding: 24px 16px 20px;
  display: flex;
  flex-direction: column;
  color: #fff;
  background:
    radial-gradient(circle at 15% 8%, rgba(255, 130, 66, 0.28), transparent 42%),
    radial-gradient(circle at 90% 92%, rgba(255, 107, 53, 0.16), transparent 50%),
    linear-gradient(170deg, #3a2419 0%, #2a1a11 55%, #21140d 100%);
  box-shadow: 8px 0 30px rgba(47, 27, 15, 0.14);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 8px 24px;
}

.brand-logo {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(140deg, #ff8a5b, #f25c26);
  box-shadow: 0 8px 20px rgba(255, 107, 53, 0.4);
}

.brand-text h1 {
  margin: 0;
  font-size: 20px;
  letter-spacing: 2px;
  font-weight: 700;
}

.brand-text p {
  margin: 3px 0 0;
  font-size: 11px;
  letter-spacing: 1.5px;
  color: #b79e8c;
}

.menu-label {
  padding: 0 12px 10px;
  font-size: 11px;
  letter-spacing: 2px;
  color: #8d7462;
}

.side-menu {
  border-right: none;
  flex: 1;
}

.side-menu :deep(.el-menu-item) {
  height: 48px;
  margin-bottom: 6px;
  border-radius: 12px;
  font-size: 14px;
  letter-spacing: 0.5px;
  transition: all 0.22s ease;
}

.side-menu :deep(.el-menu-item .el-icon) {
  font-size: 18px;
  margin-right: 10px;
}

.side-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.06);
  color: #ffe6d8;
}

.side-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(120deg, #ff7f4f, #eb5a25);
  color: #fff;
  box-shadow: 0 10px 24px rgba(235, 90, 37, 0.34);
}

.side-status {
  margin-top: 16px;
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.status-head {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #ffe0cf;
}

.pulse {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #6ee7a0;
  box-shadow: 0 0 0 0 rgba(110, 231, 160, 0.7);
  animation: pulse 1.8s infinite;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(110, 231, 160, 0.7); }
  70% { box-shadow: 0 0 0 8px rgba(110, 231, 160, 0); }
  100% { box-shadow: 0 0 0 0 rgba(110, 231, 160, 0); }
}

.side-status p {
  margin: 7px 0 0;
  font-size: 11.5px;
  line-height: 1.6;
  color: #a68e7d;
}

.main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  height: 70px;
  flex-shrink: 0;
  padding: 0 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(236, 225, 213, 0.8);
  position: sticky;
  top: 0;
  z-index: 10;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.mini-logo {
  width: 30px;
  height: 30px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  background: linear-gradient(135deg, #ff8659, #ef5a24);
}

.sys-name {
  font-size: 14px;
  font-weight: 600;
  color: #4a3a2f;
  white-space: nowrap;
}

.divider {
  width: 1px;
  height: 14px;
  background: #e3d6c8;
}

.date-text {
  font-size: 12.5px;
  color: var(--wsx-muted);
  white-space: nowrap;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-btn {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: #fbf5ef;
  color: #7d6759;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 5px 10px 5px 5px;
  border-radius: 40px;
  cursor: pointer;
  transition: background 0.2s;
}

.user-chip:hover {
  background: #fbf5ef;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #ff9a6c, #ef5a24);
}

.user-info {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.user-info b {
  font-size: 13px;
  color: #3d2d24;
}

.user-info span {
  font-size: 10.5px;
  color: #a08a7b;
}

.arrow {
  font-size: 12px;
  color: #a08a7b;
}

.content {
  flex: 1;
  padding: 22px 26px 30px;
  overflow-x: hidden;
}

@media (max-width: 1080px) {
  .sidebar {
    width: 206px;
  }
  .content {
    padding: 18px 18px 26px;
  }
}

@media (max-width: 820px) {
  .sidebar {
    display: none;
  }
  .topbar {
    padding: 0 16px;
  }
  .sys-name,
  .divider,
  .date-text,
  .user-info {
    display: none;
  }
}
</style>
