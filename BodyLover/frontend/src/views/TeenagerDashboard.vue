<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showDialog } from 'vant';
import api from '../api/request';
import { useUserStore } from '../stores/user';
import ActivityChart from '../components/ActivityChart.vue';

const router = useRouter();
const userStore = useUserStore();
const activeTab = ref('plans');

// Chart Data (Mock for now, can be real later)
const chartData = ref([
  { date: 'Mon', value: 1.5 },
  { date: 'Tue', value: 2.0 },
  { date: 'Wed', value: 0.5 },
  { date: 'Thu', value: 3.0 },
  { date: 'Fri', value: 1.0 },
  { date: 'Sat', value: 4.0 },
  { date: 'Sun', value: 2.5 }
]);

// Plans Data
const plans = ref([]);
const showAddPlan = ref(false);
const newPlan = ref({
  title: '',
  content: '',
  durationHours: '',
  planType: 'EXERCISE'
});

// Health Data
const healthRecords = ref([]);
const showAddRecord = ref(false);
const newRecord = ref({
  height: '',
  weight: ''
});

// Family Data
const familyMembers = ref([]);
const pendingRequests = ref([]);
const showAddFamily = ref(false);
const targetUsername = ref('');

const selectedRecord = ref(null);

const displayRecord = computed(() => {
  return selectedRecord.value || (healthRecords.value.length > 0 ? healthRecords.value[0] : null);
});

const bmiStatus = computed(() => {
  if (!displayRecord.value) return '';
  const bmi = displayRecord.value.bmi;
  if (bmi < 18.5) return 'Underweight';
  if (bmi < 24.9) return 'Normal';
  if (bmi < 29.9) return 'Overweight';
  if (bmi < 34.9) return 'Obesity Class I';
  if (bmi < 39.9) return 'Obesity Class II';
  return 'Obesity Class III';
});

