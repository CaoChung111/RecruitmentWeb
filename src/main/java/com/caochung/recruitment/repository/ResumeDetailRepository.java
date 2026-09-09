package com.caochung.recruitment.repository;

import com.caochung.recruitment.domain.ResumeDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeDetailRepository extends JpaRepository<ResumeDetail, Long> {
    Optional<ResumeDetail> findByResume_Id(Long resumeId);
}
