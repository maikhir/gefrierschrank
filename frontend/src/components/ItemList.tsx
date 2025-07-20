import React, { useEffect, useState } from 'react';
import { itemsAPI } from '../services/api';
import { Item } from '../types';
import { Link } from 'react-router-dom';

const ItemList: React.FC = () => {
  const [items, setItems] = useState<Item[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [filteredItems, setFilteredItems] = useState<Item[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadItems = async () => {
      try {
        const data = await itemsAPI.getAll();
        setItems(data);
        setFilteredItems(data);
      } catch (error) {
        setError('Fehler beim Laden der Artikel');
        console.error('Error loading items:', error);
      } finally {
        setIsLoading(false);
      }
    };

    loadItems();
  }, []);

  useEffect(() => {
    if (searchTerm.trim() === '') {
      setFilteredItems(items);
    } else {
      const filtered = items.filter(item =>
        item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        item.categoryName.toLowerCase().includes(searchTerm.toLowerCase())
      );
      setFilteredItems(filtered);
    }
  }, [searchTerm, items]);

  const handleDelete = async (id: number) => {
    if (window.confirm('Sind Sie sicher, dass Sie diesen Artikel löschen möchten?')) {
      try {
        await itemsAPI.delete(id);
        const updatedItems = items.filter(item => item.id !== id);
        setItems(updatedItems);
        setFilteredItems(updatedItems.filter(item =>
          searchTerm.trim() === '' ||
          item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
          item.categoryName.toLowerCase().includes(searchTerm.toLowerCase())
        ));
      } catch (error) {
        setError('Fehler beim Löschen des Artikels');
        console.error('Error deleting item:', error);
      }
    }
  };

  if (isLoading) {
    return <div data-testid="loading">Laden...</div>;
  }

  return (
    <div className="item-list-container">
      <header className="page-header">
        <h1>Alle Artikel</h1>
        <Link to="/" className="back-link">← Zurück zum Dashboard</Link>
      </header>

      {error && (
        <div className="error-message" role="alert">
          {error}
        </div>
      )}

      <div className="search-bar">
        <input
          type="text"
          placeholder="Artikel durchsuchen..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          data-testid="search-input"
        />
      </div>

      <div className="items-count" data-testid="items-count">
        {filteredItems.length} von {items.length} Artikeln
      </div>

      {filteredItems.length === 0 ? (
        <div className="no-items" data-testid="no-items">
          {searchTerm ? 'Keine Artikel gefunden.' : 'Keine Artikel vorhanden.'}
        </div>
      ) : (
        <div className="items-grid">
          {filteredItems.map((item) => (
            <div key={item.id} className="item-card" data-testid={`item-${item.id}`}>
              <h3>{item.name}</h3>
              <p><strong>Kategorie:</strong> {item.categoryName}</p>
              <p><strong>Menge:</strong> {item.quantity} {item.unit}</p>
              <p><strong>Ablaufdatum:</strong> {item.expiryDate}</p>
              <p><strong>Typ:</strong> {item.expiryType}</p>
              {item.description && (
                <p><strong>Beschreibung:</strong> {item.description}</p>
              )}
              
              <div className="item-actions">
                <button
                  onClick={() => handleDelete(item.id)}
                  className="delete-button"
                  data-testid={`delete-${item.id}`}
                >
                  Löschen
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ItemList;