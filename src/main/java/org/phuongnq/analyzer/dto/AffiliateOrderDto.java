package org.phuongnq.analyzer.dto;

import com.opencsv.bean.CsvBindByName;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class AffiliateOrderDto {

    public static final String[] FIELDS = {
        "orderId",
        "orderStatus",
        "checkoutId",
        "orderTime",
        "completionTime",
        "clickTime",
        "shopName",
        "shopId",
        "shopType",
        "itemId",
        "itemName",
        "modelId",
        "productType",
        "promotionId",
        "globalCatL1",
        "globalCatL2",
        "globalCatL3",
        "salePrice",
        "quantity",
        "affiliateCommissionType",
        "campaignPartner",
        "orderValue",
        "refundAmount",
        "commissionRateOnProduct",
        "commissionOnProduct",
        "sellerCommissionRateOnProduct",
        "xtraCommissionOnProduct",
        "totalProductCommission",
        "commissionFromOrder",
        "commissionFromSeller",
        "totalOrderCommission",
        "mcnName",
        "mcnContractCode",
        "mcnManagementRate",
        "mcnManagementFee",
        "agreedAffiliateMarketingCommissionRate",
        "netAffiliateMarketingCommission",
        "linkedProductStatus",
        "productNotes",
        "attributeType",
        "buyerStatus",
        "subId1",
        "subId2",
        "subId3",
        "subId4",
        "subId5",
        "channel"
    };

    @CsvBindByName(column = "ID đơn hàng")
    private String orderId;

    @CsvBindByName(column = "Trạng thái đặt hàng")
    private String orderStatus;

    @CsvBindByName(column = "Checkout id")
    private String checkoutId;

    @CsvBindByName(column = "Thời Gian Đặt Hàng")
    private String orderTime;

    @CsvBindByName(column = "Thời gian hoàn thành")
    private String completionTime;

    @CsvBindByName(column = "Thời gian Click")
    private String clickTime;

    @CsvBindByName(column = "Tên Shop")
    private String shopName;

    @CsvBindByName(column = "Shop id")
    private String shopId;

    @CsvBindByName(column = "Loại Shop")
    private String shopType;

    @CsvBindByName(column = "Item id")
    private String itemId;

    @CsvBindByName(column = "Tên Item")
    private String itemName;

    @CsvBindByName(column = "ID Model")
    private String modelId;

    @CsvBindByName(column = "Loại sản phẩm")
    private String productType;

    @CsvBindByName(column = "Promotion id")
    private String promotionId;

    @CsvBindByName(column = "L1 Danh mục toàn cầu")
    private String globalCatL1;

    @CsvBindByName(column = "L2 Danh mục toàn cầu")
    private String globalCatL2;

    @CsvBindByName(column = "L3 Danh mục toàn cầu")
    private String globalCatL3;

    @CsvBindByName(column = "Giá(₫)")
    private BigDecimal salePrice;

    @CsvBindByName(column = "Số lượng")
    private Integer quantity;

    @CsvBindByName(column = "Loại Hoa hồng")
    private String affiliateCommissionType;

    @CsvBindByName(column = "Đối tác chiến dịchr")
    private String campaignPartner;

    @CsvBindByName(column = "Giá trị đơn hàng (₫)")
    private BigDecimal orderValue;

    @CsvBindByName(column = "Số tiền hoàn trả (₫)")
    private BigDecimal refundAmount;

    @CsvBindByName(column = "Tỷ lệ sản phẩm hoa hồng Shope")
    private String commissionRateOnProduct;

    @CsvBindByName(column = "Hoa hồng Shopee trên sản phẩm(₫)")
    private String commissionOnProduct;

    @CsvBindByName(column = "Tỷ lệ sản phẩm hoa hồng người bán")
    private String sellerCommissionRateOnProduct;

    @CsvBindByName(column = "Hoa hồng Xtra trên sản phẩm(₫)")
    private String xtraCommissionOnProduct;

    @CsvBindByName(column = "Tổng hoa hồng sản phẩm(₫)")
    private String totalProductCommission;

    @CsvBindByName(column = "Hoa hồng đơn hàng từ Shopee(₫)")
    private String commissionFromOrder;

    @CsvBindByName(column = "Hoa hồng đơn hàng từ Người bán(₫)")
    private String commissionFromSeller;

    @CsvBindByName(column = "Tổng hoa hồng đơn hàng(₫)")
    private String totalOrderCommission;

    @CsvBindByName(column = "Tên MNC đã liên kết")
    private String mcnName;

    @CsvBindByName(column = "Mã hợp đồng MCN")
    private String mcnContractCode;

    @CsvBindByName(column = "Mức phí quản lý MCN")
    private String mcnManagementRate;

    @CsvBindByName(column = "Phí quản lý MCN(₫)")
    private String mcnManagementFee;

    @CsvBindByName(column = "Mức hoa hồng tiếp thị liên kết theo thỏa thuận")
    private String agreedAffiliateMarketingCommissionRate;

    @CsvBindByName(column = "Hoa hồng ròng tiếp thị liên kết(₫)")
    private String netAffiliateMarketingCommission;

    @CsvBindByName(column = "Trạng thái sản phẩm liên kết")
    private String linkedProductStatus;

    @CsvBindByName(column = "Ghi chú sản phẩm")
    private String productNotes;

    @CsvBindByName(column = "Loại thuộc tính")
    private String attributeType;

    @CsvBindByName(column = "Trạng thái người mua")
    private String buyerStatus;

    @CsvBindByName(column = "Sub_id1")
    private String subId1;

    @CsvBindByName(column = "Sub_id2")
    private String subId2;

    @CsvBindByName(column = "Sub_id3")
    private String subId3;

    @CsvBindByName(column = "Sub_id4")
    private String subId4;

    @CsvBindByName(column = "Sub_id5")
    private String subId5;

    @CsvBindByName(column = "Kênh")
    private String channel;
}
