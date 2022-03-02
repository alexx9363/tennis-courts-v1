package com.tenniscourts.schedules;

import com.tenniscourts.config.BaseRestController;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/schedules")
public class ScheduleController extends BaseRestController {

    private final ScheduleService scheduleService;

    @ApiOperation(value = "Find a schedule by id")
    @GetMapping(value = "/{scheduleId}")
    public ResponseEntity<ScheduleDTO> findByScheduleId(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleService.findSchedule(scheduleId));
    }

    @ApiOperation(value = "Find schedules by start date and end date")
    @GetMapping
    public ResponseEntity<List<ScheduleDTO>> findSchedulesByDates(
            @RequestParam(value = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalTime beginningOfDay = LocalTime.of(0, 0);
        LocalTime endOfDay = LocalTime.of(23, 59, 59);
        return ResponseEntity.ok(scheduleService.findSchedulesByDates(
                LocalDateTime.of(startDate, beginningOfDay),
                LocalDateTime.of(endDate, endOfDay)));
    }

    @ApiOperation(value = "Add 1 hour schedule to a tennis court")
    @PostMapping
    public ResponseEntity<Void> addScheduleTennisCourt(@RequestBody CreateScheduleRequestDTO createScheduleRequestDTO) {
        return ResponseEntity.created(locationByEntity(scheduleService.addSchedule(createScheduleRequestDTO))).build();
    }
}
