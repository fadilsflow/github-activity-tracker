package com.github.repo.tracker.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.Instant;

/**
 * Representasi sederhana dari sebuah repository GitHub.
 */
public class Repo implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private String description;
    private String language;
    
    @SerializedName("stargazers_count")
    private int stargazersCount;
    
    @SerializedName("forks_count")
    private int forksCount;
    
    @SerializedName("private")
    private boolean isPrivate;
    
    @SerializedName("updated_at")
    private Instant updatedAt;
    
    @SerializedName("html_url")
    private String htmlUrl;

    public Repo() {
    }

    // Getter & Setter
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getStargazersCount() {
        return stargazersCount;
    }

    public void setStargazersCount(int stargazersCount) {
        this.stargazersCount = stargazersCount;
    }

    public int getForksCount() {
        return forksCount;
    }

    public void setForksCount(int forksCount) {
        this.forksCount = forksCount;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    @Override
    public String toString() {
        return name + " (⭐" + stargazersCount + ")";
    }
} 