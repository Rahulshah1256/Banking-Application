package com.jantabank.repository;

import com.jantabank.entity.UpiHandle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UpiHandleRepository extends JpaRepository<UpiHandle, Long> {

    Optional<UpiHandle> findByVpaIgnoreCase(String vpa);

    boolean existsByVpaIgnoreCase(String vpa);

    List<UpiHandle> findByUserIdOrderByPrimaryDescIdAsc(Long userId);
}
