package com.caochung.recruitment.repository;

import com.caochung.recruitment.constant.ResumeStatusEnum;
import com.caochung.recruitment.domain.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ResumeRepository extends JpaRepository<Resume,Long>, JpaSpecificationExecutor<Resume> {
    boolean existsByJob_IdAndStatusIn(Long id, List<ResumeStatusEnum> statusEnums);

    @Modifying
    @Query("UPDATE Resume r SET r.status = 'SYSTEM_CANCEL', r.updatedAt = :now, r.updatedBy = :updatedBy WHERE r.job.company.id = :companyId")
    void inactivateResumeByCompanyId(Instant now, String updatedBy, Long companyId);

    @Modifying
    @Query("UPDATE Resume r SET r.status = 'WITHDRAWN', r.updatedAt = :now, r.updatedBy = :updatedBy WHERE r.user.id = :userId")
    void withdrawnResumeByUserId(Instant now, String updatedBy, Long userId);
}
