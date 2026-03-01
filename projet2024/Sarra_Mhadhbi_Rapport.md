# RAPPORT DE PROJET 2024

## Gestion Intégrée des Solutions de Sécurité Informatique

---

### RÉALISÉ PAR : Sarra Mhadhbi
### ANNÉE : 2024

---

## TABLE DES MATIÈRES

1. Introduction
2. Présentation de l'Entreprise
3. Description du Projet
4. Architecture Technique
5. Fonctionnalités Principales
6. Technologies Utilisées
7. Défis et Solutions
8. Résultats et Conclusion

---

## 1. INTRODUCTION

La sécurité informatique est devenue un enjeu majeur pour toutes les organisations modernes. Face à la multiplicité des menaces et à la complexité croissante des environnements informatiques, les entreprises cherchent des solutions intégrées et performantes pour protéger leurs données et leurs systèmes.

Ce projet vise à développer une plateforme centralisée de gestion des solutions de sécurité informatique, permettant aux administrateurs de consolider et de superviser l'ensemble de leurs outils de protection dans une interface unique et intuitive.

---

## 2. PRÉSENTATION DE L'ENTREPRISE

### Contexte Général

La société Gérance Informatique est une entreprise spécialisée dans la fourniture de solutions informatiques et de services de gestion de sécurité. Elle accompagne les organisations dans leur transformation digitale en leur proposant des technologies innovantes et des expertises métier reconnues.

### Mission de l'Entreprise

La mission de Gérance Informatique est d'assurer la continuité de service et la protection des actifs informatiques des entreprises. L'organisation met en place des stratégies globales de sécurité, d'infrastructure et de support technique pour permettre aux clients de se concentrer sur leur cœur de métier.

### Services Proposés

- Gestion des solutions antivirus et antimalware
- Intégration de solutions de sécurité tierces
- Surveillance et alertes de sécurité
- Gestion centralisée des licenses et des configurations
- Support et assistance technique

---

## 3. DESCRIPTION DU PROJET

### Objectifs Généraux

Le projet vise à créer une application web complète permettant :

- **La gestion centralisée** de multiples solutions de sécurité informatique (Bitdefender, Kaspersky, F5, Fortinet, Cisco, etc.)
- **Le monitoring en temps réel** des statuts de protection des systèmes
- **L'administration simplifiée** des configurations de sécurité
- **La génération de rapports** sur l'état de sécurité globale
- **La gestion des utilisateurs** et des permissions d'accès

### Objectifs Spécifiques

1. Centraliser les interfaces de gestion de 20+ solutions de sécurité
2. Fournir un tableau de bord unifié avec des indicateurs clés de sécurité
3. Automatiser les alertes et les notifications de sécurité
4. Permettre le déploiement et la mise à jour des solutions de sécurité
5. Générer des rapports conformes aux standards de sécurité (ISO 27001, NIST)

---

## 4. ARCHITECTURE TECHNIQUE

### Stack Technologique

#### Backend
- **Framework** : Spring Boot 3.2.2
- **Langage** : Java 17
- **Base de Données** : MySQL
- **Sécurité** : Spring Security avec JWT (JSON Web Tokens)
- **API Documentation** : Swagger/OpenAPI

#### Frontend
- **Framework** : Angular 14.2.0
- **Template UI** : Light Bootstrap Dashboard
- **Style** : SCSS et Bootstrap 3
- **Graphiques** : Chart.js et Chartist

#### Infrastructure
- **Serveur** : Port 8089
- **Upload de fichiers** : Support jusqu'à 10MB
- **Authentification** : JWT avec expiration 24h
- **Email** : Configuration SMTP (Gmail)

### Architecture Globale

