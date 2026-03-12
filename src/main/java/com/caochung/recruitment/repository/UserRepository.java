package com.caochung.recruitment.repository;

import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    User findByEmail(String email);

    boolean existsByEmail(String email);

    User findByRefreshTokenAndEmail(String refreshToken, String email);

    List<User> findAllByCompany(Company company);

    @Modifying
    @Query("UPDATE User u SET u.status = 'DISABLED', u.refreshToken=null, u.updatedAt = :now, u.updatedBy = :updatedBy WHERE u.company.id = :companyId")
    void inactivateUsersByCompanyId(Instant now, String updatedBy, Long companyId);

    @Query(value = "SELECT * FROM users u WHERE u.id = :id", nativeQuery = true)
    Optional<User> findByIdIncludingDeleted(Long id);

    @Query(value = "SELECT * FROM users u WHERE u.status = 'DISABLED'",
            countQuery = "SELECT count(*) FROM users WHERE status = 'DISABLED'",
            nativeQuery = true)
    Page<User> findAllDisableUsers(Pageable pageable);


    @Modifying
    @Query(value = "UPDATE users SET status = 'ACTIVE', updated_at = CURRENT_TIMESTAMP WHERE id = :id", nativeQuery = true)
    void restoreUserById(Long id);

    @Modifying
    @Query(value = "UPDATE users  SET status = 'ACTIVE', refresh_token=null, updated_at = CURRENT_TIMESTAMP WHERE company_id = :companyId", nativeQuery = true)
    void restoreUsersByCompanyId(Long companyId);

    boolean existsByRole(Role role);
}
