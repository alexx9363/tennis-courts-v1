package com.tenniscourts.reservations;

import com.tenniscourts.guests.Guest;
import com.tenniscourts.guests.GuestRepository;
import com.tenniscourts.schedules.Schedule;
import com.tenniscourts.schedules.ScheduleService;
import com.tenniscourts.tenniscourts.TennisCourt;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.tenniscourts.reservations.ReservationStatus.CANCELLED;
import static com.tenniscourts.reservations.ReservationStatus.READY_TO_PLAY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = ReservationService.class)
public class ReservationServiceTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    GuestRepository guestRepository;

    @Mock
    ScheduleService scheduleService;

    @InjectMocks
    ReservationService reservationService;

    @Test
    public void bookReservationTest() {
        Long guestId = 1L;
        Guest guest = Guest.builder().name("First Guest").build();
        guest.setId(guestId);
        when(guestRepository.getOne(guestId)).thenReturn(guest);

        LocalDateTime tomorrowAtThisHour = LocalDateTime.now().plusDays(1);
        Long scheduleId = 2L;
        Schedule schedule = Schedule.builder()
                .tennisCourt(TennisCourt.builder().name("Tennis Court").build())
                .startDateTime(tomorrowAtThisHour)
                .endDateTime(tomorrowAtThisHour.plusHours(1L)).build();
        schedule.setId(scheduleId);
        when(scheduleService.getValidScheduleForReservation(scheduleId)).thenReturn(schedule);

        Reservation reservation = Reservation.builder()
                .reservationStatus(READY_TO_PLAY)
                .guest(guest)
                .schedule(schedule)
                .value(new BigDecimal(10)).build();
        when(reservationRepository.saveAndFlush(reservation)).thenReturn(reservation);

        Reservation bookedReservation = reservationService.bookReservation(guestId, scheduleId);

        assertEquals(READY_TO_PLAY, bookedReservation.getReservationStatus());
        assertEquals(1L, bookedReservation.getGuest().getId());
        assertEquals(2L, bookedReservation.getSchedule().getId());
        assertEquals(new BigDecimal(10), bookedReservation.getValue());
    }

    @Test
    public void findReservationTest() {
        Long guestId = 3L;
        Guest guest = Guest.builder().name("First Guest").build();
        guest.setId(guestId);

        TennisCourt tennisCourt = TennisCourt.builder().name("Tennis Court").build();
        tennisCourt.setId(4L);

        LocalDateTime tomorrowAtThisHour = LocalDateTime.now().plusDays(1);
        Long scheduleId = 2L;
        Schedule schedule = Schedule.builder()
                .tennisCourt(tennisCourt)
                .startDateTime(tomorrowAtThisHour)
                .endDateTime(tomorrowAtThisHour.plusHours(1L)).build();
        schedule.setId(scheduleId);

        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .reservationStatus(READY_TO_PLAY)
                .guest(guest)
                .schedule(schedule)
                .value(new BigDecimal(10))
                .refundValue(new BigDecimal(10)).build();
        when(reservationRepository.getOne(reservationId)).thenReturn(reservation);

        Reservation actualReservation = reservationService.findReservation(reservationId);

        assertEquals(2L, actualReservation.getSchedule().getId());
        assertEquals(2L, actualReservation.getSchedule().getId());
        assertEquals(3L, actualReservation.getGuest().getId());
        assertEquals(4L, actualReservation.getSchedule().getTennisCourt().getId());
        assertEquals(tomorrowAtThisHour, actualReservation.getSchedule().getStartDateTime());
        assertEquals(tomorrowAtThisHour.plusHours(1L), actualReservation.getSchedule().getEndDateTime());
        assertEquals(READY_TO_PLAY, actualReservation.getReservationStatus());
        assertEquals(new BigDecimal(10), actualReservation.getValue());
        assertEquals(new BigDecimal(10), actualReservation.getRefundValue());
    }

    @Test
    public void cancelReservationTest_shouldThrowIsNotReadyToPlay() {
        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .reservationStatus(CANCELLED)
                .value(new BigDecimal(10))
                .build();
        reservation.setId(reservationId);

        when(reservationRepository.getOne(reservationId)).thenReturn(reservation);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.cancelReservation(reservationId));
        assertEquals("Can not update because it's not in ready to play status.", exception.getMessage());
    }

    @Test
    public void cancelReservationTest_shouldThrowCanNotUpdatePastReservations() {
        Schedule scheduleInThePast = Schedule.builder().startDateTime(LocalDateTime.now().minusDays(1)).build();

        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .reservationStatus(READY_TO_PLAY)
                .schedule(scheduleInThePast)
                .value(new BigDecimal(10))
                .build();
        reservation.setId(reservationId);

        when(reservationRepository.getOne(reservationId)).thenReturn(reservation);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.cancelReservation(reservationId));
        assertEquals("Can not update past reservations.", exception.getMessage());
    }

    @Test
    public void cancelReservationTest_fullRefund() {
        Schedule schedule = Schedule.builder().startDateTime(LocalDateTime.now().plusDays(2)).build();

        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .reservationStatus(READY_TO_PLAY)
                .schedule(schedule)
                .value(new BigDecimal(10))
                .build();
        reservation.setId(reservationId);
        when(reservationRepository.getOne(reservationId)).thenReturn(reservation);
        when(reservationRepository.saveAndFlush(reservation)).thenReturn(reservation);

        Reservation actualCanceledReservation = reservationService.cancelReservation(reservationId);

        assertEquals(1L, actualCanceledReservation.getId());
        assertEquals(CANCELLED, actualCanceledReservation.getReservationStatus());

        assertTrue(BigDecimal.valueOf(0).compareTo(actualCanceledReservation.getValue()) == 0);
        assertTrue(BigDecimal.valueOf(10).compareTo(actualCanceledReservation.getRefundValue()) == 0);
    }

    @Test
    public void cancelReservationTest_75PercentRefund() {
        Schedule schedule = Schedule.builder().startDateTime(LocalDateTime.now().plusHours(13)).build();

        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .reservationStatus(READY_TO_PLAY)
                .schedule(schedule)
                .value(new BigDecimal(10))
                .build();
        reservation.setId(reservationId);
        when(reservationRepository.getOne(reservationId)).thenReturn(reservation);
        when(reservationRepository.saveAndFlush(reservation)).thenReturn(reservation);

        Reservation actualCanceledReservation = reservationService.cancelReservation(reservationId);

        assertEquals(1L, actualCanceledReservation.getId());
        assertEquals(CANCELLED, actualCanceledReservation.getReservationStatus());
        assertTrue(BigDecimal.valueOf(2.50).compareTo(actualCanceledReservation.getValue()) == 0);
        assertTrue(BigDecimal.valueOf(7.50).compareTo(actualCanceledReservation.getRefundValue()) == 0);
    }

    @Test
    public void cancelReservationTest_50PercentRefund() {
        Schedule schedule = Schedule.builder().startDateTime(LocalDateTime.now().plusHours(10)).build();

        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .reservationStatus(READY_TO_PLAY)
                .schedule(schedule)
                .value(new BigDecimal(10))
                .build();
        reservation.setId(reservationId);
        when(reservationRepository.getOne(reservationId)).thenReturn(reservation);
        when(reservationRepository.saveAndFlush(reservation)).thenReturn(reservation);

        Reservation actualCanceledReservation = reservationService.cancelReservation(reservationId);

        assertEquals(1L, actualCanceledReservation.getId());
        assertEquals(CANCELLED, actualCanceledReservation.getReservationStatus());
        assertTrue(BigDecimal.valueOf(5).compareTo(actualCanceledReservation.getValue()) == 0);
        assertTrue(BigDecimal.valueOf(5).compareTo(actualCanceledReservation.getRefundValue()) == 0);
    }

    @Test
    public void cancelReservationTest_25PercentRefund() {
        Schedule schedule = Schedule.builder().startDateTime(LocalDateTime.now().plusHours(1)).build();

        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .reservationStatus(READY_TO_PLAY)
                .schedule(schedule)
                .value(new BigDecimal(10))
                .build();
        reservation.setId(reservationId);
        when(reservationRepository.getOne(reservationId)).thenReturn(reservation);
        when(reservationRepository.saveAndFlush(reservation)).thenReturn(reservation);

        Reservation actualCanceledReservation = reservationService.cancelReservation(reservationId);

        assertEquals(1L, actualCanceledReservation.getId());
        assertEquals(CANCELLED, actualCanceledReservation.getReservationStatus());
        assertTrue(BigDecimal.valueOf(7.50).compareTo(actualCanceledReservation.getValue()) == 0);
        assertTrue(BigDecimal.valueOf(2.50).compareTo(actualCanceledReservation.getRefundValue()) == 0);
    }

    @Test
    public void cancelReservationTest_0PercentRefund() {
        Schedule schedule = Schedule.builder().startDateTime(LocalDateTime.now().plusSeconds(30)).build();

        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .reservationStatus(READY_TO_PLAY)
                .schedule(schedule)
                .value(new BigDecimal(10))
                .build();
        reservation.setId(reservationId);
        when(reservationRepository.getOne(reservationId)).thenReturn(reservation);
        when(reservationRepository.saveAndFlush(reservation)).thenReturn(reservation);

        Reservation actualCanceledReservation = reservationService.cancelReservation(reservationId);

        assertEquals(1L, actualCanceledReservation.getId());
        assertEquals(CANCELLED, actualCanceledReservation.getReservationStatus());
        assertTrue(BigDecimal.valueOf(10).compareTo(actualCanceledReservation.getValue()) == 0);
        assertTrue(BigDecimal.valueOf(0).compareTo(actualCanceledReservation.getRefundValue()) == 0);
    }

    @Test
    public void rescheduleReservationTest() {
        Schedule schedule = Schedule.builder().startDateTime(LocalDateTime.now().plusDays(1)).build();
        Guest guest = Guest.builder().name("Any Guest").build();
        guest.setId(1L);
        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .reservationStatus(READY_TO_PLAY)
                .guest(guest)
                .schedule(schedule)
                .value(new BigDecimal(10))
                .build();
        reservation.setId(reservationId);
        when(reservationRepository.getOne(reservationId)).thenReturn(reservation);
        when(reservationRepository.saveAndFlush(reservation)).thenReturn(reservation);

        Schedule reSchedule = Schedule.builder().startDateTime(LocalDateTime.now().plusDays(15)).build();
        reSchedule.setId(2L);
        Long rescheduleReservationId = 2L;
        Reservation rescheduledReservation = Reservation.builder()
                .reservationStatus(READY_TO_PLAY)
                .guest(guest)
                .schedule(reSchedule)
                .value(new BigDecimal(10))
                .build();
        rescheduledReservation.setId(2L);
        when(guestRepository.getOne(guest.getId())).thenReturn(guest);
        when(scheduleService.getValidScheduleForReservation(rescheduleReservationId)).thenReturn(reSchedule);
        when(reservationRepository.saveAndFlush(rescheduledReservation)).thenReturn(rescheduledReservation);


        Reservation actualRescheduledReservation = reservationService.rescheduleReservation(reservationId, reSchedule.getId());


        assertEquals(2L, actualRescheduledReservation.getId());
        assertEquals(READY_TO_PLAY, actualRescheduledReservation.getReservationStatus());
        assertEquals(1L, actualRescheduledReservation.getGuest().getId());
        assertEquals("Any Guest", actualRescheduledReservation.getGuest().getName());
        assertEquals(2L, actualRescheduledReservation.getSchedule().getId());
        assertTrue(BigDecimal.valueOf(10).compareTo(actualRescheduledReservation.getValue()) == 0);
    }

    @Test
    public void rescheduleReservationTest_shouldThrowScheduleIdCannotBeNull() {
        Schedule schedule = Schedule.builder().startDateTime(LocalDateTime.now().plusDays(1)).build();
        Guest guest = Guest.builder().name("Any Guest").build();
        guest.setId(1L);
        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .reservationStatus(READY_TO_PLAY)
                .guest(guest)
                .schedule(schedule)
                .value(new BigDecimal(10))
                .build();
        reservation.setId(reservationId);
        when(reservationRepository.getOne(reservationId)).thenReturn(reservation);


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.rescheduleReservation(reservationId, null);
        });
        assertEquals("Schedule id cannot be null.", exception.getMessage());
    }

    @Test
    public void rescheduleReservationTest_shouldThrowCannotRescheduleToTheSameSlot() {
        Schedule schedule = Schedule.builder().startDateTime(LocalDateTime.now().plusDays(1)).build();
        schedule.setId(1L);
        Guest guest = Guest.builder().name("Any Guest").build();
        guest.setId(1L);
        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .reservationStatus(READY_TO_PLAY)
                .guest(guest)
                .schedule(schedule)
                .value(new BigDecimal(10))
                .build();
        reservation.setId(reservationId);
        when(reservationRepository.getOne(reservationId)).thenReturn(reservation);


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.rescheduleReservation(reservationId, schedule.getId());
        });
        assertEquals("Cannot reschedule to the same slot.", exception.getMessage());
    }

    @Test
    public void findAllPastReservationsTest() {
        Long guestId = 3L;
        Guest guest = Guest.builder().name("First Guest").build();
        guest.setId(guestId);

        TennisCourt tennisCourt = TennisCourt.builder().name("Tennis Court").build();
        tennisCourt.setId(4L);

        LocalDateTime pastDay = LocalDateTime.now().minusDays(1);
        Long scheduleId = 2L;
        Schedule schedule = Schedule.builder()
                .tennisCourt(tennisCourt)
                .startDateTime(pastDay)
                .endDateTime(pastDay.plusHours(1L)).build();
        schedule.setId(scheduleId);

        Reservation reservation = Reservation.builder()
                .reservationStatus(READY_TO_PLAY)
                .guest(guest)
                .schedule(schedule)
                .value(new BigDecimal(10))
                .refundValue(new BigDecimal(10)).build();
        when(reservationRepository.findAllBySchedule_StartDateTimeLessThanEqual(any())).thenReturn(List.of(reservation));

        List<Reservation> actualReservations = reservationService.findAllPastReservations();

        Reservation reservation1 = actualReservations.get(0);
        assertEquals(2L, reservation1.getSchedule().getId());
        assertEquals(3L, reservation1.getGuest().getId());
        assertEquals(4L, reservation1.getSchedule().getTennisCourt().getId());
        assertEquals(pastDay, reservation1.getSchedule().getStartDateTime());
        assertEquals(pastDay.plusHours(1L), reservation1.getSchedule().getEndDateTime());
        assertEquals(READY_TO_PLAY, reservation1.getReservationStatus());
        assertEquals(new BigDecimal(10), reservation1.getValue());
        assertEquals(new BigDecimal(10), reservation1.getRefundValue());
    }
}