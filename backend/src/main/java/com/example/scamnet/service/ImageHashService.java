package com.example.scamnet.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class ImageHashService {

    // Computes a "difference hash" (dHash) — a compact fingerprint of what the image looks like
    public String computeHash(File imageFile) throws IOException {
        BufferedImage original = ImageIO.read(imageFile);
        if (original == null) {
            throw new IOException("Could not read image file: " + imageFile.getName());
        }

        // Resize to 9x8 (small size wipes out irrelevant detail, keeps overall shape/pattern)
        BufferedImage resized = new BufferedImage(9, 8, BufferedImage.TYPE_INT_RGB);
        resized.getGraphics().drawImage(original.getScaledInstance(9, 8, java.awt.Image.SCALE_SMOOTH), 0, 0, null);

        StringBuilder hash = new StringBuilder();

        // Compare each pixel to the one right next to it (left to right), grayscale brightness
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int rgb1 = resized.getRGB(x, y);
                int rgb2 = resized.getRGB(x + 1, y);

                int gray1 = toGrayscale(rgb1);
                int gray2 = toGrayscale(rgb2);

                hash.append(gray1 > gray2 ? "1" : "0");
            }
        }

        return hash.toString(); // 64-character string of 1s and 0s
    }

    private int toGrayscale(int rgb) {
        Color c = new Color(rgb);
        return (c.getRed() + c.getGreen() + c.getBlue()) / 3;
    }

    // Compares two hashes, returns true if images are visually similar
    public boolean areSimilar(String hash1, String hash2) {
        if (hash1 == null || hash2 == null || hash1.length() != hash2.length()) {
            return false;
        }

        int differingBits = 0;
        for (int i = 0; i < hash1.length(); i++) {
            if (hash1.charAt(i) != hash2.charAt(i)) {
                differingBits++;
            }
        }

        // Out of 64 bits, allow up to 10 differing bits to still count as "similar"
        return differingBits <= 10;
    }
    // Compares a new hash against a list of existing posts, returns IDs of visually similar ones
    public java.util.List<Long> findMatches(String newHash, java.util.List<com.example.scamnet.model.Post> existingPosts) {
        java.util.List<Long> matchedIds = new java.util.ArrayList<>();
        for (com.example.scamnet.model.Post existing : existingPosts) {
            if (existing.getImageHash() != null && areSimilar(newHash, existing.getImageHash())) {
                matchedIds.add(existing.getId());
            }
        }
        return matchedIds;
    }
}