const selectRecord = (record) => {
  selectedRecord.value = record;
  showToast('Viewing record from ' + record.recordDate);
  // Optional: Scroll to top smoothly
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

const suggestions = computed(() => {
  const status = bmiStatus.value;
  if (!status) return null;

  const map = {
    'Underweight': {
      exercise: 'Focus on resistance training to build muscle mass. Avoid excessive cardio.',
      food: 'These foods not only help control weight but also provide necessary nutrients to help maintain good health.',
      foodItems: [
        { 
          name: 'Green Leafy Vegetables', 
          desc: 'Spinach, lettuce, kale. Rich in fiber & vitamins, low calorie.', 
          image: '/images/Underweight1.jpg' 
        },
        { 
          name: 'Lean Meat', 
          desc: 'Chicken breast, lean beef. Low fat, high protein.', 
          image: '/images/Underweight1_2.jpg' 
        },
        { 
          name: 'Low Sugar Fruits', 
          desc: 'Apples, strawberries, blueberries. High fiber alternative to snacks.', 
          image: '/images/Underweight1_3.jpg' 
        },
        { 
          name: 'Nuts and Seeds', 
          desc: 'Cashews, almonds, peanuts. Energy, healthy fats & fiber.', 
          image: '/images/Underweight1_4.jpg' 
        }
      ]
    },
    'Normal': {
      exercise: 'Maintain a balanced routine: 150 mins of moderate aerobic activity or 75 mins of vigorous activity per week.',
      food: 'Maintain a balanced diet with appropriate portions of the following food groups to keep your body healthy.',
      foodItems: [
        { 
          name: 'Cereals & Potatoes', 
          desc: 'Daily 150-300g. Whole grains and mixed beans help maintain normal weight.', 
          image: '/images/Normal2_1.jpg' 
        },
        { 
          name: 'Vegetables & Fruits', 
          desc: 'Veg 300-500g, Fruit ~200g daily. Limit high-sugar fruits.', 
          image: '/images/Normal2_2.jpg' 
        },
        { 
          name: 'Meat, Seafood & Eggs', 
          desc: 'High protein, low fat. Fish & shrimp are rich in unsaturated fatty acids.', 
          image: '/images/Normal2_3.jpg' 
        }
      ]
    },
    'Overweight': {
      exercise: 'Increase daily activity. Aim for 30-60 mins of moderate cardio (brisk walking, swimming) most days.',
      food: 'Choosing these foods helps overweight individuals achieve healthy weight loss while meeting nutritional needs.',
      foodItems: [
        { 
          name: 'Whole Grains', 
          desc: '50-150g daily. Rich in fiber & B vitamins. Reduces disease risk.', 
          image: '/images/Overweight3_1.jpg' 
        },
        { 
          name: 'Dark Vegetables', 
          desc: 'Spinach, carrots, etc. 300-500g daily. Rich in vitamins & minerals.', 
          image: '/images/Underweight1.jpg' 
        },
        { 
          name: 'High Quality Protein', 
          desc: 'Fish, shrimp. Low fat, high unsaturated fatty acids. 280-525g weekly.', 
          image: '/images/Normal2_3.jpg' 
        },
        { 
          name: 'Low Sugar Fruits', 
          desc: 'Apples, pears. Soluble fiber helps regulate intestinal function.', 
          image: '/images/Normal2_2.jpg' 
        },
        { 
          name: 'Low Fat Dairy', 
          desc: 'Low fat or skim milk. Provides quality protein and calcium.', 
          image: '/images/Overweight3.jpg' 
        }
      ]
    },
    'Obesity Class I': {
      exercise: 'Low-impact cardio to protect joints: Swimming, water aerobics, or stationary cycling.',
      food: 'These foods help increase satiety and control calorie intake, which is beneficial for weight management.',
      foodItems: [
        { 
          name: 'Oats', 
          desc: 'Rich in fiber & protein. Provides lasting satiety, reduces snack cravings.', 
          image: '/images/Obesity_ClassI4_1.jpg' 
        },
        { 
          name: 'Vegetables', 
          desc: 'Low calorie, high fiber. Rich in nutrients & antioxidants.', 
          image: '/images/Underweight1.jpg' 
        },
        { 
          name: 'Chicken Breast', 
          desc: 'Low fat, high protein. Helps maintain satiety & control calories.', 
          image: '/images/Obesity_ClassI4_2.jpg' 
        },
        { 
          name: 'Fish', 
          desc: 'Quality protein & unsaturated fats. Promotes metabolism, reduces fat.', 
          image: '/images/Obesity_ClassI4_3.jpg' 
        },
        { 
          name: 'Beans', 
          desc: 'Rich in fiber, low calorie. Increases satiety, good for weight control.', 
          image: '/images/Obesity_ClassI4_4.jpg' 
        }
      ]
    },
    'Obesity Class II': {
      exercise: 'Start slow with walking or chair exercises. Focus on consistency rather than intensity.',
      food: 'Choosing these foods helps control total energy intake and maintain a reasonable diet for weight loss.',
      foodItems: [
        { 
          name: 'Whole Grains & Beans', 
          desc: '50-150g daily. Rich in B vitamins, minerals & fiber.', 
          image: '/images/Overweight3_1.jpg' 
        },
        { 
          name: 'Vegetables & Fruits', 
          desc: 'Veg 300-500g (dark veg > 1/2), Fruit ~200g. Limit high sugar.', 
          image: '/images/Normal2_2.jpg' 
        },
        { 
          name: 'High Protein Meat', 
          desc: '40-75g daily. Fish, shrimp, crab, shellfish. Low fat.', 
          image: '/images/Normal2_3.jpg' 
        },
        { 
          name: 'Low Fat Dairy', 
          desc: 'Low fat or skim milk. Helps control weight.', 
          image: '/images/Overweight3_2.jpg' 
        }
      ]
    },
    'Obesity Class III': {
      exercise: 'Gentle movement under professional supervision if possible. Simple stretching and short walks.',
      food: 'Choosing these foods helps maintain normal weight, delay weight gain, and reduce the risk of all-cause mortality, type 2 diabetes, cardiovascular disease, etc.',
      foodItems: [
        { 
          name: 'Whole Grains & Beans', 
          desc: '50-150g daily. Rich in carbs, fiber & B vitamins.', 
          image: '/images/Overweight3_1.jpg' 
        },
        { 
          name: 'Dark Vegetables', 
          desc: '300-500g daily. Spinach, carrots, purple cabbage, etc.', 
          image: '/images/Underweight1.jpg' 
        },
        { 
          name: 'High Protein Meat', 
          desc: 'Fish, shrimp, shellfish. At least twice a week (280-525g total).', 
          image: '/images/Normal2_3.jpg' 
        },
        { 
          name: 'Soy Products', 
          desc: 'Tofu, soy milk. 15-25g soy daily. Avoid fried & salty ones.', 
          image: '/images/Obesity_ClassIII5_1.jpg' 
        }
      ]
    }
  };
  
  return map[status] || null;
});

const getBmiClass = (bmi) => {
  if (bmi < 18.5) return 'Underweight';
  if (bmi < 24.9) return 'Normal';
  if (bmi < 29.9) return 'Overweight';
  return 'Obesity';
};

// Fetch Plans
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
    showToast('Failed to load plans');
  }
};

