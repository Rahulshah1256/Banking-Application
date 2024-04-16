import './App.css'
import 'react-toastify/dist/ReactToastify.css';
import HeaderComponent from './components/HeaderComponent'
import FooterComponent from './components/FooterComponent'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import AccountComponent from './components/AccountComponent'
import RegisterComponent from './components/RegisterComponent'
import LoginComponent from './components/LoginComponent'
import { isAdminUser, isUserLoggedIn } from './services/AuthService'
import RegistrationSuccess from './components/RegistrationSuccess'
import AccountRequestsComponent from './components/AccountRequestsComponent'
import AccountsComponent from './components/AccountsComponent'
import BeneficiaryComponent from './components/BeneficiaryComponent';
import AddBeneficiaryComponent from './components/AddBeneficiaryComponent';
import { TransferComponent } from './components/TransferComponent';
import { ToastContainer } from 'react-toastify';

function App() {


  function AuthenticatedRoute({ children }) {
    const isAuth = isUserLoggedIn();
    if (isAuth) {
      return children;
    }
    return <Navigate to="/" />
  }

  function AdminRoute({ children }) {
    const isAdmin = isAdminUser();
    if (isAdmin) {
      return children;
    }
    return <Navigate to="/" />
  }

  return (
    
    <>
      <BrowserRouter>
        <HeaderComponent />
        <Routes>
          <Route path='/' element={<LoginComponent />}></Route>
          <Route path='/account-requests' element={
            <AuthenticatedRoute>
              <AccountRequestsComponent />
            </AuthenticatedRoute>
          }></Route>
          <Route path='/accounts' element={
            <AuthenticatedRoute>
              <AccountsComponent />
            </AuthenticatedRoute>
          }></Route>
          <Route path='/beneficiaries' element={
            <AuthenticatedRoute>
              <BeneficiaryComponent />
            </AuthenticatedRoute>
          }></Route>
          <Route path='/add-beneficiary' element={
            <AuthenticatedRoute>
              <AddBeneficiaryComponent />
            </AuthenticatedRoute>
          }></Route>
          <Route path='/transfer' element={
            <AuthenticatedRoute>
              <TransferComponent />
            </AuthenticatedRoute>
          }></Route>
          <Route path='/add-account' element={
            <AdminRoute>
              <AccountComponent />
            </AdminRoute>
          }></Route>
          <Route path='/update-account/:id' element={
            <AuthenticatedRoute>
              <AccountComponent />
            </AuthenticatedRoute>
          }></Route>
          <Route path='/register' element={<RegisterComponent />}></Route>
          <Route path='/login' element={<LoginComponent />}></Route>
          <Route path='/success' element={<RegistrationSuccess />}></Route>
        </Routes>
        <ToastContainer />
        <FooterComponent />
      </BrowserRouter>
    </>
  )
}

export default App
