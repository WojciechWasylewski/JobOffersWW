package com.joboffers.features;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.joboffers.BaseIntegrationTests;
import com.joboffers.SampleJobOfferResponse;
import com.joboffers.domain.offer.dto.OfferResponseDto;
import com.joboffers.infrastructure.offer.scheduler.HttpOffersScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TypicalScenarioUserWantsToSeeOffersIntegrationTest extends BaseIntegrationTests implements SampleJobOfferResponse {
    @Autowired
    HttpOffersScheduler httpOffersScheduler;
    @Test
    public void user_want_to_see_offers_but_have_to_be_logged_in_and_external_server_should_have_some_offers() throws Exception {
//    Step 1: external http server don't have any job offers.
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(bodyWithZeroOffersJson())));


//    Step 2: scheduler run 1st time and made GET to external server and download zero offers to database.
        // given && when
        List<OfferResponseDto> newOffers = httpOffersScheduler.fetchAllOffersAndSaveAllIfNotExists();
        // then
        assertThat(newOffers).isEmpty();


//    Step 3: user which is not registered yet tried to get JWT token by requesting POST with: username=someUser, password=somePassword and system returns UNAUTHORIZED (401).
//    Step 4: user made GET /offers with no JWT token znd system returned UNAUTHORIZED (401).
//    Step 5: user made POST /register with: username=someUser, password=somePassword and system registered and returned status OK(200).
//    Step 6: user tried get JWT token by requesting POST /token with username=someUser, password=somePassword and system returned OK(200) and jwttoken=AAAA.BBBB.CCC


//    Step 7: user made GET /offers with header ”Authorization: Bearer AAAA.BBBB.CCC” and system returned OK(200) with no offers (because after first scheduler run downloaded 0 offers).
        //given
        String offersUrl = "/offers";
        //when
        ResultActions perform = mockMvc.perform(get(offersUrl)
                .contentType(MediaType.APPLICATION_JSON)
        );
        // then
        MvcResult result = perform.andExpect(status().isOk()).andReturn();
        String json = result.getResponse().getContentAsString();
        List<OfferResponseDto> offers = objectMapper.readValue(json, new TypeReference<>() {
        });
        assertThat(offers).isEmpty();


//    Step 8: external HTTP server have 2 new offers.
//    Step 9: scheduler run 2nd time and made GET to external server and 2 offers with id:1000 and id:2000 are added to database.
//    Step 10: user made GET /offers with header ”Authorization: Bearer AAAA.BBBB.CCC” and system returned OK(200) with 2 offers with id:1000 and id:2000
//    Step 11: user made GET /offers/9999 (for search offer with id:9999), and system returned NOT_FOUND(404) with message ”Offer with id 9999 not found”. // user made GET /offers/1000 and system returned OK(200) with offer with id:1000.
        //given
        String offerId = "9999";
        //when
        ResultActions performGetOffersWithNotExistingId = mockMvc.perform(get("/offers/" + offerId));
        //then
        performGetOffersWithNotExistingId.andExpect(status().isNotFound())
                .andExpect(content().json("""
                        {
                        "message":  "Offer with id 9999 not found",
                        "status": "NOT_FOUND"
                        }
                        """.trim()));

//    Step 12: external server have 2 new offers.
//    Step 13: scheduler run 3rdtime and made GET request to server and system added two more offers with id:3000 and id:4000 to database.
//    Step 14: user made POST /offers with offer as body and system returned CREATED(201) with 1 offer
        //given
        //when
        ResultActions performPostOffer = mockMvc.perform(post("/offers")
                .content(
                        """
                                {
                                "companyName": "someCompany",
                                "position": "somePosition",
                                "salary": "7 000 - 9 000 PLN",
                                "offerUrl": "https://newoffeers.pl/offers/1234"
                                }
                                """)
                .contentType(MediaType.APPLICATION_JSON + ";charset=UTF-8")
        );
        //then
        String createdOfferJson = performPostOffer.andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OfferResponseDto parsedCreatedOfferJson = objectMapper.readValue(createdOfferJson, OfferResponseDto.class);
        String id = parsedCreatedOfferJson.id();
        assertAll(
                () -> assertThat(parsedCreatedOfferJson.offerUrl()).isEqualTo("https://newoffeers.pl/offers/1234"),
                () -> assertThat(parsedCreatedOfferJson.companyName()).isEqualTo("someCompany"),
                () -> assertThat(parsedCreatedOfferJson.salary()).isEqualTo("7 000 - 9 000 PLN"),
                () -> assertThat(parsedCreatedOfferJson.position()).isEqualTo("somePosition"),
                () -> assertThat(id).isNotNull()
        );


//step 15: user made GET /offers with header “Authorization: Bearer AAAA.BBBB.CCC” and system returned OK(200) with 1 offer
        // given & when
        ResultActions peformGetOffers = mockMvc.perform(get("/offers")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then
        String oneOfferJson = peformGetOffers.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<OfferResponseDto> parsedJsonWithOneOffer = objectMapper.readValue(oneOfferJson, new TypeReference<>() {
        });
        assertThat(parsedJsonWithOneOffer).hasSize(1);
        assertThat(parsedJsonWithOneOffer.stream().map(OfferResponseDto::id)).contains(id);

    }
}
