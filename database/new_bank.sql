DROP DATABASE BANK;
CREATE DATABASE BANK;
USE BANK;

CREATE TABLE BRANCH(
	BranchID	INTEGER			NOT NULL,
    Name		VARCHAR(20)		NOT NULL,
    City		VARCHAR(20),		
    Address 	VARCHAR(50),		
    Asset		VARCHAR(20),
    Mgr_SSN		CHAR(9),
    PRIMARY KEY(BranchID)
);    
-- -----------------------------------------------------------------
CREATE TABLE EMPLOYEE(
	SSN				CHAR(9)			NOT NULL,
    Name			VARCHAR(20)		NOT NULL,
    Password		VARCHAR(200),
    Telephone		VARCHAR(15),		
    Dependent_name 	VARCHAR(20),		
    Start_date		DATE,
    Length_of_emp	INTEGER,			
    Super_SSN 		CHAR(9) 		DEFAULT '456789012',
    BranchID		INTEGER,
    PRIMARY KEY(SSN),
    FOREIGN KEY(Super_SSN) REFERENCES EMPLOYEE(SSN)
		ON DELETE SET NULL		ON UPDATE CASCADE,
    FOREIGN KEY(BranchID) REFERENCES BRANCH(BranchID)
		ON DELETE SET NULL		ON UPDATE CASCADE
);
-- -----------------------------------------------------------------
ALTER TABLE BRANCH
ADD CONSTRAINT fk_mgr_ssn
FOREIGN KEY (Mgr_SSN) REFERENCES EMPLOYEE(SSN)
	ON DELETE SET NULL		ON UPDATE CASCADE;

-- Inserting branches with NULL managers
INSERT INTO BRANCH (BranchID, Name, City, Address, Asset, Mgr_SSN)
VALUES 
(1, 'Chase Bank', 'New York', '123 Broadway St', 'Financial', NULL), 
(2, 'TD Bank', 'Jersey City', '456 Market St', 'Financial', NULL),
(3, 'Wells Fargo', 'Newark', '789 Main St', 'Financial', NULL),
(4, 'Bank of America', 'Hoboken', '101 River Rd', 'Financial', NULL),
(5, 'Citibank', 'Manhattan', '202 Park Ave', 'Financial', NULL);

-- -----------------------------------------------------------------
-- Inserting employees with valid BranchID references
INSERT INTO EMPLOYEE (SSN, Name, Password, Telephone, Dependent_name, Start_date, Length_of_emp, Super_SSN, BranchID)
VALUES
('123456789', 'Lakshya Saharan', 'bGFrc2h5YXBhc3M=', '+862-214-9904', 'Jitin Saharan', '2015-03-15', 9, NULL, 1), -- Employee for Chase Bank
('987654321', 'Arjun Reddy', 'YXJqdW5wYXNz', '+915-214-9925', 'Ramesh Reddy', '2017-06-20', 7, NULL, 1), 
('234567890', 'Naga Sathwik', 'bmFnYXBhc3M=', '+432-214-9906', 'Prudhvi', '2018-02-10', 6, NULL, 1), 
('345678901', 'Eren Canan', 'ZXJlbnBhc3M=', '+799-214-9907', 'Scarlett Johansson', '2019-05-25', 5, NULL, 1), 
('456789013', 'Tom Cruise', 'dG9tcGFzcw==', '+851-214-9908', 'Conner Cruise', '2020-08-30', 4, NULL, 1), 
('456789014', 'Elon Musk', 'ZWxvbnBhc3M=', '+862-214-9909', 'Justine Musk', '2020-08-30', 4, NULL, 1), 
('456789012', 'Donald Trump', 'ZG9uYWxkcGFzcw==', '+862-214-9922', 'Ivanka Trump', '2020-08-30', 4, NULL, 1); 

-- -----------------------------------------------------------------
-- Updating branches with managers' SSNs
UPDATE BRANCH 
SET Mgr_SSN = '123456789'  -- Lakshya Saharan is the manager of Citi Bank
WHERE BranchID = 5;

UPDATE BRANCH 
SET Mgr_SSN = '987654321'  -- Arjun Reddy is the manager of TD Bank
WHERE BranchID = 2;

UPDATE BRANCH 
SET Mgr_SSN = '234567890'  -- Naga Sathwik is the manager of Wells Fargo
WHERE BranchID = 3;

UPDATE BRANCH 
SET Mgr_SSN = '345678901'  -- Eren Canan is the manager of Bank of America
WHERE BranchID = 4;

UPDATE BRANCH 
SET Mgr_SSN = '456789012'  -- Donald Trump is the manager of Chase
WHERE BranchID = 1;

-- -----------------------------------------------------------------

-- Lakshya Saharan manages Naga Sathwik, and Arjun Reddy
UPDATE EMPLOYEE
SET Super_SSN = '123456789'  -- Lakshya Saharan's SSN
WHERE SSN IN ('234567890', '987654321');  -- Naga Sathwik and Arjun

-- Eren manages Alpha1 and Alpha2
UPDATE EMPLOYEE
SET Super_SSN = '345678901'  -- Eren
WHERE SSN IN ('456789013', '456789014');  -- Alphas

-- Donald Trump manages Eren Canan
UPDATE EMPLOYEE
SET Super_SSN = '456789012'  -- Donald Trump's SSN
WHERE SSN IN ('345678901', '123456789');  -- Eren Canan's SSN

-- -----------------------------------------------------------------

update employee set Super_SSN='456789012' where SSN='456789012';

