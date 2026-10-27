INSERT INTO friendship (id, requester_id, addressee_id, status, created_at, responded_at, version)
VALUES (UUID_TO_BIN(UUID(), true), ?, ?, 'PENDING', NOW(6), NULL, 0)
