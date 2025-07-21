#!/bin/bash

# Gefrierschrank Management App - Start Script
echo "🚀 Starting Gefrierschrank Management App..."

# Function to check if port is in use
check_port() {
    local port=$1
    if lsof -i :$port > /dev/null 2>&1; then
        echo "⚠️  Port $port is already in use"
        return 1
    fi
    return 0
}

# Function to start backend
start_backend() {
    echo "📦 Starting Backend (Spring Boot)..."
    
    if ! check_port 8080; then
        echo "❌ Backend port 8080 is already in use. Please stop existing process first."
        return 1
    fi
    
    cd backend
    echo "🔧 Building backend..."
    mvn clean package -DskipTests > ../backend.log 2>&1 &
    BUILD_PID=$!
    
    # Wait for build to complete
    wait $BUILD_PID
    if [ $? -ne 0 ]; then
        echo "❌ Backend build failed. Check backend.log for details."
        return 1
    fi
    
    echo "🏃 Running backend server..."
    nohup mvn spring-boot:run > ../backend.log 2>&1 &
    BACKEND_PID=$!
    echo $BACKEND_PID > ../backend.pid
    
    # Wait for backend to start
    echo "⏳ Waiting for backend to start..."
    for i in {1..30}; do
        if curl -s http://localhost:8080/api/categories > /dev/null 2>&1; then
            echo "✅ Backend started successfully on http://localhost:8080"
            echo "📊 H2 Console: http://localhost:8080/h2-console"
            echo "📚 API Docs: http://localhost:8080/swagger-ui.html"
            cd ..
            return 0
        fi
        sleep 2
        echo -n "."
    done
    
    echo "❌ Backend startup timeout. Check backend.log for details."
    cd ..
    return 1
}

# Function to start frontend
start_frontend() {
    echo "🌐 Starting Frontend (React + Vite)..."
    
    if ! check_port 5173; then
        echo "⚠️  Frontend port 5173 is already in use. Trying to start anyway..."
    fi
    
    cd frontend
    
    # Check if node_modules exists
    if [ ! -d "node_modules" ]; then
        echo "📦 Installing frontend dependencies..."
        npm install
    fi
    
    echo "🏃 Running frontend development server..."
    nohup npm run dev > ../frontend.log 2>&1 &
    FRONTEND_PID=$!
    echo $FRONTEND_PID > ../frontend.pid
    
    # Wait for frontend to start
    echo "⏳ Waiting for frontend to start..."
    for i in {1..20}; do
        if curl -s http://localhost:5173 > /dev/null 2>&1; then
            echo "✅ Frontend started successfully on http://localhost:5173"
            cd ..
            return 0
        fi
        sleep 2
        echo -n "."
    done
    
    echo "⚠️  Frontend may still be starting. Check frontend.log for details."
    echo "🌐 Frontend should be available at: http://localhost:5173"
    cd ..
    return 0
}

# Main execution
echo "🔍 Checking system requirements..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 21 or higher."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven."
    exit 1
fi

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js."
    exit 1
fi

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo "❌ npm is not installed. Please install npm."
    exit 1
fi

echo "✅ System requirements met"
echo ""

# Parse command line arguments
START_BACKEND=true
START_FRONTEND=true

while [[ $# -gt 0 ]]; do
    case $1 in
        --backend-only)
            START_FRONTEND=false
            shift
            ;;
        --frontend-only)
            START_BACKEND=false
            shift
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --backend-only   Start only the backend server"
            echo "  --frontend-only  Start only the frontend server"
            echo "  --help, -h       Show this help message"
            echo ""
            echo "Default: Start both backend and frontend"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Start services
if [ "$START_BACKEND" = true ]; then
    start_backend
    if [ $? -ne 0 ]; then
        echo "❌ Failed to start backend"
        exit 1
    fi
    echo ""
fi

if [ "$START_FRONTEND" = true ]; then
    start_frontend
    echo ""
fi

echo "🎉 Application startup complete!"
echo ""
echo "📋 Service URLs:"
if [ "$START_BACKEND" = true ]; then
    echo "   🔙 Backend:     http://localhost:8080"
    echo "   🗄️  H2 Console:  http://localhost:8080/h2-console"
    echo "   📚 API Docs:    http://localhost:8080/swagger-ui.html"
fi
if [ "$START_FRONTEND" = true ]; then
    echo "   🌐 Frontend:    http://localhost:5173"
fi
echo ""
echo "📜 Logs:"
if [ "$START_BACKEND" = true ]; then
    echo "   🔙 Backend:     tail -f backend.log"
fi
if [ "$START_FRONTEND" = true ]; then
    echo "   🌐 Frontend:    tail -f frontend.log"
fi
echo ""
echo "🛑 To stop the application: ./stop.sh"
echo ""

# Create status file
echo "backend=$START_BACKEND" > .app_status
echo "frontend=$START_FRONTEND" >> .app_status
echo "started_at=$(date)" >> .app_status