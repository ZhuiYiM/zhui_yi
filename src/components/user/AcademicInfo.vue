<template>
  <div class="academic-info">
    <div class="info-item">
      <span class="item-label">🏛️ 学院</span>
      <span class="item-value">{{ academicInfo.college || '未填写' }}</span>
    </div>
    <div class="info-item">
      <span class="item-label">📚 专业</span>
      <span class="item-value">{{ academicInfo.major || '未填写' }}</span>
    </div>
    <div v-if="showStudentId" class="info-item">
      <span class="item-label">🎓 学号</span>
      <span class="item-value">{{ academicInfo.studentId || '未填写' }}</span>
    </div>
    <div class="info-item">
      <span class="item-label">📅 年级</span>
      <span class="item-value">{{ academicInfo.grade || '未填写' }}</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  academicInfo: {
    type: Object,
    required: true
  },
  showStudentId: {
    type: Boolean,
    default: true
  }
});
</script>

<style scoped>
.academic-info {
  padding: 30px 40px;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  font-size: 15px;
}

.item-label {
  color: #666;
  font-weight: 500;
}

.item-value {
  color: #333;
}

@media (max-width: 768px) {
  .academic-info {
    padding: 20px;
  }
}
</style>
