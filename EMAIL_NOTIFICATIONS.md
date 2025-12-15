# Email Notifications for Event Registrations

## Overview
Lorsqu'un utilisateur s'inscrit à un événement, le système envoie automatiquement deux emails:
1. **Email de confirmation** à l'utilisateur (enregistré)
2. **Email de notification** à l'administrateur

## Configuration

### Email Admin
L'adresse email de l'administrateur peut être configurée dans `application.properties`:

```properties
# Default admin email
app.admin.email=mahdihm140@gmail.com
```

### Email Service Configuration
Les paramètres d'envoi d'email sont configurés dans `application.properties`:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=mahdihm140@gmail.com
spring.mail.password=<app-password>
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

## Email Templates

### 1. Email de Confirmation (User)

**Destinataire**: Email de l'utilisateur inscrit
**Sujet**: `Inscription confirmée - {Event Title}`

**Contenu**:
```
Bonjour {User Name},

Merci de vous être inscrit à l'événement: {Event Title}

Détails:
- Date: {Event Start Date}
- Lieu: {Event Location}
- Votre email: {User Email}
- Téléphone: {User Phone}

À bientôt!

Cordialement,
L'équipe Wouhouch
```

**Exemple**:
```
Bonjour John Doe,

Merci de vous être inscrit à l'événement: Conference Tech 2025

Détails:
- Date: 2025-12-20T10:00:00Z
- Lieu: Paris, France
- Votre email: john@example.com
- Téléphone: +33612345678

À bientôt!

Cordialement,
L'équipe Wouhouch
```

### 2. Email de Notification (Admin)

**Destinataire**: Email configuré dans `app.admin.email`
**Sujet**: `Nouvelle inscription - {Event Title}`

**Contenu**:
```
Une nouvelle inscription a été reçue:

Événement: {Event Title}
Participant:
- Nom: {User Name}
- Email: {User Email}
- Téléphone: {User Phone}
- Contact d'urgence: {Emergency Contact}

Inscriptions totales: {Current Count}/{Capacity}
```

**Exemple**:
```
Une nouvelle inscription a été reçue:

Événement: Conference Tech 2025
Participant:
- Nom: John Doe
- Email: john@example.com
- Téléphone: +33612345678
- Contact d'urgence: Jane Doe

Inscriptions totales: 45/100
```

## Flow

1. **Utilisateur envoie une requête POST** à `/api/v1/events/{id}/registrations`
2. **Le système valide**:
   - L'événement existe
   - L'utilisateur n'est pas déjà inscrit
   - L'événement n'est pas plein
3. **L'inscription est créée** dans la base de données
4. **Le compteur d'inscriptions est mis à jour**
5. **Email de confirmation** envoyé à l'utilisateur
6. **Email de notification** envoyé à l'admin
7. **Réponse retournée** à l'utilisateur: `{ "message": "Registered" }`

## Gestion des Erreurs d'Email

Si l'envoi d'un email échoue:
- L'enregistrement reste validé et sauvegardé
- L'erreur est loggée dans la console
- L'utilisateur reçoit une réponse de succès (HTTP 200)
- Les logs peuvent être vérifiés pour déboguer

**Exemple de log d'erreur**:
```
Failed to send confirmation email to user: javax.mail.AuthenticationFailedException
Failed to send notification email to admin: java.net.SocketException
```

## Implementation Details

### Code Source
`EventServiceImpl.java` - Méthode `register()`

**Méthodes auxiliaires**:
- `sendUserConfirmationEmail()` - Envoie la confirmation à l'utilisateur
- `sendAdminNotificationEmail()` - Envoie la notification à l'admin

### Sécurité
- Les erreurs d'email n'interrompent pas l'inscription
- Les emails ne sont envoyés qu'après la validation de l'inscription
- Aucune exposition d'erreur sensible aux clients

## Testing

### Test Manual avec cURL

```bash
# 1. Récupérer un ID d'événement (événement publié)
curl http://localhost:8080/api/v1/events/upcoming

# 2. S'inscrire à un événement
curl -X POST http://localhost:8080/api/v1/events/6936881d90cde994ed52faf0/registrations \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "+33612345678",
    "emergencyContact": "Jane Doe",
    "acceptedTerms": true
  }'

# 3. Vérifier les logs pour les emails envoyés
```

### Vérification des Emails
- **Gmail**: Vérifier le dossier d'envoi et la corbeille
- **Logs**: Vérifier la console pour les messages d'erreur
- **DB**: Vérifier la collection `event_registrations` pour confirmer la création

## Troubleshooting

### Email non reçu

**Cause 1**: Authentification Gmail échouée
```
Solution: Utiliser un "App Password" au lieu du mot de passe normal
         Voir: https://support.google.com/accounts/answer/185833
```

**Cause 2**: Configuration SMTP incorrecte
```
Solution: Vérifier les paramètres dans application.properties
         - host, port, username, password
         - STARTTLS enabled
```

**Cause 3**: Email admin non configuré
```
Solution: Vérifier que app.admin.email est défini dans application.properties
         Par défaut: admin@wouhouch.com (ne reçoit pas les emails)
```

### Email reçu mais avec formatage incorrect

**Cause**: Le client email n'interprète pas correctement le texte brut
```
Solution: Utiliser SimpleMailMessage (texte brut) ou MimeMessage (HTML)
         Configuration actuelle utilise SimpleMailMessage (standard)
```

## Améliorations Futures

1. **Templates HTML** - Remplacer le texte brut par du HTML formaté
2. **Localisation** - Supporter plusieurs langues
3. **Templates personnalisés** - Permettre aux admins de personnaliser les emails
4. **Retry logic** - Réessayer l'envoi en cas d'erreur temporaire
5. **Queue d'email asynchrone** - Envoyer les emails de manière asynchrone
6. **Tracking** - Suivre l'ouverture des emails et les clics
7. **Unsubscribe** - Permettre de se désinscrire des emails de notification

## API Endpoint

### Inscription à un événement
```
POST /api/v1/events/{id}/registrations
Content-Type: application/json

{
  "name": "string (required)",
  "email": "string (required, valid email)",
  "phone": "string",
  "emergencyContact": "string",
  "acceptedTerms": "boolean"
}

Response: 200 OK
{
  "message": "Registered"
}

Emails envoyés:
- ✓ Confirmation à l'utilisateur
- ✓ Notification à l'admin
```


