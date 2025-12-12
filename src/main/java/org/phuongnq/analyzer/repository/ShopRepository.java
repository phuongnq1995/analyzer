package org.phuongnq.analyzer.repository;

import java.util.List;
import org.phuongnq.analyzer.controller.StatisticController;
import org.phuongnq.analyzer.repository.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    List<Shop> findByUserId(Long id);
}

