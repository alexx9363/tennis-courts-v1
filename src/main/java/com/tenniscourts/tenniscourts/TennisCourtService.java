package com.tenniscourts.tenniscourts;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TennisCourtService {

    private final TennisCourtRepository tennisCourtRepository;

    public TennisCourt addTennisCourt(TennisCourt newTennisCourt) {
        return tennisCourtRepository.saveAndFlush(newTennisCourt);
    }

    public TennisCourt findTennisCourtById(Long id) {
        return tennisCourtRepository.getOne(id);
    }

}
