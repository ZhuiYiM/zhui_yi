
<template>
<div>
  <h3>原生html</h3>
  <input type="text" v-model="number">
  <input type="button" value="+1" @click="add">
</div>
  <div>
    <h3>连接后端excail</h3>
    <input type="text" v-model="number">
    <input type="button" value="+1" @click="submit">
  </div>

</template>
<script setup>
import {ref} from "vue";
import axios from "axios";
const number = ref(0);
function add(){
  number.value++;
}
function submit(){
  axios.get("http://localhost:8080/number/add?number=" + number.value).then(r => {
    number.value = r.request.responseText;
  })
}
</script>

<style scoped>

</style>