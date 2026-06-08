-- Migration: separate global message visibility from message content
ALTER TABLE global_limits
    ADD COLUMN IF NOT EXISTS message_enabled BOOLEAN NOT NULL DEFAULT FALSE;

-- Existing messages were shown when non-empty; keep them visible after migration
UPDATE global_limits
SET message_enabled = TRUE
WHERE message IS NOT NULL AND TRIM(message) <> '';
