<template>
  <div class="user-stats">
    <div class="stat-item">
      <span class="stat-value">{{ statistics.postCount || 0 }}</span>
      <span class="stat-label">话题</span>
    </div>
    <div class="stat-item">
      <span class="stat-value">{{ statistics.followerCount || 0 }}</span>
      <span class="stat-label">粉丝</span>
    </div>
    <div class="stat-item">
      <span class="stat-value">{{ statistics.followingCount || 0 }}</span>
      <span class="stat-label">关注</span>
    </div>
    <div class="stat-item">
      <span class="stat-value">{{ statistics.likesReceived || 0 }}</span>
      <span class="stat-label">获赞</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  statistics: {
    type: Object,
    required: true
  }
});
</script>

<style scoped>
.user-stats {
  display: flex;
  padding: 30px 40px;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  transition: all 0.3s;
}

.stat-item:hover {
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-value {
  display: block;
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

@media (max-width: 768px) {
  .user-stats {
    padding: 20px;
  }
}
</style>
