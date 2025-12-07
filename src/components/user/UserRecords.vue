<template>
  <div class="user-records-container">
    <h2>借用记录与归还</h2>

    <!-- 搜索/筛选 -->
    <el-input
      v-model="searchQuery"
      placeholder="搜索设备名称"
      clearable
      style="width: 300px; margin-bottom: 20px;"
      prefix-icon="el-icon-search"
    >
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
import { loanApi } from '@/api/services';

export default {
  name: "UserRecords",
  data() {
    return {
      searchQuery: "",
      records: [],
      loading: false
    };
  },
  computed: {
    filteredRecords() {
      if (!this.searchQuery) return this.records;
      const q = this.searchQuery.toLowerCase();
      return this.records.filter(r => (r.device||'').toLowerCase().includes(q));
    }
  },
  mounted() {
    this.fetchRecords();
  },
  methods: {
    async fetchRecords() {
      this.loading = true;
      try {
        const response = await loanApi.getAll({ size: 100 });
        const data = response.data;
        if (!data) {
          this.records = [];
          return;
        }

        // determine items array (support Page or raw array)
        const items = Array.isArray(data) ? data : (data.content || []);

        // get current logged-in user from localStorage
        let currentUser = null;
        try {
          currentUser = JSON.parse(localStorage.getItem('user')) || null;
        } catch (e) {
          currentUser = null;
        }

        // If we have a currentUser, filter items to only those belonging to them
        const userFiltered = items.filter(record => {
          if (!record) return false;
          // backend loan record might contain applicant or applicantId
          const applicant = record.applicant || record.applicantUser || null;
          if (currentUser) {
            if (applicant && (applicant.id === currentUser.id || applicant.staffId === currentUser.staffId)) return true;
            // fallback: some APIs include applicantId or applicantStaffId at top level
            if (record.applicantId && currentUser.id && record.applicantId === currentUser.id) return true;
            if (record.applicantStaffId && currentUser.staffId && record.applicantStaffId === currentUser.staffId) return true;
            return false;
          }
          // if no current user info available, be conservative and return none
          return false;
        });

        this.records = userFiltered
          .filter(record => record.approvalStatus === 'APPROVED' || record.approvalStatus === 'APPROVED' /* keep same */)
          .map(record => ({
            id: record.id,
            device: record.deviceName || (record.device && record.device.name) || '',
            borrowDate: record.applyDate ? (record.applyDate.substring ? record.applyDate.substring(0,10) : record.applyDate) : '',
            returnDate: record.actualReturnDate ? (record.actualReturnDate.substring ? record.actualReturnDate.substring(0,10) : record.actualReturnDate) : '',
            status: this.mapStatus(record),
            image: this.getDeviceImage(record.deviceName || (record.device && record.device.name) || '')
          }));
      } catch (error) {
        console.error('Failed to fetch records:', error);
        this.$message.error('获取借用记录失败');
      } finally {
        this.loading = false;
      }
    },
    mapStatus(record) {
      if (record.actualReturnDate) {
        return '已归还';
      }
      if (record.returnStatus === 'OVERDUE' || 
          (record.expectedReturnDate && new Date(record.expectedReturnDate) < new Date())) {
        return '逾期';
      }
      return '借用中';
    },
    getDeviceImage(deviceName) {
      // Default images for different device types
      const imageMap = {
        '温度传感器': require('@/assets/OIP-C.webp'),
        '压力测试仪': require('@/assets/OIP-C (1).webp'),
        '流量计': require('@/assets/OIP-C (2).webp'),
        '电压表': require('@/assets/OIP-C (3).webp'),
        '光学传感器': require('@/assets/OIP-C (4).webp')
      };
      try {
        return imageMap[deviceName] || require('@/assets/OIP-C.webp');
      } catch (e) {
        return '';
      }
    },
    async returnDevice(row) {
      try {
        await loanApi.returnDevice(row.id);
        const today = new Date().toISOString().split("T")[0];
        row.status = "已归还";
        row.returnDate = today;
        this.$message.success(`${row.device} 已归还`);
        this.fetchRecords();
      } catch (error) {
        console.error('Failed to return device:', error);
        this.$message.error('归还失败：' + (error.response?.data?.message || error.message));
      }
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
