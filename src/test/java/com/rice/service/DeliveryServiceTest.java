package com.rice.service;

import com.rice.entity.ServiceablePincode;
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
    private DeliveryService service;

    @BeforeEach
    void setUp() {
        repo = mock(ServiceablePincodeRepository.class);
        productRepository = mock(ProductRepository.class);
        service = new DeliveryService(repo, productRepository);
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
}
