<template>
  <div class="borrow-out">
    <h2>设备借出审批</h2>

    <!-- 上半部分：搜索栏 + 表格 -->
    <div class="top-section">
      <!-- 搜索栏 -->
      <div class="toolbar">
        <el-input
          v-model="search"
          placeholder="请输入申请人或设备名搜索"
          clearable
          prefix-icon="el-icon-search"
          class="search-box"
        />
        <el-select v-model="filterStatus" placeholder="筛选状态" clearable style="width: 180px;">
          <el-option label="待审批" value="待审批" />
          <el-option label="已通过" value="已通过" />
          <el-option label="已拒绝" value="已拒绝" />
          <el-option label="已归还" value="已归还" />
        </el-select>
      </div>

      <!-- 表格 -->
      <el-table
        :data="filteredRecords.slice((currentPage-1)*pageSize, currentPage*pageSize)"
        border
        style="width: 100%; margin-top: 20px;"
      >
        <el-table-column prop="id" label="申请编号" width="100" />
        <el-table-column prop="user" label="申请人" width="150" />
        <el-table-column prop="device" label="设备名称" width="200" />
        <el-table-column prop="applyDate" label="申请日期" width="150" />
        <el-table-column prop="borrowDate" label="借用日期" width="150" />
        <el-table-column prop="status" label="审批状态" width="120">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adminRemark" label="管理员备注" width="200" />
        <el-table-column label="操作" align="center" width="240">
          <template #default="scope">
            <template v-if="scope.row.status === '待审批'">
              <el-button size="mini" type="success" @click="approve(scope.row)">通过</el-button>
              <el-button size="mini" type="danger" @click="reject(scope.row)">拒绝</el-button>
            </template>
            <template v-else-if="scope.row.status === '已通过'">
              <el-button size="mini" type="primary" @click="openReturnDialog(scope.row)">归还登记</el-button>
            </template>
            <template v-else>
              <el-button size="mini" type="info" disabled>已处理</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :page-size="pageSize"
          :total="filteredRecords.length"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 下半部分：借用申请消息流 -->
    <div class="bottom-section">
      <h3>借用申请消息</h3>
      <el-empty v-if="filteredRecords.length === 0" description="暂无申请记录" />
      <el-card
        v-for="record in filteredRecords"
        :key="record.id"
        class="message-card"
        shadow="hover"
      >
        <div class="message-header">
          <strong>{{ record.user }}</strong> 申请借用
          <span class="device-name">【{{ record.device }}】</span>
        </div>
        <div class="message-body">
          <p>申请日期：{{ record.applyDate }}</p>
          <p>借用日期：{{ record.borrowDate }}</p>
          <p>
            状态：
            <el-tag :type="statusTagType(record.status)">
              {{ record.status }}
            </el-tag>
          </p>
          <p v-if="record.adminRemark">管理员备注：{{ record.adminRemark }}</p>
        </div>
      </el-card>
    </div>

    <!-- 归还登记弹窗 -->
    <el-dialog title="归还登记" :visible.sync="returnDialogVisible" width="450px">
      <el-form :model="returnForm" label-width="100px">
        <el-form-item label="设备名称">
          <el-input v-model="returnForm.device" disabled />
        </el-form-item>
        <el-form-item label="归还日期">
          <el-date-picker v-model="returnForm.returnDate" type="date" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="returnForm.remark" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReturn">确认归还</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "BorrowOut",
  data() {
    return {
      search: "",
      filterStatus: "",
      pageSize: 8,
      currentPage: 1,
      returnDialogVisible: false,
      returnForm: { id: null, device: "", returnDate: "", remark: "" },
      borrowRecords: [
        { id: 1, user: "user1", device: "温度传感器", applyDate: "2024-06-01", borrowDate: "2024-06-02", status: "待审批", adminRemark: "" },
        { id: 2, user: "user2", device: "压力测试仪", applyDate: "2024-05-25", borrowDate: "2024-05-26", status: "已通过", adminRemark: "实验测试用途" },
        { id: 3, user: "user3", device: "风速检测仪", applyDate: "2024-04-10", borrowDate: "2024-04-12", status: "已拒绝", adminRemark: "库存不足" },
        { id: 4, user: "user4", device: "摄像头模块", applyDate: "2024-03-08", borrowDate: "2024-03-10", status: "已归还", adminRemark: "完好归还" },
        { id: 5, user: "user5", device: "环境监测仪", applyDate: "2024-05-01", borrowDate: "2024-05-03", status: "待审批", adminRemark: "" },
        { id: 6, user: "user6", device: "电压表", applyDate: "2024-05-10", borrowDate: "2024-05-11", status: "已通过", adminRemark: "短期外借" }
      ]
    };
  },
  computed: {
    filteredRecords() {
      return this.borrowRecords.filter(r => {
        const matchSearch =
          !this.search ||
          r.user.includes(this.search) ||
          r.device.includes(this.search);
        const matchStatus =
          !this.filterStatus || r.status === this.filterStatus;
        return matchSearch && matchStatus;
      });
    }
  },
  methods: {
    handlePageChange(page) {
      this.currentPage = page;
    },
    statusTagType(status) {
      switch (status) {
        case "待审批": return "warning";
        case "已通过": return "success";
        case "已拒绝": return "danger";
        case "已归还": return "info";
        default: return "";
      }
    },
    approve(row) {
      row.status = "已通过";
      row.adminRemark = "审批通过";
      this.$message.success(`已通过【${row.device}】的借出申请`);
    },
    reject(row) {
      row.status = "已拒绝";
      row.adminRemark = "审批拒绝";
      this.$message.warning(`已拒绝【${row.device}】的借出申请`);
    },
    openReturnDialog(row) {
      this.returnForm = { id: row.id, device: row.device, returnDate: "", remark: "" };
      this.returnDialogVisible = true;
    },
    submitReturn() {
      const record = this.borrowRecords.find(r => r.id === this.returnForm.id);
      if (record) {
        record.status = "已归还";
        record.adminRemark = this.returnForm.remark || "设备已归还";
      }
      this.$message.success("归还登记成功！");
      this.returnDialogVisible = false;
    }
  }
};
</script>

<style scoped>
.borrow-out {
  padding: 20px;
  background: #fff;
  border-radius: 10px;
  height: calc(100vh - 120px); /* 占满页面主要可视高度 */
  display: flex;
  flex-direction: column;
}

/* 上半部分：搜索栏 + 表格 */
.top-section {
  flex: 1.3; /* 上部分比例稍大 */
  overflow-y: auto;
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 20px;
}

.search-box {
  width: 300px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

/* 下半部分：借用申请消息流 */
.bottom-section {
  flex: 0.8; /* 控制下方区域比例（可调 0.6～1） */
  border-top: 1px solid #e5e5e5;
  padding-top: 15px;
  overflow-y: auto; /* 多条消息时滚动 */
  min-height: 220px; /* 保证能容纳两条卡片 */
}

.bottom-section h3 {
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.message-card {
  margin-bottom: 10px;
  border-radius: 8px;
  height: 100px; /* 固定每条消息高度，让两条刚好显示 */
  overflow: hidden;
}

.message-header {
  font-size: 15px;
  margin-bottom: 6px;
}

.device-name {
  color: #409eff;
}

.message-body p {
  margin: 4px 0;
  font-size: 14px;
  color: #606266;
}
</style>
