package com.github.repo.tracker.model;

import com.google.gson.annotations.SerializedName;

public class GitHubUser {
    private String login;
    private String name;
    private String bio;
    @SerializedName("avatar_url")
    private String avatarUrl;
    @SerializedName("public_repos")
    private int publicRepos;
    private int followers;
    private int following;

    //<editor-fold desc="Getters and Setters">
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getPublicRepos() {
        return publicRepos;
    }

    public void setPublicRepos(int publicRepos) {
        this.publicRepos = publicRepos;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }
    //</editor-fold>
} 