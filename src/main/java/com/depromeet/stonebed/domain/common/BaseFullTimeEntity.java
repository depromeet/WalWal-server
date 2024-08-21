package com.depromeet.stonebed.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseFullTimeEntity extends BaseTimeEntity {
    @Column(name = "deleted_at", updatable = false)
    private LocalDateTime deletedAt;
}
