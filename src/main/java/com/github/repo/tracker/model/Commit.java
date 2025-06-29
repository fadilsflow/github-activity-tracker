package com.github.repo.tracker.model;

import java.time.Instant;

public class Commit {

    private CommitDetails commit;
    private String sha;

    public static class CommitDetails {
        private CommitAuthor author;
        private String message;

        public CommitAuthor getAuthor() { return author; }
        public String getMessage() { return message; }
    }

    public static class CommitAuthor {
        private String name;
        private Instant date;

        public String getName() { return name; }
        public Instant getDate() { return date; }
    }

    public String getSha() { return sha; }
    public String getAuthorName() { return commit.getAuthor().getName(); }
    public String getMessage() { return commit.getMessage(); }
    public Instant getDate() { return commit.getAuthor().getDate(); }
} 