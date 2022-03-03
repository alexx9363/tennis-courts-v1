package com.tenniscourts.tenniscourts;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = TennisCourtService.class)
public class TennisCourtServiceTest {

    @Mock
    private TennisCourtRepository tennisCourtRepository;

    @InjectMocks
    private TennisCourtService tennisCourtService;

    @Test
    public void addTennisCourtTest() {
        TennisCourt tennisCourt = TennisCourt.builder().name("Tennis Court").build();
        when(tennisCourtRepository.saveAndFlush(tennisCourt)).thenReturn(tennisCourt);
        TennisCourt newTennisCourt = TennisCourt.builder()
                .name("Tennis Court")
                .build();

        TennisCourt actualTennisCourt = tennisCourtService.addTennisCourt(newTennisCourt);

        assertEquals("Tennis Court", actualTennisCourt.getName());
    }

    @Test
    public void findTennisCourtByIdTest() {
        Long id = 1L;
        TennisCourt tennisCourt = TennisCourt.builder().name("Any Court").build();
        tennisCourt.setId(id);
        when(tennisCourtRepository.getOne(id)).thenReturn(tennisCourt);

        TennisCourt actualTennisCourt = tennisCourtService.findTennisCourtById(id);

        assertEquals(1L, actualTennisCourt.getId());
        assertEquals("Any Court", actualTennisCourt.getName());
    }
}