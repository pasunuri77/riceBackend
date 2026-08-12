package com.rice.controller;

import com.rice.dto.delivery.AdminPincodesRequest;
import com.rice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AdminDeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/api/admin/delivery/pincodes")
    public List<String> list() {
        return deliveryService.listPincodes();
    }

    @PostMapping("/api/admin/delivery/pincodes")
    public List<String> add(@RequestBody AdminPincodesRequest req) {
        return deliveryService.addPincodes(req.getPincodes());
    }

    @DeleteMapping("/api/admin/delivery/pincodes/{pincode}")
    public void delete(@PathVariable String pincode) {
        deliveryService.removePincode(pincode);
    }

    @PostMapping(value = "/api/admin/delivery/pincodes/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<String> uploadCsv(@RequestParam("file") MultipartFile file) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> lines = br.lines()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> s.contains(",") ? s.split(",")[0].trim() : s)
                    .collect(Collectors.toList());
            return deliveryService.addPincodes(lines);
        }
    }

    @GetMapping(value = "/api/admin/delivery/pincodes/export", produces = "text/csv")
    public void exportCsv(HttpServletResponse resp) throws Exception {
        List<String> pincodes = deliveryService.listPincodes();
        resp.setContentType("text/csv");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.getWriter().write(pincodes.stream().collect(Collectors.joining("\n")));
        resp.getWriter().flush();
    }

    @GetMapping("/api/admin/delivery/products/{productId}/pincodes")
    public List<String> listProductPincodes(@PathVariable String productId) {
        return deliveryService.listProductPincodes(productId);
    }

    @PostMapping("/api/admin/delivery/products/{productId}/pincodes")
    public List<String> addProductPincodes(@PathVariable String productId, @RequestBody AdminPincodesRequest req) {
        return deliveryService.addProductPincodes(productId, req.getPincodes());
    }

    @DeleteMapping("/api/admin/delivery/products/{productId}/pincodes/{pincode}")
    public void deleteProductPincode(@PathVariable String productId, @PathVariable String pincode) {
        deliveryService.removeProductPincode(productId, pincode);
    }
}
