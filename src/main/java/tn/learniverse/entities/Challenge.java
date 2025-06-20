package tn.learniverse.entities;

public class Challenge {
    private int id;
    private String title;
    private String description;
    private String content;
    private String solution;
    private int competitionId;

    // Constructors, getters, and setters...
    public Challenge(){

    }

    public Challenge(int challengeId, String challengeTitle, String challengeContent, String challengeSolution) {
        this.id = challengeId;
        this.title = challengeTitle;
        this.content = challengeContent;
        this.solution = challengeSolution;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Challenge)) return false;
        Challenge that = (Challenge) o;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public int getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(int competitionId) {
        this.competitionId = competitionId;
    }
}