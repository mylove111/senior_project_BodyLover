<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showDialog } from 'vant';
import api from '../api/request';
import { useUserStore } from '../stores/user';
import confetti from 'canvas-confetti';

const router = useRouter();
const userStore = useUserStore();
const activeTab = ref('daily');
const familyBadge = ref('');
const newCompletions = ref([]);

const playSuccessSound = () => {
    const audio = new Audio('http://commondatastorage.googleapis.com/codeskulptor-assets/week7-brrring.m4a');
    audio.play().catch(e => console.log('Audio play failed', e));
};


// Plans (Daily Routine)
const plans = ref([]);
const showAddPlan = ref(false);
const newPlan = ref({
  title: '',
  content: '',
  durationHours: '',
  planType: 'EXERCISE'
});

// Diet Suggestions (Mock)
const todaysMeal = ref({
    breakfast: 'Oatmeal & Boiled Egg',
    lunch: 'Steamed Fish & Tofu',
    dinner: 'Vegetable Soup'
});

// Family Data
const familyMembers = ref([]);
const pendingRequests = ref([]);
const showAddFamily = ref(false);
const targetUsername = ref('');
const selectedRelation = ref('GRANDFATHER_GRANDSON'); // Default meaningful for senior

// Date
const currentDate = ref(new Date());
const formatDate = (date) => {
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
};

const fetchPlans = async () => {
    try {
        const dateStr = formatDate(currentDate.value);
        const response = await api.get('/plans', {
            params: {
                userId: userStore.userInfo?.id,
                date: dateStr
            }
        });
        if (response.data.code === 200) {
            plans.value = response.data.data;
        }
    } catch (error) {
        // Silent fail or simple toast
    }
};

const onAddPlan = async () => {
    if (!userStore.userInfo?.id) return;
    try {
        await api.post('/plans', {
            userId: userStore.userInfo.id,
            ...newPlan.value,
            scheduledDate: formatDate(currentDate.value)
        });
        showAddPlan.value = false;
        fetchPlans();
        showToast('Added Successfully');
    } catch (error) {
        showToast('Error');
    }
};

const completePlan = async (plan) => {
    try {
        await api.put(`/plans/${plan.id}`, { status: 'COMPLETED' });
        plan.status = 'COMPLETED';
        showToast('Well done!');
    } catch (error) {
        showToast('Error');
    }
};

// Family Logic
const fetchFamilyData = async () => {
  try {
    const dateStr = formatDate(currentDate.value);
    const membersRes = await api.get('/family/members', { params: { userId: userStore.userInfo?.id, date: dateStr } });
    if (membersRes.data.code === 200) {
        familyMembers.value = membersRes.data.data;
        checkFamilyNotifications();
    }

    const requestsRes = await api.get('/family/requests', { params: { userId: userStore.userInfo?.id } });
    if (requestsRes.data.code === 200) pendingRequests.value = requestsRes.data.data;
  } catch (error) {
    console.error(error);
  }
};

const checkFamilyNotifications = () => {
    newCompletions.value = [];
    familyBadge.value = '';

    familyMembers.value.forEach(member => {
        if (member.progress === 100) {
            newCompletions.value.push(member);
            familyBadge.value = 'dot';
        }
    });
};

import { watch } from 'vue';
watch(activeTab, (newVal) => {
    if (newVal === 'family' && newCompletions.value.length > 0) {
        familyBadge.value = '';
        const message = newCompletions.value.map(m => `${m.relationDisplay} ${m.username}`).join(', ');

        playSuccessSound();
        confetti({
            particleCount: 150,
            spread: 70,
            origin: { y: 0.6 },
            scalar: 1.2
        });

        showDialog({
            title: '🎉 Great News!',
            message: `${message} has finished all tasks today!\n\nLet's stay active together! ❤️`,
            theme: 'round-button',
            confirmButtonColor: '#ff6f00',
            confirmButtonText: 'Wonderful!'
        });
    }
});

const onAddFamily = async () => {
    if (!targetUsername.value) {
        showToast('Please enter username');
        return;
    }
    try {
        const res = await api.post('/family/request', {
            requesterId: userStore.userInfo.id,
            targetUsername: targetUsername.value,
            relationType: selectedRelation.value
        });
        if (res.data.code === 200) {
            showToast('Request sent');
            showAddFamily.value = false;
            targetUsername.value = '';
        } else {
            showToast(res.data.message || 'Failed');
        }
    } catch (error) {
        showToast('User not found or error');
    }
};

const handleFamilyRequest = async (id, status) => {
    try {
        const res = await api.put(`/family/request/${id}`, { status });
        if (res.data.code === 200) {
            showToast(status === 'ACCEPTED' ? 'Accepted' : 'Rejected');
            fetchFamilyData();
        }
    } catch (error) {
        showToast('Operation failed');
    }
};

