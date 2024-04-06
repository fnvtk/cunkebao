DROP TABLE IF EXISTS cunkebao.`UserIncome`;
CREATE TABLE cunkebao.`UserIncome` (
                              `lId` int(11) NOT NULL AUTO_INCREMENT,
                              `sName` varchar(255) NOT NULL,
                              `dNewTime` datetime(6) NOT NULL,
                              `NewUserId` int(11) NOT NULL,
                              `fIncome` decimal(10,2) NOT NULL,
                              `sObjectName` varchar(255) NOT NULL,
                              `ObjectId` int(11) NOT NULL,
                              PRIMARY KEY (`lId`),
                              KEY `NewUserId` (`NewUserId`,`dNewTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE cunkebao.`FriendRequestTaskDetail` ADD NewUserId int (11) AFTER sName;
ALTER TABLE cunkebao.`FriendRequestTask` ADD IncomeTypeId int (11) DEFAULT 0 AFTER PosterId;
ALTER TABLE cunkebao.`FriendRequestTask` ADD fIncome decimal(10,2) DEFAULT 0.00  AFTER IncomeTypeId;