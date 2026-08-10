package com.rice.service;

import com.rice.exception.ApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TwilioVerifyServiceTest {

    private final TwilioVerifyService twilioVerifyService = new TwilioVerifyService();

    @Test
    void normalizePhoneNumberConvertsIndianTenDigitNumberToE164() {
        assertEquals("+919876543210", twilioVerifyService.normalizePhoneNumber("9876543210"));
    }

    @Test
    void normalizePhoneNumberPreservesExistingE164Format() {
        assertEquals("+919876543210", twilioVerifyService.normalizePhoneNumber("+919876543210"));
    }

    @Test
    void normalizePhoneNumberRejectsInvalidPhoneNumbers() {
        assertThrows(ApiException.class, () -> twilioVerifyService.normalizePhoneNumber("12345"));
    }
}
