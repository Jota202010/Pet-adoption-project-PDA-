package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.refugio.nueva_vida.proyecto_de_aula.dtos.CitaNotificationDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String REMITENTE = "juanrogers1809@gmail.com";

    private final JavaMailSender mailSender;

    
    // NOTIFICACIONES DE CITA

    public void enviarNotificacion(CitaNotificationDTO n) {
        String asunto;
        String cuerpoHtml;

        switch (n.getTipoEvento()) {
            case PRE_APROBADA -> {
                asunto = "¡Tu solicitud fue pre-aprobada! — Refugio Nueva Vida";
                cuerpoHtml = plantilla(
                        "¡Buenas noticias!",
                        n.getNombreAdoptante(),
                        "Tu solicitud para adoptar a <strong>%s</strong> (cita #%d) fue "
                                .formatted(n.getNombrePerro(), n.getIdCita())
                                + "<strong>pre-aprobada</strong> por nuestro equipo.",
                        "Ya puedes ingresar a tu perfil y elegir el día y hora que prefieras para tu cita.",
                        "#2e7d32"
                );
            }
            case RECHAZADA -> {
                asunto = "Actualización sobre tu solicitud — Refugio Nueva Vida";
                cuerpoHtml = plantilla(
                        "Actualización de tu solicitud",
                        n.getNombreAdoptante(),
                        "Lamentamos informarte que tu solicitud para adoptar a <strong>%s</strong> "
                                .formatted(n.getNombrePerro())
                                + "(cita #%d) no fue aprobada en esta ocasión.".formatted(n.getIdCita()),
                        "Te invitamos a revisar otros animales disponibles en nuestro refugio que también buscan un hogar.",
                        "#c62828"
                );
            }
            case CONFIRMADA -> {
                asunto = "¡Cita confirmada! — Refugio Nueva Vida";
                cuerpoHtml = plantilla(
                        "¡Tu cita quedó confirmada!",
                        n.getNombreAdoptante(),
                        "Tu cita para adoptar a <strong>%s</strong> (cita #%d) ha sido confirmada."
                                .formatted(n.getNombrePerro(), n.getIdCita()),
                        "Te esperamos en la fecha y hora que elegiste. ¡Gracias por darle un hogar a un animal del refugio!",
                        "#1565c0"
                );
            }
            default -> {
                asunto = "Solicitud de adopción recibida — Refugio Nueva Vida";
                cuerpoHtml = plantilla(
                        "¡Solicitud recibida!",
                        n.getNombreAdoptante(),
                        "Recibimos tu solicitud de adopción para <strong>%s</strong> (cita #%d)."
                                .formatted(n.getNombrePerro(), n.getIdCita()),
                        "Te notificaremos por este medio en cuanto sea revisada por nuestro equipo.",
                        "#2e7d32"
                );
            }
        }

        enviarHtml(n.getEmailAdoptante(), asunto, cuerpoHtml);
    }

    // NOTIFICACIÓN DE SEGUIMIENTO DE PEDIDO

    public void enviarSeguimientoPedido(Integer idPedido, String estado, String descripcion) {

        String colorEstado;
        String iconoEstado;
        String textoEstado;

        switch (estado) {
            case "PREPARANDO" -> {
                colorEstado = "#f59e0b";
                iconoEstado = "📝";
                textoEstado = "Preparando";
            }
            case "DESPACHADO" -> {
                colorEstado = "#3b82f6";
                iconoEstado = "📤";
                textoEstado = "Despachado";
            }
            case "EN_TRANSITO" -> {
                colorEstado = "#8b5cf6";
                iconoEstado = "🚛";
                textoEstado = "En tránsito";
            }
            case "EN_REPARTO" -> {
                colorEstado = "#f97316";
                iconoEstado = "🛵";
                textoEstado = "En reparto";
            }
            case "ENTREGADO" -> {
                colorEstado = "#16a34a";
                iconoEstado = "✅";
                textoEstado = "Entregado";
            }
            default -> {
                colorEstado = "#64748b";
                iconoEstado = "📦";
                textoEstado = estado;
            }
        }

        String asunto = "📦 Actualización de pedido #" + idPedido + " - " + iconoEstado + " " + textoEstado;

        String cuerpoHtml = plantillaSeguimiento(idPedido, estado, descripcion,
                colorEstado, iconoEstado, textoEstado);

        enviarHtml(REMITENTE, asunto, cuerpoHtml);
    }

  

    private String plantillaSeguimiento(Integer idPedido, String estado,
                                        String descripcion, String colorEstado,
                                        String iconoEstado, String textoEstado) {

        boolean paso1 = true;
        boolean paso2 = !estado.equals("PREPARANDO");
        boolean paso3 = estado.equals("EN_TRANSITO") || estado.equals("EN_REPARTO") || estado.equals("ENTREGADO");
        boolean paso4 = estado.equals("EN_REPARTO") || estado.equals("ENTREGADO");
        boolean paso5 = estado.equals("ENTREGADO");

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; background-color: #f5f7fb; font-family: 'Segoe UI', Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto; background: #ffffff;">

                    <!-- HEADER -->
                    <div style="background: linear-gradient(135deg, #172033 0%%, #253653 55%%, #ff6b35 160%%);
                                padding: 40px 30px; text-align: center; color: #ffffff;">
                        <h1 style="margin: 0 0 10px; font-size: 26px; font-weight: 800;">
                            🐾 Refugio Nueva Vida
                        </h1>
                        <p style="margin: 0; color: #dbe4f0; font-size: 14px;">
                            Sistema de seguimiento de pedidos
                        </p>
                    </div>

                    <!-- BODY -->
                    <div style="padding: 35px 30px;">

                        <!-- INFO BOX -->
                        <div style="background: #f8fafc; border-radius: 12px; padding: 20px;
                                    margin-bottom: 25px; border-left: 4px solid %s;">
                            <div style="font-size: 12px; color: #64748b; font-weight: 700;
                                        letter-spacing: 1px; text-transform: uppercase;
                                        margin-bottom: 8px;">
                                PEDIDO
                            </div>
                            <div style="font-size: 24px; font-weight: 800; color: #172033;
                                        margin-bottom: 8px;">
                                #%d
                            </div>
                            <div style="color: #64748b; font-size: 14px; line-height: 1.5;">
                                %s
                            </div>
                        </div>

                        <!-- BADGE ESTADO -->
                        <div style="text-align: center; margin: 20px 0;">
                            <span style="display: inline-block; padding: 12px 24px;
                                         border-radius: 999px; font-weight: 800;
                                         color: #ffffff; background: %s;
                                         font-size: 15px;">
                                %s %s
                            </span>
                        </div>

                        <!-- TIMELINE -->
                        <div style="margin: 30px 0;">
                            <div style="font-size: 12px; color: #64748b; font-weight: 700;
                                        letter-spacing: 1px; text-transform: uppercase;
                                        margin-bottom: 15px;">
                                PROGRESO DEL ENVÍO
                            </div>

                            <div style="padding: 8px 0; color: %s; font-size: 14px; font-weight: %s;">
                                <span style="display: inline-block; width: 10px; height: 10px;
                                             border-radius: 50%%; background: %s;
                                             margin-right: 12px;"></span>
                                📝 Preparando
                            </div>

                            <div style="padding: 8px 0; color: %s; font-size: 14px; font-weight: %s;">
                                <span style="display: inline-block; width: 10px; height: 10px;
                                             border-radius: 50%%; background: %s;
                                             margin-right: 12px;"></span>
                                📤 Despachado
                            </div>

                            <div style="padding: 8px 0; color: %s; font-size: 14px; font-weight: %s;">
                                <span style="display: inline-block; width: 10px; height: 10px;
                                             border-radius: 50%%; background: %s;
                                             margin-right: 12px;"></span>
                                🚛 En tránsito
                            </div>

                            <div style="padding: 8px 0; color: %s; font-size: 14px; font-weight: %s;">
                                <span style="display: inline-block; width: 10px; height: 10px;
                                             border-radius: 50%%; background: %s;
                                             margin-right: 12px;"></span>
                                🛵 En reparto
                            </div>

                            <div style="padding: 8px 0; color: %s; font-size: 14px; font-weight: %s;">
                                <span style="display: inline-block; width: 10px; height: 10px;
                                             border-radius: 50%%; background: %s;
                                             margin-right: 12px;"></span>
                                ✅ Entregado
                            </div>
                        </div>

                        <!-- CTA -->
                        <div style="text-align: center; margin: 30px 0 15px;">
                            <a href="http://localhost:8082/proveedor/seguimiento/%d"
                               style="display: inline-block;
                                      background: linear-gradient(135deg, #ff6b35, #ff9f43);
                                      color: #ffffff; padding: 14px 30px;
                                      border-radius: 12px; text-decoration: none;
                                      font-weight: 800; font-size: 15px;">
                                Ver seguimiento en vivo →
                            </a>
                        </div>
                    </div>

                    <!-- FOOTER -->
                    <div style="background: #172033; color: #94a3b8; text-align: center;
                                padding: 25px 20px; font-size: 12px;">
                        <p style="margin: 0;">© 2025 Refugio Nueva Vida · Hecho con ❤️ para los perros</p>
                    </div>

                </div>
            </body>
            </html>
            """.formatted(
                colorEstado, idPedido, descripcion, colorEstado, iconoEstado, textoEstado,
                // Paso 1
                paso1 ? "#172033" : "#cbd5e1",
                paso1 ? "700" : "400",
                paso1 ? colorEstado : "#e2e8f0",
                // Paso 2
                paso2 ? "#172033" : "#cbd5e1",
                paso2 ? "700" : "400",
                paso2 ? colorEstado : "#e2e8f0",
                // Paso 3
                paso3 ? "#172033" : "#cbd5e1",
                paso3 ? "700" : "400",
                paso3 ? colorEstado : "#e2e8f0",
                // Paso 4
                paso4 ? "#172033" : "#cbd5e1",
                paso4 ? "700" : "400",
                paso4 ? colorEstado : "#e2e8f0",
                // Paso 5
                paso5 ? "#172033" : "#cbd5e1",
                paso5 ? "700" : "400",
                paso5 ? colorEstado : "#e2e8f0",
                idPedido
        );
    }

  

    private String plantilla(String titulo, String nombre, String mensajePrincipal,
                              String mensajeSecundario, String colorAcento) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f4f6f5; padding: 30px;">
              <div style="background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
                <div style="background-color: %s; padding: 20px 30px;">
                  <h1 style="color: #ffffff; margin: 0; font-size: 22px;">🐾 Refugio Nueva Vida</h1>
                </div>
                <div style="padding: 30px;">
                  <h2 style="color: %s; margin-top: 0;">%s</h2>
                  <p style="font-size: 15px; color: #333;">Hola <strong>%s</strong>,</p>
                  <p style="font-size: 15px; color: #333;">%s</p>
                  <div style="background-color: #f1f3f4; border-left: 4px solid %s; padding: 12px 16px; margin: 20px 0; border-radius: 4px;">
                    <p style="margin: 0; font-size: 14px; color: #333;">%s</p>
                  </div>
                  <p style="font-size: 14px; color: #999; margin-top: 30px;">— Equipo de Refugio Nueva Vida</p>
                </div>
              </div>
            </div>
            """.formatted(colorAcento, colorAcento, titulo, nombre, mensajePrincipal, colorAcento, mensajeSecundario);
    }

  
    public void enviarCodigoRecuperacion(String emailDestino, String codigo) {
        String asunto = "Código para restablecer tu contraseña — Refugio Nueva Vida";
        String cuerpoHtml = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f4f6f5; padding: 30px;">
              <div style="background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
                <div style="background-color: #1565c0; padding: 20px 30px;">
                  <h1 style="color: #ffffff; margin: 0; font-size: 22px;">🐾 Refugio Nueva Vida</h1>
                </div>
                <div style="padding: 30px;">
                  <h2 style="color: #1565c0; margin-top: 0;">Recuperación de contraseña</h2>
                  <p style="font-size: 15px; color: #333;">
                    Recibimos una solicitud para restablecer tu contraseña. Usa el siguiente código:
                  </p>
                  <div style="background-color: #E3F2FD; border-radius: 8px; padding: 20px; margin: 20px 0; text-align: center;">
                    <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #1565c0;">%s</span>
                  </div>
                  <p style="font-size: 14px; color: #666;">
                    Este código vence en <strong>15 minutos</strong>. Si tú no solicitaste este cambio, puedes ignorar este correo.
                  </p>
                  <p style="font-size: 14px; color: #999; margin-top: 30px;">— Equipo de Refugio Nueva Vida</p>
                </div>
              </div>
            </div>
            """.formatted(codigo);

        enviarHtml(emailDestino, asunto, cuerpoHtml);
    }

    

    private void enviarHtml(String to, String asunto, String html) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setTo(to);
            helper.setFrom(REMITENTE);
            helper.setSubject(asunto);
            helper.setText(html, true);

            mailSender.send(mimeMessage);
            log.info("Correo '{}' enviado a: {}", asunto, to);
        } catch (MessagingException e) {
            log.error("Error al enviar correo a {}: {}", to, e.getMessage());
        }
    }
}