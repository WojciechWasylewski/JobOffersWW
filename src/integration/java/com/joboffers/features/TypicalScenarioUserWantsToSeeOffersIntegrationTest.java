package com.joboffers.features;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.joboffers.BaseIntegrationTests;
import com.joboffers.SampleJobOfferResponse;
import com.joboffers.domain.loginandregister.dto.RegistrationResultDto;
import com.joboffers.domain.offer.dto.OfferResponseDto;
import com.joboffers.infrastructure.loginandregister.controller.dto.JwtResponseDto;
import com.joboffers.infrastructure.offer.scheduler.HttpOffersScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TypicalScenarioUserWantsToSeeOffersIntegrationTest extends BaseIntegrationTests implements SampleJobOfferResponse {
    @Autowired
    HttpOffersScheduler httpOffersScheduler;
    @Container
    public static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));

    @DynamicPropertySource
    public static void propertyOverride(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("offer.http.client.config.uri", () -> WIRE_MOCK_HOST);
        registry.add("offer.http.client.config.port", () -> wireMockServer.getPort());
    }

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
        ResultActions failedLoginRequest = mockMvc.perform(post("/token")
                .content("""
                        {
                        "username": "someUser",
                        "password": "somePassword"
                        }
                        """.trim())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then
        failedLoginRequest
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("""
                        {
                          "message": "Bad Credentials",
                          "status": "UNAUTHORIZED"
                        }
                        """.trim()));


