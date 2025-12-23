<template>
  <div class="dashboard-home">
    <div class="top-row">
      <div class="top-chart glass-card" ref="overviewChart"></div>
      <div class="status-pie glass-card" ref="statusPie"></div>
    </div>

    <div class="dashboard">
      <div class="stat-card glass-card" v-for="(item, index) in stats" :key="index">
        <h3>{{ item.label }}</h3>
        <p class="value">{{ item.value }}</p>
      </div>
    </div>

    <div class="charts">
      <div ref="deviceChart" class="chart glass-card"></div>
      <div class="status-summary glass-card">
        <div class="status-item">
          <h4>正常</h4>
          <p class="status-value">{{ statusCounts.normal }}</p>
        </div>
        <div class="status-item fault">
          <h4>故障</h4>
          <p class="status-value">{{ statusCounts.fault }}</p>
        </div>
      </div>
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
      statusCounts: { normal: 0, fault: 0 },
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

        // Use backend stored-procedure based endpoint that returns status distribution
        const statusResp = await dashboardApi.getStatusDistribution();
        const statusMap = statusResp.data || {};
        // Map values into counts and also fetch device types via API for the left chart
        this.statusCounts = { normal: statusMap['正常'] || 0, fault: statusMap['故障'] || 0 };

        // Still fetch device types via deviceApi (paged). For accurate counts, backend endpoint is preferred but
        // we preserve client-side type aggregation for now.
        const devicesResponse = await deviceApi.getAll({ size: 100 });
        const devices = Array.isArray(devicesResponse.data) ? devicesResponse.data : (devicesResponse.data && devicesResponse.data.content ? devicesResponse.data.content : []);

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

      // 右侧：状态饼图（正常 / 故障）
      const statusChart = echarts.init(this.$refs.statusPie);
      const statusData = [
        { value: this.statusCounts.normal || 0, name: '正常' },
        { value: this.statusCounts.fault || 0, name: '故障' }
      ];
      statusChart.setOption({
        title: { text: '设备状态（正常/故障）', left: 'center', textStyle: { color: SECONDARY_COLOR } },
        tooltip: { trigger: 'item' },
        series: [
          {
            name: '设备状态',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            label: { show: true, position: 'outside', formatter: '{b}: {c}' },
            data: statusData,
            color: [PRIMARY_COLOR_START, PRIMARY_COLOR_END]
          }
        ]
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

      window.addEventListener("resize", () => {
        overviewChart.resize();
        statusChart.resize();
        deviceChart.resize();
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

/* 顶部两栏布局：左侧类型图，右侧状态饼图 */
.top-row{
  display:flex;
  gap:16px;
  margin-bottom:24px;
}
.top-chart{ flex:1; height:350px }
.status-pie{ width:300px; height:350px; padding:16px }

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
  align-items: flex-start;
  gap: 20px;
}

.chart {
  /* 设备状态图为弹性宽度，保持突出 */
  flex: 1 1 60%;
  height: 300px;
}

.status-summary {
  width: 320px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.status-item { width: 100%; text-align: center; margin-bottom: 16px }
.status-item h4 { margin: 0; color: #333 }
.status-value { font-size: 28px; font-weight: bold; margin: 8px 0 }
.status-item.fault .status-value { color: #e9646b }
.status-item .status-value { color: #3fb07f }
</style>