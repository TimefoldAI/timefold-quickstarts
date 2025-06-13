from typing import Any, TypeVar, Callable
from pydantic import BaseModel, ConfigDict, PlainSerializer, BeforeValidator
from pydantic_core import core_schema
from pydantic.alias_generators import to_camel
from timefold.solver.score import HardMediumSoftScore

T = TypeVar('T')

# Custom serializer for HardMediumSoftScore
ScoreSerializer = PlainSerializer(
    lambda score: str(score) if score is not None else None,
    return_type=str | None
)

def validate_score(v: Any) -> Any:
    if isinstance(v, HardMediumSoftScore) or v is None:
        return v
    if isinstance(v, str):
        return HardMediumSoftScore.parse(v)
    raise ValueError('"score" should be a string')

ScoreValidator = BeforeValidator(validate_score)

class JsonDomainBase(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        from_attributes=True,
    )

class IdSerializer:
    @classmethod
    def __get_pydantic_core_schema__(
        cls, _source_type: Any, _handler: Callable[[Any], core_schema.CoreSchema]
    ) -> core_schema.CoreSchema:
        def serialize_id(obj: Any) -> str:
            if obj is None:
                return None
            return obj.id
        
        return core_schema.json_schema(
            core_schema.with_info_plain_validator_function(serialize_id),
            json_schema_extra={'type': 'string'}
        )

class TimeGrainDeserializer:
    @classmethod
    def __get_pydantic_core_schema__(
        cls, _source_type: Any, _handler: Callable[[Any], core_schema.CoreSchema]
    ) -> core_schema.CoreSchema:
        def deserialize_time_grain(id: str, info: core_schema.ValidationInfo) -> Any:
            if id is None:
                return None
            context = info.context
            if context and 'time_grains' in context:
                return context['time_grains'].get(id)
            return None
        
        return core_schema.json_schema(
            core_schema.with_info_plain_validator_function(deserialize_time_grain),
            json_schema_extra={'type': 'string'}
        )

class RoomDeserializer:
    @classmethod
    def __get_pydantic_core_schema__(
        cls, _source_type: Any, _handler: Callable[[Any], core_schema.CoreSchema]
    ) -> core_schema.CoreSchema:
        def deserialize_room(id: str, info: core_schema.ValidationInfo) -> Any:
            if id is None:
                return None
            context = info.context
            if context and 'rooms' in context:
                return context['rooms'].get(id)
            return None
        
        return core_schema.json_schema(
            core_schema.with_info_plain_validator_function(deserialize_room),
            json_schema_extra={'type': 'string'}
        )

class ScoreValidator:
    @classmethod
    def __get_pydantic_core_schema__(
        cls, _source_type: Any, _handler: Callable[[Any], core_schema.CoreSchema]
    ) -> core_schema.CoreSchema:
        return core_schema.json_schema(
            core_schema.with_info_plain_validator_function(validate_score),
            json_schema_extra={'type': 'object'}
        )