package com.ontotext.ehri.mail;

/**
 * Created by Boyan on 02-Feb-18.
 */
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SendMail.class);

    public static void send(String datasets) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("ehri.ingest","ehri.ing");
                    }
                });

        try {
/*,francesco.gelati@arch.be,dirk.roorda@dans.knaw.nl*/
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("ehri.ingest@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("boyan.simeonov@ontotext.com,Francesco.Gelati@arch.be"));
            message.setSubject("EHRI Auto generated ingest report");
            message.setText("The following datasets failed to ingest, because of validation errors:\n" +
                    datasets);

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Sending ingest report email");
    }
}