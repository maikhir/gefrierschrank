import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { itemApi, categoryApi } from '../services/api';
import { Search, Filter, Plus, Edit, Trash2, Calendar, Package } from 'lucide-react';

function ItemList() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();
  const [searchTerm, setSearchTerm] = useState(searchParams.get('search') || '');
  const [categoryFilter, setCategoryFilter] = useState(Number(searchParams.get('categoryId')) || 0);
  const [expiryFilter, setExpiryFilter] = useState(searchParams.get('filter') || '');
  
  // Fetch data
  const { data: items, isLoading: itemsLoading } = useQuery({
    queryKey: ['items-filtered', { categoryFilter, searchTerm, expiryFilter }],
    queryFn: () => {
      if (expiryFilter === 'expired') {
        return itemApi.getExpired();
      }
      if (expiryFilter === 'expiring') {
        return itemApi.getExpiring(7);
      }
      if (searchTerm) {
        return itemApi.search(searchTerm);
      }
      if (categoryFilter) {
        return itemApi.getByCategory(categoryFilter);
      }
      return itemApi.getAll();
    },
  });

  const { data: categories } = useQuery({
    queryKey: ['categories'],
    queryFn: categoryApi.getAll,
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: itemApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['items-filtered'] });
      queryClient.invalidateQueries({ queryKey: ['item-stats'] });
    },
  });

  const handleSearch = (term: string) => {
    setSearchTerm(term);
    updateSearchParams({ search: term || undefined });
  };

  const handleCategoryFilter = (categoryId: number) => {
    setCategoryFilter(categoryId);
    updateSearchParams({ categoryId: categoryId || undefined });
  };

  const handleExpiryFilter = (filter: string) => {
    setExpiryFilter(filter);
    updateSearchParams({ filter: filter || undefined });
  };

  const updateSearchParams = (updates: Record<string, string | undefined>) => {
    const newParams = new URLSearchParams(searchParams);
    Object.entries(updates).forEach(([key, value]) => {
      if (value) {
        newParams.set(key, value);
      } else {
        newParams.delete(key);
      }
    });
    setSearchParams(newParams);
  };

  const clearFilters = () => {
    setSearchTerm('');
    setCategoryFilter(0);
    setExpiryFilter('');
    setSearchParams({});
  };

  const getExpiryBadge = (item: any) => {
    if (item.expired) {
      return { text: 'Abgelaufen', color: '#ff4444' };
    }
    if (item.expiringSoon) {
      return { text: `${item.daysUntilExpiry} Tag${item.daysUntilExpiry !== 1 ? 'e' : ''}`, color: '#ff9500' };
    }
    if (item.expiryDate) {
      return { text: 'Gut', color: '#00c851' };
    }
    return null;
  };

  if (itemsLoading) {
    return (
      <div style={{ textAlign: 'center', padding: '2rem' }}>
        <div style={{ color: 'rgba(255, 255, 255, 0.8)' }}>Lade Artikel...</div>
      </div>
    );
  }

  return (
    <div>
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '2rem'
      }}>
        <h1 style={{ color: '#fff', margin: 0 }}>
          Artikel ({items?.length || 0})
        </h1>
        <Link 
          to="/items/new"
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.75rem 1.5rem',
            background: '#646cff',
            color: 'white',
            textDecoration: 'none',
            borderRadius: '8px',
            fontSize: '1rem'
          }}
        >
          <Plus size={20} />
          Neuer Artikel
        </Link>
      </div>

      {/* Filters */}
      <div style={{
        background: 'rgba(255, 255, 255, 0.1)',
        padding: '1.5rem',
        borderRadius: '12px',
        marginBottom: '2rem',
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
        gap: '1rem'
      }}>
        {/* Search */}
        <div style={{ position: 'relative' }}>
          <Search size={18} style={{
            position: 'absolute',
            left: '0.75rem',
            top: '50%',
            transform: 'translateY(-50%)',
            color: 'rgba(255, 255, 255, 0.5)'
          }} />
          <input
            type="text"
            placeholder="Artikel suchen..."
            value={searchTerm}
            onChange={(e) => handleSearch(e.target.value)}
            style={{
              width: '100%',
              padding: '0.75rem 0.75rem 0.75rem 2.5rem',
              border: '1px solid rgba(255, 255, 255, 0.3)',
              borderRadius: '8px',
              background: 'rgba(255, 255, 255, 0.1)',
              color: 'white',
              fontSize: '1rem'
            }}
          />
        </div>

        {/* Category Filter */}
        <div style={{ position: 'relative' }}>
          <Filter size={18} style={{
            position: 'absolute',
            left: '0.75rem',
            top: '50%',
            transform: 'translateY(-50%)',
            color: 'rgba(255, 255, 255, 0.5)',
            pointerEvents: 'none'
          }} />
          <select
            value={categoryFilter}
            onChange={(e) => handleCategoryFilter(Number(e.target.value))}
            style={{
              width: '100%',
              padding: '0.75rem 0.75rem 0.75rem 2.5rem',
              border: '1px solid rgba(255, 255, 255, 0.3)',
              borderRadius: '8px',
              background: 'rgba(255, 255, 255, 0.1)',
              color: 'white',
              fontSize: '1rem'
            }}
          >
            <option value={0}>Alle Kategorien</option>
            {categories?.map((category) => (
              <option key={category.id} value={category.id}>
                {category.icon} {category.name}
              </option>
            ))}
          </select>
        </div>

        {/* Expiry Filter */}
        <select
          value={expiryFilter}
          onChange={(e) => handleExpiryFilter(e.target.value)}
          style={{
            padding: '0.75rem',
            border: '1px solid rgba(255, 255, 255, 0.3)',
            borderRadius: '8px',
            background: 'rgba(255, 255, 255, 0.1)',
            color: 'white',
            fontSize: '1rem'
          }}
        >
          <option value="">Alle Artikel</option>
          <option value="expiring">Läuft bald ab</option>
          <option value="expired">Abgelaufen</option>
        </select>

        {/* Clear Filters */}
        {(searchTerm || categoryFilter || expiryFilter) && (
          <button
            onClick={clearFilters}
            style={{
              padding: '0.75rem',
              background: 'rgba(255, 255, 255, 0.1)',
              color: 'rgba(255, 255, 255, 0.8)',
              border: '1px solid rgba(255, 255, 255, 0.3)',
              borderRadius: '8px',
              cursor: 'pointer'
            }}
          >
            Filter zurücksetzen
          </button>
        )}
      </div>

      {/* Items Grid */}
      {items && items.length > 0 ? (
        <div className="item-grid">
          {items.map((item) => {
            const badge = getExpiryBadge(item);
            const category = categories?.find(cat => cat.id === item.categoryId);
            
            return (
              <div 
                key={item.id} 
                className={`item-card ${item.expired ? 'expired' : item.expiringSoon ? 'expiring' : ''}`}
              >
                <div className="item-header">
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <span className="category-icon">{category?.icon || '📦'}</span>
                    <div>
                      <h3 style={{ margin: 0, color: '#fff' }}>{item.name}</h3>
                      <div style={{ 
                        fontSize: '0.9rem', 
                        color: 'rgba(255, 255, 255, 0.7)',
                        marginTop: '0.25rem'
                      }}>
                        {item.categoryName}
                      </div>
                    </div>
                  </div>
                  
                  {badge && (
                    <span 
                      className="expiry-badge"
                      style={{ backgroundColor: badge.color }}
                    >
                      {badge.text}
                    </span>
                  )}
                </div>

                <div style={{ 
                  display: 'flex', 
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  margin: '1rem 0',
                  fontSize: '1.1rem',
                  fontWeight: 'bold'
                }}>
                  <span>{item.quantity} {item.unit}</span>
                  {item.expiryDate && (
                    <span style={{ 
                      fontSize: '0.9rem', 
                      color: 'rgba(255, 255, 255, 0.7)',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.25rem'
                    }}>
                      <Calendar size={14} />
                      {new Date(item.expiryDate).toLocaleDateString('de-DE')}
                    </span>
                  )}
                </div>

                {item.description && (
                  <p style={{ 
                    fontSize: '0.9rem', 
                    color: 'rgba(255, 255, 255, 0.8)',
                    margin: '0.5rem 0',
                    lineHeight: 1.4
                  }}>
                    {item.description}
                  </p>
                )}

                <div style={{
                  display: 'flex',
                  gap: '0.5rem',
                  marginTop: '1rem',
                  paddingTop: '1rem',
                  borderTop: '1px solid rgba(255, 255, 255, 0.1)'
                }}>
                  <Link
                    to={`/items/${item.id}/edit`}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.25rem',
                      padding: '0.5rem',
                      background: 'rgba(100, 108, 255, 0.1)',
                      border: '1px solid #646cff',
                      borderRadius: '6px',
                      color: '#646cff',
                      textDecoration: 'none',
                      fontSize: '0.9rem'
                    }}
                  >
                    <Edit size={14} />
                    Bearbeiten
                  </Link>
                  
                  <button
                    onClick={() => {
                      if (window.confirm('Artikel wirklich löschen?')) {
                        deleteMutation.mutate(item.id);
                      }
                    }}
                    disabled={deleteMutation.isPending}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.25rem',
                      padding: '0.5rem',
                      background: 'rgba(255, 68, 68, 0.1)',
                      border: '1px solid #ff4444',
                      borderRadius: '6px',
                      color: '#ff4444',
                      cursor: 'pointer',
                      fontSize: '0.9rem'
                    }}
                  >
                    <Trash2 size={14} />
                    Löschen
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        <div style={{
          textAlign: 'center',
          padding: '3rem',
          background: 'rgba(255, 255, 255, 0.1)',
          borderRadius: '12px'
        }}>
          <Package size={48} style={{ color: 'rgba(255, 255, 255, 0.5)', marginBottom: '1rem' }} />
          <h3 style={{ color: 'rgba(255, 255, 255, 0.8)', marginBottom: '1rem' }}>
            {searchTerm || categoryFilter || expiryFilter 
              ? 'Keine Artikel gefunden' 
              : 'Noch keine Artikel vorhanden'
            }
          </h3>
          <p style={{ color: 'rgba(255, 255, 255, 0.6)', marginBottom: '2rem' }}>
            {searchTerm || categoryFilter || expiryFilter
              ? 'Versuchen Sie andere Filter oder fügen Sie einen neuen Artikel hinzu.'
              : 'Fügen Sie Ihren ersten Artikel hinzu, um mit der Verwaltung zu beginnen.'
            }
          </p>
          <Link 
            to="/items/new"
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.5rem',
              padding: '0.75rem 1.5rem',
              background: '#646cff',
              color: 'white',
              textDecoration: 'none',
              borderRadius: '8px',
              fontSize: '1rem'
            }}
          >
            <Plus size={20} />
            Artikel hinzufügen
          </Link>
        </div>
      )}
    </div>
  );
}

export default ItemList;