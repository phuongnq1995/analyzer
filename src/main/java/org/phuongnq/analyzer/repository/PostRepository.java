package org.phuongnq.analyzer.repository;

import org.phuongnq.analyzer.repository.entity.Click;
import org.phuongnq.analyzer.repository.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, String> {

}
