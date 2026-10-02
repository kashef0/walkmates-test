package com.walkmates.lab2;

import com.walkmates.model.Seeker;
import com.walkmates.repository.SeekerRepository;
import com.walkmates.service.NotificationService;
import com.walkmates.service.PaymentService;
import com.walkmates.service.SeekerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// topUp tests, PaymentService is a mock
class SeekerServiceTest {

    SeekerRepository seekerRepo = mock(SeekerRepository.class);
    PaymentService payment = mock(PaymentService.class);
    SeekerService service = new SeekerService(seekerRepo, payment, mock(NotificationService.class));

    Seeker seeker = new Seeker("test@example.com", "Test User", "0701234567");

    @Test
    @DisplayName("payment ok will wallet gets 100")
    void topUpOk() throws Exception {
        when(seekerRepo.findById("s1")).thenReturn(Optional.of(seeker));
        when(seekerRepo.save(seeker)).thenReturn(seeker);

        Seeker result = service.topUp("s1", "card", 100);

        assertThat(result).isSameAs(seeker);
        assertThat(seeker.getBalance()).isEqualTo(100.0);
        verify(seekerRepo).save(seeker);
    }

    @Test
    @DisplayName("payment declined will wallet still 0")
    void topUpDeclined() throws Exception {
        when(seekerRepo.findById("s1")).thenReturn(Optional.of(seeker));
        when(payment.charge("s1", "card", 100.0))
                .thenThrow(new PaymentService.PaymentException("declined"));

        assertThrows(PaymentService.PaymentException.class,
                () -> service.topUp("s1", "card", 100));
        assertThat(seeker.getBalance()).isEqualTo(0.0);
        verify(seekerRepo, never()).save(any(Seeker.class));
    }

    @Test
    @DisplayName("payment timeout will wallet still 0")
    void topUpTimeout() throws Exception {
        when(seekerRepo.findById("s1")).thenReturn(Optional.of(seeker));
        when(payment.charge("s1", "card", 100.0))
                .thenThrow(new PaymentService.PaymentTimeoutException("timeout"));

        assertThrows(PaymentService.PaymentTimeoutException.class,
                () -> service.topUp("s1", "card", 100));
        assertThat(seeker.getBalance()).isEqualTo(0.0);
        verify(seekerRepo, never()).save(any(Seeker.class));
    }

    @Test
    @DisplayName("wrong seeker id will payment not called")
    void topUpUnknownSeeker() {
        assertThrows(IllegalArgumentException.class,
                () -> service.topUp("wrong", "card", 100));
        verifyNoInteractions(payment);
    }
}