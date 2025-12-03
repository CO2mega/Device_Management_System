<template>
  <div class="dashboard-home">
    <div class="top-chart glass-card" ref="overviewChart"></div>

    <div class="dashboard">
      <div class="stat-card glass-card" v-for="(item, index) in stats" :key="index">
        <h3>{{ item.label }}</h3>
        <p class="value">{{ item.value }}</p>
      </div>
    </div>

    <div class="charts">
      <div ref="deviceChart" class="chart glass-card"></div>
      <div ref="userChart" class="chart glass-card"></div>
    </div>
  </div>
</template>

<script>
import * as echarts from "echarts";
import { dashboardApi, deviceApi } from "@/api/services";

// 使用亮色科技感主题颜色
const PRIMARY_COLOR_START = '#00c0ff'; // 青色霓虹起始
const PRIMARY_COLOR_END = '#7a8cff';   // 紫蓝色结束
const SECONDARY_COLOR = '#062238';     // 深海蓝文本/标题

export default {
  name: "DashboardHome",
  data() {
    return {
      stats: [
        { label: "设备总数", value: 0 },
        { label: "在借设备", value: 0 },
        { label: "可用设备", value: 0 },
        { label: "注册用户", value: 0 },
      ],
      deviceTypeData: [],
      loading: false
    };
  },
  async mounted() {
    await this.fetchDashboardData();
    this.initCharts();
  },
  methods: {
    async fetchDashboardData() {
      this.loading = true;
      try {
        const response = await dashboardApi.getStatistics();
        const data = response.data;
        
        this.stats = [
          { label: "设备总数", value: data.totalDevices },
          { label: "在借设备", value: data.loanedDevices },
          { label: "可用设备", value: data.availableDevices },
          { label: "注册用户", value: data.totalUsers },
        ];

        // Fetch devices to calculate type distribution
        const devicesResponse = await deviceApi.getAll({ size: 100 });
        const devices = devicesResponse.data.content;
        
        // Calculate device type distribution
        const typeCount = {};
        devices.forEach(device => {
          const type = device.type || '其他';
          typeCount[type] = (typeCount[type] || 0) + 1;
        });
        
        this.deviceTypeData = Object.entries(typeCount).map(([name, value]) => ({ name, value }));
      } catch (error) {
        console.error('Failed to fetch dashboard data:', error);
        this.$message.error('获取统计数据失败');
      } finally {
        this.loading = false;
      }
    },
    initCharts() {
      // 顶部概览图 (颜色主题调整)
      const overviewChart = echarts.init(this.$refs.overviewChart);
      
      // Prepare data for overview chart
      const typeNames = this.deviceTypeData.map(item => item.name);
      const typeValues = this.deviceTypeData.map(item => item.value);
      
      overviewChart.setOption({
        title: { text: "设备类型分布概览", left: "center", textStyle: { color: SECONDARY_COLOR } },
        tooltip: { trigger: "axis" },
        xAxis: { type: "category", data: typeNames.length > 0 ? typeNames : ["无数据"], axisLine: { lineStyle: { color: SECONDARY_COLOR } } },
        yAxis: { type: "value", axisLine: { lineStyle: { color: SECONDARY_COLOR } } },
        series: [
          {
            name: "设备数量",
            type: "bar",
            data: typeValues.length > 0 ? typeValues : [0],
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: PRIMARY_COLOR_START },
                { offset: 1, color: PRIMARY_COLOR_END },
              ]),
            },
          },
        ],
      });

      // 设备状态图
      const deviceChart = echarts.init(this.$refs.deviceChart);
      const loanedCount = this.stats[1].value;
      const availableCount = this.stats[2].value;
      
      deviceChart.setOption({
        title: { text: "设备状态分布", left: "center", textStyle: { color: SECONDARY_COLOR } },
        tooltip: { trigger: "item" },
        series: [
          {
            name: "设备状态",
            type: "pie",
            radius: "60%",
            data: [
              { value: availableCount, name: "可用" },
              { value: loanedCount, name: "在借" },
            ],
            // 根据索引映射到主题色
            color: [PRIMARY_COLOR_END, PRIMARY_COLOR_START]
          },
        ],
      });

      // 用户趋势图
      const userChart = echarts.init(this.$refs.userChart);
      const totalUsers = this.stats[3].value;
      
      // Simulate user registration trend based on current total
      const months = ["1月", "2月", "3月", "4月", "5月", "6月"];
      const userTrend = months.map((_, index) => Math.round(totalUsers * (index + 1) / months.length));
      
      userChart.setOption({
        title: { text: "用户注册趋势", left: "center", textStyle: { color: SECONDARY_COLOR } },
        tooltip: { trigger: "axis" },
        xAxis: { type: "category", data: months, axisLine: { lineStyle: { color: SECONDARY_COLOR } } },
        yAxis: { type: "value", axisLine: { lineStyle: { color: SECONDARY_COLOR } } },
        series: [
          {
            data: userTrend,
            type: "line",
            smooth: true,
            lineStyle: { color: PRIMARY_COLOR_START },
            itemStyle: { color: PRIMARY_COLOR_START },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(0,192,255,0.18)' },
                { offset: 1, color: 'rgba(255,255,255,0)' },
              ])
            },
          },
        ],
      });

      window.addEventListener("resize", () => {
        overviewChart.resize();
        deviceChart.resize();
        userChart.resize();
      });
    },
  },
};
</script>

<style scoped>
/* 核心：定义一个玻璃卡片的通用样式，用于所有的统计卡片和图表 */
.glass-card {
    /* 移除白色背景，使用半透明背景 */
    background: rgba(255, 255, 255, 0.4); 
    /* 玻璃模糊效果 */
    backdrop-filter: blur(5px);
    -webkit-backdrop-filter: blur(5px);
    /* 提升层次感和轻盈感 */
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.3); /* 增加一个轻微的白色边框 */
    border-radius: 12px;
    padding: 16px;
}

/* 针对特定组件应用通用卡片样式 */
.top-chart {
  width: 100%;
  height: 350px;
  margin-bottom: 24px;
}

.dashboard {
  display: flex;
  justify-content: space-around;
  margin-bottom: 24px;
}

.stat-card {
    /* 继承 .glass-card 样式，不需要重复设置背景、圆角和阴影 */
  width: 22%;
  padding: 20px;
  text-align: center;
}

.stat-card h3 {
    /* 调整标题颜色，使其在浅色背景上清晰 */
    color: #333; 
    font-size: 24px;
}

.value {
  font-size: 28px;
  font-weight: bold;
  /* 调整颜色为主题色 */
  color: #ff8cfa; /* 使用粉色主题色 */
  margin-top: 10px;
}

.charts {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 20px;
}

.chart {
    /* 继承 .glass-card 样式 */
  flex: 1;
  height: 280px;
}
</style>