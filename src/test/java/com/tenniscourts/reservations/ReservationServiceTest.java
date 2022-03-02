package com.tenniscourts.reservations;

import com.tenniscourts.guests.Guest;
import com.tenniscourts.guests.GuestRepository;
import com.tenniscourts.schedules.Schedule;
import com.tenniscourts.schedules.ScheduleDTO;
import com.tenniscourts.schedules.ScheduleService;
import com.tenniscourts.tenniscourts.TennisCourt;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.tenniscourts.reservations.ReservationStatus.CANCELLED;
import static com.tenniscourts.reservations.ReservationStatus.READY_TO_PLAY;
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
    ReservationMapper reservationMapper;

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

        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .reservationStatus(READY_TO_PLAY)
                .guest(guest)
                .schedule(schedule)
                .value(new BigDecimal(10)).build();
        when(reservationRepository.saveAndFlush(reservation)).thenReturn(reservation);

        ReservationDTO reservationDTO = ReservationDTO.builder()
                .id(reservationId)
                .reservationStatus(READY_TO_PLAY.name())
                .guestId(guestId)
                .scheduledId(scheduleId)
                .value(new BigDecimal(10))
                .build();
        when(reservationMapper.map(reservation)).thenReturn(reservationDTO);

        CreateReservationRequestDTO createReservationRequestDTO = CreateReservationRequestDTO.builder().guestId(1L).scheduleId(2L).build();
        ReservationDTO actualReservationDTO = reservationService.bookReservation(createReservationRequestDTO);
        Assertions.assertEquals(1L,actualReservationDTO.getId());
        Assertions.assertEquals(READY_TO_PLAY.name(),actualReservationDTO.getReservationStatus());
        Assertions.assertEquals(1L,actualReservationDTO.getGuestId());
        Assertions.assertEquals(2L,actualReservationDTO.getScheduledId());
        Assertions.assertEquals(new BigDecimal(10),actualReservationDTO.getValue());
    }

    @Test
    public void findReservationTest() {
        Long guestId = 3L;
        Guest guest = Guest.builder().name("First Guest").build();
        guest.setId(guestId);

        LocalDateTime tomorrowAtThisHour = LocalDateTime.now().plusDays(1);
        Long scheduleId = 2L;
        Schedule schedule = Schedule.builder()
                .tennisCourt(TennisCourt.builder().name("Tennis Court").build())
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
        reservation.setId(reservationId);
        when(reservationRepository.getOne(reservationId)).thenReturn(reservation);

        ScheduleDTO scheduleDTO = ScheduleDTO.builder()
                .id(scheduleId)
                .tennisCourtId(4L)
                .startDateTime(tomorrowAtThisHour)
                .endDateTime((tomorrowAtThisHour.plusHours(1L))).build();

        ReservationDTO reservationDTO = ReservationDTO.builder()
                .id(reservationId)
                .scheduledId(scheduleId)
                .guestId(guestId)
                .schedule(scheduleDTO)
                .reservationStatus(READY_TO_PLAY.name())
                .value(new BigDecimal(10))
                .refundValue(new BigDecimal(10)).build();
        when(reservationMapper.map(reservation)).thenReturn(reservationDTO);

        ReservationDTO actualReservationDTO = reservationService.findReservation(reservationId);

        Assertions.assertEquals(1L, actualReservationDTO.getId());
        Assertions.assertEquals(2L, actualReservationDTO.getScheduledId());
        Assertions.assertEquals(2L, actualReservationDTO.getSchedule().getId());
        Assertions.assertEquals(3L, actualReservationDTO.getGuestId());
        Assertions.assertEquals(4L, actualReservationDTO.getSchedule().getTennisCourtId());
        Assertions.assertEquals(tomorrowAtThisHour, actualReservationDTO.getSchedule().getStartDateTime());
        Assertions.assertEquals(tomorrowAtThisHour.plusHours(1L), actualReservationDTO.getSchedule().getEndDateTime());
        Assertions.assertEquals(READY_TO_PLAY.name(), actualReservationDTO.getReservationStatus());
        Assertions.assertEquals(new BigDecimal(10), actualReservationDTO.getValue());
        Assertions.assertEquals(new BigDecimal(10), actualReservationDTO.getRefundValue());
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

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> reservationService.cancelReservation(reservationId));
        Assertions.assertEquals("Can not update because it's not in ready to play status.",exception.getMessage());
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

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> reservationService.cancelReservation(reservationId));
        Assertions.assertEquals("Can not update past reservations.",exception.getMessage());
    }

    @Test
    public void cancelReservationTest_fullRefund() {
        Schedule schedule = Schedule.builder().startDateTime(LocalDateTime.now().plusDays(1)).build();

        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .reservationStatus(READY_TO_PLAY)
                .schedule(schedule)
                .value(new BigDecimal(10))
                .build();
        reservation.setId(reservationId);
        when(reservationRepository.getOne(reservationId)).thenReturn(reservation);


        when(reservationRepository.saveAndFlush(reservation)).thenReturn(reservation);

//        Long scheduleId = 1L;
        ScheduleDTO scheduleDTO = ScheduleDTO.builder()
                .id(reservation.getSchedule().getId())
                .startDateTime(reservation.getSchedule().getStartDateTime())
                .endDateTime(reservation.getSchedule().getEndDateTime()).build();
        ReservationDTO reservationDTO = ReservationDTO.builder()
                .id(reservationId)
                .scheduledId(reservation.getSchedule().getId())
                .schedule(scheduleDTO)
                .reservationStatus(reservation.getReservationStatus().name())
                .value(reservation.getValue())
                .refundValue(reservation.getRefundValue()).build();

        when(reservationMapper.map(reservation)).thenReturn(reservationDTO);
        ReservationDTO actualCanceledReservation = reservationService.cancelReservation(reservationId);
        System.err.println(reservation.getReservationStatus().name());





//        Assertions.assertEquals(CANCELLED, reservation.getReservationStatus());
//        Assertions.assertTrue(new BigDecimal(0).equals(reservation.getValue()));
//        Assertions.assertEquals(new BigDecimal(10.0d), reservation.getRefundValue());


        Assertions.assertEquals(1L,actualCanceledReservation.getId());
        Assertions.assertEquals(CANCELLED.name(),actualCanceledReservation.getReservationStatus());
        Assertions.assertEquals(new BigDecimal(0),actualCanceledReservation.getValue());
        Assertions.assertEquals(new BigDecimal(10),actualCanceledReservation.getRefundValue());
    }

    @Test
    public void rescheduleReservationTest() {
    }

    @Test
    public void findAllPastReservationsTest() {
        Long guestId = 3L;
        Guest guest = Guest.builder().name("First Guest").build();
        guest.setId(guestId);

        LocalDateTime pastDay = LocalDateTime.now().minusDays(1);
        Long scheduleId = 2L;
        Schedule schedule = Schedule.builder()
                .tennisCourt(TennisCourt.builder().name("Tennis Court").build())
                .startDateTime(pastDay)
                .endDateTime(pastDay.plusHours(1L)).build();
        schedule.setId(scheduleId);

        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .reservationStatus(READY_TO_PLAY)
                .guest(guest)
                .schedule(schedule)
                .value(new BigDecimal(10))
                .refundValue(new BigDecimal(10)).build();
        when(reservationRepository.findAllBySchedule_StartDateTimeLessThanEqual(any())).thenReturn(List.of(reservation));

        ScheduleDTO scheduleDTO = ScheduleDTO.builder()
                .id(scheduleId)
                .tennisCourtId(4L)
                .startDateTime(pastDay)
                .endDateTime((pastDay.plusHours(1L))).build();

        ReservationDTO reservationDTO = ReservationDTO.builder()
                .id(reservationId)
                .scheduledId(scheduleId)
                .guestId(guestId)
                .schedule(scheduleDTO)
                .reservationStatus(READY_TO_PLAY.name())
                .value(new BigDecimal(10))
                .refundValue(new BigDecimal(10)).build();
        when(reservationMapper.map(reservation)).thenReturn(reservationDTO);

        List<ReservationDTO> actualReservationDTOs = reservationService.findAllPastReservations();

        ReservationDTO reservationDTO1 = actualReservationDTOs.get(0);
        Assertions.assertEquals(1L, reservationDTO1.getId());
        Assertions.assertEquals(2L, reservationDTO1.getScheduledId());
        Assertions.assertEquals(2L, reservationDTO1.getSchedule().getId());
        Assertions.assertEquals(3L, reservationDTO1.getGuestId());
        Assertions.assertEquals(4L, reservationDTO1.getSchedule().getTennisCourtId());
        Assertions.assertEquals(pastDay, reservationDTO1.getSchedule().getStartDateTime());
        Assertions.assertEquals(pastDay.plusHours(1L), reservationDTO1.getSchedule().getEndDateTime());
        Assertions.assertEquals(READY_TO_PLAY.name(), reservationDTO1.getReservationStatus());
        Assertions.assertEquals(new BigDecimal(10), reservationDTO1.getValue());
        Assertions.assertEquals(new BigDecimal(10), reservationDTO1.getRefundValue());
    }
}