-- -----------------------------------------------------------------
CREATE TABLE LOAN(
	Loan_Number		INTEGER		NOT NULL,
    Loan_Amount		INTEGER		NOT NULL,
    Monthly_repay	INTEGER		NOT NULL,
    BranchID        INTEGER,
    PRIMARY KEY(Loan_Number),
    FOREIGN KEY(BranchID) REFERENCES BRANCH(BranchID)
		ON DELETE SET NULL		ON UPDATE CASCADE
);

-- Inserting 5 rows into the LOAN table
INSERT INTO LOAN (Loan_Number, Loan_Amount, Monthly_repay, BranchID)
VALUES
(1001, 50000, 1500, 1), -- Loan for Chase Bank (BranchID = 1)
(1002, 30000, 900, 2),  -- Loan for TD Bank (BranchID = 2)
(1003, 25000, 750, 3),  -- Loan for Wells Fargo (BranchID = 3)
(1004, 100000, 2500, 4), -- Loan for Bank of America (BranchID = 4)
(1005, 15000, 450, 5);  -- Loan for Citibank (BranchID = 5)

-- -----------------------------------------------------------------

CREATE TABLE CUSTOMER(
	SSN					CHAR(9)			NOT NULL,
    Name				VARCHAR(20)		NOT NULL,
    Password 			VARCHAR(200),
    Apt_no				INTEGER,		
    Street 				VARCHAR(20),		
    City				VARCHAR(20),
    State				VARCHAR(20),
    Zip					CHAR(9),
    Personal_banker		CHAR(9),
    Loan_no				INTEGER,
    PRIMARY KEY(SSN),
    FOREIGN KEY(Personal_banker) REFERENCES EMPLOYEE(SSN)
    		ON DELETE SET NULL		ON UPDATE CASCADE,
    FOREIGN KEY(Loan_no) REFERENCES LOAN(Loan_number)
			ON DELETE SET NULL		ON UPDATE CASCADE
); 

-- Inserting 5 rows into the CUSTOMER table
INSERT INTO CUSTOMER (SSN, Name, Password, Apt_no, Street, City, State, Zip, Personal_banker, Loan_no)
VALUES
('111223333', 'John Doe', 'am9obnBhc3M=', 101, 'Maple St', 'New York', 'NY', '10001', '234567890', NULL), -- Personal banker: Lakshya Saharan
('222334444', 'Jane Smith', 'amFuZXBhc3M=', 202, 'Oak St', 'Jersey City', 'NJ', '07305', '987654321', NULL), -- Personal banker: Arjun Reddy
('333445555', 'Tom Brown', 'dG9tcGFzcw==', 303, 'Pine St', 'Newark', 'NJ', '07101', '234567890', NULL), -- Personal banker: Naga Sathwik
('444556666', 'Emma Wilson', 'ZW1tYXBhc3M=', 404, 'Birch St', 'Hoboken', 'NJ', '07030', '987654321', NULL), -- Personal banker: Eren Canan
('555667777', 'Sophia Lee', 'c29waGlhcGFzcw==', 505, 'Cedar St', 'Manhattan', 'NY', '10002', '987654321', NULL); -- Personal banker: Donald Trump

-- -----------------------------------------------------------------

CREATE TABLE ACCOUNT (
    Acc_no            INTEGER      		NOT NULL,
    Acc_type          VARCHAR(20)  		NOT NULL,  
    Balance           DECIMAL(10, 2) 	NOT NULL,
    Recent_Access_Date DATE        		NOT NULL,
    Customer_SSN      CHAR(9),   
    PRIMARY KEY (Acc_no),
    FOREIGN KEY (Customer_SSN) REFERENCES CUSTOMER(SSN)
			ON DELETE SET NULL		ON UPDATE CASCADE
);

CREATE TABLE CHECKING (
    Acc_no            INTEGER      NOT NULL,
    Overdraft         DECIMAL(10, 2) NOT NULL,  -- Overdraft limit for checking accounts
    PRIMARY KEY (Acc_no),
    FOREIGN KEY (Acc_no) REFERENCES ACCOUNT(Acc_no) ON DELETE CASCADE
);

CREATE TABLE SAVING (
    Acc_no            INTEGER      NOT NULL,
    Interest          DECIMAL(5, 2) NOT NULL,  -- Interest rate for saving accounts
    PRIMARY KEY (Acc_no),
    FOREIGN KEY (Acc_no) REFERENCES ACCOUNT(Acc_no) ON DELETE CASCADE
);

CREATE TABLE TRANSACTION (
    Tranc_code      VARCHAR(20)     NOT NULL,  -- Transaction code (Primary Key)
    Tranc_type      VARCHAR(20)  	NOT NULL,  -- Type of transaction (e.g., deposit, withdrawal, transfer)
    Charge          DECIMAL(10, 2),          -- Any transaction charges
    Acc_no          INTEGER      	NOT NULL,  -- Foreign Key referencing ACCOUNT table
    Amount          DECIMAL(10, 2) 	NOT NULL, -- Amount involved in the transaction
    Date            DATE         	NOT NULL,  -- Date of transaction
    Hour            TIME         	NOT NULL,  -- Hour of transaction
    Balance_Snap	DECIMAL(10, 2),
    PRIMARY KEY (Tranc_code),
    FOREIGN KEY (Acc_no) REFERENCES ACCOUNT(Acc_no)
		ON DELETE CASCADE		ON UPDATE CASCADE
);

DELIMITER $$

CREATE PROCEDURE delete_employee(IN employee_ssn INT)
BEGIN
    -- Delete the specified manager
    DELETE FROM EMPLOYEE WHERE SSN = employee_ssn;

    -- Update rows where super_id is NULL to default to 1
    UPDATE EMPLOYEE
    SET Super_SSN = '345678901'
    WHERE Super_SSN IS NULL;
END$$

DELIMITER ;

SET SQL_SAFE_UPDATES = 0;

