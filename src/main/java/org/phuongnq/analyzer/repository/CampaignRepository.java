package org.phuongnq.analyzer.repository;

import java.util.Collection;
import java.util.List;
import org.phuongnq.analyzer.repository.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findByShopId(Long sId);

    @Query("SELECT c FROM Campaign c WHERE c.shop.id = ?1 AND c.normalizedName IN (?2)")
    List<Campaign> findByShopIdAndNormalizedNameIn(Long sid, Collection<String> campaignNames);
}
