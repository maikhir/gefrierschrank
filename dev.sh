#!/bin/bash

# Gefrierschrank Management App - Development Helper Script
echo "🛠️  Gefrierschrank Management - Development Helper"

# Function to show status
show_status() {
    echo "📊 Application Status:"
    echo ""
    
    # Check backend
    if lsof -ti :8080 > /dev/null 2>&1; then
        BACKEND_PID=$(lsof -ti :8080)
        echo "✅ Backend:  Running (PID: $BACKEND_PID) - http://localhost:8080"
        echo "   🗄️  H2 Console: http://localhost:8080/h2-console"
        echo "   📚 API Docs:   http://localhost:8080/swagger-ui.html"
    else
        echo "❌ Backend:  Not running"
    fi
    
    # Check frontend
    if lsof -ti :5173 > /dev/null 2>&1; then
        FRONTEND_PID=$(lsof -ti :5173)
        echo "✅ Frontend: Running (PID: $FRONTEND_PID) - http://localhost:5173"
    else
        echo "❌ Frontend: Not running"
    fi
    
    echo ""
    
    # Show log file sizes
    if [ -f "backend.log" ]; then
        BACKEND_LOG_SIZE=$(du -h backend.log | cut -f1)
        echo "📜 Backend Log:  backend.log ($BACKEND_LOG_SIZE)"
    fi
    
    if [ -f "frontend.log" ]; then
        FRONTEND_LOG_SIZE=$(du -h frontend.log | cut -f1)
        echo "📜 Frontend Log: frontend.log ($FRONTEND_LOG_SIZE)"
    fi
}

# Function to show logs
show_logs() {
    local service=$1
    
    case $service in
        backend|be)
            if [ -f "backend.log" ]; then
                echo "📜 Backend Logs (last 50 lines):"
                echo "================================"
                tail -50 backend.log
            else
                echo "❌ Backend log file not found"
            fi
            ;;
        frontend|fe)
            if [ -f "frontend.log" ]; then
                echo "📜 Frontend Logs (last 50 lines):"
                echo "================================="
                tail -50 frontend.log
            else
                echo "❌ Frontend log file not found"
            fi
            ;;
        *)
            echo "Usage: $0 logs [backend|frontend]"
            ;;
    esac
}

# Function to run tests
run_tests() {
    local service=$1
    
    case $service in
        backend|be)
            echo "🧪 Running Backend Tests..."
            cd backend
            mvn test
            cd ..
            ;;
        frontend|fe)
            echo "🧪 Running Frontend Tests..."
            cd frontend
            npm test
            cd ..
            ;;
        all)
            echo "🧪 Running All Tests..."
            echo ""
            echo "🔙 Backend Tests:"
            cd backend
            mvn test
            BACKEND_RESULT=$?
            cd ..
            
            echo ""
            echo "🌐 Frontend Tests:"
            cd frontend
            npm test
            FRONTEND_RESULT=$?
            cd ..
            
            echo ""
            if [ $BACKEND_RESULT -eq 0 ] && [ $FRONTEND_RESULT -eq 0 ]; then
                echo "✅ All tests passed!"
            else
                echo "❌ Some tests failed"
                exit 1
            fi
            ;;
        *)
            echo "Usage: $0 test [backend|frontend|all]"
            ;;
    esac
}

# Function to build services
build_service() {
    local service=$1
    
    case $service in
        backend|be)
            echo "🔨 Building Backend..."
            cd backend
            mvn clean package -DskipTests
            cd ..
            ;;
        frontend|fe)
            echo "🔨 Building Frontend..."
            cd frontend
            npm run build
            cd ..
            ;;
        all)
            echo "🔨 Building All Services..."
            echo ""
            echo "🔙 Building Backend:"
            cd backend
            mvn clean package -DskipTests
            BACKEND_RESULT=$?
            cd ..
            
            echo ""
            echo "🌐 Building Frontend:"
            cd frontend
            npm run build
            FRONTEND_RESULT=$?
            cd ..
            
            echo ""
            if [ $BACKEND_RESULT -eq 0 ] && [ $FRONTEND_RESULT -eq 0 ]; then
                echo "✅ Build successful!"
            else
                echo "❌ Build failed"
                exit 1
            fi
            ;;
        *)
            echo "Usage: $0 build [backend|frontend|all]"
            ;;
    esac
}

# Function to clean up
clean_all() {
    echo "🧹 Cleaning up development environment..."
    
    # Stop all services first
    ./stop.sh
    
    # Clean backend
    echo "🔙 Cleaning Backend..."
    cd backend
    mvn clean > /dev/null 2>&1
    cd ..
    
    # Clean frontend
    echo "🌐 Cleaning Frontend..."
    cd frontend
    rm -rf dist/
    rm -rf .vite/
    cd ..
    
    # Clean logs
    rm -f backend.log frontend.log
    
    # Clean PID files
    rm -f backend.pid frontend.pid .app_status
    
    echo "✅ Cleanup complete!"
}

# Function to reset data
reset_data() {
    echo "🗑️  Resetting application data..."
    
    read -p "⚠️  This will delete all data in the H2 database. Continue? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Data reset cancelled"
        return 1
    fi
    
    # Stop backend if running
    if lsof -ti :8080 > /dev/null 2>&1; then
        echo "🛑 Stopping backend first..."
        ./stop.sh --backend-only
        sleep 2
    fi
    
    # Remove H2 database files
    rm -f backend/data/gefrierschrank_db.mv.db
    rm -f backend/data/gefrierschrank_db.trace.db
    rm -rf uploads/
    
    echo "✅ Data reset complete!"
    echo "ℹ️  Database will be recreated on next backend start"
}

# Main script
case ${1:-help} in
    start)
        ./start.sh "${@:2}"
        ;;
    stop)
        ./stop.sh "${@:2}"
        ;;
    status|st)
        show_status
        ;;
    logs|log)
        show_logs "${2:-help}"
        ;;
    test|tests)
        run_tests "${2:-all}"
        ;;
    build)
        build_service "${2:-all}"
        ;;
    clean)
        clean_all
        ;;
    reset-data)
        reset_data
        ;;
    restart)
        ./stop.sh "${@:2}"
        sleep 2
        ./start.sh "${@:2}"
        ;;
    help|--help|-h)
        echo "Usage: $0 COMMAND [OPTIONS]"
        echo ""
        echo "Commands:"
        echo "  start           Start the application (both services)"
        echo "  stop            Stop the application"
        echo "  restart         Restart the application"
        echo "  status          Show application status"
        echo "  logs [service]  Show logs (backend|frontend)"
        echo "  test [service]  Run tests (backend|frontend|all)"
        echo "  build [service] Build services (backend|frontend|all)"
        echo "  clean           Clean build artifacts and logs"
        echo "  reset-data      Reset H2 database (WARNING: deletes all data)"
        echo "  help            Show this help message"
        echo ""
        echo "Examples:"
        echo "  $0 start                    # Start both services"
        echo "  $0 start --backend-only     # Start only backend"
        echo "  $0 logs backend             # Show backend logs"
        echo "  $0 test all                 # Run all tests"
        echo "  $0 build backend            # Build only backend"
        echo ""
        ;;
    *)
        echo "❌ Unknown command: $1"
        echo "Use '$0 help' for usage information"
        exit 1
        ;;
esac