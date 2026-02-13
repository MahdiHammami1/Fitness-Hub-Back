package com.example.demo.repository;

import com.example.demo.model.Coach;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CoachRepository extends MongoRepository<Coach, String> {
    Optional<Coach> findByFullName(String fullName);
    List<Coach> findByDomain(String domain);
    Page<Coach> findByDomain(String domain, Pageable pageable);
    Page<Coach> findAll(Pageable pageable);
}

