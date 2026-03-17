<template>
  <div v-if="visible" class="buy-modal-overlay" @click="handleClose">
    <div class="buy-modal-container" @click.stop>
      <button @click="handleClose" class="close-btn" aria-label="关闭">×</button>
      
      <h3 class="modal-title">确认购买</h3>
      
      <div class="modal-body">
        <!-- 商品信息卡片 -->
        <div class="product-card">
          <img 
            :src="displayImage" 
            :alt="productTitle" 
            class="product-image"
            @error="handleImageError"
          >
          <div class="product-info">
            <h4 class="product-title">{{ productTitle }}</h4>
            <p class="product-price">¥{{ productPrice }}</p>
          </div>
        </div>

        <!-- 购买数量 -->
        <div class="info-section">
          <div class="section-title">购买数量：</div>
          <div class="quantity-selector">
            <button 
              @click="decreaseQuantity" 
              class="quantity-btn"
              :disabled="quantity <= 1"
            >
              -
            </button>
            <span class="quantity-value">{{ quantity }}</span>
            <button 
              @click="increaseQuantity" 
              class="quantity-btn"
              :disabled="quantity >= maxQuantity"
            >
              +
            </button>
            <span class="quantity-hint">（最多 {{ maxQuantity }} 件）</span>
          </div>
        </div>

        <!-- 商品规格选择 -->
        <div v-if="specifications && Object.keys(specifications).length > 0" class="info-section">
          <div class="section-title">选择规格：</div>
          <div class="specifications-selector">
            <div v-for="(values, specName) in specifications" :key="specName" class="spec-group">
              <div class="spec-name">{{ specName }}：</div>
              <div class="spec-options">
                <button
                  v-for="spec in values" :key="spec.specId || spec.specValue"
                  @click="selectSpecification(specName, spec)"
                  :class="['spec-option', { selected: selectedSpecs[specName] === spec.specValue }]"
                >
                  {{ spec.specValue }}
                  <span v-if="spec.priceAdjustment != 0" class="price-adjust">
                    {{ spec.priceAdjustment > 0 ? '+' : '' }}¥{{ spec.priceAdjustment }}
                  </span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 买家联系方式 -->
        <div class="info-section">
          <div class="section-title">您的联系方式：</div>
          <input 
            v-model="buyerContact" 
            type="text" 
            class="input-field"
            placeholder="请输入您的手机号或微信号（方便卖家联系您）"
            maxlength="50"
          >
          <p class="input-hint">此信息仅对卖家可见</p>
        </div>

        <!-- 购买留言 -->
        <div class="info-section">
          <div class="section-title">购买留言：</div>
          <textarea 
            v-model="message" 
            class="textarea-field"
            placeholder="可选：填写地址、备注等信息"
            maxlength="200"
            rows="3"
          ></textarea>
          <p class="input-hint">{{ message.length }}/200</p>
        </div>

        <!-- 卖家信息 -->
        <div class="info-section">
          <div class="info-row">
            <span class="info-label">卖家：</span>
            <span class="info-value">{{ sellerName }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">联系方式：</span>
            <span class="info-value">{{ contactInfo || '未提供' }}</span>
          </div>
        </div>

        <!-- 交易方式 -->
        <div class="info-section">
          <div class="section-title">交易方式：</div>
          <div class="method-tags">
            <span 
              v-for="method in tradeMethods" 
              :key="method" 
              class="method-tag"
            >
              {{ getTradeMethodName(method) }}
            </span>
          </div>
        </div>

        <!-- 交易地点 -->
        <div class="info-section">
          <div class="info-row">
            <span class="info-label">交易地点：</span>
            <span class="info-value">{{ tradeLocation || '未指定' }}</span>
          </div>
        </div>

        <!-- 订单摘要 -->
        <div class="order-summary">
          <div class="summary-row">
            <span class="summary-label">商品单价：</span>
            <span class="summary-value">¥{{ productPrice }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">购买数量：</span>
            <span class="summary-value">×{{ quantity }}</span>
          </div>
          <div class="summary-row total-row">
            <span class="summary-label">总计：</span>
            <span class="total-amount">¥{{ totalAmount }}</span>
          </div>
        </div>

        <!-- 注意事项 -->
        <div class="notice-section">
          <div class="notice-title">
            <span class="notice-icon">⚠️</span>
            <span>请注意：</span>
          </div>
          <ul class="notice-list">
            <li>购买前请仔细查看商品描述和图片</li>
            <li>建议当面交易，确保商品质量</li>
            <li>付款后商品状态将变为"已售出"</li>
            <li>请保留好交易凭证</li>
          </ul>
        </div>
      </div>

      <!-- 底部操作按钮 -->
      <div class="modal-footer">
        <button 
          @click="handleClose" 
          class="btn-cancel"
          :disabled="loading"
        >
          取消
        </button>
        <button 
          @click="handleConfirm" 
          class="btn-confirm"
          :disabled="loading"
        >
          {{ loading ? '处理中...' : '确认购买' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  productImage: {
    type: String,
    default: ''
  },
  productTitle: {
    type: String,
    required: true
  },
  productPrice: {
    type: [Number, String],
    required: true
  },
  sellerName: {
    type: String,
    required: true
  },
  contactInfo: {
    type: String,
    default: ''
  },
  tradeMethods: {
    type: Array,
    default: () => []
  },
  tradeLocation: {
    type: String,
    default: ''
  },
  maxQuantity: {
    type: Number,
    default: 5
  },
  specifications: {
    type: Object,
    default: () => ({}) // 规格数据，格式：{ '颜色': [{specId:1, specValue:'红色',...}] }
  }
});

const emit = defineEmits(['close', 'confirm']);

// 内部状态
const quantity = ref(1);
const buyerContact = ref('');
const message = ref('');
const selectedSpecs = ref({}); // 选中的规格 { '颜色': '红色' }
const specPriceAdjustment = ref(0); // 规格价格调整

// 默认图片
const defaultImage = 'https://placehold.co/300x300/e0e0e0/999999?text=商品图片';

// 计算实际显示的图片
const displayImage = computed(() => {
  return props.productImage || defaultImage;
});

// 计算总金额
const totalAmount = computed(() => {
  const price = parseFloat(props.productPrice) || 0;
  const adjustedPrice = price + specPriceAdjustment.value;
  return (adjustedPrice * quantity.value).toFixed(2);
});

// 获取交易方式名称
const getTradeMethodName = (method) => {
  const methodMap = {
    'face-to-face': '当面交易',
    'delivery': '快递配送',
    'campus-delivery': '校内配送'
  };
  return methodMap[method] || method;
};

// 处理图片加载失败
const handleImageError = (e) => {
  e.target.src = defaultImage;
};

// 关闭弹窗
const handleClose = () => {
  if (!props.loading) {
    emit('close');
  }
};

// 确认购买
const handleConfirm = () => {
  // 验证是否选择了规格（如果有规格的话）
  if (props.specifications && Object.keys(props.specifications).length > 0) {
    const selectedCount = Object.keys(selectedSpecs.value).length;
    const totalSpecGroups = Object.keys(props.specifications).length;
    
    if (selectedCount < totalSpecGroups) {
      ElMessage.warning('请选择完整的商品规格');
      return;
    }
  }
  
  // 验证联系方式
  if (!buyerContact.value.trim()) {
    ElMessage.warning('请填写您的联系方式');
    return;
  }
  
  // 验证联系方式格式
  const contactRegex = /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$|^1[3-9]\d{9}$|^[1-9]\d{4,9}$/;
  if (!contactRegex.test(buyerContact.value.trim())) {
    ElMessage.warning('请输入有效的手机号、微信号或 QQ 号');
    return;
  }
  
  // 发送确认事件，包含订单信息
  emit('confirm', {
    quantity: quantity.value,
    buyerContact: buyerContact.value.trim(),
    message: message.value.trim(),
    totalAmount: totalAmount.value,
    selectedSpecifications: getSelectedSpecificationsDetail()
  });
};

// 增加数量
const increaseQuantity = () => {
  if (quantity.value < props.maxQuantity) {
    quantity.value++;
  }
};

// 减少数量
const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--;
  }
};

