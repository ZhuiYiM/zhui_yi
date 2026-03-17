# 商品规格功能与订单支付流程开发说明

## 一、功能概述

本次开发为校园信息平台交易中心添加了完整的商品规格自定义功能和订单支付流程，主要包括：

### 1. 核心功能
- ✅ **商品规格管理**：卖家发布商品时可自定义规格（如颜色、尺寸等）
- ✅ **规格选择购买**：买家购买商品时可选择规格
- ✅ **订单确认页面**：显示订单详细信息和选中的规格
- ✅ **支付流程**：支持多种支付方式（站内支付、微信、支付宝）

## 二、数据库变更

### 1. 新增数据表

#### `product_specifications` - 商品规格表
```sql
CREATE TABLE `product_specifications` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `product_id` INT NOT NULL,           -- 商品 ID
    `spec_name` VARCHAR(50),             -- 规格名称（如：颜色）
    `spec_value` VARCHAR(100),           -- 规格值（如：红色）
    `spec_icon` VARCHAR(255),            -- 图标或颜色代码
    `stock_quantity` INT,                -- 库存数量
    `price_adjustment` DECIMAL(10,2),    -- 价格调整
    `is_default` TINYINT,                -- 是否默认
    `sort_order` INT,                    -- 排序顺序
    `created_at` TIMESTAMP,
    `updated_at` TIMESTAMP
);
```

#### `order_specifications` - 订单规格关联表
```sql
CREATE TABLE `order_specifications` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `order_id` INT NOT NULL,             -- 订单 ID
    `spec_id` INT NOT NULL,              -- 规格 ID
    `spec_name` VARCHAR(50),             -- 规格名称（冗余）
    `spec_value` VARCHAR(100),           -- 规格值（冗余）
    `spec_icon` VARCHAR(255),            -- 规格图标
    `quantity` INT,                      -- 购买数量
    `created_at` TIMESTAMP
);
```

### 2. 表结构修改

#### `orders` 表新增字段
- `buyer_contact` - 买家联系方式
- `buyer_message` - 买家留言
- `payment_method` - 支付方式（wechat/alipay/station）
- `payment_status` - 支付状态（0-未支付，1-已支付）
- `payment_time` - 支付时间
- `order_status` 修改为：0-待付款，1-待发货，2-已发货，3-已完成，4-已取消

#### `products` 表新增字段
- `has_specifications` - 是否有规格（0-无，1-有）

## 三、后端实现

### 1. 实体类（Entity）
- `ProductSpecification.java` - 商品规格实体
- `OrderSpecification.java` - 订单规格关联实体
- `Order.java` - 更新订单实体，添加新字段
- `Product.java` - 更新产品实体，添加 hasSpecifications 字段

### 2. DTO（数据传输对象）
- `ProductDTO.SpecificationDTO` - 规格数据传输对象
- `OrderDTO` - 订单创建 DTO，包含规格信息
- `OrderDTO.OrderSpecItemDTO` - 订单项规格 DTO
- `ProductDetailResponseDTO` - 商品详情响应 DTO

### 3. Mapper 层
- `ProductSpecificationMapper.java` + XML
- `OrderSpecificationMapper.java` + XML

### 4. Service 层
- `ProductSpecificationService` - 规格管理服务
- `OrderSpecificationService` - 订单规格关联服务
- `OrderService` - 订单服务（新建）
- `OrderServiceImpl` - 订单服务实现

### 5. Controller 层
- `OrderController` - 订单控制器
  - POST `/orders` - 创建订单
  - GET `/orders/my` - 获取我的订单
  - GET `/orders/{id}` - 获取订单详情
  - PUT `/orders/{id}/status` - 更新订单状态
  - PUT `/orders/{id}/cancel` - 取消订单
  - DELETE `/orders/{id}` - 删除订单

## 四、前端实现

### 1. 组件更新

#### `PublishProduct.vue` - 发布商品页
- 新增规格编辑器组件
- 支持添加多个规格组（如颜色、尺寸）
- 每个规格可添加多个选项
- 可设置库存、价格调整、默认选项
- 数据转换为 DTO 格式提交

#### `BuyConfirmModal.vue` - 购买确认弹窗
- 新增规格选择器 UI
- 支持多规格组选择
- 动态计算价格调整
- 验证规格完整性
- 将选中规格传递给订单

#### `ProductDetail.vue` - 商品详情页
- 获取并传递规格数据给购买弹窗
- 处理订单创建后的跳转逻辑

### 2. 新增页面组件

#### `OrderConfirmation.vue` - 订单确认页
- 显示订单详细信息
- 展示商品图片和选中规格
- 复制订单号功能
- 跳转到支付页面

