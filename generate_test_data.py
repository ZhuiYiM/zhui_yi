import pymysql
import json

# 连接数据库
conn = pymysql.connect(
    host='localhost',
    user='root',
    password='123456',
    database='campus_db',
    charset='utf8mb4'
)

cursor = conn.cursor()

# 商品数据
products = [
    # 分类 1: 二手物品
    (1, 'MacBook Pro 2020', 'i5 处理器 16G 内存 512G SSD 9 成新', 4500.00, 8000.00, 1, '微信 zhang123', '校内', 1, 235, 18),
    (2, 'iPhone 12 二手', '国行在保 128G 黑色 9 成新', 2800.00, 4500.00, 1, 'QQ 2345678', '校内', 1, 189, 15),
    
    # 分类 2: 服务需求
    (4, '电脑清灰换硅脂', '专业工具细心操作', 30.00, None, 2, '微信 fixpc', '计算机楼', 1, 145, 11),
    (5, 'PPT 制作美化', '经验丰富审美在线', 50.00, None, 2, 'QQ 5678901', '线上', 1, 198, 16),
    
    # 分类 3: 兼职信息
    (6, '周末家教兼职', '初中数理化辅导有经验', 100.00, None, 3, '电话 13700137000', '校外', 1, 423, 35),
    (7, '图书馆整理员', '工作时间灵活环境好', 15.00, None, 3, '图书馆办公室', '图书馆', 1, 389, 28),
    
    # 分类 4: 拼车出行
    (9, '拼车去火车站', '周五下午出发 2 个空位', 30.00, None, 4, '微信 pinche', '校门口', 1, 201, 16),
    (10, '清明节回家拼车', '4 月 3 日上午出发', 50.00, None, 4, '电话 13600136000', '校门口', 1, 167, 13),
    
    # 分类 5: 美食外卖
    (10, '自制手工饼干', '纯手工制作无添加', 25.00, None, 5, '微信 cookie', '食堂', 1, 289, 22),
    (11, '家乡特产腊肠', '正宗四川风味麻辣', 35.00, None, 5, 'QQ 4567890', '宿舍区', 1, 234, 19),
    
    # 分类 6: 书籍教材
    (1, '高等数学教材', '同济第七版有笔记', 25.00, 60.00, 6, '电话 13800138000', '图书馆', 1, 156, 8),
    (2, '考研英语词汇', '新东方红宝书 9 成新', 15.00, 45.00, 6, '微信 li789', '教学楼', 1, 98, 5),
    
    # 分类 7: 电子设备
    (4, '罗技 MX Master3', '用了半年功能正常', 350.00, 699.00, 7, '微信 wang456', '校内', 1, 145, 12),
    (5, '机械键盘 Cherry', 'Cherry 原厂轴体 RGB', 280.00, 499.00, 7, '电话 13900139001', '宿舍区', 1, 178, 14),
    (6, 'AirPods Pro 二代', '苹果原装主动降噪', 1200.00, 1899.00, 7, '微信 apple888', '校内', 1, 312, 25),
    
    # 分类 8: 生活用品
    (7, '小米台灯', '全新未拆封智能控制', 45.00, 79.00, 8, 'QQ 3456789', '宿舍区', 1, 98, 5),
    (8, '床上四件套', '纯棉材质几乎没用', 80.00, 200.00, 8, '微信 zhao123', '女生宿舍', 1, 67, 3),
    
    # 分类 9: 代取快递
    (10, '代取快递', '快速便捷送货上门', 5.00, None, 9, '电话 13900139000', '全校', 1, 312, 25),
    (11, '快递代取当天送达', '中午下午各送一次', 3.00, None, 9, '微信 kuaidai001', '全校', 1, 256, 20),
    
    # 分类 10: 跑腿服务
    (1, '校园跑腿', '代买饭代排队', 10.00, None, 10, '微信 paotui', '全校', 1, 267, 20),
    (2, '代打印代复印', '价格便宜送货上门', 0.50, None, 10, 'QQ 6789012', '图书馆', 1, 145, 11),
    
    # 分类 11: 其他
    (3, '多肉植物盆栽', '可爱多肉带盆', 15.00, None, 11, '微信 plant', '花坛边', 1, 123, 10),
    (4, '吉他初学者转让', '全新民谣送琴包', 300.00, 599.00, 11, '电话 13700137001', '艺术学院', 1, 189, 15),
]

# 插入商品
try:
    for p in products:
        images = json.dumps([f'https://placehold.co/300x200/{abs(hash(p[1])) % 16777216:06X}/FFFFFF?text={p[1][:8]}'])
        trade_methods = json.dumps(['face-to-face'])
        
        cursor.execute("""
            INSERT INTO products 
            (seller_id, title, description, price, original_price, category_id, images, contact_info, 
             trade_methods, location, status, view_count, like_count, favorite_count, is_hot)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """, (p[0], p[1], p[2], p[3], p[4], p[5], images, p[6], trade_methods, p[7], p[8], p[9], p[10], 0, 1))
    
    print(f'成功插入 {len(products)} 个商品')
except Exception as e:
    print(f'插入商品失败：{e}')
    conn.rollback()
    cursor.close()
    conn.close()
    exit(1)

# 已售出和下架商品
cursor.execute("""
    INSERT INTO products 
    (seller_id, title, description, price, original_price, category_id, images, contact_info, 
     trade_methods, location, status, view_count, like_count, favorite_count, is_hot)
    VALUES 
    (5, 'Switch 游戏机已售出', '国行续航版送游戏', 1800.00, 2500.00, 7, 
     '["https://placehold.co/300x200/E6E6FA/FFFFFF?text=Switch"]', '微信 game', 
     '["face-to-face"]', '校内', 2, 567, 45, 12, 1),
    (6, '考研资料已下架', '版权原因下架', 50.00, 100.00, 6, 
     '["https://placehold.co/300x200/CCCCCC/FFFFFF?text=Book"]', 'QQ 7890123', 
     '["face-to-face"]', '教学楼', 0, 123, 10, 2, 0)
""")

# 收藏数据
favorites = [(i, j) for i, j in zip(range(1, 12), [1, 3, 6, 2, 9, 1, 4, 5, 8, 6, 10])]
for user_id, product_id in favorites:
    cursor.execute("INSERT INTO product_favorites (user_id, product_id) VALUES (%s, %s)", (user_id, product_id))

# 浏览记录
views = [(i, j, 'desktop' if i % 2 == 0 else 'mobile') for i, j in zip(range(1, 11), range(1, 11))]
for user_id, product_id, device in views:
    cursor.execute("INSERT INTO product_views (user_id, product_id, device_type) VALUES (%s, %s, %s)", 
                   (user_id, product_id, device))

# 更新统计数据
cursor.execute("""
    UPDATE products SET 
        favorite_count = (SELECT COUNT(*) FROM product_favorites WHERE product_id = products.id),
        view_count = view_count + (SELECT COUNT(*) FROM product_views WHERE product_id = products.id)
""")

conn.commit()
print(f'成功插入 {len(products)} 个商品，2 个特殊状态商品，{len(favorites)} 条收藏记录，{len(views)} 条浏览记录')

cursor.close()
conn.close()
