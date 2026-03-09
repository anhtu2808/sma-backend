import logging
from qdrant_client import QdrantClient
from app.core.config import settings

logger = logging.getLogger(__name__)

# Initialize client as None
_qdrant_client = None

def get_qdrant_client() -> QdrantClient:
    """
    Get or initialize the Qdrant client based on settings.
    """
    global _qdrant_client
    if _qdrant_client is None:
        try:
            logger.info(f"Connecting to Qdrant at {settings.QDRANT_HOST}:{settings.QDRANT_PORT}")
            _qdrant_client = QdrantClient(
                host=settings.QDRANT_HOST,
                port=settings.QDRANT_PORT
            )
            # Test connection
            _qdrant_client.get_collections()
            logger.info("Successfully connected to Qdrant Vector DB.")
        except Exception as e:
            logger.error(f"Failed to connect to Qdrant: {e}")
            raise
    return _qdrant_client