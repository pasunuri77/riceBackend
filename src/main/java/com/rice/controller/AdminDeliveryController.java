package com.rice.controller;

import com.rice.dto.delivery.AdminPincodesRequest;
import com.rice.dto.delivery.PincodeDto;
import com.rice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or @permCheck.canManageDeliveryTax(authentication)")
public class AdminDeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/api/admin/delivery/pincodes")
    public List<PincodeDto> list() {
        return deliveryService.listPincodeDetails();
    }

    @PostMapping("/api/admin/delivery/pincodes")
    public List<PincodeDto> add(@RequestBody AdminPincodesRequest req) {
        if (req == null) return List.of();
        if (req.getItems() != null && !req.getItems().isEmpty()) {
            return deliveryService.addPincodeItems(req.getItems());
        }
        return deliveryService.addPincodesWithCity(req.getPincodes(), req.getCity());
    }

    @DeleteMapping("/api/admin/delivery/pincodes/{pincode}")
    public void delete(@PathVariable String pincode) {
        deliveryService.removePincode(pincode);
    }

    @PostMapping(value = "/api/admin/delivery/pincodes/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<PincodeDto> uploadCsv(@RequestParam("file") MultipartFile file) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> lines = br.lines()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            List<PincodeDto> items = new ArrayList<>();
            boolean isFirstLine = true;
            for (String line : lines) {
                String[] parts = line.split(",", -1);
                String rawPincode = parts[0].trim().replaceAll("^\"|\"$", "");
                if (isFirstLine) {
                    isFirstLine = false;
                    if (rawPincode.equalsIgnoreCase("pincode") || rawPincode.equalsIgnoreCase("zip") || rawPincode.equalsIgnoreCase("zipcode")) {
                        continue;
                    }
                }
                String city = parts.length > 1 ? parts[1].trim().replaceAll("^\"|\"$", "") : null;
                if (city != null && city.isBlank()) {
                    city = null;
                }
                items.add(new PincodeDto(rawPincode, city));
            }
            return deliveryService.addPincodeItems(items);
        }
    }

    @GetMapping(value = "/api/admin/delivery/pincodes/export", produces = "text/csv")
    public void exportCsv(HttpServletResponse resp) throws Exception {
        List<PincodeDto> pincodes = deliveryService.listPincodeDetails();
        resp.setContentType("text/csv");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        PrintWriter writer = resp.getWriter();
        writer.write("pincode,city\n");
        for (PincodeDto item : pincodes) {
            writer.write(item.getPincode() + "," + (item.getCity() != null ? item.getCity() : "") + "\n");
        }
        writer.flush();
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
