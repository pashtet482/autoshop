package com.example.autoshop.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class SoftDeletable {

    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    public boolean isActive() {
        return !Boolean.TRUE.equals(isDeleted);
    }

    public void markDeleted() {
        isDeleted = true;
        deletedAt = OffsetDateTime.now();
    }
}
