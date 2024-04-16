import axios from 'axios';
import { getToken } from './AuthService.js';
const BASE_REST_API_URL ='http://localhost:8080/api/accounts';


// Add a request interceptor
axios.interceptors.request.use(function (config) {
    // Do something before request is sent
    config.headers['Authorization'] = getToken();
    return config;
  }, function (error) {
    // Do something with request error
    return Promise.reject(error);
  });

export const getAllAccounts = () => axios.get(BASE_REST_API_URL);

export const saveAccount = (account) => axios.post(BASE_REST_API_URL,account)

export const getAccount = (id) => axios.get(BASE_REST_API_URL + '/' + id);

export const updateAccount = (id,account) => axios.put(BASE_REST_API_URL + '/' + id,account);

export const deleteAccount = (id) => axios.delete(BASE_REST_API_URL + '/' + id);
