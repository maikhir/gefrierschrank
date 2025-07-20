import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import Dashboard from '../Dashboard';
import { itemsAPI } from '../../services/api';
import { Item } from '../../types';

// Mock useAuth hook
const mockUser = {
  id: 1,
  username: 'testuser',
  email: 'test@example.com',
  roles: ['ROLE_USER'],
};

const mockLogout = vi.fn();

vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => ({
    user: mockUser,
    logout: mockLogout,
  }),
}));

// Mock API calls
vi.mock('../../services/api', () => ({
  itemsAPI: {
    getExpiring: vi.fn(),
    getExpired: vi.fn(),
  },
}));

const mockItemsAPI = itemsAPI as any;

const mockExpiringItems: Item[] = [
  {
    id: 1,
    name: 'Chicken Breast',
    categoryId: 1,
    categoryName: 'Fleisch',
    quantity: 1.5,
    unit: 'kg',
    expiryDate: '2024-01-15',
    expiryType: 'MHD',
    description: 'Fresh chicken',
    createdAt: '2024-01-08T10:00:00',
  },
  {
    id: 2,
    name: 'Milk',
    categoryId: 2,
    categoryName: 'Dairy',
    quantity: 1,
    unit: 'L',
    expiryDate: '2024-01-14',
    expiryType: 'VD',
    createdAt: '2024-01-07T10:00:00',
  },
];

const mockExpiredItems: Item[] = [
  {
    id: 3,
    name: 'Old Bread',
    categoryId: 3,
    categoryName: 'Bakery',
    quantity: 1,
    unit: 'piece',
    expiryDate: '2024-01-05',
    expiryType: 'MHD',
    createdAt: '2024-01-01T10:00:00',
  },
];

const renderDashboard = () => {
  return render(
    <BrowserRouter>
      <Dashboard />
    </BrowserRouter>
  );
};

