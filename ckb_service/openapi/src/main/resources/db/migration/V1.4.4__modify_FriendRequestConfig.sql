ALTER TABLE cunkebao.`FriendRequestConfig` ADD sUUId VARCHAR (255) AFTER sId;
ALTER TABLE cunkebao.`FriendMessageTask` ADD sUUId VARCHAR (255) AFTER sName;

ALTER TABLE cunkebao.`FriendMessageTask` ADD INDEX `idx_uuid` (`sUUId`);