//    Step 4: user made GET /offers with no JWT token and system returned UNAUTHORIZED (401).
        //given && when
        ResultActions failedGetOffersRequest = mockMvc.perform(post("/offers")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        //then
        failedGetOffersRequest.andExpect(status().isForbidden());


//    Step 5: user made POST /register with: username=someUser, password=somePassword and system registered and returned status Created(201).
        //given && when
        ResultActions registerRequest = mockMvc.perform(post("/register")
                .content("""
                        {
                        "username": "someUser",
                        "password": "somePassword"
                        }
                        """.trim())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then
        MvcResult registerRequestResult = registerRequest.andExpect(status().isCreated()).andReturn();
        String registerRequestJson = registerRequestResult.getResponse().getContentAsString();
        RegistrationResultDto registrationResultDto = objectMapper.readValue(registerRequestJson, RegistrationResultDto.class);
        assertAll(
                () -> assertThat(registrationResultDto.username()).isEqualTo("someUser"),
                () -> assertThat(registrationResultDto.created()).isTrue(),
                () -> assertThat(registrationResultDto.id()).isNotNull());

//    Step 6: user tried to get JWT token by requesting POST /token with username=someUser, password=somePassword and system returned OK(200) and jwttoken=AAAA.BBBB.CCC
        //given && when
        ResultActions successLoginRequest = mockMvc.perform(post("/token")
                .content("""
                        {
                        "username": "someUser",
                        "password": "somePassword"
                        }
                        """.trim())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        //then
        MvcResult mvcResult = successLoginRequest.andExpect(status().isOk()).andReturn();
        String successResultJson = mvcResult.getResponse().getContentAsString();
        JwtResponseDto jwtResponseDto = objectMapper.readValue(successResultJson, JwtResponseDto.class);
        String token = jwtResponseDto.token();
        assertAll(
                () -> assertThat(jwtResponseDto.username()).isEqualTo("someUser"),
                () -> assertThat(token).matches(Pattern.compile("^([A-Za-z0-9-_=]+\\.)+([A-Za-z0-9-_=])+\\.?$"))
        );


//    Step 7: user made GET /offers with header ”Authorization: Bearer AAAA.BBBB.CCC” and system returned OK(200) with no offers (because after first scheduler run downloaded 0 offers).
        //given
        String offersUrl = "/offers";
        //when
        ResultActions perform = mockMvc.perform(get(offersUrl)
                        .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
        );
        // then
        MvcResult result = perform.andExpect(status().isOk()).andReturn();
        String json = result.getResponse().getContentAsString();
        List<OfferResponseDto> offers = objectMapper.readValue(json, new TypeReference<>() {
        });
        assertThat(offers).isEmpty();


//    Step 8: external HTTP server have 2 new offers.
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(bodyWithTwoOffersJson())));


//    Step 9: scheduler run 2nd time and made GET to external server and 2 offers with id:1000 and id:2000 are added to database.
        //given when
        List<OfferResponseDto> twoNewOffers = httpOffersScheduler.fetchAllOffersAndSaveAllIfNotExists();
        //then
        assertThat(twoNewOffers).hasSize(2);

//    Step 10: user made GET /offers with header ”Authorization: Bearer AAAA.BBBB.CCC” and system returned OK(200) with 2 offers with id:1000 and id:2000
        //when
        ResultActions performGetForTwoOffers = mockMvc.perform(get(offersUrl)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
        );
        // then
        MvcResult resultWithTwoOffers = performGetForTwoOffers.andExpect(status().isOk()).andReturn();
        String jsonWithTwoOffers = resultWithTwoOffers.getResponse().getContentAsString();
        List<OfferResponseDto> twoOfersListResponse = objectMapper.readValue(jsonWithTwoOffers, new TypeReference<>() {
        });
        assertThat(twoOfersListResponse).hasSize(2);
        OfferResponseDto expectedOffer1 = twoOfersListResponse.get(0);
        OfferResponseDto expectedOffer2 = twoOfersListResponse.get(1);
        assertThat(twoOfersListResponse).containsExactlyInAnyOrder(
                new OfferResponseDto(expectedOffer1.id(), expectedOffer1.companyName(), expectedOffer1.position(), expectedOffer1.salary(), expectedOffer1.offerUrl()),
                new OfferResponseDto(expectedOffer2.id(), expectedOffer2.companyName(), expectedOffer2.position(), expectedOffer2.salary(), expectedOffer2.offerUrl())
        );

//    Step 11: user made GET /offers/9999 (for search offer with id:9999), and system returned NOT_FOUND(404) with message ”Offer with id 9999 not found”. // user made GET /offers/1000 and system returned OK(200) with offer with id:1000.
        //given
        String offerId = "9999";
        //when
        ResultActions performGetOffersWithNotExistingId = mockMvc.perform(get("/offers/" + offerId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        //then
        performGetOffersWithNotExistingId.andExpect(status().isNotFound())
                .andExpect(content().json("""
                        {
                        "message":  "Offer with id 9999 not found",
                        "status": "NOT_FOUND"
                        }
                        """.trim()));

//    Step 12: user made GET /offers/1000 and system returned OK(200) with offer
        //given
        String existingOfferIdInDatabase = expectedOffer1.id();
        //when
        ResultActions getOfferById = mockMvc.perform(get("/offers/" + existingOfferIdInDatabase)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
        );
        //then
        String singleOfferJson = getOfferById.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        OfferResponseDto singleOfferDto = objectMapper.readValue(singleOfferJson, OfferResponseDto.class);
        assertThat(singleOfferDto).isEqualTo(expectedOffer1);


//    Step 13: scheduler run 3rd time and made GET request to server and system added two more offers with id:3000 and id:4000 to database.
        //given
        //when
        //then
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(bodyWithFourOffersJson())));

        List<OfferResponseDto> nextTwoNewOffers = httpOffersScheduler.fetchAllOffersAndSaveAllIfNotExists();
        //then
        assertThat(nextTwoNewOffers).hasSize(2);


//step 14: user made GET /offers and system returned OK(200) with 4 offer
        //given && when
        ResultActions performGetForFourOffers = mockMvc.perform(get(offersUrl)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then
        MvcResult performGetForFourOffersMvcResult = performGetForFourOffers.andExpect(status().isOk()).andReturn();
        String jsonWithFourOffers = performGetForFourOffersMvcResult.getResponse().getContentAsString();
        List<OfferResponseDto> fourOffers = objectMapper.readValue(jsonWithFourOffers, new TypeReference<>() {
        });
        assertThat(fourOffers).hasSize(4);
        OfferResponseDto expected3Offer = nextTwoNewOffers.get(0);
        OfferResponseDto expected4Offer = nextTwoNewOffers.get(1);
        assertThat(fourOffers).contains(
                new OfferResponseDto(expected3Offer.id(), expected3Offer.companyName(), expected3Offer.position(), expected3Offer.salary(), expected3Offer.offerUrl()),
                new OfferResponseDto(expected4Offer.id(), expected4Offer.companyName(), expected4Offer.position(), expected4Offer.salary(), expected4Offer.offerUrl()
                ));

//Step 15: user made POST /offers with offer as body and system returned CREATED(201) with 1 offer
        //given
        //when
        ResultActions performPostOffer = mockMvc.perform(post("/offers")
                .header("Authorization", "Bearer " + token)
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

//step 16: user made GET /offers with header “Authorization: Bearer AAAA.BBBB.CCC” and system returned OK(200) with 5 offers
        // given & when
        ResultActions peformGetOffers = mockMvc.perform(get("/offers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then
        String oneOfferJson = peformGetOffers.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<OfferResponseDto> parsedJsonWithOneOffer = objectMapper.readValue(oneOfferJson, new TypeReference<>() {
        });
        assertThat(parsedJsonWithOneOffer).hasSize(5);
        assertThat(parsedJsonWithOneOffer.stream().map(OfferResponseDto::id)).contains(id);

    }
}
