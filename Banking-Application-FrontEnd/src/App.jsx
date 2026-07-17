import { Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute, AdminRoute } from './components/RouteGuards';
import AppLayout from './layout/AppLayout';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Accounts from './pages/Accounts';
import Transfer from './pages/Transfer';
import Beneficiaries from './pages/Beneficiaries';
import Cards from './pages/Cards';
import Loans from './pages/Loans';
import Deposits from './pages/Deposits';
import Cheques from './pages/Cheques';
import Profile from './pages/Profile';
import Notifications from './pages/Notifications';
import Support from './pages/Support';
import Reports from './pages/Reports';
import AdminConsole from './pages/admin/AdminConsole';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/accounts" element={<Accounts />} />
        <Route path="/transfer" element={<Transfer />} />
        <Route path="/beneficiaries" element={<Beneficiaries />} />
        <Route path="/cards" element={<Cards />} />
        <Route path="/loans" element={<Loans />} />
        <Route path="/deposits" element={<Deposits />} />
        <Route path="/cheques" element={<Cheques />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/notifications" element={<Notifications />} />
        <Route path="/support" element={<Support />} />
        <Route path="/reports" element={<Reports />} />
        <Route
          path="/admin"
          element={
            <AdminRoute>
              <AdminConsole />
            </AdminRoute>
          }
        />
      </Route>

      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
