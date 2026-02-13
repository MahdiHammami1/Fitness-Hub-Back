package com.example.demo.service;

import com.example.demo.model.Coach;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CoachService {
    Coach createCoach(Coach coach);
    Optional<Coach> getCoachById(String id);
    Page<Coach> getAllCoaches(Pageable pageable);
    List<Coach> getCoachesByDomain(String domain);
    Page<Coach> getCoachesByDomain(String domain, Pageable pageable);
    Coach updateCoach(String id, Coach coach);
    void deleteCoach(String id);
}