const logout = () => {
    userStore.logout();
    router.push('/login');
};

onMounted(() => {
    if (!userStore.isLoggedIn) {
        router.push('/login');
        return;
    }
    fetchPlans();
    fetchFamilyData();
});
</script>

<template>
  <div class="senior-dashboard">
    <div class="content">
      
      <!-- Daily Tab -->
      <div v-if="activeTab === 'daily'">
        <div class="large-header">
           <h2>🌞 Good Morning, {{ userStore.userInfo?.username }}</h2>
           <p class="date-display">{{ formatDate(currentDate) }}</p>
        </div>

        <div class="action-card" @click="showAddPlan = true">
            <van-icon name="plus" size="40" color="#ff6f00" />
            <span>Add Activity</span>
        </div>

        <div class="section-title">Today's Tasks</div>
        
        <van-empty v-if="plans.length === 0" description="No tasks for today." image-size="100" />

        <div class="card-list">
             <div v-for="plan in plans" :key="plan.id" class="big-card" :class="{ completed: plan.status === 'COMPLETED' }">
                <div class="card-left">
                    <h3>{{ plan.title }}</h3>
                    <p>{{ plan.content }}</p>
                </div>
                <div class="card-right">
                    <van-button v-if="plan.status !== 'COMPLETED'" round type="primary" size="normal" color="#ff8f00" @click="completePlan(plan)">Finish</van-button>
                    <van-icon v-else name="checked" size="40" color="#4caf50" />
                </div>
             </div>
        </div>
      </div>

      <!-- Diet Tab -->
      <div v-if="activeTab === 'diet'">
         <div class="large-header"><h2>🥗 Healthy Eating</h2></div>
         
         <div class="meal-card">
             <div class="meal-title">Breakfast</div>
             <div class="meal-content">{{ todaysMeal.breakfast }}</div>
         </div>
         <div class="meal-card">
             <div class="meal-title">Lunch</div>
             <div class="meal-content">{{ todaysMeal.lunch }}</div>
         </div>
         <div class="meal-card">
             <div class="meal-title">Dinner</div>
             <div class="meal-content">{{ todaysMeal.dinner }}</div>
         </div>
      </div>

      <!-- Family Tab -->
      <div v-if="activeTab === 'family'">
         <div class="large-header">
             <h2>❤️ My Family</h2>
             <van-button size="normal" color="#ff6f00" icon="plus" round @click="showAddFamily = true" style="margin-top: 10px;">Add Family</van-button>
         </div>
         
         <!-- Pending Requests -->
        <div v-if="pendingRequests.length > 0" class="section">
          <div class="section-title">New Messages</div>
          <van-cell-group inset>
            <van-cell v-for="req in pendingRequests" :key="req.id" :title="req.requesterName" :label="'Says: ' + req.relationDisplay" size="large">
              <template #right-icon>
                <div style="display: flex; gap: 10px;">
                    <van-button size="small" type="success" @click="handleFamilyRequest(req.id, 'ACCEPTED')">Accept</van-button>
                    <van-button size="small" type="danger" @click="handleFamilyRequest(req.id, 'REJECTED')">Reject</van-button>
                </div>
              </template>
            </van-cell>
          </van-cell-group>
        </div>

         <van-empty v-if="familyMembers.length === 0" description="No family members yet" image-size="100" />
         
         <div v-else class="card-list">
             <div v-for="member in familyMembers" :key="member.id" class="big-card" style="background: white; align-items: center;">
                 <div style="display: flex; align-items: center;">
                     <img :src="`https://api.dicebear.com/7.x/miniavs/svg?seed=${member.username}`" style="width: 60px; height: 60px; border-radius: 50%; border: 3px solid #ffcc80; margin-right: 15px;" />
                     <div>
                         <h3 style="margin: 0; color: #e65100; font-size: 24px;">{{ member.username }}</h3>
                         <p style="margin: 5px 0 0 0; color: #ff6f00; font-weight: bold; font-size: 18px;">{{ member.relationDisplay }}</p>
                     </div>
                 </div>
                 <div style="text-align: right; min-width: 100px;">
                     <van-tag type="warning" size="large" style="margin-bottom: 8px;">{{ member.mode }}</van-tag>
                     <div style="font-size: 14px; color: #8d6e63;">Done: {{ member.progress }}%</div>
                     <van-progress :percentage="member.progress" stroke-width="8" :show-pivot="false" color="#ff9800" track-color="#fff3e0" style="width: 100px; margin-top: 4px;" />
                 </div>
             </div>
         </div>
      </div>

      <!-- Me Tab -->
      <div v-if="activeTab === 'me'">
         <div class="large-header"><h2>👤 Profile</h2></div>
         
         <div class="big-card" style="justify-content: center; background: #fff8e1;">
             <h3>{{ userStore.userInfo?.username }}</h3>
         </div>

         <van-button block round size="large" color="#d32f2f" style="margin-top: 30px;" @click="logout"> Log Out </van-button>
      </div>

    </div>

    <!-- Add Dialog -->
    <van-dialog v-model:show="showAddPlan" title="Add New Activity" show-cancel-button @confirm="onAddPlan">
        <van-form>
            <van-cell-group>
                <van-field v-model="newPlan.title" label="What?" placeholder="Walk, Tai Chi..." size="large" />
                <van-field v-model="newPlan.durationHours" label="How long?" placeholder="Hours" type="number" size="large" />
            </van-cell-group>
        </van-form>
    </van-dialog>

    <!-- Add Family Dialog (Simplified for seniors) -->
    <van-dialog 
      v-model:show="showAddFamily" 
      title="Add Family" 
      show-cancel-button 
      confirm-button-text="Confirm"
      cancel-button-text="Cancel"
      @confirm="onAddFamily"
    >
      <van-form>
        <van-cell-group inset>
          <van-field v-model="targetUsername" label="Username" placeholder="Enter name" size="large" />
          
          <div style="padding: 10px 16px; font-size: 16px; color: #333;">Choose Relationship:</div>
          <van-radio-group v-model="selectedRelation">
            <van-cell-group inset>
              <van-cell title="I am Grandfather (He/She is Grandchild)" clickable @click="selectedRelation = 'GRANDFATHER_GRANDSON'">
                <template #right-icon>
                  <van-radio name="GRANDFATHER_GRANDSON" />
                </template>
              </van-cell>
              <van-cell title="I am Grandmother (He/She is Grandchild)" clickable @click="selectedRelation = 'GRANDMOTHER_GRANDSON'">
                <template #right-icon>
                  <van-radio name="GRANDMOTHER_GRANDSON" />
                </template>
              </van-cell>
              <van-cell title="I am Father (He/She is Child)" clickable @click="selectedRelation = 'FATHER_SON'">
                <template #right-icon>
                  <van-radio name="FATHER_SON" />
                </template>
              </van-cell>
              <van-cell title="I am Mother (He/She is Child)" clickable @click="selectedRelation = 'MOTHER_SON'">
                <template #right-icon>
                  <van-radio name="MOTHER_SON" />
                </template>
              </van-cell>
            </van-cell-group>
          </van-radio-group>

        </van-cell-group>
      </van-form>
    </van-dialog>

    <van-tabbar v-model="activeTab" active-color="#ff6f00" inactive-color="#8d6e63" style="height: 60px;">
      <van-tabbar-item name="daily" icon="notes-o">Daily</van-tabbar-item>
      <van-tabbar-item name="diet" icon="shop-o">Meals</van-tabbar-item>
      <van-tabbar-item name="family" icon="friends-o" :dot="!!familyBadge">Family</van-tabbar-item>
      <van-tabbar-item name="me" icon="user-o">Me</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<style scoped>
