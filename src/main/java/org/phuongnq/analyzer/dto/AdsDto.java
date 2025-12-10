package org.phuongnq.analyzer.dto;
import com.opencsv.bean.CsvIgnore;
import lombok.Data;

@Data
public class AdsDto {
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
    private String id;
    private String campaignName;                     // Tên chiến dịch
    private String adGroupName;                      // Tên nhóm quảng cáo
    private String date;                          // Ngày
    private String adName;                           // Tên quảng cáo
    private String campaignId;                           // Tên quảng cáo
    private String deliveryStatus;                   // Trạng thái phân phối
    private String deliveryLevel;                    // Cấp độ phân phối
    private Integer reach;                           // Người tiếp cận
    private Integer impressions;                     // Lượt hiển thị
    private String frequency;                        // Tần suất
    private String attributionSetting;               // Cài đặt ghi nhận
    private String resultType;                       // Loại kết quả
    private Integer results;                         // Kết quả (numeric)
    private String amountSpent;                  // Số tiền đã chi tiêu (VND)

    public void generateIdIfAbsent() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = generateId();
        }
    }

    public String generateId() {
        return date+campaignId;
    }
}
