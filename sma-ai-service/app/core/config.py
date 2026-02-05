from pydantic_settings import BaseSettings
from typing import List, Union
from pydantic import field_validator


class Settings(BaseSettings):
    # App
    APP_NAME: str = "SmartRecruit AI Service"
    APP_ENV: str = "dev"
    APP_PORT: int = 8000
    DEBUG: bool = True

    # CORS
    ALLOWED_ORIGINS: Union[List[str], str] = [
        "http://localhost:3000",  # React
        "http://localhost:8080",  # Java
    ]
    
    @field_validator('ALLOWED_ORIGINS', mode='before')
    @classmethod
    def parse_allowed_origins(cls, v):
        """Parse ALLOWED_ORIGINS from comma-separated string or list."""
        if isinstance(v, str):
            return [origin.strip() for origin in v.split(',')]
        return v

    # Java Service
    JAVA_SERVICE_URL: str = "http://localhost:8080"

    # LLM
    OPENAI_API_KEY: str = ""
    OPENAI_MODEL: str = "gpt-4-turbo-preview"
    OPENAI_RESUME_MODEL: str = "gpt-4o-mini"  # Cheaper model for resume parsing
    OPENAI_REQUEST_TIMEOUT: int = 60  # Timeout in seconds

    # Qdrant
    QDRANT_HOST: str = "localhost"
    QDRANT_PORT: int = 6333
    QDRANT_COLLECTION_NAME: str = "cv_embeddings"

    # AWS S3
    AWS_ACCESS_KEY_ID: str = ""
    AWS_SECRET_ACCESS_KEY: str = ""
    AWS_REGION: str = "ap-southeast-1"
    S3_BUCKET_NAME: str = "sma-cv-storage"

    # JWT (to verify tokens from Java service)
    JWT_SECRET: str = ""
    JWT_ALGORITHM: str = "HS256"


    class Config:
        env_file = ".env"
        case_sensitive = True


settings = Settings()