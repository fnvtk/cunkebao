-- 删除现有的唯一复合索引
ALTER TABLE `WeChatFriendLabel` DROP INDEX `FriendRequestTaskDetailId`;

-- 添加一个新的非唯一复合索引
ALTER TABLE `WeChatFriendLabel` ADD INDEX `idx_FriendRequestTaskDetailId_LabelId` (`FriendRequestTaskDetailId`, `LabelId`);

UPDATE `cunkebao`.`EventTrigger` SET `sName` = '申请已通过为好友', `jEventHandler` = '[\r\n    {\r\n        \"id\": \"handleAddFriendReqPass\",\r\n        \"name\": \"处理成为好友\"\r\n    },\r\n    {\r\n        \"id\": \"UpdateFriendRequestTaskStatus\",\r\n        \"name\": \"更新加友计划状态\"\r\n    },\r\n    {\r\n        \"id\": \"MarkFriendLabel\",\r\n        \"name\": \"打上好友标签\"\r\n    }\r\n]' WHERE `sId` = 'friendreqtask-addfriend-req-pass';