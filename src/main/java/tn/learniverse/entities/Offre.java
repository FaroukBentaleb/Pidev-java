package tn.learniverse.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Offre {
    private int id;
    private String name;
    private double pricePerMonth;
    private String applicablePlans;
    private String customPlan;
    private Double discount;
    private String description;
    private String conditions;
    private String promoCode;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private boolean isActive;
    private Integer maxSubscriptions;
    private String targetAudience;
    private String benefits;
    private LocalDateTime createdAt;
    private int courseId;

    // Constructors
    public Offre() {
    }

    public Offre(int id, String name, double pricePerMonth, String applicablePlans, String customPlan,
                 Double discount, String description, String conditions, String promoCode,
                 LocalDate validFrom, LocalDate validUntil, boolean isActive, Integer maxSubscriptions,
                 String targetAudience, String benefits, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.pricePerMonth = pricePerMonth;
        this.applicablePlans = applicablePlans;
        this.customPlan = customPlan;
        this.discount = discount;
        this.description = description;
        this.conditions = conditions;
        this.promoCode = promoCode;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.isActive = isActive;
        this.maxSubscriptions = maxSubscriptions;
        this.targetAudience = targetAudience;
        this.benefits = benefits;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPricePerMonth() {
        return pricePerMonth;
    }

    public void setPricePerMonth(double pricePerMonth) {
        this.pricePerMonth = pricePerMonth;
    }

    public String getApplicablePlans() {
        return applicablePlans;
    }

    public void setApplicablePlans(String applicablePlans) {
        this.applicablePlans = applicablePlans;
    }

    public String getCustomPlan() {
        return customPlan;
    }

    public void setCustomPlan(String customPlan) {
        this.customPlan = customPlan;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Integer getMaxSubscriptions() {
        return maxSubscriptions;
    }

    public void setMaxSubscriptions(Integer maxSubscriptions) {
        this.maxSubscriptions = maxSubscriptions;
    }

    public String getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(String targetAudience) {
        this.targetAudience = targetAudience;
    }

    public String getBenefits() {
        return benefits;
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    // Helper method to get status based on isActive
    public String getStatus() {
        return isActive ? "Active" : "Inactive";
    }

    @Override
    public String toString() {
        return "Offre{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", pricePerMonth=" + pricePerMonth +
                ", applicablePlans='" + applicablePlans + '\'' +
                ", customPlan='" + customPlan + '\'' +
                ", discount=" + discount +
                ", validFrom=" + validFrom +
                ", validUntil=" + validUntil +
                ", isActive=" + isActive +
                ", courseId=" + courseId +
                '}';
    }
} 