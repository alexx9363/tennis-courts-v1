package com.tenniscourts.guests;

import com.tenniscourts.reservations.ReservationService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = ReservationService.class)
public class GuestServiceTest {

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private GuestMapper guestMapper;

    @InjectMocks
    private GuestService guestService;

    @Test
    public void findByIdTest() {
        Long guestId = 1L;
        Guest guest = Guest.builder().name("Any Guest").build();
        guest.setId(guestId);
        Mockito.when(guestRepository.getOne(guestId)).thenReturn(guest);
        GuestDTO guestDTO = GuestDTO.builder().id(guestId).name("Any Guest").build();
        Mockito.when(guestMapper.map(guest)).thenReturn(guestDTO);

        GuestDTO actualGuestDTO = guestService.findById(guestId);

        Assertions.assertEquals(1L, actualGuestDTO.getId());
        Assertions.assertEquals("Any Guest", actualGuestDTO.getName());
    }

    @Test
    public void findAllTest() {
        Guest guest1 = Guest.builder().name("First Guest").build();
        Guest guest2 = Guest.builder().name("Second Guest").build();
        Mockito.when(guestRepository.findAll()).thenReturn(Arrays.asList(guest1, guest2));
        Mockito.when(guestMapper.map(guest1)).thenReturn(GuestDTO.builder().id(1L).name("First Guest").build());
        Mockito.when(guestMapper.map(guest2)).thenReturn(GuestDTO.builder().id(2L).name("Second Guest").build());

        List<GuestDTO> actualGuestDTOs = guestService.findAll();

        GuestDTO firstGuest = actualGuestDTOs.get(0);
        Assertions.assertEquals(1L, firstGuest.getId());
        Assertions.assertEquals("First Guest", firstGuest.getName());
        GuestDTO secondGuest = actualGuestDTOs.get(1);
        Assertions.assertEquals(2L, secondGuest.getId());
        Assertions.assertEquals("Second Guest", secondGuest.getName());
    }

    @Test
    public void findByNameTest() {
        Guest guest1 = Guest.builder().name("First Guest").build();
        Guest guest2 = Guest.builder().name("Second Guest").build();
        Mockito.when(guestRepository.findAllByName("First Guest")).thenReturn(List.of(guest1));
        Mockito.when(guestMapper.map(guest1)).thenReturn(GuestDTO.builder().id(1L).name("First Guest").build());

        List<GuestDTO> actualGuestDTOs = guestService.findByName("First Guest");

        GuestDTO firstGuest = actualGuestDTOs.get(0);
        Assertions.assertEquals(1L, firstGuest.getId());
        Assertions.assertEquals("First Guest", firstGuest.getName());
    }

    @Test
    public void findByPartialNameTest() {
        Guest guest1 = Guest.builder().name("First Guest").build();
        Guest guest2 = Guest.builder().name("Second Guest").build();
        Guest someoneElse = Guest.builder().name("Someone Else").build();
        Mockito.when(guestRepository.findAllByNameContainsIgnoreCase("Guest")).thenReturn(Arrays.asList(guest1, guest2));
        Mockito.when(guestMapper.map(guest1)).thenReturn(GuestDTO.builder().id(1L).name("First Guest").build());
        Mockito.when(guestMapper.map(guest2)).thenReturn(GuestDTO.builder().id(2L).name("Second Guest").build());

        List<GuestDTO> actualGuestDTOs = guestService.findByPartialName("Guest");

        GuestDTO firstGuest = actualGuestDTOs.get(0);
        Assertions.assertEquals(1L, firstGuest.getId());
        Assertions.assertEquals("First Guest", firstGuest.getName());
        GuestDTO secondGuest = actualGuestDTOs.get(1);
        Assertions.assertEquals(2L, secondGuest.getId());
        Assertions.assertEquals("Second Guest", secondGuest.getName());
    }

    @Test
    public void addGuestTest() {
        Guest guest = Guest.builder().name("Second Guest").build();
        Mockito.when(guestRepository.saveAndFlush(guest)).thenReturn(guest);
        guest.setId(2L);
        CreateGuestRequestDTO createGuestRequestDTO = CreateGuestRequestDTO.builder().name("Second Guest").build();
        Mockito.when(guestMapper.map(createGuestRequestDTO)).thenReturn(guest);

        Long actualId = guestService.addGuest(createGuestRequestDTO);

        Assertions.assertEquals(2L, actualId);
    }

    @Test
    public void updateGuestTest() {
        GuestDTO guestWithUpdatedNameDTO = GuestDTO.builder().id(3L).name("Updated Guest").build();
        Guest guestWithUpdatedName = Guest.builder().name("Updated Guest").build();
        guestWithUpdatedName.setId(3L);
        Mockito.when(guestMapper.map(guestWithUpdatedNameDTO)).thenReturn(guestWithUpdatedName);
        Mockito.when(guestRepository.saveAndFlush(guestWithUpdatedName)).thenReturn(guestWithUpdatedName);

        Long actualId = guestService.updateGuest(guestWithUpdatedNameDTO);

        Assertions.assertEquals(3L, actualId);
    }

    @Test
    public void updateGuestTest_shouldThrowException() {
        GuestDTO guestWithNoId = GuestDTO.builder().name("guestWithNoId").build();
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> guestService.updateGuest(guestWithNoId));
        Assertions.assertEquals("Guest id is missing", exception.getMessage());
    }

    @Test
    public void deleteGuestTest() {
        Assertions.assertDoesNotThrow(() -> guestService.deleteGuest(1L));
    }
}