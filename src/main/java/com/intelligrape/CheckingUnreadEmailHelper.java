package com.intelligrape;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.mail.*;
import javax.mail.search.FlagTerm;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CheckingUnreadEmailHelper {

    private boolean textIsHtml = false;

    public List<Message> fetchUnreadMails(String host, String port, String userName,
                                 String password, final String keyword, String fromEmail) {
        List<Message> emailsList = new ArrayList<Message>();
        Properties properties = new Properties();
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", port);
        properties.setProperty("mail.imap.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.imap.socketFactory.fallback", "false");
        properties.setProperty("mail.imap.socketFactory.port",
                String.valueOf(port));

        Session session = Session.getDefaultInstance(properties);

        try {
            Store store = session.getStore("imap");
            store.connect(userName, password);

            Folder folderInbox = store.getFolder("INBOX");
            folderInbox.open(Folder.READ_WRITE);

            Message[] messages = folderInbox.search(
                    new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                if (searchIfDesiredEmail(message, keyword, fromEmail)) {
                    emailsList.add(message);
                }
            }

//            folderInbox.close(false);
//            store.close();

        } catch (NoSuchProviderException ex) {
            System.out.println("No provider.");
            ex.printStackTrace();
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store.");
            ex.printStackTrace();
        }
        return emailsList;
    }

    public boolean searchIfDesiredEmail(Message message, final String keyword, String fromEmail) {
        boolean isDesired = false;
        try {
            String messageAfterRemovingSpaces = message.getSubject().replaceAll("\\s", "");
            Address[] fromAddress = message.getFrom();
            if (fromAddress != null && fromAddress.length > 0) {
                if (fromAddress[0].toString().contains(fromEmail) &&
                        messageAfterRemovingSpaces.contains(keyword.replaceAll("\\s", ""))) {
                    isDesired = true;
                }
            }

        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store.");
            ex.printStackTrace();
        }
        return isDesired;
    }

    public String fetchContentFromHtml(String content) {
        Document doc = Jsoup.parse(content);
        Element firstDivTag = doc.select("div").first();
        String description = firstDivTag.text();
        System.out.println("");
        return description;
    }

    String fetchMessageContent(Part p) throws MessagingException {
        try {
            if (p.isMimeType("text/*")) {
                String s = (String) p.getContent();
                textIsHtml = p.isMimeType("text/html");
                return s;
            }

            if (p.isMimeType("multipart/alternative")) {
                Multipart mp = (Multipart) p.getContent();
                String text = null;
                for (int i = 0; i < mp.getCount(); i++) {
                    Part bp = mp.getBodyPart(i);
                    if (bp.isMimeType("text/plain")) {
                        if (text == null)
                            text = fetchMessageContent(bp);
                        continue;
                    } else if (bp.isMimeType("text/html")) {
                        String s = fetchMessageContent(bp);
                        if (s != null)
                            return s;
                    } else {
                        return fetchMessageContent(bp);
                    }
                }
                return text;
            } else if (p.isMimeType("multipart/*")) {
                Multipart mp = (Multipart) p.getContent();
                for (int i = 0; i < mp.getCount(); i++) {
                    String s = fetchMessageContent(mp.getBodyPart(i));
                    if (s != null)
                        return s;
                }
            }
        } catch (IOException ex) {
            System.out.println("IO Exception occurred...");
        }

        return null;
    }
}
