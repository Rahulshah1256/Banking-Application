import axios from 'axios';
import { getToken } from './AuthService.js';
const BASE_REST_API_URL ='http://localhost:8080/api/users';


// Add a request interceptor
axios.interceptors.request.use(function (config) {
    // Do something before request is sent
    config.headers['Authorization'] = getToken();
    return config;
  }, function (error) {
    // Do something with request error
    return Promise.reject(error);
  });

export const getAllUserRequests = () => axios.get(BASE_REST_API_URL);

export const rejectUser = (id)=> axios.put(BASE_REST_API_URL+ '/' + id);

export const generateUser = (id)=> axios.post(BASE_REST_API_URL+ '/' + id);