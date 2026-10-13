package com.example.webApplication.Entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
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
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    private String gender;

    @Column(name = "height_cm")
    private int heightCm;

    @Column(name = "weight_kg")
    private int weightKg;

    @Column(name = "bust_cm")
    private int bustCm;

    @Column(name = "waist_cm")
    private int waistCm;

    @Column(name = "hips_cm")
    private int hipsCm;

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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(int heightCm) {
        this.heightCm = heightCm;
    }

    public int getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(int weightKg) {
        this.weightKg = weightKg;
    }

    public int getBustCm() {
        return bustCm;
    }

    public void setBustCm(int bustCm) {
        this.bustCm = bustCm;
    }

    public int getWaistCm() {
        return waistCm;
    }

    public void setWaistCm(int waistCm) {
        this.waistCm = waistCm;
    }

    public int getHipsCm() {
        return hipsCm;
    }

    public void setHipsCm(int hipsCm) {
        this.hipsCm = hipsCm;
    }
}
