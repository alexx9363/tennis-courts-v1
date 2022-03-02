package com.tenniscourts.tenniscourts;

import com.tenniscourts.schedules.ScheduleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TennisCourtService {

    private final TennisCourtRepository tennisCourtRepository;

    private final ScheduleService scheduleService;

    private final TennisCourtMapper tennisCourtMapper;

    public Long addTennisCourt(CreateTennisCourtRequestDTO createTennisCourtRequestDTO) {
        return tennisCourtRepository.saveAndFlush(tennisCourtMapper.map(createTennisCourtRequestDTO)).getId();
    }

    public TennisCourtDTO findTennisCourtById(Long id) {
        return tennisCourtMapper.map(tennisCourtRepository.getOne(id));
    }

    public TennisCourtDTO findTennisCourtWithSchedulesById(Long tennisCourtId) {
        TennisCourtDTO tennisCourtDTO = findTennisCourtById(tennisCourtId);
        tennisCourtDTO.setTennisCourtSchedules(scheduleService.findSchedulesByTennisCourtId(tennisCourtId));
        return tennisCourtDTO;
    }
}
