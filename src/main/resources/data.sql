-- Insertion des rôles s'ils n'existent pas
INSERT INTO role (id, libelle) VALUES (1, 'ADMIN') ON CONFLICT (id) DO NOTHING;
INSERT INTO role (id, libelle) VALUES (2, 'GESTIONNAIRE') ON CONFLICT (id) DO NOTHING;
INSERT INTO role (id, libelle) VALUES (3, 'CANDIDAT') ON CONFLICT (id) DO NOTHING;

-- Insertion des utilisateurs par défaut (Mot de passe = "password" pour tous, hashé avec BCrypt)
-- Hash pour "password" : $2a$10$eAccYoNOHEqXve8aIWT8Nu3ueN6NB9ITsUg.fK4isCJLJ.4lX9u3q

INSERT INTO utilisateur (id, nom, prenom, email, mot_de_passe, actif, id_role) 
VALUES (1, 'Admin', 'System', 'admin@test.com', '$2a$10$eAccYoNOHEqXve8aIWT8Nu3ueN6NB9ITsUg.fK4isCJLJ.4lX9u3q', true, 1)
ON CONFLICT (email) DO UPDATE 
SET mot_de_passe = '$2a$10$eAccYoNOHEqXve8aIWT8Nu3ueN6NB9ITsUg.fK4isCJLJ.4lX9u3q', id_role = 1;

INSERT INTO utilisateur (id, nom, prenom, email, mot_de_passe, actif, id_role) 
VALUES (2, 'Manager', 'Pierre', 'manager@test.com', '$2a$10$eAccYoNOHEqXve8aIWT8Nu3ueN6NB9ITsUg.fK4isCJLJ.4lX9u3q', true, 2)
ON CONFLICT (email) DO UPDATE 
SET mot_de_passe = '$2a$10$eAccYoNOHEqXve8aIWT8Nu3ueN6NB9ITsUg.fK4isCJLJ.4lX9u3q', id_role = 2;

INSERT INTO utilisateur (id, nom, prenom, email, mot_de_passe, actif, id_role) 
VALUES (3, 'Candidat', 'Jean', 'candidat1@test.com', '$2a$10$eAccYoNOHEqXve8aIWT8Nu3ueN6NB9ITsUg.fK4isCJLJ.4lX9u3q', true, 3)
ON CONFLICT (email) DO UPDATE 
SET mot_de_passe = '$2a$10$eAccYoNOHEqXve8aIWT8Nu3ueN6NB9ITsUg.fK4isCJLJ.4lX9u3q', id_role = 3;

-- Réinitialisation de la séquence pour éviter les conflits d'ID futurs (PostgreSQL)
SELECT setval('utilisateur_id_seq', (SELECT MAX(id) FROM utilisateur));
