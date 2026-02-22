package com.caochung.recruitment.repository;

import com.caochung.recruitment.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission,Long>, JpaSpecificationExecutor<Permission> {
    boolean existsByApiPathAndMethodAndModule(String apiPath, String method, String module);

    boolean existsByApiPathAndMethodAndModuleAndIdNot(String apiPath, String method, String module, Long id);

}
