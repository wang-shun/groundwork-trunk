DROP DATABASE IF EXISTS GWEntitlementDB;
CREATE DATABASE GWEntitlementDB;
USE GWEntitlementDB;
DROP TABLE if exists Customer;
DROP TABLE if exists OrderInfo;
DROP TABLE if exists LicenseKey;
CREATE TABLE Customer
    (
        CustomerID INTEGER NOT NULL AUTO_INCREMENT,
        FirstName VARCHAR (254) ,
        LastName VARCHAR (254) NOT NULL,
        Company VARCHAR (254),
        PRIMARY KEY(CustomerID)
    ) ENGINE=InnoDB;
ALTER TABLE Customer AUTO_INCREMENT=1001;
    CREATE TABLE OrderInfo
    (
        OrderInfoID VARCHAR(100) NOT NULL,
        CustomerID INTEGER NOT NULL,
        OrderDate DATETIME,
        StartDate DATETIME,
        ExpiryDate DATETIME,
	HardLimitExpiryDate DATETIME,
        SKU VARCHAR(512),
        SoftLimitDevice SMALLINT,
        HardLimitDevice SMALLINT,
        ProductVersion VARCHAR(100),
        ProductName VARCHAR(256),
        NetworkServiceRequired BOOLEAN DEFAULT 1,
        BitRockInstallID VARCHAR (100),
        ModifiedDate DATETIME,
        PRIMARY KEY(OrderInfoID),
        FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID)
    ) ENGINE=InnoDB;

    CREATE TABLE LicenseKey
    (
        LicenseKeyID INTEGER NOT NULL AUTO_INCREMENT,
        OrderInfoID VARCHAR(100) NOT NULL,
        License TEXT  NOT NULL,
        CreationDate DATETIME,
        PRIMARY KEY(LicenseKeyID),
        FOREIGN KEY (OrderInfoID) REFERENCES OrderInfo(OrderInfoID)
    ) ENGINE=InnoDB;
ALTER TABLE LicenseKey AUTO_INCREMENT=6001;
ALTER TABLE LicenseKey ADD COLUMN Comment varchar(100);