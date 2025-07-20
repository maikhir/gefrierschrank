import React, { Component, ErrorInfo, ReactNode } from 'react';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
  errorInfo?: ErrorInfo;
}

class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): State {
    // Update state so the next render will show the fallback UI
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    // Log the error to console and potentially to a logging service
    console.error('ErrorBoundary caught an error:', error, errorInfo);
    
    this.setState({
      error,
      errorInfo,
    });

    // You can also log to an error reporting service here
    // logErrorToService(error, errorInfo);
  }

  private handleReload = () => {
    window.location.reload();
  };

  private handleGoHome = () => {
    window.location.href = '/';
  };

  render() {
    if (this.state.hasError) {
      // Use custom fallback UI if provided, otherwise use default
      if (this.props.fallback) {
        return this.props.fallback;
      }

      return (
        <div className="error-boundary">
          <div className="error-content">
            <h1>Oops! Etwas ist schiefgelaufen</h1>
            <p>
              Es ist ein unerwarteter Fehler aufgetreten. Wir entschuldigen uns für die Unannehmlichkeiten.
            </p>
            
            <div className="error-actions">
              <button onClick={this.handleReload} className="btn btn-primary">
                Seite neu laden
              </button>
              <button onClick={this.handleGoHome} className="btn btn-secondary">
                Zur Startseite
              </button>
            </div>

            {process.env.NODE_ENV === 'development' && (
              <details className="error-details">
                <summary>Entwickler-Info (nur im Development-Modus)</summary>
                <div className="error-stack">
                  <h3>Fehler:</h3>
                  <pre>{this.state.error?.toString()}</pre>
                  
                  {this.state.errorInfo && (
                    <>
                      <h3>Component Stack:</h3>
                      <pre>{this.state.errorInfo.componentStack}</pre>
                    </>
                  )}
                </div>
              </details>
            )}
          </div>

          <style jsx>{`
            .error-boundary {
              display: flex;
              align-items: center;
              justify-content: center;
              min-height: 100vh;
              padding: 20px;
              background-color: #f8f9fa;
            }

            .error-content {
              max-width: 600px;
              text-align: center;
              background: white;
              padding: 40px;
              border-radius: 8px;
              box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            }

            .error-content h1 {
              color: #dc3545;
              margin-bottom: 20px;
              font-size: 24px;
            }

            .error-content p {
              color: #6c757d;
              margin-bottom: 30px;
              line-height: 1.6;
            }

            .error-actions {
              display: flex;
              gap: 15px;
              justify-content: center;
              margin-bottom: 30px;
            }

            .btn {
              padding: 12px 24px;
              border: none;
              border-radius: 4px;
              cursor: pointer;
              font-size: 16px;
              text-decoration: none;
              display: inline-block;
              transition: background-color 0.2s;
            }

            .btn-primary {
              background-color: #007bff;
              color: white;
            }

            .btn-primary:hover {
              background-color: #0056b3;
            }

            .btn-secondary {
              background-color: #6c757d;
              color: white;
            }

            .btn-secondary:hover {
              background-color: #5a6268;
            }

            .error-details {
              text-align: left;
              margin-top: 30px;
              padding: 20px;
              background-color: #f8f9fa;
              border-radius: 4px;
              border: 1px solid #dee2e6;
            }

            .error-details summary {
              cursor: pointer;
              font-weight: bold;
              margin-bottom: 10px;
            }

            .error-stack pre {
              background-color: #f1f3f4;
              padding: 10px;
              border-radius: 4px;
              overflow-x: auto;
              font-size: 12px;
              margin: 10px 0;
              white-space: pre-wrap;
              word-wrap: break-word;
            }

            .error-stack h3 {
              margin-top: 20px;
              margin-bottom: 10px;
              font-size: 16px;
              color: #495057;
            }
          `}</style>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;