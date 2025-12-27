package org.phuongnq.analyzer.repository;

import java.util.Collection;
import java.util.List;
import org.phuongnq.analyzer.repository.entity.OrderLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderLinkRepository extends JpaRepository<OrderLink, Long> {

    @Query("""
        SELECT o FROM OrderLink o LEFT JOIN FETCH campaigns c
        WHERE o.shop.id = ?1 AND o.subId IN (?2)
        """)
    List<OrderLink> findByShopIdAndSubIdIn(Long sId, Collection<String> subIds);
}
