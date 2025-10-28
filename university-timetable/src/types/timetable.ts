export interface Timeslot {
  id: string;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
}

export interface Room {
  id: string;
  name: string;
}

export interface StudentGroup {
  id: string;
  name: string;
}

export interface Teacher {
  id: string;
  name: string;
}

export interface Lesson {
  id: string;
  subject: string;
  teacher: Teacher;
  studentGroup: StudentGroup;
  timeslot?: Timeslot;
  room?: Room;
}

export interface Timetable {
  name: string;
  timeslots: Timeslot[];
  rooms: Room[];
  studentGroups: StudentGroup[];
  teachers: Teacher[];
  lessons: Lesson[];
  score?: {
    hardScore: number;
    softScore: number;
  };
  solverStatus?: string;
}