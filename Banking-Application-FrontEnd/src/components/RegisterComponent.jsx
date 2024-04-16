import React, { useState } from 'react'
import { registerAPICall } from '../services/AuthService'
import { toast } from 'react-toastify'
import { useNavigate } from 'react-router-dom'

const RegisterComponent = () => {

    const [name, setName] = useState('')
    const [username, setUsername] = useState('')
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [aadhaarno, setAadhaarno] = useState('')
    const [panno, setPanno] = useState('')
    const [address, setAddress] = useState('')
    const [mobile, setMobile] = useState('')

    const navigator = useNavigate();
    const [error, setError] = useState('');

    function handleRegistrationForm(e) {
        e.preventDefault();
        const register = { name, username, email, password, aadhaarno, panno, address, mobile };

        // Simple form validation
        if (!name || !username || !email || !password || !aadhaarno || !panno || !address || !mobile) {
            setError('All feilds are mandatory.');
            if (!name) {
                toast.error('Name is required.');
            }
            if (!username) {
                toast.error('Username is required.');
            }
            if (!email) {
                toast.error('Email is required.');
            }
            if (!password) {
                toast.error('Password is required.');
            }
            if (!aadhaarno) {
                toast.error('Aadhaarno is required.');
            }
            if (!panno) {
                toast.error('PAN is required.');
            }
            if (!address) {
                toast.error('Address is required.');
            }
            if (!mobile) {
                toast.error('Mobile number is required.');
            }
            return;
        }

        // Clear previous error messages
        setError('');

        console.log(register);
        registerAPICall(register).then((response) => {
            console.log(response.data);
            navigator('/success');
        }).catch(error => {
            toast.success(error)
            console.error(error);
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
                                User Registration Form
                            </h2>
                        </div>
                        <div className='card-body'>
                            <form>
                                <div className='row mb-3'>
                                    <label className='col-md-3 control-label'>Name</label>
                                    <div className='col-md-9'>
                                        <input
                                            type='text'
                                            name='name'
                                            className='form-control'
                                            placeholder='Enter name'
                                            value={name}
                                            onChange={(e) => setName(e.target.value)}
                                        >
                                        </input>
                                    </div>
                                </div>

                                <div className='row mb-3'>
                                    <label className='col-md-3 control-label'>Username</label>
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
                                    <label className='col-md-3 control-label'>Email</label>
                                    <div className='col-md-9'>
                                        <input
                                            type='text'
                                            name='email'
                                            className='form-control'
                                            placeholder='Enter email'
                                            value={email}
                                            onChange={(e) => setEmail(e.target.value)}
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

                                <div className='row mb-3'>
                                    <label className='col-md-3 control-label'>Aadhaar Number</label>
                                    <div className='col-md-9'>
                                        <input
                                            type='text'
                                            name='aadhaarno'
                                            className='form-control'
                                            placeholder='Enter aadhaar number'
                                            value={aadhaarno}
                                            onChange={(e) => setAadhaarno(e.target.value)}
                                        >
                                        </input>
                                    </div>
                                </div>

                                <div className='row mb-3'>
                                    <label className='col-md-3 control-label'>PAN Number</label>
                                    <div className='col-md-9'>
                                        <input
                                            type='text'
                                            name='panno'
                                            className='form-control'
                                            placeholder='Enter PAN number'
                                            value={panno}
                                            onChange={(e) => setPanno(e.target.value)}
                                        >
                                        </input>
                                    </div>
                                </div>

                                <div className='row mb-3'>
                                    <label className='col-md-3 control-label'>Mobile Number</label>
                                    <div className='col-md-9'>
                                        <input
                                            type='text'
                                            name='mobile'
                                            className='form-control'
                                            placeholder='Enter Mobile Number'
                                            value={mobile}
                                            onChange={(e) => setMobile(e.target.value)}
                                        >
                                        </input>
                                    </div>
                                </div>

                                <div className='row mb-3'>
                                    <label className='col-md-3 control-label'>Address</label>
                                    <div className='col-md-9'>
                                        <textarea
                                            type='text'
                                            name='address'
                                            className='form-control'
                                            placeholder='Enter address'
                                            value={address}
                                            onChange={(e) => setAddress(e.target.value)}
                                            rows={4}
                                        >
                                        </textarea>
                                    </div>
                                </div>

                                <div className='form-group nb-3'>
                                    <button className='btn btn-primary' onClick={(e) => handleRegistrationForm(e)}>Submit</button>
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

export default RegisterComponent