package com.tenniscourts.schedules;

import com.tenniscourts.exceptions.AlreadyExistsEntityException;
import com.tenniscourts.reservations.Reservation;
import com.tenniscourts.reservations.ReservationRepository;
import com.tenniscourts.reservations.ReservationService;
import com.tenniscourts.tenniscourts.TennisCourt;
import com.tenniscourts.tenniscourts.TennisCourtRepository;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.List;

import static com.tenniscourts.reservations.ReservationStatus.CANCELLED;
import static com.tenniscourts.reservations.ReservationStatus.READY_TO_PLAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = ReservationService.class)
public class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private TennisCourtRepository tennisCourtRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    public void findScheduleTest() {
        LocalDateTime tomorrowAtThisHour = LocalDateTime.now().plusDays(1);

        TennisCourt tennisCourt = TennisCourt.builder().name("Tennis Court").build();
        tennisCourt.setId(3L);
        Schedule schedule = Schedule.builder()
                .tennisCourt(tennisCourt)
                .startDateTime(tomorrowAtThisHour)
                .endDateTime(tomorrowAtThisHour.plusHours(1L)).build();
        Long scheduleId = 1L;
        schedule.setId(scheduleId);
        when(scheduleRepository.getOne(scheduleId)).thenReturn(schedule);

        Schedule actualSchedule = scheduleService.findSchedule(scheduleId);

