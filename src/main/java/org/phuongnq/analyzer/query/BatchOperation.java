package org.phuongnq.analyzer.query;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.dto.aff.AdsDto;
import org.phuongnq.analyzer.dto.aff.OrderDto;
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
    public int batchInsertOrUpdateAds(Long sid, List<AdsDto> entities) {
        String insertSql = String.format("""
            INSERT INTO ads (sId, %s) VALUES (?, %s)
        """,
            String.join(", ", AdsDto.FIELDS),
            String.join(", ", "?".repeat(AdsDto.FIELDS.length).split("")),
            generatePlaceholders(AdsDto.FIELDS)
        );

        // For databases that don't support ON CONFLICT, you'd need separate update/insert logic or a stored procedure.

        return jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                AdsDto entity = entities.get(i);
                ps.setLong(1,  sid);
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
                setBigDecimal(ps, 15, entity.getAmountSpent());
            }

            @Override
            public int getBatchSize() {
                return entities.size();
            }
        }).length;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int batchInsertOrUpdateOrders(Long sid, List<OrderDto> entities) {
        String insertSql = String.format("""
            INSERT INTO orders (sId, %s) VALUES (?, %s)
        """,
            String.join(", ", OrderDto.FIELDS),
            String.join(", ", "?".repeat(OrderDto.FIELDS.length).split("")),
            generatePlaceholders(OrderDto.FIELDS)
        );

        // For databases that don't support ON CONFLICT, you'd need separate update/insert logic or a stored procedure.
        return jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                OrderDto entity = entities.get(i);
                ps.setLong(1, sid);
                ps.setString(2, entity.getOrderId());
                ps.setString(3, entity.getOrderStatus());
                ps.setString(4, entity.getCheckoutId());
                setTimestampFromString(ps, 5, entity.getOrderTime());
                setTimestampFromString(ps, 6, entity.getCompletionTime());
                setTimestampFromString(ps, 7, entity.getClickTime());
                ps.setString(8, entity.getShopName());
                ps.setString(9, entity.getShopId());
                ps.setString(10, entity.getShopType());
                ps.setString(11, entity.getItemId());
                ps.setString(12, entity.getItemName());
                ps.setString(13, entity.getModelId());
                ps.setString(14, entity.getProductType());
                ps.setString(15, entity.getPromotionId());
                ps.setString(16, entity.getGlobalCatL1());
                ps.setString(17, entity.getGlobalCatL2());
                ps.setString(18, entity.getGlobalCatL3());
                ps.setBigDecimal(19, entity.getSalePrice());
                setIntFromInteger(ps, 20, entity.getQuantity());
                ps.setString(21, entity.getAffiliateCommissionType());
                ps.setString(22, entity.getCampaignPartner());
                ps.setBigDecimal(23, entity.getOrderValue());
                ps.setBigDecimal(24, entity.getRefundAmount());
                setBigDecimalFromString(ps, 25, entity.getCommissionRateOnProduct());
                setBigDecimalFromString(ps, 26, entity.getCommissionOnProduct());
                setBigDecimalFromString(ps, 27, entity.getSellerCommissionRateOnProduct());
                setBigDecimalFromString(ps, 28, entity.getXtraCommissionOnProduct());
                setBigDecimalFromString(ps, 29, entity.getTotalProductCommission());
                setBigDecimalFromString(ps, 30, entity.getCommissionFromOrder());
                setBigDecimalFromString(ps, 31, entity.getCommissionFromSeller());
                setBigDecimal(ps, 32, entity.getTotalOrderCommission());
                ps.setString(33, entity.getMcnName());
                ps.setString(34, entity.getMcnContractCode());
                setBigDecimalFromString(ps, 35, entity.getMcnManagementRate());
                setBigDecimalFromString(ps, 36, entity.getMcnManagementFee());
                setBigDecimalFromString(ps, 37, entity.getAgreedAffiliateMarketingCommissionRate());
                setBigDecimal(ps, 38, entity.getNetAffiliateMarketingCommission());
                ps.setString(39, entity.getLinkedProductStatus());
                ps.setString(40, entity.getProductNotes());
                ps.setString(41, entity.getAttributeType());
                ps.setString(42, entity.getBuyerStatus());
                ps.setString(43, entity.getSubId1());
                ps.setString(44, entity.getSubId2());
                ps.setString(45, entity.getSubId3());
                ps.setString(46, entity.getSubId4());
                ps.setString(47, entity.getSubId5());
                ps.setString(48, entity.getChannel());
            }

            @Override
            public int getBatchSize() {
                return entities.size();
            }
        }).length;
    }

    public int upsertCampaigns(Long sid, List<String> campaignNames) {
        String insertSql = """
            INSERT INTO campaign (sId, name, unmapped) VALUES (?, ?, false)
            ON CONFLICT (sId, name) DO NOTHING
        """;

        return jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, sid);
                ps.setString(2, campaignNames.get(i));
            }

            @Override
            public int getBatchSize() {
                return campaignNames.size();
            }
        }).length;
    }

    public int upsertSubIds(Long sid, List<String> subIds) {
        String insertSql = """
            INSERT INTO orderLink (sId, subId) VALUES (?, ?)
            ON CONFLICT (sId, subId) DO NOTHING
        """;

        return jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, sid);
                ps.setString(2, subIds.get(i));
            }

            @Override
            public int getBatchSize() {
                return subIds.size();
            }
        }).length;
    }

    private String generatePlaceholders(String[] fields) {
        return Arrays.stream(fields)
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

    private static void setDate(PreparedStatement ps, int idx, LocalDate value) throws SQLException {
        if (value == null) {
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

    private static void setBigDecimal(PreparedStatement ps, int idx, BigDecimal bd) throws SQLException {
        if (bd == null) {
            ps.setNull(idx, Types.NUMERIC);
        } else {
            ps.setBigDecimal(idx, bd);
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
