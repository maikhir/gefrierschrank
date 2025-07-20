import { render, screen } from '@testing-library/react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import ProtectedRoute from '../ProtectedRoute';
import { User } from '../../types';

// Mock useAuth hook
const mockUseAuth = vi.fn();

vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => mockUseAuth(),
}));

const TestComponent = () => <div>Protected Content</div>;
const LoginComponent = () => <div>Login Page</div>;

const renderProtectedRoute = () => {
  return render(
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginComponent />} />
        <Route 
          path="/" 
          element={
            <ProtectedRoute>
              <TestComponent />
            </ProtectedRoute>
          } 
        />
      </Routes>
    </BrowserRouter>
  );
};

describe('ProtectedRoute Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows loading state when auth is loading', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      isLoading: true,
    });

    renderProtectedRoute();

    expect(screen.getByTestId('loading')).toBeInTheDocument();
    expect(screen.getByText('Laden...')).toBeInTheDocument();
  });

  it('redirects to login when user is not authenticated', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      isLoading: false,
    });

    renderProtectedRoute();

    expect(screen.getByText('Login Page')).toBeInTheDocument();
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });

  it('renders children when user is authenticated', () => {
    const mockUser: User = {
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      roles: ['ROLE_USER'],
    };

    mockUseAuth.mockReturnValue({
      user: mockUser,
      isLoading: false,
    });

    renderProtectedRoute();

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
    expect(screen.queryByText('Login Page')).not.toBeInTheDocument();
  });

  it('renders children when user is admin', () => {
    const mockAdminUser: User = {
      id: 1,
      username: 'admin',
      email: 'admin@example.com',
      roles: ['ROLE_ADMIN', 'ROLE_USER'],
    };

    mockUseAuth.mockReturnValue({
      user: mockAdminUser,
      isLoading: false,
    });

    renderProtectedRoute();

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('does not show loading after auth completes', () => {
    const mockUser: User = {
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      roles: ['ROLE_USER'],
    };

    mockUseAuth.mockReturnValue({
      user: mockUser,
      isLoading: false,
    });

    renderProtectedRoute();

    expect(screen.queryByTestId('loading')).not.toBeInTheDocument();
    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('handles undefined user correctly', () => {
    mockUseAuth.mockReturnValue({
      user: undefined,
      isLoading: false,
    });

    renderProtectedRoute();

    expect(screen.getByText('Login Page')).toBeInTheDocument();
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });

  it('transitions from loading to authenticated state', () => {
    const mockUser: User = {
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      roles: ['ROLE_USER'],
    };

    // Start with loading
    mockUseAuth.mockReturnValue({
      user: null,
      isLoading: true,
    });

    const { rerender } = renderProtectedRoute();
    expect(screen.getByTestId('loading')).toBeInTheDocument();

    // Change to authenticated
    mockUseAuth.mockReturnValue({
      user: mockUser,
      isLoading: false,
    });

    rerender(
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginComponent />} />
          <Route 
            path="/" 
            element={
              <ProtectedRoute>
                <TestComponent />
              </ProtectedRoute>
            } 
          />
        </Routes>
      </BrowserRouter>
    );

    expect(screen.queryByTestId('loading')).not.toBeInTheDocument();
    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('transitions from loading to unauthenticated state', () => {
    // Start with loading
    mockUseAuth.mockReturnValue({
      user: null,
      isLoading: true,
    });

    const { rerender } = renderProtectedRoute();
    expect(screen.getByTestId('loading')).toBeInTheDocument();

    // Change to unauthenticated
    mockUseAuth.mockReturnValue({
      user: null,
      isLoading: false,
    });

    rerender(
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginComponent />} />
          <Route 
            path="/" 
            element={
              <ProtectedRoute>
                <TestComponent />
              </ProtectedRoute>
            } 
          />
        </Routes>
      </BrowserRouter>
    );

    expect(screen.queryByTestId('loading')).not.toBeInTheDocument();
    expect(screen.getByText('Login Page')).toBeInTheDocument();
  });
});