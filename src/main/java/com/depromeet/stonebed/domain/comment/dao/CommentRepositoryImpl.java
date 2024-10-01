package com.depromeet.stonebed.domain.comment.dao;

import static com.depromeet.stonebed.domain.comment.domain.QComment.comment;

import com.depromeet.stonebed.domain.comment.domain.Comment;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Comment> findAllCommentsByMissionRecord(MissionRecord missionRecord) {
        return queryFactory
                .selectFrom(comment)
                .leftJoin(comment.parent)
                .fetchJoin()
                .leftJoin(comment.children)
                .fetchJoin()
                .where(comment.missionRecord.eq(missionRecord))
                .fetch();
    }
}
