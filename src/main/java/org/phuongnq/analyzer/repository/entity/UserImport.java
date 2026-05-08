package org.phuongnq.analyzer.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.phuongnq.analyzer.query.model.EfficiencyLevel;

@Entity
@Table(name = "userImport")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserImport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDate dataDate;
    private Instant createdTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sId")
    private Shop shop;
}
