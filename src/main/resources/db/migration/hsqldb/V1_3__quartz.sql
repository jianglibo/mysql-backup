 CREATE TABLE cronexpression (
  id             INT          NOT NULL PRIMARY KEY,
  description     VARCHAR(200),
  expression      VARCHAR(200)  NOT NULL
);
 
 CREATE TABLE mailaddress (
  id             INT          NOT NULL PRIMARY KEY,
  description     VARCHAR(200),
  email      VARCHAR(200)  NOT NULL
);

CREATE TABLE cron_mail (
  mail_id      INT          NOT NULL,
  cron_id        INT          NOT NULL,
   
  PRIMARY KEY (mail_id, cron_id),
  CONSTRAINT fk_ab_mail     FOREIGN KEY (mail_id)  REFERENCES mailaddress (id),
  CONSTRAINT fk_ab_cron       FOREIGN KEY (cron_id)    REFERENCES cronexpression   (id)
);