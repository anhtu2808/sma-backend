import uuid
import asyncio
from loguru import logger
from qdrant_client.models import PointStruct, Filter, FieldCondition, MatchValue

from app.core.config import settings
from app.schemas.embedding import (
    EmbeddingJobRequestMessage,
    EmbeddingJobResultMessage,
    EmbedStatus
)
from app.clients.openai_client import create_embeddings, create_json_chat_completion
from app.services.vector_service import vector_service
import json


def _generate_job_chunks_with_ai(job_data: EmbeddingJobRequestMessage) -> dict:
    prompt = """You are an expert HR data assistant. Your task is to transform the provided job description data into clean, highly structured text chunks optimized for vector embedding and semantic search.

Follow these rules:
1. Keep each chunk concise (100–300 words max).
2. Preserve technical context (technologies, role requirements, responsibilities).
3. Do NOT invent any information.
4. Omit fields that are missing.
5. Return ONLY a valid JSON object.

Based on the provided JSON data, generate a JSON object containing the exact fields below. Follow the format of the examples strictly, omitting any data points that are not present in the input.

1. "job_overview" (string or null): A single summary paragraph describing the role, level, and overview.
   Example:
   Senior Backend Engineer role focused on building scalable
   microservices for fintech payment systems.
   
   We are looking for engineers experienced in Java, Spring Boot,
   distributed systems and cloud infrastructure.

2. "job_responsibilities" (list of strings): A list where each element is a formatted text block for an job responsibility detail.
   Example:
   Design and develop scalable backend services using Java and Spring Boot.
   
   Build RESTful APIs and microservices architecture.
   
   Collaborate with frontend and DevOps teams to deploy applications.

3. "job_requirements" (list of strings): A list where each element is a formatted text block for an job requirement detail.
   Example:
   Requirements:
   
   • 4+ years backend development
   • Strong Java and Spring Boot experience
   • Experience with microservices architecture
   • Experience with relational databases

4. "job_skills" (string or null): A formatted text block summarizing the required skills.
   Example:
   Looking for engineers with strong backend development
   experience using Java and Spring Boot in microservices environments.

Return ONLY a valid JSON object with the keys: "job_overview", "job_responsibilities", "job_requirements", "job_skills"."""

    messages = [
        {"role": "system", "content": prompt},
        {"role": "user", "content": job_data.model_dump_json(exclude_none=True)}
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
        logger.error(f"Failed to parse LLM JSON output for job chunking: {e}")
        return {}


async def process_and_embed_job(request: dict) -> EmbeddingJobResultMessage:

    job_data = EmbeddingJobRequestMessage(**request)

    chunks = []
    metadata = []

    logger.info(f"Building semantic chunks for job {job_data.id}")

    logger.info(f"Deleting existing vector chunks for job {job_data.id}")
    delete_filter = Filter(
        must=[
            FieldCondition(
                key="job_id",
                match=MatchValue(value=job_data.id)
            )
        ]
    )
    vector_service.delete_points_by_filter(
        settings.QDRANT_JOB_COLLECTION_NAME,
        delete_filter
    )

    ai_chunks_dict = await asyncio.to_thread(_generate_job_chunks_with_ai, job_data)

    overview = ai_chunks_dict.get("job_overview")
    if overview:
        chunks.append(overview)
        metadata.append({"type": "job_overview", "sourceId": 0})
        
    responsibilities = ai_chunks_dict.get("job_responsibilities")
    for i, resp in enumerate(responsibilities):
        if not resp:
            continue
        chunks.append(resp)
        metadata.append({
            "type": "job_responsibility",
            "sourceId": i
        })
        
    requirements = ai_chunks_dict.get("job_requirements")
    for i, req in enumerate(requirements):
        if not req:
            continue
        chunks.append(req)
        metadata.append({
            "type": "job_requirement",
            "sourceId": i
        })
        
    skills = ai_chunks_dict.get("job_skills")
    if skills:
        chunks.append(skills)
        metadata.append({"type": "job_skills", "sourceId": 0})

    if not chunks:
        logger.warning(f"No valid chunks found for job {job_data.id}")
        return EmbeddingJobResultMessage(
            id=job_data.id,
            status=EmbedStatus.FAIL,
            errorMessage="No valid chunks found for job"
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
                "job_id": job_data.id,
                "chunk_type": meta["type"],
                "source_id": meta["sourceId"],
                "chunk_index": i,
                "text": text,
                **meta
            }
        )
        points.append(point)

    logger.info(f"Upserting {len(points)} vector chunks to Qdrant for job {job_data.id}")

    vector_service.upsert_points(
        settings.QDRANT_JOB_COLLECTION_NAME,
        points
    )

    return EmbeddingJobResultMessage(
        id=job_data.id,
        status=EmbedStatus.SUCCESS
    )
