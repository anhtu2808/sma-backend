-- ==============================================
-- INITIAL DATA
-- ==============================================
-- Init Company
INSERT INTO companies (name, country, company_industry, size, description, status, email, phone)
VALUES ('Tech Innovations', 'Vietnam', 'Information Technology', '50-100', 'Leading tech solution provider', 'ACTIVE', 'contact@techinnovations.com', '0123456789');

-- Init Company Location
INSERT INTO company_locations (company_id, name, address, district, city, country, description)
VALUES ((SELECT id FROM companies WHERE name = 'Tech Innovations'), 'Headquarters', '123 Tech Park', 'District 1', 'Ho Chi Minh City', 'Vietnam', 'Main Office');

-- Init User (Recruiter)
INSERT INTO users (email, password_hash, status, full_name, role)
VALUES ('recruiter@techinnovations.com', '$2a$10$NotRealHashJustExample', 'ACTIVE', 'John Recruiter', 'RECRUITER');

-- Link User to Company (Recruiter)
INSERT INTO recruiters (user_id, company_id, is_verified)
VALUES ((SELECT id FROM users WHERE email = 'recruiter@techinnovations.com'), (SELECT id FROM companies WHERE name = 'Tech Innovations'), true);

-- Init Expertise
INSERT INTO job_expertise_groups (name, description) VALUES ('Software Development', 'All software related jobs');
INSERT INTO job_expertises (name, description, expertise_group_id)
VALUES ('Backend Developer', 'Server side development', (SELECT id FROM job_expertise_groups WHERE name = 'Software Development'));

-- Init Skills
INSERT INTO skill_categories (name) VALUES ('Programming Language'), ('Framework');
INSERT INTO skills (name, category_id) VALUES ('Java', (SELECT id FROM skill_categories WHERE name = 'Programming Language'));
INSERT INTO skills (name, category_id) VALUES ('Spring Boot', (SELECT id FROM skill_categories WHERE name = 'Framework'));
INSERT INTO skills (name, category_id) VALUES ('PostgreSQL', (SELECT id FROM skill_categories WHERE name = 'Programming Language'));

-- Init Benefits
INSERT INTO benefits (name, description) VALUES ('Health Insurance', 'Full coverage'), ('Flexible Hours', 'Work anytime');

-- Init Job 1
INSERT INTO jobs (name, about, responsibilities, requirement, salary_start, salary_end, currency, status, job_level, working_model, quantity, company_id, expertise_id)
VALUES (
           'Senior Backend Engineer',
           'Join our core team enabling high scale.',
           'Design and implement microservices.',
           '5+ years in Java, Spring Boot.',
           30000000, 60000000, 'VND',
           'APPROVED',
           'SENIOR',
           'HYBRID',
           2,
           (SELECT id FROM companies WHERE name = 'Tech Innovations'),
           (SELECT id FROM job_expertises WHERE name = 'Backend Developer')
       );

-- Init Job 2
INSERT INTO jobs (name, about, responsibilities, requirement, salary_start, salary_end, currency, status, job_level, working_model, quantity, company_id, expertise_id)
VALUES (
           'Junior Java Developer',
           'Great opportunity for learning.',
           'Maintain existing codebase.',
           '1+ years in Java.',
           15000000, 25000000, 'VND',
           'APPROVED',
           'JUNIOR',
           'ONSITE',
           5,
           (SELECT id FROM companies WHERE name = 'Tech Innovations'),
           (SELECT id FROM job_expertises WHERE name = 'Backend Developer')
       );

-- Link Job 1 Relations
INSERT INTO job_skills (job_id, skill_id) VALUES
                                              ((SELECT id FROM jobs WHERE name = 'Senior Backend Engineer'), (SELECT id FROM skills WHERE name = 'Java')),
                                              ((SELECT id FROM jobs WHERE name = 'Senior Backend Engineer'), (SELECT id FROM skills WHERE name = 'Spring Boot'));

INSERT INTO job_benefits (job_id, benefit_id) VALUES
    ((SELECT id FROM jobs WHERE name = 'Senior Backend Engineer'), (SELECT id FROM benefits WHERE name = 'Health Insurance'));

INSERT INTO job_locations (job_id, company_location_id) VALUES
    ((SELECT id FROM jobs WHERE name = 'Senior Backend Engineer'), (SELECT id FROM company_locations WHERE name = 'Headquarters'));

-- Link Job 2 Relations
INSERT INTO job_skills (job_id, skill_id) VALUES
    ((SELECT id FROM jobs WHERE name = 'Junior Java Developer'), (SELECT id FROM skills WHERE name = 'Java'));

INSERT INTO job_benefits (job_id, benefit_id) VALUES
    ((SELECT id FROM jobs WHERE name = 'Junior Java Developer'), (SELECT id FROM benefits WHERE name = 'Flexible Hours'));

INSERT INTO job_locations (job_id, company_location_id) VALUES
    ((SELECT id FROM jobs WHERE name = 'Junior Java Developer'), (SELECT id FROM company_locations WHERE name = 'Headquarters'));
