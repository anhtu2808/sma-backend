"""
API v1 router - aggregates all v1 endpoints.
"""

from fastapi import APIRouter

from app.api.v1.endpoints import resume

api_router = APIRouter(prefix="/v1")

# Include resume endpoints
api_router.include_router(resume.router)
