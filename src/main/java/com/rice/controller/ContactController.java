package com.rice.controller;

import com.rice.dto.auth.MessageResponse;
import com.rice.dto.contact.ContactRequest;
import com.rice.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public MessageResponse send(@Valid @RequestBody ContactRequest request) {
        contactService.send(request);
        return new MessageResponse("Message sent successfully");
    }
}
