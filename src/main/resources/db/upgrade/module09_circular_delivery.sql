-- Circulars and emergency alerts are delivered through the school's own providers (Settings → Integrations):
-- one delivery row per recipient, QUEUED until sent, SKIPPED when it cannot be sent (no number or email, no provider, app push).
ALTER TABLE broadcast_delivery_logs ADD COLUMN IF NOT EXISTS recipient_name character varying(300);
ALTER TABLE broadcast_delivery_logs DROP CONSTRAINT IF EXISTS broadcast_delivery_logs_delivery_status_check;
ALTER TABLE broadcast_delivery_logs ADD CONSTRAINT broadcast_delivery_logs_delivery_status_check CHECK (((delivery_status)::text = ANY ((ARRAY['QUEUED'::character varying, 'SENT'::character varying, 'DELIVERED'::character varying, 'READ'::character varying, 'FAILED'::character varying, 'SKIPPED'::character varying])::text[])));
