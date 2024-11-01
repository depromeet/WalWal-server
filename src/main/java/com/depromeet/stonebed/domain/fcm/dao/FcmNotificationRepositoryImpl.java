package com.depromeet.stonebed.domain.fcm.dao;

import static com.depromeet.stonebed.domain.fcm.domain.QFcmNotification.*;
import static com.depromeet.stonebed.domain.missionRecord.domain.QMissionRecord.*;

import com.depromeet.stonebed.domain.fcm.domain.FcmNotification;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordDisplay;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class FcmNotificationRepositoryImpl implements FcmNotificationRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<FcmNotification> findMissionRecordNotificationByMemberPaging(
            Long memberId, LocalDateTime cursorDate, Pageable pageable) {
        return jpaQueryFactory
                .select(fcmNotification)
                .from(fcmNotification)
                .innerJoin(fcmNotification.member)
                .on(fcmNotification.member.id.eq(memberId))
                .innerJoin(missionRecord)
                .on(fcmNotification.targetId.eq(missionRecord.id))
                .where(
                        fcmNotification
                                .createdAt
                                .loe(cursorDate)
                                .and(missionRecord.display.eq(MissionRecordDisplay.PUBLIC)))
                .orderBy(fcmNotification.createdAt.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();
    }
}
