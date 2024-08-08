package com.depromeet.stonebed.domain.feed.dao;

import static com.depromeet.stonebed.domain.missionRecord.domain.QMissionRecord.missionRecord;

import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedRepositoryImpl implements FeedRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<MissionRecord> getFeedContentsUsingCursor(
            Long missionRecordId, Long memberId, int limit) {
        return queryFactory
                .select(missionRecord)
                .from(missionRecord)
                .where(
                        missionRecord
                                .id
                                .lt(missionRecordId)
                                .and(missionRecord.member.id.eq(memberId)))
                .orderBy(missionRecord.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<MissionRecord> getFeedContents(Long memberId, int limit) {
        return queryFactory
                .select(missionRecord)
                .from(missionRecord)
                .where(missionRecord.member.id.eq(memberId))
                .orderBy(missionRecord.id.desc())
                .limit(limit)
                .fetch();
    }
}
