<template>
  <div class="reviews-summary">
    <div class="summary-left">
      <div class="average-score">{{ averageScore.toFixed(1) }}</div>
      <div class="score-stars">
        <span v-for="i in 5" :key="i" class="star" :class="{ active: i <= Math.round(averageScore) }">
          {{ i <= Math.round(averageScore) ? '⭐' : '☆' }}
        </span>
      </div>
      <div class="seller-rating-text">卖家服务评分</div>
    </div>
    <div class="summary-right">
      <div v-for="i in 5" :key="i" class="score-bar">
        <span class="score-label">{{ 6 - i }}星</span>
        <div class="bar-container">
          <div 
            class="bar-fill" 
            :style="{ width: getScorePercentage(6 - i) + '%' }"
          ></div>
        </div>
        <span class="score-count">{{ getScoreCount(6 - i) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  reviews: {
    type: Array,
    default: () => []
  },
  averageScore: {
    type: Number,
    default: 5.0
  }
});

// 获取评分分布
const getScoreCount = (score) => {
  return props.reviews.filter(r => r.rating === score).length;
};

// 获取评分百分比
const getScorePercentage = (score) => {
  if (props.reviews.length === 0) return 0;
  const count = getScoreCount(score);
  return (count / props.reviews.length) * 100;
};
</script>

<style scoped>
.reviews-summary {
  display: flex;
  justify-content: space-between;
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.summary-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.average-score {
  font-size: 3rem;
  font-weight: bold;
  color: #4A90E2;
}

.score-stars {
  display: flex;
  gap: 5px;
}

.score-stars .star {
  font-size: 1.2rem;
}

.seller-rating-text {
  font-size: 0.9rem;
  color: #777;
  margin-top: 5px;
}

.summary-right {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.score-bar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.score-label {
  font-size: 0.8rem;
  color: #777;
  width: 30px;
}

.bar-container {
  width: 150px;
  height: 8px;
  background-color: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #4A90E2, #5a9fd6);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.score-count {
  font-size: 0.8rem;
  color: #777;
  width: 30px;
  text-align: right;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .reviews-summary {
    flex-direction: column;
    gap: 20px;
  }
  
  .summary-right {
    width: 100%;
  }
  
  .bar-container {
    width: 100px;
  }
}
</style>