.senior-dashboard {
    min-height: 100vh;
    background: #fff3e0; /* Warm background */
    padding-bottom: 90px;
}
.content { padding: 20px; }

.large-header {
    text-align: center;
    margin-bottom: 30px;
}
.large-header h2 {
    font-size: 32px;
    color: #e65100;
    margin: 0;
}
.date-display {
    font-size: 20px;
    color: #ff6f00;
}

.action-card {
    background: white;
    border-radius: 15px;
    padding: 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    box-shadow: 0 4px 10px rgba(0,0,0,0.1);
    margin-bottom: 30px;
    border: 2px solid #ffcc80;
}
.action-card span {
    font-size: 20px;
    font-weight: bold;
    color: #e65100;
    margin-top: 10px;
}

.section-title {
    font-size: 24px;
    font-weight: bold;
    color: #bf360c;
    margin-bottom: 20px;
}

.big-card {
    background: white;
    border-radius: 15px;
    padding: 20px;
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}
.big-card.completed {
    opacity: 0.7;
    background: #e8f5e9;
}

.card-left h3 {
    font-size: 24px;
    margin: 0 0 8px 0;
    color: #3e2723;
}
.card-left p {
    font-size: 18px;
    color: #5d4037;
    margin: 0;
}

/* Diet Cards */
.meal-card {
    background: white;
    padding: 20px;
    border-radius: 15px;
    margin-bottom: 15px;
    border-left: 8px solid #8bc34a;
}
.meal-title {
    font-size: 20px;
    font-weight: bold;
    color: #33691e;
    margin-bottom: 5px;
}
.meal-content {
    font-size: 18px;
    color: #558b2f;
}

/* Override Tabbar Size for Accessibility */
:deep(.van-tabbar-item__icon) {
    font-size: 32px;
}
:deep(.van-tabbar-item__text) {
    font-size: 18px;
}
</style>
