import axios from 'axios';

const authApi = axios.create({
    baseURL: 'http://gateway-service2:1050/api/auth',
});

export const registerUser = (formData) => {
    return authApi.post('/register', formData);
};

export const loginUser = (formData) => {
    return authApi.post('/login', formData);
};
