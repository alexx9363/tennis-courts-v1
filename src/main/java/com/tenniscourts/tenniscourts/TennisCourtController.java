package com.tenniscourts.tenniscourts;

import com.tenniscourts.config.BaseRestController;
import com.tenniscourts.schedules.ScheduleMapper;
import com.tenniscourts.schedules.ScheduleService;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/tennis-courts")
public class TennisCourtController extends BaseRestController {

    private final TennisCourtService tennisCourtService;

    private final TennisCourtMapper tennisCourtMapper;

    private final ScheduleService scheduleService;

    private final ScheduleMapper scheduleMapper;


    @ApiOperation(value = "Finds only one tennis court by id")
    @GetMapping(value = "/{tennisCourtId}")
    public ResponseEntity<TennisCourtDTO> findTennisCourtById(@PathVariable Long tennisCourtId) {
        return ResponseEntity.ok(tennisCourtMapper.map(tennisCourtService.findTennisCourtById(tennisCourtId)));
    }

    @ApiOperation(value = "Creates a tennis court")
    @PostMapping
    public ResponseEntity<Void> addTennisCourt(@RequestBody CreateTennisCourtRequestDTO createTennisCourtRequestDTO) {
        return ResponseEntity.created(locationByEntity(tennisCourtService.addTennisCourt(tennisCourtMapper.map(createTennisCourtRequestDTO)).getId())).build();
    }

    @ApiOperation(value = "Finds only one tennis court by id and retrieves its schedules also")
    @GetMapping(value = "/{tennisCourtId}/schedules")
    public ResponseEntity<TennisCourtDTO> findTennisCourtWithSchedulesById(@PathVariable Long tennisCourtId) {
        TennisCourtDTO tennisCourtDTO = tennisCourtMapper.map(tennisCourtService.findTennisCourtById(tennisCourtId));
        tennisCourtDTO.setTennisCourtSchedules(scheduleMapper.map(scheduleService.findSchedulesByTennisCourtId(tennisCourtId)));
        return ResponseEntity.ok(tennisCourtDTO);
    }
}
