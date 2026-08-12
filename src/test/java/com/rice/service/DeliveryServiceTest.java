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
    void isServiceable_validAndPresent() {
        when(repo.findByPincode("500001")).thenReturn(Optional.of(new ServiceablePincode(1L, "500001")));
        assertTrue(service.isServiceable("500001"));
    }

    @Test
    void isServiceable_invalidFormat() {
        assertFalse(service.isServiceable("ABC"));
        assertFalse(service.isServiceable("123"));
    }

    @Test
    void addPincodes_savesOnlyNewValid() {
        when(repo.findByPincode("500001")).thenReturn(Optional.empty());
        when(repo.findByPincode("500002")).thenReturn(Optional.of(new ServiceablePincode(2L, "500002")));
        when(repo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<String> added = service.addPincodes(List.of("500001", "500002", "bad"));
        assertEquals(1, added.size());
        assertEquals("500001", added.get(0));
        ArgumentCaptor<List<ServiceablePincode>> cap = ArgumentCaptor.forClass(List.class);
        verify(repo).saveAll(cap.capture());
        assertEquals(1, cap.getValue().size());
        assertEquals("500001", cap.getValue().get(0).getPincode());
    }

    @Test
    void removePincode_deletesWhenPresent() {
        ServiceablePincode p = new ServiceablePincode(3L, "500003");
        when(repo.findByPincode("500003")).thenReturn(Optional.of(p));
        service.removePincode("500003");
        verify(repo).delete(p);
    }

    @Test
    void isProductServiceable_returnsTrueOnlyWhenProductExistsAndPincodeIsServiceable() {
        when(productRepository.existsById("p123")).thenReturn(true);
        when(repo.findByPincode("500001")).thenReturn(Optional.of(new ServiceablePincode(1L, "500001")));

        assertTrue(service.isProductServiceable("p123", "500001"));
        assertFalse(service.isProductServiceable("missing", "500001"));
    }
}
