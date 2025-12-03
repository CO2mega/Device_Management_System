<template>
  <div class="user-records-container">
    <h2>借用记录与归还</h2>

    <!-- 搜索/筛选 -->
    <el-input
      v-model="searchQuery"
      placeholder="搜索设备名称"
      clearable
      style="width: 300px; margin-bottom: 20px;"
    >
      <template #prefix>
        <i class="el-icon-search"></i>
      </template>
    </el-input>

<el-table
  :data="filteredRecords"
  style="width: 100%;"
  border
  stripe
  size="small"
>
  <el-table-column prop="device" label="设备名称" min-width="150"/>
  <el-table-column label="设备图像" width="80">
    <template #default="scope">
      <el-image
        :src="scope.row.image"
        :preview-src-list="[scope.row.image]"
        style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px;"
      ></el-image>
    </template>
  </el-table-column>
  <el-table-column prop="borrowDate" label="借用日期" min-width="120"/>
  <el-table-column prop="returnDate" label="归还日期" min-width="120"/>
  <el-table-column label="状态" min-width="100">
    <template #default="scope">
      <el-tag
        size="small"
        :type="scope.row.status === '已归还' ? 'success' :
               scope.row.status === '逾期' ? 'danger' : 'warning'"
      >
        {{ scope.row.status }}
      </el-tag>
    </template>
  </el-table-column>
  <el-table-column label="操作" min-width="100">
    <template #default="scope">
      <el-button
        size="mini"
        type="primary"
        @click="returnDevice(scope.row)"
        :disabled="scope.row.status === '已归还'"
      >
        归还
      </el-button>
    </template>
  </el-table-column>
</el-table>

  </div>
</template>

<script>
export default {
  name: "UserRecords",
  data() {
    return {
      searchQuery: "",
        records: [
        { 
            device: "温度传感器", 
            borrowDate: "2024-09-01", 
            returnDate: "2024-09-05", 
            status: "已归还", 
            image: require('@/assets/OIP-C.webp') 
        },
        { 
            device: "压力测试仪", 
            borrowDate: "2024-09-10", 
            returnDate: "", 
            status: "借用中", 
            image: require('@/assets/OIP-C (1).webp') 
        },
        { 
            device: "流量计", 
            borrowDate: "2024-09-05", 
            returnDate: "", 
            status: "逾期", 
            image: require('@/assets/OIP-C (2).webp') 
        },
        { 
            device: "电压表", 
            borrowDate: "2024-09-12", 
            returnDate: "", 
            status: "借用中", 
            image: require('@/assets/OIP-C (3).webp') 
        },
        { 
            device: "光学传感器", 
            borrowDate: "2024-09-01", 
            returnDate: "2024-09-03", 
            status: "已归还", 
            image: require('@/assets/OIP-C (4).webp') 
        },
        ]

    };
  },
  computed: {
    filteredRecords() {
      if (!this.searchQuery) return this.records;
      return this.records.filter(r => r.device.includes(this.searchQuery));
    }
  },
  methods: {
    returnDevice(row) {
      const today = new Date().toISOString().split("T")[0];
      row.status = "已归还";
      row.returnDate = today;
      this.$message.success(`${row.device} 已归还`);
    }
  }
};
</script>

<style scoped>
.user-records-container {
  padding: 20px;
}

.el-input {
  margin-bottom: 20px;
}
</style>
