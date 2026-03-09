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
    
    # RabbitMQ
    RABBITMQ_ENABLED: bool = True
    RABBITMQ_HOST: str = "localhost"
    RABBITMQ_PORT: int = 5672
    RABBITMQ_USER: str = "guest"
    RABBITMQ_PASSWORD: str = "guest"
    RABBITMQ_VHOST: str = "/"
    RABBITMQ_RESUME_PARSING_REQUEST_QUEUE: str = "resume.parsing.request"
    RABBITMQ_RESUME_PARSING_RESULT_QUEUE: str = "resume.parsing.result"
    RABBITMQ_MATCHING_REQUEST_QUEUE: str = "resume.matching.request"
    RABBITMQ_MATCHING_RESULT_QUEUE: str = "resume.matching.result"
    RABBITMQ_CRITERIA_CONTEXT_REQUEST_QUEUE: str = "criteria.context.request"
    RABBITMQ_CRITERIA_CONTEXT_RESULT_QUEUE: str = "criteria.context.result"
    RABBITMQ_SUGGESTION_REQUEST_QUEUE: str = "suggest.request"
    RABBITMQ_SUGGESTION_RESULT_QUEUE: str = "suggest.result"
    RABBITMQ_RE_SUGGESTION_REQUEST_QUEUE: str = "re.suggest.request"
    RABBITMQ_RE_SUGGESTION_RESULT_QUEUE: str = "re.suggest.result"
    RABBITMQ_EMBEDDING_RESUME_REQUEST_QUEUE: str = "embedding.resume.request"
    RABBITMQ_EMBEDDING_RESUME_RESULT_QUEUE: str = "embedding.resume.result"
    RABBITMQ_EMBEDDING_JOB_REQUEST_QUEUE: str = "embedding.job.request"
    RABBITMQ_EMBEDDING_JOB_RESULT_QUEUE: str = "embedding.job.result"
    RABBITMQ_RECONNECT_DELAY_SECONDS: int = 5


    # LLM
    OPENAI_API_KEY: str = ""
    OPENAI_MODEL: str = "gpt-4-turbo-preview"
    OPENAI_RESUME_MODEL: str = "gpt-4o-mini"  # Cheaper model for resume parsing
    OPENAI_MATCHING_MODEL: str = "gpt-4o"
    OPENAI_MATCHING_OVERVIEW_MODEL: str = "gpt-4o"# Model for matching analysis
    OPENAI_CRITERIA_CONTEXT_MODEL: str = "gpt-4o"  # Model for criteria context generation
    OPENAI_REQUEST_TIMEOUT: int = 60  # Timeout in seconds

    # Qdrant
    QDRANT_HOST: str = "localhost"
    QDRANT_PORT: int = 6333
    QDRANT_COLLECTION_NAME: str = "resume"
    QDRANT_JOB_COLLECTION_NAME: str = "job"

    # AWS S3
    AWS_ACCESS_KEY_ID: str = ""
    AWS_SECRET_ACCESS_KEY: str = ""
    AWS_REGION: str = "ap-southeast-1"
    S3_BUCKET_NAME: str = "sma-resume-storage"

    # JWT (to verify tokens from Java service)
    JWT_SECRET: str = ""
    JWT_ALGORITHM: str = "HS256"


    class Config:
        env_file = ".env"
        case_sensitive = True


settings = Settings()
