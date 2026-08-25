package com.example.scamnet.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // "MARKETPLACE" or "JOB"

    private String title;

    @Column(length = 2000)
    private String description;

    private String imageUrl;

    private String posterName;

    private String posterContact; // phone/email — used later for reuse detection

    private String location;

    private LocalDateTime postedAt;

    private Double riskScore; // filled in later by our detection logic
    
    private String imageHash;
    
    @Column(length = 1000)
    private String riskReasons;

    public Post() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getPosterName() { return posterName; }
    public void setPosterName(String posterName) { this.posterName = posterName; }

    public String getPosterContact() { return posterContact; }
    public void setPosterContact(String posterContact) { this.posterContact = posterContact; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }

    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }
    
    public String getImageHash() { return imageHash; }
    public void setImageHash(String imageHash) { this.imageHash = imageHash; }
    
    public String getRiskReasons() { return riskReasons; }
    public void setRiskReasons(String riskReasons) { this.riskReasons = riskReasons; }
}