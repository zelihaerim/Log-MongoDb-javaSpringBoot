package com.zeliha.ayrotek.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zeliha.ayrotek.Entity.Product;

public interface ProductRepository extends JpaRepository<Product,Integer> {

}
