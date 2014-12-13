-- Production script for MySQL.
CREATE TABLE IF NOT EXISTS inout_board_user (
  id           INT          NOT NULL AUTO_INCREMENT UNIQUE PRIMARY KEY,
  user_handle  VARCHAR(32)  NOT NULL,
  user_name    VARCHAR(255) NOT NULL,
  status       VARCHAR(32)  NOT NULL,
  last_updated DATETIME,
  comment      VARCHAR(255)
);