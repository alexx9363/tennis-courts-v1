package com.tenniscourts.guests;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;

    public Guest findById(Long guestId) {
        return guestRepository.getOne(guestId);
    }

    public List<Guest> findAll() {
        return guestRepository.findAll();
    }

    public List<Guest> findByName(String name) {
        return guestRepository.findAllByName(name);
    }

    public List<Guest> findByPartialName(String partialName) {
        return guestRepository.findAllByNameContainsIgnoreCase(partialName);
    }

    public Long addGuest(Guest newGuest) {
        return guestRepository.saveAndFlush(newGuest).getId();
    }

    public Long updateGuest(Guest guest) {
        if (guest.getId() == null) {
            throw new IllegalArgumentException("Guest id is missing");
        }
        return guestRepository.saveAndFlush(guest).getId();
    }

    public void deleteGuest(Long id) {
        guestRepository.deleteById(id);
    }
}
