<template>
  <div class="device-search-container">
    <h2>设备查询</h2>
    <el-input v-model="searchQuery" placeholder="输入设备名称或编号" clearable class="search-input" />
    <el-button type="primary" @click="search">搜索</el-button>

    <el-table :data="filteredDevices" style="width: 100%; margin-top: 20px;">
      <el-table-column prop="name" label="设备名称" />
      <el-table-column prop="type" label="类型" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="location" label="所在位置" />
    </el-table>
  </div>
</template>

<script>
import { deviceApi } from '@/api/services';

export default {
  name: "UserDeviceSearch",
  data() {
    return {
      searchQuery: "",
      devices: [],
      loading: false
    };
  },
  computed: {
    filteredDevices() {
      if (!this.searchQuery) return this.devices;
      return this.devices.filter(d =>
        d.name.includes(this.searchQuery) || d.type.includes(this.searchQuery)
      );
    }
  },
  mounted() {
    this.fetchDevices();
  },
  methods: {
    async fetchDevices() {
      this.loading = true;
      try {
        const response = await deviceApi.getAll({ size: 100 });
        const data = response.data;
        
        this.devices = data.content.map(device => ({
          id: device.id,
          name: device.name,
          type: device.type || '其他',
          status: device.status || '正常',
          location: device.location || ''
        }));
      } catch (error) {
        console.error('Failed to fetch devices:', error);
        this.$message.error('获取设备列表失败');
      } finally {
        this.loading = false;
      }
    },
    search() {
      this.$message.info(`搜索关键字：${this.searchQuery}`);
    }
  }
};
</script>

<style scoped>
.device-search-container {
  padding: 20px;
}

.search-input {
  width: 300px;
  margin-right: 10px;
}
</style>
 