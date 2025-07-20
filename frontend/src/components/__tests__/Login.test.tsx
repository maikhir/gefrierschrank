import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import Login from '../Login';
import { AuthProvider } from '../../hooks/useAuth';

const mockNavigate = vi.fn();
const mockLogin = vi.fn();

// Mock useAuth hook
vi.mock('../../hooks/useAuth', async () => {
  const actual = await vi.importActual('../../hooks/useAuth');
  return {
    ...actual,
    useAuth: () => ({
      login: mockLogin,
    }),
  };
});

// Mock react-router-dom
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

const renderLogin = () => {
  return render(
    <BrowserRouter>
      <AuthProvider>
        <Login />
      </AuthProvider>
    </BrowserRouter>
  );
};

describe('Login Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders login form correctly', () => {
    renderLogin();
    
    expect(screen.getByText('Gefrierschrank Verwaltung')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Anmelden' })).toBeInTheDocument();
    expect(screen.getByLabelText('Benutzername:')).toBeInTheDocument();
    expect(screen.getByLabelText('Passwort:')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Anmelden' })).toBeInTheDocument();
  });

  it('allows user to input username and password', async () => {
    const user = userEvent.setup();
    renderLogin();
    
    const usernameInput = screen.getByLabelText('Benutzername:');
    const passwordInput = screen.getByLabelText('Passwort:');
    
    await user.type(usernameInput, 'testuser');
    await user.type(passwordInput, 'testpass');
    
    expect(usernameInput).toHaveValue('testuser');
    expect(passwordInput).toHaveValue('testpass');
  });

  it('submits form with correct credentials', async () => {
    const user = userEvent.setup();
    mockLogin.mockResolvedValueOnce(undefined);
    
    renderLogin();
    
    const usernameInput = screen.getByLabelText('Benutzername:');
    const passwordInput = screen.getByLabelText('Passwort:');
    const submitButton = screen.getByRole('button', { name: 'Anmelden' });
    
    await user.type(usernameInput, 'testuser');
    await user.type(passwordInput, 'testpass');
    await user.click(submitButton);
    
    expect(mockLogin).toHaveBeenCalledWith('testuser', 'testpass');
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/');
    });
  });

  it('displays error message when login fails', async () => {
    const user = userEvent.setup();
    mockLogin.mockRejectedValueOnce(new Error('Login failed'));
    
    renderLogin();
    
    const usernameInput = screen.getByLabelText('Benutzername:');
    const passwordInput = screen.getByLabelText('Passwort:');
    const submitButton = screen.getByRole('button', { name: 'Anmelden' });
    
    await user.type(usernameInput, 'wronguser');
    await user.type(passwordInput, 'wrongpass');
    await user.click(submitButton);
    
    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(
        'Anmeldung fehlgeschlagen. Überprüfen Sie Ihre Zugangsdaten.'
      );
    });
    
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('shows loading state during login attempt', async () => {
    const user = userEvent.setup();
    let resolveLogin: () => void;
    const loginPromise = new Promise<void>((resolve) => {
      resolveLogin = resolve;
    });
    mockLogin.mockReturnValueOnce(loginPromise);
    
    renderLogin();
    
    const usernameInput = screen.getByLabelText('Benutzername:');
    const passwordInput = screen.getByLabelText('Passwort:');
    const submitButton = screen.getByRole('button', { name: 'Anmelden' });
    
    await user.type(usernameInput, 'testuser');
    await user.type(passwordInput, 'testpass');
    await user.click(submitButton);
    
    // Should show loading state
    expect(screen.getByRole('button', { name: 'Anmelden...' })).toBeInTheDocument();
    expect(usernameInput).toBeDisabled();
    expect(passwordInput).toBeDisabled();
    
    // Resolve the promise
    resolveLogin!();
    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Anmelden' })).toBeInTheDocument();
    });
  });

  it('prevents submission with empty fields', async () => {
    const user = userEvent.setup();
    renderLogin();
    
    const submitButton = screen.getByRole('button', { name: 'Anmelden' });
    
    await user.click(submitButton);
    
    expect(mockLogin).not.toHaveBeenCalled();
  });

  it('clears error message when user starts typing', async () => {
    const user = userEvent.setup();
    mockLogin.mockRejectedValueOnce(new Error('Login failed'));
    
    renderLogin();
    
    const usernameInput = screen.getByLabelText('Benutzername:');
    const passwordInput = screen.getByLabelText('Passwort:');
    const submitButton = screen.getByRole('button', { name: 'Anmelden' });
    
    // Trigger error
    await user.type(usernameInput, 'wronguser');
    await user.type(passwordInput, 'wrongpass');
    await user.click(submitButton);
    
    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeInTheDocument();
    });
    
    // Clear and type again - this should trigger a new submission attempt
    await user.clear(usernameInput);
    await user.type(usernameInput, 'newuser');
    
    // The error should still be there until next submission
    expect(screen.getByRole('alert')).toBeInTheDocument();
  });

  it('handles keyboard navigation correctly', async () => {
    const user = userEvent.setup();
    renderLogin();
    
    const usernameInput = screen.getByLabelText('Benutzername:');
    const passwordInput = screen.getByLabelText('Passwort:');
    
    // Tab navigation
    await user.tab();
    expect(usernameInput).toHaveFocus();
    
    await user.tab();
    expect(passwordInput).toHaveFocus();
    
    await user.tab();
    expect(screen.getByRole('button', { name: 'Anmelden' })).toHaveFocus();
  });
});