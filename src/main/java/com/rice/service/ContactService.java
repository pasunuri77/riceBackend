package com.rice.service;

import com.rice.dto.contact.ContactRequest;
import com.rice.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final EmailService emailService;

    public void send(ContactRequest request) {
        emailService.sendContactMessage(
                request.getName(),
                request.getEmail(),
                request.getSubject(),
                request.getMessage()
        );
    }
}
