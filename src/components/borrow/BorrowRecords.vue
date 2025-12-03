<template>
  <div class="borrow-out-main-view">
    
    <div class="top-header-controls">
      <h2 class="page-title">借用申请记录</h2>
      <div class="actions-group">
        <el-input
          v-model="search"
          placeholder="搜索设备名称、申请人或流水号"
          clearable
          class="search-box-styled"
          prefix-icon="el-icon-search"
          @input="handleSearch"
        >
          <i slot="prefix" class="el-input__icon el-icon-search search-icon-prefix"></i>
        </el-input>
      </div>
    </div>
    
    <el-row :gutter="20" class="content-split-area">
      
      <el-col :span="17" class="main-content-block">
        
        <div class="summary-cards-wrapper">
             <div class="summary-item glass-card-summary">
                <p>待处理申请</p>
                <span class="count warning-text">{{ summary.pendingCount }}</span>
             </div>
             <div class="summary-item glass-card-summary">
                <p>已批准记录</p>
                <span class="count success-text">{{ summary.approvedCount }}</span>
             </div>
             <div class="summary-item glass-card-summary">
                <p>已驳回记录</p>
                <span class="count danger-text">{{ summary.rejectedCount }}</span>
             </div>
        </div>
        
        <div class="table-record-area">
          <h3 class="table-title">全部申请记录</h3>
          <el-table
            :data="pagedRecords"
            border
            style="width: 100%;"
            class="styled-table glass-table records-table"
          >
            <el-table-column prop="id" label="流水号" width="100"></el-table-column>
            <el-table-column prop="deviceName" label="设备名称" min-width="150"></el-table-column>
            <el-table-column prop="applicant" label="申请人" width="120"></el-table-column>
            <el-table-column prop="applicationDate" label="申请日期" width="140"></el-table-column>
            <el-table-column prop="expectedReturnDate" label="预计归还" width="140"></el-table-column>
            
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template slot-scope="scope">
                <el-tag :type="getStatusTagType(scope.row.status)" class="status-tag">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="操作" align="center" width="100">
              <template slot-scope="scope">
                <el-button 
                  size="mini" 
                  type="text" 
                  class="edit-button-styled" 
                  @click="viewDetails(scope.row)"
                >
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              background
              layout="prev, pager, next, jumper, total"
              :page-size="pageSize"
              :total="filteredRecords.length"
              :current-page.sync="currentPage"
              @current-change="handlePageChange"
              class="styled-pagination"
            />
          </div>
        </div>
      </el-col>
      
      <el-col :span="7">
        <div class="pending-applications-panel">
          <h3 class="panel-title-white">🚨 待处理申请 ({{ pendingApplications.length }}条)</h3>
          
          <div class="pending-cards-vertical-wrapper">
            <div 
              v-for="app in pendingApplications" 
              :key="app.id" 
              class="pending-card-vertical glass-card-pending"
            >
              <div class="card-header-vertical">
                <span class="card-id">#{{ app.id }}</span>
                <el-tag size="small" type="warning" class="status-tag">待处理</el-tag>
              </div>
              <p class="card-content-vertical">
                {{ app.applicant }}申请{{ app.deviceName }}。
              </p>
              <p class="card-date-vertical">
                预计归还: {{ app.expectedReturnDate }}
              </p>
              <div class="card-actions-vertical">
                <el-button size="mini" type="success" @click="handleApprove(app)">批准</el-button>
                <el-button size="mini" type="danger" @click="handleReject(app)">驳回</el-button>
              </div>
            </div>
            
            <div v-if="pendingApplications.length === 0" class="empty-placeholder-white">
              🎉 当前没有待处理的借用申请。
            </div>
          </div>
          
        </div>
      </el-col>
      
    </el-row>
    
    <el-dialog
      title="申请详情"
      :visible.sync="detailDialogVisible"
      width="500px"
      class="styled-dialog"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="流水号">{{ currentDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentDetail.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentDetail.applicant }}</el-descriptions-item>
        <el-descriptions-item label="申请日期">{{ currentDetail.applicationDate }}</el-descriptions-item>
        <el-descriptions-item label="预计归还">{{ currentDetail.expectedReturnDate }}</el-descriptions-item>
        <el-descriptions-item label="申请理由">{{ currentDetail.reason }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(currentDetail.status)">{{ currentDetail.status }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button 
            v-if="currentDetail.status === '待处理'" 
            type="success" 
            @click="handleApprove(currentDetail)"
        >
            批准
        </el-button>
      </div>
    </el-dialog>
    
  </div>
</template>

<script>
// (保持 <script> 部分不变，因为它负责数据和逻辑)
export default {
  name: "BorrowOut",
  data() {
    return {
      search: "",
      pageSize: 8,
      currentPage: 1,
      detailDialogVisible: false,
      currentDetail: {},
      filterForm: {
        status: "",
        dateRange: null, // [start, end]
      },
      // 模拟数据
      allRecords: [
        { id: 1001, deviceName: "温度传感器", applicant: "张三", applicationDate: "2025-11-28", expectedReturnDate: "2025-12-15", status: "待处理", reason: "用于新产品测试" },
        { id: 1002, deviceName: "压力测试仪", applicant: "李四", applicationDate: "2025-11-27", expectedReturnDate: "2025-12-10", status: "已批准", reason: "例行设备校准" },
        { id: 1003, deviceName: "信号发生器", applicant: "王五", applicationDate: "2025-11-26", expectedReturnDate: "2025-12-20", status: "已驳回", reason: "设备类型不匹配" },
        { id: 1004, deviceName: "网络交换机", applicant: "赵六", applicationDate: "2025-11-25", expectedReturnDate: "2025-12-05", status: "待处理", reason: "临时搭建测试环境" },
        { id: 1005, deviceName: "激光测距仪", applicant: "孙七", applicationDate: "2025-11-20", expectedReturnDate: "2025-12-01", status: "已批准", reason: "工程测量" },
        { id: 1006, deviceName: "万用表", applicant: "周八", applicationDate: "2025-11-15", expectedReturnDate: "2025-12-08", status: "已批准", reason: "日常维修" },
        { id: 1007, deviceName: "示波器", applicant: "吴九", applicationDate: "2025-11-10", expectedReturnDate: "2025-12-01", status: "待处理", reason: "高精度信号分析" },
        { id: 1008, deviceName: "电源模块", applicant: "郑十", applicationDate: "2025-11-05", expectedReturnDate: "2025-11-30", status: "已驳回", reason: "库存不足" },
        // 更多数据
        { id: 1009, deviceName: "环境监测仪", applicant: "冯十一", applicationDate: "2025-10-28", expectedReturnDate: "2025-11-25", status: "已批准", reason: "长期环境监控" },
        { id: 1010, deviceName: "电压表", applicant: "陈十二", applicationDate: "2025-10-20", expectedReturnDate: "2025-11-10", status: "已批准", reason: "车间巡检" },
        { id: 1011, deviceName: "摄像头模块", applicant: "卫十三", applicationDate: "2025-10-15", expectedReturnDate: "2025-11-01", status: "已批准", reason: "安防系统升级" },
      ].sort((a, b) => b.id - a.id) // 按ID倒序
    };
  },
  computed: {
    // 待处理申请 (用于卡片显示)
    pendingApplications() {
      return this.allRecords.filter(r => r.status === '待处理').slice(0, 5); // 只显示前5条
    },
    // 计算属性: 根据搜索和筛选条件过滤记录 (注意：虽然移除了筛选栏，但仍保留了内部筛选逻辑，只依赖全局搜索)
    filteredRecords() {
      let records = this.allRecords;
      const lowerSearch = this.search.toLowerCase();
      
      // 1. 全局搜索
      if (lowerSearch) {
        records = records.filter(r => 
          String(r.id).includes(lowerSearch) || 
          r.deviceName.toLowerCase().includes(lowerSearch) || 
          r.applicant.toLowerCase().includes(lowerSearch)
        );
      }
      
      // 注意：这里移除了对 filterForm.status 和 filterForm.dateRange 的依赖，
      // 因为前端没有 UI 元素来设置它们了。如果需要，您可以通过编程方式设置 filterForm。

      return records;
    },
    // 当前页显示的记录
    pagedRecords() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredRecords.slice(start, end);
    },
    // 统计概览数据
    summary() {
      const pendingCount = this.allRecords.filter(r => r.status === '待处理').length;
      const approvedCount = this.allRecords.filter(r => r.status === '已批准').length;
      const rejectedCount = this.allRecords.filter(r => r.status === '已驳回').length;
      return { pendingCount, approvedCount, rejectedCount };
    }
  },
  methods: {
    handleSearch() {
      this.currentPage = 1;
    },
    handlePageChange(page) {
      this.currentPage = page;
    },
    // 移除了 applyFilter 方法，因为筛选 UI 已移除
    getStatusTagType(status) {
      switch (status) {
        case '待处理': return 'warning';
        case '已批准': return 'success';
        case '已驳回': return 'danger';
        default: return 'info';
      }
    },
    // 查看详情
    viewDetails(row) {
      this.currentDetail = { ...row };
      this.detailDialogVisible = true;
    },
    // 批准操作
    handleApprove(app) {
      this.$confirm(`确定批准 ${app.applicant} 借用 ${app.deviceName} 吗？`, "批准申请", {
        confirmButtonText: "确定批准",
        cancelButtonText: "取消",
        type: "success"
      }).then(() => {
        const index = this.allRecords.findIndex(r => r.id === app.id);
        if (index !== -1) {
          this.allRecords[index].status = '已批准';
          this.$message.success("申请已批准并记录！");
          this.detailDialogVisible = false;
        }
      }).catch(() => {});
    },
    // 驳回操作
    handleReject(app) {
      this.$confirm(`确定驳回 ${app.applicant} 借用 ${app.deviceName} 的申请吗？`, "驳回申请", {
        confirmButtonText: "确定驳回",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        const index = this.allRecords.findIndex(r => r.id === app.id);
        if (index !== -1) {
          this.allRecords[index].status = '已驳回';
          this.$message.warning("申请已驳回。");
          this.detailDialogVisible = false;
        }
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
/* -------------------- 页面基础布局 (保持不变) -------------------- */
.borrow-out-main-view {
  width: 100%;
  height: 100%;
  padding: 20px;
  background-color: transparent;
  display: flex;
  flex-direction: column;
}

.top-header-controls {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.content-split-area {
    flex-grow: 1;
}

.main-content-block {
    display: flex;
    flex-direction: column;
    padding: 0 !important;
}

/* -------------------- A. 左侧区域新增/修改样式 -------------------- */

/* 1. 概览卡片区样式 (保持不变) */
.summary-cards-wrapper {
    display: flex;
    justify-content: space-between;
    gap: 15px;
    margin-bottom: 20px; 
}
.summary-item {
    flex: 1;
    text-align: center;
    padding: 15px 10px;
    border-radius: 12px;
}
.glass-card-summary {
    background-color: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(8px);
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}
.summary-item p { color: #555; margin: 0 0 5px 0; font-size: 14px; font-weight: 500; }
.summary-item .count { font-size: 24px; font-weight: bold; }
.warning-text { color: #FFC107; } 
.success-text { color: #4CAF50; } 
.danger-text { color: #F44336; } 


/* 2. 表格区样式 - 核心修改区域 */
.table-record-area {
    flex-grow: 1;
    display: flex;
    flex-direction: column;
}
.table-title {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin-bottom: 15px;
    padding-left: 5px;
}

/* 移除表格整体边框，并调整背景和圆角 */
.styled-table.glass-table {
    /* 保持高透明度玻璃化，但提高透明度，使其更柔和 */
    background-color: rgba(255, 255, 255, 0.5) !important; 
    backdrop-filter: blur(10px); 
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.05); 
    border-radius: 15px;
    overflow: hidden;
}

/* 移除表格内部和外部边框 */
.styled-table /deep/ .el-table__body-wrapper {
    /* 移除 body 的边框 */
    border: none;
}
.styled-table /deep/ .el-table__header-wrapper th {
    /* 表头背景和文字颜色 */
    background-color: transparent !important; /* 使表头也透明 */
    color: #333; 
    font-weight: 600;
    /* 移除表头底部分割线 */
    border-bottom: none; 
}

/* 移除行之间的横向分割线 */
.styled-table /deep/ td, .styled-table /deep/ th.is-leaf {
    border-bottom: none !important;
}

/* 重新添加轻微的行分隔线（通过伪元素或 box-shadow 实现更柔和的分隔效果） */
/* Element UI 表格行分隔线通常由 td 样式控制，如果直接设置 none，则需要使用伪元素重新添加 */
.styled-table /deep/ .el-table__row td {
    position: relative;
}
.styled-table /deep/ .el-table__row td:after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 1px; /* 极细的线 */
    background-color: rgba(0, 0, 0, 0.05); /* 柔和的浅色分割线 */
}

/* 确保表格的最后一行没有底部分割线 */
.styled-table /deep/ .el-table__row:last-child td:after {
    display: none;
}

/* 行背景 */
.styled-table /deep/ .el-table__row {
    background-color: transparent !important; 
}

/* 数据行悬停效果 */
.styled-table /deep/ .el-table__row:hover {
    /* 悬停时使用更浅的背景色 */
    background-color: rgba(0, 0, 0, 0.03) !important; 
    cursor: pointer;
}

/* 操作按钮样式：改为紫色扁平按钮 */
.table-record-area /deep/ .el-button--text {
    /* 基础按钮颜色（查看） */
    color: #9575CD; /* 统一使用紫色 */
    font-weight: bold;
}
.table-record-area /deep/ .el-button--text:hover {
    color: #7E57C2; /* 悬停时深一点 */
}


.pagination-container {
    margin-top: 15px; 
    text-align: right;
    display: flex;
    justify-content: flex-end; 
}


/* -------------------- B. 右侧区域样式 (待处理卡片区) -------------------- */
/* 保持不变 */
.pending-applications-panel {
    background: linear-gradient(135deg, #bdcef8 0%, #f9b2f6 100%); 
    padding: 20px;
    border-radius: 15px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
    height: 100%; 
    display: flex;
    flex-direction: column;
    gap: 15px;
    overflow-y: auto; 
}
.panel-title-white {
    color: #fff; 
    font-size: 18px;
    font-weight: bold; 
    text-shadow: 0 1px 2px rgba(0,0,0,0.15); 
    margin-bottom: 5px;
    flex-shrink: 0; 
}
.pending-cards-vertical-wrapper {
    display: flex;
    flex-direction: column;
    gap: 10px;
}
.pending-card-vertical {
    padding: 12px;
    border-radius: 10px;
    background-color: rgba(255, 255, 255, 0.85); 
    backdrop-filter: blur(5px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* -------------------- 按钮/输入框样式继承 -------------------- */
.search-box-styled /deep/ .el-input__inner {
    background-color: rgba(255, 255, 255, 0.3) !important; 
    border: 1px solid rgba(255, 255, 255, 0.6); 
    color: #333; 
    border-radius: 8px; 
    box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
}
</style>