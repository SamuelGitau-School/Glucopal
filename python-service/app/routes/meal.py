from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Optional
import httpx
import os
import base64

router = APIRouter()

OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY")

class MealAnalysisRequest(BaseModel):
    image_url: Optional[str] = None
    image_base64: Optional[str] = None

class NutrientEstimate(BaseModel):
    meal_name: str
    carbs_g: float
    protein_g: float
    vitamins_mg: float
    calories: float
    notes: str

@router.post("/meals/analyse", response_model=NutrientEstimate)
async def analyse_meal(request: MealAnalysisRequest):
    if not OPENROUTER_API_KEY:
        raise HTTPException(status_code=503, detail="AI service not configured")

    image_content = None
    if request.image_url:
        image_content = {"type": "image_url", "image_url": {"url": request.image_url}}
    elif request.image_base64:
        image_content = {
            "type": "image_url",
            "image_url": {"url": f"data:image/jpeg;base64,{request.image_base64}"}
        }
    else:
        raise HTTPException(status_code=400, detail="No image provided")

    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.post(
                "https://openrouter.ai/api/v1/chat/completions",
                headers={
                    "Authorization": f"Bearer {OPENROUTER_API_KEY}",
                    "Content-Type": "application/json",
                    "HTTP-Referer": "https://glucopal-peach.vercel.app",
                    "X-Title": "Glucopal"
                },
                json={
                    "model": "meta-llama/llama-3.2-11b-vision-instruct:free",
                    "messages": [
                        {
                            "role": "user",
                            "content": [
                                image_content,
                                {
                                    "type": "text",
                                    "text": """Analyse this meal photo and estimate the nutritional content.
                                    Respond ONLY with a JSON object in this exact format, no other text:
                                    {
                                        "meal_name": "name of the meal",
                                        "carbs_g": estimated carbohydrates in grams as a number,
                                        "protein_g": estimated protein in grams as a number,
                                        "vitamins_mg": estimated vitamins in mg as a number,
                                        "calories": estimated total calories as a number,
                                        "notes": "brief note about the meal for a diabetic patient"
                                    }"""
                                }
                            ]
                        }
                    ],
                    "max_tokens": 300
                }
            )
            response.raise_for_status()
            data = response.json()
            content = data["choices"][0]["message"]["content"].strip()

            import json
            # Clean up any markdown code blocks if present
            content = content.replace("```json", "").replace("```", "").strip()
            nutrients = json.loads(content)
            return NutrientEstimate(**nutrients)

    except Exception as e:
        print(f"Meal analysis error: {e}")
        raise HTTPException(status_code=500, detail="Failed to analyse meal")