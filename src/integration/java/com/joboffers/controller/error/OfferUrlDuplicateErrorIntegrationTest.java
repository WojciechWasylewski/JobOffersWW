package com.joboffers.controller.error;

import com.joboffers.BaseIntegrationTests;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OfferUrlDuplicateErrorIntegrationTest extends BaseIntegrationTests {
    @Test
    public void should_return_409_when_added_second_offer_with_same_offer_url() throws Exception {
        // step 1
        // given && when
        ResultActions firstPerform = mockMvc.perform(post("/offers")
                .content("""
                        {
                        "companyName": "someCompany",
                        "position": "somePosition",
                        "salary": "7 000 - 9 000 PLN",
                        "offerUrl": "https://newoffers.pl/offer/1234"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
        );
        // then
        firstPerform.andExpect(status().isCreated());


        // step 2
        // given && when
        ResultActions secondPerform = mockMvc.perform(post("/offers")
                .content("""
                        {
                        "companyName": "someCompany",
                        "position": "somePosition",
                        "salary": "7 000 - 9 000 PLN",
                        "offerUrl": "https://newoffers.pl/offer/1234"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
        );
        // then
        secondPerform.andExpect(status().isConflict());
    }
}
