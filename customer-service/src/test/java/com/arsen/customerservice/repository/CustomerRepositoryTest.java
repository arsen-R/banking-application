package com.arsen.customerservice.repository;

import com.arsen.customerservice.model.entity.Address;
import com.arsen.customerservice.model.entity.ContactInfo;
import com.arsen.customerservice.model.entity.Customer;
import com.arsen.customerservice.model.entity.PersonalInfo;
import com.arsen.customerservice.model.enums.AddressType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class CustomerRepositoryTest {
    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldFindCustomerByAuthUserIdWithAddresses() {
        Customer customer = newCustomer("auth-1", "CUS-0000000001");
        customer.addAddress(new Address(AddressType.RESIDENTIAL, "1 Main St", null, "Springfield", null, "12345", "US"));
        customerRepository.saveAndFlush(customer);

        Optional<Customer> found = customerRepository.findByAuthUserId("auth-1");

        assertThat(found).isPresent();
        assertThat(found.get().getCustomerNumber()).isEqualTo("CUS-0000000001");
        assertThat(found.get().getAddresses()).hasSize(1);
        assertThat(found.get().getVersion()).isNotNull();
        assertThat(customerRepository.existsByCustomerNumber("CUS-0000000001")).isTrue();
    }

    @Test
    void shouldRejectDuplicateAuthUserId() {
        customerRepository.saveAndFlush(newCustomer("auth-2", "CUS-0000000002"));

        assertThatThrownBy(() -> customerRepository.saveAndFlush(newCustomer("auth-2", "CUS-0000000003")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static Customer newCustomer(String authUserId, String customerNumber) {
        return new Customer(authUserId, customerNumber,
                new PersonalInfo("Emily", null, "Ramirez", LocalDate.of(1990, 5, 20), "US", null),
                new ContactInfo("+12025550124", false, "emily@example.com"));
    }
}
