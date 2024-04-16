import React from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { isAdminUser, isUserLoggedIn, logout } from '../services/AuthService'

const HeaderComponent = () => {

    const isAuth = isUserLoggedIn();
    const isAdmin = isAdminUser();
    const navigator = useNavigate();

    function handleLogout()
    {
        logout();
        navigator('/login');
    }

  return (
    <div>
        <header>
            <nav className='navbar navbar-expand-md navbar-dark bg-dark'>
                <div>
                    <a href='http://localhost:3000' className='navbar-brand'>
                        Excellence Bank Application
                    </a>
                </div>
                <div>
                    <div className='collapse navbar-collapse'>
                        <ul className='navbar-nav'>
                            {
                                isAuth &&
                                <li className='nav-item'> 
                                    <NavLink to="/accounts" className="nav-link">Accounts</NavLink>
                                </li>
                            }
                            {
                                isAuth && !isAdmin &&
                                <li className='nav-item'> 
                                    <NavLink to="/beneficiaries" className="nav-link">Beneficiaries</NavLink>
                                </li>
                            }
                            {
                                isAdmin &&
                                <li className='nav-item'> 
                                    <NavLink to="/account-requests" className="nav-link">Account Requests</NavLink>
                                </li>
                            }
                        </ul>
                    </div>
                </div>
                <ul className='navbar-nav'>
                            {
                                !isAuth &&
                                <li className='nav-item'> 
                                    <NavLink to="/register" className="nav-link">Register</NavLink>
                                </li>
                            }
                            {
                                !isAuth &&
                                <li className='nav-item'> 
                                    <NavLink to="/login" className="nav-link">Login</NavLink>
                                </li>
                            }  
                            {
                                isAuth &&
                                <li className='nav-item'> 
                                    <NavLink to="/login" className="nav-link" onClick={handleLogout}>Logout</NavLink>
                                </li>
                            }        
                </ul>
            </nav>
        </header>
    </div>
  )
}

export default HeaderComponent