import uuid
import asyncio
from loguru import logger
from qdrant_client.models import PointStruct, Filter, FieldCondition, MatchValue

from app.core.config import settings
from app.schemas.embedding import (
    EmbeddingResumeRequestMessage,
    EmbeddingResumeResultMessage,
    EmbedStatus
)
from app.clients.openai_client import create_embeddings, create_json_chat_completion
from app.services.vector_service import vector_service
import json


def _generate_chunks_with_ai(resume_data: EmbeddingResumeRequestMessage) -> dict:
    prompt = """You are an expert HR data assistant. Your task is to transform the provided candidate's resume data into clean, highly structured text chunks optimized for vector embedding and semantic search.

Follow these rules:

1. Keep each chunk concise (100–300 words max).
2. Preserve technical context (technologies used, role responsibilities).
3. Do NOT invent any information.
4. Omit fields that are missing.
5. Return ONLY a valid JSON object.

Based on the provided JSON data, generate a JSON object containing the exact fields below. Follow the format of the examples strictly, omitting any data points that are not present in the input.

1. "overview" (string or null): A single summary paragraph combining the candidate's job title and key skills.
   Example: "Senior Backend Engineer building scalable microservices using Java, Spring Boot, Kafka and PostgreSQL."

2. "skills" (list of strings): A list where each element is a formatted text block for a specific skill context. Group skills by their `group` field if it exists, otherwise use their `category` field. If both are missing, use "other". Each string in the list should begin with the context name followed by the skills in that context.
   Example:
   [
  "Backend Development Skills\nJava (5 years)\nSpring Boot (4 years)\nREST API\nMicroservices",

  "Database Technologies\nPostgreSQL (4 years)\nMongoDB (2 years)\nMySQL",

  "DevOps and Infrastructure\nDocker\nGit\nAWS",

  "Other Tools\nPostman\nJira"
]

3. "experiences" (list of strings): A list where each element is a formatted text block for an experience detail.
   Example:
   Senior Backend Developer at ABC Tech
   Jan 2020 – Jun 2023
   
   Developed microservices architecture handling millions of requests.
   
   Responsibilities:
   - Built REST APIs using Spring Boot
   - Designed Kafka event streaming pipelines
   
   Technologies:
   Java, Spring Boot, Kafka, PostgreSQL

4. "projects" (list of strings): A list where each element is a formatted text block for a project.
   Example:
   E-commerce Platform
   Role: Tech Lead
   Team Size: 4
   
   Developed scalable order processing system handling 10k daily orders.
   
   Technologies:
   React, Node.js, MongoDB, Redis

5. "educations" (list of strings): A list where each element is a formatted text block for an education entry.
   Example:
   Bachelor of Computer Science
   University of Technology
   Major: Software Engineering

Return ONLY a valid JSON object with the keys: "overview", "skills", "experiences", "projects", "educations"."""

    messages = [
        {"role": "system", "content": prompt},
        {"role": "user", "content": resume_data.model_dump_json(exclude_none=True)}
    ]
    
    response = create_json_chat_completion(
        model="gpt-4o-mini",
        messages=messages,
        timeout=60,
        temperature=0.1
    )
    
    content = response.choices[0].message.content
    try:
        return json.loads(content)
    except Exception as e:
        logger.error(f"Failed to parse LLM JSON output for chunking: {e}")
        return {}


async def process_and_embed_resume(request: dict) -> EmbeddingResumeResultMessage:

    resume_data = EmbeddingResumeRequestMessage(**request)

    chunks = []
    metadata = []

    logger.info(f"Building semantic chunks for resume {resume_data.id} using AI...")

    logger.info(f"Deleting existing vector chunks for resume {resume_data.id}")
    delete_filter = Filter(
        must=[
            FieldCondition(
                key="resume_id",
                match=MatchValue(value=resume_data.id)
            )
        ]
    )
    vector_service.delete_points_by_filter(
        settings.QDRANT_COLLECTION_NAME,
        delete_filter
    )

    ai_chunks_dict = await asyncio.to_thread(_generate_chunks_with_ai, resume_data)

    overview = ai_chunks_dict.get("overview")
    if overview:
        chunks.append(overview)
        metadata.append({"type": "overview", "sourceId": 0})
        
    skills = ai_chunks_dict.get("skills")
    if skills and isinstance(skills, list):
        for i, skill_chunk in enumerate(skills):
            if not skill_chunk: continue
            chunks.append(skill_chunk)
            metadata.append({"type": "skill", "sourceId": i})
    elif skills and isinstance(skills, str):
        chunks.append(skills)
        metadata.append({"type": "skill", "sourceId": 0})
        
    for exp_index, exp in enumerate(ai_chunks_dict.get("experiences") or []):
        if not exp: continue
        chunks.append(exp)
        metadata.append({"type": "experience", "sourceId": exp_index})
        
    for proj_index, proj in enumerate(ai_chunks_dict.get("projects") or []):
        if not proj: continue
        chunks.append(proj)
        metadata.append({"type": "project", "sourceId": proj_index})
        
    for edu_index, edu in enumerate(ai_chunks_dict.get("educations") or []):
        if not edu: continue
        chunks.append(edu)
        metadata.append({"type": "education", "sourceId": edu_index})

    if not chunks:

        logger.warning(f"No valid chunks found for resume {resume_data.id}")

        return EmbeddingResumeResultMessage(
            id=resume_data.id,
            status=EmbedStatus.FAIL,
            errorMessage="No valid chunks found for resume"
        )

    logger.info(f"Generating embeddings for {len(chunks)} chunks")

    embeddings = await asyncio.to_thread(
        create_embeddings,
        texts=chunks
    )

    points = []

    for i, (text, vector, meta) in enumerate(zip(chunks, embeddings, metadata)):

        point = PointStruct(
            id=str(uuid.uuid4()),
            vector=vector,
            payload={
                "resume_id": resume_data.id,
                "chunk_type": meta["type"],
                "source_id": meta["sourceId"],
                "chunk_index": i,
                "language": resume_data.language,
                "text": text,
                **meta
            }
        )

        points.append(point)

    logger.info(
        f"Upserting {len(points)} vector chunks to Qdrant for resume {resume_data.id}"
    )

    vector_service.upsert_points(
        settings.QDRANT_COLLECTION_NAME,
        points
    )

    return EmbeddingResumeResultMessage(
        id=resume_data.id,
        status=EmbedStatus.SUCCESS
    )