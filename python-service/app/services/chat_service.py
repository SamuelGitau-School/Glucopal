
#Generates diabetes-care AI responses.

#By default uses a rule-based responder so the service works out of the box
#without any API keys. To enable an LLM, set the OPENAI_API_KEY environment
#variable — the service will automatically switch to GPT-4o-mini.

import os
import re
from typing import Optional


# Optional: LLM integration (requires: pip install openai)

SYSTEM_PROMPT = """You are a compassionate and knowledgeable diabetes care assistant.
Your role is to:
- Answer questions about blood glucose, nutrition, medication, exercise, and diabetes management.
- Provide evidence-based general information in clear, friendly language.
- Always remind users to consult their healthcare provider for personalised medical advice.
- Never diagnose, prescribe, or give specific dosage instructions.
- Flag any values that sound dangerously low (<55 mg/dL) or high (>300 mg/dL) and urge immediate medical attention.
Keep responses concise (2–4 sentences) unless a detailed explanation is genuinely needed."""

# Rule-based fallback (no API key required)

KEYWORD_RESPONSES: list[tuple[list[str], str]] = [
    (
        ["low", "hypoglycemia", "hypo", "shaky", "dizzy"],
        "Low blood sugar (hypoglycemia) occurs when your glucose drops below 70 mg/dL. "
        "Follow the 15-15 rule: consume 15 g of fast-acting carbs, wait 15 minutes, and recheck. "
        "If symptoms persist, seek medical help immediately.",
    ),
    (
        ["high", "hyperglycemia", "hyper"],
        "High blood sugar can happen after meals, illness, or missed medication. "
        "Drink water, take your prescribed medication if directed, and recheck in 1–2 hours. "
        "Contact your healthcare provider if readings stay above 250 mg/dL.",
    ),
    (
        ["a1c", "hba1c", "hemoglobin"],
        "HbA1c reflects your average blood glucose over the past 2–3 months. "
        "A goal below 7% is typical for most adults with diabetes, but your doctor may personalise this. "
        "It is measured by a blood test every 3–6 months.",
    ),
    (
        ["diet", "eat", "food", "carb", "nutrition", "meal"],
        "A balanced diabetes-friendly diet focuses on consistent carbohydrate portions, "
        "plenty of non-starchy vegetables, lean protein, and healthy fats. "
        "Working with a registered dietitian can help you build a personalised meal plan.",
    ),
    (
        ["exercise", "workout", "activity", "walk", "run"],
        "Regular physical activity improves insulin sensitivity and helps lower blood glucose. "
        "Aim for at least 150 minutes of moderate activity per week, and check your glucose "
        "before and after exercise. Always carry a fast-acting carb source during workouts.",
    ),
    (
        ["insulin", "medication", "medicine", "metformin", "drug"],
        "Taking diabetes medications consistently as prescribed is key to glucose control. "
        "Never adjust doses without consulting your healthcare provider. "
        "Keep a log of any side effects and discuss them at your next appointment.",
    ),
    (
        ["stress", "anxiety", "mental", "emotional"],
        "Stress hormones like cortisol can raise blood glucose levels. "
        "Mindfulness, deep breathing, and regular physical activity can help manage stress. "
        "Consider speaking with a mental health professional if stress feels overwhelming.",
    ),
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


# Public interface

async def generate_reply(message: str) -> str:
    """Return a diabetes-care-aware AI reply for the given user message."""

    if _openai_client is not None:
        try:
            completion = await _openai_client.chat.completions.create(
                model="gpt-4o-mini",
                max_tokens=300,
                messages=[
                    {"role": "system", "content": SYSTEM_PROMPT},
                    {"role": "user", "content": message},
                ],
            )
            return completion.choices[0].message.content.strip()
        except Exception:
            pass

# Fall through to rule-based

    return _rule_based_reply(message)
