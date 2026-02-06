"""
PDF text extraction utilities using PyMuPDF (fitz).
"""

import fitz  # PyMuPDF
import re
from typing import Optional
from loguru import logger


def extract_text_from_pdf(file_bytes: bytes) -> str:
    """
    Extract raw text from a PDF file.
    
    Args:
        file_bytes: PDF file content as bytes
        
    Returns:
        Extracted text from all pages
        
    Raises:
        ValueError: If the PDF cannot be parsed
    """
    try:
        # Open PDF from bytes
        doc = fitz.open(stream=file_bytes, filetype="pdf")
        
        text_parts = []
        for page_num, page in enumerate(doc):
            page_text = page.get_text("text")
            if page_text.strip():
                text_parts.append(page_text)
            logger.debug(f"Extracted {len(page_text)} characters from page {page_num + 1}")
        
        doc.close()
        
        full_text = "\n\n".join(text_parts)
        logger.info(f"Total extracted text: {len(full_text)} characters from {len(text_parts)} pages")
        
        return full_text
        
    except Exception as e:
        logger.error(f"Failed to extract text from PDF: {e}")
        raise ValueError(f"Failed to parse PDF file: {str(e)}")


def clean_text(text: str) -> str:
    """
    Clean and normalize extracted text.
    
    - Remove excessive whitespace
    - Normalize line breaks
    - Remove special control characters
    
    Args:
        text: Raw extracted text
        
    Returns:
        Cleaned and normalized text
    """
    if not text:
        return ""
    
    # Remove control characters except newlines and tabs
    text = re.sub(r'[\x00-\x08\x0b\x0c\x0e-\x1f\x7f]', '', text)
    
    # Normalize multiple spaces to single space
    text = re.sub(r'[ \t]+', ' ', text)
    
    # Normalize multiple newlines to double newline (paragraph break)
    text = re.sub(r'\n{3,}', '\n\n', text)
    
    # Remove leading/trailing whitespace from each line
    lines = [line.strip() for line in text.split('\n')]
    text = '\n'.join(lines)
    
    # Remove leading/trailing whitespace from entire text
    text = text.strip()
    
    return text


def is_valid_pdf(file_bytes: bytes) -> bool:
    """
    Check if the provided bytes represent a valid PDF file.
    
    Args:
        file_bytes: File content as bytes
        
    Returns:
        True if valid PDF, False otherwise
    """
    if len(file_bytes) < 5:
        return False
    
    # Check PDF magic header
    return file_bytes[:5] == b'%PDF-'
