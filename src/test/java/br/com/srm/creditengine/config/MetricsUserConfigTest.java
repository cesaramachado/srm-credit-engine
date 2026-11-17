package br.com.srm.creditengine.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

class MetricsUserConfigTest {

    @Test
    void createsTheMetricsOnlyUserWithAnEncodedPassword() {
        MetricsUserConfig configuration = new MetricsUserConfig();
        PasswordEncoder encoder = configuration.metricsPasswordEncoder();
        var users = configuration.metricsUserDetailsService("prometheus", "test-password", encoder);
        var user = users.loadUserByUsername("prometheus");

        assertThat(user.getPassword()).isNotEqualTo("test-password");
        assertThat(encoder.matches("test-password", user.getPassword())).isTrue();
        assertThat(user.getAuthorities()).extracting(Object::toString).contains("ROLE_METRICS");
        assertThatThrownBy(() -> users.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
