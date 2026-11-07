<template>
  <div class="login-page">
    <section class="visual">
      <div class="visual-bg"></div>
      <div class="visual-overlay"></div>
      <div class="visual-inner">
        <div class="brand-row">
          <div class="brand-mark">味</div>
          <span class="brand-name">味食香</span>
          <span class="brand-en">WEI SHI XIANG</span>
        </div>

        <h1>让每一道好菜<br />都被准时端上桌</h1>
        <p class="visual-desc">微信点餐、后厨接单、订单流转与经营数据，一套系统管理整间餐厅。</p>

        <div class="feature-row">
          <div class="feature">
            <span class="num">01</span>
            <b>扫码点餐</b>
            <p>顾客微信自助下单，减少等位与人工点单</p>
          </div>
          <div class="feature">
            <span class="num">02</span>
            <b>后厨协同</b>
            <p>接单、制作、上菜全流程状态实时同步</p>
          </div>
          <div class="feature">
            <span class="num">03</span>
            <b>经营洞察</b>
            <p>订单趋势、菜品销量与顾客评价一目了然</p>
          </div>
        </div>

        <div class="visual-footer">
          <span>味食香餐饮 · 数字化门店</span>
          <span>© 2026</span>
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-inner">
        <div class="card">
          <div class="card-eyebrow">
            <span class="eyebrow-dot"></span>
            商家管理端
          </div>
          <h2>欢迎回来</h2>
          <p class="card-sub">登录后即可管理菜品、订单与顾客评价</p>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="login-form">
            <el-form-item label="管理员账号" prop="username">
              <el-input v-model="form.username" size="large" placeholder="请输入账号" clearable>
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="登录密码" prop="password">
              <el-input
                v-model="form.password"
                size="large"
                type="password"
                placeholder="请输入密码"
                show-password
                @keyup.enter="submit"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="submit">
              登录管理后台
            </el-button>
          </el-form>

          <div class="demo-hint">
            <span class="hint-label">演示账号</span>
            <span class="hint-value">admin</span>
            <span class="hint-sep">/</span>
            <span class="hint-value">123456</span>
          </div>
        </div>

        <div class="powered">味食香 · 餐饮数字化解决方案 · v1.0</div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminApi } from '../api'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: '123456'
})

const rules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }]
}

const submit = async () => {
  if (loading.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    const data = await adminApi.login(form)
    auth.setLogin(data.token, data.admin)
    ElMessage.success(`欢迎回来，${data.admin?.name || '店长'}`)
    router.push('/dashboard')
  } catch (e) {
    // 错误提示已由请求拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  background: #241610;
}

.visual {
  position: relative;
  flex: 1.15;
  min-height: 100vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  padding: 56px 64px;
}

.visual-bg {
  position: absolute;
  inset: 0;
  background: url('/uploads/banner.jpg') center / cover no-repeat;
  transform: scale(1.04);
}

.visual-overlay {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(120deg, rgba(25, 13, 8, 0.94) 0%, rgba(43, 24, 15, 0.82) 45%, rgba(64, 33, 18, 0.55) 100%),
    radial-gradient(circle at 80% 20%, rgba(255, 107, 53, 0.22), transparent 45%);
}

.visual-inner {
  position: relative;
  z-index: 1;
  max-width: 640px;
  color: #fff;
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 112px);
}

.brand-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 52px;
}

.brand-mark {
  width: 44px;
  height: 44px;
  border-radius: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(140deg, #ff8c5c, #f05a24);
  box-shadow: 0 10px 24px rgba(240, 90, 36, 0.36);
}

.brand-name {
  font-size: 19px;
  font-weight: 700;
  letter-spacing: 3px;
}

.brand-en {
  font-size: 10px;
  letter-spacing: 2px;
  color: #b99c88;
  border-left: 1px solid rgba(255, 255, 255, 0.18);
  padding-left: 12px;
}

.visual-inner h1 {
  margin: 0;
  font-size: 42px;
  line-height: 1.32;
  font-weight: 800;
  letter-spacing: 1px;
}

.visual-desc {
  margin: 20px 0 42px;
  font-size: 14.5px;
  line-height: 1.9;
  color: #cbb4a3;
  max-width: 470px;
}

.feature-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
  margin-top: auto;
}

.feature {
  padding: 16px 16px 18px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.055);
  border: 1px solid rgba(255, 255, 255, 0.09);
  backdrop-filter: blur(6px);
}

.feature .num {
  display: block;
  font-size: 11px;
  letter-spacing: 1px;
  color: #ff9a6c;
  margin-bottom: 8px;
}

.feature b {
  font-size: 14px;
  letter-spacing: 0.5px;
}

.feature p {
  margin: 8px 0 0;
  font-size: 11.5px;
  line-height: 1.7;
  color: #a89484;
}

.visual-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 34px;
  padding-top: 18px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  font-size: 11px;
  letter-spacing: 0.6px;
  color: #8f7767;
}

.panel {
  width: 520px;
  flex-shrink: 0;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #f7f2ec;
  position: relative;
}

.panel-inner {
  width: 100%;
  max-width: 372px;
}

.card {
  background: #fff;
  border-radius: 20px;
  padding: 36px 34px 30px;
  border: 1px solid #efe4d8;
  box-shadow: 0 24px 60px rgba(72, 42, 20, 0.1);
}

.card-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 5px 11px;
  border-radius: 20px;
  background: #fff1e9;
  color: #d9571f;
  font-size: 11.5px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.eyebrow-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ff6b35;
}

.card h2 {
  margin: 20px 0 6px;
  font-size: 27px;
  letter-spacing: 1px;
  color: #33251d;
}

.card-sub {
  margin: 0 0 26px;
  font-size: 13px;
  color: #978577;
}

.login-form :deep(.el-form-item__label) {
  font-size: 12.5px;
  color: #6b5548;
  font-weight: 600;
  padding-bottom: 6px;
}

.login-form :deep(.el-input__wrapper) {
  height: 46px;
  border-radius: 11px;
  background: #fcf8f3;
  box-shadow: 0 0 0 1px #eee2d6 inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #ff8a5c inset;
  background: #fff;
}

.login-form :deep(.el-input__prefix) {
  color: #b49e8d;
  font-size: 16px;
}

.login-btn {
  width: 100%;
  height: 47px;
  margin-top: 8px;
  border-radius: 11px;
  font-size: 15px;
  letter-spacing: 1px;
  font-weight: 600;
}

.demo-hint {
  margin-top: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 12px;
  padding: 10px;
  border-radius: 9px;
  background: #fbf6ef;
  color: #8a7465;
}

.hint-label {
  font-size: 10.5px;
  padding: 2px 7px;
  border-radius: 5px;
  background: #f0e2d4;
  color: #9c7b64;
}

.hint-value {
  font-weight: 600;
  color: #5d4637;
  letter-spacing: 0.5px;
}

.hint-sep {
  color: #c1ab99;
}

.powered {
  margin-top: 22px;
  text-align: center;
  font-size: 11px;
  color: #aa9686;
  letter-spacing: 0.5px;
}

@media (max-width: 1100px) {
  .visual {
    padding: 40px;
  }
  .visual-inner h1 {
    font-size: 34px;
  }
  .panel {
    width: 460px;
    padding: 28px;
  }
}

@media (max-width: 900px) {
  .visual {
    display: none;
  }
  .panel {
    width: 100%;
  }
}
</style>
