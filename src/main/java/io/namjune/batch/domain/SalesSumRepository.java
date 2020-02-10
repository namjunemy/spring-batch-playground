package io.namjune.batch.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesSumRepository extends JpaRepository<SalesSum, Long> {
}
