"""
Resume parsing service - orchestrates PDF extraction and GPT parsing.
"""

import time

from loguru import logger
from pydantic import ValidationError

from app.schemas.resume import ParsedCV
from app.utils.pdf_extractor import extract_text_from_pdf, clean_text, is_valid_pdf
from app.services.gpt_client import parse_resume_with_gpt
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
    start_total = time.perf_counter()
    
    # Step 1: Validate PDF
    if not is_valid_pdf(file_bytes):
        logger.error("Invalid PDF file provided")
        raise ValueError("Invalid PDF file: file does not appear to be a valid PDF")
    
    # Step 2: Extract text from PDF
    logger.info("Extracting text from PDF")
    start_extract = time.perf_counter()
    raw_text = extract_text_from_pdf(file_bytes)
    extract_ms = (time.perf_counter() - start_extract) * 1000
    logger.info(f"PDF extraction completed in {extract_ms:.2f}ms")
    
    if not raw_text or len(raw_text.strip()) < 50:
        logger.error("PDF contains too little text")
        raise ValueError("PDF file contains insufficient text content")
    
    # Step 3: Clean the extracted text
    logger.info("Cleaning extracted text")
    start_clean = time.perf_counter()
    cleaned_text = clean_text(raw_text)
    clean_ms = (time.perf_counter() - start_clean) * 1000
    logger.debug(f"Cleaned text length: {len(cleaned_text)} characters")
    logger.info(f"Text cleaning completed in {clean_ms:.2f}ms")
    
    # Step 4: Parse with GPT
    logger.info("Parsing resume with GPT")
    start_gpt = time.perf_counter()
    timeout = getattr(settings, "OPENAI_REQUEST_TIMEOUT", 60)
    parsed_data = parse_resume_with_gpt(cleaned_text, timeout=timeout)
    gpt_ms = (time.perf_counter() - start_gpt) * 1000
    logger.info(f"GPT parsing completed in {gpt_ms:.2f}ms")

    # Inject rawText locally to avoid high output tokens from LLM.
    if "resume" not in parsed_data or not isinstance(parsed_data["resume"], dict):
        parsed_data["resume"] = {}
    parsed_data["resume"]["rawText"] = raw_text
    
    # Step 5: Validate against Pydantic model
    logger.info("Validating parsed data against schema")
    start_validate = time.perf_counter()
    try:
        parsed_cv = ParsedCV(**parsed_data)
    except ValidationError as e:
        logger.error(f"Schema validation failed: {e}")
        raise ValueError(f"Parsed data does not match expected schema: {str(e)}")
    validate_ms = (time.perf_counter() - start_validate) * 1000
    total_ms = (time.perf_counter() - start_total) * 1000
    logger.info(f"Schema validation completed in {validate_ms:.2f}ms")
    logger.info(f"Total resume parsing time: {total_ms:.2f}ms")

    logger.info(f"Resume parsed successfully: {parsed_cv.resume.fullName}")
    return parsed_cv
