package com.arsen.customerservice.controller;

import com.arsen.customerservice.model.dto.CustomerDto;
import com.arsen.customerservice.model.request.AddressRequest;
import com.arsen.customerservice.model.request.IdentityDocumentRequest;
import com.arsen.customerservice.model.request.KycRejectRequest;
import com.arsen.customerservice.model.request.UpdateCustomerRequest;
import com.arsen.customerservice.security.AuthenticatedUser;
import com.arsen.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping("/me")
    public CustomerDto getMe(@AuthenticationPrincipal AuthenticatedUser user) {
        return customerService.getByAuthUserId(user.userId());
    }

    @PutMapping("/me")
    public CustomerDto updateMe(@AuthenticationPrincipal AuthenticatedUser user,
                                @Valid @RequestBody UpdateCustomerRequest request) {
        return customerService.updateByAuthUserId(user.userId(), request);
    }

    @PostMapping("/me/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDto addAddress(@AuthenticationPrincipal AuthenticatedUser user,
                                  @Valid @RequestBody AddressRequest request) {
        return customerService.addAddress(user.userId(), request);
    }

    @PostMapping("/me/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDto addDocument(@AuthenticationPrincipal AuthenticatedUser user,
                                   @Valid @RequestBody IdentityDocumentRequest request) {
        return customerService.addIdentityDocument(user.userId(), request);
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER')")
    public CustomerDto getById(@PathVariable String customerId) {
        return customerService.getById(customerId);
    }

    @PostMapping("/{customerId}/kyc/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER')")
    public CustomerDto approveKyc(@PathVariable String customerId) {
        return customerService.approveKyc(customerId);
    }

    @PostMapping("/{customerId}/kyc/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER')")
    public CustomerDto rejectKyc(@PathVariable String customerId, @Valid @RequestBody KycRejectRequest request) {
        return customerService.rejectKyc(customerId, request.reason());
    }
}
