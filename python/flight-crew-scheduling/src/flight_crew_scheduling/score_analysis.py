from dataclasses import dataclass, field

from .json_serialization import *
from .domain import *


class MatchAnalysisDTO(JsonDomainBase):
    name: str
    score: Annotated[HardSoftScore, ScoreSerializer]
    justification: object


class ConstraintAnalysisDTO(JsonDomainBase):
    name: str
    weight: Annotated[HardSoftScore, ScoreSerializer]
    matches: list[MatchAnalysisDTO]
    score: Annotated[HardSoftScore, ScoreSerializer]
