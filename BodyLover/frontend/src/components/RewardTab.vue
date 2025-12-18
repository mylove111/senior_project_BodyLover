<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { showDialog, showToast } from 'vant';
import api from '../api/request';
import { useUserStore } from '../stores/user';

const userStore = useUserStore();
const activeNames = ref('1'); // Accordion mode uses string
const points = ref(0);
const allApps = [
    { id: 'tiktok', name: 'TikTok', icon: '/icon/tiktok.jpg', cost: 100, duration: 20, color: '#000000', locked: true, remaining: 0 },
    { id: 'youtube', name: 'YouTube', icon: '/icon/youtube.jpg', cost: 100, duration: 25, color: '#FF0000', locked: true, remaining: 0 },
    { id: 'instagram', name: 'Instagram', icon: '/icon/instagram.jpg', cost: 80, duration: 15, color: '#C13584', locked: true, remaining: 0 },
    { id: 'snapchat', name: 'Snapchat', icon: '/icon/snapchat.jpg', cost: 50, duration: 10, color: '#FFFC00', locked: true, remaining: 0 },
    { id: 'spotify', name: 'Spotify', icon: '/icon/spotify.jpg', cost: 60, duration: 30, color: '#1DB954', locked: true, remaining: 0 },
    { id: 'line', name: 'Line', icon: '/icon/line.jpg', cost: 80, duration: 20, color: '#00C300', locked: true, remaining: 0 },
    { id: 'twitch', name: 'Twitch', icon: '/icon/twitch.jpg', cost: 120, duration: 30, color: '#9146FF', locked: true, remaining: 0 },
    { id: 'steam', name: 'Steam', icon: '/icon/steam.jpg', cost: 150, duration: 30, color: '#1b2838', locked: true, remaining: 0 },
    { id: 'minecraft', name: 'Minecraft', icon: '/icon/minecraft.jpg', cost: 150, duration: 30, color: '#2C2B2B', locked: true, remaining: 0 },
    { id: 'bilibili', name: 'Bilibili', icon: '/icon/bilibili.jpg', cost: 200, duration: 45, color: '#00A1D6', locked: true, remaining: 0 },
    { id: 'genshin', name: 'Genshin', icon: '/icon/genshin.jpg', cost: 180, duration: 30, color: '#333333', locked: true, remaining: 0 }
];

const myApps = ref([]);
const showAppSelector = ref(false);
const availableApps = computed(() => {
    const selectedIds = new Set(myApps.value.map(a => a.id));
    return allApps.filter(a => !selectedIds.has(a.id));
});

let timerInterval = null;

const loadMyApps = () => {
    if (!userStore.userInfo?.id) return;
    const key = `user_selected_apps_${userStore.userInfo.id}`;
    const saved = localStorage.getItem(key);
    
    if (saved) {
        const savedIds = JSON.parse(saved);
        // Restore app objects from IDs
        myApps.value = allApps.filter(a => savedIds.includes(a.id)).map(original => {
             return { ...original };
        });
    } else {
        myApps.value = [];
    }
};

const addToMyApps = (app) => {
    myApps.value.push({ ...app });
    saveMyApps();
    showAppSelector.value = false;
    showToast('App Added!');
};

const saveMyApps = () => {
    if (!userStore.userInfo?.id) return;
    const key = `user_selected_apps_${userStore.userInfo.id}`;
    const ids = myApps.value.map(a => a.id);
    localStorage.setItem(key, JSON.stringify(ids));
};

const fetchPoints = async () => {
    try {
        const res = await api.get(`/user/${userStore.userInfo.id}`);
        if (res.data.code === 200) {
            points.value = res.data.data.points || 0;
            // Also update store if needed, but local ref is fine
        }
    } catch (e) {
        console.error("Failed to fetch points");
    }
};

const unlockApp = (app) => {
    if (!app.locked) return; // Already unlocked

    showDialog({
        title: 'Unlock ' + app.name + '?',
        message: `Cost: ${app.cost} Points\nDuration: ${app.duration} Minutes`,
        showCancelButton: true,
        confirmButtonText: 'Unlock',
        confirmButtonColor: '#ff9800'
    }).then(async () => {
        if (points.value < app.cost) {
            showToast('Not enough points! Go exercise!');
            return;
        }

        try {
            const res = await api.post('/user/points/deduct', {
                userId: userStore.userInfo.id,
                points: app.cost
            });
            
            if (res.data.code === 200) {
                showToast(`Unlocked! Enjoy ${app.duration} mins.`);
                points.value = res.data.currentPoints; // Update local points
                startAppTimer(app, app.duration * 60);
            } else {
                showToast(res.data.message);
            }
        } catch (e) {
            showToast('Transaction failed');
        }
    }).catch(() => {
        // Canceled
    });
};

