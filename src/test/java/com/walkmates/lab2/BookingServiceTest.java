package com.walkmates.lab2;

import com.walkmates.model.Booking;
import com.walkmates.model.BookingStatus;
import com.walkmates.model.Listing;
import com.walkmates.model.ListingStatus;
import com.walkmates.model.ListingType;
import com.walkmates.model.Provider;
import com.walkmates.model.Seeker;
import com.walkmates.repository.BookingRepository;
import com.walkmates.repository.ListingRepository;
import com.walkmates.repository.ProviderRepository;
import com.walkmates.repository.SeekerRepository;
import com.walkmates.service.BookingService;
import com.walkmates.service.NotificationService;
import com.walkmates.service.PricingCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.*;

// Lab 2 - tests for BookingService with mocks
class BookingServiceTest {

    SeekerRepository seekerRepo = mock(SeekerRepository.class);
    ListingRepository listingRepo = mock(ListingRepository.class);
    ProviderRepository providerRepo = mock(ProviderRepository.class);
    BookingRepository bookingRepo = mock(BookingRepository.class);
    NotificationService notification = mock(NotificationService.class);

    BookingService service = new BookingService(seekerRepo, listingRepo, providerRepo,
            bookingRepo, new PricingCalculator(), notification);

    Seeker seeker = new Seeker("Sam@example.com", "Sam", "0701234567");
    Provider provider = new Provider("Happy Paws", 59.33, 18.06);
    Listing listing = new Listing(provider.getId(), "Evening walk", "desc", ListingType.DOG_WALK);

    // 60 min dog walk for NEW seeker: 80 + 15% = 92
    @Test
    @DisplayName("booking works and notification is sent")
    void bookingWorks() {
        seeker.addFunds(500);
        when(seekerRepo.findById(seeker.getId())).thenReturn(Optional.of(seeker));
        when(listingRepo.findById(listing.getId())).thenReturn(Optional.of(listing));
        when(providerRepo.findById(provider.getId())).thenReturn(Optional.of(provider));

        Booking booking = service.createBooking(seeker.getId(), listing.getId(), 60);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getPrice()).isEqualTo(92.0);
        assertThat(seeker.getBalance()).isEqualTo(408.0);
        assertThat(listing.getStatus()).isEqualTo(ListingStatus.BOOKED);
        verify(notification).sendBookingConfirmed(seeker, booking);
    }

    @Test
    @DisplayName("listing not available is rejected")
    void listingNotAvailable() {
        seeker.addFunds(500);
        listing.transitionTo(ListingStatus.BOOKED);
        when(seekerRepo.findById(seeker.getId())).thenReturn(Optional.of(seeker));
        when(listingRepo.findById(listing.getId())).thenReturn(Optional.of(listing));

        assertThrows(BookingService.BookingRejectedException.class,
                () -> service.createBooking(seeker.getId(), listing.getId(), 60));
        verifyNoInteractions(notification);
    }

    // this one failed first because the code had > and not >=
    @Test
    @DisplayName("NEW seeker with 1 booking is rejected and max is 1")
    void newSeekerAtLimit() {
        seeker.addFunds(500);
        when(seekerRepo.findById(seeker.getId())).thenReturn(Optional.of(seeker));
        when(listingRepo.findById(listing.getId())).thenReturn(Optional.of(listing));
        when(providerRepo.findById(provider.getId())).thenReturn(Optional.of(provider));
        when(bookingRepo.findBySeekerId(seeker.getId()))
                .thenReturn(List.of(new Booking(seeker.getId(), "other", 60)));

        assertThrows(BookingService.BookingRejectedException.class,
                () -> service.createBooking(seeker.getId(), listing.getId(), 60));
        verifyNoInteractions(notification);
    }

    @Test
    @DisplayName("provider with 3 bookings is rejected")
    void providerFull() {
        seeker.addFunds(500);
        when(seekerRepo.findById(seeker.getId())).thenReturn(Optional.of(seeker));
        when(listingRepo.findById(listing.getId())).thenReturn(Optional.of(listing));
        when(providerRepo.findById(provider.getId())).thenReturn(Optional.of(provider));
        when(listingRepo.findByProviderId(provider.getId())).thenReturn(List.of(listing));
        when(bookingRepo.findByListingId(listing.getId())).thenReturn(List.of(
                new Booking("someone", listing.getId(), 60),
                new Booking("someone", listing.getId(), 60),
                new Booking("someone", listing.getId(), 60)));

        assertThrows(BookingService.BookingRejectedException.class,
                () -> service.createBooking(seeker.getId(), listing.getId(), 60));
        verifyNoInteractions(notification);
    }


    @Test
    @DisplayName("price = balance 92 is ok")
    void priceSameAsBalance() {
        seeker.addFunds(92);
        when(seekerRepo.findById(seeker.getId())).thenReturn(Optional.of(seeker));
        when(listingRepo.findById(listing.getId())).thenReturn(Optional.of(listing));
        when(providerRepo.findById(provider.getId())).thenReturn(Optional.of(provider));

        Booking booking = service.createBooking(seeker.getId(), listing.getId(), 60);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(seeker.getBalance()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("price more than balance will rejected")
    void notEnoughMoney() {
        seeker.addFunds(91.99);
        when(seekerRepo.findById(seeker.getId())).thenReturn(Optional.of(seeker));
        when(listingRepo.findById(listing.getId())).thenReturn(Optional.of(listing));
        when(providerRepo.findById(provider.getId())).thenReturn(Optional.of(provider));

        assertThrows(BookingService.BookingRejectedException.class,
                () -> service.createBooking(seeker.getId(), listing.getId(), 60));
        assertThat(seeker.getBalance()).isEqualTo(91.99);
    }

    @Test
    @DisplayName("wrong seeker id")
    void unknownSeeker() {
        assertThrows(BookingService.BookingRejectedException.class,
                () -> service.createBooking("wrong", listing.getId(), 60));
    }

    @Test
    @DisplayName("wrong listing id")
    void unknownListing() {
        when(seekerRepo.findById(seeker.getId())).thenReturn(Optional.of(seeker));

        assertThrows(BookingService.BookingRejectedException.class,
                () -> service.createBooking(seeker.getId(), "wrong", 60));
    }

    @Test
    @DisplayName("provider not found")
    void unknownProvider() {
        seeker.addFunds(500);
        when(seekerRepo.findById(seeker.getId())).thenReturn(Optional.of(seeker));
        when(listingRepo.findById(listing.getId())).thenReturn(Optional.of(listing));

        assertThrows(BookingService.BookingRejectedException.class,
                () -> service.createBooking(seeker.getId(), listing.getId(), 60));
    }
}