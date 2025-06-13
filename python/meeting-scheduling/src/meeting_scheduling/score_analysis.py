from pydantic import BaseModel
from typing import List, Any

class MatchAnalysisDTO(BaseModel):
    name: str
    score: int
    justification: Any

class ConstraintAnalysisDTO(BaseModel):
    name: str
    weight: str
    score: int
    matches: List[MatchAnalysisDTO]