import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

export default function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { user, loading } = useAuth();
  
  if (loading) {
    return <div className="flex justify-center items-center min-h-screen">Loading...</div>;
  }
  
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (location.pathname === '/onboarding') {
    return <>{children}</>;
  }

  if (user.onboardingComplete === false) {
    return <Navigate to='/onboarding' replace />;
  }
  
  return <>{children}</>;
}