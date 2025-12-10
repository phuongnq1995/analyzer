package org.phuongnq.analyzer.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "click")
@Setter
@Getter
public class Click {

    @Id
    String id;
    LocalDateTime clickTime;
    String areaZone;
    String subIds;
    String channel;
}
