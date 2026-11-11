package com.example.webApplication.Entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class OutfitRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "context_prompt")
    private String contextPrompt;

    @Column(name = "ai_response_text")
    private String aiResponseText;

    @Column(name = "recommended_variant_ids")
    private String recommendedVariantIds;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContextPrompt() {
        return contextPrompt;
    }

    public void setContextPrompt(String contextPrompt) {
        this.contextPrompt = contextPrompt;
    }

    public String getAiResponseText() {
        return aiResponseText;
    }

    public void setAiResponseText(String aiResponseText) {
        this.aiResponseText = aiResponseText;
    }

    public String getRecommendedVariantIds() {
        return recommendedVariantIds;
    }

    public void setRecommendedVariantIds(String recommendedVariantIds) {
        this.recommendedVariantIds = recommendedVariantIds;
    }
}
