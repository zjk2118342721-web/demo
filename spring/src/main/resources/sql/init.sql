-- 味食香微信点餐小程序 数据库初始化脚本
-- MySQL 8.0+
CREATE DATABASE IF NOT EXISTS weishixiang DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE weishixiang;

DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart;
DROP TABLE IF EXISTS dish;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS dining_table;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS admin;

-- 管理员
CREATE TABLE admin (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录账号',
    password    VARCHAR(100) NOT NULL COMMENT 'SHA-256 密码',
    name        VARCHAR(50)  DEFAULT '管理员' COMMENT '姓名',
    avatar      VARCHAR(255) DEFAULT '',
    role        VARCHAR(20)  DEFAULT 'admin',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='管理员表';

-- 顾客
CREATE TABLE user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    openid      VARCHAR(64)  NOT NULL UNIQUE COMMENT '微信 openid',
    nickname    VARCHAR(50)  DEFAULT '微信用户',
    avatar      VARCHAR(500) DEFAULT '',
    phone       VARCHAR(20)  DEFAULT '',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    KEY idx_openid (openid)
) ENGINE=InnoDB COMMENT='顾客表';

-- 菜品分类
CREATE TABLE category (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL COMMENT '分类名称',
    sort        INT         DEFAULT 0 COMMENT '排序，越小越靠前',
    status      TINYINT     DEFAULT 1 COMMENT '1启用 0停用',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='菜品分类表';

-- 菜品
CREATE TABLE dish (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id    BIGINT        NOT NULL COMMENT '分类ID',
    name           VARCHAR(80)   NOT NULL COMMENT '菜品名称',
    description    VARCHAR(500)  DEFAULT '' COMMENT '菜品描述',
    image          VARCHAR(500)  DEFAULT '' COMMENT '图片地址',
    price          DECIMAL(10,2) NOT NULL COMMENT '售价',
    original_price DECIMAL(10,2) DEFAULT 0 COMMENT '原价',
    sales          INT           DEFAULT 0 COMMENT '销量',
    status         TINYINT       DEFAULT 1 COMMENT '1上架 0下架',
    sort           INT           DEFAULT 0,
    create_time    DATETIME      DEFAULT CURRENT_TIMESTAMP,
    KEY idx_category (category_id)
) ENGINE=InnoDB COMMENT='菜品表';

-- 购物车
CREATE TABLE cart (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT   NOT NULL,
    dish_id     BIGINT   NOT NULL,
    quantity    INT      DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_dish (user_id, dish_id)
) ENGINE=InnoDB COMMENT='购物车表';

-- 订单
CREATE TABLE orders (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no      VARCHAR(40)   NOT NULL UNIQUE COMMENT '订单号',
    user_id       BIGINT        NOT NULL,
    table_no      VARCHAR(20)   DEFAULT '' COMMENT '桌号',
    total_amount  DECIMAL(10,2) NOT NULL DEFAULT 0,
    status        TINYINT       DEFAULT 0 COMMENT '0待付款 1待接单 2制作中 3待上菜 4已完成 5已取消',
    remark        VARCHAR(255)  DEFAULT '',
    pay_time      DATETIME      NULL,
    complete_time DATETIME      NULL,
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user (user_id),
    KEY idx_status (status),
    KEY idx_create (create_time)
) ENGINE=InnoDB COMMENT='订单表';

-- 订单明细
CREATE TABLE order_item (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT        NOT NULL,
    dish_id    BIGINT        NOT NULL,
    dish_name  VARCHAR(80)   NOT NULL,
    dish_image VARCHAR(500)  DEFAULT '',
    price      DECIMAL(10,2) NOT NULL,
    quantity   INT           NOT NULL DEFAULT 1,
    KEY idx_order (order_id)
) ENGINE=InnoDB COMMENT='订单明细表';

-- 菜品评价
CREATE TABLE review (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id    BIGINT       NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL,
    rating      TINYINT      DEFAULT 5 COMMENT '1-5星',
    content     VARCHAR(500) DEFAULT '',
    images      VARCHAR(1000) DEFAULT '' COMMENT '图片地址，逗号分隔',
    anonymous   TINYINT      DEFAULT 0 COMMENT '1匿名',
    reply       VARCHAR(500) DEFAULT '' COMMENT '商家回复',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user (user_id)
) ENGINE=InnoDB COMMENT='菜品评价表';

-- 餐桌
CREATE TABLE dining_table (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_no    VARCHAR(20) NOT NULL UNIQUE COMMENT '桌号',
    seats       INT         DEFAULT 4 COMMENT '座位数',
    status      TINYINT     DEFAULT 0 COMMENT '0空闲 1使用中',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='餐桌表';

-- ============ 初始数据 ============
-- 管理员密码：123456（SHA-256）
INSERT INTO admin (username, password, name, role) VALUES
('admin', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '味食香店长', 'admin');

-- 演示顾客
INSERT INTO user (openid, nickname, avatar, phone) VALUES
('mock_demo', '美食家小食', 'https://trae-api-cn.mchost.guru/api/ide/v1/tool_text_to_image?prompt=close-up%20portrait%20of%20a%20friendly%20young%20chinese%20woman%20smiling%2C%20soft%20studio%20lighting%2C%20clean%20background&image_size=square', '13800138000'),
('mock_002', '张小明', '', '13900139000'),
('mock_003', '李小红', '', '13700137000');

-- 分类
INSERT INTO category (id, name, sort, status) VALUES
(1, '招牌热菜', 1, 1),
(2, '精致凉菜', 2, 1),
(3, '主食面点', 3, 1),
(4, '汤羹', 4, 1),
(5, '饮品', 5, 1),
(6, '甜品', 6, 1);

-- 菜品
INSERT INTO dish (id, category_id, name, description, image, price, original_price, sales, status, sort) VALUES
(1, 1, '香辣水煮鱼', '现杀鲜鱼片，麻辣鲜香，配豆芽与宽粉，红油汤底回味悠长。', '/uploads/dishes/dish1.jpg', 68.00, 78.00, 326, 1, 1),
(2, 1, '农家小炒肉', '土猪前腿肉配青尖椒大火爆炒，锅气十足，下饭神器。', '/uploads/dishes/dish2.jpg', 42.00, 48.00, 288, 1, 2),
(3, 1, '宫保鸡丁', '鸡腿肉丁配花生米、干辣椒，酸甜微辣，经典川味。', '/uploads/dishes/dish3.jpg', 38.00, 45.00, 245, 1, 3),
(4, 1, '外婆红烧肉', '五花肉慢炖两小时，肥而不腻，酱汁浓郁。', '/uploads/dishes/dish4.jpg', 48.00, 58.00, 198, 1, 4),
(5, 2, '川味口水鸡', '散养土鸡冰镇后淋红油，皮爽肉嫩，麻辣鲜香。', '/uploads/dishes/dish5.jpg', 32.00, 38.00, 176, 1, 1),
(6, 2, '凉拌黑木耳', '东北黑木耳配小米辣与香菜，爽脆开胃。', '/uploads/dishes/dish6.jpg', 18.00, 22.00, 154, 1, 2),
(7, 2, '蒜泥拍黄瓜', '现拍黄瓜锁住清甜，蒜香浓郁，解腻首选。', '/uploads/dishes/dish7.jpg', 12.00, 15.00, 231, 1, 3),
(8, 3, '黄金蛋炒饭', '隔夜米粒粒分明，土鸡蛋与青豆火腿同炒。', '/uploads/dishes/dish8.jpg', 22.00, 26.00, 267, 1, 1),
(9, 3, '招牌牛肉面', '牛骨高汤熬制八小时，手工面配大块牛腩。', '/uploads/dishes/dish9.jpg', 26.00, 30.00, 312, 1, 2),
(10, 3, '葱油拌面', '小葱慢熬葱油，拌入劲道细面，香气扑鼻。', '/uploads/dishes/dish10.jpg', 18.00, 20.00, 189, 1, 3),
(11, 3, '手工鲜肉水饺', '现包现煮，猪肉大葱馅，皮薄馅大。', '/uploads/dishes/dish11.jpg', 24.00, 28.00, 205, 1, 4),
(12, 4, '酸辣开胃汤', '胡椒与香醋调和，豆腐笋丝木耳，酸辣暖胃。', '/uploads/dishes/dish12.jpg', 16.00, 18.00, 143, 1, 1),
(13, 4, '番茄蛋花汤', '熟成番茄熬汤，蛋花细嫩，酸甜清爽。', '/uploads/dishes/dish13.jpg', 14.00, 16.00, 167, 1, 2),
(14, 5, '古法酸梅汤', '乌梅山楂陈皮慢煮，冰镇后酸甜解辣。', '/uploads/dishes/dish14.jpg', 10.00, 12.00, 356, 1, 1),
(15, 5, '鲜榨橙汁', '当日鲜橙现榨，无添加，果香浓郁。', '/uploads/dishes/dish15.jpg', 18.00, 20.00, 221, 1, 2),
(16, 5, '手打柠檬茶', '鸭屎香茶底配手打香水柠檬，清新回甘。', '/uploads/dishes/dish16.jpg', 12.00, 15.00, 298, 1, 3),
(17, 6, '红糖糍粑', '糯米糍粑外酥里糯，淋古法红糖浆与黄豆粉。', '/uploads/dishes/dish17.jpg', 16.00, 18.00, 174, 1, 1),
(18, 6, '桂花酒酿圆子', '小圆子配酒酿与干桂花，温润香甜。', '/uploads/dishes/dish18.jpg', 14.00, 16.00, 132, 1, 2);

-- 餐桌
INSERT INTO dining_table (table_no, seats, status) VALUES
('A01', 2, 0), ('A02', 2, 0), ('A03', 4, 0), ('A04', 4, 0),
('A05', 4, 1), ('A06', 6, 0), ('A07', 6, 0), ('A08', 8, 0),
('B01', 2, 0), ('B02', 4, 0), ('B03', 6, 0), ('B04', 8, 0);

-- 演示订单（用于后台统计与订单列表展示）
INSERT INTO orders (id, order_no, user_id, table_no, total_amount, status, remark, pay_time, complete_time, create_time) VALUES
(1, 'WSX202611060001', 1, 'A03', 90.00, 1, '少辣，米饭多一份', DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, 'WSX202611060002', 2, 'A05', 100.00, 2, '不要香菜', DATE_SUB(NOW(), INTERVAL 90 MINUTE), NULL, DATE_SUB(NOW(), INTERVAL 90 MINUTE)),
(3, 'WSX202611060003', 3, 'A08', 64.00, 3, '', DATE_SUB(NOW(), INTERVAL 40 MINUTE), NULL, DATE_SUB(NOW(), INTERVAL 40 MINUTE)),
(4, 'WSX202611060004', 1, 'A02', 90.00, 4, '鱼片多一点', DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(5, 'WSX202611050001', 2, 'B02', 52.00, 4, '', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(6, 'WSX202611040001', 3, 'A06', 74.00, 4, '', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(7, 'WSX202611030001', 1, 'B01', 40.00, 5, '临时有事', NULL, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(8, 'WSX202611020001', 1, 'A04', 102.00, 4, '朋友聚餐', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(9, 'WSX202611010001', 2, 'A07', 66.00, 4, '', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(10, 'WSX202610310001', 3, 'B03', 56.00, 4, '', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
(11, 'WSX202611060005', 1, 'B04', 80.00, 0, '不要葱花香菜', NULL, NULL, DATE_SUB(NOW(), INTERVAL 12 MINUTE)),
(12, 'WSX202611060006', 1, 'A06', 74.00, 3, '打包一份米饭', DATE_SUB(NOW(), INTERVAL 25 MINUTE), NULL, DATE_SUB(NOW(), INTERVAL 25 MINUTE));

-- 订单明细
INSERT INTO order_item (order_id, dish_id, dish_name, dish_image, price, quantity) VALUES
(1, 2, '农家小炒肉', '/uploads/dishes/dish2.jpg', 42.00, 1),
(1, 3, '宫保鸡丁', '/uploads/dishes/dish3.jpg', 38.00, 1),
(1, 14, '古法酸梅汤', '/uploads/dishes/dish14.jpg', 10.00, 1),
(2, 1, '香辣水煮鱼', '/uploads/dishes/dish1.jpg', 68.00, 1),
(2, 5, '川味口水鸡', '/uploads/dishes/dish5.jpg', 32.00, 1),
(3, 9, '招牌牛肉面', '/uploads/dishes/dish9.jpg', 26.00, 1),
(3, 3, '宫保鸡丁', '/uploads/dishes/dish3.jpg', 38.00, 1),
(4, 1, '香辣水煮鱼', '/uploads/dishes/dish1.jpg', 68.00, 1),
(4, 8, '黄金蛋炒饭', '/uploads/dishes/dish8.jpg', 22.00, 1),
(5, 2, '农家小炒肉', '/uploads/dishes/dish2.jpg', 42.00, 1),
(5, 14, '古法酸梅汤', '/uploads/dishes/dish14.jpg', 10.00, 1),
(6, 4, '外婆红烧肉', '/uploads/dishes/dish4.jpg', 48.00, 1),
(6, 9, '招牌牛肉面', '/uploads/dishes/dish9.jpg', 26.00, 1),
(7, 8, '黄金蛋炒饭', '/uploads/dishes/dish8.jpg', 22.00, 1),
(7, 6, '凉拌黑木耳', '/uploads/dishes/dish6.jpg', 18.00, 1),
(8, 1, '香辣水煮鱼', '/uploads/dishes/dish1.jpg', 68.00, 1),
(8, 12, '酸辣开胃汤', '/uploads/dishes/dish12.jpg', 16.00, 1),
(8, 15, '鲜榨橙汁', '/uploads/dishes/dish15.jpg', 18.00, 1),
(9, 2, '农家小炒肉', '/uploads/dishes/dish2.jpg', 42.00, 1),
(9, 11, '手工鲜肉水饺', '/uploads/dishes/dish11.jpg', 24.00, 1),
(10, 5, '川味口水鸡', '/uploads/dishes/dish5.jpg', 32.00, 1),
(10, 11, '手工鲜肉水饺', '/uploads/dishes/dish11.jpg', 24.00, 1),
(11, 1, '香辣水煮鱼', '/uploads/dishes/dish1.jpg', 68.00, 1),
(11, 16, '手打柠檬茶', '/uploads/dishes/dish16.jpg', 12.00, 1),
(12, 2, '农家小炒肉', '/uploads/dishes/dish2.jpg', 42.00, 1),
(12, 8, '黄金蛋炒饭', '/uploads/dishes/dish8.jpg', 22.00, 1),
(12, 14, '古法酸梅汤', '/uploads/dishes/dish14.jpg', 10.00, 1);

-- 演示评价
INSERT INTO review (order_id, user_id, rating, content, images, anonymous, reply, create_time) VALUES
(4, 1, 5, '水煮鱼分量很足，鱼片滑嫩，辣度刚刚好！上菜也快，服务员态度很好。', '', 0, '感谢您的认可，期待再次光临味食香～', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(5, 2, 4, '小炒肉很下饭，就是稍微有点咸，酸梅汤很解腻。', '', 0, '已反馈给后厨调整口味，感谢您的建议！', DATE_SUB(NOW(), INTERVAL 20 HOUR)),
(6, 3, 5, '红烧肉入口即化，环境干净，适合家庭聚餐。', '', 0, '', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(8, 1, 4, '菜品整体不错，水煮鱼和酸梅汤必点，上菜稍慢。', '', 1, '', DATE_SUB(NOW(), INTERVAL 4 DAY));