```
┌─────────────────────────────────────────────────────┐
│           FRONT-END (Angular 14)                    │
│    Light Bootstrap Dashboard Interface               │
└──────────────────┬──────────────────────────────────┘
                   │ REST API (JSON)
                   │ JWT Authentication
┌──────────────────▼──────────────────────────────────┐
│         BACK-END (Spring Boot 3.2.2)                │
│  - Controllers (API Endpoints)                       │
│  - Services (Business Logic)                         │
│  - Security Layer (JWT + Spring Security)            │
│  - Email Service (SMTP)                              │
│  - File Upload Management                            │
└──────────────────┬──────────────────────────────────┘
                   │ JDBC
┌──────────────────▼──────────────────────────────────┐
│      DATABASE (MySQL)                               │
│  - Users & Permissions                              │
│  - Configuration Solutions                          │
│  - Audit Logs                                        │
│  - Reports Data                                      │
└─────────────────────────────────────────────────────┘
```

---

## 5. FONCTIONNALITÉS PRINCIPALES

### 5.1 Authentification et Gestion des Utilisateurs

- Système d'authentification sécurisé avec JWT
- Gestion des rôles et permissions (Admin, Manager, User)
- Contrôle d'accès basé sur les rôles (RBAC)
- Historique des connexions et des actions

### 5.2 Tableau de Bord Unifié

- Vue d'ensemble de tous les systèmes de sécurité
- Indicateurs de santé en temps réel
- Alertes et notifications prioritaires
- Graphiques de tendances et statistiques

### 5.3 Gestion des Solutions de Sécurité Intégrées

L'application supporte l'intégration avec plus de 20 solutions incluant :

**Antivirus et Antimalware :**
- Bitdefender
- ESET
- Kaspersky (via Alwarebytes)

**Firewalls et Contrôle d'Accès :**
- Fortinet FortiGate
- Cisco ASA
- Palo Alto Networks
- F5 Networks

**Prévention des Menaces :**
- Crowdstrike Falcon
- Rapid7 InsightIDR
- Netskope

**Sécurité Email :**
- Proofpoint
- Microsoft Office 365 Security

**Sécurité des Données :**
- Varonis Data Security Platform
- Imperva Data Center Security

**Infrastructure et Identité :**
- One Identity Solutions
- InfoBlox (DNS/DHCP)
- Cisco SecureX

**Autres Solutions :**
- Splunk Security Monitoring
- VMware NSX
- Veeam Backup & Recovery
- Wallix PAM
- Fortra Secure Solutions

### 5.4 Gestion des Alertes

- Configuration des règles d'alerte personnalisées
- Escalade automatique des alertes critiques
- Notification par email en temps réel
- Historique complet des alertes

### 5.5 Gestion des Rapports

- Génération de rapports personnalisés
- Export en formats multiples (PDF, Excel)
- Planification de rapports automatiques
- Conformité avec les standards de sécurité

### 5.6 Gestion des Profils Utilisateur

- Affichage et modification des informations de profil
- Gestion des préférences de sécurité
- Historique des activités de l'utilisateur
- Gestion des appareils autorisés

---

## 6. TECHNOLOGIES UTILISÉES

### Dépendances Principales

#### Spring Boot
- `spring-boot-starter-web` : Support REST API
- `spring-boot-starter-security` : Gestion de la sécurité
- `spring-boot-starter-data-jpa` : Accès aux données
- `spring-boot-starter-mail` : Service de messagerie

#### Sécurité et JWT
- `jjwt` (JSON Web Token) v0.11.5
- Spring Security avec JWT Bearer Token

#### Frontend
- `@angular/core` et `@angular/common`
- `@angular/material` : Composants UI avancés
- `bootstrap` : Framework CSS
- `chart.js` : Graphiques
- `rxjs` : Programmation réactive

#### Base de Données
- `mysql-connector-j` : Driver MySQL
- `Hibernate` : ORM
- `MapStruct` : Mapping d'entités

### Outils de Développement
- Maven 3.x : Build automation
- Java 17 : Langage de programmation
- Angular CLI 14 : Développement frontend
- npm/Node.js : Gestion des dépendances

