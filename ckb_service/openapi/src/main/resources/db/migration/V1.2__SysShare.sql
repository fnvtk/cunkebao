drop table if exists `SysShare`;
CREATE TABLE `SysShare` (
                            `sObjectName` varchar(255) DEFAULT NULL,
                            `ObjectId` int(11) DEFAULT NULL,
                            `ToUserId` int(255) DEFAULT NULL,
                            KEY `sObjectName` (`sObjectName`,`ObjectId`),
                            KEY `sObjectName_2` (`sObjectName`,`ToUserId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;