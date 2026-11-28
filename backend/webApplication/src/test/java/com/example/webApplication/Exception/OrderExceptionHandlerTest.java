package com.example.webApplication.Exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.webApplication.Entity.OrderStatus;
import com.example.webApplication.Entity.UserRoleStatus;

/** Dam bao handler rieng thang handler Exception chung (neu khong se tra 500). */
class OrderExceptionHandlerTest {

    @RestController
    static class FailingController {
        @GetMapping("/invalid")
        void invalid() {
            throw new InvalidOrderTransitionException(OrderStatus.SHIPPING, OrderStatus.CANCELLED);
        }

        @GetMapping("/forbidden")
        void forbidden() {
            throw new OrderTransitionForbiddenException(
                OrderStatus.PENDING_CONFIRMATION, OrderStatus.AWAITING_PICKUP, UserRoleStatus.CUSTOMER);
        }
    }

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new FailingController())
        .setControllerAdvice(new GlobalExceptionHandler(), new OrderExceptionHandler())
        .build();

    @Test
    void invalidTransitionIsConflict() throws Exception {
        mvc.perform(get("/invalid")).andExpect(status().isConflict());
    }

    @Test
    void wrongRoleIsForbidden() throws Exception {
        mvc.perform(get("/forbidden")).andExpect(status().isForbidden());
    }
}
