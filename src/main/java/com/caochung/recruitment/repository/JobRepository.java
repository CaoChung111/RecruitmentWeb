package com.caochung.recruitment.repository;

import com.caochung.recruitment.constant.JobStatusEnum;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.Skill;
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
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    boolean existsByName(String name);

    @Override
    @EntityGraph(attributePaths = {"company"})
    Page<Job> findAll(Specification<Job> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"company", "skills"})
    Optional<Job> findById(Long id);

    List<Job> findAllBySkillsContaining(Skill skill);

    @EntityGraph(attributePaths = {"company"})
    List<Job> findAllByActive(JobStatusEnum active);

    @Modifying
    @Query("UPDATE Job j SET j.active = 'INACTIVE', j.updatedAt = :now, j.updatedBy = :updatedBy WHERE j.company.id = :companyId")
    void inactivateJobsByCompanyId(Instant now, String updatedBy,Long companyId);

    @Modifying
    @Query(value = "UPDATE jobs SET active = 'CLOSED', updated_at = CURRENT_TIMESTAMP WHERE company_id = :companyId", nativeQuery = true)
    void restoreJobsByCompanyId(Long companyId);

    boolean existsByIdAndCompany_Id(Long jobId, Long id);
}
