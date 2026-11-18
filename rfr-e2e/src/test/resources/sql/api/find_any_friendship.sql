SELECT id
FROM friendship
WHERE requester_id = ?
   OR addressee_id = ?
LIMIT 1
