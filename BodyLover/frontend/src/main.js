import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import Vant, { Locale } from 'vant';
import enUS from 'vant/es/locale/lang/en-US';
import 'vant/lib/index.css';

import router from './router';
import { createPinia } from 'pinia';

// Set Vant to English
Locale.use('en-US', enUS);

const app = createApp(App);
app.use(createPinia());
app.use(router);
app.use(Vant);
app.mount('#app');
