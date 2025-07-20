import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import ItemList from '../ItemList';
import { itemsAPI } from '../../services/api';
import { Item } from '../../types';

// Mock API
vi.mock('../../services/api', () => ({
  itemsAPI: {
    getAll: vi.fn(),
    delete: vi.fn(),
  },
}));

const mockItemsAPI = itemsAPI as any;

const mockItems: Item[] = [
  {
    id: 1,
    name: 'Chicken Breast',
    categoryId: 1,
    categoryName: 'Fleisch',
    quantity: 1.5,
    unit: 'kg',
    expiryDate: '2024-01-15',
    expiryType: 'MHD',
    description: 'Fresh chicken breast',
    createdAt: '2024-01-08T10:00:00',
  },
  {
    id: 2,
    name: 'Carrots',
    categoryId: 2,
    categoryName: 'Gemüse',
    quantity: 500,
    unit: 'g',
    expiryDate: '2024-01-20',
    expiryType: 'VD',
    description: 'Fresh carrots',
    createdAt: '2024-01-07T10:00:00',
  },
  {
    id: 3,
    name: 'Beef Steak',
    categoryId: 1,
    categoryName: 'Fleisch',
    quantity: 2,
    unit: 'kg',
    expiryDate: '2024-01-18',
    expiryType: 'MHD',
    createdAt: '2024-01-06T10:00:00',
  },
];

const renderItemList = () => {
  return render(
    <BrowserRouter>
      <ItemList />
    </BrowserRouter>
  );
};

// Mock window.confirm
global.confirm = vi.fn();