// 选择规格
const selectSpecification = (specName, spec) => {
  selectedSpecs.value[specName] = spec.specValue;
  
  // 计算价格调整（只计算当前选中的规格）
  let adjustment = 0;
  for (const name in selectedSpecs.value) {
    const values = props.specifications[name];
    if (values) {
      const selectedValue = values.find(v => v.specValue === selectedSpecs.value[name]);
      if (selectedValue && selectedValue.priceAdjustment) {
        adjustment += parseFloat(selectedValue.priceAdjustment);
      }
    }
  }
  specPriceAdjustment.value = adjustment;
};

// 获取选中的规格详情
const getSelectedSpecificationsDetail = () => {
  const result = [];
  for (const specName in selectedSpecs.value) {
    const values = props.specifications[specName];
    if (values) {
      const selectedValue = values.find(v => v.specValue === selectedSpecs.value[specName]);
      if (selectedValue) {
        result.push({
          specId: selectedValue.specId,
          specName: specName,
          specValue: selectedValue.specValue,
          specIcon: selectedValue.specIcon,
          quantity: quantity.value
        });
      }
    }
  }
  return result;
};

// 重置表单
const resetForm = () => {
  quantity.value = 1;
  buyerContact.value = '';
  message.value = '';
  selectedSpecs.value = {};
  specPriceAdjustment.value = 0;
};

// 监听 visible 变化，重置表单
watch(() => props.visible, (newVal) => {
  if (newVal) {
    resetForm();
  }
});
</script>

<style scoped>
.buy-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3000;
  padding: 20px;
  backdrop-filter: blur(2px);
  animation: overlayFadeIn 0.3s ease-out;
}

@keyframes overlayFadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.buy-modal-container {
  background: white;
  border-radius: 16px;
  max-width: 480px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.close-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  background: none;
  border: none;
  font-size: 28px;
  color: #999;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s;
  z-index: 1;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #333;
}

