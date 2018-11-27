CREATE DATABASE OV_payrates;

Humres (res_id integer,
fullname varchar(200),
emp_stat char(20),
freefield_16 varchar(20),
PK (res_id));

Employee (crdnr integer,
PurchasePrice double,
vandatum Date,
totdatum Date,
FK (crdnr) REF Humres(res_id));

Local_accounts (username string,
password string
PK (username));

Google_accounts (e_mail string);