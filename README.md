# Plateforme de Cartographie des Projets de Recherche

Application Web développée avec **Spring Boot** pour la gestion, le suivi et la visualisation (cartographie) des projets de recherche académiques.

## 📋 Description

Cette plateforme permet aux étudiants (candidats) de soumettre leurs projets et aux administrateurs/gestionnaires de suivre l'évolution de la recherche au sein de l'établissement. Elle intègre une authentification sécurisée via **Keycloak**, des tableaux de bord statistiques avancés, et une gestion financière (budget).

## 🚀 Fonctionnalités Principales

### 🔐 Authentification & Sécurité
*   **Authentification unifiée :** Connexion via Keycloak (Support Google + Formulaire local).
*   **Gestion des Rôles :**
    *   **ADMIN :** Accès total, configuration globale, gestion des utilisateurs, import CSV, logs d'audit.
    *   **GESTIONNAIRE :** Vue globale des projets, validation, accès aux statistiques.
    *   **CANDIDAT :** Soumission et gestion de ses propres projets uniquement.
*   **Synchronisation :** Synchronisation automatique des utilisateurs entre Keycloak et la base de données locale.

### 📊 Tableaux de Bord & Statistiques
*   **KPIs en temps réel :** Nombre de projets, budget total, taux d'avancement moyen.
*   **Suivi Budgétaire :** Calcul du budget restant par rapport au budget global alloué (Configurable).
*   **Alertes :** Identification visuelle des projets en retard.
*   **Graphiques :** Répartition par domaine, par statut, courbe d'évolution (Chart.js).
*   **Rapports :** Génération de rapports **PDF** complets.

### 🛠 Gestion des Projets
*   **CRUD complet :** Création, modification, suppression.
*   **Recherche :** Filtrage par mot-clé, domaine et statut.
*   **Import de données :** Import massif de projets via fichiers **CSV**.

### 📄 Documentation API
*   API documentée via **Swagger / OpenAPI**.
*   Documentation disponible en français.
*   Filtrage des routes essentielles pour une lecture simplifiée.

---

## 🛠 Stack Technique

*   **Backend :** Java 17, Spring Boot 3
*   **Base de données :** PostgreSQL
*   **Sécurité :** Spring Security, OAuth2 Client, Keycloak
*   **Frontend :** Thymeleaf, Bootstrap 5, Chart.js
*   **Outils :** Maven, Lombok, OpenPDF (Génération PDF), SpringDoc (Swagger)

---

## ⚙️ Prérequis

Avant de lancer le projet, assurez-vous d'avoir :
1.  **Java 17** installé.
2.  **PostgreSQL** installé et configuré.
3.  **Keycloak** (serveur d'authentification) lancé localement sur le port `8080`.

---

## 🔧 Installation et Configuration

### 1. Base de données
Créez une base de données PostgreSQL nommée `plateforme-recherche-db`.
Assurez-vous que les identifiants dans `src/main/resources/application.properties` correspondent aux vôtres :
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/plateforme-recherche-db
spring.datasource.username=postgres
spring.datasource.password=votre_mot_de_passe
```

### 2. Keycloak
*   Créez un Realm nommé `plateforme-recherche`.
*   Créez un Client nommé `spring-client` (Access Type: Confidential).
*   Récupérez le `client-secret` et mettez-le à jour dans `application.properties`.
*   Configurez les utilisateurs et rôles dans Keycloak ou laissez l'application les synchroniser.

### 3. Lancement de l'application
L'application est configurée pour tourner sur le port **8082** (pour ne pas entrer en conflit avec Keycloak sur 8080).

À la racine du projet :
```bash
mvn spring-boot:run
```

L'application sera accessible à l'adresse : **http://localhost:8082**

---

## 📖 Documentation de l'API (Swagger)

Pour tester les endpoints API ou consulter la documentation technique :

1.  Connectez-vous à l'application.
2.  Accédez à : **http://localhost:8082/swagger-ui/index.html**
3.  Sélectionnez la définition **"Essentiel"** en haut à droite.

Routes principales documentées :
*   `GET /projets/tous` : Liste globale.
*   `GET /admin/users` : Gestion des utilisateurs.
*   `GET /stats/dashboard` : Données statistiques.

---

## 👤 Auteurs

Projet académique réalisé dans le cadre du Master.
