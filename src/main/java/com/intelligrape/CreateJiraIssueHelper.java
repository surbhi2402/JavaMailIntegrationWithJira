package com.intelligrape;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import java.net.URI;

public class CreateJiraIssueHelper {

    private static URI jiraServerUri = URI.create("http://jira.tothenew.com");
    private static final String PROJECT_KEY = "DTH";
    private static final long CHANGE_REQUEST = 10201L;
    private static final String JIRA_USERNAME = System.getenv("JIRA_USERNAME");
    private static final String JIRA_PASSWORD = System.getenv("JIRA_PASSWORD");

    final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();

    public BasicIssue createIssue(String summary, String description) {
        try {
            final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, JIRA_USERNAME, JIRA_PASSWORD);
            IssueInputBuilder builder = new IssueInputBuilder(PROJECT_KEY,CHANGE_REQUEST, summary);
            builder.setDescription(description);
            IssueInput input = builder.build();
            BasicIssue issue= restClient.getIssueClient().createIssue(input).claim();
            System.out.println(issue);
            return issue;
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }

    }
}
