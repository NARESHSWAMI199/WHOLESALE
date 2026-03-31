package com.sales.admin.repositories;


import com.sales.entities.EncryptedImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EncryptedImageRepository extends JpaRepository<EncryptedImage, Integer> {
}
