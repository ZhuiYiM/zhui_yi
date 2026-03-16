<template>
  <div class="seller-section">
    <h3>卖家信息</h3>
    <div class="seller-info" @click="handleViewSeller">
      <img 
        :src="seller.avatar || defaultAvatar" 
        :alt="seller.name"
        class="seller-avatar"
      >
      <div class="seller-details">
        <span class="seller-name">{{ seller.name }}</span>
        <div class="seller-tags">
          <span v-if="seller.identityTag" class="seller-tag" :class="seller.identityTag">
            {{ getIdentityTagName(seller.identityTag) }}
          </span>
          <span v-if="seller.productCount !== undefined" class="product-count-tag">
            TA 有 {{ seller.productCount }} 件商品
          </span>
        </div>
        <p v-if="seller.college" class="seller-college">📍 {{ seller.college }}</p>
      </div>
      <div class="seller-actions">
        <button class="contact-btn" @click.stop="handleContact">联系卖家</button>
        <button class="view-shop-btn" @click.stop="handleViewSeller">逛 TA 的小店</button>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  seller: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['view-seller', 'contact']);

const defaultAvatar = 'https://placehold.co/100x100/4A90E2/FFFFFF?text=U';

const getIdentityTagName = (tag) => {
  const tagMap = {
    'student': '学生',
    'merchant': '商家',
    'admin': '管理员',
    'organization': '团体',
    'society': '社会'
  };
  return tagMap[tag] || tag;
};

const handleViewSeller = () => {
  emit('view-seller', props.seller.id);
};

const handleContact = () => {
  emit('contact', props.seller.id);
};
</script>

<style scoped>
.seller-section h3 {
  margin: 0 0 15px;
  font-size: 1.1rem;
  color: #333;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.seller-info:hover {
  background-color: #e9f7fe;
}

.seller-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.seller-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.seller-name {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
}

.seller-tags {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.seller-tag {
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 0.75rem;
  font-weight: bold;
  color: white;
  align-self: flex-start;
}

.seller-tag.student {
  background-color: #4A90E2;
}

.seller-tag.merchant {
  background-color: #50C878;
}

.seller-tag.admin {
  background-color: #FF6B6B;
}

.seller-tag.organization {
  background-color: #9B59B6;
}

.product-count-tag {
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 0.75rem;
  background-color: #f0f2f5;
  color: #666;
}

.seller-college {
  margin: 0;
  font-size: 0.85rem;
  color: #777;
}

.seller-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.contact-btn {
  padding: 8px 20px;
  background-color: #4A90E2;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.contact-btn:hover {
  background-color: #5a9fd6;
}

.view-shop-btn {
  padding: 8px 20px;
  background-color: white;
  color: #4A90E2;
  border: 2px solid #4A90E2;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.view-shop-btn:hover {
  background-color: #e9f7fe;
}
</style>