        assertEquals(1L, actualSchedule.getId());
        assertEquals(3L, actualSchedule.getTennisCourt().getId());
        assertEquals(tomorrowAtThisHour, actualSchedule.getStartDateTime());
        assertEquals(tomorrowAtThisHour.plusHours(1L), actualSchedule.getEndDateTime());
    }

    @Test
    public void findSchedulesByDatesTest() {
        LocalDateTime tomorrowAtThisHour = LocalDateTime.now().plusDays(1);

        LocalDateTime searchRangeStart = tomorrowAtThisHour.minusDays(2);
        LocalDateTime searchRangeEnd = tomorrowAtThisHour.plusDays(2);

        TennisCourt tennisCourt1 = TennisCourt.builder().name("Tennis Court 1").build();
        tennisCourt1.setId(2L);
        Schedule schedule1 = Schedule.builder()
                .tennisCourt(tennisCourt1)
                .startDateTime(tomorrowAtThisHour)
                .endDateTime(tomorrowAtThisHour.plusHours(1L)).build();
        Long scheduleId = 1L;
        schedule1.setId(scheduleId);

        TennisCourt tennisCourt2 = TennisCourt.builder().name("Tennis Court 1").build();
        tennisCourt2.setId(3L);
        Schedule schedule2 = Schedule.builder()
                .tennisCourt(tennisCourt2)
                .startDateTime(tomorrowAtThisHour)
                .endDateTime(tomorrowAtThisHour.plusHours(1L)).build();
        Long scheduleId2 = 1L;
        schedule2.setId(scheduleId2);
        when(scheduleRepository.findAllByStartDateTimeIsGreaterThanEqualAndEndDateTimeIsLessThanEqual(searchRangeStart, searchRangeEnd))
                .thenReturn(List.of(schedule1, schedule2));


        List<Schedule> actualSchedules = scheduleService.findSchedulesByDates(searchRangeStart, searchRangeEnd);


        Schedule actualSchedule1 = actualSchedules.get(0);
        assertEquals(1L, actualSchedule1.getId());
        assertEquals(2L, actualSchedule1.getTennisCourt().getId());
        assertEquals(tomorrowAtThisHour, actualSchedule1.getStartDateTime());
        assertEquals(tomorrowAtThisHour.plusHours(1L), actualSchedule1.getEndDateTime());

        Schedule actualSchedule2 = actualSchedules.get(1);
        assertEquals(1L, actualSchedule2.getId());
        assertEquals(3L, actualSchedule2.getTennisCourt().getId());
        assertEquals(tomorrowAtThisHour, actualSchedule2.getStartDateTime());
        assertEquals(tomorrowAtThisHour.plusHours(1L), actualSchedule2.getEndDateTime());
    }

    @Test
    public void findSchedulesByTennisCourtIdTest() {
        Long tennisCourtId = 1L;

        LocalDateTime tomorrowAtThisHour = LocalDateTime.now().plusDays(1);

        TennisCourt tennisCourt1 = TennisCourt.builder().name("Tennis Court 1").build();
        tennisCourt1.setId(1L);

        Schedule schedule = Schedule.builder()
                .tennisCourt(tennisCourt1)
                .startDateTime(tomorrowAtThisHour)
                .endDateTime(tomorrowAtThisHour.plusHours(1L)).build();
        Long scheduleId = 1L;
        schedule.setId(scheduleId);

        LocalDateTime nextScheduleTomorrow = tomorrowAtThisHour.plusHours(1);
        Schedule schedule2 = Schedule.builder()
                .tennisCourt(tennisCourt1)
                .startDateTime(nextScheduleTomorrow)
                .endDateTime(nextScheduleTomorrow.plusHours(1L)).build();
        Long scheduleId2 = 2L;
        schedule2.setId(scheduleId2);

        when(scheduleRepository.findByTennisCourt_IdOrderByStartDateTime(tennisCourtId)).thenReturn(List.of(schedule, schedule2));

        List<Schedule> actualSchedules = scheduleService.findSchedulesByTennisCourtId(tennisCourtId);

        Schedule actualSchedule1 = actualSchedules.get(0);
        assertEquals(1L, actualSchedule1.getId());
        assertEquals(1L, actualSchedule1.getTennisCourt().getId());
        assertEquals(tomorrowAtThisHour, actualSchedule1.getStartDateTime());
        assertEquals(tomorrowAtThisHour.plusHours(1L), actualSchedule1.getEndDateTime());

        Schedule actualSchedule = actualSchedules.get(1);
        assertEquals(2L, actualSchedule.getId());
        assertEquals(1L, actualSchedule.getTennisCourt().getId());
        assertEquals(nextScheduleTomorrow, actualSchedule.getStartDateTime());
        assertEquals(nextScheduleTomorrow.plusHours(1L), actualSchedule.getEndDateTime());
    }


    @Test
    public void addScheduleTest_shouldPass() {
        Long tennisCourtId = 1L;
        TennisCourt tennisCourt = TennisCourt.builder().name("Tennis Court").build();
        tennisCourt.setId(tennisCourtId);
        when(tennisCourtRepository.getOne(tennisCourtId)).thenReturn(tennisCourt);

        LocalDateTime tomorrowAtThisHour = LocalDateTime.now().plusDays(1);
        Schedule schedule = Schedule.builder()
                .tennisCourt(tennisCourt)
                .startDateTime(tomorrowAtThisHour)
                .endDateTime(tomorrowAtThisHour.plusHours(1L)).build();
        when(scheduleRepository.saveAndFlush(schedule)).thenReturn(schedule);

        when(scheduleRepository.findByTennisCourt_IdAndStartDateTimeEquals(tennisCourtId, tomorrowAtThisHour)).thenReturn(null);

        Schedule actualSchedule = scheduleService.addSchedule(CreateScheduleRequestDTO.builder()
                .tennisCourtId(1L)
                .startDateTime(tomorrowAtThisHour).build());

        assertEquals(1L, actualSchedule.getTennisCourt().getId());
        assertEquals(tomorrowAtThisHour, actualSchedule.getStartDateTime());
        assertEquals(tomorrowAtThisHour.plusHours(1L), actualSchedule.getEndDateTime());
    }

    @Test
    public void addScheduleTest_startDateIsMissing() {
        Long tennisCourtId = 1L;
        TennisCourt tennisCourt = TennisCourt.builder().name("Tennis Court").build();
        tennisCourt.setId(tennisCourtId);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                scheduleService.addSchedule(CreateScheduleRequestDTO.builder()
                        .tennisCourtId(1L)
                        .build()));

        assertEquals("Schedule start date is missing", exception.getMessage());
    }

    @Test
    public void addScheduleTest_startDateCannotBeOlderThanToday() {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        Long tennisCourtId = 1L;
        TennisCourt tennisCourt = TennisCourt.builder().name("Tennis Court").build();
        tennisCourt.setId(tennisCourtId);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                scheduleService.addSchedule(CreateScheduleRequestDTO.builder()
                        .tennisCourtId(1L)
                        .startDateTime(pastDate)
                        .build()));

        assertEquals("Schedule start date cannot be older than today", exception.getMessage());
    }

    @Test
    public void addScheduleTest_scheduleIsNotAvailable() {
        LocalDateTime tomorrowAtThisHour = LocalDateTime.now().plusDays(1);

        Long tennisCourtId = 1L;
        TennisCourt tennisCourt = TennisCourt.builder().name("Tennis Court").build();
        tennisCourt.setId(tennisCourtId);

        Schedule scheduleAreadyExistent = Schedule.builder()
                .tennisCourt(tennisCourt)
                .startDateTime(tomorrowAtThisHour)
                .endDateTime(tomorrowAtThisHour.plusHours(1L)).build();
        when(scheduleRepository.findByTennisCourt_IdAndStartDateTimeEquals(tennisCourtId, tomorrowAtThisHour)).thenReturn(scheduleAreadyExistent);

        AlreadyExistsEntityException exception = assertThrows(AlreadyExistsEntityException.class, () ->
                scheduleService.addSchedule(CreateScheduleRequestDTO.builder()
                        .tennisCourtId(1L)
                        .startDateTime(tomorrowAtThisHour)
                        .build()));

        assertEquals("The schedule is not available", exception.getMessage());
    }

    @Test
    public void getValidScheduleForReservationTest_shouldThrowAlreadyExistsEntityException() {
        Long scheduleId = 1L;
        Reservation reservationCanceled = Reservation.builder().reservationStatus(CANCELLED).build();
        Reservation reservationReadyToPlay = Reservation.builder().reservationStatus(READY_TO_PLAY).build();
        when(reservationRepository.findBySchedule_Id(scheduleId)).thenReturn(List.of(reservationCanceled, reservationReadyToPlay));
        AlreadyExistsEntityException exception = Assertions.assertThrows(AlreadyExistsEntityException.class,
                () -> scheduleService.getValidScheduleForReservation(scheduleId));
        Assertions.assertEquals("Reservation already exists", exception.getMessage());
    }

    @Test
    public void getValidScheduleForReservationTest_shouldThrowIllegalArgumentException() {
        Long scheduleId = 1L;
        Reservation reservationCanceled = Reservation.builder().reservationStatus(CANCELLED).build();
        when(reservationRepository.findBySchedule_Id(scheduleId)).thenReturn(List.of(reservationCanceled));

        Schedule scheduleInThePast = Schedule.builder().startDateTime(LocalDateTime.now().minusDays(1)).build();
        scheduleInThePast.setId(scheduleId);
        when(scheduleRepository.getOne(scheduleId)).thenReturn(scheduleInThePast);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> scheduleService.getValidScheduleForReservation(scheduleId));
        Assertions.assertEquals("Start date can not be older than today", exception.getMessage());
    }

    @Test
    public void getValidScheduleForReservationTest_shouldPass() {
        Long scheduleId = 1L;
        Reservation reservationCanceled = Reservation.builder().reservationStatus(CANCELLED).build();
        reservationCanceled.setId(2L);
        when(reservationRepository.findBySchedule_Id(scheduleId)).thenReturn(List.of(reservationCanceled));

        LocalDateTime startDateSchedule = LocalDateTime.now().plusDays(1);
        LocalDateTime endDateSchedule = LocalDateTime.now().plusDays(1).plusHours(1L);

        Schedule schedule = Schedule.builder()
                .reservations(List.of(reservationCanceled))
                .startDateTime(startDateSchedule)
                .endDateTime(endDateSchedule)
                .build();
        schedule.setId(scheduleId);
        when(scheduleRepository.getOne(scheduleId)).thenReturn(schedule);

        Schedule actualSchedule = scheduleService.getValidScheduleForReservation(scheduleId);

        Assertions.assertEquals(1L, actualSchedule.getId());
        Assertions.assertEquals(startDateSchedule, actualSchedule.getStartDateTime());
        Assertions.assertEquals(endDateSchedule, actualSchedule.getEndDateTime());

        Reservation reservationInTheSchedule = actualSchedule.getReservations().get(0);
        Assertions.assertEquals(2L, reservationInTheSchedule.getId());
        Assertions.assertEquals(CANCELLED, reservationInTheSchedule.getReservationStatus());
    }
}