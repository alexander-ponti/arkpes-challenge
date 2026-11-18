package com.ark.funds;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
class FundsApiIntegrationTest {

    @Autowired MockMvc mvc;

    private long create(String url, String body) throws Exception {
        String res = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(res, "$.id")).longValue();
    }

    private void tx(long fund, long inv, String type, String amount, String date) throws Exception {
        create("/api/transactions", """
            {"fundId":%d,"investorId":%d,"type":"%s","amount":%s,"transactionDate":"%s"}
            """.formatted(fund, inv, type, amount, date));
    }

    @Test
    void reportsNetCreditsAndDebitsPerFundAndInvestor() throws Exception {
        long fund = create("/api/funds", "{\"name\":\"Growth Fund I\"}");
        long other = create("/api/funds", "{\"name\":\"Income Fund II\"}");
        long alice = create("/api/investors", "{\"name\":\"Alice\",\"email\":\"alice@example.com\"}");
        long bob = create("/api/investors", "{\"name\":\"Bob\",\"email\":\"bob@example.com\"}");

        tx(fund, alice, "CONTRIBUTION", "1000.00", "2024-01-10");
        tx(fund, alice, "MANAGEMENT_FEE", "20.00", "2024-02-01");
        tx(fund, bob, "CONTRIBUTION", "500.00", "2024-01-15");
        tx(fund, bob, "INTEREST_INCOME", "25.50", "2024-03-01");
        tx(fund, bob, "DISTRIBUTION", "100.00", "2024-04-01");
        tx(other, alice, "CONTRIBUTION", "300.00", "2024-02-20");

        // fund: credits 1525.50, debits 120.00, net 1405.50
        mvc.perform(get("/api/funds/" + fund + "/report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCredits", comparesEqualTo(1525.50)))
                .andExpect(jsonPath("$.totalDebits", comparesEqualTo(120.0)))
                .andExpect(jsonPath("$.netBalance", comparesEqualTo(1405.50)))
                .andExpect(jsonPath("$.investorCount").value(2));

        // date filter excludes the April distribution
        mvc.perform(get("/api/funds/" + fund + "/report?from=2024-01-01&to=2024-03-31"))
                .andExpect(jsonPath("$.totalDebits", comparesEqualTo(20.0)));

        // investor spans two funds: credits 1300, debits 20
        mvc.perform(get("/api/investors/" + alice + "/report"))
                .andExpect(jsonPath("$.fundCount").value(2))
                .andExpect(jsonPath("$.netBalance", comparesEqualTo(1280.0)));

        mvc.perform(get("/api/funds/" + fund + "/investors")).andExpect(jsonPath("$.content.length()").value(2));
        mvc.perform(get("/api/investors/" + alice + "/funds")).andExpect(jsonPath("$.content.length()").value(2));
        mvc.perform(get("/api/transactions?fundId=" + fund + "&type=CONTRIBUTION")).andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void validationAndConflicts() throws Exception {
        long fund = create("/api/funds", "{\"name\":\"Dup Fund\"}");
        long inv = create("/api/investors", "{\"name\":\"Carol\",\"email\":\"carol@example.com\"}");

        mvc.perform(post("/api/funds").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"dup fund\"}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/investors").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\",\"email\":\"nope\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fundId\":%d,\"investorId\":%d,\"type\":\"CONTRIBUTION\",\"amount\":-5,\"transactionDate\":\"2024-01-01\"}".formatted(fund, inv)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/funds/999999")).andExpect(status().isNotFound());

        tx(fund, inv, "CONTRIBUTION", "10", "2024-01-01");
        mvc.perform(delete("/api/funds/" + fund)).andExpect(status().isConflict());
        mvc.perform(delete("/api/investors/" + inv)).andExpect(status().isConflict());
    }
}
