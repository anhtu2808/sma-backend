"""
Resume parsing API endpoints.
"""

from fastapi import APIRouter, UploadFile, File, HTTPException, status
from loguru import logger

from app.schemas.resume import ParsedResume
from app.services.resume_service import parse_resume

router = APIRouter(prefix="/resume", tags=["Resume"])


@router.post(
    "/parse",
    response_model=ParsedResume,
    summary="Parse a resume PDF",
    description="Upload a PDF resume file and receive structured JSON data extracted using AI.",
    responses={
        200: {"description": "Successfully parsed resume"},
        400: {"description": "Invalid PDF file"},
        422: {"description": "Schema validation failed"},
        504: {"description": "Request timeout"}
    }
)
async def parse_resume_endpoint(
    file: UploadFile = File(..., description="PDF resume file to parse")
):
    """
    Parse a resume PDF file and return structured data.
    
    - **file**: PDF file containing the resume
    
    Returns structured JSON with:
    - Resume entity fields (fullName, emailInResume, githubLink, etc.)
    - resumeSkills grouped by groupName, each skill includes optional yearsOfExperience
    - resumeEducations (supports optional orderIndex)
    - resumeExperiences with workingModel/employmentType and nested details/skills
    - resumeProjects and nested skills (supports optional orderIndex)
    - resumeCertifications
    - Parsing metadata
    """
    # Validate content type
    if file.content_type and file.content_type != "application/pdf":
        logger.warning(f"Invalid content type: {file.content_type}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invalid file type. Expected PDF, got: {file.content_type}"
        )
    
    # Validate file extension
    if file.filename and not file.filename.lower().endswith(".pdf"):
        logger.warning(f"Invalid file extension: {file.filename}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid file extension. Please upload a PDF file."
        )
    
    try:
        # Read file content
        file_bytes = await file.read()
        
        if len(file_bytes) == 0:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Uploaded file is empty"
            )
        
        logger.info(f"Received resume file: {file.filename}, size: {len(file_bytes)} bytes")
        
        # Parse resume
        parsed_resume = await parse_resume(file_bytes)
        
        return parsed_resume
        
    except ValueError as e:
        logger.error(f"Parsing error: {e}")
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=str(e)
        )
    
    except TimeoutError as e:
        logger.error(f"Timeout error: {e}")
        raise HTTPException(
            status_code=status.HTTP_504_GATEWAY_TIMEOUT,
            detail="Resume parsing request timed out. Please try again."
        )
    
    except Exception as e:
        logger.exception(f"Unexpected error during resume parsing: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="An unexpected error occurred during resume parsing"
        )