.modal-title {
  margin: 0;
  padding: 24px 24px 16px;
  font-size: 1.4rem;
  font-weight: 600;
  color: #333;
  text-align: center;
}

.modal-body {
  padding: 0 24px 24px;
}

/* 商品信息卡片 */
.product-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #f8f9fa 0%, #fff5f5 100%);
  border-radius: 12px;
  margin-bottom: 20px;
  border: 1px solid #ffe6e6;
}

.product-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}

.product-title {
  margin: 0 0 12px;
  font-size: 1.05rem;
  color: #333;
  font-weight: 600;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

.product-price {
  font-size: 1.5rem;
  font-weight: bold;
  color: #e74c3c;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 信息区域 */
.info-section {
  padding: 14px 16px;
  background: #f8f9fa;
  border-radius: 10px;
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  margin-bottom: 10px;
  line-height: 1.6;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  font-weight: 600;
  color: #666;
  min-width: 80px;
  flex-shrink: 0;
}

.info-value {
  color: #333;
  flex: 1;
}

.section-title {
  font-weight: 600;
  color: #666;
  margin-bottom: 10px;
  font-size: 0.95rem;
}

/* 数量选择器 */
.quantity-selector {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 规格选择器 */
.specifications-selector {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.spec-group {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.spec-name {
  font-weight: 600;
  color: #666;
  min-width: 80px;
  padding-top: 8px;
}

.spec-options {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.spec-option {
  padding: 8px 16px;
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 6px;
}

.spec-option:hover {
  border-color: #667eea;
  transform: translateY(-2px);
}

.spec-option.selected {
  border-color: #667eea;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.price-adjust {
  font-size: 0.8rem;
  font-weight: 600;
}

.spec-option.selected .price-adjust {
  color: #ffd700;
}

.quantity-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px;
  font-size: 1.2rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.quantity-btn:hover:not(:disabled) {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.5);
}

.quantity-btn:disabled {
  background: #e0e0e0;
  cursor: not-allowed;
  box-shadow: none;
}

.quantity-value {
  font-size: 1.3rem;
  font-weight: bold;
  color: #333;
  min-width: 40px;
  text-align: center;
}

.quantity-hint {
  font-size: 0.85rem;
  color: #999;
  margin-left: 8px;
}

/* 输入框 */
.input-field {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 0.95rem;
  transition: all 0.3s;
  box-sizing: border-box;
}

.input-field:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-hint {
  font-size: 0.8rem;
  color: #999;
  margin-top: 6px;
  line-height: 1.4;
}

/* 文本域 */
.textarea-field {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 0.95rem;
  resize: vertical;
  font-family: inherit;
  transition: all 0.3s;
  box-sizing: border-box;
}

.textarea-field:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/* 订单摘要 */
.order-summary {
  padding: 16px;
  background: linear-gradient(135deg, #fff9e6 0%, #fff3cd 100%);
  border-left: 4px solid #ffc107;
  border-radius: 10px;
  margin-bottom: 16px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 0.95rem;
}

.summary-row:last-child {
  margin-bottom: 0;
}

.summary-label {
  color: #666;
  font-weight: 500;
}

.summary-value {
  color: #333;
  font-weight: 600;
}

.total-row {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #ffc107;
  font-size: 1.1rem;
}

.total-amount {
  color: #e74c3c;
  font-weight: bold;
  font-size: 1.3rem;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 交易方式标签 */
.method-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.method-tag {
  padding: 6px 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 16px;
  font-size: 0.85rem;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(102, 126, 234, 0.3);
}

/* 注意事项 */
.notice-section {
  padding: 16px;
  background: linear-gradient(135deg, #fff9e6 0%, #fff3cd 100%);
  border-left: 4px solid #ffc107;
  border-radius: 10px;
}

.notice-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #856404;
  font-size: 1rem;
}

.notice-icon {
  font-size: 1.1rem;
}

.notice-list {
  margin: 0;
  padding-left: 20px;
  color: #856404;
}

.notice-list li {
  margin: 6px 0;
  font-size: 0.85rem;
  line-height: 1.6;
}

/* 底部按钮 */
.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 24px 24px;
  border-top: 1px solid #f0f0f0;
}

.btn-cancel,
.btn-confirm {
  flex: 1;
  padding: 14px;
  border-radius: 25px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-cancel:hover:not(:disabled) {
  background: #e8e8e8;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.btn-cancel:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-confirm {
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  color: white;
}

.btn-confirm:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(231, 76, 60, 0.4);
}

.btn-confirm:disabled {
  background: #ccc;
  cursor: not-allowed;
  box-shadow: none;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .buy-modal-container {
    max-height: 95vh;
    margin: 10px;
  }

  .product-card {
    flex-direction: column;
  }

  .product-image {
    width: 100%;
    height: 200px;
  }

  .modal-footer {
    flex-direction: column;
    gap: 10px;
  }

  .btn-cancel,
  .btn-confirm {
    flex: none;
    width: 100%;
  }
}
</style>
