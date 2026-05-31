import os
import re
import httpx
from typing import Optional
from contextlib import asynccontextmanager

OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY")
OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/com`pletions"

SYSTEM_PROMPT = """You are a compassionate and knowledgeable diabetes care assistant.
Your role is to:
- Answer questions about blood glucose, nutrition, medication, exercise, and diabetes management.
- Provide evidence-based general information in clear, friendly language.
- Always remind users to consult their healthcare provider for personalised medical advice.
- Never diagnose, prescribe, or give specific dosage instructions.
- Flag any values that sound dangerously low (<55 mg/dL) or high (>300 mg/dL) and urge immediate medical attention.
Keep responses concise (2-4 sentences) unless a detailed explanation is genuinely needed."""

# The global client variable
async_client: Optional[httpx.AsyncClient] = None

# ── Lifespan Setup ──────────────────────────────────────────────────
# Use this in your framework (e.g., FastAPI: @app.lifespan or lifespan=lifespan)
@asynccontextmanager
async def lifespan_pool():
    global async_client
    async_client = httpx.AsyncClient(timeout=30.0)
    try:
        yield
    finally:
        await async_client.aclose()

# ── Rule-based fallback (Pre-compiled Regex Patterns) ──────────────

RAW_KEYWORD_RESPONSES = [
    (["low", "hypoglycemia", "hypo", "shaky", "dizzy"],
     "Low blood sugar (hypoglycemia) occurs when your glucose drops below 70 mg/dL. "
     "Follow the 15-15 rule: consume 15 g of fast-acting carbs, wait 15 minutes, and recheck. "
     "If symptoms persist, seek medical help immediately."),
    (["high", "hyperglycemia", "hyper"],
     "High blood sugar can happen after meals, illness, or missed medication. "
     "Drink water, take your prescribed medication if directed, and recheck in 1-2 hours. "
     "Contact your healthcare provider if readings stay above 250 mg/dL."),
    (["a1c", "hba1c", "hemoglobin"],
     "HbA1c reflects your average blood glucose over the past 2-3 months. "
     "A goal below 7% is typical for most adults with diabetes, but your doctor may personalise this."),
    (["diet", "eat", "food", "carb", "nutrition", "meal"],
     "A balanced diabetes-friendly diet focuses on consistent carbohydrate portions, "
     "plenty of non-starchy vegetables, lean protein, and healthy fats."),
    (["exercise", "workout", "activity", "walk", "run"],
     "Regular physical activity improves insulin sensitivity and helps lower blood glucose. "
     "Aim for at least 150 minutes of moderate activity per week."),
    (["insulin", "medication", "medicine", "metformin", "drug"],
     "Taking diabetes medications consistently as prescribed is key to glucose control. "
     "Never adjust doses without consulting your healthcare provider."),
    (["stress", "anxiety", "mental", "emotional"],
     "Stress hormones like cortisol can raise blood glucose levels. "
     "Mindfulness, deep breathing, and regular physical activity can help manage stress."),
]

# Optimisation: Compile patterns once on startup rather than during the request path
KEYWORD_RESPONSES: list[tuple[re.Pattern, str]] = [
    (re.compile(rf"\b({'|'.join(keywords)})\b"), response)
    for keywords, response in RAW_KEYWORD_RESPONSES
]

GENERIC_REPLY = (
    "I'm your diabetes care assistant. I can answer questions about blood glucose, "
    "nutrition, medication, exercise, and general diabetes management. "
    "For personalised medical advice, please consult your healthcare provider."
)


def _rule_based_reply(message: str) -> str:
    lower = message.lower()
    for pattern, response in KEYWORD_RESPONSES:
        if pattern.search(lower):
            return response
    return GENERIC_REPLY


async def _openrouter_reply(message: str) -> Optional[str]:
    if not OPENROUTER_API_KEY:
        return None

    global async_client
    created_locally = False
    client: httpx.AsyncClient

    if async_client is None:
        client = httpx.AsyncClient(timeout=30.0)
        created_locally = True
    else:
        client = async_client

    try:
        headers = {
            "Authorization": f"Bearer {OPENROUTER_API_KEY}",
            "Content-Type": "application/json",
            "HTTP-Referer": "https://glucopal-peach.vercel.app",
            "X-Title": "Glucopal"
        }
        payload = {
            "model": "mistralai/mistral-7b-instruct:free",
            "messages": [
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": message[:1000]}
            ],
            "max_tokens": 300
        }
        response = await client.post(OPENROUTER_URL, json=payload, headers=headers)
        response.raise_for_status()
        data = response.json()
        return data["choices"][0]["message"]["content"].strip()
    except Exception as e:
        print(f"OpenRouter error: {e}")
        return None
    finally:
        if created_locally:
            await client.aclose()


async def generate_reply(message: str) -> str:
    llm_reply = await _openrouter_reply(message)
    if llm_reply:
        return llm_reply
    return _rule_based_reply(message)