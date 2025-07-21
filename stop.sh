#!/bin/bash

# Gefrierschrank Management App - Stop Script
echo "🛑 Stopping Gefrierschrank Management App..."

# Function to stop process by PID file
stop_process() {
    local service_name=$1
    local pid_file=$2
    
    if [ -f "$pid_file" ]; then
        PID=$(cat "$pid_file")
        if ps -p $PID > /dev/null 2>&1; then
            echo "🔄 Stopping $service_name (PID: $PID)..."
            kill $PID
            
            # Wait for process to stop
            for i in {1..10}; do
                if ! ps -p $PID > /dev/null 2>&1; then
                    echo "✅ $service_name stopped successfully"
                    break
                fi
                sleep 1
            done
            
            # Force kill if still running
            if ps -p $PID > /dev/null 2>&1; then
                echo "⚠️  Force killing $service_name..."
                kill -9 $PID
                sleep 1
                if ! ps -p $PID > /dev/null 2>&1; then
                    echo "✅ $service_name force stopped"
                else
                    echo "❌ Failed to stop $service_name"
                fi
            fi
        else
            echo "⚠️  $service_name process not running (stale PID file)"
        fi
        rm -f "$pid_file"
    else
        echo "ℹ️  No PID file found for $service_name"
    fi
}

# Function to stop processes by port
stop_by_port() {
    local service_name=$1
    local port=$2
    
    PID=$(lsof -ti :$port 2>/dev/null)
    if [ ! -z "$PID" ]; then
        echo "🔄 Stopping $service_name on port $port (PID: $PID)..."
        kill $PID
        sleep 2
        
        # Check if still running
        if lsof -ti :$port > /dev/null 2>&1; then
            echo "⚠️  Force killing $service_name on port $port..."
            kill -9 $(lsof -ti :$port 2>/dev/null)
            sleep 1
        fi
        
        if ! lsof -ti :$port > /dev/null 2>&1; then
            echo "✅ $service_name stopped successfully"
        else
            echo "❌ Failed to stop $service_name"
        fi
    else
        echo "ℹ️  No process running on port $port for $service_name"
    fi
}

# Parse command line arguments
STOP_BACKEND=true
STOP_FRONTEND=true
FORCE_KILL=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --backend-only)
            STOP_FRONTEND=false
            shift
            ;;
        --frontend-only)
            STOP_BACKEND=false
            shift
            ;;
        --force|-f)
            FORCE_KILL=true
            shift
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --backend-only   Stop only the backend server"
            echo "  --frontend-only  Stop only the frontend server"
            echo "  --force, -f      Force kill processes if normal stop fails"
            echo "  --help, -h       Show this help message"
            echo ""
            echo "Default: Stop both backend and frontend"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Stop services
if [ "$STOP_BACKEND" = true ]; then
    echo "🔙 Stopping Backend..."
    stop_process "Backend" "backend.pid"
    
    # Also try to stop by port as fallback
    if lsof -ti :8080 > /dev/null 2>&1; then
        stop_by_port "Backend" "8080"
    fi
    
    # Stop any Maven processes if force kill is enabled
    if [ "$FORCE_KILL" = true ]; then
        echo "💀 Force killing Maven processes..."
        pkill -f "maven" 2>/dev/null || true
        pkill -f "spring-boot:run" 2>/dev/null || true
    fi
fi

if [ "$STOP_FRONTEND" = true ]; then
    echo "🌐 Stopping Frontend..."
    stop_process "Frontend" "frontend.pid"
    
    # Also try to stop by port as fallback
    if lsof -ti :5173 > /dev/null 2>&1; then
        stop_by_port "Frontend" "5173"
    fi
    
    # Stop any Node/Vite processes if force kill is enabled
    if [ "$FORCE_KILL" = true ]; then
        echo "💀 Force killing Node.js/Vite processes..."
        pkill -f "vite" 2>/dev/null || true
        pkill -f "npm run dev" 2>/dev/null || true
    fi
fi

# Clean up log files if both services are stopped
if [ "$STOP_BACKEND" = true ] && [ "$STOP_FRONTEND" = true ]; then
    echo ""
    echo "🧹 Cleaning up..."
    
    # Clean up status file
    rm -f .app_status
    
    # Optionally clean up log files (commented out to preserve logs)
    # read -p "Delete log files? (y/N): " -n 1 -r
    # echo
    # if [[ $REPLY =~ ^[Yy]$ ]]; then
    #     rm -f backend.log frontend.log
    #     echo "🗑️  Log files deleted"
    # fi
fi

echo ""
echo "✅ Shutdown complete!"

# Show remaining processes on the ports
if lsof -ti :8080 > /dev/null 2>&1 || lsof -ti :5173 > /dev/null 2>&1; then
    echo ""
    echo "⚠️  Warning: Some processes may still be running:"
    if lsof -ti :8080 > /dev/null 2>&1; then
        echo "   Port 8080: $(lsof -ti :8080)"
    fi
    if lsof -ti :5173 > /dev/null 2>&1; then
        echo "   Port 5173: $(lsof -ti :5173)"
    fi
    echo "   Use './stop.sh --force' to force kill all processes"
fi