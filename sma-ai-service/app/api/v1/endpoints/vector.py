from fastapi import APIRouter
from app.services.vector_service import vector_service

router = APIRouter(prefix="/vector", tags=["Vector Database"])

@router.get("/collections")
def get_all_collections():
    """
    Get a list of all collections in the Qdrant Vector Database.
    """
    return vector_service.get_collections()

@router.get("/collections/{collection_name}")
def get_collection_info(collection_name: str):
    """
    Get detailed information about a specific Qdrant collection.
    """
    return vector_service.get_collection_info(collection_name)

@router.post("/collections/{collection_name}")
def create_collection(collection_name: str):
    """
    Create a new vector collection
    """
    return vector_service.create_collection(
        collection_name=collection_name,
        vector_size=1536
    )