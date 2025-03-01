CREATE TABLE `user`  (
  `userId` int NOT NULL AUTO_INCREMENT,
  `userName` varchar(20) NOT NULL,
  `userPwd` varchar(30) NOT NULL,
  `permission` varchar(10) NOT NULL,
  PRIMARY KEY (`userId`) USING BTREE
);