describe('Dashboard Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders dashboard with user welcome message', async () => {
    mockItemsAPI.getExpiring.mockResolvedValue(mockExpiringItems);
    mockItemsAPI.getExpired.mockResolvedValue(mockExpiredItems);

    renderDashboard();

    expect(screen.getByText('Gefrierschrank Verwaltung')).toBeInTheDocument();
    expect(screen.getByText('Willkommen, testuser!')).toBeInTheDocument();
    expect(screen.getByText('Abmelden')).toBeInTheDocument();
  });

  it('shows loading state initially', () => {
    mockItemsAPI.getExpiring.mockImplementation(() => new Promise(() => {}));
    mockItemsAPI.getExpired.mockImplementation(() => new Promise(() => {}));

    renderDashboard();

    expect(screen.getByTestId('loading')).toBeInTheDocument();
  });

  it('displays correct statistics after loading', async () => {
    mockItemsAPI.getExpiring.mockResolvedValue(mockExpiringItems);
    mockItemsAPI.getExpired.mockResolvedValue(mockExpiredItems);

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByTestId('expiring-count')).toHaveTextContent('2');
      expect(screen.getByTestId('expired-count')).toHaveTextContent('1');
    });
  });

  it('displays expiring items section when items exist', async () => {
    mockItemsAPI.getExpiring.mockResolvedValue(mockExpiringItems);
    mockItemsAPI.getExpired.mockResolvedValue([]);

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByText('Bald ablaufende Artikel')).toBeInTheDocument();
      expect(screen.getByText('Chicken Breast')).toBeInTheDocument();
      expect(screen.getByText('Milk')).toBeInTheDocument();
      expect(screen.getByText('Ablaufdatum: 2024-01-15')).toBeInTheDocument();
      expect(screen.getByText('Kategorie: Fleisch')).toBeInTheDocument();
    });
  });

  it('displays expired items section when items exist', async () => {
    mockItemsAPI.getExpiring.mockResolvedValue([]);
    mockItemsAPI.getExpired.mockResolvedValue(mockExpiredItems);

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByText('Abgelaufene Artikel')).toBeInTheDocument();
      expect(screen.getByText('Old Bread')).toBeInTheDocument();
      expect(screen.getByText('Abgelaufen: 2024-01-05')).toBeInTheDocument();
      expect(screen.getByText('Kategorie: Bakery')).toBeInTheDocument();
    });
  });

  it('does not show sections when no items exist', async () => {
    mockItemsAPI.getExpiring.mockResolvedValue([]);
    mockItemsAPI.getExpired.mockResolvedValue([]);

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByTestId('expiring-count')).toHaveTextContent('0');
      expect(screen.getByTestId('expired-count')).toHaveTextContent('0');
    });

    expect(screen.queryByText('Bald ablaufende Artikel')).not.toBeInTheDocument();
    expect(screen.queryByText('Abgelaufene Artikel')).not.toBeInTheDocument();
  });

  it('limits displayed items to 5 for expiring and 3 for expired', async () => {
    const manyExpiringItems = Array.from({ length: 10 }, (_, index) => ({
      ...mockExpiringItems[0],
      id: index + 1,
      name: `Item ${index + 1}`,
    }));

    const manyExpiredItems = Array.from({ length: 5 }, (_, index) => ({
      ...mockExpiredItems[0],
      id: index + 10,
      name: `Expired Item ${index + 1}`,
    }));

    mockItemsAPI.getExpiring.mockResolvedValue(manyExpiringItems);
    mockItemsAPI.getExpired.mockResolvedValue(manyExpiredItems);

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByTestId('expiring-count')).toHaveTextContent('10');
      expect(screen.getByTestId('expired-count')).toHaveTextContent('5');
    });

    // Should show only 5 expiring items
    expect(screen.getByText('Item 1')).toBeInTheDocument();
    expect(screen.getByText('Item 5')).toBeInTheDocument();
    expect(screen.queryByText('Item 6')).not.toBeInTheDocument();

    // Should show only 3 expired items
    expect(screen.getByText('Expired Item 1')).toBeInTheDocument();
    expect(screen.getByText('Expired Item 3')).toBeInTheDocument();
    expect(screen.queryByText('Expired Item 4')).not.toBeInTheDocument();
  });

  it('calls logout function when logout button is clicked', async () => {
    const user = userEvent.setup();
    mockItemsAPI.getExpiring.mockResolvedValue([]);
    mockItemsAPI.getExpired.mockResolvedValue([]);

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByTestId('expiring-count')).toBeInTheDocument();
    });

    const logoutButton = screen.getByText('Abmelden');
    await user.click(logoutButton);

    expect(mockLogout).toHaveBeenCalledTimes(1);
  });

  it('displays error message when API calls fail', async () => {
    mockItemsAPI.getExpiring.mockRejectedValue(new Error('API Error'));
    mockItemsAPI.getExpired.mockRejectedValue(new Error('API Error'));

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Fehler beim Laden der Daten');
    });

    expect(screen.getByTestId('expiring-count')).toHaveTextContent('0');
    expect(screen.getByTestId('expired-count')).toHaveTextContent('0');
  });

  it('contains navigation link to items page', async () => {
    mockItemsAPI.getExpiring.mockResolvedValue([]);
    mockItemsAPI.getExpired.mockResolvedValue([]);

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByTestId('expiring-count')).toBeInTheDocument();
    });

    const itemsLink = screen.getByRole('link', { name: 'Alle Artikel verwalten' });
    expect(itemsLink).toBeInTheDocument();
    expect(itemsLink).toHaveAttribute('href', '/items');
  });

  it('handles partial API failures gracefully', async () => {
    mockItemsAPI.getExpiring.mockResolvedValue(mockExpiringItems);
    mockItemsAPI.getExpired.mockRejectedValue(new Error('Expired items API failed'));

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Fehler beim Laden der Daten');
    });

    // Should still show the statistics as 0 for both since the Promise.all failed
    expect(screen.getByTestId('expiring-count')).toHaveTextContent('0');
    expect(screen.getByTestId('expired-count')).toHaveTextContent('0');
  });
});