// Fetch Health Records
const fetchHealthRecords = async () => {
  try {
    const res = await api.get('/health', {
      params: { userId: userStore.userInfo?.id }
    });
    if (res.data.code === 200) {
      healthRecords.value = res.data.data;
      selectedRecord.value = null; // Reset to show latest
    }
  } catch (error) {
    console.error(error);
  }
};

// Fetch Family Data
const fetchFamilyData = async () => {
  try {
    const membersRes = await api.get('/family/members', { params: { userId: userStore.userInfo?.id } });
    if (membersRes.data.code === 200) familyMembers.value = membersRes.data.data;

    const requestsRes = await api.get('/family/requests', { params: { userId: userStore.userInfo?.id } });
    if (requestsRes.data.code === 200) pendingRequests.value = requestsRes.data.data;
  } catch (error) {
    console.error(error);
  }
};

// Add Family Request
const onAddFamily = async () => {
  if (!targetUsername.value) {
    showToast('Please enter username');
    return;
  }
  try {
    const res = await api.post('/family/request', {
      requesterId: userStore.userInfo.id,
      targetUsername: targetUsername.value
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

// Handle Request
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

// Add Plan
const onAddPlan = async () => {
  if (!userStore.userInfo?.id) return;
  try {
    await api.post('/plans', {
      userId: userStore.userInfo.id,
      ...newPlan.value,
      scheduledDate: formatDate(currentDate.value)
    });
    showToast('Plan added');
    showAddPlan.value = false;
    fetchPlans();
    newPlan.value = { title: '', content: '', durationHours: '', planType: 'EXERCISE' };
  } catch (error) {
    showToast('Failed to add plan');
  }
};


// Add Health Record
const onAddRecord = async () => {
  if (!newRecord.value.height || !newRecord.value.weight) {
    showToast('Please fill all fields');
    return;
  }
  try {
    const res = await api.post('/health', {
      userId: userStore.userInfo.id,
      ...newRecord.value
    });
    if (res.data.code === 200) {
      showToast('Record added');
      showAddRecord.value = false;
      newRecord.value = { height: '', weight: '' };
      fetchHealthRecords();
    }
  } catch (error) {
    showToast('Add failed');
  }
};

// Complete Plan
const completePlan = async (plan) => {
  try {
    await api.put(`/plans/${plan.id}`, { status: 'COMPLETED' });
    plan.status = 'COMPLETED';
    showToast('Task completed!');
  } catch (error) {
    showToast('Operation failed');
  }
};

const logout = () => {
  userStore.logout();
  router.push('/login');
};

// Timer State
const showTimer = ref(false);
const isTimerRunning = ref(false);
const remainingSeconds = ref(0);
const totalSeconds = ref(0);
const activePlan = ref(null);
let timerInterval = null;

// Format Time MM:SS
const formatTime = (seconds) => {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
};

// Date Management
const currentDate = ref(new Date());
const showCalendar = ref(false);
const formatDate = (date) => {
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
};
const currentDateStr = computed(() => formatDate(currentDate.value));

const onConfirmDate = (value) => {
  showCalendar.value = false;
  currentDate.value = value;
  fetchPlans();
};

// Start Plan
const startPlan = (plan) => {
  if (!plan.durationHours) {
    showToast('Invalid duration');
    return;
  }
  activePlan.value = plan;
  totalSeconds.value = Math.floor(plan.durationHours * 3600);
  remainingSeconds.value = totalSeconds.value;
  showTimer.value = true;
  isTimerRunning.value = true;
  
  startInterval();
};

const startInterval = () => {
  clearInterval(timerInterval);
  timerInterval = setInterval(() => {
    if (remainingSeconds.value > 0) {
      remainingSeconds.value--;
    } else {
      // Timer Finished
      finishTimer(true);
    }
  }, 1000);
};

const pauseTimer = () => {
  isTimerRunning.value = false;
  clearInterval(timerInterval);
};

const resumeTimer = () => {
  isTimerRunning.value = true;
  startInterval();
};

const stopTimerEarly = () => {
  showDialog({
    title: 'End Workout?',
    message: 'Do you want to save the progress so far?',
    showCancelButton: true,
    confirmButtonText: 'Save & End',
    cancelButtonText: 'Continue'
  }).then(() => {
    finishTimer(false);
  }).catch(() => {
    // Resume
  });
};

const finishTimer = async (completed) => {
  clearInterval(timerInterval);
  showTimer.value = false;
  isTimerRunning.value = false;
  
  // Calculate actual minutes spent
  const spentSeconds = totalSeconds.value - remainingSeconds.value;
  const actualMinutes = Math.ceil(spentSeconds / 60);

  if (actualMinutes < 1 && !completed) {
    showToast('Too short to save');
    return;
  }

  try {
    await api.put(`/plans/${activePlan.value.id}`, { 
      status: 'COMPLETED',
      actualMinutes: actualMinutes
    });
    activePlan.value.status = 'COMPLETED';
    showToast(`Completed! Recorded ${actualMinutes} mins`);
    fetchPlans(); // Refresh list
    // Refresh chart if needed (we need to implement chart refresh logic)
  } catch (error) {
    showToast('Failed to save');
  }
};

onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.push('/login');
    return;
  }
  fetchPlans();
  fetchHealthRecords();
  fetchFamilyData();
});
</script>

<template>
  <div class="teenager-dashboard">
    <!-- Content Area -->
    <div class="content">
      
      <!-- Plans Tab -->
      <div v-if="activeTab === 'plans'">
        <div class="header-action">
          <h2>Plans for {{ currentDateStr }}</h2>
          <van-button size="small" type="primary" icon="plus" @click="showAddPlan = true">Add Plan</van-button>
        </div>

        <!-- Chart Section -->
        <ActivityChart :data="chartData" title="Weekly Activity Trend" />

        <van-empty v-if="plans.length === 0" description="No plans yet, add one!" />

        <div class="plan-list">
          <van-cell-group inset v-for="plan in plans" :key="plan.id" class="plan-card">
            <van-cell :title="plan.title" :label="plan.content">
              <template #right-icon>
                <van-tag v-if="plan.status === 'COMPLETED'" type="success">Completed</van-tag>
                <div v-else>
                  <van-button size="mini" type="primary" @click="startPlan(plan)" style="margin-right: 5px">Start</van-button>
                  <van-button size="mini" type="success" @click="completePlan(plan)">Done</van-button>
                </div>
              </template>
            </van-cell>
            <van-cell title="Duration" :value="plan.durationHours + ' Hours'" />
          </van-cell-group>
        </div>
      </div>

      <!-- Health Tab -->
      <div v-if="activeTab === 'health'">
        <div class="header-action">
          <h2>Health Records</h2>
          <van-button size="small" type="primary" icon="plus" @click="showAddRecord = true">Add Record</van-button>
        </div>

        <!-- BMI Card -->
        <div v-if="displayRecord" class="bmi-card">
          <div class="bmi-value">{{ displayRecord.bmi }}</div>
          <div class="bmi-label">Current BMI ({{ displayRecord.recordDate }})</div>
          <div class="bmi-status" :class="bmiStatus">{{ bmiStatus }}</div>
          <div class="bmi-details">
            <span>Height: {{ displayRecord.height }}cm</span>
            <span>Weight: {{ displayRecord.weight }}kg</span>
          </div>
        </div>
        <van-empty v-else description="No records yet" />

        <!-- Suggestions Section -->
        <div v-if="suggestions" class="section suggestions-card">
          <h3>💡 Health Advice</h3>
          
          <div class="suggestion-item">
            <h4>Recommended Exercise</h4>
            <p>{{ suggestions.exercise }}</p>
          </div>

          <div class="suggestion-item">
            <h4>Recommended Food</h4>
            <p>{{ suggestions.food }}</p>
            
            <!-- Single Image (Legacy) -->
            <img v-if="suggestions.foodImage" :src="suggestions.foodImage" alt="Food Suggestion" class="food-image" />

            <!-- Rich Food List (New) -->
            <div v-if="suggestions.foodItems" class="food-grid">
              <div v-for="(item, index) in suggestions.foodItems" :key="index" class="food-card">
                <img :src="item.image" :alt="item.name" class="food-card-img" />
                <div class="food-card-info">
                  <h5>{{ item.name }}</h5>
                  <p>{{ item.desc }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- History Table -->
        <h3 v-if="healthRecords.length > 0">History</h3>
        <div v-if="healthRecords.length > 0" class="history-table-container">
          <table class="history-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Height</th>
                <th>Weight</th>
                <th>BMI</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr 
                v-for="record in healthRecords" 
                :key="record.id" 
                @click="selectRecord(record)"
                :class="{ active: selectedRecord && selectedRecord.id === record.id }"
              >
                <td>{{ record.recordDate }}</td>
                <td>{{ record.height }}</td>
                <td>{{ record.weight }}</td>
                <td>
                  <span class="bmi-tag" :class="getBmiClass(record.bmi)">{{ record.bmi }}</span>
                </td>
                <td><van-icon name="eye-o" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Family Tab -->
      <div v-if="activeTab === 'family'">
        <div class="header-action">
          <h2>Family Connect</h2>
          <van-button size="small" type="primary" icon="plus" @click="showAddFamily = true">Add Family</van-button>
        </div>

        <!-- Pending Requests -->
        <div v-if="pendingRequests.length > 0" class="section">
          <h3>New Requests</h3>
          <van-cell-group inset>
            <van-cell v-for="req in pendingRequests" :key="req.id" :title="req.requesterName" :label="'Mode: ' + req.requesterMode">
              <template #right-icon>
                <van-button size="mini" type="success" @click="handleFamilyRequest(req.id, 'ACCEPTED')">Accept</van-button>
                <van-button size="mini" type="danger" @click="handleFamilyRequest(req.id, 'REJECTED')" style="margin-left: 5px">Reject</van-button>
              </template>
            </van-cell>
          </van-cell-group>
        </div>

        <!-- Family Members -->
        <div class="section">
          <h3>My Family</h3>
          <van-empty v-if="familyMembers.length === 0" description="No family members yet" />
          <van-cell-group inset v-else>
            <van-cell v-for="member in familyMembers" :key="member.id" :title="member.username" :label="member.mode + ' Mode'" icon="user-o" />
          </van-cell-group>
        </div>
      </div>

      <!-- Me Tab -->
      <div v-if="activeTab === 'me'">
        <div class="header-action">
          <h2>Personal Center</h2>
        </div>

        <!-- User Info Card -->
        <div class="user-card">
          <div class="avatar-placeholder">{{ userStore.userInfo?.username?.charAt(0).toUpperCase() }}</div>
          <div class="user-info">
            <h3>{{ userStore.userInfo?.username }}</h3>
            <van-tag type="primary" size="medium">{{ userStore.userInfo?.mode }} Mode</van-tag>
          </div>
        </div>

        <!-- Date Management -->
        <div class="section">
          <h3>📅 Date Management</h3>
          <van-cell title="Current Date" :value="currentDateStr" is-link @click="showCalendar = true" />
          <p class="hint-text">Select a date to view or add plans for that specific day.</p>
        </div>

        <!-- Settings -->
        <div class="section">
          <h3>⚙️ Settings</h3>
          <van-button type="danger" block @click="logout">Logout</van-button>
        </div>
      </div>

    </div>

    <!-- Add Plan Dialog -->
    <van-dialog 
      v-model:show="showAddPlan" 
      title="Add Workout Plan" 
      show-cancel-button 
      confirm-button-text="Confirm"
      cancel-button-text="Cancel"
      @confirm="onAddPlan"
    >
      <van-form>
        <van-cell-group inset>
          <van-field v-model="newPlan.title" label="Title" placeholder="e.g. Morning Run" />
          <van-field v-model="newPlan.content" label="Content" placeholder="Details..." type="textarea" />
          <van-field v-model="newPlan.durationHours" label="Duration (Hours)" type="number" placeholder="0.5" />
        </van-cell-group>
      </van-form>
    </van-dialog>

    <!-- Add Health Record Dialog -->
    <van-dialog 
      v-model:show="showAddRecord" 
      title="Add Health Record" 
      show-cancel-button 
      confirm-button-text="Confirm"
      cancel-button-text="Cancel"
      @confirm="onAddRecord"
    >
      <van-form>
        <van-cell-group inset>
          <van-field v-model="newRecord.height" label="Height (cm)" type="number" placeholder="e.g. 175" />
          <van-field v-model="newRecord.weight" label="Weight (kg)" type="number" placeholder="e.g. 65" />
        </van-cell-group>
      </van-form>
    </van-dialog>

    <!-- Add Family Dialog -->
    <van-dialog 
      v-model:show="showAddFamily" 
      title="Add Family Member" 
      show-cancel-button 
      cancel-button-text="Cancel"
      @confirm="onAddFamily"
    >
      <van-form>
        <van-cell-group inset>
          <van-field v-model="targetUsername" label="Username" placeholder="Enter family's username" />
        </van-cell-group>
      </van-form>
    </van-dialog>

    <!-- Calendar -->
    <van-calendar v-model:show="showCalendar" @confirm="onConfirmDate" color="#4caf50" />

    <!-- Focus Timer Overlay -->
    <van-overlay :show="showTimer" @click.stop>
      <div class="timer-wrapper">
        <div class="timer-circle">
          <div class="timer-time">{{ formatTime(remainingSeconds) }}</div>
          <div class="timer-label">Focusing...</div>
        </div>
        
        <div class="timer-controls">
          <van-button 
            round 
            type="warning" 
            icon="pause-circle-o" 
            size="large" 
            v-if="isTimerRunning"
            @click="pauseTimer"
          >
            Pause
          </van-button>
          <van-button 
            round 
            type="success" 
            icon="play-circle-o" 
            size="large" 
            v-else
            @click="resumeTimer"
          >
            Resume
          </van-button>
          
          <van-button 
            round 
            type="danger" 
            icon="stop-circle-o" 
            size="large" 
            class="stop-btn"
            @click="stopTimerEarly"
          >
            End & Save
          </van-button>
        </div>
      </div>
    </van-overlay>

    <!-- Tabbar -->
    <van-tabbar v-model="activeTab">
      <van-tabbar-item name="plans" icon="todo-list-o">Plans</van-tabbar-item>
      <van-tabbar-item name="health" icon="chart-trending-o">Health</van-tabbar-item>
      <van-tabbar-item name="family" icon="friends-o">Family</van-tabbar-item>
      <van-tabbar-item name="me" icon="user-o">Me</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<style scoped>
.teenager-dashboard {
  min-height: 100vh;
  /* Fresh Green Gradient for Youth/Energy */
  background: linear-gradient(135deg, #e8f5e9 0%, #b9f6ca 100%);
  padding-bottom: 80px;
  padding-top: 20px;
}

.content {
  padding: 20px;
}

.header-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-action h2 {
  font-size: 24px;
  font-weight: 700;
  color: #2e7d32; /* Dark Green */
  margin: 0;
  background: linear-gradient(45deg, #2e7d32, #00c853);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* Cards */
.plan-card, .section {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 10px 20px rgba(76, 175, 80, 0.1); /* Greenish shadow */
  margin-bottom: 20px;
  overflow: hidden;
  transition: transform 0.2s;
  transition: transform 0.2s;
  border: 1px solid rgba(255,255,255,0.5);
}

.plan-card:hover {
  transform: translateY(-2px);
}

.section {
  padding: 20px;
}

.section h3 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 18px;
  color: #1b5e20;
}

/* BMI Card */
.bmi-card {
  background: linear-gradient(135deg, #ffffff 0%, #f1f8e9 100%);
  border-radius: 20px;
  padding: 24px;
  text-align: center;
  margin-bottom: 24px;
  box-shadow: 0 15px 30px rgba(76, 175, 80, 0.15);
  position: relative;
  overflow: hidden;
}

.bmi-card::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(76, 175, 80, 0.1) 0%, transparent 70%);
  transform: rotate(30deg);
}

.bmi-value {
  font-size: 48px;
  font-weight: 800;
  color: #43a047; /* Green */
  margin: 10px 0;
  text-shadow: 0 2px 4px rgba(67, 160, 71, 0.2);
}

.bmi-label {
  font-size: 14px;
  color: #66bb6a;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.bmi-status {
  display: inline-block;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 20px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
}

.bmi-status.Normal { background: #e8f5e9; color: #2e7d32; }
.bmi-status.Underweight { background: #e3f2fd; color: #1565c0; }
.bmi-status.Overweight { background: #fff3e0; color: #ef6c00; }
.bmi-status.Obesity { background: #ffebee; color: #c62828; } /* Fallback */
.bmi-status[class*="Obesity"] { background: #ffebee; color: #c62828; }

.bmi-details {
  display: flex;
  justify-content: center;
  gap: 30px;
  color: #558b2f;
  font-weight: 500;
}

/* Animations */
.van-cell-group {
  background: transparent;
}

.van-cell {
  background: transparent;
  padding: 16px;
}

/* Override Vant Colors locally for this page */
:deep(.van-button--primary) {
  background: #4caf50;
  border-color: #4caf50;
}
:deep(.van-tag--success) {
  background: #81c784;
}
:deep(.van-tabbar-item--active) {
  color: #43a047;
}

:deep(.van-tabbar) {
  z-index: 9999 !important;
}

.suggestions-card {
  background: linear-gradient(to bottom right, #ffffff, #f1f8e9);
}

.suggestion-item {
  margin-bottom: 16px;
}

.suggestion-item h4 {
  margin: 0 0 8px 0;
  color: #2e7d32;
  font-size: 16px;
}

.suggestion-item p {
  margin: 0 0 12px 0;
  color: #558b2f;
  font-size: 14px;
  line-height: 1.6;
}

.food-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

/* History Table Styles */
.history-table-container {
  background: white;
  border-radius: 16px;
  padding: 10px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  overflow-x: auto;
}

.history-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.history-table th {
  text-align: left;
  padding: 12px;
  color: #888;
  font-weight: 600;
  border-bottom: 1px solid #eee;
}

.history-table td {
  padding: 12px;
  color: #333;
  border-bottom: 1px solid #f9f9f9;
}

.history-table tr {
  cursor: pointer;
  transition: background 0.2s;
}

.history-table tr:hover {
  background-color: #f1f8e9; /* Light green hover */
}

.history-table tr.active {
  background-color: #e8f5e9;
  border-left: 4px solid #4caf50;
}

.bmi-tag {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: bold;
}

.bmi-tag.Normal { color: #2e7d32; background: #e8f5e9; }
.bmi-tag.Underweight { color: #1565c0; background: #e3f2fd; }
.bmi-tag.Overweight { color: #ef6c00; background: #fff3e0; }
.bmi-tag.Obesity { color: #c62828; background: #ffebee; }

/* Food Grid */
.food-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-top: 12px;
}

.food-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.food-card-img {
  width: 100%;
  height: 100px;
  object-fit: cover;
}

.food-card-info {
  padding: 8px;
}

.food-card-info h5 {
  margin: 0 0 4px 0;
  font-size: 13px;
  color: #333;
}

.food-card-info p {
  margin: 0;
  font-size: 11px;
  color: #666;
  line-height: 1.4;
}

/* Timer Styles */
.timer-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: white;
}

.timer-circle {
  width: 250px;
  height: 250px;
  border: 8px solid rgba(255,255,255,0.3);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-bottom: 50px;
  background: rgba(0,0,0,0.2);
  backdrop-filter: blur(10px);
  box-shadow: 0 0 30px rgba(255,255,255,0.1);
}

.timer-time {
  font-size: 60px;
  font-weight: bold;
  font-family: monospace;
}

.timer-label {
  font-size: 18px;
  opacity: 0.8;
  margin-top: 10px;
}

.timer-controls {
  display: flex;
  gap: 20px;
  width: 80%;
  justify-content: center;
}

.stop-btn {
  background: rgba(255,255,255,0.2) !important;
  border: 1px solid rgba(255,255,255,0.5) !important;
  color: white !important;
}

/* User Card */
.user-card {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.9);
  padding: 20px;
  border-radius: 16px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.avatar-placeholder {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #4caf50;
  color: white;
  font-size: 24px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.user-info h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #333;
}

.hint-text {
  font-size: 12px;
  color: #666;
  margin-top: 8px;
}
</style>
