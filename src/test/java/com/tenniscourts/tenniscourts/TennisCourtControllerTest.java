package com.tenniscourts.tenniscourts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenniscourts.schedules.Schedule;
import com.tenniscourts.schedules.ScheduleDTO;
import com.tenniscourts.schedules.ScheduleMapper;
import com.tenniscourts.schedules.ScheduleService;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
public class TennisCourtControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TennisCourtService tennisCourtService;

    @Mock
    private TennisCourtMapper tennisCourtMapper;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private TennisCourtController tennisCourtController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(tennisCourtController).build();
    }

    @Test
    public void findTennisCourtByIdTest() throws Exception {
        TennisCourtDTO tennisCourtDTO = TennisCourtDTO.builder().id(1L).name("Tennis Court").build();
        Mockito.when(tennisCourtMapper.map((TennisCourt) any())).thenReturn(tennisCourtDTO);


        mockMvc.perform(get("/tennis-courts/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Tennis Court")));
    }

    @Test
    public void addTennisCourtTest() throws Exception {
        CreateTennisCourtRequestDTO tennisCourtDTO = CreateTennisCourtRequestDTO.builder().name("Tennis Court").build();

        TennisCourt tennisCourt = TennisCourt.builder().name("Tennis Court").build();
        tennisCourt.setId(1L);
        Mockito.when(tennisCourtService.addTennisCourt(any())).thenReturn(tennisCourt);


        mockMvc.perform(post("/tennis-courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(tennisCourtDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("location", containsString("/tennis-courts/1")));
    }

    @Test
    public void findTennisCourtWithSchedulesByIdTest() throws Exception {
        Long tennisCourtId = 1L;

        ScheduleDTO scheduleDTO1 = ScheduleDTO.builder()
                .id(1L)
                .tennisCourtId(tennisCourtId)
                .startDateTime(LocalDateTime.of(2022, 3, 2, 12, 0, 0))
                .endDateTime(LocalDateTime.of(2022, 3, 2, 13, 0, 0))
                .build();
        ScheduleDTO scheduleDTO2 = ScheduleDTO.builder()
                .id(2L)
                .tennisCourtId(tennisCourtId)
                .startDateTime(LocalDateTime.of(2022, 3, 2, 13, 0, 0))
                .endDateTime(LocalDateTime.of(2022, 3, 2, 15, 0, 0))
                .build();
        List<ScheduleDTO> scheduleList = List.of(scheduleDTO1, scheduleDTO2);

        Mockito.when(scheduleService.findSchedulesByTennisCourtId(any())).thenReturn(new ArrayList<Schedule>());
        Mockito.when(scheduleMapper.map((List<Schedule>) any())).thenReturn(scheduleList);

        TennisCourtDTO tennisCourtDTO = TennisCourtDTO.builder()
                .id(tennisCourtId)
                .name("Tennis Court")
                .tennisCourtSchedules(List.of(scheduleDTO1, scheduleDTO2))
                .build();
        Mockito.when(tennisCourtMapper.map((TennisCourt) any())).thenReturn(tennisCourtDTO);


        mockMvc.perform(get("/tennis-courts/1/schedules")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Tennis Court")))
                .andExpect(jsonPath("$.tennisCourtSchedules[0].id", is(1)))
                .andExpect(jsonPath("$.tennisCourtSchedules[0].tennisCourtId", is(1)))
                .andExpect(jsonPath("$.tennisCourtSchedules[0].startDateTime", is("2022-03-02T12:00")))
                .andExpect(jsonPath("$.tennisCourtSchedules[0].endDateTime", is("2022-03-02T13:00")))
                .andExpect(jsonPath("$.tennisCourtSchedules[1].id", is(2)))
                .andExpect(jsonPath("$.tennisCourtSchedules[1].tennisCourtId", is(1)))
                .andExpect(jsonPath("$.tennisCourtSchedules[1].startDateTime", is("2022-03-02T13:00")))
                .andExpect(jsonPath("$.tennisCourtSchedules[1].endDateTime", is("2022-03-02T15:00")));
    }
}