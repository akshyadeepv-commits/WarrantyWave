export interface AuditLog {
  id: number;
  entityName: string;
  entityId: number;
  actionType: string;
  performedBy: string;
  logTimestamp: string;
  details?: string;
}
