package com.depromeet.stonebed.domain.fcm.application;

import com.depromeet.stonebed.domain.fcm.dao.FcmRepository;
import com.depromeet.stonebed.domain.fcm.domain.FcmToken;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final FcmRepository fcmRepository;
    private final MemberUtil memberUtil;

    @Transactional(readOnly = true)
    public List<String> getAllTokens() {
        return fcmRepository.findAll().stream()
                .map(FcmToken::getToken)
                .filter(token -> !token.isEmpty())
                .toList();
    }

    @Transactional
    public void invalidateTokenForCurrentMember() {
        Member currentMember = memberUtil.getCurrentMember();
        fcmRepository
                .findByMember(currentMember)
                .ifPresentOrElse(
                        fcmToken -> updateToken(fcmToken, null),
                        () -> {
                            throw new CustomException(ErrorCode.FAILED_TO_FIND_FCM_TOKEN);
                        });
    }

    @Transactional
    public void invalidateToken(String token) {
        fcmRepository
                .findByToken(token)
                .ifPresentOrElse(
                        fcmToken -> updateToken(fcmToken, null),
                        () -> {
                            throw new CustomException(ErrorCode.FAILED_TO_FIND_FCM_TOKEN);
                        });
    }

    private void updateToken(FcmToken fcmToken, String token) {
        fcmToken.updateToken(token);
        fcmRepository.save(fcmToken);
    }

    @Transactional
    public void storeOrUpdateToken(String token) {
        final Member member = memberUtil.getCurrentMember();

        if (token == null || token.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_FCM_TOKEN);
        }

        Optional<FcmToken> tokenByValue = fcmRepository.findByToken(token);
        tokenByValue.ifPresent(
                existingToken -> {
                    if (!existingToken.getMember().equals(member)) {
                        fcmRepository.delete(existingToken);
                    }
                });

        Optional<FcmToken> existingTokenByMember = fcmRepository.findByMember(member);
        existingTokenByMember.ifPresentOrElse(
                fcmToken -> {
                    fcmToken.updateToken(token);
                    fcmRepository.save(fcmToken);
                },
                () -> {
                    FcmToken fcmToken = new FcmToken(member, token);
                    fcmRepository.save(fcmToken);
                });
    }
}
