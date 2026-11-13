package com.warrantywave.service;

import com.warrantywave.model.entity.AuditLog;
import com.warrantywave.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(String entityName, Long entityId, String actionType, String performedBy, String details) {
        AuditLog log = new AuditLog(entityName, entityId, actionType, performedBy, details);
        auditLogRepository.save(log);
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop50ByOrderByLogTimestampDesc();
    }

    public List<AuditLog> getLogsForEntity(String entityName, Long entityId) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByLogTimestampDesc(entityName, entityId);
    }
}
