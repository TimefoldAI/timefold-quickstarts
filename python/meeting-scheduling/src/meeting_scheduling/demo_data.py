from random import Random
from datetime import datetime, timedelta
from enum import Enum
from typing import List, Tuple, Any, Callable
from dataclasses import dataclass

from .domain import Person, TimeGrain, Room, Meeting, MeetingAssignment, MeetingSchedule

class DemoData(str, Enum):
    SMALL = "SMALL"
    MEDIUM = "MEDIUM"
    LARGE = "LARGE"

@dataclass(frozen=True, kw_only=True)
class CountDistribution:
    count: int
    weight: float

def counts(distributions: tuple[CountDistribution, ...]) -> tuple[int, ...]:
    return tuple(distribution.count for distribution in distributions)

def weights(distributions: tuple[CountDistribution, ...]) -> tuple[float, ...]:
    return tuple(distribution.weight for distribution in distributions)

def generate_demo_data() -> MeetingSchedule:
    """Generate demo data for the meeting scheduling problem."""
    rnd = Random(0)  # For reproducible results
    
    # People
    people = generate_people(20, rnd)
    
    # Time grains
    time_grains = generate_time_grains()
    
    # Rooms
    rooms = [
        Room(id="R1", name="Room 1", capacity=30),
        Room(id="R2", name="Room 2", capacity=20),
        Room(id="R3", name="Room 3", capacity=16)
    ]
    
    # Meetings
    meetings = generate_meetings(people, rnd)
    
    # Rebuild meetings with correct attendances
    all_required_attendances = [ra for meeting in meetings for ra in meeting.required_attendances]
    all_preferred_attendances = [pa for meeting in meetings for pa in meeting.preferred_attendances]
    new_meetings = []
    for m in meetings:
        new_meetings.append(
            type(m)(
                id=m.id,
                topic=m.topic,
                duration_in_grains=m.duration_in_grains,
                speakers=m.speakers,
                content=m.content,
                entire_group_meeting=m.entire_group_meeting,
                required_attendances=[a for a in all_required_attendances if str(a.meeting_id) == str(m.id)],
                preferred_attendances=[a for a in all_preferred_attendances if str(a.meeting_id) == str(m.id)],
            )
        )
    meetings = new_meetings
    
    # Meeting assignments
    meeting_assignments = generate_meeting_assignments(meetings)
    
    # Create schedule
    schedule = MeetingSchedule(
        people=people,
        time_grains=time_grains,
        rooms=rooms,
        meetings=meetings,
        meeting_assignments=meeting_assignments,
        required_attendances=[ra for meeting in meetings for ra in meeting.required_attendances],
        preferred_attendances=[pa for meeting in meetings for pa in meeting.preferred_attendances],
    )
    
    return schedule


def generate_people(count_people: int, rnd: Random) -> List[Person]:
    """Generate a list of people."""
    FIRST_NAMES = ["Amy", "Beth", "Carl", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy", "Jay",
                  "Jeri", "Hope", "Avis", "Lino", "Lyle", "Nick", "Dino", "Otha", "Gwen", "Jose", 
                  "Dena", "Jana", "Dave", "Russ", "Josh", "Dana", "Katy"]
    LAST_NAMES = ["Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith", "Watt", 
                 "Howe", "Lowe", "Wise", "Clay", "Carr", "Hood", "Long", "Horn", "Haas", "Meza"]
    
    def generate_name() -> str:
        first_name = rnd.choice(FIRST_NAMES)
        last_name = rnd.choice(LAST_NAMES)
        return f"{first_name} {last_name}"
    
    return [Person(id=str(i), full_name=generate_name()) for i in range(count_people)]


def generate_time_grains() -> List[TimeGrain]:
    """Generate time grains for the next 4 days starting from tomorrow."""
    time_grains = []
    current_date = datetime.now().date() + timedelta(days=1)
    count = 0
    
    for _ in range(4):  # 4 days
        current_time = datetime.combine(current_date, datetime.min.time()) + timedelta(hours=8)  # Start at 8:00
        end_time = datetime.combine(current_date, datetime.min.time()) + timedelta(hours=17, minutes=45)  # End at 17:45
        
        while current_time <= end_time:
            day_of_year = current_date.timetuple().tm_yday
            minutes_of_day = current_time.hour * 60 + current_time.minute
            
            time_grains.append(TimeGrain(
                id=str(count),
                grain_index=count,
                day_of_year=day_of_year,
                starting_minute_of_day=minutes_of_day
            ))
            
            count += 1
            current_time += timedelta(minutes=15)  # 15-minute increments
        
        current_date += timedelta(days=1)
    
    return time_grains


