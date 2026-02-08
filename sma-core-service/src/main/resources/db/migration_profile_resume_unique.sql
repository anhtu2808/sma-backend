BEGIN;

WITH ranked AS (
  SELECT id,
         ROW_NUMBER() OVER (PARTITION BY candidate_id ORDER BY id DESC) AS rn
  FROM resumes
  WHERE type = 'PROFILE'::resume_type
    AND candidate_id IS NOT NULL
)
UPDATE resumes r
SET type = 'ORIGINAL'::resume_type
FROM ranked x
WHERE r.id = x.id
  AND x.rn > 1;

INSERT INTO resumes (candidate_id, type, status, parse_status, is_default, is_overrided)
SELECT c.id,
       'PROFILE'::resume_type,
       'ACTIVE'::resume_status,
       'WAITING'::parse_status,
       FALSE,
       FALSE
FROM candidates c
WHERE NOT EXISTS (
  SELECT 1
  FROM resumes r
  WHERE r.candidate_id = c.id
    AND r.type = 'PROFILE'::resume_type
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_resumes_one_profile_per_candidate
ON resumes (candidate_id)
WHERE type = 'PROFILE'::resume_type;

COMMIT;
