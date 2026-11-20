package com.example.webApplication.Controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Runs against a real MySQL, so it is skipped when SPRING_DATASOURCE_URL is not set.
 * Every voucher of a test run has a code starting with a unique prefix, which is also used to clean up.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "SPRING_DATASOURCE_URL", matches = ".+")
class VoucherAdminApiTest {

    private static final String API = "/api/v1/admin/vouchers";
    private static final String START = "2026-01-01T00:00:00Z";
    private static final String END = "2026-12-31T00:00:00Z";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;

    private final String prefix = "T" + (System.nanoTime() % 1_000_000_000L) + "-";
    private final RequestPostProcessor admin = user("admin@test.local").roles("ADMIN");

    @AfterEach
    void cleanUp() {
        jdbc.update("delete from vouchers where code like ?", prefix + "%");
    }

    private static String json(String code, String type, String value, String max, String min,
            String start, String end, Integer limit) {
        return "{\"code\":" + (code == null ? "null" : "\"" + code + "\"")
                + ",\"type\":" + (type == null ? "null" : "\"" + type + "\"")
                + ",\"discountValue\":" + value
                + ",\"maxDiscountAmount\":" + max
                + ",\"minOrderAmount\":" + min
                + ",\"startDate\":" + (start == null ? "null" : "\"" + start + "\"")
                + ",\"endDate\":" + (end == null ? "null" : "\"" + end + "\"")
                + ",\"usageLimit\":" + limit + "}";
    }

    private static String percent(String code, String value) {
        return json(code, "PERCENT", value, "50000", "0", START, END, 100);
    }

