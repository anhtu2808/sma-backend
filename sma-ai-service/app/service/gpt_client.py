"""
OpenAI GPT client for CV parsing.
"""

import json
from datetime import datetime, timezone
from typing import Optional
from openai import OpenAI, APITimeoutError, APIError
from loguru import logger

from app.core.config import settings


# System prompt for CV parsing
CV_PARSING_SYSTEM_PROMPT = """You are a CV parsing engine.

Your task is to extract structured information from CV/resume text and return it as valid JSON.

RULES:
1. Return ONLY valid JSON - no markdown, no explanations, no additional text
2. Follow the EXACT schema provided below
3. Do NOT invent or fabricate data - only extract what is present in the CV
4. If information is missing or unclear, use null for optional fields or empty arrays []
5. Dates should be in ISO format (YYYY-MM-DD for full dates, YYYY-MM for month/year, YYYY for year only)
6. Gender should be one of: "MALE", "FEMALE", or null if not specified
7. Skill level should be one of: "BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT", or null
8. Detect the CV language and include it in metadata (e.g., "en", "vi")
9. Estimate a confidence score (0.0-1.0) based on how well-structured and complete the CV is

REQUIRED JSON SCHEMA:
{
  "resume": {
    "full_name": "string (required)",
    "job_title": "string or null",
    "date_of_birth": "YYYY-MM-DD or null",
    "avatar": null,
    "gender": "MALE|FEMALE or null",
    "address": "string or null",
    "phone": "string or null",
    "email": "string or null",
    "github_url": "string or null",
    "linkedin_url": "string or null",
    "website_url": "string or null",
    "reference_content": "string or null"
  },
  "skills": [
    {
      "name": "string",
      "level": "BEGINNER|INTERMEDIATE|ADVANCED|EXPERT or null",
      "years_of_experience": "number or null"
    }
  ],
  "educations": [
    {
      "institution": "string",
      "degree": "string or null",
      "major": "string or null",
      "start_year": "number or null",
      "end_year": "number or null",
      "gpa": "number or null"
    }
  ],
  "experiences": [
    {
      "company_name": "string",
      "position": "string",
      "start_date": "YYYY-MM or null",
      "end_date": "YYYY-MM or null",
      "is_current": "boolean",
      "details": [
        {
          "description": "string",
          "skills": ["array of strings"]
        }
      ]
    }
  ],
  "projects": [
    {
      "name": "string",
      "role": "string or null",
      "description": "string or null",
      "technologies": ["array of strings"],
      "link": "string or null"
    }
  ],
  "certifications": [
    {
      "name": "string",
      "issuer": "string or null",
      "issue_year": "number or null"
    }
  ],
  "metadata": {
    "cv_language": "string (e.g., 'en', 'vi')",
    "source_type": "pdf",
    "confidence_score": "number between 0.0 and 1.0"
  }
}"""


def build_cv_parsing_prompt(cv_text: str) -> list[dict]:
    """
    Build the messages list for CV parsing prompt.
    
    Args:
        cv_text: Cleaned CV text content
        
    Returns:
        List of message dicts for OpenAI API
    """
    return [
        {
            "role": "system",
            "content": CV_PARSING_SYSTEM_PROMPT
        },
        {
            "role": "user",
            "content": f"Parse the following CV text and return structured JSON:\n\n---\n{cv_text}\n---"
        }
    ]


# Pricing per 1M tokens (approximate)
PRICING = {
    "gpt-4o": {"input": 2.50, "output": 10.00},
    "gpt-4o-mini": {"input": 0.15, "output": 0.60},
    "gpt-4-turbo-preview": {"input": 10.00, "output": 30.00},
    "gpt-4-turbo": {"input": 10.00, "output": 30.00},
    "gpt-4.1": {"input": 10.00, "output": 30.00},  # Assign same as turbo for now if unknown
    "gpt-3.5-turbo": {"input": 0.50, "output": 1.50},
}

def calculate_cost(model: str, prompt_tokens: int, completion_tokens: int) -> float:
    """
    Calculate estimated cost for the API call.
    
    Args:
        model: Model name used
        prompt_tokens: Number of input tokens
        completion_tokens: Number of output tokens
        
    Returns:
        Estimated cost in USD
    """
    # Default to gpt-4-turbo pricing if model not found
    price = PRICING.get(model, PRICING["gpt-4-turbo"])
    
    input_cost = (prompt_tokens / 1_000_000) * price["input"]
    output_cost = (completion_tokens / 1_000_000) * price["output"]
    
    return round(input_cost + output_cost, 6)


def parse_cv_with_gpt(cv_text: str, timeout: int = 60) -> dict:
    """
    Call GPT API to parse CV text into structured data.
    
    Args:
        cv_text: Cleaned CV text content
        timeout: Request timeout in seconds
        
    Returns:
        Parsed CV data as dictionary
        
    Raises:
        TimeoutError: If GPT API times out
        ValueError: If GPT returns invalid JSON
    """
    if not settings.OPENAI_API_KEY:
        raise ValueError("OPENAI_API_KEY is not configured")
    
    client = OpenAI(
        api_key=settings.OPENAI_API_KEY,
        timeout=timeout
    )
    
    messages = build_cv_parsing_prompt(cv_text)
    model = getattr(settings, 'OPENAI_RESUME_MODEL', settings.OPENAI_MODEL)
    
    logger.info(f"Calling GPT model '{model}' for CV parsing")
    
    try:
        response = client.chat.completions.create(
            model=model,
            messages=messages,
            temperature=0.1,  # Low temperature for consistent parsing
            response_format={"type": "json_object"}
        )
        
        content = response.choices[0].message.content
        logger.debug(f"GPT response length: {len(content)} characters")
        
        # Parse JSON response
        try:
            parsed_data = json.loads(content)
        except json.JSONDecodeError as e:
            logger.error(f"GPT returned invalid JSON: {e}")
            raise ValueError(f"GPT returned invalid JSON: {str(e)}")
        
        # Extract usage statistics
        usage = response.usage
        prompt_tokens = usage.prompt_tokens
        completion_tokens = usage.completion_tokens
        total_tokens = usage.total_tokens
        
        # Calculate cost
        cost = calculate_cost(model, prompt_tokens, completion_tokens)
        
        logger.info(f"Token usage: {total_tokens} (Prompt: {prompt_tokens}, Completion: {completion_tokens})")
        logger.info(f"Estimated cost: ${cost:.6f}")
        
        # Add metadata
        if "metadata" not in parsed_data:
            parsed_data["metadata"] = {}
        
        parsed_data["metadata"]["parsed_by"] = model
        parsed_data["metadata"]["parsed_at"] = datetime.now(timezone.utc).isoformat()
        parsed_data["metadata"]["source_type"] = "pdf"
        
        # Add usage and cost to metadata
        parsed_data["metadata"]["usage"] = {
            "prompt_tokens": prompt_tokens,
            "completion_tokens": completion_tokens,
            "total_tokens": total_tokens
        }
        parsed_data["metadata"]["cost_usd"] = cost
        
        logger.info("CV parsing completed successfully")
        return parsed_data
        
    except APITimeoutError:
        logger.error("GPT API request timed out")
        raise TimeoutError("CV parsing request timed out")
    
    except APIError as e:
        logger.error(f"GPT API error: {e}")
        raise ValueError(f"GPT API error: {str(e)}")
