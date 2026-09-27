package com.arsen.customerservice.service;

import com.arsen.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class CustomerNumberGenerator {
    private static final String PREFIX = "CUS-";
    private static final long BOUND = 10_000_000_000L;

    private final CustomerRepository customerRepository;
    private final SecureRandom random = new SecureRandom();

    public String next() {
        String number;
        do {
            number = PREFIX + String.format("%010d", random.nextLong(BOUND));
        } while (customerRepository.existsByCustomerNumber(number));
        return number;
    }
}
