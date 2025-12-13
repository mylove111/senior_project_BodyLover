<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import api from '../api/request';
import { useUserStore } from '../stores/user';

const router = useRouter();
const userStore = useUserStore();

const accountId = ref('');
const password = ref('');
const loading = ref(false);

const onSubmit = async () => {
  loading.value = true;
  try {
    const res = await api.post('/auth/login', {
      accountId: accountId.value,
      password: password.value
    });
    
    if (res.data.code === 200) {
      showToast('Login successful');
      userStore.setUser(res.data.data);
      
      // Redirect based on mode
      const mode = res.data.data.mode;
      if (mode === 'TEENAGER') router.push('/teenager');
      else if (mode === 'ADULT') router.push('/adult');
      else if (mode === 'SENIOR') router.push('/senior');
      else router.push('/');
      
    } else {
      showToast(res.data.message || 'Login failed');
    }
  } catch (error) {
    showToast('Network error, please check backend');
  } finally {
    loading.value = false;
  }
};

const goToRegister = () => {
  router.push('/register');
};
</script>

<template>
  <div class="login-container">
    <h1 class="title">BodyLover</h1>
    <p class="subtitle">Your Smart Fitness Companion</p>
    
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="accountId"
          name="accountId"
          label="Account ID"
          placeholder="Enter Account ID"
          :rules="[{ required: true, message: 'Account ID is required' }]"
        />
        <van-field
          v-model="password"
          type="password"
          name="Password"
          label="Password"
          placeholder="Enter password"
          :rules="[{ required: true, message: 'Password is required' }]"
        />
      </van-cell-group>
      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit" :loading="loading">
          Login
        </van-button>
        <div class="register-link" @click="goToRegister">
          No account? Register
        </div>
      </div>
    </van-form>
  </div>
</template>

<style scoped>
.login-container {
  padding-top: 50px;
  min-height: 100vh;
  background-color: #f7f8fa;
}
.title {
  text-align: center;
  color: #1989fa;
  margin-bottom: 10px;
}
.subtitle {
  text-align: center;
  color: #969799;
  margin-bottom: 40px;
}
.register-link {
  text-align: center;
  margin-top: 15px;
  color: #1989fa;
  font-size: 14px;
  cursor: pointer;
}
</style>
