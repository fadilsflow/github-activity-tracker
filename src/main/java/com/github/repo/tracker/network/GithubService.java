package com.github.repo.tracker.network;

import com.github.repo.tracker.model.Commit;
import com.github.repo.tracker.model.GitHubUser;
import com.github.repo.tracker.model.Repo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class GithubService {

    private static final String API_BASE = "https://api.github.com";

    private final HttpClient httpClient;
    private final Gson gson;

    public GithubService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new JsonDeserializer<Instant>() {
                @Override
                public Instant deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    return Instant.parse(json.getAsString());
                }
            })
            .create();
    }

    public GitHubUser fetchUser(String username) throws IOException, InterruptedException {
        String url = String.format("%s/users/%s", API_BASE, username);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github+json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        switch (response.statusCode()) {
            case 200:
                return gson.fromJson(response.body(), GitHubUser.class);
            case 404:
                throw new IOException("User '" + username + "' tidak ditemukan di GitHub");
            case 403:
                throw new IOException("Rate limit terlampaui. Silakan coba lagi nanti");
            case 401:
                throw new IOException("Akses tidak diizinkan. Periksa kembali username");
            default:
                throw new IOException("Gagal mengambil data user: HTTP " + response.statusCode());
        }
    }

    public List<Repo> fetchRepos(String username) throws IOException, InterruptedException {
        String url = String.format("%s/users/%s/repos?per_page=100&sort=updated", API_BASE, username);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github+json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Gagal mengambil data: " + response.statusCode());
        }
        Repo[] repos = gson.fromJson(response.body(), Repo[].class);
        return List.of(repos);
    }

    public List<Commit> fetchCommits(String username, String repoName) throws IOException, InterruptedException {
        String url = String.format("%s/repos/%s/%s/commits?per_page=10", API_BASE, username, repoName);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github+json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Gagal mengambil commit: " + response.statusCode());
        }
        Commit[] commits = gson.fromJson(response.body(), Commit[].class);
        return List.of(commits);
    }
} 