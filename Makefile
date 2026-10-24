.PHONY: help up down build test lint clean

help:
	@echo "Task Manager - Makefile commands"
	@echo "  make up      - Start PostgreSQL with Docker"
	@echo "  make down    - Stop PostgreSQL"
	@echo "  make build   - Build backend and frontend"
	@echo "  make test    - Run all tests"
	@echo "  make lint    - Lint frontend"
	@echo "  make clean   - Clean build artifacts"

up:
	docker compose up -d

down:
	docker compose down

build:
	cd backend && mvn clean package -DskipTests
	cd frontend && npm run build

test:
	cd backend && mvn test
	cd frontend && npm run test

lint:
	cd frontend && npm run lint

clean:
	cd backend && mvn clean
	cd frontend && rm -rf dist node_modules/.cache
