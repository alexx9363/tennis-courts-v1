package com.tenniscourts.reservations;

import com.tenniscourts.guests.GuestRepository;
import com.tenniscourts.schedules.ScheduleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.tenniscourts.reservations.ReservationStatus.*;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    private final ReservationMapper reservationMapper;

    private final GuestRepository guestRepository;

    private final ScheduleService scheduleService;

    private final BigDecimal DEPOSIT = new BigDecimal(10);

    public ReservationDTO bookReservation(CreateReservationRequestDTO createReservationRequestDTO) {
        Reservation reservation = Reservation.builder()
                .guest(guestRepository.getOne(createReservationRequestDTO.getGuestId()))
                .schedule(scheduleService.getValidScheduleForReservation(createReservationRequestDTO.getScheduleId()))
                .value(DEPOSIT)
                .reservationStatus(READY_TO_PLAY)
                .build();
        return reservationMapper.map(reservationRepository.saveAndFlush(reservation));
    }

    public ReservationDTO findReservation(Long reservationId) {
        return reservationMapper.map(reservationRepository.getOne(reservationId));
    }

    public ReservationDTO cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.getOne(reservationId);
        validateUpdate(reservation);
        updateReservation(reservation, CANCELLED);
        return reservationMapper.map(reservationRepository.saveAndFlush(reservation));
    }

    public ReservationDTO rescheduleReservation(Long previousReservationId, Long newScheduleId) {
        Reservation reservation = reservationRepository.getOne(previousReservationId);

        if (newScheduleId == null) {
            throw new IllegalArgumentException("Schedule id cannot be null.");
        }
        if (newScheduleId.equals(reservation.getSchedule().getId())) {
            throw new IllegalArgumentException("Cannot reschedule to the same slot.");
        }

        validateUpdate(reservation);
        updateReservation(reservation, RESCHEDULED);
        reservationRepository.saveAndFlush(reservation);

        ReservationDTO newReservation = bookReservation(CreateReservationRequestDTO.builder()
                .guestId(reservation.getGuest().getId())
                .scheduleId(newScheduleId)
                .build());
        newReservation.setPreviousReservation(reservationMapper.map(reservation));
        return newReservation;
    }

    public List<ReservationDTO> findAllPastReservations() {
        return reservationRepository.findAllBySchedule_StartDateTimeLessThanEqual(now())
                .stream()
                .map(reservationMapper::map)
                .collect(toList());
    }

    private static void validateUpdate(Reservation reservation) {
        if (!READY_TO_PLAY.equals(reservation.getReservationStatus())) {
            throw new IllegalArgumentException("Can not update because it's not in ready to play status.");
        }
        if (reservation.getSchedule().getStartDateTime().isBefore(now())) {
            throw new IllegalArgumentException("Can not update past reservations.");
        }
    }

    private void updateReservation(Reservation reservation, ReservationStatus newStatus) {
        BigDecimal refundValue = getRefundValue(reservation);
        reservation.setReservationStatus(newStatus);
        reservation.setValue(reservation.getValue().subtract(refundValue));
        reservation.setRefundValue(refundValue);
    }

    private static BigDecimal getRefundValue(Reservation reservation) {
        long minutes = ChronoUnit.MINUTES.between(now(), reservation.getSchedule().getStartDateTime());
        long hours = minutes * 60;
        if (hours >= 24) {
            return calculateRefundValue(reservation, 1);
        } else if (hours >= 12) {
            return calculateRefundValue(reservation, .75);
        } else if (hours >= 2) {
            return calculateRefundValue(reservation, .5);
        } else if (minutes >= 1) {
            return calculateRefundValue(reservation, .25);
        }
        return calculateRefundValue(reservation, 0);
    }

    private static BigDecimal calculateRefundValue(Reservation reservation, double percentage) {
        return reservation.getValue().multiply(BigDecimal.valueOf(percentage));
    }

}
