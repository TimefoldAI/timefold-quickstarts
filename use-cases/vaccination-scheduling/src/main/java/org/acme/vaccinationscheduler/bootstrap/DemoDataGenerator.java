package org.acme.vaccinationscheduler.bootstrap;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.YEARS;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.acme.vaccinationscheduler.domain.Appointment;
import org.acme.vaccinationscheduler.domain.Location;
import org.acme.vaccinationscheduler.domain.Person;
import org.acme.vaccinationscheduler.domain.VaccinationCenter;
import org.acme.vaccinationscheduler.domain.VaccinationSchedule;
import org.acme.vaccinationscheduler.domain.VaccineType;
import org.acme.vaccinationscheduler.persistence.VaccinationScheduleRepository;
import org.acme.vaccinationscheduler.solver.geo.DistanceCalculator;
import org.acme.vaccinationscheduler.solver.geo.EuclideanDistanceCalculator;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class DemoDataGenerator {

    public static final String[] PERSON_FIRST_NAMES = {
            "Ann", "Beth", "Carl", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy", "Jay",
            "Kurt", "Luke", "Mia", "Noa", "Otto", "Paul", "Quin", "Ray", "Sue", "Taj",
            "Uma", "Vix", "Wade", "Xiu" , "Yuna", "Zara"};
    public static final String[] VACCINATION_CENTER_NAMES = {
            "Downtown", "Uptown", "Market", "Park", "River", "Square", "Bay", "Hill", "Station", "Hospital",
            "Tower", "Wall", "Plaza", "Grove", "Boulevard", "Avenue", "Fort", "Beach", "Harbor", "Airport",
            "Garden", "Forest", "Springs", "Ville" , "Stad"};
    public static double massVaccinationCenterRatio = (1.0 / 3.0);

    public static final LocalDate MINIMUM_BIRTH_DATE = LocalDate.of(1930, 1, 1);
    public static final int BIRTH_DATE_RANGE_LENGTH = (int) DAYS.between(MINIMUM_BIRTH_DATE, LocalDate.of(2000, 1, 1));

    protected static final Logger logger = LoggerFactory.getLogger(DemoDataGenerator.class);

    @ConfigProperty(name = "demo-data.vaccination-center-count", defaultValue = "3")
    int vaccinationCenterCount;
    @ConfigProperty(name = "demo-data.total-booth-count", defaultValue = "5")
    int totalBoothCount;

    // Default latitude and longitude window: city of Atlanta, US.
    @ConfigProperty(name = "demo-data.map.minimum-latitude", defaultValue = "33.40")
    double minimumLatitude;
    @ConfigProperty(name = "demo-data.map.maximum-latitude", defaultValue = "34.10")
    double maximumLatitude;
    @ConfigProperty(name = "demo-data.map.minimum-longitude", defaultValue = "-84.90")
    double minimumLongitude;
    @ConfigProperty(name = "demo-data.map.maximum-longitude", defaultValue = "-83.90")
    double maximumLongitude;

    public DemoDataGenerator() {
    }

    public DemoDataGenerator(double minimumLatitude, double maximumLatitude, double minimumLongitude, double maximumLongitude) {
        this.minimumLatitude = minimumLatitude;
        this.maximumLatitude = maximumLatitude;
        this.minimumLongitude = minimumLongitude;
        this.maximumLongitude = maximumLongitude;
    }

    @Inject
    VaccinationScheduleRepository vaccinationScheduleRepository;

    public void startup(@Observes StartupEvent startupEvent) {
        vaccinationScheduleRepository.save(generate(vaccinationCenterCount, totalBoothCount, 0.0));
    }

    public VaccinationSchedule generate(int vaccinationCenterCount, int totalBoothCount, double pinnedAppointmentRatio) {
        Random random = new Random(17);
        LocalDate windowStartDate = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        int windowDaysLength = 5;
        LocalTime dayStartTime = LocalTime.of(9, 0);
        int injectionsPerBoothPerTimeslot = 4;
        int timeslotsPerBoothPerDay = (int) HOURS.between(dayStartTime, LocalTime.of(17, 0));

        List<VaccineType> vaccineTypeList = generateVaccineTypeList();
        List<VaccinationCenter> vaccinationCenterList = generateVaccinationCenterList(
                random, vaccinationCenterCount);
        List<Appointment> appointmentList = generateAppointmentList(random,
                windowStartDate, windowDaysLength, dayStartTime, totalBoothCount,
                injectionsPerBoothPerTimeslot, timeslotsPerBoothPerDay,
                vaccineTypeList, vaccinationCenterList);
        List<Person> personList = generatePersonList(random,
                windowStartDate, windowDaysLength, pinnedAppointmentRatio,
                vaccineTypeList, vaccinationCenterList, appointmentList);

        logger.info("Generated dataset with {} appointments and {} persons.",
                appointmentList.size(),
                personList.size());
        return new VaccinationSchedule(vaccineTypeList, vaccinationCenterList, appointmentList, personList);
    }

    private List<VaccineType> generateVaccineTypeList() {
        return Arrays.asList(
                new VaccineType("Pfizer"),
                new VaccineType("Moderna"),
                new VaccineType("AstraZeneca")
        );
    }

    private List<VaccinationCenter> generateVaccinationCenterList(Random random, int vaccinationCenterCount) {
        List<VaccinationCenter> vaccinationCenterList = new ArrayList<>(vaccinationCenterCount);
        long vaccinationCenterId = 0L;
        for (int i = 0; i < vaccinationCenterCount; i++) {
            String name = VACCINATION_CENTER_NAMES[i % VACCINATION_CENTER_NAMES.length]
                    + (i < VACCINATION_CENTER_NAMES.length ? "" : " " + (i / VACCINATION_CENTER_NAMES.length + 1));
            VaccinationCenter vaccinationCenter = new VaccinationCenter(Long.toString(vaccinationCenterId++),
                    name, pickLocation(random));
            vaccinationCenterList.add(vaccinationCenter);
        }
        return vaccinationCenterList;
    }

    private List<Appointment> generateAppointmentList(Random random,
            LocalDate windowStartDate, int windowDaysLength, LocalTime dayStartTime, int totalBoothCount, int injectionsPerBoothPerTimeslot, int timeslotsPerBoothPerDay, List<VaccineType> vaccineTypeList, List<VaccinationCenter> vaccinationCenterList) {
        int vaccinationCenterCount = vaccinationCenterList.size();
        int massCount = (int) Math.round(massVaccinationCenterRatio * vaccinationCenterCount);
        int massExtraBoothCount = totalBoothCount - vaccinationCenterCount;
        List<Appointment> appointmentList = new ArrayList<>(windowDaysLength * timeslotsPerBoothPerDay * totalBoothCount * injectionsPerBoothPerTimeslot);
        for (int i = 0; i < vaccinationCenterCount; i++) {
            VaccinationCenter vaccinationCenter = vaccinationCenterList.get(i);
            int boothCount;
            if (i < massCount) {
                // The + i distributes the remainder, for example if massExtraBoothCount=8 and massCount=3
                boothCount = 1 + (massExtraBoothCount + i) / massCount;
            } else {
                boothCount = 1;
            }
            for (long boothId = 0; boothId < boothCount; boothId++) {
                for (int dayIndex = 0; dayIndex < windowDaysLength; dayIndex++) {
                    VaccineType vaccineType = pickVaccineType(random, null, vaccineTypeList);
                    LocalDate date = windowStartDate.plusDays(dayIndex);
                    for (int timeIndex = 0; timeIndex < timeslotsPerBoothPerDay; timeIndex++) {
                        LocalTime time = dayStartTime.plusHours(timeIndex);
                        for (int j = 0; j < injectionsPerBoothPerTimeslot; j++) {
                            LocalDateTime dateTime = LocalDateTime.of(date, time.plusMinutes(j * (60L / injectionsPerBoothPerTimeslot)));
                            Appointment appointment = new Appointment(
                                    vaccinationCenter, Long.toString(boothId), dateTime, vaccineType);
                            appointmentList.add(appointment);
                        }
                    }
                }
            }
        }
        return appointmentList;
    }

    private List<Person> generatePersonList(Random random,
            LocalDate windowStartDate, int windowDaysLength, double pinnedAppointmentRatio,
            List<VaccineType> vaccineTypeList, List<VaccinationCenter> vaccinationCenterList,
            List<Appointment> appointmentList) {
        int personListSize = appointmentList.size() * 6 / 5; // 20% too many
        List<Person> personList = new ArrayList<>(personListSize);
        long personId = 0L;
        DistanceCalculator distanceCalculator = new EuclideanDistanceCalculator();
        List<Appointment> shuffledAppointmentList;
        int pinnedAppointmentSize;
        if (pinnedAppointmentRatio <= 0.0) {
            shuffledAppointmentList = null;
            pinnedAppointmentSize = 0;
        } else {
            LocalDate windowEndDate = windowStartDate.plusDays(windowDaysLength - 1);
            shuffledAppointmentList = appointmentList.stream()
                    .filter(appointment -> appointment.getDateTime().toLocalDate().isBefore(windowEndDate))
                    .collect(Collectors.toList());
            Collections.shuffle(shuffledAppointmentList, random);
            pinnedAppointmentSize = Math.min((int) (appointmentList.size() * pinnedAppointmentRatio),
                    shuffledAppointmentList.size());
        }
        for (int i = 0; i < personListSize; i++) {
            int lastNameI = i / PERSON_FIRST_NAMES.length;
            String name = PERSON_FIRST_NAMES[i % PERSON_FIRST_NAMES.length]
                    + " " + (lastNameI < 26 ? ((char) ('A' + lastNameI)) + "." : lastNameI - 25);
            Location location = pickLocation(random);
            LocalDate birthdate = MINIMUM_BIRTH_DATE.plusDays(random.nextInt(BIRTH_DATE_RANGE_LENGTH));
            int age = (int) YEARS.between(birthdate, windowStartDate);
            boolean healthcareWorker = random.nextDouble() < 0.05;
            if (healthcareWorker) {
                name = "Dr. " + name;
            }
            long priorityRating = age + (healthcareWorker ? 1_000 : 0);
            boolean firstDoseInjected = random.nextDouble() < 0.25;
            Person person;
            if (!firstDoseInjected) {
                person = new Person(Long.toString(personId++), name, location, birthdate, priorityRating);
            } else {
                VaccineType firstDoseVaccineType = pickVaccineType(random, age, vaccineTypeList);
                VaccinationCenter preferredVaccinationCenter = (random.nextDouble() > 0.10) ? null
                        : vaccinationCenterList.stream()
                        .sorted(Comparator.comparing(vc -> distanceCalculator.calculateDistance(location, vc.getLocation())))
                        .skip(1).findFirst().orElse(null);
                LocalDate idealDate = windowStartDate.plusDays(random.nextInt(windowDaysLength));
                LocalDate readyDate = idealDate.minusDays(2);
                LocalDate dueDate = idealDate.plusDays(windowDaysLength - 2);
                person = new Person(Long.toString(personId++), name, location, birthdate, priorityRating,
                        2, firstDoseVaccineType, null, null, preferredVaccinationCenter, readyDate, idealDate, dueDate);

            }
            if (i < pinnedAppointmentSize) {
                person.setAppointment(shuffledAppointmentList.get(i));
                person.setPinned(true);
            }
            personList.add(person);
        }
        return personList;
    }

    public Location pickLocation(Random random) {
        double latitude = minimumLatitude + (random.nextDouble() * (maximumLatitude - minimumLatitude));
        double longitude = minimumLongitude + (random.nextDouble() * (maximumLongitude - minimumLongitude));
        return new Location(latitude, longitude);
    }

    public VaccineType pickVaccineType(Random random, Integer age, List<VaccineType> vaccineTypeList) {
        List<VaccineType> suitableVaccineTypeList;
        if (age == null) {
            suitableVaccineTypeList = vaccineTypeList;
        } else {
            suitableVaccineTypeList = vaccineTypeList.stream()
                    .filter(vaccineType -> {
                        boolean minimumAgeOk = vaccineType.getMinimumAge() == null || age >= vaccineType.getMinimumAge();
                        boolean maximumAgeOk = vaccineType.getMaximumAge() == null || age <= vaccineType.getMaximumAge();
                        return minimumAgeOk && maximumAgeOk;
                    })
                    .collect(Collectors.toList());
        }
        return suitableVaccineTypeList.get(random.nextInt(suitableVaccineTypeList.size()));
    }

}
