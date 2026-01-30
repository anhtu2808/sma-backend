# 🚀 SmartRecruit Backend (SMA Backend)

> Hệ thống backend cho nền tảng tuyển dụng thông minh SmartRecruit

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.115.5-009688?logo=fastapi)](https://fastapi.tiangolo.com/)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Python](https://img.shields.io/badge/Python-3.11+-3776AB?logo=python&logoColor=white)](https://www.python.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📋 Mục Lục

- [Tổng Quan](#-tổng-quan)
- [Kiến Trúc Hệ Thống](#-kiến-trúc-hệ-thống)
- [Các Services](#-các-services)
- [Tech Stack](#-tech-stack)
- [Yêu Cầu Hệ Thống](#-yêu-cầu-hệ-thống)
- [Cài Đặt và Chạy](#-cài-đặt-và-chạy)
- [Cấu Trúc Thư Mục](#-cấu-trúc-thư-mục)
- [API Endpoints](#-api-endpoints)
- [Cấu Hình Environment](#-cấu-hình-environment)
- [Phát Triển](#-phát-triển)
- [Liên Hệ](#-liên-hệ)

---

## 🎯 Tổng Quan

**SMA Backend** là hệ thống backend của nền tảng tuyển dụng thông minh SmartRecruit, bao gồm 2 microservices chính:

1. **sma-core-service** - Xử lý nghiệp vụ chính (Java/Spring Boot)
2. **sma-ai-service** - Xử lý AI/ML cho CV parsing và matching (Python/FastAPI)

### Tính Năng Chính

- 🔐 **Authentication & Authorization**: JWT-based authentication
- 📄 **CV Management**: Upload, lưu trữ và quản lý CV
- 🤖 **AI CV Parsing**: Trích xuất thông tin từ CV PDF sử dụng LLM
- 🎯 **JD-CV Matching**: Scoring và ranking ứng viên
- ☁️ **Cloud Storage**: Tích hợp AWS S3
- 🔍 **Semantic Search**: Vector-based search với Qdrant

---

## 🏗️ Kiến Trúc Hệ Thống

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend Apps                             │
│  (React - sma-employer, sma-candidate, sma-web)                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    sma-core-service (:8080)                      │
│                    Java 17 / Spring Boot 3.5.10                  │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐             │
│  │    Auth      │ │   User Mgmt  │ │   Job Mgmt   │             │
│  └──────────────┘ └──────────────┘ └──────────────┘             │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐             │
│  │ CV Upload    │ │  S3 Storage  │ │   Payment    │             │
│  └──────────────┘ └──────────────┘ └──────────────┘             │
└─────────────────────────────────────────────────────────────────┘
        │                                           │
        │                                           ▼
        │                           ┌─────────────────────────────┐
        │                           │  sma-ai-service (:8000)     │
        │                           │  Python 3.11 / FastAPI      │
        │                           │  ┌──────────────────────┐   │
        │                           │  │ CV Parsing (PyMuPDF) │   │
        │                           │  └──────────────────────┘   │
        │                           │  ┌──────────────────────┐   │
        │                           │  │ LLM (OpenAI GPT-4)   │   │
        │                           │  └──────────────────────┘   │
        │                           │  ┌──────────────────────┐   │
        │                           │  │ Vector DB (Qdrant)   │   │
        │                           │  └──────────────────────┘   │
        │                           └─────────────────────────────┘
        ▼
┌─────────────────────────────────────────────────────────────────┐
│                        PostgreSQL Database                       │
│                          (AWS RDS / Local)                       │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📦 Các Services

### 1. sma-core-service (Java)

> Service chính xử lý toàn bộ nghiệp vụ của hệ thống

| Thông tin | Chi tiết |
|-----------|----------|
| **Framework** | Spring Boot 3.5.10 |
| **Language** | Java 17 |
| **Port** | 8080 |
| **Database** | PostgreSQL |
| **Cloud Storage** | AWS S3 |

**Dependencies chính:**
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter Validation
- AWS SDK S3
- JWT (jjwt)
- MapStruct
- Lombok

### 2. sma-ai-service (Python)

> AI Microservice cho CV parsing và matching

| Thông tin | Chi tiết |
|-----------|----------|
| **Framework** | FastAPI 0.115.5 |
| **Language** | Python 3.11 |
| **Port** | 8000 |
| **AI/ML** | OpenAI GPT-4, Qdrant |

**Dependencies chính:**
- FastAPI + Uvicorn
- PyMuPDF (PDF parsing)
- OpenAI (LLM integration)
- Qdrant Client (Vector DB)
- Pydantic (Data validation)
- python-jose (JWT)

---

## 🛠️ Tech Stack

### Backend Core

| Category | Technology | Version |
|----------|------------|---------|
| **Java Framework** | Spring Boot | 3.5.10 |
| **Python Framework** | FastAPI | 0.115.5 |
| **Database** | PostgreSQL | Latest |
| **Cloud Storage** | AWS S3 | SDK 2.20.56 |
| **Authentication** | JWT | 0.12.5 |

### AI/ML Stack

| Category | Technology | Purpose |
|----------|------------|---------|
| **LLM** | OpenAI GPT-4 | CV structured extraction |
| **PDF Processing** | PyMuPDF | PDF text extraction |
| **Vector DB** | Qdrant | Semantic search |
| **NLP** | spaCy | Named Entity Recognition |

---

## 📋 Yêu Cầu Hệ Thống

### Bắt buộc
- **Java**: JDK 17+
- **Python**: 3.11+
- **Maven**: 3.6+
- **PostgreSQL**: 14+

### Tùy chọn (cho AI features)
- **OpenAI API Key**
- **Qdrant**: Vector database
- **AWS Account**: Cho S3 storage

---

## 🚀 Cài Đặt và Chạy

### 1. Clone Repository

```bash
git clone <repository-url>
cd sma-backend
```

### 2. Setup sma-core-service (Java)

```bash
cd sma-core-service

# Tạo file .env (copy từ template hoặc tạo mới)
# Cấu hình các biến môi trường cần thiết

# Build project
./mvnw clean install

# Chạy service
./mvnw spring-boot:run

# Hoặc chạy jar file
java -jar target/sma-core-service-0.0.1-SNAPSHOT.jar
```

### 3. Setup sma-ai-service (Python)

```bash
cd sma-ai-service

# Tạo virtual environment
python -m venv venv

# Activate venv
source venv/bin/activate  # macOS/Linux
# venv\Scripts\activate   # Windows

# Cài đặt dependencies
pip install -r requirements.txt

# Copy và cấu hình .env
cp .env.example .env
# Edit .env với API keys

# Chạy service
uvicorn app.main:app --reload --port 8000
```

### 4. Verify Services

```bash
# Check core service
curl http://localhost:8080/actuator/health

# Check AI service
curl http://localhost:8000/health
```

---

## 📁 Cấu Trúc Thư Mục

```
sma-backend/
├── sma-core-service/              # Java Spring Boot service
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/sma/core/
│   │   │   │   ├── config/        # Configuration classes
│   │   │   │   ├── controller/    # REST controllers
│   │   │   │   ├── service/       # Business logic
│   │   │   │   ├── repository/    # Data access
│   │   │   │   ├── entity/        # JPA entities
│   │   │   │   ├── dto/           # Data transfer objects
│   │   │   │   ├── security/      # Security config
│   │   │   │   └── exception/     # Exception handling
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/            # Database migrations
│   │   └── test/
│   ├── pom.xml
│   └── .env
│
├── sma-ai-service/                # Python FastAPI service
│   ├── app/
│   │   ├── api/v1/endpoints/      # API endpoints
│   │   ├── core/                  # Config & security
│   │   ├── service/               # Business logic
│   │   ├── utils/                 # Utilities
│   │   └── main.py               # FastAPI entry point
│   ├── requirements.txt
│   ├── .env.example
│   └── README.md
│
├── .gitignore
└── README.md                      # This file
```

---

## 🔌 API Endpoints

### Core Service (Port 8080)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/register` | User registration |
| GET | `/api/users/me` | Get current user |
| POST | `/api/cv/upload` | Upload CV |
| GET | `/api/jobs` | List jobs |

### AI Service (Port 8000)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| POST | `/api/v1/cv/parse` | Parse CV from file |
| POST | `/api/v1/cv/parse-url` | Parse CV from S3 URL |
| POST | `/api/v1/matching/score` | Match CV against JD |

---

## ⚙️ Cấu Hình Environment

### sma-core-service (.env)

```env
# Database
DB_URL=jdbc:postgresql://localhost:5432/sma_db
DB_USERNAME=postgres
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000

# AWS S3
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_REGION=ap-southeast-1
S3_BUCKET_NAME=sma-cv-storage

# AI Service
AI_SERVICE_URL=http://localhost:8000
```

### sma-ai-service (.env)

```env
# FastAPI
APP_ENV=development
APP_PORT=8000
DEBUG=True

# CORS
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080

# OpenAI
OPENAI_API_KEY=sk-your-api-key

# Qdrant
QDRANT_HOST=localhost
QDRANT_PORT=6333

# JWT (phải match với Java service)
JWT_SECRET=your_jwt_secret_key
JWT_ALGORITHM=HS256
```

---

## 💻 Phát Triển

### Code Style

**Java (Core Service):**
- Checkstyle configuration
- Lombok for boilerplate reduction
- MapStruct for DTO mapping

**Python (AI Service):**
- Black: Code formatting
- Ruff: Linting
- MyPy: Type checking

### Testing

```bash
# Java - Run tests
cd sma-core-service
./mvnw test

# Python - Run tests
cd sma-ai-service
pytest
```

### API Documentation

- **Core Service**: http://localhost:8080/swagger-ui.html
- **AI Service**: http://localhost:8000/docs

---

## 👥 Team

| Role | Name | Contact |
|------|------|---------|
| **Project Lead** | Đặng Mai Anh Tú | - |
| **Supervisor** | Lê Thị Quỳnh Chi | chiltq6@fe.edu.vn |

---

## 📄 License

MIT License - SmartRecruit Team © 2026

---

*Last Updated: January 30, 2026*
