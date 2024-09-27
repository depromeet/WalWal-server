package com.depromeet.stonebed.domain.comment.application;

import com.depromeet.stonebed.domain.comment.dao.CommentRepository;
import com.depromeet.stonebed.domain.comment.dto.request.CommentCreateRequest;
import com.depromeet.stonebed.domain.comment.dto.response.CommentCreateResponse;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final MemberUtil memberUtil;
    private final CommentRepository commentRepository;
    private final MissionRecordRepository missionRecordRepository;

    public CommentCreateResponse createComment(CommentCreateRequest request) {
        final Member member = memberUtil.getCurrentMember();
        final MissionRecord missionRecord =
                missionRecordRepository
                        .findById(request.recordId())
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));

        return CommentCreateResponse.of(null);
    }
}