def generate_meetings(people: List[Person], rnd: Random) -> List[Meeting]:
    """Generate meetings with topics and attendees."""
    meeting_topics = [
        "Strategize B2B", "Fast track e-business", "Cross sell virtualization",
        "Profitize multitasking", "Transform one stop shop", "Engage braindumps",
        "Downsize data mining", "Ramp up policies", "On board synergies",
        "Reinvigorate user experience", "Strategize e-business", "Fast track virtualization",
        "Cross sell multitasking", "Profitize one stop shop", "Transform braindumps",
        "Engage data mining", "Downsize policies", "Ramp up synergies",
        "On board user experience", "Reinvigorate B2B", "Strategize virtualization",
        "Fast track multitasking", "Cross sell one stop shop", "Reinvigorate multitasking"
    ]
    
    meetings = []
    for i, topic in enumerate(meeting_topics):
        meeting = Meeting(id=str(i), topic=topic, duration_in_grains=0)
        meetings.append(meeting)
    
    # Set durations using CountDistribution and random.choices
    duration_distribution = (
        CountDistribution(count=8, weight=1),   # 33% with 8 time grains
        CountDistribution(count=12, weight=1),  # 33% with 12 time grains
        CountDistribution(count=16, weight=1)   # 33% with 16 time grains
    )

    for meeting in meetings:
        duration_time_grains, = rnd.choices(population=counts(duration_distribution),
                                           weights=weights(duration_distribution))
        meeting.duration_in_grains = duration_time_grains
    
    # Add required attendees using CountDistribution and random.choices
    required_attendees_distribution = (
        CountDistribution(count=2, weight=0.36),
        CountDistribution(count=3, weight=0.08),
        CountDistribution(count=4, weight=0.02),
        CountDistribution(count=5, weight=0.08),
        CountDistribution(count=6, weight=0.10),
        CountDistribution(count=7, weight=0.05),
        CountDistribution(count=8, weight=0.05),
        CountDistribution(count=10, weight=0.05)
    )

    def add_required_attendees(meeting: Meeting, count: int) -> None:
        meeting.required_attendances = rnd.sample(people, k=count)

    for meeting in meetings:
        count, = rnd.choices(population=counts(required_attendees_distribution),
                            weights=weights(required_attendees_distribution))
        add_required_attendees(meeting, count)
    
    # Add preferred attendees using CountDistribution and random.choices
    preferred_attendees_distribution = (
        CountDistribution(count=1, weight=0.06),
        CountDistribution(count=2, weight=0.20),
        CountDistribution(count=3, weight=0.18),
        CountDistribution(count=4, weight=0.06),
        CountDistribution(count=5, weight=0.04),
        CountDistribution(count=6, weight=0.04),
        CountDistribution(count=7, weight=0.04),
        CountDistribution(count=8, weight=0.04),
        CountDistribution(count=9, weight=0.08),
        CountDistribution(count=10, weight=0.04)
    )

    def add_preferred_attendees(meeting: Meeting, count: int) -> None:
        unused_people = list(set(people) - set(meeting.required_attendance))
        sorted_unused_people = sorted(unused_people, key=lambda person: person.id)
        random.sample(sorted_unused_people, count)
            
    for meeting in meetings:
        count, = rnd.choices(population=counts(preferred_attendees_distribution),
                            weights=weights(preferred_attendees_distribution))
        add_preferred_attendees(meeting, count)
    
    return meetings


def generate_meeting_assignments(meetings: List[Meeting]) -> List[MeetingAssignment]:
    """Generate meeting assignments for each meeting."""
    return [MeetingAssignment(id=str(i), meeting=meeting) for i, meeting in enumerate(meetings)]
