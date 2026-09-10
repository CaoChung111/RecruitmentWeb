package com.caochung.recruitment.repository;

import com.caochung.recruitment.constant.ResumeStatusEnum;
import com.caochung.recruitment.domain.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume,Long>, JpaSpecificationExecutor<Resume> {

    @Override
    @EntityGraph(attributePaths = {"user", "job", "job.company", "resumeDetail"})
    Page<Resume> findAll(Specification<Resume> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user", "job", "job.company", "resumeDetail"})
    Optional<Resume> findById(Long id);

    boolean existsByJob_IdAndStatusIn(Long id, List<ResumeStatusEnum> statusEnums);

    boolean existsByJob_IdAndEmail(Long id,  String email);

    @Modifying
    @Query("UPDATE Resume r SET r.status = 'SYSTEM_CANCEL', r.updatedAt = :now, r.updatedBy = :updatedBy WHERE r.job.company.id = :companyId")
    void inactivateResumeByCompanyId(Instant now, String updatedBy, Long companyId);

    @Modifying
    @Query("UPDATE Resume r SET r.status = 'WITHDRAWN', r.updatedAt = :now, r.updatedBy = :updatedBy WHERE r.user.id = :userId")
    void withdrawnResumeByUserId(Instant now, String updatedBy, Long userId);

    boolean existsByIdAndJob_Company_Id(Long resumeId, Long id);

    boolean existsByIdAndUser_Id(Long resumeId, Long id);
}
