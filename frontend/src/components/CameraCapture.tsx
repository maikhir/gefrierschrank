import React, { useRef, useState, useCallback } from 'react';
import { filesAPI } from '../services/api';
import { FileUploadResponse } from '../types';

interface CameraCaptureProps {
  onImageCaptured: (filePath: string) => void;
  onError: (error: string) => void;
  className?: string;
}

const CameraCapture: React.FC<CameraCaptureProps> = ({ onImageCaptured, onError, className }) => {
  const videoRef = useRef<HTMLVideoElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [isStreaming, setIsStreaming] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [stream, setStream] = useState<MediaStream | null>(null);

  const startCamera = useCallback(async () => {
    try {
      const mediaStream = await navigator.mediaDevices.getUserMedia({
        video: {
          width: { ideal: 1280 },
          height: { ideal: 720 },
          facingMode: 'environment' // Use back camera if available
        }
      });
      
      if (videoRef.current) {
        videoRef.current.srcObject = mediaStream;
        videoRef.current.play();
        setIsStreaming(true);
        setStream(mediaStream);
      }
    } catch (err) {
      console.error('Error accessing camera:', err);
      onError('Camera access denied or not available');
    }
  }, [onError]);

  const stopCamera = useCallback(() => {
    if (stream) {
      stream.getTracks().forEach(track => track.stop());
      setStream(null);
    }
    setIsStreaming(false);
  }, [stream]);

  const capturePhoto = useCallback(async () => {
    if (!videoRef.current || !canvasRef.current) return;

    const video = videoRef.current;
    const canvas = canvasRef.current;
    const context = canvas.getContext('2d');

    if (!context) return;

    // Set canvas size to match video
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;

    // Draw current video frame to canvas
    context.drawImage(video, 0, 0, canvas.width, canvas.height);

    // Convert canvas to blob
    canvas.toBlob(async (blob) => {
      if (!blob) {
        onError('Failed to capture image');
        return;
      }

      setIsUploading(true);
      try {
        // Create file from blob
        const file = new File([blob], `captured-${Date.now()}.jpg`, {
          type: 'image/jpeg',
          lastModified: Date.now()
        });

        // Upload to server
        const response: FileUploadResponse = await filesAPI.uploadImage(file);
        
        if (response.success && response.filePath) {
          onImageCaptured(response.filePath);
          stopCamera(); // Stop camera after successful capture
        } else {
          onError(response.error || 'Upload failed');
        }
      } catch (error) {
        console.error('Error uploading image:', error);
        onError('Failed to upload image');
      } finally {
        setIsUploading(false);
      }
    }, 'image/jpeg', 0.9);
  }, [onImageCaptured, onError, stopCamera]);

  // Cleanup on unmount
  React.useEffect(() => {
    return () => {
      stopCamera();
    };
  }, [stopCamera]);

  return (
    <div className={`camera-capture ${className || ''}`}>
      {!isStreaming ? (
        <div className="camera-start">
          <button
            onClick={startCamera}
            className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg flex items-center gap-2"
            type="button"
          >
            📷 Camera öffnen
          </button>
        </div>
      ) : (
        <div className="camera-active">
          <div className="video-container relative">
            <video
              ref={videoRef}
              className="w-full h-64 object-cover rounded-lg bg-gray-200"
              playsInline
              muted
            />
            <canvas
              ref={canvasRef}
              className="hidden"
            />
          </div>
          
          <div className="camera-controls flex gap-2 mt-4">
            <button
              onClick={capturePhoto}
              disabled={isUploading}
              className="bg-green-500 hover:bg-green-600 disabled:bg-gray-400 text-white px-4 py-2 rounded-lg flex items-center gap-2 flex-1"
              type="button"
            >
              {isUploading ? '⏳ Uploading...' : '📸 Foto aufnehmen'}
            </button>
            
            <button
              onClick={stopCamera}
              className="bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded-lg"
              type="button"
            >
              ❌ Abbrechen
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default CameraCapture;