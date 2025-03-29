package com.jobmatcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootApplication
@RestController
public class Prompts {
    public static String name = "";
    public static String[] skills = new String[0];
    public static String[] education = new String[0];
    public static String experience = "";
    public static String location = "";
    public static String[] languages = new String[0];
    public static String[] jobTitles = new String[0];
    public static String[] certifications = new String[0];

    private static String sendPromptToAPI(String promptText, String modelName, String apiUrl) {
        JSONObject jsonInput = new JSONObject();
        jsonInput.put("model", modelName);
        jsonInput.put("prompt", promptText);
        String jsonInputString = jsonInput.toString();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json; utf-8")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
                .build();

        StringBuilder responseText = new StringBuilder();
        try {
            HttpResponse<java.util.stream.Stream<String>> response = client.send(request, HttpResponse.BodyHandlers.ofLines());
            response.body().forEach(line -> {
                try {
                    JSONObject jsonLine = new JSONObject(line);
                    responseText.append(jsonLine.getString("response"));
                } catch (org.json.JSONException e) {
                    System.err.println("Error parsing line: " + line);
                }
            });
            return responseText.toString().replaceAll("\\s+", " ").trim();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error during API request: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/api/cv")
    @CrossOrigin(origins = "http://localhost:5000")
    public String uploadCV(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "{\"error\": \"No file uploaded\"}";
        }
        try {
            File tempFile = File.createTempFile("cv_", ".pdf");
            file.transferTo(tempFile);
            String pdfPath = tempFile.getAbsolutePath();
            String CVText = PDFTextExtractor.extractTextFromPDF(pdfPath);
            tempFile.delete();

            if (CVText.startsWith("Error")) {
                return "{\"error\": \"" + CVText + "\"}";
            }

            String modelName = "gemma2:2b";
            String apiUrl = "http://localhost:11434/api/generate";

            name = sendPromptToAPI("Extract the full name from this CV text and only this: " + CVText, modelName, apiUrl);
            String skillsResponse = sendPromptToAPI("Extract all programming languages and skills mentioned in this CV text, separated by commas and only this: " + CVText, modelName, apiUrl);
            skills = skillsResponse.equals("Error") ? new String[0] : skillsResponse.split(",\\s*");
            String educationResponse = sendPromptToAPI("Extract all education institutions from this CV text, separated by commas and only this: " + CVText, modelName, apiUrl);
            education = educationResponse.equals("Error") ? new String[0] : educationResponse.split(",\\s*");
            experience = sendPromptToAPI("Extract the work experience (not school, only jobs if this person had any - if not, just say No Experience) including duration (in years or months) from this CV text and only this: " + CVText, modelName, apiUrl);
            location = sendPromptToAPI("Extract the location (city) from this CV text and only this: " + CVText, modelName, apiUrl);
            String langsResponse = sendPromptToAPI("Extract all spoken/written languages mentioned in this CV text, separated by commas and only this - in english please: " + CVText, modelName, apiUrl);
            languages = langsResponse.equals("Error") ? new String[0] : langsResponse.split(",\\s*");
            String jobTitlesResponse = sendPromptToAPI("Extract all job titles or roles mentioned in this CV text (from jobs, internships, or projects - if none, say None), separated by commas and only this: " + CVText, modelName, apiUrl);
            jobTitles = jobTitlesResponse.equals("Error") ? new String[0] : jobTitlesResponse.split(",\\s*");
            String certificationsResponse = sendPromptToAPI("Extract all certifications mentioned in this CV text, separated by commas and only this - if none, say None: " + CVText, modelName, apiUrl);
            certifications = certificationsResponse.equals("Error") ? new String[0] : certificationsResponse.split(",\\s*");

            JSONObject cvJson = new JSONObject();
            cvJson.put("name", name);
            cvJson.put("skills", String.join(",", skills));
            cvJson.put("education", String.join(",", education));
            cvJson.put("experience", experience);
            cvJson.put("location", location);
            cvJson.put("languages", String.join(",", languages));
            cvJson.put("jobTitles", String.join(",", jobTitles));
            cvJson.put("certifications", String.join(",", certifications));

            try (FileWriter fileWriter = new FileWriter("cv_data.json")) {
                fileWriter.write(cvJson.toString());
            } catch (IOException e) {
                System.err.println("Error saving JSON: " + e.getMessage());
            }

            return cvJson.toString();
        } catch (IOException e) {
            return "{\"error\": \"Failed to process CV: " + e.getMessage() + "\"}";
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Prompts.class, args);
    }
}