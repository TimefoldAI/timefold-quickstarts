from timefold.solver.score import HardSoftScore
from pydantic import BaseModel, ConfigDict, Field, PlainSerializer, BeforeValidator, ValidationInfo
from pydantic.alias_generators import to_camel
from typing import Any, Dict


def make_list_item_validator(key: str):
    def validator(v: Any, info: ValidationInfo) -> Any:
        if v is None:
            return None

        if isinstance(v, str) and info.context and key in info.context:
            return info.context[key].get(v, None)

        return v

    return BeforeValidator(validator)


FlightDeserializer = make_list_item_validator('flights')
AirportDeserializer = make_list_item_validator('airports')
EmployeeDeserializer = make_list_item_validator('employees')

IdSerializer = PlainSerializer(
    lambda item: getattr(item, 'code', getattr(item, 'id', getattr(item, 'flight_number', None))) if item is not None else None,
    return_type=str | None
)
ScoreSerializer = PlainSerializer(lambda score: str(score) if score is not None else None,
                                  return_type=str | None)


def validate_score(v: Any, info: ValidationInfo) -> Any:
    if isinstance(v, HardSoftScore) or v is None:
        return v
    if isinstance(v, str):
        return HardSoftScore.parse(v)
    raise ValueError('"score" should be a string')


def validate_taxi_time_in_minutes(value: Any, info: ValidationInfo) -> Dict[str, int]:
    if not isinstance(value, dict):
        raise ValueError("taxi_time_in_minutes must be a dictionary.")

    for key, val in value.items():
        if not isinstance(key, str):
            raise ValueError(f"Key {key} in taxi_time_in_minutes must be a Airport instance.")
        if not isinstance(val, int):
            raise ValueError(f"Value for {key} must be an integer.")

    return value


ScoreValidator = BeforeValidator(validate_score)
TaxiTimeValidator = BeforeValidator(validate_taxi_time_in_minutes)

class JsonDomainBase(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        from_attributes=True,
    )
