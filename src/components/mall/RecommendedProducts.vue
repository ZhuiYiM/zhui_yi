<template>
  <section class="recommended-section">
    <h3>猜你喜欢</h3>
    <ProductList 
      :products="products" 
      :is-mobile="isMobile"
      :columns="4"
      :mobile-columns="2"
      :show-load-more="false"
      @product-click="$emit('product-click', $event)"
    />
  </section>
</template>

<script setup>
import ProductList from './ProductList.vue';

defineProps({
  products: {
    type: Array,
    default: () => []
  },
  isMobile: {
    type: Boolean,
    default: false
  }
});

defineEmits(['product-click']);
</script>

<style scoped>
.recommended-section {
  margin: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.recommended-section h3 {
  margin: 0 0 20px;
  font-size: 1.2rem;
  color: #333;
}
</style>
