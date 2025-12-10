package org.phuongnq.analyzer.query;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.dto.AdsDto;
import org.phuongnq.analyzer.dto.AffiliateOrderDto;
import org.phuongnq.analyzer.dto.ClickDto;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BatchOperation {

    private final JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void batchInsertOrUpdateCLicks(List<ClickDto> entities) {
        String insertSql = """
            INSERT INTO click (id, click_time, area_zone, sub_ids, channel) VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                click_time = EXCLUDED.click_time,
                area_zone = EXCLUDED.area_zone,
                sub_ids = EXCLUDED.sub_ids,
                channel = EXCLUDED.channel
        """;
        // For databases that don't support ON CONFLICT, you'd need separate update/insert logic or a stored procedure.

        jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ClickDto entity = entities.get(i);
                ps.setString(1, entity.getId());
                ps.setTimestamp(2,  Timestamp.valueOf(entity.getClickTime()));
                ps.setString(3, entity.getAreaZone());
                ps.setString(4, entity.getSubIds());
                ps.setString(5, entity.getChannel());
            }

            @Override
            public int getBatchSize() {
                return entities.size();
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void batchInsertOrUpdateAds(List<AdsDto> entities) {
        String insertSql = String.format("""
            INSERT INTO ads (id, %s) VALUES (?, %s)
            ON CONFLICT (id) DO UPDATE SET %s
        """,
            String.join(", ", AdsDto.FIELDS),
            String.join(", ", "?".repeat(AdsDto.FIELDS.length).split("")),
            generatePlaceholders(AdsDto.FIELDS, "id")
        );

        System.out.println(insertSql);
        // For databases that don't support ON CONFLICT, you'd need separate update/insert logic or a stored procedure.

        jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                AdsDto entity = entities.get(i);
                entity.generateIdIfAbsent();
                ps.setString(1,  entity.getId());

                ps.setString(2,  entity.getCampaignName());
                ps.setString(3,  entity.getAdGroupName());
                setDateFromString(ps, 4, entity.getDate());
                ps.setString(5,  entity.getAdName());
                ps.setString(6,  entity.getCampaignId());
                ps.setString(7,  entity.getDeliveryStatus());
                ps.setString(8,  entity.getDeliveryLevel());
                setIntFromInteger(ps,  9, entity.getReach());
                setIntFromInteger(ps,  10, entity.getImpressions());
                setBigDecimalFromString(ps, 11, entity.getFrequency());
                ps.setString(12, entity.getAttributionSetting());
                ps.setString(13, entity.getResultType());
                setIntFromInteger(ps,  14, entity.getResults());
                setBigDecimalFromString(ps, 15, entity.getAmountSpent());
            }

            @Override
            public int getBatchSize() {
                return entities.size();
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void batchInsertOrUpdateOrders(List<AffiliateOrderDto> entities) {
        String insertSql = String.format("""
            INSERT INTO affiliate_orders (%s) VALUES (%s)
            ON CONFLICT (orderId) DO UPDATE SET %s
        """,
            String.join(", ", AffiliateOrderDto.FIELDS),
            String.join(", ", "?".repeat(AffiliateOrderDto.FIELDS.length).split("")),
            generatePlaceholders(AffiliateOrderDto.FIELDS, "orderId")
        );

        System.out.println(insertSql);
        // For databases that don't support ON CONFLICT, you'd need separate update/insert logic or a stored procedure.
        jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                AffiliateOrderDto entity = entities.get(i);
                ps.setString(1, entity.getOrderId()+entity.getItemId());
                ps.setString(2, entity.getOrderStatus());
                ps.setString(3, entity.getCheckoutId());
                setTimestampFromString(ps, 4, entity.getOrderTime());
                setTimestampFromString(ps, 5, entity.getCompletionTime());
                setTimestampFromString(ps, 6, entity.getClickTime());
                ps.setString(7, entity.getShopName());
                ps.setString(8, entity.getShopId());
                ps.setString(9, entity.getShopType());
                ps.setString(10, entity.getItemId());
                ps.setString(11, entity.getItemName());
                ps.setString(12, entity.getModelId());
                ps.setString(13, entity.getProductType());
                ps.setString(14, entity.getPromotionId());
                ps.setString(15, entity.getGlobalCatL1());
                ps.setString(16, entity.getGlobalCatL2());
                ps.setString(17, entity.getGlobalCatL3());
                ps.setBigDecimal(18, entity.getSalePrice());
                setIntFromInteger(ps, 19, entity.getQuantity());
                ps.setString(20, entity.getAffiliateCommissionType());
                ps.setString(21, entity.getCampaignPartner());
                ps.setBigDecimal(22, entity.getOrderValue());
                ps.setBigDecimal(23, entity.getRefundAmount());
                setBigDecimalFromString(ps, 24, entity.getCommissionRateOnProduct()); // DECIMAL(10,2) <- DTO String
                setBigDecimalFromString(ps, 25, entity.getCommissionOnProduct()); // DECIMAL(18,2) <- DTO String
                setBigDecimalFromString(ps, 26, entity.getSellerCommissionRateOnProduct()); // DECIMAL(10,2)
                setBigDecimalFromString(ps, 27, entity.getXtraCommissionOnProduct()); // DECIMAL(18,2)
                setBigDecimalFromString(ps, 28, entity.getTotalProductCommission()); // DECIMAL(18,2)
                setBigDecimalFromString(ps, 29, entity.getCommissionFromOrder()); // DECIMAL(18,2)
                setBigDecimalFromString(ps, 30, entity.getCommissionFromSeller()); // DECIMAL(18,2)
                setBigDecimalFromString(ps, 31, entity.getTotalOrderCommission()); // DECIMAL(18,2)
                ps.setString(32, entity.getMcnName());
                ps.setString(33, entity.getMcnContractCode());
                setBigDecimalFromString(ps, 34, entity.getMcnManagementRate()); // DECIMAL(10,2)
                setBigDecimalFromString(ps, 35, entity.getMcnManagementFee()); // DECIMAL(18,2)
                setBigDecimalFromString(ps, 36, entity.getAgreedAffiliateMarketingCommissionRate()); // DECIMAL(10,2)
                setBigDecimalFromString(ps, 37, entity.getNetAffiliateMarketingCommission()); // DECIMAL(18,2)
                ps.setString(38, entity.getLinkedProductStatus());
                ps.setString(39, entity.getProductNotes());
                ps.setString(40, entity.getAttributeType());
                ps.setString(41, entity.getBuyerStatus());
                ps.setString(42, entity.getSubId1());
                ps.setString(43, entity.getSubId2());
                ps.setString(44, entity.getSubId3());
                ps.setString(45, entity.getSubId4());
                ps.setString(46, entity.getSubId5());
                ps.setString(47, entity.getChannel());
            }

            @Override
            public int getBatchSize() {
                return entities.size();
            }
        });
    }

    private String generatePlaceholders(String[] fields, String identifierField) {
        return Arrays.stream(fields)
            .filter(field -> !field.equals(identifierField))
            .map(field -> field + " = EXCLUDED." + field)
            .collect(Collectors.joining(", "));
    }

    // Helpers for nullable conversions
    private static void setTimestampFromString(PreparedStatement ps, int idx, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            ps.setNull(idx, Types.TIMESTAMP);
            return;
        }
        try {
            ps.setTimestamp(idx, Timestamp.valueOf(value));
        } catch (IllegalArgumentException ex) {
            ps.setNull(idx, Types.TIMESTAMP);
        }
    }

    private static void setDateFromString(PreparedStatement ps, int idx, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            ps.setNull(idx, Types.DATE);
            return;
        }
        try {
            ps.setDate(idx, Date.valueOf(value));
        } catch (IllegalArgumentException ex) {
            ps.setNull(idx, Types.DATE);
        }
    }

    private static void setBigDecimalFromLong(PreparedStatement ps, int idx, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(idx, Types.NUMERIC);
        } else {
            ps.setBigDecimal(idx, BigDecimal.valueOf(value));
        }
    }

    private static void setDouble(PreparedStatement ps, int idx, Double value) throws SQLException {
        if (value == null) {
            ps.setNull(idx, Types.NUMERIC);
        } else {
            ps.setDouble(idx, value);
        }
    }

    private static void setIntFromInteger(PreparedStatement ps, int idx, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(idx, Types.INTEGER);
        } else {
            ps.setInt(idx, value);
        }
    }

    private static void setBigDecimalFromString(PreparedStatement ps, int idx, String value) throws SQLException {
        BigDecimal bd = parseBigDecimal(value);
        if (bd == null) {
            ps.setNull(idx, Types.NUMERIC);
        } else {
            ps.setBigDecimal(idx, bd);
        }
    }

    private static BigDecimal parseBigDecimal(String s) {
        if (s == null) return null;
        String cleaned = s.replaceAll("[^\\d.-]", "");
        if (cleaned.isBlank()) return null;
        try {
            return new BigDecimal(cleaned);
        } catch (Exception ex) {
            return null;
        }
    }

}
