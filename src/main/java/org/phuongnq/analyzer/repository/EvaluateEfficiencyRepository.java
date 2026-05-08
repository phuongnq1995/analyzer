package org.phuongnq.analyzer.repository;

import java.util.Optional;
import org.phuongnq.analyzer.repository.entity.EvaluateEfficiency;
import org.phuongnq.analyzer.repository.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluateEfficiencyRepository extends JpaRepository<EvaluateEfficiency, Long> {

    Optional<EvaluateEfficiency> findTop1ByShopOrderByEvaluateDateDesc(Shop shop);
}

