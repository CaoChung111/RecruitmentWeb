package com.caochung.recruitment.repository;

import com.caochung.recruitment.constant.JobStatusEnum;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    boolean existsByName(String name);

    List<Job> findAllBySkillsContaining(Skill skill);

    List<Job> findAllByActive(JobStatusEnum active);

    @Modifying
    @Query("UPDATE Job j SET j.active = 'INACTIVE', j.updatedAt = :now, j.updatedBy = :updatedBy WHERE j.company.id = :companyId")
    void inactivateJobsByCompanyId(Instant now, String updatedBy,Long companyId);

    @Modifying
    @Query(value = "UPDATE jobs SET active = 'CLOSED', updated_at = CURRENT_TIMESTAMP WHERE company_id = :companyId", nativeQuery = true)
    void restoreJobsByCompanyId(Long companyId);

    boolean existsByIdAndCompany_Id(Long jobId, Long id);
}
