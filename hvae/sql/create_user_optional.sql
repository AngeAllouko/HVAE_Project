-- Optional: create a dedicated MySQL user instead of using root.
-- Run as an administrator, then put these credentials in src/main/resources/db.properties.
CREATE USER IF NOT EXISTS 'hvae'@'localhost' IDENTIFIED BY 'choose_a_password';
GRANT ALL PRIVILEGES ON hvae.* TO 'hvae'@'localhost';
-- The application creates the database itself (createDatabaseIfNotExist=true); if your user may not do that, run:
CREATE DATABASE IF NOT EXISTS hvae CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
FLUSH PRIVILEGES;
