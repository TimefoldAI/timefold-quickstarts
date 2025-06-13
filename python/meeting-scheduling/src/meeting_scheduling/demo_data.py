import random
from datetime import datetime, timedelta
from enum import Enum
from typing import List, Tuple, Any, Callable

from .domain import Person, TimeGrain, Room, Meeting, MeetingAssignment, MeetingSchedule

class DemoData(str, Enum):
    SMALL = "SMALL"
    MEDIUM = "MEDIUM"
    LARGE = "LARGE"


def generate_demo_data() -> MeetingSchedule:
    """Generate demo data for the meeting scheduling problem."""
    random.seed(0)  # For reproducible results
    
    # People
    people = generate_people(20)
    
    # Time grains
    time_grains = generate_time_grains()
    
    # Rooms
    rooms = [
        Room(id="R1", name="Room 1", capacity=30),
        Room(id="R2", name="Room 2", capacity=20),
        Room(id="R3", name="Room 3", capacity=16)
    ]
    
    # Meetings
    meetings = generate_meetings(people)
    
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


def generate_people(count_people: int) -> List[Person]:
    """Generate a list of people."""
    FIRST_NAMES = ["Amy", "Beth", "Carl", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy", "Jay",
                  "Jeri", "Hope", "Avis", "Lino", "Lyle", "Nick", "Dino", "Otha", "Gwen", "Jose", 
                  "Dena", "Jana", "Dave", "Russ", "Josh", "Dana", "Katy"]
    LAST_NAMES = ["Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith", "Watt", 
                 "Howe", "Lowe", "Wise", "Clay", "Carr", "Hood", "Long", "Horn", "Haas", "Meza"]
    
    def generate_name() -> str:
        first_name = random.choice(FIRST_NAMES)
        last_name = random.choice(LAST_NAMES)
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


def generate_meetings(people: List[Person]) -> List[Meeting]:
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
    
    # Set durations
    duration_distribution = [
        (0.33, 8),   # 33% with 8 time grains
        (0.33, 12),  # 33% with 12 time grains
        (0.33, 16)   # 33% with 16 time grains
    ]
    
    apply_random_value(
        [(int(p * len(meetings)), d) for p, d in duration_distribution],
        meetings,
        lambda m: m.duration_in_grains == 0,
        lambda m, d: setattr(m, 'duration_in_grains', d)
    )
    
    # Ensure no empty durations
    for meeting in meetings:
        if meeting.duration_in_grains == 0:
            meeting.duration_in_grains = 8
    
    # Add required attendees
    required_attendees_distribution = [
        (0.36, 2),  # 36% with 2 attendees
        (0.08, 3),  # 8% with 3 attendees
        (0.02, 4),  # 2% with 4 attendees
        (0.08, 5),  # 8% with 5 attendees
        (0.10, 6),  # 10% with 6 attendees
        (0.05, 7),  # 5% with 7 attendees
        (0.05, 8),  # 5% with 8 attendees
        (0.05, 10)  # 5% with 10 attendees
    ]
    
    def add_required_attendees(meeting: Meeting, count: int) -> None:
        while len(meeting.required_attendances) < count:
            person = random.choice(people)
            try:
                meeting.add_required_attendant(person)
            except ValueError:
                # Person already assigned, try another one
                pass
    
    apply_random_value(
        [(int(p * len(meetings)), c) for p, c in required_attendees_distribution],
        meetings,
        lambda m: len(m.required_attendances) == 0,
        add_required_attendees
    )
    
    # Ensure all meetings have at least some required attendees
    for meeting in meetings:
        if len(meeting.required_attendances) == 0:
            add_required_attendees(meeting, 2)
    
    # Add preferred attendees
    preferred_attendees_distribution = [
        (0.06, 1),   # 6% with 1 attendee
        (0.20, 2),   # 20% with 2 attendees
        (0.18, 3),   # 18% with 3 attendees
        (0.06, 4),   # 6% with 4 attendees
        (0.04, 5),   # 4% with 5 attendees
        (0.04, 6),   # 4% with 6 attendees
        (0.04, 7),   # 4% with 7 attendees
        (0.04, 8),   # 4% with 8 attendees
        (0.08, 9),   # 8% with 9 attendees
        (0.04, 10)   # 4% with 10 attendees
    ]
    
    def add_preferred_attendees(meeting: Meeting, count: int) -> None:
        while len(meeting.preferred_attendances) < count:
            person = random.choice(people)
            try:
                # Check if person is already a required attendee
                if not any(ra.person.id == person.id for ra in meeting.required_attendances):
                    meeting.add_preferred_attendant(person)
            except ValueError:
                # Person already assigned, try another one
                pass
    
    apply_random_value(
        [(int(p * len(meetings)), c) for p, c in preferred_attendees_distribution],
        meetings,
        lambda m: len(m.preferred_attendances) == 0,
        add_preferred_attendees
    )
    
    return meetings


def generate_meeting_assignments(meetings: List[Meeting]) -> List[MeetingAssignment]:
    """Generate meeting assignments for each meeting."""
    return [MeetingAssignment(id=str(i), meeting=meeting) for i, meeting in enumerate(meetings)]


def apply_random_value(distribution: List[Tuple[int, Any]], 
                      items: List[Any], 
                      filter_func: Callable[[Any], bool], 
                      apply_func: Callable[[Any, Any], None]) -> None:
    """Apply random values to items based on a distribution."""
    for count, value in distribution:
        eligible_items = [item for item in items if filter_func(item)]
        if not eligible_items:
            break
            
        for _ in range(min(count, len(eligible_items))):
            if not eligible_items:
                break
            item = random.choice(eligible_items)
            apply_func(item, value)
            eligible_items.remove(item)