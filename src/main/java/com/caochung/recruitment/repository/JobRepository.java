package com.caochung.recruitment.repository;

import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    boolean existsByName(String name);

    List<Job> findAllBySkillsContaining(Skill skill);
}
