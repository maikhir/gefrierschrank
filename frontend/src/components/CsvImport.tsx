import React, { useState, useRef } from 'react';
import { filesAPI, itemsAPI } from '../services/api';
import { CsvUploadResponse, CsvImportResponse, CsvItem } from '../types';

interface CsvImportProps {
  onImportCompleted: (result: CsvImportResponse) => void;
  onError: (error: string) => void;
  className?: string;
}

const CsvImport: React.FC<CsvImportProps> = ({ onImportCompleted, onError, className }) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [isImporting, setIsImporting] = useState(false);
  const [csvData, setCsvData] = useState<CsvUploadResponse['data'] | null>(null);
  const [skipInvalidItems, setSkipInvalidItems] = useState(true);

  const handleFileSelect = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    // Validate file type
    if (!file.name.toLowerCase().endsWith('.csv')) {
      onError('Bitte wählen Sie eine CSV-Datei aus');
      return;
    }

    setIsUploading(true);
    try {
      const response = await filesAPI.uploadCsv(file);
      
      if (response.success && response.data) {
        setCsvData(response.data);
      } else {
        onError(response.error || 'Fehler beim Hochladen der CSV-Datei');
      }
    } catch (error) {
      console.error('Error uploading CSV:', error);
      onError('Fehler beim Hochladen der CSV-Datei');
    } finally {
      setIsUploading(false);
    }
  };

  const handleImport = async () => {
    if (!csvData?.items) return;

    setIsImporting(true);
    try {
      const response = await itemsAPI.importFromCsv({
        items: csvData.items,
        skipInvalidItems
      });
      
      onImportCompleted(response);
      setCsvData(null); // Reset after successful import
      
      // Clear file input
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    } catch (error) {
      console.error('Error importing CSV:', error);
      onError('Fehler beim Importieren der Artikel');
    } finally {
      setIsImporting(false);
    }
  };

  const resetImport = () => {
    setCsvData(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const validItemsCount = csvData?.items.filter(item => item.valid).length || 0;
  const invalidItemsCount = csvData?.items.filter(item => !item.valid).length || 0;

  return (
    <div className={`csv-import ${className || ''}`}>
      {!csvData ? (
        <div className="upload-section">
          <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center">
            <div className="mb-4">
              <span className="text-4xl">📄</span>
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">
              CSV-Datei importieren
            </h3>
            <p className="text-sm text-gray-500 mb-4">
              Wählen Sie eine CSV-Datei mit Artikeln zum Import aus
            </p>
            
            <input
              ref={fileInputRef}
              type="file"
              accept=".csv"
              onChange={handleFileSelect}
              className="hidden"
              id="csv-file-input"
            />
            
            <label
              htmlFor="csv-file-input"
              className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg cursor-pointer inline-block"
            >
              {isUploading ? '⏳ Lade hoch...' : '📂 CSV-Datei auswählen'}
            </label>
            
            <div className="mt-4 text-xs text-gray-400">
              <p>Erwartete Spalten: Name, Kategorie, Menge, Einheit, Ablaufdatum (optional), Beschreibung (optional)</p>
            </div>
          </div>
        </div>
      ) : (
        <div className="preview-section">
          <div className="bg-white rounded-lg shadow-sm border p-6">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-medium text-gray-900">
                Import-Vorschau
              </h3>
              <button
                onClick={resetImport}
                className="text-gray-500 hover:text-gray-700"
                type="button"
              >
                ❌ Abbrechen
              </button>
            </div>
            
            <div className="stats mb-4 grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="text-center">
                <div className="text-2xl font-bold text-blue-600">{csvData.totalItems}</div>
                <div className="text-sm text-gray-500">Gesamt</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-green-600">{validItemsCount}</div>
                <div className="text-sm text-gray-500">Gültig</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-red-600">{invalidItemsCount}</div>
                <div className="text-sm text-gray-500">Fehlerhaft</div>
              </div>
            </div>
            
            {invalidItemsCount > 0 && (
              <div className="mb-4">
                <label className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={skipInvalidItems}
                    onChange={(e) => setSkipInvalidItems(e.target.checked)}
                    className="rounded"
                  />
                  <span className="text-sm text-gray-700">
                    Fehlerhafte Artikel überspringen ({invalidItemsCount} Artikel)
                  </span>
                </label>
              </div>
            )}
            
            <div className="items-preview max-h-60 overflow-y-auto mb-4">
              <table className="w-full text-sm">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-2 py-1 text-left">Zeile</th>
                    <th className="px-2 py-1 text-left">Name</th>
                    <th className="px-2 py-1 text-left">Kategorie</th>
                    <th className="px-2 py-1 text-left">Menge</th>
                    <th className="px-2 py-1 text-left">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {csvData.items.slice(0, 10).map((item: CsvItem) => (
                    <tr key={item.rowNumber} className={item.valid ? 'bg-green-50' : 'bg-red-50'}>
                      <td className="px-2 py-1">{item.rowNumber}</td>
                      <td className="px-2 py-1">{item.name}</td>
                      <td className="px-2 py-1">{item.categoryName}</td>
                      <td className="px-2 py-1">{item.quantity} {item.unit}</td>
                      <td className="px-2 py-1">
                        {item.valid ? (
                          <span className="text-green-600">✓ Gültig</span>
                        ) : (
                          <span className="text-red-600" title={item.errors.join(', ')}>
                            ❌ Fehler
                          </span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {csvData.totalItems > 10 && (
                <div className="text-center text-sm text-gray-500 mt-2">
                  ... und {csvData.totalItems - 10} weitere Artikel
                </div>
              )}
            </div>
            
            <div className="import-actions flex gap-3">
              <button
                onClick={handleImport}
                disabled={isImporting || (validItemsCount === 0)}
                className="bg-green-500 hover:bg-green-600 disabled:bg-gray-400 text-white px-6 py-2 rounded-lg flex-1"
                type="button"
              >
                {isImporting ? '⏳ Importiere...' : `📥 ${validItemsCount} Artikel importieren`}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CsvImport;