---

## 7. DÉFIS ET SOLUTIONS

### Défi 1 : Intégration de Multiples Solutions Hétérogènes

**Problème :** Chaque solution de sécurité possède ses propres APIs et formats de données.

**Solution :** 
- Création d'une couche d'abstraction avec interfaces génériques
- Utilisation de MapStruct pour transformer les données
- Développement de contrôleurs spécifiques par solution

### Défi 2 : Sécurité et Authentification

**Problème :** Protéger les données sensibles et les credentials des solutions intégrées.

**Solution :**
- Implémentation de JWT avec expiration 24h
- Chiffrement des credentials en base de données
- Spring Security avec configuration personnalisée

### Défi 3 : Performance et Scalabilité

**Problème :** Gérer les requêtes simultanées de multiples utilisateurs.

**Solution :**
- Architecture microservices ready
- Cache de données fréquemment accédées
- Optimisation des requêtes JPA

### Défi 4 : Upload de Fichiers Volumineux

**Problème :** Gestion de fichiers de configuration jusqu'à 10MB.

**Solution :**
- Configuration multipart optimisée
- Validation du type et de la taille
- Stockage sécurisé dans `/uploads/profiles/`

---

## 8. RÉSULTATS ET CONCLUSION

### Résultats Obtenus

✅ **Plateforme Opérationnelle :** Application web complète et fonctionnelle

✅ **Interface Conviviale :** Dashboard intuitif pour les administrateurs

✅ **Intégrations Multiples :** 20+ solutions de sécurité intégrées

✅ **Sécurité Renforcée :** Authentification JWT, contrôle d'accès RBAC

✅ **Scalabilité :** Architecture permettant l'ajout de nouvelles solutions

### Points Forts du Projet

1. **Centralisation :** Gestion unique de multiples solutions
2. **Facilité d'Utilisation :** Interface moderne et responsive
3. **Sécurité :** Multiples couches de protection
4. **Extensibilité :** Facile d'ajouter de nouvelles solutions
5. **Documentation :** API documentée avec Swagger

### Améliorations Futures

1. **Analytics Avancée :** Machine Learning pour la détection d'anomalies
2. **Automation :** Orchestration automatique des réponses aux incidents
3. **Mobile App :** Application mobile native pour monitoring mobile
4. **Blockchain :** Audit trail immuable des actions de sécurité
5. **Multi-tenant :** Support pour gérer plusieurs organisations

### Conclusion

Ce projet démontre la capacité à développer une solution d'entreprise complète et sécurisée, intégrant de multiples technologies et systèmes. L'application offre une gestion centralisée de la sécurité informatique, essentielle pour les organisations modernes.

Les technologies modernes utilisées (Spring Boot 3, Angular 14, JWT) garantissent une solution robuste, maintenable et évolutive. La plateforme est prête pour une utilisation en production et peut facilement s'adapter aux évolutions futures des solutions de sécurité.

La gestion intégrée des solutions de sécurité représente un avantage compétitif majeur pour Gérance Informatique et ses clients, en leur permettant de centraliser, monitorer et optimiser leur posture de sécurité globale.

---

## ANNEXES

### A. Contrôleurs API Implémentés

- AuthController : Gestion d'authentification
- DashboardController : Tableau de bord
- UserRestController : Gestion des utilisateurs
- [20+ contrôleurs pour chaque solution de sécurité]

### B. Structure de Base de Données

- Tables Utilisateurs et Rôles
- Tables de Configuration des Solutions
- Tables d'Audit et de Logs
- Tables de Rapports

### C. Configuration Sécurité

- JWT Secret configuré avec 64+ caractères
- Expiration JWT : 24 heures
- CORS configuré pour le frontend
- Multipart upload sécurisé

---

**Rapport réalisé par : Sarra Mhadhbi**
**Date : Novembre 2024**
**Projet : Gestion Intégrée des Solutions de Sécurité Informatique**
