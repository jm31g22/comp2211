#
# -- ----------------------------
# -- Table structure for users
# -- ----------------------------
# DROP TABLE IF EXISTS `users`;
# CREATE TABLE `users`  (
#   `id` int NOT NULL,
#   `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
#   `password` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
#   `role` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL, --admin, editor, viewer
#   PRIMARY KEY (`id`) USING BTREE
# ) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;
#
# -- ----------------------------
# -- Records of users
# -- ----------------------------
# INSERT INTO `users` VALUES (1, 'admin', 'admin', 'admin');
# INSERT INTO `users` VALUES (2, 'editor', 'editor', 'editor');
#
# SET FOREIGN_KEY_CHECKS = 1;
