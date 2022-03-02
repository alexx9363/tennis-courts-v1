package com.tenniscourts.guests;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;

    private final GuestMapper guestMapper;

    public GuestDTO findById(Long guestId) {
        return guestMapper.map(guestRepository.getOne(guestId));
    }

    public List<GuestDTO> findAll() {
        return guestRepository.findAll().stream().map(guestMapper::map).collect(toList());
    }

    public List<GuestDTO> findByName(String name) {
        return guestRepository.findAllByName(name).stream().map(guestMapper::map).collect(toList());
    }

    public List<GuestDTO> findByPartialName(String partialName) {
        return guestRepository.findAllByNameContainsIgnoreCase(partialName).stream().map(guestMapper::map).collect(toList());
    }

    public Long addGuest(CreateGuestRequestDTO guestDTO) {
        return guestRepository.saveAndFlush(guestMapper.map(guestDTO)).getId();
    }

    public Long updateGuest(GuestDTO guestDTO) {
        if (guestDTO.getId() == null) {
            throw new IllegalArgumentException("Guest id is missing");
        }
        return guestRepository.saveAndFlush(guestMapper.map(guestDTO)).getId();
    }

    public void deleteGuest(Long id) {
        guestRepository.deleteById(id);
    }
}
