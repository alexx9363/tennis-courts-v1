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

    @InjectMocks
    private GuestService guestService;

    @Test
    public void findByIdTest() {
        Long guestId = 1L;
        Guest guest = Guest.builder().name("Any Guest").build();
        guest.setId(guestId);
        Mockito.when(guestRepository.getOne(guestId)).thenReturn(guest);

        Guest actualGuest = guestService.findById(guestId);

        Assertions.assertEquals(1L, actualGuest.getId());
        Assertions.assertEquals("Any Guest", actualGuest.getName());
    }

    @Test
    public void findAllTest() {
        Guest guest1 = Guest.builder().name("First Guest").build();
        guest1.setId(1L);
        Guest guest2 = Guest.builder().name("Second Guest").build();
        guest2.setId(2L);
        Mockito.when(guestRepository.findAll()).thenReturn(Arrays.asList(guest1, guest2));

        List<Guest> actualGuests = guestService.findAll();

        Guest firstGuest = actualGuests.get(0);
        Assertions.assertEquals(1L, firstGuest.getId());
        Assertions.assertEquals("First Guest", firstGuest.getName());
        Guest secondGuest = actualGuests.get(1);
        Assertions.assertEquals(2L, secondGuest.getId());
        Assertions.assertEquals("Second Guest", secondGuest.getName());
    }

    @Test
    public void findByNameTest() {
        Guest guest1 = Guest.builder().name("First Guest").build();
        guest1.setId(1L);
        Mockito.when(guestRepository.findAllByName("First Guest")).thenReturn(List.of(guest1));

        List<Guest> actualGuests = guestService.findByName("First Guest");

        Guest firstGuest = actualGuests.get(0);
        Assertions.assertEquals(1L, firstGuest.getId());
        Assertions.assertEquals("First Guest", firstGuest.getName());
    }

    @Test
    public void findByPartialNameTest() {
        Guest guest1 = Guest.builder().name("First Guest").build();
        guest1.setId(1L);
        Guest guest2 = Guest.builder().name("Second Guest").build();
        guest2.setId(2L);
        Guest someoneElse = Guest.builder().name("Someone Else").build();
        Mockito.when(guestRepository.findAllByNameContainsIgnoreCase("Guest")).thenReturn(Arrays.asList(guest1, guest2));

        List<Guest> actualGuests = guestService.findByPartialName("Guest");

        Guest firstGuest = actualGuests.get(0);
        Assertions.assertEquals(1L, firstGuest.getId());
        Assertions.assertEquals("First Guest", firstGuest.getName());
        Guest secondGuest = actualGuests.get(1);
        Assertions.assertEquals(2L, secondGuest.getId());
        Assertions.assertEquals("Second Guest", secondGuest.getName());
    }

    @Test
    public void addGuestTest() {
        Guest guest = Guest.builder().name("Second Guest").build();
        Mockito.when(guestRepository.saveAndFlush(guest)).thenReturn(guest);
        guest.setId(2L);

        Long actualId = guestService.addGuest(guest);

        Assertions.assertEquals(2L, actualId);
    }

    @Test
    public void updateGuestTest() {
        Guest guestWithUpdatedName = Guest.builder().name("Updated Guest").build();
        guestWithUpdatedName.setId(3L);
        Mockito.when(guestRepository.saveAndFlush(guestWithUpdatedName)).thenReturn(guestWithUpdatedName);

        Long actualId = guestService.updateGuest(guestWithUpdatedName);

        Assertions.assertEquals(3L, actualId);
    }

    @Test
    public void updateGuestTest_shouldThrowException() {
        Guest guestWithNoId = Guest.builder().name("guestWithNoId").build();
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> guestService.updateGuest(guestWithNoId));
        Assertions.assertEquals("Guest id is missing", exception.getMessage());
    }

    @Test
    public void deleteGuestTest() {
        Assertions.assertDoesNotThrow(() -> guestService.deleteGuest(1L));
    }
}