package com.tenniscourts.guests;

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

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
public class GuestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GuestService guestService;

    @InjectMocks
    private GuestController guestController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(guestController).build();
    }

    @Test
    public void findGuestTest() throws Exception {
        GuestDTO guestDTO = GuestDTO.builder().id(1L).name("First Guest").build();
        Mockito.when(guestService.findById(1L)).thenReturn(guestDTO);
        mockMvc.perform(get("/guests/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("First Guest")));
    }

    @Test
    public void findGuestsTest() throws Exception {
        GuestDTO firstGuest = GuestDTO.builder().id(1L).name("First Guest").build();
        GuestDTO secondGuest = GuestDTO.builder().id(2L).name("Second Guest").build();
        List<GuestDTO> guests = Arrays.asList(firstGuest, secondGuest);
        Mockito.when(guestService.findAll()).thenReturn(guests);

        mockMvc.perform(get("/guests").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("First Guest")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Second Guest")));
    }

    @Test
    public void findsGuestsByPartialNameTest() throws Exception {
        GuestDTO firstGuest = GuestDTO.builder().id(1L).name("First Guest").build();
        GuestDTO secondGuest = GuestDTO.builder().id(2L).name("Second Guest").build();
        List<GuestDTO> guests = Arrays.asList(firstGuest, secondGuest);
        Mockito.when(guestService.findByPartialName("Second")).thenReturn(List.of(secondGuest));

        mockMvc.perform(get("/guests/search-in-name/Second").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].name", is("Second Guest")));
    }

    @Test
    public void createGuestTest() throws Exception {
        CreateGuestRequestDTO firstGuest = CreateGuestRequestDTO.builder().name("First Guest").build();
        Mockito.when(guestService.addGuest(firstGuest)).thenReturn(1L);

        mockMvc.perform(post("/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(firstGuest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("location", containsString("/guests/1")));
    }

    @Test
    public void updateGuestTest() throws Exception {
        GuestDTO firstGuest = GuestDTO.builder().id(1L).name("First Guest").build();
        Mockito.when(guestService.updateGuest(firstGuest)).thenReturn(firstGuest.getId());

        mockMvc.perform(put("/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(firstGuest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("location", containsString("/guests/1")));
    }

    @Test
    public void deleteGuestTest() throws Exception {
        mockMvc.perform(delete("/guests/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
