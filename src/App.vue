<template>
  <div id="app">
    <router-view />  <!-- ✅ 这里用 router-view 而不是固定加载 Login -->
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>
