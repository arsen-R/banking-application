package com.arsen.customerservice.messaging;

public final class EventTypes {
    public static final String USER_REGISTERED = "UserRegistered";
    public static final String CUSTOMER_CREATED = "CustomerCreated";
    public static final String CUSTOMER_CREATION_FAILED = "CustomerCreationFailed";
    public static final String CUSTOMER_UPDATED = "CustomerUpdated";
    public static final String CUSTOMER_KYC_APPROVED = "CustomerKycApproved";
    public static final String CUSTOMER_KYC_REJECTED = "CustomerKycRejected";

    private EventTypes() {
    }
}
