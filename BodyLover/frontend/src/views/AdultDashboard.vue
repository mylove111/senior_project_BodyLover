<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showDialog } from 'vant';
import api from '../api/request';
import { useUserStore } from '../stores/user';
import ActivityChart from '../components/ActivityChart.vue';
import confetti from 'canvas-confetti';

const router = useRouter();
const userStore = useUserStore();
const activeTab = ref('training');
const familyBadge = ref('');
const newCompletions = ref([]);

const playSuccessSound = () => {
    const audio = new Audio('http://commondatastorage.googleapis.com/codeskulptor-assets/week7-brrring.m4a');
    audio.play().catch(e => console.log('Audio play failed', e));
};


// Chart Data
const chartData = ref([]);

const fetchStats = async () => {
    try {
        const res = await api.get('/plans/stats', { params: { userId: userStore.userInfo?.id } });
        if (res.data.code === 200) {
            chartData.value = res.data.data;
        }
    } catch (error) {
        console.error('Failed to fetch stats', error);
    }
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
      finishTimer(true);
    }
  }, 1000);
};

const pauseTimer = () => {
  isTimerRunning.value = false;
  clearInterval(timerInterval);
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
     if (completed) {
         // Should not happen if completed is true (means natural finish)
     } else {
         showToast('Too short to save');
         return;
     }
  }

  try {
    await api.put(`/plans/${activePlan.value.id}`, { 
      status: 'COMPLETED',
      actualMinutes: actualMinutes
    });
    activePlan.value.status = 'COMPLETED';
    showToast(`Completed! Recorded ${actualMinutes} mins`);
    fetchPlans();
    fetchStats(); // Update chart 
  } catch (error) {
    showToast('Failed to save');
  }
};

// Plans Data
const plans = ref([]);
const showAddPlan = ref(false);
const newPlan = ref({
  title: '',
  content: '',
  durationHours: '',
  planType: 'EXERCISE'
});

// Family Data
const familyMembers = ref([]);
const pendingRequests = ref([]);
const showAddFamily = ref(false);
const targetUsername = ref('');
const selectedRelation = ref('FATHER_SON');

