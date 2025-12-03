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
export default {
  name: "UserDeviceSearch",
  data() {
    return {
      searchQuery: "",
      devices: [
  { name: "温度传感器", type: "传感器", status: "正常", location: "仓库A" },
  { name: "压力测试仪", type: "检测设备", status: "故障", location: "实验室2" },
  { name: "流量计", type: "计量设备", status: "正常", location: "仓库B" },
  { name: "电压表", type: "测量设备", status: "正常", location: "实验室1" },
  { name: "光学传感器", type: "传感器", status: "维修中", location: "仓库C" },
  { name: "湿度计", type: "传感器", status: "正常", location: "仓库A" },
  { name: "微波功率计", type: "检测设备", status: "正常", location: "实验室3" },
  { name: "激光测距仪", type: "测量设备", status: "故障", location: "仓库B" },
  { name: "红外热像仪", type: "检测设备", status: "正常", location: "实验室2" },
  { name: "电流表", type: "测量设备", status: "正常", location: "仓库C" }
]
,
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
  methods: {
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
 