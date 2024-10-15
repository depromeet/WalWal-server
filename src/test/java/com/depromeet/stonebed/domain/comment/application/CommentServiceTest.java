package com.depromeet.stonebed.domain.comment.application;

import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.stonebed.FixtureMonkeySetUp;
import com.depromeet.stonebed.global.util.MemberUtil;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class CommentServiceTest extends FixtureMonkeySetUp {

    @InjectMocks private CommentService commentService;
    @Mock private MemberUtil memberUtil;
}
