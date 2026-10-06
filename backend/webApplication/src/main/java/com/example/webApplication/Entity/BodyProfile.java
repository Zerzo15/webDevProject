package com.example.webApplication.Entity;

import java.time.Instant;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity 
public class BodyProfile {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne  
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) //this mean when the user delete the account, this data is also wiped away.
    private User appUser;

    private String gender;

    private int height_cm;

    private int weight_kg;

    private int bust_cm;

    private int hips_cm;

    private Instant created_at;

    private Instant updated_at;

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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getHeight_cm() {
        return height_cm;
    }

    public void setHeight_cm(int height_cm) {
        this.height_cm = height_cm;
    }

    public int getWeight_kg() {
        return weight_kg;
    }

    public void setWeight_kg(int weight_kg) {
        this.weight_kg = weight_kg;
    }

    public int getBust_cm() {
        return bust_cm;
    }

    public void setBust_cm(int bust_cm) {
        this.bust_cm = bust_cm;
    }

    public int getHips_cm() {
        return hips_cm;
    }

    public void setHips_cm(int hips_cm) {
        this.hips_cm = hips_cm;
    }

    public Instant getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Instant created_at) {
        this.created_at = created_at;
    }

    public Instant getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Instant updated_at) {
        this.updated_at = updated_at;
    }
}
