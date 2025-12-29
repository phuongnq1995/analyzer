package org.phuongnq.analyzer.repository;

import java.util.Collection;
import java.util.List;
import org.phuongnq.analyzer.dto.shop.CampaignDto;
import org.phuongnq.analyzer.repository.entity.Campaign;
import org.phuongnq.analyzer.repository.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findByShopId(Long sId);

    @Query("SELECT c FROM Campaign c WHERE c.shop = ?1 AND c.id IN (?2)")
    List<Campaign> findByIdIn(Shop sid, List<Long> ids);

    @Query("SELECT c FROM Campaign c WHERE c.shop.id = ?1 AND c.normalizedName IN (?2)")
    List<Campaign> findByShopIdAndNormalizedNameIn(Long sid, Collection<String> campaignNames);

    @Query("SELECT c FROM Campaign c WHERE c.shop.id = ?1 AND c.unmapped = TRUE AND c.normalizedName IN (?2)")
    List<Campaign> findUnmappedByShopIAndNormalizedNameIn(Long sid, Collection<String> campaignNames);

    @Query("SELECT c FROM Campaign c WHERE c.shop = ?1 AND c.unmapped = TRUE")
    List<Campaign> getUnmappedCampaigns(Shop shop);

    @Query("SELECT new org.phuongnq.analyzer.dto.shop.CampaignDto(c.id, c.name, c.unmapped) FROM Campaign c WHERE c.shop = ?1")
    List<CampaignDto> getAll(Shop shop);

}
