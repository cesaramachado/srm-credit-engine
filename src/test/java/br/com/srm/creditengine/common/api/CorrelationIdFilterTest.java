package br.com.srm.creditengine.common.api;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void preservesSafeCorrelationIdAndCleansMdcAfterRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-Correlation-ID", "request-123_abc");
        FilterChain chain = (servletRequest, servletResponse) ->
                assertThat(MDC.get("correlation_id")).isEqualTo("request-123_abc");

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader("X-Correlation-ID")).isEqualTo("request-123_abc");
        assertThat(MDC.get("correlation_id")).isNull();
    }

    @Test
    void replacesUnsafeCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-Correlation-ID", "invalid\nvalue");

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader("X-Correlation-ID"))
                .matches("[a-f0-9-]{36}")
                .isNotEqualTo("invalid\nvalue");
    }
}
