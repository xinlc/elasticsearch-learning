package com.es.example.repository;

import com.es.example.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 商品 Dao
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

}
