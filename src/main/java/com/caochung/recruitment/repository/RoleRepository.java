package com.caochung.recruitment.repository;

import com.caochung.recruitment.domain.Permission;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.dto.request.RoleRequestDTO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long>, JpaSpecificationExecutor<Role> {
    boolean existsByName(String name);

    Role findByName(String name);

    @Override
    @EntityGraph(attributePaths = {"permissions"})
    Optional<Role> findById(Long id);

    List<Role> findAllByPermissionsContains(Permission permission);
}
