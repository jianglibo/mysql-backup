ALTER TABLE reuseable_cron ADD CONSTRAINT unique_expression UNIQUE (expression);
ALTER TABLE mail_address ADD CONSTRAINT unique_email UNIQUE (email);