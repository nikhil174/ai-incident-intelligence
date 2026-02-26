from fastapi import FastAPI
from pydantic import BaseModel, Field
from statistics import mean, pstdev
from typing import List

app = FastAPI(title="AI Analysis Service", version="1.0.0")


class EventFeatures(BaseModel):
    service_name: str
    severity: str
    message: str
    recent_latencies_ms: List[float] = Field(default_factory=list)
    error_rate: float = 0.0


class AnalysisResponse(BaseModel):
    anomaly_score: float
    summary: str
    probable_root_cause: str
    remediation: List[str]


@app.get("/health")
def health() -> dict:
    return {"status": "ok"}


def z_score_score(values: List[float]) -> float:
    if len(values) < 3:
        return 0.2
    avg = mean(values)
    sigma = pstdev(values) or 1.0
    latest = values[-1]
    z = abs((latest - avg) / sigma)
    return min(z / 5.0, 1.0)


def severity_factor(severity: str) -> float:
    return {
        "LOW": 0.2,
        "MEDIUM": 0.5,
        "HIGH": 0.75,
        "CRITICAL": 0.95,
    }.get(severity.upper(), 0.3)


@app.post("/analyze", response_model=AnalysisResponse)
def analyze(payload: EventFeatures) -> AnalysisResponse:
    latency_score = z_score_score(payload.recent_latencies_ms)
    error_score = min(payload.error_rate, 1.0)
    sev_score = severity_factor(payload.severity)
    anomaly_score = round(0.4 * latency_score + 0.3 * error_score + 0.3 * sev_score, 3)

    probable_root_cause = (
        "Database saturation or slow downstream dependency"
        if anomaly_score > 0.7
        else "Transient service degradation"
    )
    summary = (
        f"{payload.service_name} shows anomaly score {anomaly_score}. "
        f"Severity={payload.severity}, error_rate={payload.error_rate}."
    )

    remediation = [
        "Check p95/p99 latency and DB connection pool usage",
        "Inspect recent deploys and dependency health",
        "Scale affected service and enable circuit-breaker policy",
    ]

    return AnalysisResponse(
        anomaly_score=anomaly_score,
        summary=summary,
        probable_root_cause=probable_root_cause,
        remediation=remediation,
    )
