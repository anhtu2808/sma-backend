"""Service to find the best matching resumes for a given job using Qdrant vector similarity."""

from collections import defaultdict

from loguru import logger
from qdrant_client.models import Filter, FieldCondition, MatchValue

from app.core.config import settings
from app.schemas.proposed_cv import (
    ProposedCVRequestMessage,
    ProposedCVData,
    ProposedCVResultMessage,
    ProposeStatus,
)
from app.services.vector_service import vector_service

# Number of similar resume chunks to retrieve per job chunk query
_TOP_K_PER_CHUNK = 50

# Maximum number of proposed resumes to return
_MAX_PROPOSED_RESUMES = 20


def find_proposed_resumes(request: dict) -> ProposedCVResultMessage:
    """
    For a given job (identified by id in the request), find the most similar
    resumes by comparing their embedded vectors in Qdrant.

    Algorithm:
      1. Scroll all embedded chunks for the job from the job collection.
      2. For each job chunk vector, search the resume collection for the
         most similar resume chunks.
      3. Aggregate scores by resume_id (average cosine similarity).
      4. Return the top N resumes with their match rates.
    """
    data = ProposedCVRequestMessage(**request)
    job_id = data.id

    logger.info("Finding proposed resumes for jobId={}", job_id)

    # 1. Get all job vector chunks
    job_filter = Filter(
        must=[FieldCondition(key="job_id", match=MatchValue(value=job_id))]
    )
    job_points = vector_service.scroll_points(
        collection_name=settings.QDRANT_JOB_COLLECTION_NAME,
        scroll_filter=job_filter,
        limit=100,
    )

    if not job_points:
        logger.warning("No embedded vectors found for jobId={}", job_id)
        return ProposedCVResultMessage(
            status=ProposeStatus.FINISHED, jobId=job_id, proposedCVs=[]
        )

    logger.info("Found {} job chunks for jobId={}", len(job_points), job_id)

    # 2. For each job chunk, search for similar resume chunks
    resume_scores: dict[int, list[float]] = defaultdict(list)

    for job_point in job_points:
        job_vector = job_point.vector
        if not job_vector:
            continue

        results = vector_service.search_points(
            collection_name=settings.QDRANT_COLLECTION_NAME,
            query_vector=job_vector,
            limit=_TOP_K_PER_CHUNK,
        )

        for scored_point in results:
            resume_id = scored_point.payload.get("resume_id")
            if resume_id is not None:
                resume_scores[resume_id].append(scored_point.score)

    if not resume_scores:
        logger.info("No matching resumes found for jobId={}", job_id)
        return ProposedCVResultMessage(
            status=ProposeStatus.FINISHED, jobId=job_id, proposedCVs=[]
        )

    # 3. Aggregate: average score per resume
    aggregated = []
    for resume_id, scores in resume_scores.items():
        avg_score = sum(scores) / len(scores)
        aggregated.append((resume_id, avg_score))

    # 4. Sort descending by score and take top N
    aggregated.sort(key=lambda x: x[1], reverse=True)
    top_resumes = aggregated[:_MAX_PROPOSED_RESUMES]

    proposed_cvs = [
        ProposedCVData(
            resumeId=resume_id,
            matchRate=round(score, 4),
        )
        for resume_id, score in top_resumes
    ]

    logger.info(
        "Found {} proposed resumes for jobId={}, top matchRate={}",
        len(proposed_cvs),
        job_id,
        proposed_cvs[0].matchRate if proposed_cvs else 0,
    )

    return ProposedCVResultMessage(
        status=ProposeStatus.FINISHED, jobId=job_id, proposedCVs=proposed_cvs
    )
