package com.tenniscourts.tenniscourts;

import com.tenniscourts.config.BaseRestController;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/tennis-courts")
public class TennisCourtController extends BaseRestController {

    private final TennisCourtService tennisCourtService;

    @ApiOperation(value = "Finds only one tennis court by id")
    @GetMapping(value = "/{tennisCourtId}")
    public ResponseEntity<TennisCourtDTO> findTennisCourtById(@PathVariable Long tennisCourtId) {
        return ResponseEntity.ok(tennisCourtService.findTennisCourtById(tennisCourtId));
    }

    @ApiOperation(value = "Creates a tennis court")
    @PostMapping
    public ResponseEntity<Void> addTennisCourt(@RequestBody CreateTennisCourtRequestDTO createTennisCourtRequestDTO) {
        return ResponseEntity.created(locationByEntity(tennisCourtService.addTennisCourt(createTennisCourtRequestDTO))).build();
    }

    @ApiOperation(value = "Finds only one tennis court by id and retrieves its schedules also")
    @GetMapping(value = "/{tennisCourtId}/schedules")
    public ResponseEntity<TennisCourtDTO> findTennisCourtWithSchedulesById(@PathVariable Long tennisCourtId) {
        return ResponseEntity.ok(tennisCourtService.findTennisCourtWithSchedulesById(tennisCourtId));
    }
}
