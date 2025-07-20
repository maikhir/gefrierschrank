import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { categoryApi } from '../services/api';
import { Category } from '../types/api';
import { Plus, Edit, Trash2, Settings, Save, X } from 'lucide-react';

function CategoryList() {
  const queryClient = useQueryClient();
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [isCreating, setIsCreating] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    icon: '',
    defaultUnit: '',
    unitStep: 1,
    minValue: 0,
    maxValue: 100
  });

  // Fetch categories
  const { data: categories, isLoading } = useQuery({
    queryKey: ['categories'],
    queryFn: categoryApi.getAll,
  });

  // Mutations
  const createMutation = useMutation({
    mutationFn: categoryApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
      resetForm();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Omit<Category, 'id' | 'createdAt' | 'updatedAt'> }) =>
      categoryApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
      resetForm();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: categoryApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
    },
  });

  const resetForm = () => {
    setFormData({
      name: '',
      icon: '',
      defaultUnit: '',
      unitStep: 1,
      minValue: 0,
      maxValue: 100
    });
    setEditingCategory(null);
    setIsCreating(false);
  };

  const startEdit = (category: Category) => {
    setFormData({
      name: category.name,
      icon: category.icon,
      defaultUnit: category.defaultUnit,
      unitStep: category.unitStep,
      minValue: category.minValue,
      maxValue: category.maxValue
    });
    setEditingCategory(category);
    setIsCreating(false);
  };

  const startCreate = () => {
    resetForm();
    setIsCreating(true);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (editingCategory) {
      updateMutation.mutate({
        id: editingCategory.id,
        data: formData
      });
    } else {
      createMutation.mutate(formData);
    }
  };

  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: '2rem' }}>
        <div style={{ color: 'rgba(255, 255, 255, 0.8)' }}>Lade Kategorien...</div>
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
        <h1 style={{ color: '#fff', margin: 0, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Settings size={24} />
          Kategorien ({categories?.length || 0})
        </h1>
        <button
          onClick={startCreate}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.75rem 1.5rem',
            background: '#646cff',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer',
            fontSize: '1rem'
          }}
        >
          <Plus size={20} />
          Neue Kategorie
        </button>
      </div>

      {/* Create/Edit Form */}
      {(isCreating || editingCategory) && (
        <div style={{
          background: 'rgba(255, 255, 255, 0.1)',
          padding: '2rem',
          borderRadius: '12px',
          marginBottom: '2rem',
          border: '1px solid rgba(255, 255, 255, 0.2)'
        }}>
          <h3 style={{ color: '#fff', marginTop: 0, marginBottom: '1.5rem' }}>
            {editingCategory ? 'Kategorie bearbeiten' : 'Neue Kategorie erstellen'}
          </h3>
          
          <form onSubmit={handleSubmit}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
              <div className="form-group">
                <label>Name *</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="z.B. Fleisch"
                  required
                />
              </div>

              <div className="form-group">
                <label>Icon</label>
                <input
                  type="text"
                  value={formData.icon}
                  onChange={(e) => setFormData({ ...formData, icon: e.target.value })}
                  placeholder="z.B. 🥩"
                  style={{ fontSize: '1.2rem' }}
                />
              </div>

              <div className="form-group">
                <label>Standard-Einheit *</label>
                <input
                  type="text"
                  value={formData.defaultUnit}
                  onChange={(e) => setFormData({ ...formData, defaultUnit: e.target.value })}
                  placeholder="z.B. kg, L, Stück"
                  required
                />
              </div>

              <div className="form-group">
                <label>Schritt-Größe *</label>
                <input
                  type="number"
                  value={formData.unitStep}
                  onChange={(e) => setFormData({ ...formData, unitStep: Number(e.target.value) })}
                  step="0.01"
                  min="0.01"
                  required
                />
              </div>

              <div className="form-group">
                <label>Minimum *</label>
                <input
                  type="number"
                  value={formData.minValue}
                  onChange={(e) => setFormData({ ...formData, minValue: Number(e.target.value) })}
                  step="0.01"
                  min="0"
                  required
                />
              </div>

              <div className="form-group">
                <label>Maximum *</label>
                <input
                  type="number"
                  value={formData.maxValue}
                  onChange={(e) => setFormData({ ...formData, maxValue: Number(e.target.value) })}
                  step="0.01"
                  min="0.01"
                  required
                />
              </div>
            </div>

            <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem' }}>
              <button
                type="submit"
                disabled={createMutation.isPending || updateMutation.isPending}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  padding: '0.75rem 1.5rem',
                  background: '#646cff',
                  color: 'white',
                  border: 'none',
                  borderRadius: '8px',
                  cursor: 'pointer'
                }}
              >
                <Save size={18} />
                {editingCategory ? 'Änderungen speichern' : 'Kategorie erstellen'}
              </button>
              
              <button
                type="button"
                onClick={resetForm}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  padding: '0.75rem 1.5rem',
                  background: 'transparent',
                  color: 'rgba(255, 255, 255, 0.8)',
                  border: '1px solid rgba(255, 255, 255, 0.3)',
                  borderRadius: '8px',
                  cursor: 'pointer'
                }}
              >
                <X size={18} />
                Abbrechen
              </button>
            </div>

            {(createMutation.error || updateMutation.error) && (
              <div style={{
                background: 'rgba(255, 68, 68, 0.1)',
                border: '1px solid #ff4444',
                borderRadius: '8px',
                padding: '0.75rem',
                marginTop: '1rem',
                color: '#ff4444'
              }}>
                Fehler: {(createMutation.error as Error)?.message || (updateMutation.error as Error)?.message}
              </div>
            )}
          </form>
        </div>
      )}

      {/* Categories List */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '1rem' }}>
        {categories?.map((category) => (
          <div key={category.id} style={{
            background: 'rgba(255, 255, 255, 0.1)',
            border: '1px solid rgba(255, 255, 255, 0.2)',
            borderRadius: '12px',
            padding: '1.5rem'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <span style={{ fontSize: '1.5rem' }}>{category.icon}</span>
                <h3 style={{ margin: 0, color: '#fff' }}>{category.name}</h3>
              </div>
              
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <button
                  onClick={() => startEdit(category)}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.25rem',
                    padding: '0.5rem',
                    background: 'rgba(100, 108, 255, 0.1)',
                    border: '1px solid #646cff',
                    borderRadius: '6px',
                    color: '#646cff',
                    cursor: 'pointer',
                    fontSize: '0.9rem'
                  }}
                >
                  <Edit size={14} />
                </button>
                
                <button
                  onClick={() => {
                    if (window.confirm(`Kategorie "${category.name}" wirklich löschen? Dies wird auch alle zugehörigen Artikel betreffen.`)) {
                      deleteMutation.mutate(category.id);
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
                </button>
              </div>
            </div>

            <div style={{ color: 'rgba(255, 255, 255, 0.8)', fontSize: '0.9rem', lineHeight: 1.6 }}>
              <div><strong>Einheit:</strong> {category.defaultUnit}</div>
              <div><strong>Schritte:</strong> {category.unitStep}</div>
              <div><strong>Bereich:</strong> {category.minValue} - {category.maxValue}</div>
              <div><strong>Erstellt:</strong> {new Date(category.createdAt).toLocaleDateString('de-DE')}</div>
            </div>
          </div>
        ))}
      </div>

      {(!categories || categories.length === 0) && !isCreating && (
        <div style={{
          textAlign: 'center',
          padding: '3rem',
          background: 'rgba(255, 255, 255, 0.1)',
          borderRadius: '12px'
        }}>
          <Settings size={48} style={{ color: 'rgba(255, 255, 255, 0.5)', marginBottom: '1rem' }} />
          <h3 style={{ color: 'rgba(255, 255, 255, 0.8)', marginBottom: '1rem' }}>
            Noch keine Kategorien vorhanden
          </h3>
          <p style={{ color: 'rgba(255, 255, 255, 0.6)', marginBottom: '2rem' }}>
            Erstellen Sie Kategorien, um Ihre Artikel besser zu organisieren.
          </p>
          <button
            onClick={startCreate}
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.5rem',
              padding: '0.75rem 1.5rem',
              background: '#646cff',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '1rem'
            }}
          >
            <Plus size={20} />
            Erste Kategorie erstellen
          </button>
        </div>
      )}
    </div>
  );
}

export default CategoryList;