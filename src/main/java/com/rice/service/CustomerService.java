package com.rice.service;

import com.rice.dto.customer.CreateCustomerRequest;
import com.rice.dto.customer.CustomerResponse;
import com.rice.entity.PasswordResetToken;
import com.rice.entity.User;
import com.rice.entity.enums.Role;
import com.rice.entity.enums.UserStatus;
import com.rice.exception.ApiException;
import com.rice.repository.OrderRepository;
import com.rice.repository.PasswordResetTokenRepository;
import com.rice.repository.UserRepository;
import com.rice.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    public List<CustomerResponse> list() {
        return userRepository.findByRoleOrderByCreatedAtDesc(Role.USER).stream().map(this::toResponse).toList();
    }

    public CustomerResponse getById(Long id) {
        return toResponse(find(id));
    }

    /**
     * Create a customer on behalf of them (walk-in/phone order).
     * Generates a random temp password and sends a password-reset link email.
     */
    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        // Check email doesn't exist
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("Email already registered: " + request.getEmail());
        }

        // Check phone doesn't exist (optional)
        if (request.getMobile() != null && !request.getMobile().isBlank() 
            && userRepository.existsByPhone(request.getMobile())) {
            throw ApiException.conflict("Mobile number already registered: " + request.getMobile());
        }

        // Create user with random temp password
        String tempPassword = UUID.randomUUID().toString();
        User user = User.builder()
                .name(request.getFullName())
                .email(request.getEmail())
                .phone(request.getMobile())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User saved = userRepository.save(user);

        // Generate password-reset token for 24h
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(saved)
                .token(token)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();
        tokenRepository.save(resetToken);

        // Send invitation email
        String resetLink = frontendBaseUrl + "/set-password?token=" + token;
        emailService.sendInvitationEmail(saved.getEmail(), saved.getName(), resetLink, "customer");

        return toResponse(saved);
    }

    @Transactional
    public CustomerResponse updateStatus(Long id, String status) {
        User user = find(id);
        user.setStatus("Blocked".equalsIgnoreCase(status) ? UserStatus.BLOCKED : UserStatus.ACTIVE);
        userRepository.save(user);
        return toResponse(user);
    }

    /**
     * Delete a customer account.
     */
    @Transactional
    public void delete(Long id) {
        User user = find(id);
        userRepository.delete(user);
    }

    private User find(Long id) {
        return userRepository.findById(id)
                .filter(u -> u.getRole() == Role.USER)
                .orElseThrow(() -> ApiException.notFound("Customer not found: " + id));
    }

    private CustomerResponse toResponse(User u) {
        return CustomerResponse.builder()
                .id(u.getId().toString())
                .name(u.getName())
                .email(u.getEmail())
                .mobile(u.getPhone())
                .orders(orderRepository.countByCustomerId(u.getId()))
                .totalSpent(orderRepository.totalSpentByCustomer(u.getId()))
                .joined(DATE_FORMAT.format(u.getCreatedAt().atZone(ZoneOffset.UTC)))
                .status(u.getStatus() == UserStatus.ACTIVE ? "Active" : "Blocked")
                .build();
    }
}
