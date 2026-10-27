DELETE FROM friendship
WHERE status = 'ACCEPTED'
  AND (requester_id = ? OR addressee_id = ?)
