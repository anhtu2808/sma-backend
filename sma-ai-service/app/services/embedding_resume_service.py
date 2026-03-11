import uuid
import asyncio
from loguru import logger
from qdrant_client.models import PointStruct

from app.core.config import settings
from app.schemas.embedding import (
    EmbeddingResumeRequestMessage,
    EmbeddingResumeResultMessage,
    EmbedStatus
)
from app.clients.openai_client import create_embeddings
from app.services.vector_service import vector_service


def _safe_join(values):
    if not values:
        return ""
    return ", ".join([v for v in values if v])


def _build_skill_text(skill):

    if skill.yearsOfExperience:
        return f"Skill: {skill.name} with {skill.yearsOfExperience} years of professional experience"

    return f"Skill: {skill.name}"


def _build_education_text(edu):

    parts = []

    if edu.degree:
        parts.append(f"{edu.degree}")

    if edu.majorField:
        parts.append(f"in {edu.majorField}")

    if edu.institution:
        parts.append(f"from {edu.institution}")

    return " ".join(parts)


def _build_experience_text(exp):

    texts = []

    if not exp.details:
        return texts

    for detail in exp.details:

        skills = _safe_join(
            [s.name for s in (detail.experienceSkills or [])]
        )

        description = detail.description or ""

        text = f"""
Worked at {exp.company or 'a company'} as {detail.title or 'a developer'}.
{description}
"""

        if skills:
            text += f"\nTechnologies used: {skills}"

        texts.append(text.strip())

    return texts


def _build_project_text(project):

    skills = _safe_join(
        [s.name for s in (project.projectSkills or [])]
    )

    text = f"""
Project: {project.title or 'Unnamed project'}
Role: {project.position or 'Developer'}
{project.description or ''}
"""

    if skills:
        text += f"\nTechnologies used: {skills}"

    return text.strip()


async def process_and_embed_resume(request: dict) -> EmbeddingResumeResultMessage:

    resume_data = EmbeddingResumeRequestMessage(**request)

    chunks = []
    metadata = []

    logger.info(f"Building semantic chunks for resume {resume_data.id}")

    # ---------- Skills ----------
    for index, skill in enumerate(resume_data.skills or []):

        if not skill.name:
            continue

        text = _build_skill_text(skill)

        chunks.append(text)

        metadata.append({
            "type": "skill",
            "sourceId": index
        })

    # ---------- Education ----------
    for index, edu in enumerate(resume_data.educations or []):

        text = _build_education_text(edu)

        if not text:
            continue

        chunks.append(text)

        metadata.append({
            "type": "education",
            "sourceId": index
        })

    # ---------- Experience ----------
    for exp_index, exp in enumerate(resume_data.experiences or []):

        exp_chunks = _build_experience_text(exp)

        for text in exp_chunks:

            chunks.append(text)

            metadata.append({
                "type": "experience",
                "sourceId": exp_index,
                "company": exp.company
            })

    # ---------- Projects ----------
    for proj_index, proj in enumerate(resume_data.projects or []):

        text = _build_project_text(proj)

        if not text:
            continue

        chunks.append(text)

        metadata.append({
            "type": "project",
            "sourceId": proj_index
        })

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

    for text, vector, meta in zip(chunks, embeddings, metadata):

        point = PointStruct(
            id=str(uuid.uuid4()),
            vector=vector,
            payload={
                "resume_id": resume_data.id,
                "chunk_type": meta["type"],
                "source_id": meta["sourceId"],
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