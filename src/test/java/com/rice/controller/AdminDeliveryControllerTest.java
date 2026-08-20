package com.rice.controller;

import com.rice.dto.delivery.AdminPincodesRequest;
import com.rice.dto.delivery.PincodeDto;
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
        when(deliveryService.listPincodeDetails()).thenReturn(List.of(
                new PincodeDto("78664", "Round Rock"),
                new PincodeDto("78701", "Austin")
        ));
        mvc.perform(get("/api/admin/delivery/pincodes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].pincode").value("78664"))
                .andExpect(jsonPath("$[0].city").value("Round Rock"));
    }

    @Test
    void uploadCsv_parsesAndDelegates() throws Exception {
        when(deliveryService.addPincodeItems(anyList())).thenReturn(List.of(
                new PincodeDto("78664", "Round Rock")
        ));
        MockMultipartFile file = new MockMultipartFile("file", "pincodes.csv", "text/csv", "pincode,city\n78664,Round Rock\n78665,Round Rock".getBytes());
        mvc.perform(multipart("/api/admin/delivery/pincodes/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pincode").value("78664"))
                .andExpect(jsonPath("$[0].city").value("Round Rock"));
        verify(deliveryService).addPincodeItems(anyList());
    }

    @Test
    void exportCsv_returnsCsvWithHeaderAndCity() throws Exception {
        when(deliveryService.listPincodeDetails()).thenReturn(List.of(
                new PincodeDto("78664", "Round Rock"),
                new PincodeDto("78701", null)
        ));
        mvc.perform(get("/api/admin/delivery/pincodes/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string("pincode,city\n78664,Round Rock\n78701,\n"));
    }
}