// Workout Guides Data (Organized by Muscle Group)
const workoutGuides = ref([
    {
        id: 1,
        name: 'Chest',
        icon: '💪',
        color: '#e53935',
        expanded: false,
        exercises: [
            { name: 'Push-ups', sets: '3 sets × 12-15 reps', description: 'Keep your body in a straight line. Lower chest to floor, then push back up. Great for overall chest development.' },
            { name: 'Incline Push-ups', sets: '3 sets × 15 reps', description: 'Hands on elevated surface (chair/bench). Targets upper chest. Easier variation for beginners.' },
            { name: 'Diamond Push-ups', sets: '3 sets × 10-12 reps', description: 'Hands together forming diamond shape. Focuses on inner chest and triceps.' },
            { name: 'Wide Push-ups', sets: '3 sets × 12 reps', description: 'Hands wider than shoulder width. Emphasizes outer chest muscles.' }
        ],
        tips: 'Rest 60-90 seconds between sets. Focus on controlled movements, not speed.'
    },
    {
        id: 2,
        name: 'Back',
        icon: '🦾',
        color: '#1e88e5',
        expanded: false,
        exercises: [
            { name: 'Superman Hold', sets: '3 sets × 30 sec', description: 'Lie face down, lift arms and legs simultaneously. Strengthens lower back and improves posture.' },
            { name: 'Reverse Snow Angels', sets: '3 sets × 12 reps', description: 'Face down, move arms from sides to overhead while lifted. Targets upper back.' },
            { name: 'Doorway Rows', sets: '3 sets × 12 reps/arm', description: 'Hold door frame, lean back and pull yourself forward. Mimics rowing motion.' },
            { name: 'Prone Y Raises', sets: '3 sets × 15 reps', description: 'Lie face down, raise arms in Y position. Excellent for shoulder stability and upper back.' }
        ],
        tips: 'Squeeze shoulder blades together at peak contraction. Avoid jerky movements.'
    },
    {
        id: 3,
        name: 'Shoulders',
        icon: '🏋️',
        color: '#7cb342',
        expanded: false,
        exercises: [
            { name: 'Pike Push-ups', sets: '3 sets × 10-12 reps', description: 'Hips high, body in inverted V. Targets front deltoids. Progression to handstand push-ups.' },
            { name: 'Arm Circles', sets: '3 sets × 30 sec each direction', description: 'Extend arms and rotate in circles. Great warm-up and endurance builder.' },
            { name: 'Wall Push-ups (Overhead)', sets: '3 sets × 12 reps', description: 'Face wall, push at head height. Safe shoulder strengthening for all levels.' },
            { name: 'Lateral Arm Raises', sets: '3 sets × 15 reps', description: 'Raise arms to sides until shoulder height. Use water bottles for added resistance.' }
        ],
        tips: 'Shoulders are injury-prone. Always warm up and avoid excessive weight initially.'
    },
    {
        id: 4,
        name: 'Arms',
        icon: '💪',
        color: '#ff9800',
        expanded: false,
        exercises: [
            { name: 'Tricep Dips (Chair)', sets: '3 sets × 12-15 reps', description: 'Hands on chair edge behind you. Lower body by bending elbows. Primary tricep exercise.' },
            { name: 'Bicep Curls (Water Bottles)', sets: '3 sets × 15 reps', description: 'Use filled water bottles as weights. Curl towards shoulders with controlled motion.' },
            { name: 'Diamond Push-ups', sets: '3 sets × 10 reps', description: 'Also great for triceps! Hands close together under chest.' },
            { name: 'Towel Curls', sets: '3 sets × 12 reps', description: 'Step on towel center, curl ends up. Creates isometric resistance for biceps.' }
        ],
        tips: 'Full range of motion is key. Avoid swinging - control the weight throughout.'
    },
    {
        id: 5,
        name: 'Legs',
        icon: '🦵',
        color: '#8e24aa',
        expanded: false,
        exercises: [
            { name: 'Bodyweight Squats', sets: '3 sets × 15-20 reps', description: 'Feet shoulder-width, lower until thighs parallel. King of leg exercises!' },
            { name: 'Lunges', sets: '3 sets × 12 reps/leg', description: 'Step forward, lower back knee toward ground. Works quads, hamstrings, and glutes.' },
            { name: 'Calf Raises', sets: '3 sets × 20 reps', description: 'Rise onto toes, lower slowly. Can be done on stairs for extra range.' },
            { name: 'Glute Bridges', sets: '3 sets × 15 reps', description: 'Lie on back, push hips up squeezing glutes. Essential for hip strength.' },
            { name: 'Wall Sit', sets: '3 sets × 45 sec', description: 'Back against wall, thighs parallel to floor. Isometric quad burner!' }
        ],
        tips: 'Never let knees cave inward. Drive through heels on squats and lunges.'
    },
    {
        id: 6,
        name: 'Core',
        icon: '🎯',
        color: '#00897b',
        expanded: false,
        exercises: [
            { name: 'Plank', sets: '3 sets × 30-60 sec', description: 'Forearms and toes, body straight as board. Foundation of core strength.' },
            { name: 'Bicycle Crunches', sets: '3 sets × 20 reps', description: 'Alternate elbow to opposite knee. Targets obliques and rectus abdominis.' },
            { name: 'Mountain Climbers', sets: '3 sets × 30 sec', description: 'Plank position, alternate driving knees to chest. Cardio + core combo.' },
            { name: 'Dead Bug', sets: '3 sets × 12 reps/side', description: 'On back, extend opposite arm and leg. Anti-rotation core stability.' },
            { name: 'Russian Twists', sets: '3 sets × 20 reps', description: 'Seated, lean back, rotate torso side to side. Great for obliques.' }
        ],
        tips: 'Engage core by drawing belly button to spine. Quality over quantity!'
    }
]);

const toggleGuide = (guide) => {
    guide.expanded = !guide.expanded;
};

