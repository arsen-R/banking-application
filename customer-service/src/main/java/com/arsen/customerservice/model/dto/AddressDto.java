package com.arsen.customerservice.model.dto;

import com.arsen.customerservice.model.entity.Address;
import com.arsen.customerservice.model.enums.AddressType;

public record AddressDto(
        String id,
        AddressType type,
        String line1,
        String line2,
        String city,
        String region,
        String postalCode,
        String country
) {
    public static AddressDto from(Address address) {
        return new AddressDto(address.getId(), address.getType(), address.getLine1(), address.getLine2(),
                address.getCity(), address.getRegion(), address.getPostalCode(), address.getCountry());
    }
}
