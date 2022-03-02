package com.tenniscourts.tenniscourts;

import com.tenniscourts.schedules.ScheduleDTO;
import com.tenniscourts.schedules.ScheduleService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = TennisCourtService.class)
public class TennisCourtServiceTest {

    @Mock
    private TennisCourtRepository tennisCourtRepository;

    @Mock
    private TennisCourtMapper tennisCourtMapper;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private TennisCourtService tennisCourtService;

    @Test
    public void addTennisCourtTest() {
        TennisCourt tennisCourt = TennisCourt.builder().name("Tennis Court").build();
        when(tennisCourtRepository.saveAndFlush(tennisCourt)).thenReturn(tennisCourt);
        tennisCourt.setId(2L);
        CreateTennisCourtRequestDTO createTennisCourtRequestDTO = CreateTennisCourtRequestDTO.builder()
                .name("Tennis Court")
                .build();
        when(tennisCourtMapper.map(createTennisCourtRequestDTO)).thenReturn(tennisCourt);

        Long actualId = tennisCourtService.addTennisCourt(createTennisCourtRequestDTO);

        assertEquals(2L, actualId);
    }

    @Test
    public void findTennisCourtByIdTest() {
        Long id = 1L;
        TennisCourt tennisCourt = TennisCourt.builder().name("Any Court").build();
        tennisCourt.setId(id);
        when(tennisCourtRepository.getOne(id)).thenReturn(tennisCourt);
        TennisCourtDTO tennisCourtDTO = TennisCourtDTO.builder().id(id).name("Any Court").build();
        when(tennisCourtMapper.map(tennisCourt)).thenReturn(tennisCourtDTO);

        TennisCourtDTO actualTennisCourtDTO = tennisCourtService.findTennisCourtById(id);

        assertEquals(1L, actualTennisCourtDTO.getId());
        assertEquals("Any Court", actualTennisCourtDTO.getName());
    }

    @Test
    public void findTennisCourtWithSchedulesByIdTest() {
        Long id = 1L;
        TennisCourt tennisCourt = TennisCourt.builder().name("Any Court").build();
        tennisCourt.setId(id);
        when(tennisCourtRepository.getOne(id)).thenReturn(tennisCourt);
        TennisCourtDTO tennisCourtDTO = TennisCourtDTO.builder().id(id).name("Any Court").build();
        when(tennisCourtMapper.map(tennisCourt)).thenReturn(tennisCourtDTO);
        ScheduleDTO scheduleDTO1 = ScheduleDTO.builder()
                .id(1L)
                .tennisCourtId(id)
                .startDateTime(LocalDateTime.of(2022, 3, 2, 12, 0, 0))
                .endDateTime(LocalDateTime.of(2022, 3, 2, 13, 0, 0))
                .build();
        ScheduleDTO scheduleDTO2 = ScheduleDTO.builder()
                .id(2L)
                .tennisCourtId(id)
                .startDateTime(LocalDateTime.of(2022, 3, 2, 13, 0, 0))
                .endDateTime(LocalDateTime.of(2022, 3, 2, 15, 0, 0))
                .build();
        when(scheduleService.findSchedulesByTennisCourtId(id)).thenReturn(List.of(scheduleDTO1, scheduleDTO2));

        TennisCourtDTO actualTennisCourtDTO = tennisCourtService.findTennisCourtWithSchedulesById(id);

        assertEquals(1L, actualTennisCourtDTO.getId());
        assertEquals("Any Court", actualTennisCourtDTO.getName());
        ScheduleDTO actualSchedule1 = actualTennisCourtDTO.getTennisCourtSchedules().get(0);
        assertEquals(1L, actualSchedule1.getId());
        assertEquals(1L, actualSchedule1.getTennisCourtId());
        assertEquals(
                LocalDateTime.of(2022, 3, 2, 12, 0, 0),
                actualSchedule1.getStartDateTime());
        assertEquals(
                LocalDateTime.of(2022, 3, 2, 13, 0, 0),
                actualSchedule1.getEndDateTime());
        ScheduleDTO actualSchedule2 = actualTennisCourtDTO.getTennisCourtSchedules().get(1);
        assertEquals(2L, actualSchedule2.getId());
        assertEquals(1L, actualSchedule2.getTennisCourtId());
        assertEquals(
                LocalDateTime.of(2022, 3, 2, 13, 0, 0),
                actualSchedule2.getStartDateTime());
        assertEquals(
                LocalDateTime.of(2022, 3, 2, 15, 0, 0),
                actualSchedule2.getEndDateTime());
    }
}