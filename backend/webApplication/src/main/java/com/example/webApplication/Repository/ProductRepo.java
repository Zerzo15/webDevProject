package com.example.webApplication.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.webApplication.Entity.Product;

public interface ProductRepo extends JpaRepository<Product, Long>{
    
}