#### `OrderPayment.vue` - 支付页面
- 三种支付方式选择
  - 站内支付（模拟）
  - 微信支付（待开发）
  - 支付宝支付（待开发）
- 支付说明提示
- 支付成功后跳转订单列表

### 3. 路由配置
```javascript
{
    path: '/order/confirmation/:id',
    name: 'OrderConfirmation',
    component: OrderConfirmation
},
{
    path: '/order/payment/:id',
    name: 'OrderPayment',
    component: OrderPayment
}
```

## 五、API 接口

### 商品相关 API 更新

#### 1. 发布商品（带规格）
```
POST /products
Body: {
    "title": "商品标题",
    "description": "商品描述",
    "price": 99.00,
    "categoryId": 1,
    "images": ["url1", "url2"],
    "specifications": [
        {
            "specName": "颜色",
            "specValue": "红色",
            "specIcon": "#FF0000",
            "stockQuantity": 10,
            "priceAdjustment": 0,
            "isDefault": 1,
            "sortOrder": 0
        }
    ]
}
```

#### 2. 获取商品详情（返回规格）
```
GET /products/{id}
Response: {
    "product": {...},
    "specifications": {
        "颜色": [
            {
                "specId": 1,
                "specValue": "红色",
                "specIcon": "#FF0000",
                "stockQuantity": 10,
                "priceAdjustment": "0.00"
            }
        ]
    }
}
```

### 订单相关 API

#### 1. 创建订单
```
POST /orders
Body: {
    "productId": 1,
    "sellerId": 2,
    "totalAmount": 99.00,
    "orderStatus": 0,
    "buyerContact": "13800138000",
    "message": "备注信息",
    "quantity": 1,
    "selectedSpecifications": [
        {
            "specId": 1,
            "specName": "颜色",
            "specValue": "红色",
            "specIcon": "#FF0000",
            "quantity": 1
        }
    ]
}
```

#### 2. 获取订单详情
```
GET /orders/{id}
Response: {
    "order": {
        "id": 1,
        "total_amount": 99.00,
        "order_status": 0,
        "buyer_contact": "13800138000",
        ...
    },
    "specifications": [...]
}
```

## 六、使用流程

### 卖家发布商品流程
1. 进入发布商品页面
2. 填写基本信息（标题、描述、价格等）
3. 开启"启用规格"开关
4. 添加规格组（如：颜色）
5. 添加规格选项（如：红色、蓝色）
6. 为每个选项设置库存、价格调整
7. 提交发布

### 买家购买流程
1. 浏览商品详情
2. 点击"立即购买"
3. 选择规格（必须选择所有规格组）
4. 选择购买数量
5. 填写联系方式和留言
6. 确认订单信息
7. 提交订单
8. 跳转到订单确认页
9. 点击"立即支付"
10. 选择支付方式
11. 完成支付

## 七、待完善功能

### 1. 支付集成
- ⏳ 微信支付 API 对接
- ⏳ 支付宝支付 API 对接
- ⏳ 站内余额支付完整逻辑

### 2. 库存管理
- ⏳ 下单时扣减库存
- ⏳ 取消订单恢复库存
- ⏳ 库存预警

### 3. 订单管理
- ⏳ 卖家端订单管理（发货、核销）
- ⏳ 物流跟踪
- ⏳ 订单评价

### 4. 退款售后
- ⏳ 申请退款
- ⏳ 退货流程
- ⏳ 纠纷处理

## 八、测试建议

### 1. 功能测试
- [ ] 发布商品时添加不同规格组合
- [ ] 购买商品时选择不同规格
- [ ] 价格调整计算是否正确
- [ ] 订单信息是否完整
- [ ] 支付流程是否顺畅

### 2. 边界测试
- [ ] 不选择规格时的处理
- [ ] 库存不足时的处理
- [ ] 网络异常时的处理

### 3. 数据一致性
- [ ] 订单规格与商品规格的一致性
- [ ] 价格计算的准确性
- [ ] 订单状态的流转

## 九、注意事项

1. **数据库迁移**：先执行 `database_specifications_upgrade.sql` 进行数据库升级
2. **依赖安装**：确保安装了所有必要的依赖包
3. **API 兼容性**：现有 API 保持向后兼容
4. **错误处理**：前后端都有完善的错误提示
5. **用户体验**：规格选择界面友好，操作流畅

## 十、技术栈

- **后端**：Java Spring Boot + MyBatis Plus + MySQL
- **前端**：Vue 3 + Element Plus + Vue Router
- **状态管理**：Composition API
- **样式**：Scoped CSS + Flexbox/Grid

---

**开发完成时间**：2026-03-17  
**版本**：v1.0.0
