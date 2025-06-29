package com.github.repo.tracker.network;

import com.github.repo.tracker.model.Repo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class GithubService {

    private static final String API_URL = "https://api.github.com/users/%s/repos?per_page=100&sort=updated";

    private final HttpClient httpClient;
    private final Gson gson;

    public GithubService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public List<Repo> fetchRepos(String username) throws IOException, InterruptedException {
        String url = String.format(API_URL, username);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github+json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Gagal mengambil data: " + response.statusCode());
        }
        JsonArray arr = gson.fromJson(response.body(), JsonArray.class);
        List<Repo> repos = new ArrayList<>();
        for (JsonElement elem : arr) {
            JsonObject obj = elem.getAsJsonObject();
            Repo repo = new Repo();
            repo.setId(obj.get("id").getAsLong());
            repo.setName(obj.get("name").getAsString());
            repo.setDescription(obj.get("description").isJsonNull() ? null : obj.get("description").getAsString());
            repo.setLanguage(obj.get("language").isJsonNull() ? null : obj.get("language").getAsString());
            repo.setStargazersCount(obj.get("stargazers_count").getAsInt());
            repo.setForksCount(obj.get("forks_count").getAsInt());
            repo.setPrivate(obj.get("private").getAsBoolean());
            repo.setHtmlUrl(obj.get("html_url").getAsString());
            repo.setUpdatedAt(Instant.parse(obj.get("updated_at").getAsString()));
            repos.add(repo);
        }
        return repos;
    }
} 