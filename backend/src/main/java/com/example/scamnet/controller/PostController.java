package com.example.scamnet.controller;

import com.example.scamnet.model.Post;
import com.example.scamnet.repository.PostRepository;
import com.example.scamnet.service.ImageHashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ImageHashService imageHashService;
    
    @Autowired
    private com.example.scamnet.service.ClusterService clusterService;

    private final String uploadDir = "uploads/";

    @PostMapping
    public Object createPost(
            @RequestParam String type,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String posterName,
            @RequestParam String posterContact,
            @RequestParam String location,
            @RequestParam(required = false) String priceOrSalary,
            @RequestParam("image") MultipartFile imageFile
    ) throws IOException {

        // Save the uploaded file to a local folder
        new File(uploadDir).mkdirs();
        String filename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
        Path filePath = Path.of(uploadDir + filename);
        Files.write(filePath, imageFile.getBytes());

        // Compute perceptual hash of the uploaded image
        String hash = imageHashService.computeHash(filePath.toFile());

        List<Post> existingPosts = postRepository.findAll();
        boolean contactReused = existingPosts.stream()
                .anyMatch(p -> p.getPosterContact() != null 
                        && p.getPosterContact().equals(posterContact) 
                        && !p.getPosterName().equals(posterName));
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(15);
        long recentPostCount = existingPosts.stream()
                .filter(p -> p.getPostedAt() != null && p.getPostedAt().isAfter(windowStart))
                .count();
        boolean burstDetected = recentPostCount >= 2; // 2 existing + this new one = 3 in the window
        List<Long> matches = imageHashService.findMatches(hash, existingPosts);
        Post post = new Post();
        post.setType(type);
        post.setTitle(title);
        post.setDescription(description);
        post.setPosterName(posterName);
        post.setPosterContact(posterContact);
        post.setLocation(location);
        post.setPriceOrSalary(priceOrSalary);
        post.setImageUrl(filename);
        post.setImageHash(hash);
        post.setPostedAt(LocalDateTime.now());

        double riskScore = 0.0;
        if (!matches.isEmpty()) riskScore += 0.5;
        if (contactReused) riskScore += 0.3;
        boolean burstCounts = burstDetected && (!matches.isEmpty() || contactReused);
        if (burstCounts) riskScore += 0.2;

        java.util.List<String> reasons = new java.util.ArrayList<>();
        if (!matches.isEmpty()) {
            java.util.List<String> matchedTitles = existingPosts.stream()
                    .filter(p -> matches.contains(p.getId()))
                    .map(Post::getTitle)
                    .collect(java.util.stream.Collectors.toList());
            reasons.add("Image matches: \"" + String.join("\", \"", matchedTitles) + "\"");
        }
        if (contactReused) {
            reasons.add("Contact number reused under a different name");
        }
        if (burstCounts) {
            reasons.add("Part of an unusual burst of " + (recentPostCount + 1) + " posts within 15 minutes");
        }

        post.setRiskScore(Math.min(riskScore, 1.0));
        post.setRiskReasons(String.join(" | ", reasons));
        Post saved = postRepository.save(post);
        
        // Retroactively bump risk on matched posts too, since they're now confirmed part of a network
        if (!matches.isEmpty()) {
            for (Post existing : existingPosts) {
                if (matches.contains(existing.getId()) && (existing.getRiskScore() == null || existing.getRiskScore() < 0.5)) {
                    existing.setRiskScore(0.5);
                    String prevReasons = existing.getRiskReasons() != null ? existing.getRiskReasons() + " | " : "";
                    existing.setRiskReasons(prevReasons + "Image reused in a later listing: \"" + saved.getTitle() + "\"");
                    postRepository.save(existing);
                }
            }
        }

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("post", saved);
        response.put("matchedPostIds", matches);
        return response;
    }

    @GetMapping
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }
    
    @GetMapping("/clusters")
    public List<Map<String, Object>> getClusters() {
        List<Post> allPosts = postRepository.findAll();
        return clusterService.findClusters(allPosts);
    }
    
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        postRepository.deleteById(id);
    }
}