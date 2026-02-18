package com.cartographie.config;

import com.cartographie.model.Utilisateur;
import com.cartographie.repository.RoleRepository;
import com.cartographie.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;

import java.util.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UtilisateurRepository userRepo;
    private final RoleRepository roleRepo;
    private final org.springframework.security.oauth2.client.registration.ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Désactivé pour simplifier le dév, à réactiver en prod
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**", "/webjars/**",
                                "/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/stats/**").hasAnyRole("GESTIONNAIRE", "ADMIN")
                        .requestMatchers("/projets/nouveau", "/projets/enregistrer").authenticated()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/projets/mes-projets", true) // Default redirect after local login
                        .permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login") // Page de login personnalisée

                        .userInfoEndpoint(userInfo -> userInfo
                                .userAuthoritiesMapper(userAuthoritiesMapper()))
                        .successHandler((request, response, authentication) -> {
                            // 1. Synchronisation de l'utilisateur
                            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal(); // Pour OIDC user
                            syncUser(oAuth2User);

                            // 2. Redirection par rôle
                            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
                            System.out.println("Roles detected in SuccessHandler: " + roles); // DEBUG

                            if (roles.contains("ROLE_ADMIN")) {
                                response.sendRedirect("/admin/dashboard");
                            } else if (roles.contains("ROLE_GESTIONNAIRE")) {
                                response.sendRedirect("/stats/dashboard");
                            } else {
                                response.sendRedirect("/projets/mes-projets");
                            }
                        }))
                .logout(logout -> logout
                        .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))
                        .deleteCookies("JSESSIONID"));

        return http.build();
    }

    private org.springframework.security.web.authentication.logout.LogoutSuccessHandler oidcLogoutSuccessHandler(
            org.springframework.security.oauth2.client.registration.ClientRegistrationRepository clientRegistrationRepository) {
        org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler = new org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler(
                clientRegistrationRepository);

        // Rediriger vers la page d'accueil après la déconnexion Keycloak
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/");
        return oidcLogoutSuccessHandler;
    }

    // Mapper pour convertir les rôles Keycloak en Authority Spring Security ET
    // enrichir depuis la BDD locale
    private GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>(authorities);
            String email = null;

            for (GrantedAuthority authority : authorities) {
                if (authority instanceof OidcUserAuthority) {
                    OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;
                    Map<String, Object> userInfo = oidcUserAuthority.getAttributes();

                    if (email == null)
                        email = (String) userInfo.get("email");

                    // Extraction des rôles du Realm Keycloak
                    Map<String, Object> realmAccess = null;
                    if (userInfo.containsKey("realm_access")) {
                        realmAccess = (Map<String, Object>) userInfo.get("realm_access");
                    } else if (oidcUserAuthority.getIdToken().getClaims().containsKey("realm_access")) {
                        realmAccess = (Map<String, Object>) oidcUserAuthority.getIdToken().getClaims()
                                .get("realm_access");
                    }

                    if (realmAccess != null && realmAccess.containsKey("roles")) {
                        Collection<String> roles = (Collection<String>) realmAccess.get("roles");
                        roles.forEach(role -> {
                            mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                        });
                    }
                } else if (authority instanceof org.springframework.security.oauth2.core.user.OAuth2UserAuthority) {
                    Map<String, Object> attributes = ((org.springframework.security.oauth2.core.user.OAuth2UserAuthority) authority)
                            .getAttributes();
                    if (email == null)
                        email = (String) attributes.get("email");
                }
            }

            // Enrichment from Local DB
            if (email != null) {
                // Remove existing ROLE_CANDIDAT if we are adding a stronger role?
                // No, hierarchy might handle it, or we just add.
                // But better to check.
                Optional<Utilisateur> localUser = userRepo.findByEmail(email);
                if (localUser.isPresent() && localUser.get().getRole() != null) {
                    String localRole = "ROLE_" + localUser.get().getRole().getLibelle().toUpperCase();

                    // If local user is ADMIN or GESTIONNAIRE, remove generic CANDIDAT if it was
                    // added by default logic later?
                    // No, default logic is AFTER this block.
                    // "Check if we have any functional role" checks mappedAuthorities.
                    // So if we add ROLE_ADMIN here, the functional check passes, and ROLE_CANDIDAT
                    // is NOT added.
                    // Perfect.

                    mappedAuthorities.add(new SimpleGrantedAuthority(localRole));
                    System.out.println("DEBUG: Enriched authorities with local role for " + email + ": " + localRole);
                }
            }

            // Check if we have any functional role. If not, default to CANDIDAT.
            boolean hasFunctionalRole = mappedAuthorities.stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                            || a.getAuthority().equals("ROLE_GESTIONNAIRE")
                            || a.getAuthority().equals("ROLE_CANDIDAT"));

            if (!hasFunctionalRole) {
                mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_CANDIDAT"));
                // System.out.println("No functional role found, assigning default:
                // ROLE_CANDIDAT");
            }

            return mappedAuthorities;
        };
    }

    private void syncUser(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        if (email == null)
            return; // Sécurité

        Utilisateur user = userRepo.findByEmail(email).orElse(null);

        if (user == null) {
            user = new Utilisateur();
            user.setEmail(email);
            user.setNom(oAuth2User.getAttribute("family_name"));
            user.setPrenom(oAuth2User.getAttribute("given_name"));
            // Le rôle est géré par Keycloak, mais on peut stocker une info par défaut en
            // BDD locale
            roleRepo.findByLibelle("CANDIDAT").ifPresent(user::setRole);
        }

        // Mise à jour systèmatique du Google ID (pour lier les comptes existants)
        user.setGoogleId(oAuth2User.getName());

        userRepo.save(user);
    }
}
