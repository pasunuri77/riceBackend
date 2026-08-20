package com.rice.service;

import com.rice.dto.delivery.DeliveryAreaResponse;
import com.rice.dto.delivery.PincodeDto;
import com.rice.entity.DeliveryZone;
import com.rice.entity.ServiceablePincode;
import com.rice.repository.DeliveryZoneRepository;
import com.rice.repository.ProductRepository;
import com.rice.repository.ServiceablePincodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeliveryServiceTest {

    private ServiceablePincodeRepository repo;
    private ProductRepository productRepository;
    private DeliveryZoneRepository deliveryZoneRepository;
    private DeliveryService service;

    @BeforeEach
    void setUp() {
        repo = mock(ServiceablePincodeRepository.class);
        productRepository = mock(ProductRepository.class);
        deliveryZoneRepository = mock(DeliveryZoneRepository.class);
        service = new DeliveryService(repo, productRepository, null, null, deliveryZoneRepository);
    }

    @Test
    void isServiceable_validUsZipAndPresent() {
        when(repo.findByPincode("12345")).thenReturn(Optional.of(new ServiceablePincode(1L, "12345", null, true, false)));
        when(repo.findByPincode("12345-6789")).thenReturn(Optional.of(new ServiceablePincode(2L, "12345", null, true, false)));

        assertTrue(service.isServiceable("12345"));
        assertTrue(service.isServiceable("12345-6789"));
    }

    @Test
    void isServiceable_invalidFormat() {
        assertFalse(service.isServiceable("ABC"));
        assertFalse(service.isServiceable("123"));
        assertFalse(service.isServiceable("123456"));
    }

    @Test
    void addPincodes_savesOnlyNewValid() {
        when(repo.findByPincode("12345")).thenReturn(Optional.empty());
        when(repo.findByPincode("12345-6789")).thenReturn(Optional.of(new ServiceablePincode(2L, "12345", null, true, false)));
        when(repo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<String> added = service.addPincodes(List.of("12345", "12345-6789", "bad"));
        assertEquals(1, added.size());
        assertEquals("12345", added.get(0));
        ArgumentCaptor<List<ServiceablePincode>> cap = ArgumentCaptor.forClass(List.class);
        verify(repo).saveAll(cap.capture());
        assertEquals(1, cap.getValue().size());
        assertEquals("12345", cap.getValue().get(0).getPincode());
    }

    @Test
    void removePincode_deletesWhenPresent() {
        ServiceablePincode p = new ServiceablePincode(3L, "50000", null, true, false);
        when(repo.findByPincode("50000")).thenReturn(Optional.of(p));
        service.removePincode("50000");
        verify(repo).delete(p);
    }

    @Test
    void isProductServiceable_returnsTrueOnlyWhenProductExistsAndPincodeIsServiceable() {
        when(productRepository.existsById("p123")).thenReturn(true);
        when(repo.findByPincode("12345")).thenReturn(Optional.of(new ServiceablePincode(1L, "12345", null, true, false)));

        assertTrue(service.isProductServiceable("p123", "12345"));
        assertFalse(service.isProductServiceable("missing", "12345"));
    }

    @Test
    void getDeliveryAreas_groupsByCityAndLeftoverGreaterAustin() {
        DeliveryZone dz1 = DeliveryZone.builder().id(1L).name("Downtown Austin").description("Downtown").active(true).build();
        when(deliveryZoneRepository.findByActiveTrue()).thenReturn(List.of(dz1));

        ServiceablePincode p1 = ServiceablePincode.builder().id(1L).pincode("78701").zone(dz1).active(true).isNamedZone(true).build();
        ServiceablePincode p2 = ServiceablePincode.builder().id(2L).pincode("78664").city("Round Rock").active(true).isNamedZone(false).build();
        ServiceablePincode p3 = ServiceablePincode.builder().id(3L).pincode("78665").city("Round Rock").active(true).isNamedZone(false).build();
        ServiceablePincode p4 = ServiceablePincode.builder().id(4L).pincode("78660").city("Pflugerville").active(true).isNamedZone(false).build();
        ServiceablePincode p5 = ServiceablePincode.builder().id(5L).pincode("78721").city(null).active(true).isNamedZone(false).build();

        when(repo.findAll()).thenReturn(List.of(p1, p2, p3, p4, p5));

        List<DeliveryAreaResponse> areas = service.getDeliveryAreas();
        assertEquals(4, areas.size());

        // Named zone
        assertEquals("Downtown Austin", areas.get(0).getName());
        assertTrue(areas.get(0).isNamedZone());
        assertEquals(List.of("78701"), areas.get(0).getZipCodes());

        // Cities
        assertEquals("Round Rock", areas.get(1).getName());
        assertFalse(areas.get(1).isNamedZone());
        assertEquals(List.of("78664", "78665"), areas.get(1).getZipCodes());

        assertEquals("Pflugerville", areas.get(2).getName());
        assertFalse(areas.get(2).isNamedZone());
        assertEquals(List.of("78660"), areas.get(2).getZipCodes());

        // Leftover Greater Austin
        assertEquals("Greater Austin", areas.get(3).getName());
        assertEquals(999L, areas.get(3).getId());
        assertFalse(areas.get(3).isNamedZone());
        assertEquals(List.of("78721"), areas.get(3).getZipCodes());
    }

    @Test
    void addPincodeItems_updatesExistingAndAddsNewWithCity() {
        ServiceablePincode existing = ServiceablePincode.builder().id(10L).pincode("78664").city(null).active(true).build();
        when(repo.findByPincode("78664")).thenReturn(Optional.of(existing));
        when(repo.findByPincode("78665")).thenReturn(Optional.empty());
        when(repo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<PincodeDto> result = service.addPincodeItems(List.of(
                new PincodeDto("78664", "Round Rock"),
                new PincodeDto("78665", "Round Rock")
        ));

        assertEquals(2, result.size());
        assertEquals("Round Rock", result.get(0).getCity());
        assertEquals("Round Rock", result.get(1).getCity());
    }
}
