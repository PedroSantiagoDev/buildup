package com.maistech.buildup.auth.domain;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetCode(String toEmail, String verificationCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Código de Recuperação - BuildUp");

            String htmlContent = buildCodeEmailHtml(verificationCode);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Verification code sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Error sending verification code to: {}", toEmail, e);
            throw new RuntimeException("Error sending verification email", e);
        }
    }

    public void sendPasswordChangedConfirmation(String toEmail){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            message.setSubject("Senha Alterada com Sucesso - BuildUp");

            String htmlContent = buildPasswordChangedEmailHtml();
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password changed confirmation sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending confirmation email to: {}", toEmail, e);
        }
    }

    private String buildCodeEmailHtml(String verificationCode) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f5f5f5;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #ffffff;
                        border-radius: 8px;
                        overflow: hidden;
                        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
                        margin-top: 20px;
                        margin-bottom: 20px;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 30px 20px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: 600;
                    }
                    .content {
                        padding: 40px 30px;
                    }
                    .code-container {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        border-radius: 12px;
                        padding: 25px;
                        text-align: center;
                        margin: 30px 0;
                        box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
                    }
                    .code {
                        font-size: 36px;
                        font-weight: bold;
                        letter-spacing: 8px;
                        color: #ffffff;
                        font-family: 'Courier New', monospace;
                        margin: 0;
                        text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
                    }
                    .code-label {
                        color: rgba(255, 255, 255, 0.9);
                        font-size: 14px;
                        margin-bottom: 10px;
                        text-transform: uppercase;
                        letter-spacing: 1px;
                    }
                    .instructions {
                        background-color: #f8f9fa;
                        border-left: 4px solid #667eea;
                        padding: 20px;
                        margin: 30px 0;
                        border-radius: 4px;
                    }
                    .instructions h3 {
                        margin-top: 0;
                        color: #667eea;
                        font-size: 18px;
                    }
                    .instructions ol {
                        margin: 10px 0;
                        padding-left: 20px;
                    }
                    .instructions li {
                        margin: 8px 0;
                        color: #555;
                    }
                    .warning {
                        background-color: #fff3cd;
                        border: 1px solid #ffc107;
                        border-radius: 6px;
                        padding: 15px;
                        margin: 25px 0;
                    }
                    .warning-icon {
                        color: #ff9800;
                        font-size: 20px;
                        vertical-align: middle;
                        margin-right: 8px;
                    }
                    .warning-text {
                        color: #856404;
                        font-size: 14px;
                    }
                    .footer {
                        background-color: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        border-top: 1px solid #e9ecef;
                    }
                    .footer p {
                        margin: 5px 0;
                        color: #6c757d;
                        font-size: 13px;
                    }
                    .support-link {
                        color: #667eea;
                        text-decoration: none;
                        font-weight: 500;
                    }
                    .divider {
                        height: 1px;
                        background: linear-gradient(to right, transparent, #e0e0e0, transparent);
                        margin: 30px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔒 BuildUp</h1>
                        <p style="margin-top: 10px; opacity: 0.95; font-size: 16px;">
                            Recuperação de Senha
                        </p>
                    </div>
                    <div class="content">
                        <h2 style="color: #333; margin-bottom: 20px;">Olá!</h2>
                        <p style="color: #555; font-size: 16px;">
                            Recebemos uma solicitação para redefinir a senha da sua conta no BuildUp.
                            Use o código abaixo para continuar:
                        </p>
                        <div class="code-container">
                            <p class="code-label">Seu código de verificação</p>
                            <p class="code">%s</p>
                        </div>
                        <div class="instructions">
                            <h3>📱 Como usar este código:</h3>
                            <ol>
                                <li>Abra o aplicativo BuildUp</li>
                                <li>Na tela de recuperação de senha, insira este código</li>
                                <li>Crie sua nova senha</li>
                            </ol>
                        </div>
                        <div class="warning">
                            <span class="warning-icon">⚠️</span>
                            <span class="warning-text">
                                <strong>Este código expira em 15 minutos.</strong><br>
                                Por segurança, você tem até 5 tentativas para inserir o código corretamente.
                            </span>
                        </div>
                        <div class="divider"></div>
                        <p style="color: #777; font-size: 14px; text-align: center;">
                            Se você não solicitou a redefinição de senha, ignore este e-mail.<br>
                            Sua senha permanecerá a mesma.
                        </p>
                    </div>
                    <div class="footer">
                        <p>
                            <strong>Precisa de ajuda?</strong><br>
                            Entre em contato: <a href="mailto:suporte@buildup.com" class="support-link">suporte@buildup.com</a>
                        </p>
                        <p style="margin-top: 15px; font-size: 12px; color: #999;">
                            © 2024 BuildUp. Todos os direitos reservados.<br>
                            Este é um e-mail automático, por favor não responda.
                        </p>
                    </div>
                </div>
            </body>
            </html>
        """.formatted(verificationCode);
    }

    private String buildPasswordChangedEmailHtml() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f5f5f5;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background-color: #ffffff;
                        border-radius: 8px;
                        overflow: hidden;
                        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #4CAF50 0%%, #45a049 100%%);
                        color: white;
                        padding: 30px 20px;
                        text-align: center;
                    }
                    .content {
                        padding: 40px 30px;
                    }
                    .success-icon {
                        width: 80px;
                        height: 80px;
                        margin: 0 auto 20px;
                        background-color: #4CAF50;
                        border-radius: 50%%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 40px;
                        color: white;
                    }
                    .footer {
                        background-color: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        border-top: 1px solid #e9ecef;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Senha Alterada com Sucesso</h1>
                    </div>
                    <div class="content">
                        <div class="success-icon">✓</div>
                        <h2 style="text-align: center; color: #333;">Sua senha foi alterada!</h2>
                        <p style="color: #555; text-align: center; margin: 20px 0;">
                            A senha da sua conta BuildUp foi alterada com sucesso.
                            Você já pode fazer login com sua nova senha.
                        </p>
                        <div style="background-color: #fff3cd; border: 1px solid #ffc107; border-radius: 6px; padding: 15px; margin: 30px 0;">
                            <p style="margin: 0; color: #856404; font-size: 14px;">
                                <strong>🔒 Dica de Segurança:</strong><br>
                                Se você não realizou essa alteração, entre em contato conosco imediatamente.
                            </p>
                        </div>
                    </div>
                    <div class="footer">
                        <p style="margin: 5px 0; color: #6c757d; font-size: 13px;">
                            © 2024 BuildUp. Todos os direitos reservados.
                        </p>
                    </div>
                </div>
            </body>
            </html>
        """;
    }
}
