-- HVAE schema (MySQL 8+). Executed automatically at start-up; every statement is idempotent.

CREATE TABLE IF NOT EXISTS scenarios (
  scenario_id            INT AUTO_INCREMENT PRIMARY KEY,
  scenario_key           VARCHAR(40) NULL,
  title                  VARCHAR(150) NOT NULL,
  user_prompt            TEXT NOT NULL,
  known_facts            TEXT NULL,
  contradiction_patterns TEXT NULL,
  is_predefined          BOOLEAN NOT NULL DEFAULT FALSE,
  created_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_scenarios_key (scenario_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS rules (
  rule_id        VARCHAR(10) PRIMARY KEY,
  value_name     VARCHAR(20) NOT NULL,
  rule_name      VARCHAR(150) NOT NULL,
  weight         INT NOT NULL,
  condition_text TEXT NOT NULL,
  explanation    TEXT NOT NULL,
  recommendation TEXT NOT NULL,
  active         BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS evaluations (
  evaluation_id   INT AUTO_INCREMENT PRIMARY KEY,
  scenario_id     INT NOT NULL,
  response_text   TEXT NOT NULL,
  overall_outcome VARCHAR(30) NOT NULL,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_eval_scenario FOREIGN KEY (scenario_id) REFERENCES scenarios(scenario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS value_assessments (
  assessment_id INT AUTO_INCREMENT PRIMARY KEY,
  evaluation_id INT NOT NULL,
  value_name    VARCHAR(20) NOT NULL,
  score         INT NOT NULL,
  outcome       VARCHAR(30) NOT NULL,
  note          VARCHAR(300) NULL,
  CONSTRAINT fk_va_eval FOREIGN KEY (evaluation_id) REFERENCES evaluations(evaluation_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS triggered_rules (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  assessment_id INT NOT NULL,
  rule_id       VARCHAR(10) NOT NULL,
  matched_text  VARCHAR(300) NULL,
  suppressed    BOOLEAN NOT NULL DEFAULT FALSE,
  suppressed_by VARCHAR(40) NULL,
  CONSTRAINT fk_tr_assessment FOREIGN KEY (assessment_id) REFERENCES value_assessments(assessment_id) ON DELETE CASCADE,
  CONSTRAINT fk_tr_rule FOREIGN KEY (rule_id) REFERENCES rules(rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS test_cases (
  case_id       VARCHAR(10) PRIMARY KEY,
  scenario_id   INT NOT NULL,
  split_name    VARCHAR(5) NOT NULL,
  response_text TEXT NOT NULL,
  CONSTRAINT fk_tc_scenario FOREIGN KEY (scenario_id) REFERENCES scenarios(scenario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS test_case_labels (
  case_id         VARCHAR(10) NOT NULL,
  value_name      VARCHAR(20) NOT NULL,
  reference_label VARCHAR(30) NOT NULL,
  PRIMARY KEY (case_id, value_name),
  CONSTRAINT fk_tcl_case FOREIGN KEY (case_id) REFERENCES test_cases(case_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS test_runs (
  run_id     INT AUTO_INCREMENT PRIMARY KEY,
  run_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  split_name VARCHAR(5) NOT NULL,
  tau        INT NOT NULL,
  n_cases    INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS test_run_results (
  run_id     INT NOT NULL,
  case_id    VARCHAR(10) NOT NULL,
  value_name VARCHAR(20) NOT NULL,
  predicted  VARCHAR(30) NOT NULL,
  reference  VARCHAR(30) NOT NULL,
  score      INT NOT NULL,
  rule_ids   VARCHAR(200) NULL,
  PRIMARY KEY (run_id, case_id, value_name),
  CONSTRAINT fk_trr_run FOREIGN KEY (run_id) REFERENCES test_runs(run_id) ON DELETE CASCADE,
  CONSTRAINT fk_trr_case FOREIGN KEY (case_id) REFERENCES test_cases(case_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
