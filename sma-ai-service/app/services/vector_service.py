import logging
from typing import Any
from fastapi import HTTPException, status
from app.db.qdrant_client import get_qdrant_client
from app.core.config import settings
from qdrant_client.models import VectorParams, Distance

logger = logging.getLogger(__name__)

class VectorService:
    def __init__(self):
        self.default_collection = settings.QDRANT_COLLECTION_NAME

    def get_client(self):
        """Helper to get the initialized qdrant client."""
        return get_qdrant_client()

    def get_collections(self) -> Any:
        """Get a list of all collections in Qdrant."""
        try:
            client = self.get_client()
            return client.get_collections()
        except Exception as e:
            logger.error(f"Error fetching collections from Qdrant: {e}")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"Error connecting to Vector Database: {str(e)}"
            )

    def get_collection_info(self, collection_name: str) -> Any:
        """Get detailed information about a specific collection."""
        try:
            client = self.get_client()
            return client.get_collection(collection_name)
        except Exception as e:
            logger.error(f"Error fetching collection info for {collection_name}: {e}")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Collection '{collection_name}' not found or error: {str(e)}"
            )

    def create_collection(self, collection_name: str, vector_size: int):
        """
        Create a new collection in Qdrant.
        """
        try:
            client = self.get_client()

            client.create_collection(
                collection_name=collection_name,
                vectors_config=VectorParams(
                    size=vector_size,
                    distance=Distance.COSINE
                )
            )

            return {
                "message": f"Collection '{collection_name}' created successfully"
            }

        except Exception as e:
            logger.error(f"Error creating collection {collection_name}: {e}")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"Error creating collection: {str(e)}"
            )

    def upsert_points(self, collection_name: str, points: list):
        """
        Upsert a list of points (vectors + payloads) into a Qdrant collection.
        """
        try:
            client = self.get_client()
            return client.upsert(
                collection_name=collection_name,
                points=points
            )
        except Exception as e:
            logger.error(f"Error upserting points to {collection_name}: {e}")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"Error upserting points: {str(e)}"
            )

vector_service = VectorService()