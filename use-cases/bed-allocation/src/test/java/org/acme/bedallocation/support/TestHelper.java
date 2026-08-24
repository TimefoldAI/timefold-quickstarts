package org.acme.bedallocation.support;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.acme.bedallocation.domain.Bed;
import org.acme.bedallocation.domain.Department;
import org.acme.bedallocation.domain.Gender;
import org.acme.bedallocation.domain.GenderLimitation;
import org.acme.bedallocation.domain.Room;
import org.acme.bedallocation.domain.Stay;
import org.acme.bedallocation.dto.BedDTO;
import org.acme.bedallocation.dto.DepartmentDTO;
import org.acme.bedallocation.dto.RoomDTO;
import org.acme.bedallocation.dto.StayDTO;

// To keep our production classes as simple as possible, we've added these methods to help construct the data needed for testing.
public final class TestHelper {

    private TestHelper() {
    }

    public static BedBuilder aBed(String id) {
        return new BedBuilder(id);
    }

    public static RoomBuilder aRoom(String id) {
        return new RoomBuilder(id);
    }

    public static DepartmentBuilder aDepartment(String id) {
        return new DepartmentBuilder(id);
    }

    public static StayBuilder aStay(String id, Bed bed) {
        return new StayBuilder(id, bed);
    }

    public static BedDTOBuilder aBedDTO(String id) {
        return new BedDTOBuilder(id);
    }

    public static RoomDTOBuilder aRoomDTO(String id) {
        return new RoomDTOBuilder(id);
    }

    public static DepartmentDTOBuilder aDepartmentDTO(String id) {
        return new DepartmentDTOBuilder(id);
    }

    public static StayDTOBuilder aStayDTO(String id) {
        return new StayDTOBuilder(id);
    }

    public static final class BedBuilder {

        private final String id;
        private Room room;
        private int indexInRoom;

        private BedBuilder(String id) {
            this.id = id;
        }

        public BedBuilder room(Room room) {
            this.room = room;
            return this;
        }

        public BedBuilder indexInRoom(int indexInRoom) {
            this.indexInRoom = indexInRoom;
            return this;
        }

        public Bed build() {
            Bed bed = new Bed(id, room, indexInRoom);
            if (room != null) {
                room.beds().add(bed);
            }
            return bed;
        }
    }

    public static final class RoomBuilder {

        private final String id;
        private String name;
        private Department department;
        private int capacity;
        private GenderLimitation genderLimitation = GenderLimitation.ANY_GENDER;
        private Set<String> equipments = Set.of();
        private final List<Bed> beds = new ArrayList<>();

        private RoomBuilder(String id) {
            this.id = id;
            this.name = id;
        }

        public RoomBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoomBuilder department(Department department) {
            this.department = department;
            return this;
        }

        public RoomBuilder capacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public RoomBuilder genderLimitation(GenderLimitation genderLimitation) {
            this.genderLimitation = genderLimitation;
            return this;
        }

        public RoomBuilder equipments(Set<String> equipments) {
            this.equipments = equipments;
            return this;
        }

        public Room build() {
            Room room = new Room(id, name, department, capacity, genderLimitation, equipments, beds);
            if (department != null) {
                department.rooms().add(room);
            }
            return room;
        }
    }

    public static final class DepartmentBuilder {

        private final String id;
        private String name;
        private Integer minimumAge;
        private Integer maximumAge;
        private Map<String, Integer> specialtyToPriority = Map.of();
        private final List<Room> rooms = new ArrayList<>();

        private DepartmentBuilder(String id) {
            this.id = id;
            this.name = id;
        }

        public DepartmentBuilder name(String name) {
            this.name = name;
            return this;
        }

        public DepartmentBuilder minimumAge(Integer minimumAge) {
            this.minimumAge = minimumAge;
            return this;
        }

        public DepartmentBuilder maximumAge(Integer maximumAge) {
            this.maximumAge = maximumAge;
            return this;
        }

        public DepartmentBuilder specialtyToPriority(Map<String, Integer> specialtyToPriority) {
            this.specialtyToPriority = specialtyToPriority;
            return this;
        }

        public Department build() {
            return new Department(id, name, minimumAge, maximumAge, specialtyToPriority, rooms);
        }
    }

    public static final class StayBuilder {

        private static final LocalDate ZERO_NIGHT = LocalDate.of(2021, 2, 1);
        private static final LocalDate FIVE_NIGHT = ZERO_NIGHT.plusDays(5);
        private static final String DEFAULT_SPECIALTY = "default";

        private final String id;
        private final Bed bed;

        private LocalDate arrivalDate = ZERO_NIGHT;
        private LocalDate departureDate = FIVE_NIGHT;
        private String specialty = DEFAULT_SPECIALTY;
        private String patientName;
        private Gender patientGender;
        private int patientAge;
        private Integer patientPreferredMaximumRoomCapacity;
        private List<String> patientRequiredEquipments;
        private List<String> patientPreferredEquipments;

        private StayBuilder(String id, Bed bed) {
            this.id = id;
            this.bed = bed;
        }

        public StayBuilder specialty(String specialty) {
            this.specialty = specialty;
            return this;
        }

        public StayBuilder patientName(String patientName) {
            this.patientName = patientName;
            return this;
        }

        public StayBuilder patientGender(Gender patientGender) {
            this.patientGender = patientGender;
            return this;
        }

        public StayBuilder patientAge(int patientAge) {
            this.patientAge = patientAge;
            return this;
        }