    private long create(String body) throws Exception {
        String response = mvc.perform(post(API).with(admin).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) com.jayway.jsonpath.JsonPath.read(response, "$.id")).longValue();
    }

    // ---- create / read / update ----

    @Test
    void adminCreatesAndReadsAVoucher() throws Exception {
        long id = create(percent(prefix + "A", "10"));

        mvc.perform(get(API + "/" + id).with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(prefix + "A"))
                .andExpect(jsonPath("$.type").value("PERCENT"))
                .andExpect(jsonPath("$.discountValue").value(10.0))
                .andExpect(jsonPath("$.maxDiscountAmount").value(50000.0))
                .andExpect(jsonPath("$.usageLimit").value(100))
                .andExpect(jsonPath("$.usedCount").value(0))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void crossFieldAndRangeRulesAreChecked() throws Exception {
        // end before start
        mvc.perform(post(API).with(admin).contentType(MediaType.APPLICATION_JSON)
                .content(json(prefix + "B", "PERCENT", "10", "null", "0", END, START, 10)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("endDate must be after startDate")));
        // percent above 100
        mvc.perform(post(API).with(admin).contentType(MediaType.APPLICATION_JSON).content(percent(prefix + "B", "101")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("between 0 and 100")));
        // negative values and missing code, all reported together
        mvc.perform(post(API).with(admin).contentType(MediaType.APPLICATION_JSON)
                .content(json(null, "FIXED_AMOUNT", "-5", "null", "-1", START, END, 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("code is required")))
                .andExpect(jsonPath("$.message").value(containsString("discountValue must be zero or more")))
                .andExpect(jsonPath("$.message").value(containsString("minOrderAmount must be zero or more")))
                .andExpect(jsonPath("$.message").value(containsString("usageLimit must be at least 1")));
    }

    @Test
    void duplicateCodeIsRejectedWithConflict() throws Exception {
        create(percent(prefix + "C", "10"));

        mvc.perform(post(API).with(admin).contentType(MediaType.APPLICATION_JSON).content(percent(prefix + "C", "20")))
                .andExpect(status().isConflict());
    }

    @Test
    void updateChangesTheFields() throws Exception {
        long id = create(percent(prefix + "D", "10"));

        mvc.perform(put(API + "/" + id).with(admin).contentType(MediaType.APPLICATION_JSON)
                .content(json(prefix + "D2", "FIXED_AMOUNT", "30000", "null", "100000", START, END, 5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(prefix + "D2"))
                .andExpect(jsonPath("$.type").value("FIXED_AMOUNT"))
                .andExpect(jsonPath("$.minOrderAmount").value(100000.0))
                .andExpect(jsonPath("$.usageLimit").value(5));
    }

    @Test
    void usageLimitCannotDropBelowTheUsesAlreadyMade() throws Exception {
        long id = create(percent(prefix + "E", "10"));
        jdbc.update("update vouchers set used_count = 3 where id = ?", id);

        mvc.perform(put(API + "/" + id).with(admin).contentType(MediaType.APPLICATION_JSON)
                .content(json(prefix + "E", "PERCENT", "10", "null", "0", START, END, 2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("3 uses already made")));
    }

    @Test
    void updatingToACodeUsedByAnotherVoucherIsAConflict() throws Exception {
        create(percent(prefix + "F1", "10"));
        long second = create(percent(prefix + "F2", "10"));

        mvc.perform(put(API + "/" + second).with(admin).contentType(MediaType.APPLICATION_JSON)
                .content(percent(prefix + "F1", "10")))
                .andExpect(status().isConflict());
    }

    @Test
    void unknownVoucherIsNotFound() throws Exception {
        mvc.perform(get(API + "/999999999").with(admin)).andExpect(status().isNotFound());
    }

    // ---- the five filters ----

    private void createFilterFixtures() throws Exception {
        // F-PCT: PERCENT, active, Jan-Jun 2026, min 0
        create(json(prefix + "PCT", "PERCENT", "10", "null", "0", "2026-01-01T00:00:00Z", "2026-06-30T00:00:00Z", 10));
        // F-FIX: FIXED_AMOUNT, active, Jul-Dec 2026, min 200000
        create(json(prefix + "FIX", "FIXED_AMOUNT", "50000", "null", "200000", "2026-07-01T00:00:00Z", "2026-12-31T00:00:00Z", 10));
        // F-SHIP: FREESHIP, inactive, Jan-Dec 2026, min 500000
        String inactive = json(prefix + "SHIP", "FREESHIP", "0", "null", "500000", START, END, 10)
                .replace("\"usageLimit\":10}", "\"usageLimit\":10,\"active\":false}");
        create(inactive);
    }

    @Test
    void filtersNarrowTheList() throws Exception {
        createFilterFixtures();

        // code (contains, case-insensitive)
        mvc.perform(get(API).with(admin).param("code", prefix.toLowerCase()))
                .andExpect(jsonPath("$.totalElements").value(3));
        mvc.perform(get(API).with(admin).param("code", prefix + "fix"))
                .andExpect(jsonPath("$.totalElements").value(1));
        // type
        mvc.perform(get(API).with(admin).param("code", prefix).param("type", "PERCENT"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].code").value(prefix + "PCT"));
        // active
        mvc.perform(get(API).with(admin).param("code", prefix).param("active", "false"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].code").value(prefix + "SHIP"));
        mvc.perform(get(API).with(admin).param("code", prefix).param("active", "true"))
                .andExpect(jsonPath("$.totalElements").value(2));
        // validity range: vouchers valid at some point from 2026-08-01 on (FIX and SHIP)
        mvc.perform(get(API).with(admin).param("code", prefix).param("validFrom", "2026-08-01T00:00:00Z"))
                .andExpect(jsonPath("$.totalElements").value(2));
        // validity range: vouchers valid at some point up to 2026-03-01 (PCT and SHIP)
        mvc.perform(get(API).with(admin).param("code", prefix).param("validTo", "2026-03-01T00:00:00Z"))
                .andExpect(jsonPath("$.totalElements").value(2));
        // minimum order range
        mvc.perform(get(API).with(admin).param("code", prefix).param("minOrderFrom", "200000").param("minOrderTo", "300000"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].code").value(prefix + "FIX"));
    }

    // ---- paging and sorting ----

    @Test
    void pageSizeMustBeOneOfTheAllowedValues() throws Exception {
        mvc.perform(get(API).with(admin).param("size", "15")).andExpect(status().isBadRequest());
        for (String size : new String[] { "10", "20", "50", "100" }) {
            mvc.perform(get(API).with(admin).param("size", size)).andExpect(status().isOk());
        }
    }

    @Test
    void listReportsFromToAndTotal() throws Exception {
        createFilterFixtures();

        mvc.perform(get(API).with(admin).param("code", prefix).param("size", "10"))
                .andExpect(jsonPath("$.from").value(1))
                .andExpect(jsonPath("$.to").value(3))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.size").value(10));
        mvc.perform(get(API).with(admin).param("code", prefix).param("page", "5"))
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.from").value(0))
                .andExpect(jsonPath("$.to").value(0));
    }

    @Test
    void listCanBeSortedByTwoColumns() throws Exception {
        createFilterFixtures();

        // minOrderAmount ascending, then code descending: PCT(0), FIX(200000), SHIP(500000)
        mvc.perform(get(API).with(admin).param("code", prefix)
                .param("sort", "minOrderAmount,asc").param("sort", "code,desc"))
                .andExpect(jsonPath("$.content[0].code").value(prefix + "PCT"))
                .andExpect(jsonPath("$.content[2].code").value(prefix + "SHIP"));
        mvc.perform(get(API).with(admin).param("code", prefix).param("sort", "minOrderAmount,desc"))
                .andExpect(jsonPath("$.content[0].code").value(prefix + "SHIP"));
    }

    @Test
    void sortingByAnUnknownFieldIsRejected() throws Exception {
        mvc.perform(get(API).with(admin).param("sort", "password,asc")).andExpect(status().isBadRequest());
    }

    // ---- soft delete, trash, restore, purge ----

    @Test
    void softDeleteMovesTheVoucherToTheTrashAndFreesTheCode() throws Exception {
        long id = create(percent(prefix + "G", "10"));

        mvc.perform(delete(API + "/" + id).with(admin)).andExpect(status().isNoContent());

        mvc.perform(get(API + "/" + id).with(admin)).andExpect(status().isNotFound());
        mvc.perform(get(API).with(admin).param("code", prefix + "G")).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get(API + "/trash").with(admin).param("size", "100"))
                .andExpect(jsonPath("$.content[?(@.id==" + id + ")].deletedBy").value("admin@test.local"));
        // the same code can be used again
        create(percent(prefix + "G", "20"));
    }

    @Test
    void restoreBringsTheOriginalCodeBack() throws Exception {
        long id = create(percent(prefix + "H", "10"));
        mvc.perform(delete(API + "/" + id).with(admin)).andExpect(status().isNoContent());

        mvc.perform(post(API + "/" + id + "/restore").with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(prefix + "H"))
                .andExpect(jsonPath("$.deletedAt").doesNotExist());

        mvc.perform(get(API + "/" + id).with(admin)).andExpect(status().isOk());
    }

    @Test
    void restoreFailsWhenAnotherVoucherTookTheCode() throws Exception {
        long id = create(percent(prefix + "I", "10"));
        mvc.perform(delete(API + "/" + id).with(admin)).andExpect(status().isNoContent());
        create(percent(prefix + "I", "20"));

        mvc.perform(post(API + "/" + id + "/restore").with(admin)).andExpect(status().isConflict());
    }

    @Test
    void purgeWorksOnlyOnVouchersInTheTrash() throws Exception {
        long id = create(percent(prefix + "J", "10"));

        mvc.perform(delete(API + "/" + id + "/purge").with(admin)).andExpect(status().isNotFound());

        mvc.perform(delete(API + "/" + id).with(admin)).andExpect(status().isNoContent());
        mvc.perform(delete(API + "/" + id + "/purge").with(admin)).andExpect(status().isNoContent());

        mvc.perform(get(API + "/trash").with(admin).param("size", "100"))
                .andExpect(jsonPath("$.content[?(@.id==" + id + ")]").isEmpty());
    }

    // ---- roles ----

    @Test
    void onlyAdminsCanManageVouchers() throws Exception {
        mvc.perform(get(API).with(user("c@test.local").roles("CUSTOMER"))).andExpect(status().isForbidden());
        mvc.perform(get(API).with(user("s@test.local").roles("STAFF"))).andExpect(status().isForbidden());
        mvc.perform(post(API).with(user("s@test.local").roles("STAFF")).contentType(MediaType.APPLICATION_JSON)
                .content(percent(prefix + "K", "10"))).andExpect(status().isForbidden());
        mvc.perform(get(API)).andExpect(status().is4xxClientError());
    }
}