// Date Management
const currentDate = ref(new Date());
const showCalendar = ref(false);
const minDate = new Date(new Date().setFullYear(new Date().getFullYear() - 1)); // Allow 1 year back
const formatDate = (date) => {
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
};
const currentDateStr = computed(() => formatDate(currentDate.value));

const onConfirmDate = (value) => {
  showCalendar.value = false;
  currentDate.value = value;
  fetchPlans();
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
        showToast('Failed to load plans');
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
        showToast('Plan Added');
        showAddPlan.value = false;
        fetchPlans();
        newPlan.value = { title: '', content: '', durationHours: '', planType: 'EXERCISE' };
    } catch (error) {
        showToast('Failed to add');
    }
};

const completePlan = async (plan) => {
    try {
        await api.put(`/plans/${plan.id}`, { status: 'COMPLETED' });
        plan.status = 'COMPLETED';
        showToast('Great job!');
    } catch (error) {
        showToast('Operation failed');
    }
};

// Family Logic (Copied from TeenagerDashboard)
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
        const message = newCompletions.value.map(m => `${m.relationDisplay} (${m.username})`).join(', ');

        playSuccessSound();
        confetti({
            particleCount: 150,
            spread: 70,
            origin: { y: 0.6 }
        });

        showDialog({
            title: '🎉 Amazing!',
            message: `${message} has finished all daily tasks!\n\nKeep pushing! You can do it too! 🚀`,
            theme: 'round-button',
            confirmButtonColor: '#2979ff',
            confirmButtonText: 'Let\'s Go!'
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

// Removed - using toggleGuide instead for collapsible sections

onMounted(() => {
    if (!userStore.isLoggedIn) {
        router.push('/login');
        return;
    }
    fetchPlans();
    fetchFamilyData();
    fetchStats();
});
</script>

<template>
  <div class="adult-dashboard">
    <div class="content">
      
      <!-- Training Tab -->
      <div v-if="activeTab === 'training'">
        <div class="header-action">
          <h2>Training Plan</h2>
          <van-button type="primary" size="small" icon="plus" class="add-btn" @click="showAddPlan = true">New Session</van-button>
        </div>

        <div class="date-header" @click="showCalendar = true">
             <span>{{ currentDateStr }}</span>
             <van-icon name="calendar-o" />
        </div>

        <!-- Using Line Chart as requested -->
        <ActivityChart :data="chartData" title="Performance Overview" chart-type="line" />

        <div class="plan-list">
             <van-empty v-if="plans.length === 0" description="No scheduled training today." />
             <van-cell-group inset v-else v-for="plan in plans" :key="plan.id" class="plan-card">
                <van-cell :title="plan.title" :label="plan.content">
                    <template #right-icon>
                        <van-tag v-if="plan.status === 'COMPLETED'" type="success" plain>Done</van-tag>
                        <van-button v-else size="mini" type="primary" @click="startPlan(plan)">Start</van-button>
                    </template>
                </van-cell>
                <div class="plan-footer">
                    <span>⏱ {{ plan.durationHours }} Hours</span>
                    <span class="type-tag">{{ plan.planType }}</span>
                </div>
             </van-cell-group>
        </div>
      </div>

      <!-- Explore Tab (Workout Guides by Muscle Group) -->
      <div v-if="activeTab === 'explore'">
        <div class="header-action"><h2>Workout Guides</h2></div>
        
        <!-- Workout Principles Intro -->
        <div class="workout-intro">
            <h3>Workout Order Principles</h3>
            <p>The arrangement of workout order is crucial for improving training efficiency and muscle gain. Here are some common training principles:</p>
            <ul>
                <li><strong>Big Muscle Groups First:</strong> Prioritize large muscle groups like chest, back, and legs before training smaller groups like shoulders, arms, and abs.</li>
                <li><strong>Compound Exercises First:</strong> Prioritize compound movements such as bench press, squats, and rows. These exercises engage multiple muscle groups simultaneously, boosting efficiency.</li>
                <li><strong>Split Training:</strong> Depending on your level and goals, you can choose 2-day, 3-day, 4-day, or 5-day splits to ensure major muscle groups get full and complete training.</li>
            </ul>
            <p class="final-tip">When planning your routine, consider your personal fitness goals and physical condition. Reasonably arrange training frequency and intensity for optimal results.</p>
        </div>

        <p class="explore-subtitle">Tap any muscle group to see exercises</p>
        
        <div class="guide-list">
            <div v-for="guide in workoutGuides" :key="guide.id" class="guide-card">
                <!-- Header (Clickable) -->
                <div class="guide-header" @click="toggleGuide(guide)" :style="{ borderLeftColor: guide.color }">
                    <div class="guide-title">
                        <span class="guide-icon">{{ guide.icon }}</span>
                        <span class="guide-name">{{ guide.name }}</span>
                        <van-tag :color="guide.color" text-color="white" size="medium">{{ guide.exercises.length }} exercises</van-tag>
                    </div>
                    <van-icon :name="guide.expanded ? 'arrow-up' : 'arrow-down'" />
                </div>
                
                <!-- Expandable Content -->
                <div v-if="guide.expanded" class="guide-content">
                    <!-- Exercises -->
                    <div v-for="(ex, idx) in guide.exercises" :key="idx" class="exercise-item">
                        <div class="exercise-header">
                            <span class="exercise-name">{{ ex.name }}</span>
                            <span class="exercise-sets">{{ ex.sets }}</span>
                        </div>
                        <p class="exercise-desc">{{ ex.description }}</p>
                    </div>
                    
                    <!-- Tips -->
                    <div class="guide-tips">
                        <van-icon name="bulb-o" /> <strong>Pro Tip:</strong> {{ guide.tips }}
                    </div>
                </div>
            </div>
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
            <van-cell v-for="req in pendingRequests" :key="req.id" :title="req.requesterName" :label="'Role: ' + req.relationDisplay">
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
            <van-cell v-for="member in familyMembers" :key="member.id" center>
                <template #icon>
                    <van-image round width="45" height="45" :src="`https://api.dicebear.com/7.x/miniavs/svg?seed=${member.username}`" style="margin-right: 12px; border: 2px solid #e3f2fd;" />
                </template>
                <template #title>
                    <div style="font-weight: 700; font-size: 16px; color: #37474f;">{{ member.username }}</div>
                </template>
                <template #label>
                    <van-tag type="primary" plain size="medium" style="margin-top: 4px;">{{ member.relationDisplay || 'Family' }}</van-tag>
                    <span style="font-size: 12px; color: #90a4ae; margin-left: 8px;">{{ member.mode }} Mode</span>
                    
                    <div style="margin-top: 8px;">
                        <div style="display: flex; justify-content: space-between; font-size: 10px; color: #78909c; margin-bottom: 2px;">
                            <span>Today's Progress</span>
                            <span>{{ member.progress }}% ({{ member.completedPlans }}/{{ member.totalPlans }})</span>
                        </div>
                        <van-progress :percentage="member.progress" stroke-width="6" :show-pivot="false" color="#2979ff" track-color="#e3f2fd" />
                    </div>
                </template>
            </van-cell>
          </van-cell-group>
        </div>
      </div>

      <!-- Me Tab -->
      <div v-if="activeTab === 'me'">
        <div class="user-profile">
            <div class="avatar">{{ userStore.userInfo?.username?.charAt(0) }}</div>
            <h3>{{ userStore.userInfo?.username }}</h3>
            <p>Adult Mode</p>
        </div>

        <div class="logout-container">
            <van-button block type="danger" @click="logout">Log Out</van-button>
        </div>
      </div>
    </div>

    <!-- Timer Overlay -->
    <!-- Timer Overlay -->
    <van-overlay :show="showTimer" @click.stop class="focus-overlay" :z-index="2000">
      <div class="focus-content">
        <h2 style="color:white; margin-bottom: 30px;">Focus Mode</h2>
        <div class="timer-circle">
          <span>{{ formatTime(remainingSeconds) }}</span>
          <p>Scanning...</p>
        </div>
        <h3 style="color:white; margin-top: 20px;">{{ activePlan?.title }}</h3>
        
        <div class="timer-controls">
           <van-button round type="warning" v-if="isTimerRunning" @click="pauseTimer">Pause</van-button>
           <van-button round type="success" v-else @click="startInterval">Resume</van-button>
           <van-button round type="danger" @click="stopTimerEarly">Stop</van-button>
        </div>
      </div>
    </van-overlay>

    <!-- Dialogs -->
    <van-dialog v-model:show="showAddPlan" title="New Training" show-cancel-button @confirm="onAddPlan">
        <van-form>
            <van-cell-group inset>
                <van-field v-model="newPlan.title" label="Title" placeholder="Gym / Cardio" />
                <van-field v-model="newPlan.content" label="Description" type="textarea" />
                <van-field v-model="newPlan.durationHours" label="Duration (h)" type="number" />
            </van-cell-group>
        </van-form>
    </van-dialog>

    <!-- Add Family Dialog -->
    <van-dialog 
      v-model:show="showAddFamily" 
      title="Add Family Member" 
      show-cancel-button 
      confirm-button-text="Confirm"
      cancel-button-text="Cancel"
      @confirm="onAddFamily"
    >
      <van-form>
        <van-cell-group inset>
          <van-field v-model="targetUsername" label="Username" placeholder="Enter family's username" />
          <van-field name="picker" label="He/She is my:">
            <template #input>
              <van-radio-group v-model="selectedRelation">
                 <div style="margin-bottom: 8px; font-weight: bold; color: #666;">Adding Child:</div>
                 <van-radio name="FATHER_SON" style="margin-bottom: 5px;">Son (I'm Father)</van-radio>
                 <van-radio name="MOTHER_SON" style="margin-bottom: 10px;">Son (I'm Mother)</van-radio>
                 
                 <div style="margin-bottom: 8px; font-weight: bold; color: #666;">Adding Parent:</div>
                 <van-radio name="SON_FATHER" style="margin-bottom: 5px;">Father</van-radio>
                 <van-radio name="SON_MOTHER">Mother</van-radio>
              </van-radio-group>
            </template>
          </van-field>
        </van-cell-group>
      </van-form>
    </van-dialog>

    <van-calendar v-model:show="showCalendar" @confirm="onConfirmDate" color="#2979ff" confirm-text="Confirm" title="Select Date" :min-date="minDate" />

    <van-tabbar v-model="activeTab" active-color="#2979ff" inactive-color="#b0bec5">
      <van-tabbar-item name="training" icon="fire-o">Training</van-tabbar-item>
      <van-tabbar-item name="explore" icon="eye-o">Explore</van-tabbar-item>
      <van-tabbar-item name="family" icon="friends-o" :dot="!!familyBadge">Family</van-tabbar-item>
      <van-tabbar-item name="me" icon="user-o">Me</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<style scoped>
.adult-dashboard {
    min-height: 100vh;
    /* Vibrant Blue Gradient */
    background: linear-gradient(135deg, #e3f2fd 0%, #90caf9 100%);
    padding-bottom: 80px;
}
.content { padding: 20px; }
.header-action { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-action h2 { color: #2979ff; margin: 0; font-weight: 700; }
.add-btn { background-color: #2979ff; border: none; }

/* Vibrant Blue Section Titles */
h3 { color: #2962ff; margin-bottom: 10px; font-size: 16px; margin-top: 5px; }

.date-header {
    background: white;
    padding: 10px 15px;
    border-radius: 8px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    font-weight: 600;
    color: #455a64;
    box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.plan-card, .section {
    background: white;
    border-radius: 12px;
    margin-bottom: 15px;
    box-shadow: 0 2px 8px rgba(41, 121, 255, 0.08); /* Blue tinted shadow */
}

.plan-card {
    border-left: 5px solid #2979ff;
    overflow: hidden;
}

.plan-footer {
    padding: 10px 16px;
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: #78909c;
    background: #fafafa;
}
.type-tag { font-weight: bold; color: #2979ff; }

/* Workout Guide Cards */
.explore-subtitle {
    color: #78909c;
    font-size: 14px;
    margin: -10px 0 15px 0;
}
.guide-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}
.guide-card {
    background: white;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(41, 121, 255, 0.1);
}
.guide-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    cursor: pointer;
    border-left: 5px solid #2979ff;
    transition: background 0.2s;
}
.guide-header:active {
    background: #f5f5f5;
}
.guide-title {
    display: flex;
    align-items: center;
    gap: 10px;
}
.guide-icon {
    font-size: 24px;
}
.guide-name {
    font-size: 18px;
    font-weight: 700;
    color: #263238;
}
.guide-content {
    padding: 0 16px 16px 16px;
    border-top: 1px solid #e0e0e0;
    background: #fafafa;
}
.exercise-item {
    padding: 12px 0;
    border-bottom: 1px dashed #e0e0e0;
}
.exercise-item:last-of-type {
    border-bottom: none;
}
.exercise-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6px;
}
.exercise-name {
    font-weight: 600;
    color: #37474f;
    font-size: 15px;
}
.exercise-sets {
    font-size: 12px;
    color: #2979ff;
    background: #e3f2fd;
    padding: 3px 8px;
    border-radius: 10px;
    font-weight: 500;
}
.exercise-desc {
    margin: 0;
    font-size: 13px;
    color: #607d8b;
    line-height: 1.5;
}
.guide-tips {
    margin-top: 12px;
    padding: 12px;
    background: linear-gradient(135deg, #fff8e1 0%, #fff3e0 100%);
    border-radius: 8px;
    font-size: 13px;
    color: #e65100;
    line-height: 1.4;
}

/* User Profile */
.user-profile {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40px 0;
    background: white;
    margin-bottom: 20px;
    border-radius: 0 0 20px 20px;
    box-shadow: 0 4px 10px rgba(0,0,0,0.05);
}
.avatar {
    width: 80px; height: 80px;
    background: linear-gradient(135deg, #2979ff, #2962ff);
    color: white;
    font-size: 36px;
    border-radius: 50%;
    display: flex; align-items: center; justify-content: center;
    margin-bottom: 10px;
    box-shadow: 0 4px 10px rgba(41, 121, 255, 0.3);
}
.logout-container { margin: 20px; }

/* Override Vant Buttons */
:deep(.van-button--primary) {
    background: #2979ff;
    border-color: #2979ff;
}

/* Timer Overlay Styles */
.focus-overlay {
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgba(0, 0, 0, 0.9);
    z-index: 9999;
}
.focus-content {
    text-align: center;
    color: white;
    width: 100%;
}
.timer-circle {
    width: 250px; height: 250px;
    border: 8px solid #2979ff;
    border-radius: 50%;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    box-shadow: 0 0 30px rgba(41, 121, 255, 0.5);
    background: radial-gradient(circle, #1a237e 0%, #000000 100%);
}
.timer-circle span {
    font-size: 50px;
    font-weight: bold;
    font-family: monospace;
    color: #40c4ff;
    text-shadow: 0 0 10px #2979ff;
}
.timer-controls {
    display: flex;
    justify-content: center;
    gap: 20px;
    margin-top: 40px;
}
/* Workout Intro Styles */
.workout-intro {
    background: white;
    padding: 16px;
    border-radius: 12px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(41, 121, 255, 0.08);
}
.workout-intro h3 {
    color: #2979ff;
    font-size: 16px;
    margin-top: 0;
    margin-bottom: 12px;
}
.workout-intro p {
    font-size: 14px;
    color: #546e7a;
    line-height: 1.6;
    margin: 8px 0;
}
.workout-intro ul {
    padding-left: 20px;
    margin: 10px 0;
}
.workout-intro li {
    font-size: 13px;
    color: #455a64;
    margin-bottom: 8px;
    line-height: 1.5;
}
.workout-intro .final-tip {
    font-size: 13px;
    color: #ef6c00;
    font-style: italic;
    background: #fff3e0;
    padding: 10px;
    border-radius: 6px;
    margin-top: 15px;
}
</style>
