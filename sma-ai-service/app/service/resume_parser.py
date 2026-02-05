"""
Resume parsing service - orchestrates PDF extraction and GPT parsing.
"""

from loguru import logger
from pydantic import ValidationError

from app.models.resume import ParsedCV
from app.utils.pdf_extractor import extract_text_from_pdf, clean_text, is_valid_pdf
from app.service.gpt_client import parse_cv_with_gpt
from app.core.config import settings


async def parse_resume(file_bytes: bytes) -> ParsedCV:
    """
    Parse a resume PDF file and return structured data.
    
    This is the main orchestration function that:
    1. Validates the PDF file
    2. Extracts text from PDF
    3. Cleans the extracted text
    4. Calls GPT for semantic parsing
    5. Validates the response against schema
    
    Args:
        file_bytes: PDF file content as bytes
        
    Returns:
        ParsedCV model with structured resume data
        
    Raises:
        ValueError: If PDF is invalid or parsing fails
        TimeoutError: If GPT request times out
    """
    logger.info("Starting resume parsing")
    
    # Step 1: Validate PDF
    if not is_valid_pdf(file_bytes):
        logger.error("Invalid PDF file provided")
        raise ValueError("Invalid PDF file: file does not appear to be a valid PDF")
    
    # Step 2: Extract text from PDF
    logger.info("Extracting text from PDF")
    raw_text = extract_text_from_pdf(file_bytes)
    
    if not raw_text or len(raw_text.strip()) < 50:
        logger.error("PDF contains too little text")
        raise ValueError("PDF file contains insufficient text content")
    
    # Step 3: Clean the extracted text
    logger.info("Cleaning extracted text")
    cleaned_text = clean_text(raw_text)
    logger.debug(f"Cleaned text length: {len(cleaned_text)} characters")
    
    # Step 4: Parse with GPT
    logger.info("Parsing resume with GPT")
    timeout = getattr(settings, 'OPENAI_REQUEST_TIMEOUT', 60)
    parsed_data = parse_cv_with_gpt(cleaned_text, timeout=timeout)
    
    # Step 5: Validate against Pydantic model
    logger.info("Validating parsed data against schema")
    try:
        parsed_cv = ParsedCV(**parsed_data)
    except ValidationError as e:
        logger.error(f"Schema validation failed: {e}")
        raise ValueError(f"Parsed data does not match expected schema: {str(e)}")
    
    logger.info(f"Resume parsed successfully: {parsed_cv.resume.full_name}")
    return parsed_cv
