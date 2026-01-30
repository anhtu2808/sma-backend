from pydantic_settings import BaseSettings
from typing import List


class Settings(BaseSettings):
    # App
    APP_NAME: str = "SmartRecruit AI Service"
    APP_ENV: str = "dev"
    APP_PORT: int = 8000
    DEBUG: bool = True

    # CORS
    ALLOWED_ORIGINS: List[str] = [
        "http://localhost:3000",  # React
        "http://localhost:8080",  # Java
    ]

    # Java Service
    JAVA_SERVICE_URL: str = "http://localhost:8080"

    # LLM
    OPENAI_API_KEY: str = ""
    OPENAI_MODEL: str = "gpt-4-turbo-preview"

    # Qdrant
    QDRANT_HOST: str = "localhost"
    QDRANT_PORT: int = 6333
    QDRANT_COLLECTION_NAME: str = "cv_embeddings"


    class Config:
        env_file = ".env"
        case_sensitive = True


settings = Settings()