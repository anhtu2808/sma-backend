import logging
from typing import Any, Optional
from fastapi import HTTPException, status
from app.db.qdrant_client import get_qdrant_client
from app.core.config import settings
from qdrant_client.models import VectorParams, Distance, Filter, FieldCondition, MatchValue

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

    def search_points(
        self,
        collection_name: str,
        query_vector: list[float],
        limit: int = 10,
        query_filter: Optional[Filter] = None,
    ) -> list:
        """
        Search for similar vectors in a Qdrant collection.
        Returns a list of ScoredPoint objects with id, score, and payload.
        """
        try:
            client = self.get_client()
            return client.search(
                collection_name=collection_name,
                query_vector=query_vector,
                limit=limit,
                query_filter=query_filter,
                with_payload=True,
                with_vectors=False,
            )
        except Exception as e:
            logger.error(f"Error searching points in {collection_name}: {e}")
            raise

    def scroll_points(
        self,
        collection_name: str,
        scroll_filter: Filter,
        limit: int = 100,
        with_vectors: bool = True,
    ) -> list:
        """
        Retrieve points from a Qdrant collection by filter.
        Returns all matching points with their vectors.
        """
        try:
            client = self.get_client()
            points, _ = client.scroll(
                collection_name=collection_name,
                scroll_filter=scroll_filter,
                limit=limit,
                with_payload=True,
                with_vectors=with_vectors,
            )
            return points
        except Exception as e:
            logger.error(f"Error scrolling points in {collection_name}: {e}")
            raise

    def delete_points_by_filter(self, collection_name: str, delete_filter: Filter):
        """
        Delete points from a Qdrant collection matching a filter.
        """
        try:
            client = self.get_client()
            return client.delete(
                collection_name=collection_name,
                points_selector=delete_filter
            )
        except Exception as e:
            logger.error(f"Error deleting points from {collection_name}: {e}")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"Error deleting points: {str(e)}"
            )

vector_service = VectorService()