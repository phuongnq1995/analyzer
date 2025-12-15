package org.phuongnq.analyzer.dto.aff;
import com.opencsv.bean.CsvIgnore;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class AdsDto {
    private static final long serialVersionUID = 1L;

    public static final String[] FIELDS = {
        "campaignName",
        "adGroupName",
        "date",
        "adName",
        "campaignId",
        "deliveryStatus",
        "deliveryLevel",
        "reach",
        "impressions",
        "frequency",
        "attributionSetting",
        "resultType",
        "results",
        "amountSpent"
    };

    @CsvIgnore
    private Long id;

    @CsvIgnore
    private Long sId;

    private String campaignName;                     // Tên chiến dịch
    private String adGroupName;
    private String date;                          // Ngày
    private String adName;
    private String campaignId;                           // Tên quảng cáo
    private String deliveryStatus;                   // Trạng thái phân phối
    private String deliveryLevel;                    // Cấp độ phân phối
    private Integer reach;                           // Người tiếp cận
    private Integer impressions;                     // Lượt hiển thị
    private String frequency;                        // Tần suất
    private String attributionSetting;               // Cài đặt ghi nhận
    private String resultType;                       // Loại kết quả
    private Integer results;                         // Kết quả (numeric)
    private BigDecimal amountSpent;                  // Số tiền đã chi tiêu (VND)
}
