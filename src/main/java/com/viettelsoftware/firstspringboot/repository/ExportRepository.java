package com.viettelsoftware.firstspringboot.repository;

import com.viettelsoftware.firstspringboot.entity.Export;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface ExportRepository extends JpaRepository<Export, Long> {

    // MariaDB SELECT ... FOR UPDATE pessimistic write lock for atomic job claiming
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Export e WHERE e.id = :id")
    Optional<Export> findByIdForUpdate(@Param("id") Long id);
}
