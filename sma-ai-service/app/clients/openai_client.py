"""Shared OpenAI client helpers."""

from openai import OpenAI

from app.core.config import settings


def create_json_chat_completion(
    *,
    model: str,
    messages: list[dict],
    timeout: int,
    temperature: float = 0.1,
):
    """
    Create a JSON-only chat completion call.

    This wrapper keeps OpenAI API invocation in one place so
    future AI services can reuse the same integration layer.
    """
    client = OpenAI(
        api_key=settings.OPENAI_API_KEY,
        timeout=timeout,
    )

    return client.chat.completions.create(
        model=model,
        messages=messages,
        temperature=temperature,
        response_format={"type": "json_object"},
    )

def create_embeddings(
    *,
    texts: list[str],
    model: str = "text-embedding-3-small",
    timeout: int = 60,
) -> list[list[float]]:
    """
    Generate vectors using OpenAI embeddings for multiple texts.
    """
    client = OpenAI(
        api_key=settings.OPENAI_API_KEY,
        timeout=timeout,
    )
    response = client.embeddings.create(
        input=texts,
        model=model
    )
    return [data.embedding for data in response.data]
