package com.intelligrape;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class ReplyingToEmailHelper {

    final String username = System.getenv("REPLY_FROM_EMAIL");
    final String password = System.getenv("REPLY_FROM_PASSWORD");

    public void sendReplyToEmail(String fromAddress, String toAddress, String emailText,Message message ) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message replyMessage = (MimeMessage) message.reply(false);
            replyMessage.setFrom(new InternetAddress(fromAddress));
            replyMessage.setReplyTo(message.getReplyTo());
            replyMessage.setText(emailText);

            Transport transport = session.getTransport("smtp");
            try {
                transport.connect(username, password);
                transport.sendMessage(replyMessage, replyMessage.getAllRecipients());
            } finally {
                transport.close();
            }
            System.out.println("Sent email to " + toAddress + " from " + fromAddress + ".");

        } catch (MessagingException e) {

            throw new RuntimeException(e);
        }
    }
}
