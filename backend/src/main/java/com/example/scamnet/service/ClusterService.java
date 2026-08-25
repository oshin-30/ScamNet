package com.example.scamnet.service;

import com.example.scamnet.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ClusterService {

    @Autowired
    private ImageHashService imageHashService;

    // Groups posts into connected clusters based on shared image, contact, or burst timing
    public List<Map<String, Object>> findClusters(List<Post> allPosts) {
        // Union-Find setup: each post starts as its own group
        Map<Long, Long> parent = new HashMap<>();
        for (Post p : allPosts) parent.put(p.getId(), p.getId());

        // Connect posts that share a signal
        for (int i = 0; i < allPosts.size(); i++) {
            for (int j = i + 1; j < allPosts.size(); j++) {
                Post a = allPosts.get(i);
                Post b = allPosts.get(j);

                boolean sameImage = a.getImageHash() != null && b.getImageHash() != null
                        && imageHashService.areSimilar(a.getImageHash(), b.getImageHash());

                boolean sameContact = a.getPosterContact() != null
                        && a.getPosterContact().equals(b.getPosterContact());

                boolean burstTogether = a.getPostedAt() != null && b.getPostedAt() != null
                        && Math.abs(ChronoUnit.MINUTES.between(a.getPostedAt(), b.getPostedAt())) <= 15;

                // Burst timing alone isn't suspicious — only connect on burst if there's ALSO some other shared trait
                boolean suspiciousBurst = burstTogether && (sameImage || sameContact
                        || (a.getLocation() != null && a.getLocation().equalsIgnoreCase(b.getLocation())));

                if (sameImage || sameContact || suspiciousBurst) {
                    union(parent, a.getId(), b.getId());
                }
            }
        }

        // Group posts by their root cluster ID
        Map<Long, List<Post>> groups = new HashMap<>();
        for (Post p : allPosts) {
            Long root = find(parent, p.getId());
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(p);
        }

        // Only keep clusters with 2+ posts (a cluster of 1 isn't a "network")
        List<Map<String, Object>> result = new ArrayList<>();
        int clusterNum = 1;
        for (List<Post> group : groups.values()) {
            if (group.size() < 2) continue;

            Set<String> contacts = new HashSet<>();
            Set<String> names = new HashSet<>();
            for (Post p : group) {
                contacts.add(p.getPosterContact());
                names.add(p.getPosterName());
            }

            Map<String, Object> cluster = new HashMap<>();
            cluster.put("clusterId", clusterNum++);
            cluster.put("posts", group);
            cluster.put("postCount", group.size());
            cluster.put("accountCount", names.size());
            cluster.put("sharedContacts", contacts.size() < group.size());
            double avgRisk = group.stream().mapToDouble(p -> p.getRiskScore() != null ? p.getRiskScore() : 0.0).average().orElse(0.0);
            cluster.put("riskLevel", avgRisk);

            result.add(cluster);
        }

        return result;
    }

    private Long find(Map<Long, Long> parent, Long x) {
        if (!parent.get(x).equals(x)) {
            parent.put(x, find(parent, parent.get(x)));
        }
        return parent.get(x);
    }

    private void union(Map<Long, Long> parent, Long a, Long b) {
        Long rootA = find(parent, a);
        Long rootB = find(parent, b);
        if (!rootA.equals(rootB)) parent.put(rootA, rootB);
    }
}