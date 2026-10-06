package com.walkmates.lab3;

import com.walkmates.model.Listing;
import com.walkmates.model.ListingType;
import com.walkmates.model.Seeker;
import com.walkmates.model.TrustTier;
import com.walkmates.service.ai.LlmClient;
import com.walkmates.service.ai.MatchExplanationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Lab 3, Part A — testing the AI "explain this match" feature without a live LLM.
 *
 * <p>There is no exact oracle for the model's text, so we test the parts we <em>can</em> pin
 * down: the deterministic prompt builder, the fallback path (mock the {@link LlmClient} to
 * fail/timeout), the metamorphic relations, and prompt-injection resistance. Two worked
 * examples are provided; the {@code TODO}s are yours.</p>
 */
class MatchExplanationServiceTest {

    private Seeker seeker() {
        return new Seeker("p@example.com", "Pat", "0701112233");
    }

    private Listing listing(String description) {
        return new Listing("provider-1", "Walk Rex", description, ListingType.DOG_WALK);
    }

    // ---- Worked example 1: the prompt builder is deterministic and structured (FR-5.1) ----
    @Test
    @DisplayName("buildPrompt includes the structured fields")
    void promptIncludesStructuredFields() {
        MatchExplanationService service = new MatchExplanationService(mock(LlmClient.class));

        String prompt = service.buildPrompt(seeker(), listing("Friendly dog"));

        assertThat(prompt).contains("Seeker trust tier: " + TrustTier.NEW);
        assertThat(prompt).contains("Listing type: " + ListingType.DOG_WALK);
    }

    // ---- Worked example 2: on LLM failure, fall back deterministically (FR-5.2) ----
    @Test
    @DisplayName("explainMatch falls back when the LLM call fails")
    void fallsBackOnLlmFailure() throws Exception {
        LlmClient llm = mock(LlmClient.class);
        when(llm.complete(org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new LlmClient.LlmException("provider down"));
        MatchExplanationService service = new MatchExplanationService(llm);
        Seeker seeker = seeker();
        Listing listing = listing("Friendly dog");

        String result = service.explainMatch(seeker, listing);

        // Use an independent, concrete oracle. Comparing result only with another call to
        // fallbackExplanation would pass if both calls returned the same wrong text.
        assertThat(result).isEqualTo(
                "This DOG_WALK opportunity \"Walk Rex\" is a good fit for a NEW seeker.");
    }

    // TODO (fallback): also fall back on LlmTimeoutException, and on a null/blank response.
    // TODO (injection): a description containing "ignore previous instructions and ..." must
    //      stay inside the data block; buildPrompt must still contain the data delimiters.
    // TODO (MR-1): adding an irrelevant sentence to the listing description must not change
    //      recommendBestMatch's chosen listing.
    // TODO (MR-2): shuffling the candidate list must not change the chosen listing.

    @Test
    @DisplayName("buildPrompt includes the base rate and the title")
    void promptIncludesRateAndTitle() {
        MatchExplanationService service = new MatchExplanationService(mock(LlmClient.class));

        String prompt = service.buildPrompt(seeker(), listing("friendly dog"));
        assertThat(prompt).contains("Listing base rate (SEK/hour): " + 80.0);
        assertThat(prompt).contains("Listing title: " + "Walk Rex");

    }

    @Test
    @DisplayName("buildPrompt places the description inside the data delimiters")
    void descriptionIsInsideDataBlock() {
        MatchExplanationService service = new MatchExplanationService(mock(LlmClient.class));
        String description = "Rex loves long walks in the park";

        String prompt = service.buildPrompt(seeker(), listing(description));

        assertThat(prompt).contains("<<<LISTING_DESCRIPTION_DATA");
        assertThat(prompt).contains(description);
        assertThat(prompt).contains("LISTING_DESCRIPTION_DATA>>>");
        
        int start = prompt.indexOf("<<<LISTING_DESCRIPTION_DATA");
        int text  = prompt.indexOf(description);
        int end   = prompt.indexOf("LISTING_DESCRIPTION_DATA>>>");

        assertThat(text).isGreaterThan(start);
        assertThat(text).isLessThan(end);
    }

    @Test
    @DisplayName("explainMatch falls back when the LLM times out")
    void fallsBackOnLlmTimeout() throws Exception {
        LlmClient llm = mock(LlmClient.class);
        when(llm.complete(org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new LlmClient.LlmTimeoutException("too slow"));
        MatchExplanationService service = new MatchExplanationService(llm);

        String result = service.explainMatch(seeker(), listing("Friendly dog"));

        assertThat(result).isEqualTo("This DOG_WALK opportunity \"Walk Rex\" is a good fit for a NEW seeker.");
    }
    @Test
    @DisplayName("explainMatch falls back when the LLM returns null")
    void fallsBackOnLlmIsNull() throws Exception {
        LlmClient llm = mock(LlmClient.class);
        when(llm.complete(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(null);
        MatchExplanationService service = new MatchExplanationService(llm);

        String result = service.explainMatch(seeker(), listing("Friendly dog"));

        assertThat(result).isEqualTo("This DOG_WALK opportunity \"Walk Rex\" is a good fit for a NEW seeker.");
    }
    @Test
    @DisplayName("explainMatch falls back when the LLM returns a blank response")
    void fallsBackOnLlmIsEmpty() throws Exception {
        LlmClient llm = mock(LlmClient.class);
        when(llm.complete(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(" ");
        MatchExplanationService service = new MatchExplanationService(llm);

        String result = service.explainMatch(seeker(), listing("Friendly dog"));

        assertThat(result).isEqualTo("This DOG_WALK opportunity \"Walk Rex\" is a good fit for a NEW seeker.");
    }

    @Test
    @DisplayName("MR-1: an irrelevant sentence in a description does not change the chosen listing")
    void mr1IrrelevantDetailDoesNotChangeChoice() {
        MatchExplanationService service = new MatchExplanationService(mock(LlmClient.class));
        Listing walk  = new Listing("provider-1", "Walk Rex", "Friendly dog", ListingType.DOG_WALK);
        Listing house = new Listing("provider-2", "Watch my house", "Two calm cats", ListingType.HOUSE_SITTING);
        List<Listing> candidates = List.of(walk, house);

        Listing first = service.recommendBestMatch(seeker(), candidates);

        house.setDescription(house.getDescription() + " No thing is important");

        Listing second = service.recommendBestMatch(seeker(), candidates);

        assertThat(second.getId()).isEqualTo(first.getId());
    }
    @Test
    @DisplayName("MR-1: an irrelevant sentence in a description does not change the chosen listing")
    void mr1IrrelevantDetailDoesNotChange() {
        MatchExplanationService service = new MatchExplanationService(mock(LlmClient.class));
        Listing walk  = new Listing("provider-1", "Walk Rex", "Friendly dog", ListingType.DOG_WALK);
        Listing walkA  = new Listing("provider-2", "Walk Rex", "Friendly dog", ListingType.DOG_WALK);
        Listing house = new Listing("provider-3", "Watch my house", "Two calm cats", ListingType.HOUSE_SITTING);
        List<Listing> candidates = List.of(walk, walkA, house);

        Listing first = service.recommendBestMatch(seeker(), candidates);

        house.setDescription(house.getDescription() + " No thing is important");

        
        List<Listing> shuffledList = new ArrayList<>(candidates);
        Collections.shuffle(shuffledList, new Random(30));
        
        Listing second = service.recommendBestMatch(seeker(), shuffledList);
        assertThat(second.getId()).isEqualTo(first.getId());
    }

    


}
