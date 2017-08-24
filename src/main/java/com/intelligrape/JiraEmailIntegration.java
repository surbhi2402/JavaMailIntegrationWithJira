package com.intelligrape;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import java.util.List;

public class JiraEmailIntegration {

    private static final String HOST = "imap.gmail.com";
    private static final String PORT = "993";
    private static final String FROM_EMAIL = System.getenv("FROM_EMAIL");
    private static final String REPLY_FROM_EMAIL = System.getenv("REPLY_FROM_EMAIL");
    private static final String REPLY_TO_EMAIL = System.getenv("REPLY_TO_EMAIL");
    private static final String USERNAME = System.getenv("REPLY_FROM_EMAIL");
    private static final String PASSWORD = System.getenv("REPLY_FROM_PASSWORD");
    private static final String KEYWORD = "ChangeRequest";

    public static void main(String[] args) {
        CheckingUnreadEmailHelper searcher = new CheckingUnreadEmailHelper();
        CreateJiraIssueHelper jiraIssue = new CreateJiraIssueHelper();
        ReplyingToEmailHelper emailHelper = new ReplyingToEmailHelper();
        List<Message> emailsList = searcher.fetchUnreadMails(HOST, PORT, USERNAME, PASSWORD, KEYWORD, FROM_EMAIL);
        try {
            for (int i = 0; i < emailsList.size(); i++) {
                Message message = emailsList.get(i);
                String content = searcher.fetchMessageContent((Part) message);
                String subject = message.getSubject();
                String description = searcher.fetchContentFromHtml(content);
                BasicIssue basicIssue = jiraIssue.createIssue(subject,description);
                System.out.println("Jira issue created with issue Id " + basicIssue.getKey());
                String emailText = "Hi" +
                        "\n\nThis is a reply that your Change Request issue has been created " +
                        "on Jira successfully. \nJira Id is " + basicIssue.getKey() +
                        "\n\nThanks and Regards" +
                        "\n\nAdmin\n\n";
                emailHelper.sendReplyToEmail(REPLY_FROM_EMAIL,REPLY_TO_EMAIL,emailText,message);
            }
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store.");
            ex.printStackTrace();
        }
    }
}
