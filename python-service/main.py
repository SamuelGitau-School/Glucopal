from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.routes.chat import router as chat_router
from app.routes.analytics import router as analytics_router

from app.services.chat_service import lifespan_pool

app = FastAPI(
    title="Diabetes AI Service",
    description="Python microservice for AI-powered chat and glucose analytics",
    version="1.0.0",
    lifespan=lifespan_pool
)
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:5173",
        "http://localhost:8080",
        "https://glucopal.vercel.app"  # add this
    ],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(chat_router, prefix="", tags=["Chat"])
app.include_router(analytics_router, prefix="/analytics", tags=["Analytics"])


@app.get("/health")
def health():
    return {"status": "ok"}
