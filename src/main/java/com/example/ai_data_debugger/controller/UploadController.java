package com.example.ai_data_debugger.controller;

import com.example.ai_data_debugger.service.AiExplanationService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@RestController
@RequestMapping("/api")
public class UploadController {

    @PostMapping("/upload")
    public Map<String, Object>  uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(file.getInputStream()));

            String headerLine = reader.readLine();
            String[] headers = headerLine.split(",");

            int columnCount = headers.length;

            int[] nullCounts = new int[columnCount];

            int rowCount = 0;
            int duplicateCount = 0;

            Set<String> uniqueRows = new HashSet<>();

            // numeric column tracking
            double[] sum = new double[columnCount];
            double[] min = new double[columnCount];
            double[] max = new double[columnCount];
            int[] numericCount = new int[columnCount];

            Arrays.fill(min, Double.MAX_VALUE);
            Arrays.fill(max, Double.MIN_VALUE);

            String line;

            while ((line = reader.readLine()) != null) {

                if (uniqueRows.contains(line)) {
                    duplicateCount++;
                } else {
                    uniqueRows.add(line);
                }

                String[] values = line.split(",");

                for (int i = 0; i < columnCount; i++) {

                    if (i >= values.length || values[i].trim().isEmpty()) {
                        nullCounts[i]++;
                        continue;
                    }

                    String value = values[i].trim();

                    try {

                        double num = Double.parseDouble(value);

                        sum[i] += num;
                        numericCount[i]++;

                        if (num < min[i]) min[i] = num;
                        if (num > max[i]) max[i] = num;

                    } catch (Exception ignored) {
                    }

                }

                rowCount++;
            }

            StringBuilder result = new StringBuilder();

            result.append("Rows: ").append(rowCount).append("\n\n");
            result.append("Duplicate rows: ").append(duplicateCount).append("\n\n");

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("rows", rowCount);
            resultMap.put("duplicates", duplicateCount);

            List<Map<String, Object>> columns = new ArrayList<>();
            for (int i = 0; i < columnCount; i++) {
                Map<String, Object> col = new HashMap<>();
                col.put("name", headers[i]);
                col.put("nulls", nullCounts[i]);
                if (numericCount[i] > 0) {
                    col.put("min", min[i]);
                    col.put("max", max[i]);
                    col.put("average", sum[i] / numericCount[i]);
                }
                columns.add(col);
//                result.append("Column: ").append(headers[i]).append("\n");
//                result.append("Null values: ").append(nullCounts[i]).append("\n");
//
//                if (numericCount[i] > 0) {
//
//                    double avg = sum[i] / numericCount[i];
//
//                    result.append("Min: ").append(min[i]).append("\n");
//                    result.append("Max: ").append(max[i]).append("\n");
//                    result.append("Average: ").append(avg).append("\n");
//                }
//
//                result.append("\n");
            }
            resultMap.put("columns", columns);
           // return result.toString();
//            String aiExplanation = aiService.explainDataIssues(result.toString());
//
//            return result.toString() + "\n\nAI Analysis:\n" + aiExplanation;

            String aiExplanation = aiService.explainDataIssues(result.toString());
            response.put("stats", resultMap); // your structured stats
            response.put("aiAnalysis", aiExplanation);

        } catch (Exception e) {
           // return "Error processing file: " + e.getMessage();
            response.put("error", e.getMessage());
        }

        return response;

    }
    private final AiExplanationService aiService;

    public UploadController(AiExplanationService aiService) {
        this.aiService = aiService;
    }
}