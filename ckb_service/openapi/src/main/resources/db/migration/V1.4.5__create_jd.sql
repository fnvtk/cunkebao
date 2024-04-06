DROP TABLE IF EXISTS `JdPromotionSite`;
DROP TABLE IF EXISTS `JdSocialMedia`;

CREATE TABLE `JdPromotionSite` (
                                   `lId` bigint(11) NOT NULL,
                                   `sName` varchar(255) DEFAULT NULL,
                                   `JdSocialMediaId` bigint(11) DEFAULT NULL,
                                   `dNewTime` datetime DEFAULT NULL,
                                   PRIMARY KEY (`lId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE `JdSocialMedia` (
                                 `lId` bigint(11) NOT NULL,
                                 `sName` varchar(255) DEFAULT NULL,
                                 PRIMARY KEY (`lId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;