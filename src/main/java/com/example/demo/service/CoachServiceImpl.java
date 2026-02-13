package com.example.demo.service;

import com.example.demo.model.Coach;
import com.example.demo.repository.CoachRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoachServiceImpl implements CoachService {

    private final CoachRepository coachRepository;

    @Override
    public Coach createCoach(Coach coach) {
        coach.setCreatedAt(Instant.now());
        coach.setUpdatedAt(Instant.now());
        return coachRepository.save(coach);
    }

    @Override
    public Optional<Coach> getCoachById(String id) {
        return coachRepository.findById(id);
    }

    @Override
    public Page<Coach> getAllCoaches(Pageable pageable) {
        return coachRepository.findAll(pageable);
    }

    @Override
    public List<Coach> getCoachesByDomain(String domain) {
        return coachRepository.findByDomain(domain);
    }

    @Override
    public Page<Coach> getCoachesByDomain(String domain, Pageable pageable) {
        return coachRepository.findByDomain(domain, pageable);
    }

    @Override
    public Coach updateCoach(String id, Coach coachUpdates) {
        return coachRepository.findById(id)
                .map(existingCoach -> {
                    if (coachUpdates.getFullName() != null) {
                        existingCoach.setFullName(coachUpdates.getFullName());
                    }
                    if (coachUpdates.getTitle() != null) {
                        existingCoach.setTitle(coachUpdates.getTitle());
                    }
                    if (coachUpdates.getDomain() != null) {
                        existingCoach.setDomain(coachUpdates.getDomain());
                    }
                    if (coachUpdates.getImageUrl() != null) {
                        existingCoach.setImageUrl(coachUpdates.getImageUrl());
                    }
                    if (coachUpdates.getBio() != null) {
                        existingCoach.setBio(coachUpdates.getBio());
                    }
                    if (coachUpdates.getQuote() != null) {
                        existingCoach.setQuote(coachUpdates.getQuote());
                    }
                    if (coachUpdates.getExperienceYears() != null) {
                        existingCoach.setExperienceYears(coachUpdates.getExperienceYears());
                    }
                    if (coachUpdates.getClientsTransformed() != null) {
                        existingCoach.setClientsTransformed(coachUpdates.getClientsTransformed());
                    }
                    if (coachUpdates.getEventsHosted() != null) {
                        existingCoach.setEventsHosted(coachUpdates.getEventsHosted());
                    }
                    if (coachUpdates.getSatisfactionRate() != null) {
                        existingCoach.setSatisfactionRate(coachUpdates.getSatisfactionRate());
                    }
                    if (coachUpdates.getCertifications() != null) {
                        existingCoach.setCertifications(coachUpdates.getCertifications());
                    }
                    existingCoach.setUpdatedAt(Instant.now());
                    return coachRepository.save(existingCoach);
                })
                .orElseThrow(() -> new RuntimeException("Coach not found with id: " + id));
    }

    @Override
    public void deleteCoach(String id) {
        coachRepository.deleteById(id);
    }
}

