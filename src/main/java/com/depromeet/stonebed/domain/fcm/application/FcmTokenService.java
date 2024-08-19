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
        if (token == null || token.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_FCM_TOKEN);
        }

        Member member = memberUtil.getCurrentMember();
        Optional<FcmToken> existingTokenOptional = fcmRepository.findByToken(token);

        existingTokenOptional.ifPresent(
                existingToken -> {
                    if (!existingToken.getMember().equals(member)) {
                        fcmRepository.delete(existingToken);
                    }
                });

        fcmRepository
                .findByMember(member)
                .ifPresentOrElse(
                        fcmToken -> fcmToken.updateToken(token),
                        () -> {
                            FcmToken fcmToken = new FcmToken(member, token);
                            fcmRepository.save(fcmToken);
                        });
    }
}
