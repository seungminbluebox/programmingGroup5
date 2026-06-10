package kr.ac.dankook.group5.azit.project;

import java.util.List;

public record ProjectRecommendation(
        Project project,
        long matchCount,
        List<String> matchedStacks,
        long memberCount,
        List<String> memberNames,
        Long ownerId,
        String ownerName,
        boolean alreadyMember,
        boolean alreadyApplied
) {
}
