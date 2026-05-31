# Generates diabetes-care AI responses.
# Uses OpenRouter LLM if OPENROUTER_API_KEY is set, otherwise falls back to rule-based responses.

import os
import httpx
from typing import Optional

OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY")
OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"

SYSTEM_PROMPT = """You are a compassionate and knowledgeable diabetes care assistant.
Your role is to:
- Answer questions about blood glucose, nutrition, medication, exercise, and diabetes management.
- Provide evidence-based general information in clear, friendly language.
- Always remind users to consult their healthcare provider for personalised medical advice.
- Never diagnose, prescribe, or give specific dosage instructions.
- Flag any values that sound dangerously low (<55 mg/dL) or high (>300 mg/dL) and urge immediate medical attention.
Keep responses concise (2-4 sentences) unless a detailed explanation is genuinely needed."""

# ── Rule-based fallback ────────────────────────────────────────────

KEYWORD_RESPONSES: list[tuple[list[str], str]] = [
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

GENERIC_REPLY = (
    "I'm your diabetes care assistant. I can answer questions about blood glucose, "
    "nutrition, medication, exercise, and general diabetes management. "
    "For personalised medical advice, please consult your healthcare provider."
)


def _rule_based_reply(message: str) -> str:
    lower = message.lower()
    for keywords, response in KEYWORD_RESPONSES:
        if any(kw in lower for kw in keywords):
            return response
    return GENERIC_REPLY




async def _openrouter_reply(message: str) -> Optional[str]:
    if not OPENROUTER_API_KEY:
        return None
    try:
        headers = {
            "Authorization": f"Bearer {OPENROUTER_API_KEY}",
            "Content-Type": "application/json",
            "HTTP-Referer": "https://glucopal.vercel.app",
            "X-Title": "Glucopal"
        }
        payload = {
            "model": "openrouter/owl-alpha",
            "messages": [
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": message}
            ],
            "max_tokens": 300
        }
        async with httpx.AsyncClient() as client:
            response = await client.post(
                OPENROUTER_URL, json=payload, headers=headers, timeout=30
            )
            response.raise_for_status()
            data = response.json()
            return data["choices"][0]["message"]["content"].strip()
    except Exception:
        return None


# ── Public interface ───────────────────────────────────────────────

async def generate_reply(message: str) -> str:
    llm_reply = await _openrouter_reply(message)
    if llm_reply:
        return llm_reply
    return _rule_based_reply(message)