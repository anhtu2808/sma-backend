import uuid
import asyncio
from loguru import logger
from qdrant_client.models import PointStruct

from app.core.config import settings
from app.schemas.embedding import (
    EmbeddingJobRequestMessage,
    EmbeddingJobResultMessage
)
from app.clients.openai_client import create_embeddings
from app.services.vector_service import vector_service


def _build_job_info_text(job: EmbeddingJobRequestMessage) -> str:
    parts = []
    if job.title:
        parts.append(f"Job Title: {job.title}")
    if job.jobLevel:
        parts.append(f"Level: {job.jobLevel}")
    if job.expertiseName:
        parts.append(f"Expertise: {job.expertiseName}")
    if job.about:
        parts.append(f"About: {job.about}")
    return "\n".join(parts)


async def process_and_embed_job(request: dict) -> EmbeddingJobResultMessage:

    job_data = EmbeddingJobRequestMessage(**request)

    chunks = []
    metadata = []

    logger.info(f"Building semantic chunks for job {job_data.id}")

    # ---------- 1. General Info ----------
    info_text = _build_job_info_text(job_data)
    if info_text.strip():
        chunks.append(info_text)
        metadata.append({
            "type": "general_info",
            "sourceId": 0
        })

    # ---------- 2. Responsibilities ----------
    if job_data.responsibilities and job_data.responsibilities.strip():
        chunks.append(f"Responsibilities:\n{job_data.responsibilities}")
        metadata.append({
            "type": "responsibilities",
            "sourceId": 0
        })

    # ---------- 3. Requirements ----------
    if job_data.requirement and job_data.requirement.strip():
        chunks.append(f"Requirements:\n{job_data.requirement}")
        metadata.append({
            "type": "requirements",
            "sourceId": 0
        })

    # ---------- 4. Skills ----------
    for index, skill in enumerate(job_data.skills or []):
        if not skill.name:
            continue
        
        text = f"Required Skill: {skill.name}"
        chunks.append(text)
        metadata.append({
            "type": "skill",
            "sourceId": index
        })

    if not chunks:
        logger.warning(f"No valid chunks found for job {job_data.id}")
        return EmbeddingJobResultMessage(
            jobId=job_data.id,
            status="EMPTY"
        )

    logger.info(f"Generating embeddings for {len(chunks)} chunks")

    embeddings = await asyncio.to_thread(
        create_embeddings,
        texts=chunks
    )

    points = []

    for text, vector, meta in zip(chunks, embeddings, metadata):
        point = PointStruct(
            id=str(uuid.uuid4()),
            vector=vector,
            payload={
                "job_id": job_data.id,
                "chunk_type": meta["type"],
                "source_id": meta["sourceId"],
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
        jobId=job_data.id,
        status="SUCCESS"
    )