const startAppTimer = (app, seconds) => {
    if (!userStore.userInfo?.id) return;
    const endTime = Date.now() + seconds * 1000;
    const key = `reward_timer_${userStore.userInfo.id}_${app.id}`;
    localStorage.setItem(key, endTime);
    
    updateAppStatus(app);
};

const updateAppStatus = (app) => {
    if (!userStore.userInfo?.id) return;
    const key = `reward_timer_${userStore.userInfo.id}_${app.id}`;
    const storedEnd = localStorage.getItem(key);
    
    if (storedEnd) {
        const remaining = Math.floor((parseInt(storedEnd) - Date.now()) / 1000);
        if (remaining > 0) {
            app.locked = false;
            app.remaining = remaining;
        } else {
            app.locked = true;
            app.remaining = 0;
            localStorage.removeItem(key);
        }
    } else {
        app.locked = true;
        app.remaining = 0;
    }
};

const formatTime = (seconds) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
};

onMounted(() => {
    fetchPoints();
    loadMyApps();
    
    // Initial check for myApps
    myApps.value.forEach(app => updateAppStatus(app));

    // Global timer loop for all apps (in myApps)
    timerInterval = setInterval(() => {
        myApps.value.forEach(app => {
            if (!app.locked) {
                updateAppStatus(app);
            }
        });
    }, 1000);
});

onUnmounted(() => {
    clearInterval(timerInterval);
});
</script>

<template>
  <div class="reward-tab">
      <!-- Habits Comparison Section -->
      <div class="habits-section">
          <van-collapse v-model="activeNames" accordion>
            <van-collapse-item name="1" title="🌟 Good Habits vs ⚠️ Bad Habits">
                <div class="comparison-grid">
                    <div class="habit-col good">
                        <h4>🌟 Good Habits</h4>
                        <ul>
                            <li><strong>Efficiency:</strong> Foundation for high achievement.</li>
                            <li><strong>Routine:</strong> Promotes a consistent schedule.</li>
                            <li><strong>Diet:</strong> Helps maintain healthy eating.</li>
                            <li><strong>Study:</strong> Key to effective learning.</li>
                            <li><strong>Steps:</strong> Break goals into small tasks.</li>
                            <li><strong>Track:</strong> Record daily progress.</li>
                            <li><strong>Rewards:</strong> Positive feedback keeps you going.</li>
                        </ul>
                    </div>
                    <div class="habit-col bad">
                        <h4>⚠️ Bad Habits</h4>
                        <ul>
                            <li><strong>Health:</strong> Late nights & overeating cause illness.</li>
                            <li><strong>Mindset:</strong> Leads to self-loathing & low confidence.</li>
                            <li><strong>Rhythm:</strong> Disrupts life & wastes time.</li>
                            <li><strong>Social:</strong> Drains relationships.</li>
                            <li><strong>Mood:</strong> Causes emotional instability.</li>
                        </ul>
                    </div>
                </div>
                <p class="habit-summary">
                    "Cultivating good habits promotes growth, while bad habits harm health and quality of life."
                </p>
            </van-collapse-item>
          </van-collapse>
      </div>

      <!-- Header / Points Display -->
      <div class="points-header">
          <div class="coin-icon">🪙</div>
          <div class="points-value">{{ points }}</div>
          <div class="points-label">Available Points</div>
          <div class="points-tip">Earn points by completing workouts!</div>
      </div>

      <!-- App Grid -->
      <div class="apps-grid">
          <!-- My Apps -->
          <div 
            v-for="app in myApps" 
            :key="app.id" 
            class="app-item" 
            :class="{ 'is-locked': app.locked }"
            @click="unlockApp(app)"
          >
              <div class="app-icon-wrapper" :style="{ borderColor: app.locked ? '#ccc' : app.color }">
                  <img :src="app.icon" class="app-icon-img" :style="{ filter: app.locked ? 'grayscale(100%)' : 'none' }" />
                  
                  <!-- Lock Overlay -->
                  <div v-if="app.locked" class="lock-overlay">
                      <van-icon name="lock" />
                  </div>
              </div>

              <div class="app-name">{{ app.name }}</div>

              <div v-if="app.locked" class="app-cost">
                  {{ app.cost }} pts / {{ app.duration }}m
              </div>
              <div v-else class="app-timer">
                  {{ formatTime(app.remaining) }}
              </div>
          </div>

          <!-- Add Button (Always visible if there are more apps) -->
          <div class="app-item add-item" @click="showAppSelector = true" v-if="availableApps.length > 0">
              <div class="app-icon-wrapper add-wrapper">
                  <van-icon name="plus" />
              </div>
              <div class="app-name">Add App</div>
          </div>
      </div>

      <!-- App Selector Dialog -->
      <van-dialog v-model:show="showAppSelector" title="Add to Rewards" :show-confirm-button="false" close-on-click-overlay>
          <div class="app-selector-list">
              <van-cell 
                v-for="app in availableApps" 
                :key="app.id" 
                :title="app.name" 
                clickable 
                @click="addToMyApps(app)"
              >
                <template #icon>
                    <img :src="app.icon" style="width: 30px; height: 30px; margin-right: 10px; border-radius: 6px;" />
                </template>
                <template #right-icon>
                    <van-icon name="plus" color="#1989fa" />
                </template>
              </van-cell>
          </div>
      </van-dialog>
  </div>
