package com.rice.service;

import com.rice.dto.customer.CustomerResponse;
import com.rice.entity.User;
import com.rice.entity.enums.Role;
import com.rice.entity.enums.UserStatus;
import com.rice.exception.ApiException;
import com.rice.repository.OrderRepository;
import com.rice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    public List<CustomerResponse> list() {
        return userRepository.findByRole(Role.USER).stream().map(this::toResponse).toList();
    }

    public CustomerResponse getById(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public CustomerResponse updateStatus(Long id, String status) {
        User user = find(id);
        user.setStatus("Blocked".equalsIgnoreCase(status) ? UserStatus.BLOCKED : UserStatus.ACTIVE);
        return toResponse(user);
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
