from random import Random
from datetime import datetime, timedelta
from enum import Enum
from typing import List, Tuple, Any, Callable
from dataclasses import dataclass

from .domain import Person, TimeGrain, Room, Meeting, MeetingAssignment, MeetingSchedule, RequiredAttendance, PreferredAttendance

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
                content=m.content or "",
                entire_group_meeting=m.entire_group_meeting,
                required_attendances=[a for a in all_required_attendances if a.meeting_id == m.id],
                preferred_attendances=[a for a in all_preferred_attendances if a.meeting_id == m.id],
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
    
    while current_date < datetime.now().date() + timedelta(days=5):  # Match Java: from +1 to +5 (4 days)
        current_time = datetime.combine(current_date, datetime.min.time()) + timedelta(hours=8)  # Start at 8:00
        end_time = datetime.combine(current_date, datetime.min.time()) + timedelta(hours=17, minutes=45)  # End at 17:45
        
        while current_time <= end_time:
            day_of_year = current_date.timetuple().tm_yday
            minutes_of_day = current_time.hour * 60 + current_time.minute
            
            count += 1  # Pre-increment like Java ++count
            time_grains.append(TimeGrain(
                id=str(count),
                grain_index=count,
                day_of_year=day_of_year,
                starting_minute_of_day=minutes_of_day
            ))
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
    
    # Add required attendees using CountDistribution - slightly reduced to make more feasible
    required_attendees_distribution = (
        CountDistribution(count=2, weight=0.45),  # More 2-person meetings
        CountDistribution(count=3, weight=0.15),  # More 3-person meetings
        CountDistribution(count=4, weight=0.10),  # Increased 4-person
        CountDistribution(count=5, weight=0.10),  # Slightly more 5-person
        CountDistribution(count=6, weight=0.08),  # Reduced larger meetings
        CountDistribution(count=7, weight=0.05),
        CountDistribution(count=8, weight=0.04),
        CountDistribution(count=10, weight=0.03)  # Reduced 10-person meetings
    )

    def add_required_attendees(meeting: Meeting, count: int) -> None:
        # Use random.sample to avoid duplicates
        selected_people = rnd.sample(people, count)
        for person in selected_people:
            meeting.required_attendances.append(
                RequiredAttendance(
                    id=f"{meeting.id}-{len(meeting.required_attendances) + 1}",
                    person=person,
                    meeting_id=meeting.id
                )
            )

    for meeting in meetings:
        count, = rnd.choices(population=counts(required_attendees_distribution),
                            weights=weights(required_attendees_distribution))
        add_required_attendees(meeting, count)
    
    # Add preferred attendees using CountDistribution - reduced to make more feasible
    preferred_attendees_distribution = (
        CountDistribution(count=1, weight=0.25),  # More 1-person preferred
        CountDistribution(count=2, weight=0.30),  # More 2-person preferred
        CountDistribution(count=3, weight=0.20),  # More 3-person preferred
        CountDistribution(count=4, weight=0.10),  # Increased 4-person
        CountDistribution(count=5, weight=0.06),  # Slightly more 5-person
        CountDistribution(count=6, weight=0.04),  # Reduced larger groups
        CountDistribution(count=7, weight=0.02),  # Reduced
        CountDistribution(count=8, weight=0.02),  # Reduced
        CountDistribution(count=9, weight=0.01),  # Minimal large groups
        CountDistribution(count=10, weight=0.00) # Eliminated 10-person preferred
    )

    def add_preferred_attendees(meeting: Meeting, count: int) -> None:
        # Get people not already required for this meeting
        required_people_ids = {ra.person.id for ra in meeting.required_attendances}
        available_people = [person for person in people if person.id not in required_people_ids]
        
        # Use random.sample to avoid duplicates, but only if we have enough people
        if len(available_people) >= count:
            selected_people = rnd.sample(available_people, count)
            for person in selected_people:
                meeting.preferred_attendances.append(
                    PreferredAttendance(
                        id=f"{meeting.id}-{len(meeting.required_attendances) + len(meeting.preferred_attendances) + 1}",
                        person=person,
                        meeting_id=meeting.id
                    )
                )
            
    for meeting in meetings:
        count, = rnd.choices(population=counts(preferred_attendees_distribution),
                            weights=weights(preferred_attendees_distribution))
        add_preferred_attendees(meeting, count)
    
    return meetings


def generate_meeting_assignments(meetings: List[Meeting]) -> List[MeetingAssignment]:
    """Generate meeting assignments for each meeting."""
    return [MeetingAssignment(id=str(i), meeting=meeting) for i, meeting in enumerate(meetings)]
