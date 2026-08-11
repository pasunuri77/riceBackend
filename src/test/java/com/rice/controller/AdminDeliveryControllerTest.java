package com.rice.controller;

import com.rice.dto.delivery.AdminPincodesRequest;
import com.rice.service.DeliveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminDeliveryControllerTest {

    private DeliveryService deliveryService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        deliveryService = mock(DeliveryService.class);
        AdminDeliveryController controller = new AdminDeliveryController(deliveryService);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void list_returnsJsonArray() throws Exception {
        when(deliveryService.listPincodes()).thenReturn(List.of("500001", "500002"));
        mvc.perform(get("/api/admin/delivery/pincodes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0]").value("500001"));
    }

    @Test
    void uploadCsv_parsesAndDelegates() throws Exception {
        when(deliveryService.addPincodes(anyList())).thenReturn(List.of("500001"));
        MockMultipartFile file = new MockMultipartFile("file", "pincodes.csv", "text/csv", "500001\n500002".getBytes());
        mvc.perform(multipart("/api/admin/delivery/pincodes/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("500001"));
        verify(deliveryService).addPincodes(anyList());
    }
}
