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
