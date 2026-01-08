package org.phuongnq.analyzer.repository;

import java.util.List;
import org.phuongnq.analyzer.repository.entity.ConversionCurvePercentage;
import org.phuongnq.analyzer.repository.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConversionCurvePercentageRepository extends JpaRepository<ConversionCurvePercentage, Long> {

    @Query("SELECT c FROM ConversionCurvePercentage c WHERE c.shop = ?1 AND c.name = ?2")
    List<ConversionCurvePercentage> findByShopAndName(Shop sid, String name);
}