describe('ItemList Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders page header correctly', async () => {
    mockItemsAPI.getAll.mockResolvedValue(mockItems);

    renderItemList();

    expect(screen.getByText('Alle Artikel')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: '← Zurück zum Dashboard' })).toBeInTheDocument();
  });

  it('shows loading state initially', () => {
    mockItemsAPI.getAll.mockImplementation(() => new Promise(() => {}));

    renderItemList();

    expect(screen.getByTestId('loading')).toBeInTheDocument();
  });

  it('displays all items after loading', async () => {
    mockItemsAPI.getAll.mockResolvedValue(mockItems);

    renderItemList();

    await waitFor(() => {
      expect(screen.getByText('Chicken Breast')).toBeInTheDocument();
      expect(screen.getByText('Carrots')).toBeInTheDocument();
      expect(screen.getByText('Beef Steak')).toBeInTheDocument();
    });

    expect(screen.getByTestId('items-count')).toHaveTextContent('3 von 3 Artikeln');
  });

  it('displays item details correctly', async () => {
    mockItemsAPI.getAll.mockResolvedValue(mockItems);

    renderItemList();

    await waitFor(() => {
      expect(screen.getByText('Chicken Breast')).toBeInTheDocument();
    });

    // Check details for first item
    expect(screen.getByText('Kategorie: Fleisch')).toBeInTheDocument();
    expect(screen.getByText('Menge: 1.5 kg')).toBeInTheDocument();
    expect(screen.getByText('Ablaufdatum: 2024-01-15')).toBeInTheDocument();
    expect(screen.getByText('Typ: MHD')).toBeInTheDocument();
    expect(screen.getByText('Beschreibung: Fresh chicken breast')).toBeInTheDocument();
  });

  it('filters items by search term', async () => {
    const user = userEvent.setup();
    mockItemsAPI.getAll.mockResolvedValue(mockItems);

    renderItemList();

    await waitFor(() => {
      expect(screen.getByText('Chicken Breast')).toBeInTheDocument();
    });

    const searchInput = screen.getByTestId('search-input');
    await user.type(searchInput, 'chicken');

    expect(screen.getByText('Chicken Breast')).toBeInTheDocument();
    expect(screen.queryByText('Carrots')).not.toBeInTheDocument();
    expect(screen.queryByText('Beef Steak')).not.toBeInTheDocument();
    expect(screen.getByTestId('items-count')).toHaveTextContent('1 von 3 Artikeln');
  });

  it('filters items by category name', async () => {
    const user = userEvent.setup();
    mockItemsAPI.getAll.mockResolvedValue(mockItems);

    renderItemList();

    await waitFor(() => {
      expect(screen.getByText('Chicken Breast')).toBeInTheDocument();
    });

    const searchInput = screen.getByTestId('search-input');
    await user.type(searchInput, 'fleisch');

    expect(screen.getByText('Chicken Breast')).toBeInTheDocument();
    expect(screen.getByText('Beef Steak')).toBeInTheDocument();
    expect(screen.queryByText('Carrots')).not.toBeInTheDocument();
    expect(screen.getByTestId('items-count')).toHaveTextContent('2 von 3 Artikeln');
  });

  it('search is case insensitive', async () => {
    const user = userEvent.setup();
    mockItemsAPI.getAll.mockResolvedValue(mockItems);

    renderItemList();

    await waitFor(() => {
      expect(screen.getByText('Chicken Breast')).toBeInTheDocument();
    });

    const searchInput = screen.getByTestId('search-input');
    await user.type(searchInput, 'CHICKEN');

    expect(screen.getByText('Chicken Breast')).toBeInTheDocument();
    expect(screen.queryByText('Carrots')).not.toBeInTheDocument();
  });

  it('shows no items message when search returns no results', async () => {
    const user = userEvent.setup();
    mockItemsAPI.getAll.mockResolvedValue(mockItems);

    renderItemList();

    await waitFor(() => {
      expect(screen.getByText('Chicken Breast')).toBeInTheDocument();
    });

    const searchInput = screen.getByTestId('search-input');
    await user.type(searchInput, 'nonexistent');

    expect(screen.getByTestId('no-items')).toHaveTextContent('Keine Artikel gefunden.');
    expect(screen.getByTestId('items-count')).toHaveTextContent('0 von 3 Artikeln');
  });

  it('shows no items message when no items exist', async () => {
    mockItemsAPI.getAll.mockResolvedValue([]);

    renderItemList();

    await waitFor(() => {
      expect(screen.getByTestId('no-items')).toHaveTextContent('Keine Artikel vorhanden.');
    });

    expect(screen.getByTestId('items-count')).toHaveTextContent('0 von 0 Artikeln');
  });

  it('clears search filter correctly', async () => {
    const user = userEvent.setup();
    mockItemsAPI.getAll.mockResolvedValue(mockItems);

    renderItemList();

    await waitFor(() => {
      expect(screen.getByText('Chicken Breast')).toBeInTheDocument();
    });

    const searchInput = screen.getByTestId('search-input');
    
    // Type search term
    await user.type(searchInput, 'chicken');
    expect(screen.getByTestId('items-count')).toHaveTextContent('1 von 3 Artikeln');
    
    // Clear search
    await user.clear(searchInput);
    expect(screen.getByTestId('items-count')).toHaveTextContent('3 von 3 Artikeln');
    expect(screen.getByText('Carrots')).toBeInTheDocument();
  });

  it('deletes item when confirmed', async () => {
    const user = userEvent.setup();
    mockItemsAPI.getAll.mockResolvedValue(mockItems);
    mockItemsAPI.delete.mockResolvedValue(undefined);
    (global.confirm as any).mockReturnValue(true);

    renderItemList();

    await waitFor(() => {
      expect(screen.getByText('Chicken Breast')).toBeInTheDocument();
    });

    const deleteButton = screen.getByTestId('delete-1');
    await user.click(deleteButton);

    expect(global.confirm).toHaveBeenCalledWith('Sind Sie sicher, dass Sie diesen Artikel löschen möchten?');
    expect(mockItemsAPI.delete).toHaveBeenCalledWith(1);

    await waitFor(() => {
      expect(screen.queryByText('Chicken Breast')).not.toBeInTheDocument();
      expect(screen.getByTestId('items-count')).toHaveTextContent('2 von 2 Artikeln');
    });
  });

  it('does not delete item when not confirmed', async () => {
    const user = userEvent.setup();
    mockItemsAPI.getAll.mockResolvedValue(mockItems);
    (global.confirm as any).mockReturnValue(false);

    renderItemList();

    await waitFor(() => {
      expect(screen.getByText('Chicken Breast')).toBeInTheDocument();
    });

    const deleteButton = screen.getByTestId('delete-1');
    await user.click(deleteButton);

    expect(global.confirm).toHaveBeenCalled();
    expect(mockItemsAPI.delete).not.toHaveBeenCalled();
    expect(screen.getByText('Chicken Breast')).toBeInTheDocument();
  });

  it('shows error message when delete fails', async () => {
    const user = userEvent.setup();
    mockItemsAPI.getAll.mockResolvedValue(mockItems);
    mockItemsAPI.delete.mockRejectedValue(new Error('Delete failed'));
    (global.confirm as any).mockReturnValue(true);

    renderItemList();

    await waitFor(() => {
      expect(screen.getByText('Chicken Breast')).toBeInTheDocument();
    });

    const deleteButton = screen.getByTestId('delete-1');
    await user.click(deleteButton);

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Fehler beim Löschen des Artikels');
    });

    // Item should still be there
    expect(screen.getByText('Chicken Breast')).toBeInTheDocument();
  });

  it('shows error message when loading items fails', async () => {
    mockItemsAPI.getAll.mockRejectedValue(new Error('Loading failed'));

    renderItemList();

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Fehler beim Laden der Artikel');
    });

    expect(screen.getByTestId('no-items')).toHaveTextContent('Keine Artikel vorhanden.');
  });

  it('updates filtered items after deletion', async () => {
    const user = userEvent.setup();
    mockItemsAPI.getAll.mockResolvedValue(mockItems);
    mockItemsAPI.delete.mockResolvedValue(undefined);
    (global.confirm as any).mockReturnValue(true);

    renderItemList();

    await waitFor(() => {
      expect(screen.getByText('Chicken Breast')).toBeInTheDocument();
    });

    // Filter for meat items
    const searchInput = screen.getByTestId('search-input');
    await user.type(searchInput, 'fleisch');

    expect(screen.getByTestId('items-count')).toHaveTextContent('2 von 3 Artikeln');

    // Delete one meat item
    const deleteButton = screen.getByTestId('delete-1');
    await user.click(deleteButton);

    await waitFor(() => {
      expect(screen.getByTestId('items-count')).toHaveTextContent('1 von 2 Artikeln');
      expect(screen.queryByText('Chicken Breast')).not.toBeInTheDocument();
      expect(screen.getByText('Beef Steak')).toBeInTheDocument();
    });
  });

  it('handles items without description', async () => {
    const itemsWithoutDescription = mockItems.map(item => ({ ...item, description: undefined }));
    mockItemsAPI.getAll.mockResolvedValue(itemsWithoutDescription);

    renderItemList();

    await waitFor(() => {
      expect(screen.getByText('Chicken Breast')).toBeInTheDocument();
    });

    expect(screen.queryByText('Beschreibung:')).not.toBeInTheDocument();
  });

  it('renders correct number of item cards', async () => {
    mockItemsAPI.getAll.mockResolvedValue(mockItems);

    renderItemList();

    await waitFor(() => {
      expect(screen.getByTestId('item-1')).toBeInTheDocument();
      expect(screen.getByTestId('item-2')).toBeInTheDocument();
      expect(screen.getByTestId('item-3')).toBeInTheDocument();
    });
  });
});