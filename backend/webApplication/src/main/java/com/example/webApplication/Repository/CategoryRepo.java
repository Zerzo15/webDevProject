package com.example.webApplication.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.webApplication.Entity.Category;

public interface CategoryRepo extends JpaRepository<Category, Long>{
    
}
