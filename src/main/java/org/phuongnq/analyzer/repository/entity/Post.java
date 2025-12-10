package org.phuongnq.analyzer.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "post")
@Setter
@Getter
public class Post {

    @Id
    String id;
    String name;

}
