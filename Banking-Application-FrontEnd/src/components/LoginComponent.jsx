import React, { useState } from 'react'
import { loginAPICall, saveLoggedInUser, storeToken } from '../services/AuthService';
import { useNavigate } from 'react-router-dom';
import jwt from 'jwt-decode'
import { toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const LoginComponent = () => {

    const [username, setUsername] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('');

    const navigator = useNavigate();

    async function handleLoginForm(e) {
        e.preventDefault();

        // Simple form validation
        if (!username || !password) {
            setError('Username and password are required.');
            toast.error('Username and password are required.');
            return;
        }

        // Clear previous error messages
        setError('');

        
        await loginAPICall(username, password).then((response) => {
            console.log(response.data);
            // const token = 'Basic ' + window.btoa(username+":"+password);
            const user = jwt(response.data.accessToken);
            const role = user.role;
            const name = user.name;
            const email = user.email;
            const token = 'Bearer ' + response.data.accessToken;
            storeToken(token);
            saveLoggedInUser(username, role, email, name);
            navigator('/accounts', { state: { from: 'login' } });
            window.location.reload(false);
        }).catch(error => {
            console.error(error);
            setError('Cannot login now!. Please try again later');
        })

    }

    return (
        <div className='container'>
            <br /><br />
            <div className='row'>
                <div className='col-md-6 offset-md-3'>
                    <div className='card'>
                        <div className='card-header'>
                            <h2 className='text-center'>
                                Login Form
                            </h2>
                        </div>
                        <div className='card-body'>
                            <form>
                                <div className='row mb-3'>
                                    <label className='col-md-3 control-label'>Username or Email</label>
                                    <div className='col-md-9'>
                                        <input
                                            type='text'
                                            name='username'
                                            className='form-control'
                                            placeholder='Enter username'
                                            value={username}
                                            onChange={(e) => setUsername(e.target.value)}
                                        >
                                        </input>
                                    </div>
                                </div>

                                <div className='row mb-3'>
                                    <label className='col-md-3 control-label'>Password</label>
                                    <div className='col-md-9'>
                                        <input
                                            type='password'
                                            name='password'
                                            className='form-control'
                                            placeholder='Enter password'
                                            value={password}
                                            onChange={(e) => setPassword(e.target.value)}
                                        >
                                        </input>
                                    </div>
                                </div>
                                <div className='form-group nb-3'>
                                    <button className='btn btn-primary' onClick={(e) => handleLoginForm(e)}>Login</button>
                                </div>
                                <br />
                                {/* Display error message */}
                                {error && <div className='alert alert-danger'>{error}</div>}

                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default LoginComponent


