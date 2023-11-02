package com.joboffers.features;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.joboffers.BaseIntegrationTests;
import com.joboffers.SampleJobOfferResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class TypicalScenarioUserWantsToSeeOffersIntegrationTest extends BaseIntegrationTests implements SampleJobOfferResponse {
    @Test
    public void user_want_to_see_offers_but_have_to_be_logged_in_and_external_server_should_have_some_offers() {
//    Step 1: external http server dont have any job offers.
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(bodyWithZeroOffersJson())));

//    Step 2: scheduler run 1st time and made GET to external server and download zero offers to database.
//    Step 3: user which is not registered yet tried to get JWT token by requesting POST with: username=someUser, password=somePassword and system returns UNAUTHORIZED (401).
//    Step 4: user made GET /offers with no JWT token znd system returned UNAUTHORIZED (401).
//    Step 5: user made POST /register with: username=someUser, password=somePassword and system registered and returned status OK(200).
//    Step 6: user tried get JWT token by requesting POST /token with username=someUser, password=somePassword and system returned OK(200) and jwttoken=AAAA.BBBB.CCC
//    Step 7: user made GET /offers with header ”Authorization: Bearer AAAA.BBBB.CCC” and system returned OK(200) with no offers (because after first scheduler run downloaded 0 offers).
//    Step 8: external HTTP server have 2 new offers.
//    Step 9: scheduler run 2nd time and made GET to external server and 2 offers with id:1000 and id:2000 are added to database.
//    Step 10: user made GET /offers with header ”Authorization: Bearer AAAA.BBBB.CCC” and system returned OK(200) with 2 offers with id:1000 and id:2000
//    Step 11: user made GET /offers/9999 (for search offer with id:9999), and system returned NOT_FOUND(404) with message ”Offer with id 9999 not found”. // user made GET /offers/1000 and system returned OK(200) with offer with id:1000.
//    Step 12: external server have 2 new offers.
//    Step 13: scheduler run 3rdtime and made GET request to server and system added two more offers with id:3000 and id:4000 to database.

    }
}