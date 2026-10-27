DELETE FROM friendship
WHERE status = 'PENDING'
  AND (requester_id = ? OR addressee_id = ?)
