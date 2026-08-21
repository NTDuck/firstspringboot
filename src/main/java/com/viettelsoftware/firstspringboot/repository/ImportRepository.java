package com.viettelsoftware.firstspringboot.repository;

import com.viettelsoftware.firstspringboot.entity.Import;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface ImportRepository extends JpaRepository<Import, Long> {

    // MariaDB SELECT ... FOR UPDATE pessimistic write lock for atomic job claiming
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Import i WHERE i.id = :id")
    Optional<Import> findByIdForUpdate(@Param("id") Long id);
}
