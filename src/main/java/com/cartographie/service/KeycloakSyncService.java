package com.cartographie.service;

import com.cartographie.model.Utilisateur;
import com.cartographie.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakSyncService {

    private final UtilisateurRepository utilisateurRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;

    public String syncUsersToKeycloak() {
        try {
            String token = getAccessToken();
            if (token == null) {
                return "Erreur : Impossible d'obtenir le token d'administration Keycloak (Vérifiez les droits du client spring-client).";
            }

            List<Utilisateur> users = utilisateurRepository.findAll();
            int createdCount = 0;
            int existingCount = 0;
            int errorCount = 0;

            for (Utilisateur user : users) {
                try {
                    if (userExists(token, user.getEmail())) {
                        existingCount++;
                    } else {
                        createUser(token, user);
                        createdCount++;
                    }
                } catch (Exception e) {
                    System.err.println("Erreur creation user " + user.getEmail() + ": " + e.getMessage());
                    errorCount++;
                }
            }

            return String.format("Sync terminé : %d créés, %d existants, %d erreurs.", createdCount, existingCount,
                    errorCount);

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur globale sync : " + e.getMessage();
        }
    }

    private String getAccessToken() {
        String tokenUrl = issuerUri + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=client_credentials" +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret;

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("access_token");
            }
        } catch (Exception e) {
            System.err.println("Failed to get token: " + e.getMessage());
        }
        return null;
    }

    private boolean userExists(String token, String email) {
        // Correct endpoint for creating/listing users is /admin/realms/{realm}/users
        // issuerUri is .../realms/{realm}
        // So we presume .../admin/realms/{realm}/users
        String adminApiUrl = issuerUri.replace("/realms/", "/admin/realms/") + "/users?email=" + email;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(adminApiUrl, HttpMethod.GET, request, List.class);
            return response.getBody() != null && !response.getBody().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private void createUser(String token, Utilisateur user) {
        String adminApiUrl = issuerUri.replace("/realms/", "/admin/realms/") + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> userJson = new HashMap<>();
        userJson.put("username", user.getEmail()); // Use email as username for simplicity
        userJson.put("email", user.getEmail());
        userJson.put("firstName", user.getPrenom());
        userJson.put("lastName", user.getNom());
        userJson.put("enabled", true);
        userJson.put("emailVerified", true);

        // Credentials
        Map<String, Object> credential = new HashMap<>();
        credential.put("type", "password");
        credential.put("value", "12345678"); // Default password
        credential.put("temporary", true); // User must change it? Or false.

        userJson.put("credentials", Collections.singletonList(credential));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(userJson, headers);
        restTemplate.postForEntity(adminApiUrl, request, Void.class);

        // Assign Role?
        // Need to fetch Role ID from Keycloak then assign. Complex.
        // For now, let's just create the user.
    }

    public boolean updateUserPassword(String email, String newPassword) {
        try {
            String token = getAccessToken();
            if (token == null)
                return false;

            String userId = getKeycloakUserId(token, email);
            if (userId == null)
                return false;

            // Endpoint: /admin/realms/{realm}/users/{id}/reset-password
            String adminApiUrl = issuerUri.replace("/realms/", "/admin/realms/") + "/users/" + userId
                    + "/reset-password";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            Map<String, Object> credential = new HashMap<>();
            credential.put("type", "password");
            credential.put("value", newPassword);
            credential.put("temporary", false);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(credential, headers);
            restTemplate.put(adminApiUrl, request);

            return true;
        } catch (Exception e) {
            System.err.println("Failed to update Keycloak password: " + e.getMessage());
            return false;
        }
    }

    private String getKeycloakUserId(String token, String email) {
        String adminApiUrl = issuerUri.replace("/realms/", "/admin/realms/") + "/users?email=" + email;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(adminApiUrl, HttpMethod.GET, request, List.class);
            if (response.getBody() != null && !response.getBody().isEmpty()) {
                Map<String, Object> user = (Map<String, Object>) response.getBody().get(0);
                return (String) user.get("id");
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
