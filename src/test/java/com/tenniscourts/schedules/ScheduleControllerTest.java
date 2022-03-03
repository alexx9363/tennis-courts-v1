package com.tenniscourts.schedules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenniscourts.tenniscourts.TennisCourt;
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
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
public class ScheduleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private ScheduleMapper scheduleMapper;

    @InjectMocks
    private ScheduleController scheduleController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(scheduleController).build();
    }

    @Test
    public void findByScheduleIdTest() throws Exception {
        ScheduleDTO scheduleDTO = ScheduleDTO.builder()
                .id(1L)
                .tennisCourtId(1L)
                .startDateTime(LocalDateTime.of(2022, 3, 2, 12, 0, 0))
                .endDateTime(LocalDateTime.of(2022, 3, 2, 13, 0, 0))
                .build();
        Mockito.when(scheduleMapper.map((Schedule) any())).thenReturn(scheduleDTO);
        mockMvc.perform(get("/schedules/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.tennisCourtId", is(1)))
                .andExpect(jsonPath("$.startDateTime", is("2022-03-02T12:00")))
                .andExpect(jsonPath("$.endDateTime", is("2022-03-02T13:00")));
    }

    @Test
    public void findSchedulesByDatesTest() throws Exception {
        ScheduleDTO secondDayOfMarchScheduleDTO = ScheduleDTO.builder()
                .id(1L)
                .tennisCourtId(1L)
                .startDateTime(LocalDateTime.of(2022, 3, 2, 12, 0, 0))
                .endDateTime(LocalDateTime.of(2022, 3, 2, 13, 0, 0))
                .build();
        ScheduleDTO thirdDayOfMarchScheduleDTO = ScheduleDTO.builder()
                .id(2L)
                .tennisCourtId(1L)
                .startDateTime(LocalDateTime.of(2022, 3, 3, 12, 0, 0))
                .endDateTime(LocalDateTime.of(2022, 3, 3, 13, 0, 0))
                .build();
        Mockito.when(scheduleMapper.map((List<Schedule>) any())).thenReturn(List.of(secondDayOfMarchScheduleDTO, thirdDayOfMarchScheduleDTO));

        mockMvc.perform(get("/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("startDate", "2022-03-01")
                        .param("endDate", "2022-03-04"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].tennisCourtId", is(1)))
                .andExpect(jsonPath("$[0].startDateTime", is("2022-03-02T12:00")))
                .andExpect(jsonPath("$[0].endDateTime", is("2022-03-02T13:00")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].tennisCourtId", is(1)))
                .andExpect(jsonPath("$[1].startDateTime", is("2022-03-03T12:00")))
                .andExpect(jsonPath("$[1].endDateTime", is("2022-03-03T13:00")));
    }

    @Test
    public void addScheduleTennisCourtTest() throws Exception {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);

        CreateScheduleRequestDTO createScheduleRequestDTO = CreateScheduleRequestDTO
                .builder()
                .tennisCourtId(1L)
                .startDateTime(tomorrow)
                .build();

        TennisCourt tennisCourt = TennisCourt.builder().name("anything").build();
        tennisCourt.setId(1L);
        Schedule schedule = Schedule.builder().tennisCourt(tennisCourt).startDateTime(tomorrow).build();
        schedule.setId(1L);
        Mockito.when(scheduleService.addSchedule(any())).thenReturn(schedule);

        mockMvc.perform(post("/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(createScheduleRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("location", containsString("/schedules/1")));
    }
}