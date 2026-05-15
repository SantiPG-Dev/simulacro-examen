package com.examenes.service;

import com.examenes.model.Question;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class GitHubService {

    private static final String API_BASE = "https://api.github.com/repos/";
    private static final String RAW_BASE = "https://raw.githubusercontent.com/";
    private final HttpClient client;
    private final Gson gson;

    public GitHubService() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public List<Question> importHtmlFromRepo(String repo, String branch, String folder, String token)
            throws IOException, InterruptedException {

        List<String> htmlPaths = listHtmlFiles(repo, branch, folder, token);
        List<Question> allQuestions = new ArrayList<>();

        for (String path : htmlPaths) {
            List<Question> parsed = downloadAndParse(repo, branch, path, token);
            allQuestions.addAll(parsed);
        }

        return allQuestions;
    }

    private List<String> listHtmlFiles(String repo, String branch, String folder, String token)
            throws IOException, InterruptedException {

        String url = API_BASE + repo + "/contents/" + folder;
        if (branch != null && !branch.isEmpty()) {
            url += "?ref=" + branch;
        }

        String json = fetchUrl(url, token);
        List<String> htmlFiles = new ArrayList<>();

        JsonElement element = gson.fromJson(json, JsonElement.class);
        if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            for (JsonElement e : arr) {
                JsonObject obj = e.getAsJsonObject();
                String name = obj.get("name").getAsString();
                String type = obj.get("type").getAsString();
                if ("file".equals(type) && name.toLowerCase().endsWith(".html")) {
                    htmlFiles.add(obj.get("path").getAsString());
                }
            }
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            String name = obj.get("name").getAsString();
            String type = obj.get("type").getAsString();
            if ("file".equals(type) && name.toLowerCase().endsWith(".html")) {
                htmlFiles.add(obj.get("path").getAsString());
            }
        }

        return htmlFiles;
    }

    private List<Question> downloadAndParse(String repo, String branch, String path, String token)
            throws IOException, InterruptedException {

        String rawUrl = RAW_BASE + repo + "/" + branch + "/" + path;

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(rawUrl));

        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        HttpResponse<String> response = client.send(
                builder.build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Error downloading " + path + ": HTTP " + response.statusCode());
        }

        return HtmlParser.parseContent(response.body());
    }

    private String fetchUrl(String url, String token) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github.v3+json");

        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("GitHub API error " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    /**
     * Comprueba si un archivo existe en el repo y devuelve su SHA si existe.
     */
    public String getFileSha(String repo, String path, String branch, String token)
            throws IOException, InterruptedException {
        String url = API_BASE + repo + "/contents/" + path;
        if (branch != null && !branch.isEmpty()) {
            url += "?ref=" + branch;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github.v3+json");

        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 404) return null;
        if (response.statusCode() != 200) {
            throw new IOException("GitHub API error " + response.statusCode());
        }

        JsonObject obj = gson.fromJson(response.body(), JsonObject.class);
        return obj.get("sha").getAsString();
    }

    /**
     * Sube un archivo al repositorio. Si ya existe, lo actualiza (necesita sha).
     */
    public void uploadFile(String repo, String path, byte[] content, String message,
                           String branch, String token, String sha)
            throws IOException, InterruptedException {
        String url = API_BASE + repo + "/contents/" + path;

        JsonObject body = new JsonObject();
        body.addProperty("message", message);
        body.addProperty("content", Base64.getEncoder().encodeToString(content));
        body.addProperty("branch", branch);
        if (sha != null && !sha.isEmpty()) {
            body.addProperty("sha", sha);
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github.v3+json")
                .header("Content-Type", "application/json")
                .method("PUT", HttpRequest.BodyPublishers.ofString(gson.toJson(body)));

        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201 && response.statusCode() != 200) {
            throw new IOException("Error subiendo archivo: HTTP " + response.statusCode() + " - " + response.body());
        }
    }

}
