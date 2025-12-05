<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import api from '../api/request';

const router = useRouter();

const username = ref('');
const password = ref('');
const age = ref('');
const loading = ref(false);

// Automatically determine mode based on age
const mode = computed(() => {
  const ageVal = parseInt(age.value);
  if (!ageVal) return '';
  if (ageVal >= 12 && ageVal <= 18) return 'TEENAGER';
  if (ageVal >= 19 && ageVal <= 45) return 'ADULT';
  if (ageVal >= 46) return 'SENIOR';
  return 'UNKNOWN';
});

const modeText = computed(() => {
  const m = mode.value;
  if (m === 'TEENAGER') return 'Teenager Mode (12-18)';
  if (m === 'ADULT') return 'Adult Mode (19-45)';
  if (m === 'SENIOR') return 'Senior Mode (46+)';
  return 'Enter valid age';
});

const themeColor = computed(() => {
  const m = mode.value;
  if (m === 'TEENAGER') return '#4caf50';
  if (m === 'ADULT') return '#1989fa';
  if (m === 'SENIOR') return '#ff9800';
  return '#1989fa'; // Default
});

const onSubmit = async () => {
  if (mode.value === 'UNKNOWN' || !mode.value) {
    showToast('Enter valid age (12+)');
    return;
  }

  loading.value = true;
  try {
    const res = await api.post('/auth/register', {
      username: username.value,
      password: password.value,
      age: parseInt(age.value),
      mode: mode.value
    });
    
    if (res.data.code === 200) {
      showToast('Registration successful, please login');
      router.push('/login');
    } else {
      showToast(res.data.message || 'Registration failed');
    }
  } catch (error) {
    showToast('Registration failed, please retry');
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="register-container">
    <van-nav-bar
      title="Register"
      left-text="Back"
      left-arrow
      @click-left="router.back()"
    />
    
    <div class="content">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="username"
            name="Username"
            label="Username"
            placeholder="Enter username"
            :rules="[{ required: true, message: 'Username is required' }]"
          />
          <van-field
            v-model="password"
            type="password"
            name="Password"
            label="Password"
            placeholder="Enter password"
            :rules="[{ required: true, message: 'Password is required' }]"
          />
          <van-field
            v-model="age"
            type="digit"
            name="Age"
            label="Age"
            placeholder="Enter age"
            :rules="[{ required: true, message: 'Age is required' }]"
          />
          
          <!-- Mode Display -->
          <div class="mode-display" v-if="age">
            Current Mode: <span :class="mode">{{ modeText }}</span>
          </div>

          <!-- Age Reference -->
          <div class="age-reference">
            <div class="ref-card teenager">
              <div class="ref-icon">🏃</div>
              <div class="ref-title">Teenager</div>
              <div class="ref-range">12 - 18</div>
            </div>
            <div class="ref-card adult">
              <div class="ref-icon">💪</div>
              <div class="ref-title">Adult</div>
              <div class="ref-range">19 - 45</div>
            </div>
            <div class="ref-card senior">
              <div class="ref-icon">🧘</div>
              <div class="ref-title">Senior</div>
              <div class="ref-range">46+</div>
            </div>
          </div>
          
        </van-cell-group>
        <div style="margin: 16px;">
          <van-button round block type="primary" native-type="submit" :loading="loading">
            Register & Login
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  min-height: 100vh;
  background-color: #f7f8fa;
}
.content {
  padding-top: 20px;
}
.mode-display {
  padding: 10px 16px;
  font-size: 14px;
  color: #666;
  text-align: center;
}
.TEENAGER { color: #07c160; font-weight: bold; }
.ADULT { color: #1989fa; font-weight: bold; }
.SENIOR { color: #ff976a; font-weight: bold; }

.age-reference {
  display: flex;
  justify-content: space-between;
  padding: 10px 16px;
  gap: 10px;
}

.ref-card {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 10px 5px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border: 1px solid #f0f0f0;
}

.ref-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.ref-title {
  font-size: 12px;
  font-weight: bold;
  color: #333;
  margin-bottom: 2px;
}

.ref-range {
  font-size: 10px;
  color: #999;
}

.ref-card.teenager { border-bottom: 3px solid #07c160; }
.ref-card.adult { border-bottom: 3px solid #1989fa; }
.ref-card.senior { border-bottom: 3px solid #ff976a; }
</style>
