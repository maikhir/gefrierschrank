import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import { categoryApi, itemApi } from '../services/api';
import { CreateItemRequest, UpdateItemRequest } from '../types/api';
import { ArrowLeft, Save } from 'lucide-react';

function ItemForm() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = !!id;

  const [formData, setFormData] = useState<CreateItemRequest>({
    name: '',
    categoryId: 0,
    quantity: 0,
    unit: '',
    expiryDate: '',
    expiryType: 'BEST_BEFORE',
    photoPath: '',
    description: ''
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  // Fetch categories
  const { data: categories, isLoading: categoriesLoading } = useQuery({
    queryKey: ['categories'],
    queryFn: categoryApi.getAll,
  });

  // Fetch item for editing
  const { data: item, isLoading: itemLoading } = useQuery({
    queryKey: ['item', id],
    queryFn: () => itemApi.getById(Number(id)),
    enabled: isEdit,
  });

  // Mutations
  const createMutation = useMutation({
    mutationFn: itemApi.create,
    onSuccess: () => {
      navigate('/items');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateItemRequest }) =>
      itemApi.update(id, data),
    onSuccess: () => {
      navigate('/items');
    },
  });

  // Load item data for editing
  useEffect(() => {
    if (item) {
      setFormData({
        name: item.name,
        categoryId: item.categoryId,
        quantity: item.quantity,
        unit: item.unit,
        expiryDate: item.expiryDate || '',
        expiryType: item.expiryType,
        photoPath: item.photoPath || '',
        description: item.description || ''
      });
    }
  }, [item]);

  // Update unit when category changes
  useEffect(() => {
    if (formData.categoryId && categories) {
      const selectedCategory = categories.find(cat => cat.id === formData.categoryId);
      if (selectedCategory && !isEdit) {
        setFormData(prev => ({
          ...prev,
          unit: selectedCategory.defaultUnit,
          quantity: selectedCategory.unitStep
        }));
      }
    }
  }, [formData.categoryId, categories, isEdit]);

  const selectedCategory = categories?.find(cat => cat.id === formData.categoryId);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Name ist erforderlich';
    }

    if (!formData.categoryId) {
      newErrors.categoryId = 'Kategorie ist erforderlich';
    }

    if (!formData.quantity || formData.quantity <= 0) {
      newErrors.quantity = 'Menge muss größer als 0 sein';
    }

    if (selectedCategory) {
      if (formData.quantity < selectedCategory.minValue) {
        newErrors.quantity = `Menge muss mindestens ${selectedCategory.minValue} sein`;
      }
      
      if (formData.quantity > selectedCategory.maxValue) {
        newErrors.quantity = `Menge darf maximal ${selectedCategory.maxValue} sein`;
      }

      // Check unit step alignment
      const remainder = (formData.quantity - selectedCategory.minValue) % selectedCategory.unitStep;
      if (Math.abs(remainder) > 0.001) {
        newErrors.quantity = `Menge muss in ${selectedCategory.unitStep}-Schritten erfolgen`;
      }
    }

    if (!formData.unit.trim()) {
      newErrors.unit = 'Einheit ist erforderlich';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    const submitData = {
      ...formData,
      expiryDate: formData.expiryDate || undefined
    };

    if (isEdit) {
      updateMutation.mutate({ id: Number(id), data: submitData });
    } else {
      createMutation.mutate(submitData);
    }
  };

  const handleQuantityChange = (value: number) => {
    if (selectedCategory) {
      // Ensure the quantity aligns with unit step
      const adjustedValue = Math.round(value / selectedCategory.unitStep) * selectedCategory.unitStep;
      const clampedValue = Math.max(
        selectedCategory.minValue,
        Math.min(selectedCategory.maxValue, adjustedValue)
      );
      setFormData(prev => ({ ...prev, quantity: clampedValue }));
    } else {
      setFormData(prev => ({ ...prev, quantity: value }));
    }
  };

  if (categoriesLoading || (isEdit && itemLoading)) {
    return (
      <div style={{ textAlign: 'center', padding: '2rem' }}>
        <div style={{ color: 'rgba(255, 255, 255, 0.8)' }}>Lade Formular...</div>
      </div>
    );
  }

  return (
    <div>
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '1rem',
        marginBottom: '2rem'
      }}>
        <button
          onClick={() => navigate('/items')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.5rem',
            background: 'transparent',
            color: 'rgba(255, 255, 255, 0.8)',
            border: '1px solid rgba(255, 255, 255, 0.3)',
            borderRadius: '8px',
            cursor: 'pointer'
          }}
        >
          <ArrowLeft size={18} />
        </button>
        <h1 style={{ color: '#fff', margin: 0 }}>
          {isEdit ? 'Artikel bearbeiten' : 'Neuer Artikel'}
        </h1>
      </div>

      <form onSubmit={handleSubmit} className="form">
        <div className="form-group">
          <label htmlFor="name">Name *</label>
          <input
            id="name"
            type="text"
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            placeholder="z.B. Tiefkühlpizza Margherita"
          />
          {errors.name && <div style={{ color: '#ff4444', fontSize: '0.9rem', marginTop: '0.5rem' }}>{errors.name}</div>}
        </div>

        <div className="form-group">
          <label htmlFor="category">Kategorie *</label>
          <select
            id="category"
            value={formData.categoryId}
            onChange={(e) => setFormData({ ...formData, categoryId: Number(e.target.value) })}
          >
            <option value={0}>Kategorie wählen</option>
            {categories?.map((category) => (
              <option key={category.id} value={category.id}>
                {category.icon} {category.name}
              </option>
            ))}
          </select>
          {errors.categoryId && <div style={{ color: '#ff4444', fontSize: '0.9rem', marginTop: '0.5rem' }}>{errors.categoryId}</div>}
        </div>

        {selectedCategory && (
          <div style={{
            background: 'rgba(100, 108, 255, 0.1)',
            border: '1px solid #646cff',
            borderRadius: '8px',
            padding: '1rem',
            marginBottom: '1rem'
          }}>
            <div style={{ color: '#646cff', fontWeight: 'bold', marginBottom: '0.5rem' }}>
              Kategorie-Einstellungen:
            </div>
            <div style={{ color: 'rgba(255, 255, 255, 0.8)', fontSize: '0.9rem' }}>
              Einheit: {selectedCategory.defaultUnit} • 
              Schritte: {selectedCategory.unitStep} • 
              Min: {selectedCategory.minValue} • 
              Max: {selectedCategory.maxValue}
            </div>
          </div>
        )}

        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '1rem' }}>
          <div className="form-group">
            <label htmlFor="quantity">Menge *</label>
            <input
              id="quantity"
              type="number"
              value={formData.quantity}
              onChange={(e) => handleQuantityChange(Number(e.target.value))}
              step={selectedCategory?.unitStep || 0.1}
              min={selectedCategory?.minValue || 0}
              max={selectedCategory?.maxValue || 999999}
            />
            {errors.quantity && <div style={{ color: '#ff4444', fontSize: '0.9rem', marginTop: '0.5rem' }}>{errors.quantity}</div>}
          </div>

          <div className="form-group">
            <label htmlFor="unit">Einheit *</label>
            <input
              id="unit"
              type="text"
              value={formData.unit}
              onChange={(e) => setFormData({ ...formData, unit: e.target.value })}
              placeholder="kg, L, Stück"
            />
            {errors.unit && <div style={{ color: '#ff4444', fontSize: '0.9rem', marginTop: '0.5rem' }}>{errors.unit}</div>}
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
          <div className="form-group">
            <label htmlFor="expiryDate">Ablaufdatum</label>
            <input
              id="expiryDate"
              type="date"
              value={formData.expiryDate}
              onChange={(e) => setFormData({ ...formData, expiryDate: e.target.value })}
              min={new Date().toISOString().split('T')[0]}
            />
          </div>

          <div className="form-group">
            <label htmlFor="expiryType">Ablauf-Art</label>
            <select
              id="expiryType"
              value={formData.expiryType}
              onChange={(e) => setFormData({ ...formData, expiryType: e.target.value as 'USE_BY' | 'BEST_BEFORE' })}
            >
              <option value="BEST_BEFORE">Mindesthaltbar bis</option>
              <option value="USE_BY">Zu verbrauchen bis</option>
            </select>
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="photoPath">Foto-Pfad</label>
          <input
            id="photoPath"
            type="text"
            value={formData.photoPath}
            onChange={(e) => setFormData({ ...formData, photoPath: e.target.value })}
            placeholder="Optional: Pfad zum Foto"
          />
        </div>

        <div className="form-group">
          <label htmlFor="description">Beschreibung</label>
          <textarea
            id="description"
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            placeholder="Optional: Zusätzliche Informationen"
            rows={3}
          />
        </div>

        <div style={{ display: 'flex', gap: '1rem', marginTop: '2rem' }}>
          <button
            type="button"
            onClick={() => navigate('/items')}
            style={{
              padding: '0.75rem 1.5rem',
              background: 'transparent',
              color: 'rgba(255, 255, 255, 0.8)',
              border: '1px solid rgba(255, 255, 255, 0.3)',
              borderRadius: '8px',
              cursor: 'pointer'
            }}
          >
            Abbrechen
          </button>
          
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
              cursor: createMutation.isPending || updateMutation.isPending ? 'not-allowed' : 'pointer',
              opacity: createMutation.isPending || updateMutation.isPending ? 0.7 : 1
            }}
          >
            <Save size={18} />
            {createMutation.isPending || updateMutation.isPending
              ? 'Speichern...'
              : isEdit
              ? 'Änderungen speichern'
              : 'Artikel erstellen'
            }
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
            Fehler beim Speichern: {(createMutation.error as Error)?.message || (updateMutation.error as Error)?.message}
          </div>
        )}
      </form>
    </div>
  );
}

export default ItemForm;