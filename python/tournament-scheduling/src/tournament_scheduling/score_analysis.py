from dataclasses import dataclass
from decimal import Decimal
from .json_serialization import *
from .domain import *


@dataclass
class LoadBalanceJustification(ConstraintJustification):
    unfairness: Decimal


class MatchAnalysisDTO(JsonDomainBase):
    name: str
    score: Annotated[HardMediumSoftDecimalScore, ScoreSerializer]
    justification: object


class ConstraintAnalysisDTO(JsonDomainBase):
    name: str
    weight: Annotated[HardMediumSoftDecimalScore, ScoreSerializer]
    matches: list[MatchAnalysisDTO]
    score: Annotated[HardMediumSoftDecimalScore, ScoreSerializer]