create table demo.bankdetails(acc_id numeric(13) primary key,username varchar(30),balance numeric(10));
insert into demo.bankdetails(acc_id,username,balance) values(1234567890,"B.Sravani",50000);
select *from demo.bankdetails;
create table demo.translist(serialno numeric(10),acc_id numeric(13),amount numeric(10),foreign key(acc_id) references demo.bankdetails(acc_id)); 
select *from demo.translist;
DELIMITER $$
DROP PROCEDURE IF EXISTS `demo`.`get_transcation_list`$$

CREATE PROCEDURE `demo`.`get_transcation_list`(IN accountid numeric(13))
BEGIN
	
	select amount from demo.translist where serialno>((select count(*) from demo.translist)-5) and acc_id=accountid;

END$$
DELIMITER ;
