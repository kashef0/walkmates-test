package com.walkmates.lab3;

import com.walkmates.model.Listing;
import com.walkmates.model.ListingType;
import com.walkmates.model.Seeker;
import com.walkmates.repository.ListingRepository;
import com.walkmates.repository.SeekerRepository;
import com.walkmates.service.ai.MatchExplanationService;
import com.walkmates.web.MatchController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lab 3, Part A (interface rung) — testing the AI feature through its HTTP boundary with
 * {@code MockMvc}, with the service/repositories mocked. This is the "test the interface, not a
 * live model" example. One worked test is provided (the 404 path); extend it to the success and
 * fallback paths.
 */
@WebMvcTest(MatchController.class)
class MatchControllerWebTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SeekerRepository seekers;
    @MockitoBean
    private ListingRepository listings;
    @MockitoBean
    private MatchExplanationService matchExplanation;

    @Test
    @DisplayName("GET explain returns 404 when the seeker does not exist")
    void explainReturns404WhenSeekerMissing() throws Exception {
        when(seekers.findById("missing")).thenReturn(Optional.empty());
        when(listings.findById("l1")).thenReturn(Optional.empty());

        mvc.perform(get("/api/match/missing/explain").param("listingId", "l1"))
                .andExpect(status().isNotFound());
    }

    // TODO: stub a seeker + listing and a canned explanation, assert 200 + JSON body.
    // OPTIONAL EXTENSION: make the mocked service return the fallback text and assert the
    // endpoint still returns 200; also cover the listing-missing 404 path separately.

    @Test
    @DisplayName("GET explain returns 200 and a JSON body when seeker and listing exist")
    void explainReturns200WithJsonBody() throws Exception {
        Seeker seeker = new Seeker("patman@example.com", "Patman", "0701112233");
        Listing listing = new Listing("provider-1", "Walk Rex", "Friendly dog", ListingType.DOG_WALK);
        String answer = "T-Rex is a great fit for Patman.";

        when(seekers.findById("s1")).thenReturn(Optional.of(seeker));
        when(listings.findById("l1")).thenReturn(Optional.of(listing));
        when(matchExplanation.explainMatch(any(), any())).thenReturn(answer);

        mvc.perform(get("/api/match/s1/explain").param("listingId", "l1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seekerId").value("s1"))
                .andExpect(jsonPath("$.listingId").value("l1"))
                .andExpect(jsonPath("$.explanation").value(answer));
    }
}
