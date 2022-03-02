package com.tenniscourts.guests;

import com.tenniscourts.config.BaseRestController;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/guests")
public class GuestController extends BaseRestController {

    private final GuestService guestService;

    @ApiOperation(value = "Finds only one guest by id")
    @GetMapping(value = "/{id}")
    public ResponseEntity<GuestDTO> findGuest(@PathVariable Long id) {
        return ResponseEntity.ok(guestService.findById(id));
    }

    @ApiOperation(value = "Gets a list with all the guests and filters them by full name if parameter is present")
    @GetMapping
    public ResponseEntity<List<GuestDTO>> findGuests(@RequestParam(value = "name", required = false) String name) {
        return name == null ? ResponseEntity.ok(guestService.findAll()) : ResponseEntity.ok(guestService.findByName(name));
    }

    @ApiOperation(value = "Gets a list with guests that have part of the full name like the input parameter")
    @GetMapping(value = "search-in-name/{partialName}")
    public ResponseEntity<List<GuestDTO>> findsGuestsByPartialName(@PathVariable String partialName) {
        return ResponseEntity.ok(guestService.findByPartialName(partialName));
    }

    @ApiOperation(value = "Creates a guest")
    @PostMapping
    public ResponseEntity<Void> createGuest(@RequestBody CreateGuestRequestDTO guestDTO) {
        return ResponseEntity.created(locationByEntity(guestService.addGuest(guestDTO))).build();
    }

    @ApiOperation(value = "Updates a guest")
    @PutMapping
    public ResponseEntity<Void> updateGuest(@RequestBody GuestDTO guestDTO) {
        return ResponseEntity.created(locationByEntity(guestService.updateGuest(guestDTO))).build();
    }

    @ApiOperation(value = "Deletes a guest by id")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteGuest(@PathVariable Long id) {
        guestService.deleteGuest(id);
        return ResponseEntity.ok().build();
    }

}
