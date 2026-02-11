import React, { createContext, ReactNode, useContext, useEffect, useState } from 'react'
import { User } from '../types/User';
const BASE_URL = "http://localhost:8080";

interface AuthContextType {
    user: User | null;
    setUser: (user: User | null) => void;
    logout: () => Promise<void>;
    loading: boolean;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({children} : {children: ReactNode}) => {
    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        const checkAuth = async () => {
            try {
                const res = await fetch(BASE_URL + '/auth/me', {
                    credentials: 'include'
                });
                if (res.ok) {
                    const data = await res.json();
                    setUser(data);
                } else {
                    console.log('Auth check failed:', res.status);
                    setUser(null);
                }
            } catch(err) {
                console.error("Auth check error:", err);
                setUser(null);
            } finally {
                setLoading(false);
            }
        };
        checkAuth();
    }, []);

    const logout = async (): Promise<void> => {
        try {
            const res = await fetch(BASE_URL + '/auth/logout', {
                method: 'POST',
                credentials: 'include'
            });
            if (res.ok) {
                setUser(null);
            } else {
                console.error('Logout failed:', res.status);
            }
        } catch (err) {
            console.error("Logout error:", err);
        }
    };

    return(
        <AuthContext.Provider value={{user, setUser, logout, loading}}>
            {!loading && children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if(!context) throw new Error('useAuth must be used within AuthProvider');
    return context;
}