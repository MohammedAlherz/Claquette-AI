package com.example.claquetteai.Config;

import com.example.claquetteai.Service.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ConfigSecurity {

    private final MyUserDetailsService myUserDetailsService;

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(myUserDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(new BCryptPasswordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityWebFilterChain(HttpSecurity http) throws Exception{
        http.csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
                .authenticationProvider(daoAuthenticationProvider())
                .authorizeHttpRequests()

                // Public endpoints (no authentication required)
                .requestMatchers("/api/v1/company/register",
                        "/api/v1/company/forgot-password",
                        "/api/v1/company/reset-password").permitAll()

                // ADMIN only endpoints
                .requestMatchers("/api/v1/company/companies",
                        "/api/v1/company/delete/**",
                        "/api/v1/subscription/get",
                        "/api/v1/project/get").hasAuthority("ADMIN")

                // COMPANY only endpoints (main business operations)
                .requestMatchers("/api/v1/project/add",
                        "/api/v1/project/update",
                        "/api/v1/project/delete/**",
                        "/api/v1/project/my-projects",
                        "/api/v1/project/project-count",
                        "/api/v1/project/total-budget",
                        "/api/v1/project/project/*/characters",
                        "/api/v1/project/generate-poster/**",
                        "/api/v1/project/project/*/poster.png",
                        "/api/v1/project/project/*/poster",
                        "/api/v1/project/dashboard",
                        "/api/v1/project/content/statistics",
                        "/api/v1/project/*/status/*").hasAuthority("COMPANY")

                // AI and Content Management (COMPANY only)
                .requestMatchers("/api/v1/ai-interaction/**",
                        "/api/v1/casting-recommendation/**",
                        "/api/v1/characters/**",
                        "/api/v1/scene/**",
                        "/api/v1/episode/**").hasAuthority("COMPANY")

                // COMPANY subscription management
                .requestMatchers("/api/v1/subscription/add/**",
                        "/api/v1/subscription/update-status/**",
                        "/api/v1/subscription/cancel-subscription/**",
                        "/api/v1/subscription/activate-subscription/{subscriptionId}",
                        "/api/v1/subscription/renew",
                        "/api/v1/subscription/history-payment",
                        "/api/v1/subscription/manage-subscription").hasAuthority("COMPANY")

                // Payment endpoints (COMPANY only)
                .requestMatchers("/api/v1/pay/**").hasAuthority("COMPANY")

                // Both ADMIN and COMPANY can access these
                .requestMatchers("/api/v1/company/update",
                        "/api/v1/company/photo",
                        "/api/v1/company/verify",
                        "/api/v1/company/resend",
                        "/api/v1/company/profile").hasAnyAuthority("ADMIN", "COMPANY")

                // All other requests require authentication
                .anyRequest().authenticated()
                .and()
                .logout().logoutUrl("/api/v1/auth/logout")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .and()
                .httpBasic();
        return http.build();
    }
}