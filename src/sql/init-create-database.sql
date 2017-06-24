DROP DATABASE IF EXISTS papercrawler;
CREATE DATABASE papercrawler DEFAULT CHARACTER SET utf8 COLLATE utf8_bin;

-- init users
CREATE USER 'winter'@'localhost' IDENTIFIED BY 'Cndnj37!@#';
CREATE USER 'summer'@'localhost' IDENTIFIED BY 'Ejdnj49!@#';

-- DB
USE mysql;

-- papercrawler Database
INSERT INTO db (HOST,Db,USER,Select_priv,Insert_priv,Update_priv,Delete_priv,Create_priv,Drop_priv,Index_priv,Alter_priv) VALUES('%','papercrawler','summer','Y','Y','Y','Y','N','N','N','N');
INSERT INTO db (HOST,Db,USER,Select_priv,Insert_priv,Update_priv,Delete_priv,Create_priv,Drop_priv,Index_priv,Alter_priv) VALUES('%','papercrawler','winter','Y','Y','Y','Y','Y','Y','Y','Y');

FLUSH PRIVILEGES;
