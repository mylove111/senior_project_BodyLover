import { createRouter, createWebHistory } from 'vue-router';
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', component: Login },
    { path: '/register', component: Register },
    { path: '/teenager', component: () => import('../views/TeenagerDashboard.vue') }, // Placeholder
    { path: '/adult', component: () => import('../views/AdultDashboard.vue') }, // Placeholder
    { path: '/senior', component: () => import('../views/SeniorDashboard.vue') } // Placeholder
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

export default router;
