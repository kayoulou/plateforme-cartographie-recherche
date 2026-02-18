# 🔐 Guide d'Installation Keycloak

Pour que l'application fonctionne avec l'authentification sécurisée, vous devez lancer un serveur Keycloak.

## Option 1 : Avec Docker (Recommandé & Le Plus Simple)
Si vous avez Docker Desktop installé :

1.  Ouvrez un terminal dans ce dossier.
2.  Lancez la commande :
    ```bash
    docker-compose up
    ```
3.  Attendez que le message `Listening on: http://0.0.0.0:8080` apparaisse.
4.  C'est tout !
    *   **Console Admin** : [http://localhost:8080/admin](http://localhost:8080/admin) (User: `admin`, Pass: `admin`)
    *   **Realm** : `plateforme-recherche` (déjà créé)
    *   **Client** : `spring-client` (déjà créé)
    *   **Utilisateurs Tests** :
        *   `admin` / `admin`
        *   `gestionnaire` / `gestionnaire`
        *   `candidat` / `candidat`

## Option 2 : Installation Manuelle (Sans Docker)
Si vous n'avez pas Docker :

1.  Téléchargez Keycloak 24+ (Format ZIP) : [Site Officiel](https://www.keycloak.org/downloads)
2.  Décompressez le dossier.
3.  Ouvrez un terminal dans le dossier `bin` de Keycloak.
4.  Lancez la commande :
    ```bash
    # Windows
    kc.bat start-dev
    ```
5.  Allez sur [http://localhost:8080](http://localhost:8080).
6.  Créez le compte Admin initial (admin / admin).
7.  Allez dans la console d'administration.
8.  **Important** : Vous devrez faire la configuration manuellement ou importer le fichier json :
    *   Menu "Create Realm" -> Import -> Sélectionner le fichier `keycloak-realm-config.json` fourni dans ce projet.
