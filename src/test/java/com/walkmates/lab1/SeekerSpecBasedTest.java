package com.walkmates.lab1;

import com.walkmates.model.Seeker;
import com.walkmates.model.TrustTier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;

/**
 * Lab 1, Part B — specification-based tests for {@link Seeker}.
 *
 * <p>Design your tests on paper first (equivalence partitions, boundary values, decision table)
 * from {@code docs/REQUIREMENTS.md} FR-1.1 / FR-1.3 / FR-1.2, then implement them here. One
 * worked example is provided; the {@code TODO}s are yours.</p>
 */
class SeekerSpecBasedTest {

    // ---- Worked example: boundary value at the maximum single top-up (FR-1.3) ----
    @Test
    @DisplayName("Top-up exactly at the 5000 SEK single-transaction maximum is accepted")
    void topUpAtSingleMaximumIsAccepted() {
        Seeker seeker = new Seeker("sam@example.com", "Sam", "0707654321");

        seeker.addFunds(Seeker.MAX_SINGLE_TOP_UP); // 5000.00, the boundary value

        assertThat(seeker.getBalance()).isEqualTo(Seeker.MAX_SINGLE_TOP_UP);
    }
    
    
    // TODO (EP): one valid + one invalid equivalence class for email, name, and phone (FR-1.1).
    @Test
    @DisplayName("Valid email is accepted at registration")
    void validEmailIsAccepted() {
            Seeker seeker = new Seeker("exampel@gmail.com", "Sam", "0707654321");
            assertThat(seeker.getEmail()).isEqualTo("exampel@gmail.com");
    }

    @Test
    @DisplayName("Email with exactly 254 characters is accepted")
    void emailAtMaximumLengthIsAccepted() {
        String email = "a".repeat(242) + "@example.com";

        Seeker seeker = new Seeker(email, "Sam", "0707654321");

        assertThat(seeker.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("Missing @ symbol in email is rejected at registration")
    void invalidEmailIsRejected() {
        // Example of the shape; expand into your full EP set.
        assertThrows(IllegalArgumentException.class,
                () -> new Seeker("not-an-email", "Sam", "0707654321"));
    }
    @Test
    @DisplayName("Empty email is rejected at registration")
    void emptyEmailIsRejected() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Seeker("", "Sam",  "0707654321"));
    }

