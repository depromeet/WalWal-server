package com.depromeet.stonebed.domain.comment.dao;

import com.depromeet.stonebed.domain.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {}
