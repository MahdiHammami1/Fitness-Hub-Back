package com.example.demo.service;

import java.util.List;
import com.example.demo.model.OrderItem;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class EmailTemplates {

  private static final String ADMIN_SUBTITLE = "Admin • Wouhouch Hub";

  // prevent instantiation
  private EmailTemplates() {}

  // méthode qui retourne l'enveloppe HTML (shell) pour tous les emails
  private static String shell(String title, String subtitle, String contentHtml) {
    return """
    <!doctype html>
    <html>
    <head>
      <meta charset="utf-8">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <title>%s</title>
    </head>
    <body style="margin:0;padding:0;background:#0B0B0B;font-family:Arial,Helvetica,sans-serif;color:#F5F5F5;">
      <table width="100%%" cellpadding="0" cellspacing="0" style="background:#0B0B0B;padding:24px 12px;">
        <tr>
          <td align="center">
            <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px;background:#111;border:1px solid #1f1f1f;border-radius:16px;overflow:hidden;">
              <tr>
                <td style="padding:20px 24px;background:#000;">
                  <div style="font-size:20px;font-weight:700;letter-spacing:.5px;">
                    <span style="color:#E10600;">Wouhouch</span> Hub
                  </div>
                  <div style="margin-top:6px;font-size:13px;color:#BDBDBD;">%s</div>
                </td>
              </tr>

              <tr>
                <td style="padding:24px;">
                  <div style="font-size:18px;font-weight:700;margin:0 0 10px 0;">%s</div>
                  %s
                </td>
              </tr>

              <tr>
                <td style="padding:16px 24px;background:#0d0d0d;border-top:1px solid #1f1f1f;">
                  <div style="font-size:12px;color:#9a9a9a;line-height:1.5;">
                    Vous recevez cet email car vous avez interagi avec Wouhouch Hub.<br>
                    © %d Wouhouch Hub — Tous droits réservés.
                  </div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </body>
    </html>
    """.formatted(escape(title), escape(subtitle), escape(title), contentHtml, java.time.Year.now().getValue());
  }

  public static String registrationConfirmation(String name, String eventName) {
    String content = """
      <p style="margin:0 0 14px 0;color:#EDEDED;line-height:1.6;">
        Bonjour <b>%s</b>,<br>
        Votre inscription à l'événement a bien été enregistrée.
      </p>

      <div style="background:#0b0b0b;border:1px solid #1f1f1f;border-radius:12px;padding:14px 16px;margin:14px 0;">
        <div style="font-size:12px;color:#BDBDBD;margin-bottom:6px;">Récapitulatif</div>
        <div style="font-size:14px;line-height:1.6;">
          <div><span style="color:#BDBDBD;">Événement:</span> <span style="color:#fff;">%s</span></div>
        </div>
      </div>

      <p style="margin:14px 0 0 0;color:#BDBDBD;line-height:1.6;">
        Si vous avez une question, répondez à cet email ou contactez-nous via Instagram/WhatsApp.
      </p>
    """.formatted(escape(name), escape(eventName));

    return shell("Confirmation d'inscription", "Événements • Wouhouch Hub", content);
  }

  public static String adminNewRegistration(String eventName, String name, String email, String phone, String emergency, String createdAtIso) {
    String content = """
      <p style="margin:0 0 14px 0;color:#EDEDED;line-height:1.6;">
        Nouvelle inscription reçue.
      </p>

      <div style="background:#0b0b0b;border:1px solid #1f1f1f;border-radius:12px;padding:14px 16px;">
        <div style="display:flex;gap:8px;flex-wrap:wrap;font-size:14px;line-height:1.7;">
          <div><span style="color:#BDBDBD;">Événement:</span> <span style="color:#fff;">%s</span></div><br>
          <div><span style="color:#BDBDBD;">Nom:</span> <span style="color:#fff;">%s</span></div><br>
          <div><span style="color:#BDBDBD;">Email:</span> <span style="color:#fff;">%s</span></div><br>
          <div><span style="color:#BDBDBD;">Téléphone:</span> <span style="color:#fff;">%s</span></div><br>
          <div><span style="color:#BDBDBD;">Urgence:</span> <span style="color:#fff;">%s</span></div><br>
          <div><span style="color:#BDBDBD;">Date:</span> <span style="color:#fff;">%s</span></div>
        </div>
      </div>
    """.formatted(
        escape(eventName), escape(name), escape(email), escape(phone), escape(emergency), escape(createdAtIso)
    );

    return shell("Nouvelle inscription", ADMIN_SUBTITLE, content);
  }

  // New: order confirmation for customer
  public static String orderConfirmation(String customerName, String orderId, String total, String status) {
    String content = """
      <p style="margin:0 0 14px 0;color:#EDEDED;line-height:1.6;">
        Bonjour <b>%s</b>,<br>
        Merci pour votre commande. Voici le récapitulatif de votre commande.
      </p>

      <div style="background:#0b0b0b;border:1px solid #1f1f1f;border-radius:12px;padding:14px 16px;margin:14px 0;">
        <div style="font-size:12px;color:#BDBDBD;margin-bottom:6px;">Récapitulatif de la commande</div>
        <div style="font-size:14px;line-height:1.6;">
          <div><span style="color:#BDBDBD;">ID commande:</span> <span style="color:#fff;">%s</span></div>
          <div><span style="color:#BDBDBD;">Total:</span> <span style="color:#fff;">%s</span></div>
          <div><span style="color:#BDBDBD;">Statut:</span> <span style="color:#fff;">%s</span></div>
        </div>
      </div>

      <p style="margin:14px 0 0 0;color:#BDBDBD;line-height:1.6;">
        Si vous avez une question, répondez à cet email ou contactez-nous via Instagram/WhatsApp.
      </p>
    """.formatted(escape(customerName), escape(orderId), escape(total), escape(status));

    return shell("Confirmation de commande", "Commande • Wouhouch Hub", content);
  }

  // New: admin notification for new order
  public static String adminNewOrder(String orderId, String customerName, String email, String phone, String total, String createdAtIso) {
    String content = """
      <p style="margin:0 0 14px 0;color:#EDEDED;line-height:1.6;">
        Nouvelle commande reçue.
      </p>

      <div style="background:#0b0b0b;border:1px solid #1f1f1f;border-radius:12px;padding:14px 16px;">
        <div style="display:flex;gap:8px;flex-wrap:wrap;font-size:14px;line-height:1.7;">
          <div><span style="color:#BDBDBD;">ID commande:</span> <span style="color:#fff;">%s</span></div><br>
          <div><span style="color:#BDBDBD;">Nom:</span> <span style="color:#fff;">%s</span></div><br>
          <div><span style="color:#BDBDBD;">Email:</span> <span style="color:#fff;">%s</span></div><br>
          <div><span style="color:#BDBDBD;">Téléphone:</span> <span style="color:#fff;">%s</span></div><br>
          <div><span style="color:#BDBDBD;">Total:</span> <span style="color:#fff;">%s</span></div><br>
          <div><span style="color:#BDBDBD;">Date:</span> <span style="color:#fff;">%s</span></div>
        </div>
      </div>
    """.formatted(
        escape(orderId), escape(customerName), escape(email), escape(phone), escape(total), escape(createdAtIso)
    );

    return shell("Nouvelle commande", ADMIN_SUBTITLE, content);
  }

  // Ajout : construit un tableau HTML récapitulant les produits commandés
  public static String buildProductsTable(List<OrderItem> items) {
    if (items == null || items.isEmpty()) return "";

    StringBuilder sb = new StringBuilder();
    sb.append("<div style=\"margin:14px 0;\">\n");
    sb.append("<table width=\"100%\" cellpadding=\"6\" cellspacing=\"0\" style=\"border-collapse:collapse;background:#0b0b0b;border:1px solid #1f1f1f;border-radius:8px;overflow:hidden;\">\n");
    sb.append("<thead><tr>\n");
    sb.append("<th align=\"left\" style=\"color:#BDBDBD;font-size:12px;border-bottom:1px solid #1f1f1f;padding:8px;\">Produit</th>\n");
    sb.append("<th align=\"left\" style=\"color:#BDBDBD;font-size:12px;border-bottom:1px solid #1f1f1f;padding:8px;\">Variante</th>\n");
    sb.append("<th align=\"right\" style=\"color:#BDBDBD;font-size:12px;border-bottom:1px solid #1f1f1f;padding:8px;\">Qté</th>\n");
    sb.append("<th align=\"right\" style=\"color:#BDBDBD;font-size:12px;border-bottom:1px solid #1f1f1f;padding:8px;\">Prix unitaire</th>\n");
    sb.append("<th align=\"right\" style=\"color:#BDBDBD;font-size:12px;border-bottom:1px solid #1f1f1f;padding:8px;\">Total</th>\n");
    sb.append("</tr></thead><tbody>\n");

    for (OrderItem it : items) {
      String title = escape(it.getProductTitleSnapshot() == null ? "" : it.getProductTitleSnapshot());
      String variant = escape(it.getVariantSnapshot() == null ? "" : it.getVariantSnapshot());
      int qty = it.getQty() == null ? 0 : it.getQty();
      BigDecimal unit = it.getUnitPrice() == null ? BigDecimal.ZERO : it.getUnitPrice();
      BigDecimal lineTotal = unit.multiply(BigDecimal.valueOf(qty));

      sb.append("<tr>\n");
      sb.append("<td style=\"color:#fff;padding:8px 6px;border-bottom:1px solid #1f1f1f;\"")
        .append(title)
        .append("</td>\n");
      sb.append("<td style=\"color:#BDBDBD;padding:8px 6px;border-bottom:1px solid #1f1f1f;\"")
        .append(variant)
        .append("</td>\n");
      sb.append("<td align=\"right\" style=\"color:#fff;padding:8px 6px;border-bottom:1px solid #1f1f1f;\"")
        .append(qty)
        .append("</td>\n");
      sb.append("<td align=\"right\" style=\"color:#fff;padding:8px 6px;border-bottom:1px solid #1f1f1f;\"")
        .append(unit.setScale(2, RoundingMode.HALF_UP))
        .append("</td>\n");
      sb.append("<td align=\"right\" style=\"color:#fff;padding:8px 6px;border-bottom:1px solid #1f1f1f;\"")
        .append(lineTotal.setScale(2, RoundingMode.HALF_UP))
        .append("</td>\n");
      sb.append("</tr>\n");
    }

    sb.append("</tbody></table></div>\n");
    return sb.toString();
  }

  // Surcharge : confirmation de commande (client) avec tableau des produits
  public static String orderConfirmation(String customerName, String orderId, String total, String status, List<OrderItem> items) {
    String productsHtml = buildProductsTable(items);

    String content = """
      <p style="margin:0 0 14px 0;color:#EDEDED;line-height:1.6;">
        Bonjour <b>%s</b>,<br>
        Merci pour votre commande. Voici le récapitulatif de votre commande.
      </p>

      <div style="background:#0b0b0b;border:1px solid #1f1f1f;border-radius:12px;padding:14px 16px;margin:14px 0;">
        <div style="font-size:12px;color:#BDBDBD;margin-bottom:6px;">Récapitulatif de la commande</div>
        <div style="font-size:14px;line-height:1.6;">
          <div><span style="color:#BDBDBD;">ID commande:</span> <span style="color:#fff;">%s</span></div>
          <div><span style="color:#BDBDBD;">Total:</span> <span style="color:#fff;">%s</span></div>
          <div><span style="color:#BDBDBD;">Statut:</span> <span style="color:#fff;">%s</span></div>
        </div>
      </div>

      %s

      <p style="margin:14px 0 0 0;color:#BDBDBD;line-height:1.6;">
        Si vous avez une question, répondez à cet email ou contactez-nous via Instagram/WhatsApp.
      </p>
    """.formatted(escape(customerName), escape(orderId), escape(total), escape(status), productsHtml);

    return shell("Confirmation de commande", "Commande • Wouhouch Hub", content);
  }

  // Surcharge : notification admin pour nouvelle commande avec tableau produits
  public static String adminNewOrder(String orderId, String customerName, String email, String phone, String total, String createdAtIso, List<OrderItem> items) {
    String productsHtml = buildProductsTable(items);

    String content = """
      <p style="margin:0 0 14px 0;color:#EDEDED;line-height:1.6;">
        Nouvelle commande reçue.
      </p>

      <div style="background:#0b0b0b;border:1px solid #1f1f1f;border-radius:12px;padding:14px 16px;">
        <div style="display:flex;gap:8px;flex-wrap:wrap;font-size:14px;line-height:1.7;">
          <div><span style="color:#BDBDBD;">ID commande:</span> <span style="color:#fff;">%s</span></div><br>
          <div><span style="color:#BDBDBD;">Nom:</span> <span style="color:#fff;">%s</span></div><br>
          <div><span style="color:#BDBDBD;">Email:</span> <span style="color:#fff;">%s</span></div><br>
          <div><span style="color:#BDBDBD;">Téléphone:</span> <span style="color:#fff;">%s</span></div><br>
          <div><span style="color:#BDBDBD;">Total:</span> <span style="color:#fff;">%s</span></div><br>
          <div><span style="color:#BDBDBD;">Date:</span> <span style="color:#fff;">%s</span></div>
        </div>
      </div>

      %s
    """.formatted(
        escape(orderId), escape(customerName), escape(email), escape(phone), escape(total), escape(createdAtIso), productsHtml
    );

    return shell("Nouvelle commande", ADMIN_SUBTITLE, content);
  }

  // utilitaire simple pour échapper quelques caractères HTML
  private static String escape(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  // ...existing code...

  // --- Verification and Password Reset Templates ---

  public static String emailVerification(String name, String verificationCode) {
    String content = """
      <p style="margin:0 0 14px 0;color:#EDEDED;line-height:1.6;">
        Bonjour <b>%s</b>,<br>
        Bienvenue chez Wouhouch Hub ! Pour finaliser votre inscription, veuillez vérifier votre adresse email.
      </p>

      <div style="background:#0b0b0b;border:1px solid #1f1f1f;border-radius:12px;padding:14px 16px;margin:14px 0;">
        <div style="font-size:12px;color:#BDBDBD;margin-bottom:6px;">Code de vérification</div>
        <div style="font-size:24px;font-weight:700;color:#E10600;letter-spacing:2px;text-align:center;padding:12px;">
          %s
        </div>
        <div style="font-size:12px;color:#BDBDBD;text-align:center;margin-top:8px;">
          Ce code expire dans 24 heures
        </div>
      </div>

      <p style="margin:14px 0 0 0;color:#BDBDBD;line-height:1.6;">
        Si vous n'avez pas créé de compte, ignorez simplement cet email.
      </p>
    """.formatted(escape(name), escape(verificationCode));

    return shell("Vérification de votre compte", "Sécurité • Wouhouch Hub", content);
  }

  public static String passwordReset(String name, String resetCode) {
    String content = """
      <p style="margin:0 0 14px 0;color:#EDEDED;line-height:1.6;">
        Bonjour <b>%s</b>,<br>
        Nous avons reçu une demande de réinitialisation de votre mot de passe.
      </p>

      <div style="background:#0b0b0b;border:1px solid #1f1f1f;border-radius:12px;padding:14px 16px;margin:14px 0;">
        <div style="font-size:12px;color:#BDBDBD;margin-bottom:6px;">Code de réinitialisation</div>
        <div style="font-size:24px;font-weight:700;color:#E10600;letter-spacing:2px;text-align:center;padding:12px;">
          %s
        </div>
        <div style="font-size:12px;color:#BDBDBD;text-align:center;margin-top:8px;">
          Ce code expire dans 30 minutes
        </div>
      </div>

      <p style="margin:14px 0 0 0;color:#BDBDBD;line-height:1.6;">
        Si vous n'avez pas demandé la réinitialisation de votre mot de passe, ignorez simplement cet email.
      </p>
    """.formatted(escape(name), escape(resetCode));

    return shell("Réinitialisation de votre mot de passe", "Sécurité • Wouhouch Hub", content);
  }

  // --- Contact templates ---
  public static String contactConfirmation(String name, String email, String subject, String message) {
    String content = """
      <p style="margin:0 0 14px 0;color:#EDEDED;line-height:1.6;">
        Bonjour <b>%s</b>,<br>
        Merci pour votre message. Nous l'avons bien reçu et reviendrons vers vous rapidement.
      </p>

      <div style="background:#0b0b0b;border:1px solid #1f1f1f;border-radius:12px;padding:14px 16px;margin:14px 0;">
        <div style="font-size:12px;color:#BDBDBD;margin-bottom:8px;font-weight:700;">Détails du message</div>
        <div style="font-size:14px;line-height:1.6;">
          <div><span style="color:#BDBDBD;">Email:</span> <span style="color:#fff;">%s</span></div>
          <div><span style="color:#BDBDBD;">Sujet:</span> <span style="color:#fff;">%s</span></div>
          <div style="margin-top:8px;"><span style="color:#BDBDBD;">Message:</span>
            <div style="color:#fff;margin-top:6px;">%s</div>
          </div>
        </div>
      </div>

      <p style="margin:14px 0 0 0;color:#BDBDBD;line-height:1.6;">
        Si vous avez d'autres informations à ajouter, répondez simplement à cet email.
      </p>
    """.formatted(escape(name), escape(email), escape(subject), escape(message));

    return shell("Message reçu", "Support • Wouhouch Hub", content);
  }

  public static String contactConfirmation(com.example.demo.model.Contact contact) {
    if (contact == null) return contactConfirmation("Client", "", "", "");
    return contactConfirmation(contact.getName(), contact.getEmail(), contact.getSubject(), contact.getMessage());
  }

  public static String adminContactNotification(String name, String email, String subject, String message) {
    String content = """
      <p style="margin:0 0 14px 0;color:#EDEDED;line-height:1.6;">
        Nouveau message de contact reçu.
      </p>

      <div style="background:#0b0b0b;border:1px solid #1f1f1f;border-radius:12px;padding:14px 16px;margin:14px 0;">
        <div style="font-size:12px;color:#BDBDBD;margin-bottom:8px;font-weight:700;">Détails du contact</div>
        <div style="font-size:14px;line-height:1.6;">
          <div><span style="color:#BDBDBD;">Nom:</span> <span style="color:#fff;">%s</span></div>
          <div><span style="color:#BDBDBD;">Email:</span> <span style="color:#fff;">%s</span></div>
          <div><span style="color:#BDBDBD;">Sujet:</span> <span style="color:#fff;">%s</span></div>
          <div style="margin-top:8px;"><span style="color:#BDBDBD;">Message:</span>
            <div style="color:#fff;margin-top:6px;">%s</div>
          </div>
        </div>
      </div>

      <p style="margin:14px 0 0 0;color:#BDBDBD;line-height:1.6;">Consultez le panneau d'administration pour répondre ou suivre ce message.</p>
    """.formatted(escape(name), escape(email), escape(subject), escape(message));

    return shell("Nouveau message de contact", ADMIN_SUBTITLE, content);
  }

  public static String adminContactNotification(com.example.demo.model.Contact contact) {
    if (contact == null) return adminContactNotification("", "", "", "");
    return adminContactNotification(contact.getName(), contact.getEmail(), contact.getSubject(), contact.getMessage());
  }

}
