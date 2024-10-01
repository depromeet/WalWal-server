package com.depromeet.stonebed.domain.comment.dao;

import com.depromeet.stonebed.domain.comment.domain.Comment;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import java.util.List;

public interface CommentRepositoryCustom {
    List<Comment> findAllCommentsByMissionRecord(MissionRecord missionRecord);
}
