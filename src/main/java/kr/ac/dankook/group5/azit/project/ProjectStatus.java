package kr.ac.dankook.group5.azit.project;

import lombok.Getter;

@Getter
public enum ProjectStatus {
    IN_PROGRESS("진행중", "in-progress"),
    COMPLETED("완료", "completed"),
    PAUSED("중단", "paused");

    private final String label;
    private final String cssClass;

    ProjectStatus(String label, String cssClass) {
        this.label = label;
        this.cssClass = cssClass;
    }
}
