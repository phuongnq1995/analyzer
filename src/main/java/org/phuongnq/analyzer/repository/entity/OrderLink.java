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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orderLink")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String subId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sId")
    private Shop shop;

    @OneToMany(mappedBy = "orderLink", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Campaign> campaigns = new HashSet<>();

    public void addCampaign(Campaign campaign) {
        campaign.setUnmapped(false);
        campaigns.add(campaign);
        campaign.setOrderLink(this);
    }
}
