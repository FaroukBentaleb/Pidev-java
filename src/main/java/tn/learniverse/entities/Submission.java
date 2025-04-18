//package tn.learniverse.entities;
//
//import tn.learniverse.tools.Session;
//
//import java.time.LocalDate;
//
//public class Submission {
//    private int id;
//    private Session.User user;
//    private Competition competition;
//    private LocalDate date;
//    private String studentTry;
//    private String aiFeedback;
//    private int timeTaken;
//
//    public Submission() {
//    }
//
//    public Submission(User user, Competition competition, LocalDate date, String studentTry, String aiFeedback, int timeTaken) {
//        this.user = user;
//        this.competition = competition;
//        this.date = date;
//        this.studentTry = studentTry;
//        this.aiFeedback = aiFeedback;
//        this.timeTaken = timeTaken;
//    }
//
//    // Getters and Setters
//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public User getUser() {
//        return user;
//    }
//
//    public void setUser(User user) {
//        this.user = user;
//    }
//
//    public Competition getCompetition() {
//        return competition;
//    }
//
//    public void setCompetition(Competition competition) {
//        this.competition = competition;
//    }
//
//    public LocalDate getDate() {
//        return date;
//    }
//
//    public void setDate(LocalDate date) {
//        this.date = date;
//    }
//
//    public String getStudentTry() {
//        return studentTry;
//    }
//
//    public void setStudentTry(String studentTry) {
//        this.studentTry = studentTry;
//    }
//
//    public String getAiFeedback() {
//        return aiFeedback;
//    }
//
//    public void setAiFeedback(String aiFeedback) {
//        this.aiFeedback = aiFeedback;
//    }
//
//    public int getTimeTaken() {
//        return timeTaken;
//    }
//
//    public void setTimeTaken(int timeTaken) {
//        this.timeTaken = timeTaken;
//    }
//
//    @Override
//    public String toString() {
//        return "Submission{" +
//                "id=" + id +
//                ", user=" + user +
//                ", competition=" + competition +
//                ", date=" + date +
//                ", studentTry='" + studentTry + '\'' +
//                ", aiFeedback='" + aiFeedback + '\'' +
//                ", timeTaken=" + timeTaken +
//                '}';
//    }
//}