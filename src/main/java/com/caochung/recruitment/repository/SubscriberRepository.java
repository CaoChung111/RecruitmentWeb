package com.caochung.recruitment.repository;

import com.caochung.recruitment.domain.Skill;
import com.caochung.recruitment.domain.Subscriber;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long>, JpaSpecificationExecutor<Subscriber> {
    boolean existsByEmail(String email);

    @Override
    @EntityGraph(attributePaths = {"skills"})
    Optional<Subscriber> findById(Long id);

    List<Subscriber> findAllBySkillsContaining(Skill skill);
}
