Une solution complète et moderne pour la gestion automatisée d'une ferme, combinant une interface **Web (Symfony)** et une application **Desktop (JavaFX)** synchronisées.

---

## 🚀 Architecture & Intégration

Le projet repose sur une base de données partagée **MySQL**, permettant une interopérabilité parfaite entre les plateformes.

### 🌐 Partie Web (Symfony)
L'espace client web offre une expérience premium en **Glassmorphism**.
- **Sécurité** : Authentification sécurisée avec rôles (`ROLE_ADMIN`, `ROLE_USER`, `ROLE_CLIENT`).
- **Espace Client** : Dashboard dynamique avec statistiques en temps réel sur les animaux et les productions.
- **Gestion** : CRUD complet pour les Animaux, Employés et Systèmes d'Irrigation.
- **Filtrage** : Protection des données par propriétaire de ferme (chaque utilisateur ne voit que ses propres ressources).

### 🖥️ Partie Desktop (JavaFX)
Une application robuste pour la gestion opérationnelle sur site.
- **UI Premium** : Design **Dark Tech / Glassmorphism** cohérent avec l'interface Web.
- **Session Unique** : Gestion de session via `SessionManager` pour identifier l'utilisateur connecté.
- **Services Synchronisés** : Utilisation de JDBC pour interagir avec la même base de données que Symfony.
- **Dashboard Desktop** : Visualisation des KPIs (Effectifs, Animaux, Tâches) avec graphiques animés.

---

## ✨ Fonctionnalités Clés

1.  **Gestion des Animaux** : Suivi de la santé, de l'espèce et de la race.
2.  **Gestion du Personnel** : Assignation de tâches et suivi des performances (Rating).
3.  **Système d'Irrigation** : Contrôle et monitoring des vannes et capteurs de sol.
4.  **IA & Rapports** : Analyse intelligente des données et génération de rapports PDF (Intégration Gemini/IA).
5.  **Chatbot** : Assistant intelligent pour aider les fermiers dans leurs tâches quotidiennes.

---

## 🛠️ Installation & Configuration

### Prérequis
- **PHP 8.1+** & **Composer**
- **Java 17+** & **JavaFX SDK**
- **MySQL / MariaDB**

### Configuration Symfony
1. Clonez le dépôt et installez les dépendances :
   ```bash
   composer install
   ```
2. Configurez votre `.env` avec vos accès MySQL :
   ```env
   DATABASE_URL="mysql://root:@127.0.0.1:3306/smart_farm"
   ```
3. Mettez à jour le schéma de base de données :
   ```bash
   php bin/console doctrine:schema:update --force
   ```
4. Lancez le serveur :
   ```bash
   symfony serve
   ```

### Configuration JavaFX
1. Importez le projet dans votre IDE (IntelliJ/Eclipse).
2. Vérifiez la configuration JDBC dans `utils.MyDatabase`.
3. Assurez-vous que les bibliothèques JavaFX et le driver MySQL sont dans le classpath.
4. Lancez la classe `MainApp`.

---

## 🎨 Design System

Le projet utilise une esthétique **Glassmorphism Premium** :
- **Couleurs** : Dégradés `Slate` (#0f172a) vers `Indigo` (#1e1b4b).
- **Accents** : Vert Émeraude (#4ade80) pour le succès et Bleu Ciel (#38bdf8) pour l'info.
- **Effets** : Translucidité (Alpha 0.05), flou de profondeur et ombres portées douces.

---

## 👥 Équipe de Développement
Développé avec ❤️ pour la modernisation de l'agriculture.
