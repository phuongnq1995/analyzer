package org.phuongnq.analyzer.repository.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "evaluateEfficiency")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EvaluateEfficiency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate evaluateDate;
    private Instant createdTime;
    private String errorStatus;

    @Builder.Default
    @OneToMany(mappedBy = "evaluateEfficiency", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EvaluateCampaignEfficiency> campaignEfficiencies = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sId")
    private Shop shop;

    public void addCampaignEfficiency(EvaluateCampaignEfficiency campaignEfficiency) {
        if (campaignEfficiency != null) {
            if (campaignEfficiency == null) {
                campaignEfficiencies = new HashSet<>();
            }
            campaignEfficiencies.add(campaignEfficiency);
            campaignEfficiency.setEvaluateEfficiency(this);
        }
    }
}
