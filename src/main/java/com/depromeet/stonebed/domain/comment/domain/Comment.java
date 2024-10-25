package com.depromeet.stonebed.domain.comment.domain;

import com.depromeet.stonebed.domain.common.BaseTimeEntity;
import com.depromeet.stonebed.domain.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "comment")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Schema(description = "미션 기록 ID", example = "1")
    @Column(name = "record_id", nullable = false)
    private Long recordId;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private Member writer;

    @Schema(description = "댓글 내용", example = "너무 이쁘자나~")
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // 부모 댓글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    // 자식 댓글
    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private List<Comment> replyComments = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    public Comment(Long recordId, Member writer, String content, Comment parent) {
        this.recordId = recordId;
        this.writer = writer;
        this.content = content;
        this.parent = parent;
    }

    public static Comment createComment(
            Long recordId, Member writer, String content, @Nullable Comment parent) {
        return Comment.builder()
                .recordId(recordId)
                .writer(writer)
                .content(content)
                .parent(parent)
                .build();
    }
}
