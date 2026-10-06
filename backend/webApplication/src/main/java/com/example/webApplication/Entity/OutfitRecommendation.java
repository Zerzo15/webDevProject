package com.example.webApplication.Entity;

import java.time.Instant;

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
    private User appUser;

    private String context_prompt;

    private long budget_limit;

    private String ai_response_text;

    private Instant created_at;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getAppUser() {
        return appUser;
    }

    public void setAppUser(User appUser) {
        this.appUser = appUser;
    }

    public String getContext_prompt() {
        return context_prompt;
    }

    public void setContext_prompt(String context_prompt) {
        this.context_prompt = context_prompt;
    }

    public long getBudget_limit() {
        return budget_limit;
    }

    public void setBudget_limit(long budget_limit) {
        this.budget_limit = budget_limit;
    }

    public String getAi_response_text() {
        return ai_response_text;
    }

    public void setAi_response_text(String ai_response_text) {
        this.ai_response_text = ai_response_text;
    }

    public Instant getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Instant created_at) {
        this.created_at = created_at;
    }
}
