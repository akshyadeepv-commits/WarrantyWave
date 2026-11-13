package com.warrantywave.model.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "AUDIT_LOGS")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ENTITY_NAME", nullable = false, length = 60)
    private String entityName;

    @Column(name = "ENTITY_ID", nullable = false)
    private Long entityId;

    @Column(name = "ACTION_TYPE", nullable = false, length = 40)
    private String actionType; // CREATE, UPDATE, APPROVE, REJECT, STATUS_CHANGE

    @Column(name = "PERFORMED_BY", nullable = false, length = 60)
    private String performedBy;

    @Column(name = "LOG_TIMESTAMP", nullable = false)
    private OffsetDateTime logTimestamp;

    @Column(length = 2000)
    private String details;

    public AuditLog() {
    }

    public AuditLog(String entityName, Long entityId, String actionType, String performedBy, String details) {
        this.entityName = entityName;
        this.entityId = entityId;
        this.actionType = actionType;
        this.performedBy = performedBy;
        this.details = details;
        this.logTimestamp = OffsetDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.logTimestamp == null) {
            this.logTimestamp = OffsetDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public OffsetDateTime getLogTimestamp() {
        return logTimestamp;
    }

    public void setLogTimestamp(OffsetDateTime logTimestamp) {
        this.logTimestamp = logTimestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
