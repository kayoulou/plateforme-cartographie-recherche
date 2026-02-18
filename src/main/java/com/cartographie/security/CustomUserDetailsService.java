package com.cartographie.security;

import com.cartographie.model.Utilisateur;
import com.cartographie.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        // On suppose que les utilisateurs locaux ont un mot de passe défini
        // S'ils viennent de Google, le mot de passe est null, donc ils ne peuvent pas
        // se connecter via formulaire
        if (utilisateur.getMotDePasse() == null || utilisateur.getMotDePasse().isEmpty()) {
            throw new UsernameNotFoundException("Pas de mot de passe défini pour cet utilisateur (Compte Google ?)");
        }

        String role = "USER";
        if (utilisateur.getRole() != null) {
            role = utilisateur.getRole().getLibelle().toUpperCase();
        }

        return new User(
                utilisateur.getEmail(),
                utilisateur.getMotDePasse(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
    }
}
