<template>
  <div class="return-records-view">
    
    <div class="top-header-controls">
      <h2 class="page-title">设备归还记录</h2>
      
      <div class="actions-group">
        <el-input
          placeholder="搜索设备名称、借用人或流水号 (当前禁用)"
          disabled
          clearable
          class="search-box-styled"
          prefix-icon="el-icon-search"
        >
          <i slot="prefix" class="el-input__icon el-icon-search search-icon-prefix"></i>
        </el-input>
      </div>
    </div>
    
    <el-row :gutter="20" class="content-split-area">
      
      <el-col :span="17" class="main-content-block">
        
        <div class="summary-cards-wrapper">
          <div class="summary-item glass-card-summary">
            <p>总归还记录</p>
            <span class="count info-text">{{ summary.totalCount }}</span>
          </div>
          <div class="summary-item glass-card-summary">
            <p>正常归还</p>
            <span class="count success-text">{{ summary.normalCount }}</span>
          </div>
          <div class="summary-item glass-card-summary">
            <p>逾期归还</p>
            <span class="count warning-text">{{ summary.overdueCount }}</span>
          </div>
        </div>
        
        <div class="table-record-area">
          <h3 class="table-title">历史归还详情</h3>
          <el-table
            :data="pagedRecords"
            :border="false"
            style="width: 100%;"
            class="styled-table glass-table records-table"
          >
            <el-table-column prop="id" label="流水号" width="90"></el-table-column>
            <el-table-column prop="deviceName" label="设备名称" min-width="150"></el-table-column>
            <el-table-column prop="borrower" label="借用人" width="100"></el-table-column>
            <el-table-column prop="borrowDate" label="借出时间" width="130"></el-table-column>
            <el-table-column prop="expectedReturnDate" label="应归还日期" width="130"></el-table-column>
            <el-table-column prop="actualReturnDate" label="实际归还时间" width="130"></el-table-column>
            
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template slot-scope="scope">
                <el-tag :type="getStatusTagType(scope.row.status)" class="status-tag">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="操作" align="center" width="80">
              <template slot-scope="scope">
                <el-button 
                  size="mini" 
                  type="text" 
                  class="purple-text-button" 
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
              class="styled-pagination"
            />
          </div>
        </div>
      </el-col>
      
      <el-col :span="7">
        <div class="right-panel gradient-panel">
          
          <h3 class="panel-title-white timeline-title">⏳ 近期归还时间轴</h3>
          <div class="timeline-scroll-wrapper">
            <el-timeline :reverse="true">
              <el-timeline-item
                v-for="(item, index) in recentReturns"
                :key="index"
                :timestamp="item.actualReturnDate"
                placement="top"
                :type="item.status === '逾期归还' ? 'warning' : 'success'"
              >
                <el-card class="timeline-card">
                  <h4>{{ item.borrower }} 归还了 {{ item.deviceName }}</h4>
                  <p>{{ item.status }} | 耗时 {{ item.duration }} 天</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>
          
        </div>
      </el-col>
    </el-row>
    
  </div>
</template>

<script>
import { defineComponent } from 'vue';
import { loanApi } from '@/api/services';

export default defineComponent({
  name: 'ReturnRecords',
  data() {
    return {
      pageSize: 8,
      currentPage: 1,
      allRecords: [],
      loading: false
    };
  },
  computed: {
    summary() {
      const totalCount = this.allRecords.length;
      const normalCount = this.allRecords.filter(r => r.status === '正常归还').length;
      const overdueCount = this.allRecords.filter(r => r.status === '逾期归还').length;
      return { totalCount, normalCount, overdueCount };
    },
    recentReturns() {
      return this.allRecords.slice(0, 5);
    },
    filteredRecords() {
      return this.allRecords;
    },
    pagedRecords() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredRecords.slice(start, end);
    },
  },
  mounted() {
    this.fetchReturnRecords();
  },
  methods: {
    async fetchReturnRecords() {
      this.loading = true;
      try {
        const response = await loanApi.getAll({ size: 100 });
        const data = response.data;
        
        // Filter only returned records (actualReturnDate is not null)
        this.allRecords = data.content
          .filter(record => record.actualReturnDate)
          .map(record => {
            const borrowDate = record.applyDate ? record.applyDate.substring(0, 10) : '';
            const actualReturnDate = record.actualReturnDate ? record.actualReturnDate.substring(0, 10) : '';
            
            // Calculate duration
            let duration = 0;
            if (borrowDate && actualReturnDate) {
              const start = new Date(borrowDate);
              const end = new Date(actualReturnDate);
              duration = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
            }
            
            return {
              id: record.id,
              deviceName: record.deviceName,
              borrower: record.applicantName,
              borrowDate: borrowDate,
              expectedReturnDate: record.expectedReturnDate || '',
              actualReturnDate: actualReturnDate,
              status: this.mapReturnStatus(record.returnStatus),
              duration: duration
            };
          })
          .sort((a, b) => new Date(b.actualReturnDate) - new Date(a.actualReturnDate));
      } catch (error) {
        console.error('Failed to fetch return records:', error);
        this.$message.error('获取归还记录失败');
      } finally {
        this.loading = false;
      }
    },
    mapReturnStatus(status) {
      switch (status) {
        case 'NORMAL': return '正常归还';
        case 'OVERDUE': return '逾期归还';
        default: return status || '正常归还';
      }
    },
    getStatusTagType(status) {
      switch (status) {
        case '正常归还': return 'success';
        case '逾期归还': return 'warning';
        default: return 'info';
      }
    },
    viewDetails(row) {
      console.log('Viewing details:', row);
      this.$message.info(`查看 ${row.deviceName} 的归还详情`);
    }
  }
});
</script>

