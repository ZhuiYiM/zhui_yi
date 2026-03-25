<template>
  <div class="identity-badges">
    <el-tag
      v-for="badge in badges"
      :key="badge.type"
      :type="getTagType(badge.type)"
      :color="badge.color"
      size="small"
      effect="light"
      class="identity-badge"
    >
      <el-icon v-if="badge.icon" style="margin-right: 4px;">
        <component :is="badge.icon" />
      </el-icon>
      {{ badge.name }}
      <el-icon v-if="badge.verified" color="#67C23A" style="margin-left: 4px;">
        <CircleCheckFilled />
      </el-icon>
    </el-tag>
  </div>
</template>

<script setup>
import { CircleCheckFilled, User, Suit, Shop, OfficeBuilding } from '@element-plus/icons-vue'

const props = defineProps({
  badges: {
    type: Array,
    default: () => []
  }
})

const getTagType = (type) => {
  const typeMap = {
    student: '',
    staff: 'success',
    merchant: 'warning',
    organization: 'info'
  }
  return typeMap[type] || ''
}
</script>

<style scoped>
.identity-badges {
  display: inline-flex;
  gap: 8px;
  flex-wrap: wrap;
}

.identity-badge {
  display: flex;
  align-items: center;
}
</style>
