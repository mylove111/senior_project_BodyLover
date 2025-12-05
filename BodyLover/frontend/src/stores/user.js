import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUserStore = defineStore('user', () => {
    const userInfo = ref(null);
    const isLoggedIn = ref(false);

    function setUser(user) {
        userInfo.value = user;
        isLoggedIn.value = true;
        localStorage.setItem('user', JSON.stringify(user));
    }

    function logout() {
        userInfo.value = null;
        isLoggedIn.value = false;
        localStorage.removeItem('user');
    }

    // Initialize from local storage
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
        setUser(JSON.parse(storedUser));
    }

    return { userInfo, isLoggedIn, setUser, logout };
});
