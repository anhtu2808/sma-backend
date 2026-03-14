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

from qdrant_client.models import Filter, FieldCondition, MatchValue
from app.core.config import settings

@router.get("/resumes/{resume_id}")
def get_resume_chunks(resume_id: int):
    """
    Get all semantic chunks for a given resume_id
    """
    q_filter = Filter(must=[FieldCondition(key="resume_id", match=MatchValue(value=resume_id))])
    
    try:
        points = vector_service.scroll_points(
            collection_name=settings.QDRANT_COLLECTION_NAME,
            scroll_filter=q_filter,
            limit=1000,
            with_vectors=False
        )
        
        # Sort points by source_id or chunk_index if present
        payloads = [p.payload for p in points if p.payload]
        payloads.sort(key=lambda x: (x.get("chunk_type", ""), x.get("chunk_index", 0), x.get("source_id", 0)))
        
        return {
            "resume_id": resume_id,
            "chunks_count": len(payloads),
            "data": payloads
        }
    except Exception as e:
        from fastapi import HTTPException
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/jobs/{job_id}")
def get_job_chunks(job_id: int):
    """
    Get all semantic chunks for a given job_id
    """
    q_filter = Filter(must=[FieldCondition(key="job_id", match=MatchValue(value=job_id))])
    
    try:
        points = vector_service.scroll_points(
            collection_name=settings.QDRANT_JOB_COLLECTION_NAME,
            scroll_filter=q_filter,
            limit=1000,
            with_vectors=False
        )
        
        # Sort points by source_id or chunk_index if present
        payloads = [p.payload for p in points if p.payload]
        payloads.sort(key=lambda x: (x.get("chunk_type", ""), x.get("chunk_index", 0), x.get("source_id", 0)))
        
        return {
            "job_id": job_id,
            "chunks_count": len(payloads),
            "data": payloads
        }
    except Exception as e:
        from fastapi import HTTPException
        raise HTTPException(status_code=500, detail=str(e))