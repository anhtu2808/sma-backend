# 🤖 SmartRecruit AI Service

> AI-Powered CV Parsing & Matching Engine for SmartRecruit Platform

[![FastAPI](https://img.shields.io/badge/FastAPI-0.115.5-009688?logo=fastapi)](https://fastapi.tiangolo.com/)
[![Python](https://img.shields.io/badge/Python-3.10+-3776AB?logo=python&logoColor=white)](https://www.python.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Running the Service](#-running-the-service)
- [API Documentation](#-api-documentation)
- [Project Structure](#-project-structure)
- [Development](#-development)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Troubleshooting](#-troubleshooting)

---

## 🎯 Overview

**sma-ai-service** là AI microservice của SmartRecruit platform, chịu trách nhiệm:

- 📄 **CV Parsing**: Trích xuất thông tin có cấu trúc từ CV PDF (skills, experience, education)
- 🎯 **JD-CV Matching**: Scoring và ranking ứng viên dựa trên độ phù hợp với Job Description
- 🧠 **AI-Powered Analysis**: Phân tích gap skills và đưa ra recommendations
- 🔍 **Semantic Search**: Vector-based candidate retrieval sử dụng Qdrant

**Communication với Java Core Service:**
```
React Frontend (:3000)
       ↓
Java Core Service (:8080)  ←→  Python AI Service (:8000)
       ↓
PostgreSQL Database
```

---

## ✨ Features

### 🚀 Phase 1 - Core Features (Current)
- ✅ CV Text Extraction từ PDF (PyMuPDF)
- ✅ RESTful API với FastAPI
- ✅ JWT Token Validation
- ✅ Health Check & Monitoring endpoints
- ✅ CORS configuration
- ✅ Async/await support

### 🎯 Phase 2 - AI Integration (In Progress)
- 🔄 NLP-based CV parsing (Named Entity Recognition)
- 🔄 LLM integration (OpenAI GPT-4) cho structured extraction
- 🔄 Weighted CV-JD matching algorithm
- 🔄 Skill gap analysis

### 🌟 Phase 3 - Advanced Features (Planned)
- 📊 Vector embeddings với Sentence Transformers
- 🗄️ Qdrant vector database integration
- 📈 Candidate ranking visualization data
- 🔮 AI-powered CV optimization suggestions

---

## 🏗️ Architecture

```
sma-ai-service/
│
├── app/
│   ├── __init__.py
│   ├── main.py                 # FastAPI application entry point
│   │
│   ├── api/                    # API endpoints
│   │   ├── __init__.py
│   │   └── v1/
│   │       ├── __init__.py
│   │       ├── router.py       # API router configuration
│   │       └── endpoints/
│   │           ├── __init__.py
│   │           └── resume.py   # Resume/CV parsing endpoints
│   │
│   ├── core/                   # Core configurations
│   │   ├── __init__.py
│   │   └── config.py           # Settings & environment variables
│   │
│   ├── models/                 # Pydantic models
│   │   ├── __init__.py
│   │   └── resume.py           # Resume/CV-related schemas
│   │
│   ├── service/                # Business logic
│   │   ├── __init__.py
│   │   ├── gpt_client.py       # OpenAI GPT API integration
│   │   └── resume_parser.py    # Resume parsing service
│   │
│   └── utils/                  # Utilities
│       ├── __init__.py
│       └── pdf_extractor.py    # PDF text extraction
│
├── .venv/                      # Virtual environment (gitignored)
├── .idea/                      # IDE configuration (gitignored)
│
├── requirements.txt            # Python dependencies
├── .env.example               # Environment variables template
├── .env                       # Local environment config (gitignored)
├── .gitignore
└── README.md                  # This file
```

---

## 🛠️ Tech Stack

| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| **Framework** | FastAPI | 0.115.5 | Web framework |
| **Server** | Uvicorn | 0.32.1 | ASGI server |
| **Validation** | Pydantic | 2.10.3 | Data validation |
| **PDF Processing** | PyMuPDF | 1.24.14 | PDF parsing |
| **LLM** | OpenAI API | 1.58.1 | GPT-4 integration |
| **Vector DB** | Qdrant Client | 1.12.1 | Semantic search |
| **NLP** | spaCy | 3.8.3 | NER & text processing |
| **ML** | Transformers | 4.47.1 | Embeddings |
| **HTTP Client** | httpx | 0.28.1 | Async HTTP requests |
| **Logging** | Loguru | 0.7.3 | Structured logging |
| **Testing** | pytest | 8.3.4 | Unit & integration tests |

---

## 📋 Prerequisites

### Required
- **Python**: 3.11
- **pip**: Latest version
- **Virtual Environment**: venv

### AI Features
- **OpenAI API Key**: Để sử dụng GPT-4
- **Qdrant**: Vector database (Docker hoặc Cloud)

---

## 🚀 Installation

### 1️⃣ Clone Repository

```bash
# Nếu chưa clone monorepo
git clone <repository-url>
cd sma-backend/sma-ai-service
```

### 2️⃣ Create Virtual Environment

```bash
# Tạo venv
python -m venv venv

# Activate
# Windows:
venv\Scripts\activate

# macOS/Linux:
source venv/bin/activate
```

### 3️⃣ Upgrade pip

```bash
pip install --upgrade pip
```

### 4️⃣ Install Dependencies

**Option A: Full Installation (bao gồm AI/ML)**
```bash
pip install -r requirements.txt

# Download spaCy model
python -m spacy download en_core_web_sm
```

**Option B: Minimal Installation (chỉ core features)**
```bash
# Install core dependencies only
pip install fastapi uvicorn pydantic pydantic-settings PyMuPDF httpx python-dotenv loguru pytest
```

**Option C: Install PyTorch (CPU version - nhẹ hơn)**
```bash
# Install PyTorch CPU first
pip install torch --index-url https://download.pytorch.org/whl/cpu

# Then install other AI packages
pip install transformers sentence-transformers spacy
```

### 5️⃣ Verify Installation

```bash
python -c "import fastapi; import pydantic; import fitz; print('✅ Installation successful!')"
```

---

## ⚙️ Configuration

### 1️⃣ Create Environment File

```bash
cp .env.example .env
```

### 2️⃣ Edit `.env` file

```env
# ===================================
# FastAPI Configuration
# ===================================
APP_ENV=development
APP_PORT=8000
DEBUG=True

# ===================================
# CORS Settings
# ===================================
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080

# ===================================
# Java Core Service
# ===================================
JAVA_SERVICE_URL=http://localhost:8080

# ===================================
# Database (Optional)
# ===================================
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/sma_db

# ===================================
# OpenAI API (Required for AI features)
# ===================================
OPENAI_API_KEY=sk-proj-xxxxxxxxxxxxxxxxxxxxx
OPENAI_MODEL=gpt-4-turbo-preview
OPENAI_MAX_TOKENS=2000
OPENAI_TEMPERATURE=0.3

# ===================================
# Qdrant Vector Database (Optional)
# ===================================
QDRANT_HOST=localhost
QDRANT_PORT=6333
QDRANT_COLLECTION_NAME=cv_embeddings
QDRANT_API_KEY=

# ===================================
# AWS S3 (Optional - nếu AI service cần đọc CV)
# ===================================
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_REGION=ap-southeast-1
S3_BUCKET_NAME=sma-cv-storage

# ===================================
# JWT (phải match với Java service)
# ===================================
JWT_SECRET=your_jwt_secret_here_must_match_java
JWT_ALGORITHM=HS256

# ===================================
# Logging
# ===================================
LOG_LEVEL=INFO
LOG_FORMAT=json
```

### 3️⃣ Get OpenAI API Key

1. Truy cập: https://platform.openai.com/api-keys
2. Tạo API key mới
3. Copy vào `.env` file

---

## 🏃 Running the Service

### Development Mode

```bash
# Method 1: Using uvicorn directly
uvicorn app.main:app --reload --port 8000

# Method 2: Using Python
python -m app.main

# Method 3: With custom host/port
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

### Production Mode

```bash
# Without auto-reload
uvicorn app.main:app --host 0.0.0.0 --port 8000 --workers 4
```

### Using Docker (Optional)

```bash
# Build image
docker build -t sma-ai-service .

# Run container
docker run -p 8000:8000 --env-file .env sma-ai-service
```

### Verify Service is Running

```bash
# Health check
curl http://localhost:8000/health

# Expected response:
# {"status": "healthy"}
```

---

## 📚 API Documentation

### Interactive Documentation

Sau khi service chạy, truy cập:

- **Swagger UI**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc
- **OpenAPI JSON**: http://localhost:8000/openapi.json

### Key Endpoints

#### 1. Health Check
```bash
GET /health
GET /

# Response:
{
  "status": "healthy"
}
```

#### 2. Parse CV from File Upload
```bash
POST /api/v1/resume/parse
Content-Type: multipart/form-data

# Form data:
file: <CV.pdf>

# Response:
{
  "success": true,
  "data": {
    "full_name": "John Doe",
    "email": "john@example.com",
    "phone": "+84123456789",
    "skills": [
      {
        "name": "Python",
        "category": "Programming Language",
        "proficiency": "Senior"
      }
    ],
    "experience": [...],
    "education": [...]
  }
}
```

> **Note**: Currently, the service focuses on resume/CV parsing. Additional endpoints for JD matching and S3 URL parsing are planned for future releases.


---

## 📁 Project Structure (Detailed)

```
sma-ai-service/
│
├── app/
│   ├── __init__.py
│   ├── main.py                          # FastAPI app initialization
│   │
│   ├── api/
│   │   ├── __init__.py
│   │   └── v1/
│   │       ├── __init__.py
│   │       ├── router.py                # Main API router
│   │       └── endpoints/
│   │           ├── __init__.py
│   │           └── resume.py            # Resume/CV parsing routes
│   │
│   ├── core/
│   │   ├── __init__.py
│   │   └── config.py                    # Settings class with environment variables
│   │
│   ├── models/
│   │   ├── __init__.py
│   │   └── resume.py                    # Resume Pydantic models
│   │
│   ├── service/
│   │   ├── __init__.py
│   │   ├── gpt_client.py                # OpenAI GPT API integration
│   │   └── resume_parser.py             # Resume parsing logic
│   │
│   └── utils/
│       ├── __init__.py
│       └── pdf_extractor.py             # PDF → text extraction
│
├── .venv/                               # Virtual environment (gitignored)
├── .idea/                               # IDE configuration (gitignored)
│
├── .env.example                         # Environment template
├── .env                                 # Your local config (gitignored)
├── .gitignore
├── requirements.txt                     # Dependencies
└── README.md                            # This file
```

### 📝 Key Files Description

#### `app/main.py`
- FastAPI application entry point
- CORS middleware configuration
- Health check endpoints
- API router inclusion

#### `app/core/config.py`
- Pydantic Settings for environment variables
- Configuration for OpenAI, Qdrant, AWS S3
- CORS origins management
- JWT settings

#### `app/service/gpt_client.py`
- OpenAI GPT API client
- Resume parsing with GPT-4o-mini
- Structured output extraction

#### `app/service/resume_parser.py`
- Resume parsing orchestration
- PDF text extraction integration
- Data validation and formatting

#### `app/utils/pdf_extractor.py`
- PDF text extraction using PyMuPDF
- Text cleaning and preprocessing

#### `app/models/resume.py`
- Pydantic models for resume data
- Request/response schemas
- Data validation rules

#### `app/api/v1/endpoints/resume.py`
- POST `/api/v1/resume/parse` - Parse resume from file upload
- Resume parsing endpoint handlers


---

## 💻 Development

### Code Style

Project sử dụng:
- **Black**: Code formatting
- **Ruff**: Linting
- **MyPy**: Type checking

```bash
# Format code
black app/ tests/

# Lint
ruff check app/ tests/

# Type check
mypy app/
```

### Pre-commit Hook (Optional)

```bash
# Install pre-commit
pip install pre-commit

# Setup hooks
pre-commit install

# Manually run
pre-commit run --all-files
```

### Adding New Dependencies

```bash
# Install package
pip install <package-name>

# Update requirements.txt
pip freeze > requirements.txt
```

---

## 🧪 Testing

### Run All Tests

```bash
# Run all tests
pytest

# With coverage
pytest --cov=app --cov-report=html

# Verbose mode
pytest -v

# Specific test file
pytest tests/test_cv_parser.py

# Specific test function
pytest tests/test_cv_parser.py::test_parse_cv_success
```

### Test Structure

```python
# tests/test_cv_parser.py
import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_health_check():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "healthy"}

def test_parse_cv_upload():
    with open("tests/fixtures/sample_cv.pdf", "rb") as f:
        response = client.post(
            "/api/v1/cv/parse",
            files={"file": ("cv.pdf", f, "application/pdf")}
        )
    assert response.status_code == 200
    assert response.json()["success"] is True
```

---

## 🚢 Deployment

### Using Docker

**Dockerfile:**
```dockerfile
FROM python:3.11-slim

WORKDIR /app

# Install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application
COPY ./app ./app

# Expose port
EXPOSE 8000

# Run application
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
```

**Build & Run:**
```bash
docker build -t sma-ai-service:latest .
docker run -p 8000:8000 --env-file .env sma-ai-service:latest
```

### Using Docker Compose

```yaml
# docker-compose.yml
version: '3.8'

services:
  ai-service:
    build: .
    ports:
      - "8000:8000"
    env_file:
      - .env
    volumes:
      - ./app:/app/app
    depends_on:
      - qdrant

  qdrant:
    image: qdrant/qdrant:latest
    ports:
      - "6333:6333"
    volumes:
      - qdrant_storage:/qdrant/storage
```

---

## 🐛 Troubleshooting

### Common Issues

#### 1. Port Already in Use
```bash
# Error: Address already in use
# Solution: Kill process or use different port
lsof -ti:8000 | xargs kill -9

# Or change port
uvicorn app.main:app --port 8001
```

#### 2. PyMuPDF Installation Error
```bash
# Windows: Install Visual C++ Build Tools
# macOS: Install Xcode Command Line Tools
xcode-select --install

# Linux: Install dependencies
sudo apt-get install libmupdf-dev
```

#### 3. Import Error
```bash
# Make sure you're in venv
source venv/bin/activate  # macOS/Linux
venv\Scripts\activate     # Windows

# Reinstall package
pip install -e .
```

#### 4. OpenAI API Error
```bash
# Check API key
echo $OPENAI_API_KEY

# Test API
curl https://api.openai.com/v1/models \
  -H "Authorization: Bearer $OPENAI_API_KEY"
```

### Debug Mode

```python
# app/main.py
import logging
logging.basicConfig(level=logging.DEBUG)

# Or use Loguru
from loguru import logger
logger.debug("Debug message")
```

---

## 📞 Support

### Contact
- **Project Lead**: Đặng Mai Anh Tú
- **Supervisor**: Lê Thị Quỳnh Chi (chiltq6@fe.edu.vn)

### Resources
- [FastAPI Documentation](https://fastapi.tiangolo.com/)
- [Pydantic Docs](https://docs.pydantic.dev/)
- [OpenAI API Reference](https://platform.openai.com/docs/api-reference)
- [PyMuPDF Documentation](https://pymupdf.readthedocs.io/)

---

## 📄 License

MIT License - SmartRecruit Team © 2026

---

## 🎯 Roadmap

- [x] Basic FastAPI setup
- [x] PDF text extraction
- [x] Health check endpoints
- [ ] LLM-powered CV parsing
- [ ] CV-JD matching algorithm
- [ ] Vector database integration
- [ ] Skill gap analysis
- [ ] AI optimization suggestions
- [ ] Performance optimization
- [ ] Production deployment

---

**Made with ❤️ by SmartRecruit Team**

Last Updated: January 30, 2026