        public StayBuilder patientPreferredMaximumRoomCapacity(Integer patientPreferredMaximumRoomCapacity) {
            this.patientPreferredMaximumRoomCapacity = patientPreferredMaximumRoomCapacity;
            return this;
        }

        public StayBuilder patientRequiredEquipments(List<String> patientRequiredEquipments) {
            this.patientRequiredEquipments = patientRequiredEquipments;
            return this;
        }

        public StayBuilder patientPreferredEquipments(List<String> patientPreferredEquipments) {
            this.patientPreferredEquipments = patientPreferredEquipments;
            return this;
        }

        public Stay build() {
            return new Stay(id, patientName, patientGender, patientAge, patientPreferredMaximumRoomCapacity,
                    patientRequiredEquipments, patientPreferredEquipments, arrivalDate, departureDate, specialty, bed);
        }
    }

    public static final class BedDTOBuilder {

        private final String id;
        private int indexInRoom;

        private BedDTOBuilder(String id) {
            this.id = id;
        }

        public BedDTOBuilder indexInRoom(int indexInRoom) {
            this.indexInRoom = indexInRoom;
            return this;
        }

        public BedDTO build() {
            return new BedDTO(id, indexInRoom);
        }
    }

    public static final class RoomDTOBuilder {

        private final String id;
        private String name;
        private int capacity;
        private String genderLimitation = "ANY_GENDER";
        private Set<String> equipments = Set.of();
        private List<BedDTO> beds = List.of();

        private RoomDTOBuilder(String id) {
            this.id = id;
            this.name = "Room " + id;
        }

        public RoomDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoomDTOBuilder capacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public RoomDTOBuilder genderLimitation(String genderLimitation) {
            this.genderLimitation = genderLimitation;
            return this;
        }

        public RoomDTOBuilder equipments(Set<String> equipments) {
            this.equipments = equipments;
            return this;
        }

        public RoomDTOBuilder beds(List<BedDTO> beds) {
            this.beds = beds;
            this.capacity = beds.size();
            return this;
        }

        public RoomDTO build() {
            return new RoomDTO(id, name, capacity, genderLimitation, equipments, beds);
        }
    }

    public static final class DepartmentDTOBuilder {

        private final String id;
        private String name;
        private Integer minimumAge;
        private Integer maximumAge;
        private Map<String, Integer> specialtyToPriority = Map.of();
        private List<RoomDTO> rooms = List.of();

        private DepartmentDTOBuilder(String id) {
            this.id = id;
            this.name = "Department " + id;
        }

        public DepartmentDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public DepartmentDTOBuilder minimumAge(Integer minimumAge) {
            this.minimumAge = minimumAge;
            return this;
        }

        public DepartmentDTOBuilder maximumAge(Integer maximumAge) {
            this.maximumAge = maximumAge;
            return this;
        }

        public DepartmentDTOBuilder specialtyToPriority(Map<String, Integer> specialtyToPriority) {
            this.specialtyToPriority = specialtyToPriority;
            return this;
        }

        public DepartmentDTOBuilder rooms(List<RoomDTO> rooms) {
            this.rooms = rooms;
            return this;
        }

        public DepartmentDTO build() {
            return new DepartmentDTO(id, name, minimumAge, maximumAge, specialtyToPriority, rooms);
        }
    }

    public static final class StayDTOBuilder {

        private final String id;
        private String patientName;
        private String patientGender = "MALE";
        private int patientAge = 30;
        private Integer patientPreferredMaximumRoomCapacity;
        private List<String> patientRequiredEquipments = List.of();
        private List<String> patientPreferredEquipments = List.of();
        private String arrivalDate = "2024-01-01";
        private String departureDate = "2024-01-03";
        private String specialty;
        private String bedId;

        private StayDTOBuilder(String id) {
            this.id = id;
            this.patientName = "Patient " + id;
        }

        public StayDTOBuilder patientName(String patientName) {
            this.patientName = patientName;
            return this;
        }

        public StayDTOBuilder patientGender(String patientGender) {
            this.patientGender = patientGender;
            return this;
        }

        public StayDTOBuilder patientAge(int patientAge) {
            this.patientAge = patientAge;
            return this;
        }

        public StayDTOBuilder patientPreferredMaximumRoomCapacity(Integer patientPreferredMaximumRoomCapacity) {
            this.patientPreferredMaximumRoomCapacity = patientPreferredMaximumRoomCapacity;
            return this;
        }

        public StayDTOBuilder patientRequiredEquipments(List<String> patientRequiredEquipments) {
            this.patientRequiredEquipments = patientRequiredEquipments;
            return this;
        }

        public StayDTOBuilder patientPreferredEquipments(List<String> patientPreferredEquipments) {
            this.patientPreferredEquipments = patientPreferredEquipments;
            return this;
        }

        public StayDTOBuilder arrivalDate(String arrivalDate) {
            this.arrivalDate = arrivalDate;
            return this;
        }

        public StayDTOBuilder departureDate(String departureDate) {
            this.departureDate = departureDate;
            return this;
        }

        public StayDTOBuilder specialty(String specialty) {
            this.specialty = specialty;
            return this;
        }

        public StayDTOBuilder bedId(String bedId) {
            this.bedId = bedId;
            return this;
        }

        public StayDTO build() {
            return new StayDTO(id, patientName, patientGender, patientAge, patientPreferredMaximumRoomCapacity,
                    patientRequiredEquipments, patientPreferredEquipments, arrivalDate, departureDate, specialty, bedId);
        }
    }
}