    @Test
    @DisplayName("Null email is rejected at registration")
    void nullEmailIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Seeker(null, "Sam", "0707654321"));
    }
    
    @Test
    @DisplayName("Email with 255 or more characters is rejected")
    void emailAboveMaximumLengthIsRejected() {
        String email = "a".repeat(243) + "@example.com";

        assertThrows(IllegalArgumentException.class, 
            () -> new Seeker(email, "Sam", "0707654321")
        );
    }

    // all valid and invalid names's tests 
    @Test
    @DisplayName("Name with exactly 2 characters is accepted")
    void validDisplayNameWithTwoCHarIsAccepted() {
            // A name containing allowed characters minlength 2
            Seeker seeker1 = new Seeker("exampel@gmail.com", "Bo", "0707654321");
            assertThat(seeker1.getDisplayName()).isEqualTo("Bo");

            // A name containing allowed characters maxlength 40
            Seeker seeker2 = new Seeker("exampel@gmail.com", "Christopher Alexander-Michael O'Connor J", "0707654321");
            assertThat(seeker2.getDisplayName()).isEqualTo("Christopher Alexander-Michael O'Connor J");

            // A name containing allowed characters (hyphen, apostrophe, and space).
            Seeker seeker3 = new Seeker("exampel@gmail.com", "Anne-Marie O'Connor", "0707654321");
            assertThat(seeker3.getDisplayName()).isEqualTo("Anne-Marie O'Connor");
    }
    @Test
    @DisplayName("Name with 1 character is rejected")
    void nameBelowMinimumIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Seeker("example@gmail.com", "A", "0707654321"));
    }

    @Test
    @DisplayName("Name longer than 40 characters is rejected")
    void nameAboveMaximumIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Seeker("example@gmail.com", "Christopher Alexander-Michael O'Connor JX", "0707654321"));
    }

    @Test
    @DisplayName("Empty name is rejected")
    void emptyNameIsRejected() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Seeker("example@gmail.com", "", "0707654321"));
    }

    @Test
    @DisplayName("Null name is rejected")
    void nullNameIsRejected() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Seeker("example@gmail.com", null, "0707654321"));
    }

    @Test
    @DisplayName("Name containing unsupported characters is rejected")
    void nameWithInvalidCharactersIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Seeker("example@gmail.com", "Sam123", "0707654321"));
    }

    // all valid and invalid phonenumber's tests 
    @Test
    @DisplayName("Valid PhoneNumber is accepted at registration")
    void validPhoneNumbersAccepted() {
            Seeker seeker = new Seeker("exampel@gmail.com", "Sam", "0707654321");
            assertThat(seeker.getPhoneNumber()).isEqualTo("0707654321");
    }

    @Disabled("BUG-01: FR-1.1 requires +467 + 8 digits Seeker only accepts 7")
    @Test
    @DisplayName("Swedish international phone number is accepted")
    void internationalPhoneNumberIsAccepted() {
        Seeker seeker = new Seeker("example@gmail.com","Sam","+46707654321"
        );

        assertThat(seeker.getPhoneNumber()).isEqualTo("+46707654321");
    }


    @Test
    @DisplayName("Phone number that does not start with 07 is rejected")
    void phoneNumberWithout07IsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Seeker("exampel@gmail.com", "Sam", "0937654321"));
    }

    @Test
    @DisplayName("Phone number longer than 10 digits is rejected")
    void phoneNumberLongerThan10DigitsIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Seeker("exampel@gmail.com", "Sam", "073765432123"));
    }

    @Test
    @DisplayName("Phone number shorter than 10 digits is rejected")
    void phoneNumberShorterThan10DigitsIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Seeker("exampel@gmail.com", "Sam", "09376"));
    }

    @Test
    @DisplayName("Null phone number is rejected")
    void nullPhoneNumberIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Seeker("example@gmail.com", "Sam", null));
    }


    // TODO (BVA): just-below / at / just-above the 10.00 minimum top-up (FR-1.3).
    
    @Test 
    @DisplayName("Top up at and just above 10.00 SEK minimum is accepted")
    void topUpAtAndJustAboveMinimumIsAccepted() {
        Seeker seeker = new Seeker("sam@example.com", "Sam", "0707654321");

        // exactly at the 10.00 SEK minimum
        seeker.addFunds(10.00);
        assertThat(seeker.getBalance()).isEqualTo(10.00);

        // just above the 10.00 SEK minimum
        seeker.addFunds(10.01);
        assertThat(seeker.getBalance()).isEqualTo(20.01);
    }


    // TODO (BVA): a top-up that would push the balance above 20000.00 is rejected (FR-1.3).
    @Test
    @DisplayName("Top up that results in exactly 20000.00 SEK balance is accepted")
    void topUpReachingMaxBalanceIsAccepted() {
        Seeker seeker = new Seeker("sam@example.com", "Sam", "0707654321");

        seeker.addFunds(5000.00);
        seeker.addFunds(5000.00);
        seeker.addFunds(5000.00);
        seeker.addFunds(5000.00);

        assertThat(seeker.getBalance()).isEqualTo(20000.00);
    }

    @Test
    @DisplayName("Balance just below the 20000.00 SEK maximum is accepted")
    void balanceJustBelowMaxIsAccepted() {
        Seeker seeker = new Seeker("sam@example.com", "Sam", "0707654321");
        seeker.addFunds(5000.00);
        seeker.addFunds(5000.00);
        seeker.addFunds(5000.00);
        seeker.addFunds(4999.99);
        assertThat(seeker.getBalance()).isEqualTo(19999.99);
    }
    @Test
    @DisplayName("Top up just below 5000 SEK is accepted")
    void topUpJustBelowSingleMaximumIsAccepted() {
        Seeker seeker = new Seeker(
                "example@gmail.com", "Sam", "0707654321");

        seeker.addFunds(4999.99);

        assertThat(seeker.getBalance()).isEqualTo(4999.99);
    }

    @Test
    @DisplayName("Charging an amount within the balance is accepted")
    void chargeWithinBalanceIsAccepted() {
        Seeker seeker = new Seeker("example@gmail.com", "Sam", "0707654321");
        seeker.addFunds(100.00);

        seeker.charge(50.00);

        assertThat(seeker.getBalance()).isEqualTo(50.00);
    }

    @Test
    @DisplayName("Top up just below 10.00 SEK minimum is rejected")
    void topUpJustBelowMinimumIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> {
            Seeker seeker = new Seeker("sam@example.com", "Sam", "0707654321");
            // just-below: 9.99 SEK
            seeker.addFunds(9.99);
        });
    }

    @Test
    @DisplayName("Top up above 5000 SEK is rejected")
    void topUpAboveSingleMaximumIsRejected() {
        Seeker seeker = new Seeker(
                "example@gmail.com", "Sam", "0707654321");

        assertThrows(IllegalArgumentException.class,
                () -> seeker.addFunds(5000.01));
    }

    @Test
    @DisplayName("Top up that causes total balance to exceed 20000.00 SEK is rejected")
    void topUpExceedingMaxBalanceIsRejected() {
        Seeker seeker = new Seeker("sam@example.com", "Sam", "0707654321");

        seeker.addFunds(5000.00);
        seeker.addFunds(5000.00);
        seeker.addFunds(5000.00);
        seeker.addFunds(3000.00);

        assertThrows(IllegalArgumentException.class, () -> seeker.addFunds(2000.01));
    }

    @Test
    @DisplayName("Charging a negative amount is rejected")
    void negativeChargeIsRejected() {
        Seeker seeker = new Seeker("example@gmail.com", "Sam", "0707654321");

        assertThrows(IllegalArgumentException.class,
                () -> seeker.charge(-1.00));
    }

    @Test
    @DisplayName("Charging more than the current balance is rejected")
    void chargeAboveBalanceIsRejected() {
        Seeker seeker = new Seeker("example@gmail.com", "Sam", "0707654321");
        seeker.addFunds(100.00);

        assertThrows(IllegalArgumentException.class,
                () -> seeker.charge(100.01));
    }

    @Test
    @DisplayName("Charging exactly the full balance leaves 0.00 SEK")
    void chargeOfFullBalanceIsAccepted() {
        Seeker seeker = new Seeker("example@gmail.com", "Sam", "0707654321");
        seeker.addFunds(100.00);
        seeker.charge(100.00);
        assertThat(seeker.getBalance()).isEqualTo(0.00);
    }

    // TODO (Decision table): expected fee + max-bookings for each trust tier (FR-1.2).
    @Test
    @DisplayName("Rule1: New tier limits")
    void newTier() {
        Seeker seeker = new Seeker("example@gmail.com", "Sam", "0707654321");

        seeker.setTrustTier(TrustTier.NEW);

        assertThat(seeker.getMaxConcurrentBookings()).isEqualTo(1);
        assertThat(seeker.getTrustTier().getPlatformFee()).isEqualTo(0.15);
    }

    @Test
    @DisplayName("Rule2: VERIFIED tier limits")
    void verifiedTier() {
        Seeker seeker = new Seeker("example@gmail.com", "Sam", "0707654321");

        seeker.setTrustTier(TrustTier.VERIFIED);

        assertThat(seeker.getMaxConcurrentBookings()).isEqualTo(3);
        assertThat(seeker.getTrustTier().getPlatformFee()).isEqualTo(0.12);
    }

    @Test
    @DisplayName("Rule3: Trusted tier limits")
    void trustedTier() {
        Seeker seeker = new Seeker("example@gmail.com", "Sam", "0707654321");

        seeker.setTrustTier(TrustTier.TRUSTED);

        assertThat(seeker.getMaxConcurrentBookings()).isEqualTo(5);
        assertThat(seeker.getTrustTier().getPlatformFee()).isEqualTo(0.08);
    }

    @Test
    @DisplayName("Rule4: Pro_Sitter tier limits")
    void proSitterTier() {
        Seeker seeker = new Seeker("example@gmail.com", "Sam", "0707654321");

        seeker.setTrustTier(TrustTier.PRO_SITTER);

        assertThat(seeker.getMaxConcurrentBookings()).isEqualTo(10);
        assertThat(seeker.getTrustTier().getPlatformFee()).isEqualTo(0.05);
    }
}
