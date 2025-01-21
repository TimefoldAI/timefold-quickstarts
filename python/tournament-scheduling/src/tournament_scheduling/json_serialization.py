from timefold.solver.score import HardMediumSoftDecimalScore, ConstraintJustification
from pydantic import BaseModel, ConfigDict, Field, PlainSerializer, BeforeValidator, ValidationInfo
from pydantic.alias_generators import to_camel
from typing import Any, Dict


def make_list_item_validator(key: str):
    def validator(v: Any, info: ValidationInfo) -> Any:
        if v is None:
            return None

        if not isinstance(v, int) or not info.context:
            return v

        return info.context.get(key)[v]

    return BeforeValidator(validator)


TeamDeserializer = make_list_item_validator('teams')
DayDeserializer = make_list_item_validator('days')


IdSerializer = PlainSerializer(
    lambda item: getattr(item, 'id', getattr(item, 'date_index', None)) if item is not None else None,
    return_type=int | None
)
ScoreSerializer = PlainSerializer(lambda score: str(score) if score is not None else None,
                                  return_type=str | None)


def validate_score(v: Any, info: ValidationInfo) -> Any:
    if isinstance(v, HardMediumSoftDecimalScore) or v is None:
        return v
    if isinstance(v, str):
        return HardMediumSoftDecimalScore.parse(v)
    raise ValueError('"score" should be a string')


ScoreValidator = BeforeValidator(validate_score)

class JsonDomainBase(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        from_attributes=True,
    )