</template>

<style scoped>
.reward-tab {
    padding: 20px;
}

.reward-tab {
    padding: 20px;
    padding-top: 10px;
}

.habits-section {
    margin-bottom: 20px;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.comparison-grid {
    display: flex;
    gap: 15px;
    font-size: 12px;
}

.habit-col {
    flex: 1;
    padding: 10px;
    border-radius: 8px;
}

.habit-col.good { background-color: #e8f5e9; color: #2e7d32; }
.habit-col.bad { background-color: #ffebee; color: #c62828; }

.habit-col h4 { margin: 0 0 8px 0; font-size: 13px; text-align: center; }
.habit-col ul { list-style: none; padding: 0; margin: 0; }
.habit-col li { margin-bottom: 4px; line-height: 1.4; }

.habit-summary {
    margin: 15px 0 0 0;
    font-size: 12px;
    font-style: italic;
    text-align: center;
    color: #666;
}

.points-header {
    background: linear-gradient(135deg, #FFD700 0%, #FFA000 100%);
    border-radius: 16px;
    padding: 20px;
    text-align: center;
    color: white;
    box-shadow: 0 4px 15px rgba(255, 160, 0, 0.3);
    margin-bottom: 30px;
}

.coin-icon { font-size: 40px; margin-bottom: 5px; }
.points-value { font-size: 48px; font-weight: 800; line-height: 1; text-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.points-label { font-size: 14px; opacity: 0.9; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; }
.points-tip { font-size: 12px; margin-top: 10px; background: rgba(255,255,255,0.2); padding: 5px 10px; border-radius: 20px; display: inline-block; }

.apps-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
}

.app-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    transition: transform 0.1s;
}
.app-item:active { transform: scale(0.95); }

.app-icon-wrapper {
    width: 70px; height: 70px;
    border-radius: 18px;
    background: #f5f5f5;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    margin-bottom: 8px;
    position: relative;
    border: 2px solid #e0e0e0;
    transition: all 0.3s;
}

.app-icon-img {
    width: 60%;
    height: 60%;
    object-fit: contain;
    transition: filter 0.3s;
}

.add-wrapper {
    border-style: dashed;
    border-color: #ccc;
    background: transparent !important;
    color: #999;
}
.add-item:active .add-wrapper {
    background: #f0f0f0 !important;
}

.app-selector-list {
    max-height: 300px;
    overflow-y: auto;
    padding: 10px 0;
}

.app-item:not(.is-locked) .app-icon-wrapper {
    background: white;
    border-width: 3px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.lock-overlay {
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
    background: rgba(0,0,0,0.1);
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #757575;
    font-size: 24px;
}

.app-name { font-weight: 600; font-size: 14px; color: #333; margin-bottom: 4px; }
.app-cost { font-size: 10px; color: #999; }

.app-timer {
    font-size: 14px;
    font-weight: 700;
    color: #4caf50;
    background: #e8f5e9;
    padding: 2px 8px;
    border-radius: 10px;
}
</style>