<style scoped>
/* -------------------- 页面基础布局 -------------------- */
.return-records-view {
  width: 100%;
  height: 100%;
  padding: 20px;
  background-color: transparent;
  display: flex;
  flex-direction: column;
}

.top-header-controls {
  display: flex;
  justify-content: space-between; /* 确保搜索框在右侧 */
  align-items: center;
  margin-bottom: 20px;
}

/* 确保左侧主体块具有 flex 布局 */
.main-content-block {
  display: flex;
  flex-direction: column;
  padding: 0 !important;
}

/* -------------------- A. 左侧区域样式 - 统一风格 -------------------- */

/* 1. 概览卡片区样式 (与借用记录保持一致) */
.summary-cards-wrapper {
  display: flex;
  justify-content: space-between;
  gap: 15px; /* 与借用记录一致 */
  margin-bottom: 20px; 
}
.summary-item {
  flex: 1;
  text-align: center;
  padding: 15px 10px; /* 与借用记录一致 */
  border-radius: 12px; /* 与借用记录一致 */
}
.glass-card-summary {
  background-color: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(8px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}
.summary-item p { 
  color: #555; 
  margin: 0 0 5px 0; 
  font-size: 14px; 
  font-weight: 500; 
  opacity: 1; 
}
.summary-item .count { 
  font-size: 24px; 
  font-weight: bold; 
  margin-top: 0; 
} 
.warning-text { color: #FFC107 !important; } 
.success-text { color: #4CAF50 !important; } 
.info-text { color: #2196F3 !important; } 


/* 2. 表格区样式 - 无边框玻璃化 (与借用记录保持一致) */
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

.styled-table.glass-table {
  /* 保持高透明度玻璃化 */
  background-color: rgba(255, 255, 255, 0.5) !important; 
  backdrop-filter: blur(10px); 
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.05); 
  border-radius: 15px;
  overflow: hidden;
}

/* 移除表格内部和外部边框（与借用记录的样式保持一致） */
.styled-table /deep/ .el-table__header-wrapper th {
  background-color: transparent !important; 
  color: #333; 
  font-weight: 600;
  border-bottom: none !important; 
}
.styled-table /deep/ td, .styled-table /deep/ th.is-leaf {
  border-bottom: none !important;
}

/* 重新添加轻微的行分隔线（通过伪元素实现，与借用记录一致） */
.styled-table /deep/ .el-table__row td {
  position: relative;
}
.styled-table /deep/ .el-table__row td:after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px; 
  background-color: rgba(0, 0, 0, 0.05); 
}
.styled-table /deep/ .el-table__row:last-child td:after {
  display: none;
}

/* 行背景和悬停效果 */
.styled-table /deep/ .el-table__row {
  background-color: transparent !important; 
}
.styled-table /deep/ .el-table__row:hover {
  background-color: rgba(0, 0, 0, 0.03) !important; 
  cursor: pointer;
}

/* 操作按钮样式：紫色扁平按钮 (与借用记录一致) */
.purple-text-button { 
  color: #9575CD !important; 
  font-weight: bold;
}
.purple-text-button:hover { 
  color: #7E57C2 !important; 
}

.pagination-container {
  margin-top: 15px; 
  text-align: right;
  display: flex;
  justify-content: flex-end; 
}

/* -------------------- B. 右侧区域样式 (时间轴) - 统一高度 -------------------- */
.right-panel.gradient-panel {
  background: linear-gradient(135deg, #bdcef8 0%, #f9b2f6 100%); 
  padding: 20px;
  border-radius: 15px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  flex-grow: 1; 
  display: flex;
  flex-direction: column;
}

.panel-title-white.timeline-title {
  color: #fff; 
  font-size: 18px;
  font-weight: bold; 
  text-shadow: 0 1px 2px rgba(0,0,0,0.15); 
  margin-bottom: 20px;
  flex-shrink: 0; 
}

.timeline-scroll-wrapper {
  flex-grow: 1; 
  /* 确保外层时间轴容器可以滚动，以便显示所有卡片 */
  max-height: calc(100vh - 200px); 
  overflow-y: auto; 
  padding-right: 10px; 
}

/* 滚动条美化 (保持不变) */
.timeline-scroll-wrapper::-webkit-scrollbar {
  width: 6px;
}
.timeline-scroll-wrapper::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.4);
  border-radius: 3px;
}
.timeline-scroll-wrapper::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.6);
}

/* === 核心修改区域：时间轴卡片样式调整，实现内部滚动 === */
.timeline-card {
  background-color: rgba(255, 255, 255, 0.85); 
  backdrop-filter: blur(5px);
  border: none;
  border-radius: 8px;
  padding: 10px 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  /* 启用内部滚动 */
  max-height: 120px; /* 设置一个最大高度，根据实际需要调整 */
  overflow-y: auto; /* 允许垂直滚动 */
}

/* 确保 El-Card 的 body 也不影响滚动 */
.timeline-card /deep/ .el-card__body {
    padding: 0; /* 移除 El-Card 默认 padding，由 .timeline-card 统一控制 */
}

.timeline-card h4 {
    margin-top: 0;
    margin-bottom: 5px;
    font-size: 14px;
    color: #333;
}

.timeline-card p {
    margin: 0;
    font-size: 12px;
    color: #666;
}

/* 搜索框样式（用于占位，保持外观一致） */
.search-box-styled {
  width: 250px; /* 给出搜索框一个宽度，与借用记录一致 */
}
.search-box-styled /deep/ .el-input__inner {
  background-color: rgba(255, 255, 255, 0.3) !important; 
  border: 1px solid rgba(255, 255, 255, 0.6); 
  color: #333; 
  border-radius: 8px; 
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
}
.search-box-styled[disabled] /deep/ .el-input__inner {
  opacity: 0.6;
}
</style>