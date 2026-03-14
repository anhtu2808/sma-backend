from fastapi import APIRouter

from app.schemas.suggestion import SuggestResultMessage, ReSuggestRequestMessage
from app.services.re_suggestion_service import generate_re_suggestions

router = APIRouter(prefix="/suggestion")

@router.post("/re-generate", response_model=SuggestResultMessage)
async def re_generate(req: ReSuggestRequestMessage):
    result = await generate_re_suggestions(req.model_dump())
    return result