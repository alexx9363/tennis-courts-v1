package com.tenniscourts.reservations;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
public class ReservationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReservationService reservationService;

    @Mock
    private ReservationMapper reservationMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ReservationController reservationController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(reservationController).build();
    }

    @Test
    public void bookReservationTest() throws Exception {
        Long reservationId = 1L;
        CreateReservationRequestDTO createReservationRequestDTO = CreateReservationRequestDTO.builder().guestId(2L).scheduleId(3L).build();

        ReservationDTO reservationDTO = ReservationDTO.builder()
                .id(reservationId)
                .guestId(2L)
                .scheduledId(3L)
                .reservationStatus(ReservationStatus.READY_TO_PLAY.name())
                .value(new BigDecimal(10))
                .build();
        Mockito.when(reservationMapper.map((Reservation) Mockito.any())).thenReturn(reservationDTO);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(createReservationRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("location", containsString("/reservations/" + reservationId)));
    }

    @Test
    public void findReservationTest() throws Exception {
        Long reservationId = 1L;
        ReservationDTO reservationDTO = ReservationDTO.builder()
                .id(reservationId)
                .guestId(2L)
                .scheduledId(3L)
                .reservationStatus(ReservationStatus.READY_TO_PLAY.name())
                .value(new BigDecimal(10))
                .build();
        Mockito.when(reservationMapper.map((Reservation) Mockito.any())).thenReturn(reservationDTO);

        mockMvc.perform(get("/reservations/" + reservationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.guestId", is(2)))
                .andExpect(jsonPath("$.scheduledId", is(3)))
                .andExpect(jsonPath("$.reservationStatus", is(ReservationStatus.READY_TO_PLAY.name())))
                .andExpect(jsonPath("$.value", is(10)));
    }

    @Test
    public void cancelReservationTest() throws Exception {
        Long reservationId = 1L;
        ReservationDTO reservationDTO = ReservationDTO.builder()
                .id(reservationId)
                .guestId(2L)
                .scheduledId(3L)
                .reservationStatus(ReservationStatus.CANCELLED.name())
                .value(new BigDecimal(10))
                .build();
        Mockito.when(reservationMapper.map((Reservation) Mockito.any())).thenReturn(reservationDTO);

        mockMvc.perform(put("/reservations/" + reservationId + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.guestId", is(2)))
                .andExpect(jsonPath("$.scheduledId", is(3)))
                .andExpect(jsonPath("$.reservationStatus", is(ReservationStatus.CANCELLED.name())))
                .andExpect(jsonPath("$.value", is(10)));
    }

    @Test
    public void rescheduleReservationTest() throws Exception {
        Long reservationId = 1L;
        Long newScheduleId = 4L;
        ReservationDTO rescheduledReservationDTO = ReservationDTO.builder()
                .id(reservationId)
                .guestId(2L)
                .scheduledId(newScheduleId)
                .reservationStatus(ReservationStatus.CANCELLED.name())
                .value(new BigDecimal(10))
                .build();
        Mockito.when(reservationMapper.map((Reservation) Mockito.any())).thenReturn(rescheduledReservationDTO);

        mockMvc.perform(put("/reservations/reschedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("reservationId", reservationId.toString())
                        .param("scheduleId", newScheduleId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.guestId", is(2)))
                .andExpect(jsonPath("$.scheduledId", is(4)))
                .andExpect(jsonPath("$.reservationStatus", is(ReservationStatus.CANCELLED.name())))
                .andExpect(jsonPath("$.value", is(10)));
    }

    @Test
    public void findAllPastReservationsTest() throws Exception {
        ReservationDTO firstReservation = ReservationDTO.builder()
                .id(1L)
                .guestId(2L)
                .scheduledId(3L)
                .reservationStatus(ReservationStatus.READY_TO_PLAY.name())
                .value(new BigDecimal(10))
                .refundValue(new BigDecimal(10))
                .build();
        ReservationDTO secondReservation = ReservationDTO.builder()
                .id(2L)
                .guestId(2L)
                .scheduledId(4L)
                .reservationStatus(ReservationStatus.READY_TO_PLAY.name())
                .value(new BigDecimal(10))
                .refundValue(new BigDecimal(10))
                .build();
        List<ReservationDTO> reservationDTOS = List.of(firstReservation, secondReservation);
        Mockito.when(reservationService.findAllPastReservations()).thenReturn(new ArrayList<Reservation>());
        Mockito.when(reservationMapper.map((List<Reservation>) Mockito.any())).thenReturn(reservationDTOS);

        mockMvc.perform(get("/reservations").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].guestId", is(2)))
                .andExpect(jsonPath("$[0].scheduledId", is(3)))
                .andExpect(jsonPath("$[0].reservationStatus", is(ReservationStatus.READY_TO_PLAY.name())))
                .andExpect(jsonPath("$[0].refundValue", is(10)))
                .andExpect(jsonPath("$[0].value", is(10)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].guestId", is(2)))
                .andExpect(jsonPath("$[1].scheduledId", is(4)))
                .andExpect(jsonPath("$[1].reservationStatus", is(ReservationStatus.READY_TO_PLAY.name())))
                .andExpect(jsonPath("$[1].refundValue", is(10)))
                .andExpect(jsonPath("$[1].value", is(10)));
    }
}