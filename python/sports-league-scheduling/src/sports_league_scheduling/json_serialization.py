from timefold.solver.score import HardSoftScore
from pydantic import BaseModel, ConfigDict, Field, PlainSerializer, BeforeValidator, ValidationInfo
from pydantic.alias_generators import to_camel
from typing import Any, Dict


def make_list_item_validator(key: str):
    def validator(v: Any, info: ValidationInfo) -> Any:
        if v is None:
            return None

        if not isinstance(v, (str, int)):
            return v

        if not info.context or key not in info.context:
            raise ValueError(f"Context is missing or does not contain key '{key}'.")

        context_data = info.context.get(key)
        if v not in context_data:
            raise ValueError(f"Value '{v}' not found in context for key '{key}'.")

        return context_data[v]

    return BeforeValidator(validator)


RoundDeserializer = make_list_item_validator('rounds')
TeamDeserializer = make_list_item_validator('teams')

IdStrSerializer = PlainSerializer(
    lambda item: item.id if item is not None else None,
    return_type=str | None
)
IdIntSerializer = PlainSerializer(
    lambda item: item.index if item is not None else None,
    return_type=int | None
)
ScoreSerializer = PlainSerializer(lambda score: str(score) if score is not None else None,
                                  return_type=str | None)


def validate_score(v: Any, info: ValidationInfo) -> Any:
    if isinstance(v, HardSoftScore) or v is None:
        return v
    if isinstance(v, str):
        return HardSoftScore.parse(v)
    raise ValueError('"score" should be a string')


def validate_distance_to_team(value: Any, info: ValidationInfo) -> Dict[str, int]:
    if not isinstance(value, dict):
        raise ValueError("distance_to_team must be a dictionary.")

    for key, val in value.items():
        if not isinstance(key, str):
            raise ValueError(f"Key {key} in distance_to_team must be a Team instance.")
        if not isinstance(val, int):
            raise ValueError(f"Value for {key} must be an integer.")

    return value


ScoreValidator = BeforeValidator(validate_score)
DistanceToTeamValidator = BeforeValidator(validate_distance_to_team)

class JsonDomainBase(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        from_attributes=True,
    )
