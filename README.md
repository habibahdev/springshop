# SpringShop

Boutique en ligne : API REST Spring Boot, base de données PostgreSQL, Angular pour le front et le tout conteneursié avec
Docker.

---

## Présentation

| Profil             | Ce qu'il peut faire                                         |
|--------------------|-------------------------------------------------------------|
| **Visiteur**       | Parcourir le catalogue, remplir son panier, créer un compte |
| **Adhérant**       | Se connecter, commander, accèder à son espace perso         |
| **Administrateur** | Gestion des produits, catalogue, commandes                  |

**Stack** : Java 21 , Spring Boot, JWT, PostgreSQL 16, Angular 21, Boostrap 5, Docker, Nginx

---

## Structure du dépôt

```
springshop/
├── frontend/ frontend de l'application
├── src/ backend de l'application
└── docs/BACKEND.md  documentation détaillée du backend
```

---

## Lancer le projet

### Docker
**Prérequis** : Docker et Docker Compose

1. Crée le fichier `.env` à la racine du projet :
```bash
DB_PASSWORD=change-moi
JWT_SECRET=à-générer
ADMIN_EMAIL=admin@springshop.com
ADMIN_PASSWORD=change-moi-aussi
```

Générer une clé JWT :
```bash
openssl rand -hex 32
```

2. Démarrer
```bash
docker compose up -d --build
```

3. Ouvre `http://localhost:8080` et connecte-toi avec `ADMIN_EMAIL` / `ADMIN_PASSWORD` pour accèder à l'administration.

4. `docker compose up -d` suffit pour redémarrer ; ajout de `--build` qu'après une modification du docker.

---

## Documentation

Une documentation est disponible [ici](docs/index.md).

---

## Licence

[MIT](LICENSE.md)