package com.tenniscourts.schedules;

import com.tenniscourts.exceptions.AlreadyExistsEntityException;
import com.tenniscourts.reservations.Reservation;
import com.tenniscourts.reservations.ReservationRepository;
import com.tenniscourts.reservations.ReservationStatus;
import com.tenniscourts.tenniscourts.TennisCourtRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    private final ReservationRepository reservationRepository;

    private final ScheduleMapper scheduleMapper;

    private final TennisCourtRepository tennisCourtRepository;

    private final static Long NUMBER_OF_HOURS_GUESTS_ALWAYS_PLAY = 1L;

    public ScheduleDTO findSchedule(Long scheduleId) {
        return scheduleMapper.map(scheduleRepository.getOne(scheduleId));
    }

    public List<ScheduleDTO> findSchedulesByDates(LocalDateTime startDate, LocalDateTime endDate) {
        return scheduleMapper.map(scheduleRepository.findAllByStartDateTimeIsGreaterThanEqualAndEndDateTimeIsLessThanEqual(startDate, endDate));
    }

    public List<ScheduleDTO> findSchedulesByTennisCourtId(Long tennisCourtId) {
        return scheduleMapper.map(scheduleRepository.findByTennisCourt_IdOrderByStartDateTime(tennisCourtId));
    }

    public Long addSchedule(CreateScheduleRequestDTO createScheduleRequestDTO) {
        if (createScheduleRequestDTO.getStartDateTime() == null) {
            throw new IllegalArgumentException("Schedule start date is missing");
        }
        if (createScheduleRequestDTO.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Schedule start date cannot be older than today");
        }
        if (scheduleRepository.findByTennisCourt_IdAndStartDateTimeEquals(
                createScheduleRequestDTO.getTennisCourtId(), createScheduleRequestDTO.getStartDateTime()) != null) {
            throw new AlreadyExistsEntityException("The schedule is not available");
        }
        return scheduleMapper.map(scheduleRepository.saveAndFlush(
                Schedule.builder()
                        .tennisCourt(tennisCourtRepository.getOne(createScheduleRequestDTO.getTennisCourtId()))
                        .startDateTime(createScheduleRequestDTO.getStartDateTime())
                        .endDateTime(createScheduleRequestDTO.getStartDateTime().plusHours(NUMBER_OF_HOURS_GUESTS_ALWAYS_PLAY))
                        .build())).getId();
    }

    public Schedule getValidScheduleForReservation(Long scheduleId) {
        List<Reservation> reservationList = reservationRepository.findBySchedule_Id(scheduleId);
        if (reservationList.stream().anyMatch(reservation -> ReservationStatus.READY_TO_PLAY.equals(reservation.getReservationStatus()))) {
            throw new AlreadyExistsEntityException("Reservation already exists");
        }
        Schedule schedule = scheduleRepository.getOne(scheduleId);

        if (schedule.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start date can not be older than today");
        }
        return schedule;
    }

}
