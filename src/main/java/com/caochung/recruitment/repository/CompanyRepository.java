package com.caochung.recruitment.repository;

import com.caochung.recruitment.domain.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {
    Boolean existsByName(String name);

    @Query(value = "SELECT * FROM companies WHERE id = :id", nativeQuery = true)
    Optional<Company> findByIdIncludingDeleted(Long id);

    @Modifying
    @Query(value = "UPDATE companies SET status = 'ACTIVE', updated_at = CURRENT_TIMESTAMP WHERE id = :id", nativeQuery = true)
    void restoreCompanyById(Long id);

    @Query(value = "SELECT * FROM companies c WHERE c.status = 'INACTIVE'",
    countQuery = "SELECT count(*) FROM companies WHERE status = 'INACTIVE'",
    nativeQuery = true)
    Page<Company> findAllInactiveCompanies(Pageable pageable);

}
