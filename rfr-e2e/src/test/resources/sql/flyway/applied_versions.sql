SELECT version
FROM flyway_schema_history
WHERE success = 1
  AND version IS NOT NULL
  AND type <> 'BASELINE'
ORDER BY installed_rank
