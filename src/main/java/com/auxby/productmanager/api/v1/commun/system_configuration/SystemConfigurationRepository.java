package com.auxby.productmanager.api.v1.commun.system_configuration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, Integer> {
    @Query("SELECT sc FROM SystemConfiguration sc WHERE sc.code LIKE :pattern")
    List<SystemConfiguration> findByCodePattern(@Param("pattern") String pattern);
}
