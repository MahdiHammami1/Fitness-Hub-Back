package com.example.demo.service.email;

import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;

import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class EmailTemplates {

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
                    Vous recevez cet email car vous avez effectué une commande sur Wouhouch Hub.<br>
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

  public static String orderConfirmation(Order order, List<OrderItem> items) {
    String customerName = order.getCustomerName() != null ? order.getCustomerName() : "Client";
    String orderId = order.getId() != null ? order.getId() : "";
    String total = order.getTotal() != null ? String.format("%.2f", order.getTotal()) : "0.00";

    String itemsHtml = buildProductsTable(items);

    String content = """
      <p style="margin:0 0 14px 0;color:#EDEDED;line-height:1.6;">
        Bonjour <b>%s</b>,<br>
        Votre commande a bien été reçue et confirmée.
      </p>

      <div style="background:#0b0b0b;border:1px solid #1f1f1f;border-radius:12px;padding:16px;margin:14px 0;">
        <div style="font-size:12px;color:#BDBDBD;margin-bottom:12px;font-weight:700;">RÉCAPITULATIF DE COMMANDE</div>
        <div style="margin-bottom:12px;">
          <div style="display:flex;justify-content:space-between;margin-bottom:8px;padding-bottom:8px;border-bottom:1px solid #1f1f1f;">
            <span style="color:#BDBDBD;font-size:12px;">N° Commande</span>
            <span style="color:#fff;font-weight:700;">%s</span>
          </div>
          %s
          <div style="display:flex;justify-content:space-between;margin-top:12px;padding-top:12px;border-top:1px solid #1f1f1f;">
            <span style="color:#fff;font-weight:700;">Total</span>
            <span style="color:#E10600;font-weight:700;font-size:18px;">%s DH</span>
          </div>
        </div>
      </div>

      <p style="margin:14px 0 0 0;color:#BDBDBD;line-height:1.6;">
        Vous recevrez bientôt plus d'informations sur la livraison.<br>
        Pour toute question, contactez-nous via WhatsApp ou Instagram.
      </p>
      """.formatted(escape(customerName), escape(orderId), itemsHtml, escape(total));

    return shell("Confirmation de commande", "Boutique • Wouhouch Hub", content);
  }

  public static String adminOrderNotification(Order order, List<OrderItem> items) {
    String customerName = order.getCustomerName() != null ? order.getCustomerName() : "Client";
    String orderId = order.getId() != null ? order.getId() : "";
    String customerEmail = order.getEmail() != null ? order.getEmail() : "";
    String phone = order.getPhone() != null ? order.getPhone() : "";
    String total = order.getTotal() != null ? String.format("%.2f", order.getTotal()) : "0.00";

    String city = order.getAddress() != null && order.getAddress().getCity() != null ? order.getAddress().getCity() : "";
    String country = order.getAddress() != null && order.getAddress().getCountry() != null ? order.getAddress().getCountry() : "";

    String itemsHtml = buildProductsTable(items);

    String content = """
      <p style="margin:0 0 14px 0;color:#EDEDED;line-height:1.6;">
        Nouvelle commande reçue.
      </p>

      <div style="background:#0b0b0b;border:1px solid #1f1f1f;border-radius:12px;padding:16px;margin:14px 0;">
        <div style="font-size:12px;color:#BDBDBD;margin-bottom:12px;font-weight:700;">DÉTAILS CLIENT</div>
        <div style="display:flex;flex-direction:column;gap:8px;font-size:14px;">
          <div><span style="color:#BDBDBD;">Nom:</span> <span style="color:#fff;">%s</span></div>
          <div><span style="color:#BDBDBD;">Email:</span> <span style="color:#fff;word-break:break-all;">%s</span></div>
          <div><span style="color:#BDBDBD;">Téléphone:</span> <span style="color:#fff;">%s</span></div>
          <div><span style="color:#BDBDBD;">Ville:</span> <span style="color:#fff;">%s</span></div>
          <div><span style="color:#BDBDBD;">Pays:</span> <span style="color:#fff;">%s</span></div>
        </div>
      </div>

      <div style="background:#0b0b0b;border:1px solid #1f1f1f;border-radius:12px;padding:16px;margin:14px 0;">
        <div style="font-size:12px;color:#BDBDBD;margin-bottom:12px;font-weight:700;">PRODUITS (Commande #%s)</div>
        %s
        <div style="display:flex;justify-content:space-between;margin-top:12px;padding-top:12px;border-top:1px solid #1f1f1f;">
          <span style="color:#fff;font-weight:700;">Total</span>
          <span style="color:#E10600;font-weight:700;font-size:16px;">%s DH</span>
        </div>
      </div>
      """.formatted(
          escape(customerName), escape(customerEmail), escape(phone), escape(city), escape(country),
          escape(orderId), itemsHtml, escape(total)
      );

    return shell("Nouvelle commande", "Admin • Wouhouch Hub", content);
  }

  // helper: build an HTML table for order items
  private static String buildProductsTable(List<OrderItem> items) {
    if (items == null || items.isEmpty()) return "<div style=\"color:#BDBDBD;font-size:13px;\">Aucun produit</div>";

    StringBuilder sb = new StringBuilder();
    sb.append("<div style=\"margin:14px 0;\">\n");
    sb.append("<table width=\"100%\" cellpadding=\"6\" cellspacing=\"0\" style=\"border-collapse:collapse;background:#0b0b0b;border:1px solid #1f1f1f;border-radius:8px;overflow:hidden;\">\n");
    sb.append("<thead><tr>\n");
    sb.append("<th align=\"left\" style=\"color:#BDBDBD;font-size:12px;border-bottom:1px solid #1f1f1f;padding:8px;\">Produit</th>\n");
    sb.append("<th align=\"left\" style=\"color:#BDBDBD;font-size:12px;border-bottom:1px solid #1f1f1f;padding:8px;\">Variante</th>\n");
    sb.append("<th align=\"right\" style=\"color:#BDBDBD;font-size:12px;border-bottom:1px solid #1f1f1f;padding:8px;\">Qté</th>\n");
    sb.append("<th align=\"right\" style=\"color:#BDBDBD;font-size:12px;border-bottom:1px solid #1f1f1f;padding:8px;\">Prix unit.</th>\n");
    sb.append("<th align=\"right\" style=\"color:#BDBDBD;font-size:12px;border-bottom:1px solid #1f1f1f;padding:8px;\">Total</th>\n");
    sb.append("</tr></thead><tbody>\n");

    for (OrderItem it : items) {
      String title = escape(it.getProductTitleSnapshot() == null ? "" : it.getProductTitleSnapshot());
      String variant = escape(it.getVariantSnapshot() == null ? "" : it.getVariantSnapshot());
      int qty = it.getQty() == null ? 0 : it.getQty();
      BigDecimal unit = it.getUnitPrice() == null ? BigDecimal.ZERO : it.getUnitPrice();
      BigDecimal lineTotal = unit.multiply(BigDecimal.valueOf(qty));

      sb.append("<tr>\n");
      sb.append("<td style=\"color:#fff;padding:8px 6px;border-bottom:1px solid #1f1f1f;\">" + title + "</td>\n");
      sb.append("<td style=\"color:#BDBDBD;padding:8px 6px;border-bottom:1px solid #1f1f1f;\">" + variant + "</td>\n");
      sb.append("<td align=\"right\" style=\"color:#fff;padding:8px 6px;border-bottom:1px solid #1f1f1f;\">" + qty + "</td>\n");
      sb.append("<td align=\"right\" style=\"color:#fff;padding:8px 6px;border-bottom:1px solid #1f1f1f;\">" + unit.setScale(2, RoundingMode.HALF_UP).toString() + "</td>\n");
      sb.append("<td align=\"right\" style=\"color:#fff;padding:8px 6px;border-bottom:1px solid #1f1f1f;\">" + lineTotal.setScale(2, RoundingMode.HALF_UP).toString() + "</td>\n");
      sb.append("</tr>\n");
    }

    sb.append("</tbody></table></div>\n");
    return sb.toString();
  }

  private static String escape(String s) {
    if (s == null) return "";
    return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
  }
}
