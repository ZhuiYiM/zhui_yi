<template>
  <section class="reviews-section">
    <div class="reviews-header">
      <h3>商家评价</h3>
      <div class="header-actions">
        <span class="total-reviews-inline">共 {{ reviews.length }} 条评价</span>
        <button class="write-review-btn" @click="$emit('write-review')">
          ✍️ 写评价
        </button>
      </div>
    </div>
    
    <!-- 评价统计 -->
    <ReviewsSummary 
      :reviews="reviews" 
      :average-score="averageScore"
    />

    <!-- 评价列表 -->
    <div class="reviews-list">
      <div v-if="reviews.length === 0" class="no-reviews">
        <div class="no-reviews-icon">💬</div>
        <p>暂无评价，快来抢沙发吧~</p>
      </div>
      <ReviewItem
        v-for="review in reviews" 
        :key="review.id" 
        :review="review"
        @view-product="$emit('view-product', $event)"
        @view-image="$emit('view-image', $event)"
        @like="$emit('like', $event)"
        @reply="$emit('reply', $event)"
      />
    </div>
  </section>
</template>

<script setup>
import ReviewsSummary from './ReviewsSummary.vue';
import ReviewItem from './ReviewItem.vue';

defineProps({
  reviews: {
    type: Array,
    default: () => []
  },
  averageScore: {
    type: Number,
    default: 5.0
  }
});

defineEmits(['write-review', 'view-product', 'view-image', 'like', 'reply']);
</script>

<style scoped>
.reviews-section {
  margin: 20px;
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f2f5;
}

.reviews-header h3 {
  margin: 0;
  font-size: 1.3rem;
  color: #333;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.total-reviews-inline {
  font-size: 0.9rem;
  color: #777;
}

.write-review-btn {
  padding: 8px 20px;
  background-color: #4A90E2;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.write-review-btn:hover {
  background-color: #5a9fd6;
  transform: translateY(-2px);
}

/* 评价列表 */
.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.no-reviews {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.no-reviews-icon {
  font-size: 3rem;
  margin-bottom: 10px;
}

.no-reviews p {
  margin: 0;
  font-size: 0.9rem;
}
</style>
