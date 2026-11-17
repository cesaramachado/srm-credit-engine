package br.com.srm.creditengine.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
class MetricsUserConfig {

    @Bean
    PasswordEncoder metricsPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnProperty(name = "management.metrics-auth.password")
    UserDetailsService metricsUserDetailsService(
            @Value("${management.metrics-auth.username}") String username,
            @Value("${management.metrics-auth.password}") String password,
            PasswordEncoder metricsPasswordEncoder) {
        return new InMemoryUserDetailsManager(User.withUsername(username)
                .password(metricsPasswordEncoder.encode(password))
                .roles("METRICS")
                .build());
    }
}
