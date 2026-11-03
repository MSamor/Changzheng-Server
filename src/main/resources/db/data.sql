-- 默认目标：甘肃180公里；每天5000步兑换6公里，连续30天到达。
-- 按完整单位兑换：500步=0.6公里；每天最多兑换一次，不足一个单位不产生里程。
INSERT OR IGNORE INTO system_configs(config_key,config_value,description,created_at,updated_at) VALUES
('step_unit','500','每个兑换单位500步（每天5000步约30天到甘肃）',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('mileage_unit','0.6','每500步兑换0.6公里，甘肃终点180公里',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('minimum_exchange_steps','500','最低兑换步数500步，每天最多兑换一次',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT OR IGNORE INTO route_nodes(node_order,local_id,city_name,longitude,latitude,required_mileage,badge_code,city_image,city_detail,enabled,created_at,updated_at) VALUES
(1,'0','瑞金',116.032847,25.895306,0,'sign1','../../images/mipmap-hdpi/flag/瑞金.png','中央红军长征出发地，红色历史文化城市。',1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(2,'1','乌江',107.412039,27.319326,20,'sign2','../../images/mipmap-hdpi/flag/乌江.png','乌江是红军长征途中重要的渡江地点。',1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(3,'2','遵义',107.038787,27.729349,40,'sign3','../../images/mipmap-hdpi/flag/遵义.png','遵义会议是中国革命历史上的重要转折点。',1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(4,'3','赤水',106.006030,28.272275,60,'sign4','../../images/mipmap-hdpi/flag/赤水.png','四渡赤水是长征途中著名的经典战例。',1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(5,'4','大渡河',102.290858,29.280346,80,'sign5','../../images/mipmap-hdpi/flag/大渡河.png','红军强渡大渡河，留下了长征历史的壮丽篇章。',1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(6,'5','泸定桥',102.237403,29.918142,100,'sign6','../../images/mipmap-hdpi/flag/泸定桥.png','飞夺泸定桥是红军长征的重要战斗。',1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(7,'6','雪山',102.746436,30.847010,120,'sign7','../../images/mipmap-hdpi/flag/雪山.png','红军翻越雪山，展现了坚定的革命信念。',1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(8,'7','草地',102.647658,33.114584,140,'sign8','../../images/mipmap-hdpi/flag/草地.png','红军穿越草地，克服了极其艰苦的自然环境。',1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(9,'8','陕北',108.181303,36.929781,160,'sign9','../../images/mipmap-hdpi/flag/陕北.png','陕北是中央红军长征胜利会师的重要区域。',1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(10,'9','甘肃',105.058878,35.697055,180,'sign10','../../images/mipmap-hdpi/flag/甘肃.png','长征路线在西北地区留下了不朽的历史印记。',1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
