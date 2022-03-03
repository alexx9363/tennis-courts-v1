package com.tenniscourts.reservations;

import com.tenniscourts.config.BaseRestController;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/reservations")
public class ReservationController extends BaseRestController {

    private final ReservationService reservationService;

    private final ReservationMapper reservationMapper;

    @ApiOperation(value = "Add a reservation")
    @PostMapping
    public ResponseEntity<Void> bookReservation(@RequestBody CreateReservationRequestDTO createReservationRequestDTO) {
        return ResponseEntity.created(locationByEntity(reservationMapper.map(reservationService.bookReservation(
                        createReservationRequestDTO.getGuestId(),
                        createReservationRequestDTO.getScheduleId()))
                .getId())).build();
    }

    @ApiOperation(value = "Finds reservation")
    @GetMapping(value = "/{reservationId}")
    public ResponseEntity<ReservationDTO> findReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationMapper.map(reservationService.findReservation(reservationId)));
    }

    @ApiOperation(value = "Cancels reservation")
    @PutMapping(value = "/{reservationId}/cancel")
    public ResponseEntity<ReservationDTO> cancelReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationMapper.map(reservationService.cancelReservation(reservationId)));
    }

    @ApiOperation(value = "Reschedules reservation")
    @PutMapping(value = "/reschedule")
    public ResponseEntity<ReservationDTO> rescheduleReservation(@RequestParam Long reservationId, @RequestParam Long scheduleId) {
        return ResponseEntity.ok(reservationMapper.map(reservationService.rescheduleReservation(reservationId, scheduleId)));
    }

    @ApiOperation(value = "Finds all past reservations")
    @GetMapping
    public ResponseEntity<List<ReservationDTO>> findAllPastReservations() {
        return ResponseEntity.ok(reservationMapper.map(reservationService.findAllPastReservations()));
    }
}
