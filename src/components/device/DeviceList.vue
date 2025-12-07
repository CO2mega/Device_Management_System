<template>
  <div class="device-list-main-view"> 
    
    <div class="top-header-controls">
      <h2 class="page-title">设备列表</h2>
    </div>
    
    <el-row :gutter="20" class="content-split-area">
      
      <el-col :span="17" class="table-content-block">
          
          <el-table
            :data="devices"
            :key="tableKey"
             border
             style="width: 100%;"
             class="styled-table glass-table"
           >
            <el-table-column prop="id" label="设备编号" width="100"></el-table-column>
            <el-table-column prop="name" label="设备名称" min-width="150"></el-table-column>
            <el-table-column prop="location" label="存放位置" min-width="150"></el-table-column>
            
            <el-table-column prop="borrowed" label="是否借出" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="row.borrowed === '否' ? 'success' : 'warning'" class="borrow-tag">
                  {{ row.borrowed === '是' ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="borrowDate" label="借出日期" width="140" align="center">
              <template #default="{ row }">
                {{ row.borrowed === '是' ? row.borrowDate : '-' }}
              </template>
            </el-table-column>

            <el-table-column prop="purchaseDate" label="购入日期" width="140"></el-table-column>
            
            <el-table-column label="操作" align="center" width="160">
              <template #default="{ row }">
                <el-button
                  size="mini"
                  class="edit-button-styled"
                  @click="handleEdit(row)"
                >
                  编辑
                </el-button>
                <el-button
                  size="mini"
                  class="delete-button-styled"
                  @click="handleDelete(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              background
              layout="prev, pager, next, jumper, total"
              :page-size="pageSize"
              :total="totalElements"
               :current-page.sync="currentPage"
               @current-change="handlePageChange"
               class="styled-pagination"
             />
          </div>
      </el-col>
      
      <el-col :span="7">
        <div class="add-panel">
          <h2 class="add-title">添加设备</h2>
          <el-form :model="addForm" label-width="80px" label-position="top">
            
            <el-form-item label="设备名称" required>
              <el-input v-model="addForm.name" placeholder="请输入设备名称" />
            </el-form-item> 

            <el-form-item label="是否借出" required>
              <el-select v-model="addForm.borrowed" placeholder="请选择" style="width: 100%">
                <el-option label="是" value="是" />
                <el-option label="否" value="否" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="借出日期" v-if="addForm.borrowed === '是'">
              <el-date-picker 
                v-model="addForm.borrowDate" 
                type="date" 
                placeholder="选择借出日期" 
                style="width: 100%"
                value-format="yyyy-MM-dd"
              />
            </el-form-item>

            <el-form-item label="存放位置">
              <el-input v-model="addForm.location" placeholder="请输入存放位置" />
            </el-form-item>
            
            <el-form-item label="购入日期">
              <el-date-picker 
                v-model="addForm.purchaseDate" 
                type="date" 
                placeholder="选择购入日期" 
                style="width: 100%"
                value-format="yyyy-MM-dd"
              />
            </el-form-item>
            
            <el-form-item class="submit-button-item">
              <el-button 
                type="primary" 
                @click="submitAdd" 
                class="submit-add-button-styled"
              >
                提交添加
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
      
    </el-row>
    
    <el-dialog
      title="编辑设备信息"
      :visible.sync="editDialogVisible"
      width="500px"
      class="styled-dialog"
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="设备名称"><el-input v-model="editForm.name" /></el-form-item>
        <el-form-item label="设备类型"><el-input v-model="editForm.type" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" placeholder="请选择状态">
            <el-option label="正常" value="正常" />
            <el-option label="故障" value="故障" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否借出">
          <el-select v-model="editForm.borrowed" placeholder="请选择">
            <el-option label="是" value="是" />
            <el-option label="否" value="否" />
          </el-select>
        </el-form-item>

        <el-form-item label="借出日期" v-if="editForm.borrowed === '是'">
          <el-date-picker 
            v-model="editForm.borrowDate" 
            type="date" 
            placeholder="选择借出日期" 
            value-format="yyyy-MM-dd" 
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="存放位置"><el-input v-model="editForm.location" /></el-form-item>

        <el-form-item label="购入日期">
          <el-date-picker 
            v-model="editForm.purchaseDate" 
            type="date" 
            placeholder="选择日期" 
            value-format="yyyy-MM-dd" 
            style="width: 100%"
          />
        </el-form-item>

      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" class="edit-save-button">保存</el-button>
      </div>
    </el-dialog>
    
  </div>
</template>

<script>
import { deviceApi } from '@/api/services';

export default {
  name: "DeviceList",
  data() {
    return {
      pageSize: 10,
      currentPage: 1,
      totalElements: 0,
      editDialogVisible: false,
      editForm: {},
      addForm: { name: "", type: "", status: "正常", borrowed: "否", location: "", purchaseDate: "", borrowDate: "" },
      devices: [],
      tableKey: 0,
      loading: false
    };
  },
  computed: {
  },
  mounted() {
    this.fetchDevices();
  },
  methods: {
    async fetchDevices(retry = true) {
      this.loading = true;
      try {
        const params = {
          page: this.currentPage - 1,
          size: this.pageSize
        };
        console.debug('fetchDevices request params:', params);
        const response = await deviceApi.getAll(params);
        const data = response.data;
        console.debug('fetchDevices response:', data);

        // support Page or raw array
        const items = Array.isArray(data) ? data : (data && data.content ? data.content : []);
        const totalReported = (data && typeof data.totalElements !== 'undefined') ? data.totalElements : (Array.isArray(data) ? data.length : 0);
        const lastPage = Math.max(1, Math.ceil(totalReported / this.pageSize));

        if ((!items || items.length === 0) && totalReported > 0 && retry) {
          // server reports there are items but returned empty page -> try to correct page
          // if current page > lastPage, set to lastPage; otherwise try first page as fallback
          this.currentPage = this.currentPage > lastPage ? lastPage : 1;
          await this.fetchDevices(false);
        }

        if (!items || items.length === 0) {
          this.devices = [];
          this.totalElements = totalReported;
          // inform user if there are no items at all
          if (totalReported === 0) {
            // no items
          } else {
            // no items for this page after retrying
            this.$message.warning('当前页暂无数据，已自动调整分页。');
          }
        } else {
          this.devices = items.map(device => ({
           id: device.id,
           name: device.name,
           type: device.type || '',
           status: device.status || '正常',
           borrowed: device.isLoaned ? '是' : '否',
           location: device.location || '',
           purchaseDate: device.purchaseDate || '',
           borrowDate: device.loanDate || ''
         }));

          this.totalElements = totalReported;
        }

        // force table rerender to avoid layout glitches
        this.tableKey += 1;

        // fix page if out of range
        const last = Math.max(1, Math.ceil(this.totalElements / this.pageSize));
        if (this.currentPage > last && retry) {
          this.currentPage = last;
          await this.fetchDevices(false);
        }
      } catch (error) {
        console.error('Failed to fetch devices:', error);
        this.$message.error('获取设备列表失败');
      } finally {
        this.loading = false;
      }
    },
    handleEdit(row) {
      this.editForm = { ...row };
      this.editDialogVisible = true;
    },
    async submitEdit() {
      try {
        if (this.editForm.borrowed === '否') {
          this.editForm.borrowDate = "";
        }
        await deviceApi.update(this.editForm.id, {
          name: this.editForm.name,
          type: this.editForm.type,
          status: this.editForm.status,
          location: this.editForm.location,
          purchaseDate: this.editForm.purchaseDate || null,
          isLoaned: this.editForm.borrowed === '是',
          loanDate: this.editForm.borrowDate || null
        });
        this.$message.success("修改成功！");
        this.editDialogVisible = false;
        this.fetchDevices();
      } catch (error) {
        console.error('Failed to update device:', error);
        this.$message.error('修改失败：' + (error.response?.data?.message || error.message));
      }
    },
    handleDelete(row) {
      this.$confirm(`确定删除设备【${row.name}】吗？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(async () => {
        try {
          await deviceApi.delete(row.id);
          this.$message.success("删除成功");
          this.fetchDevices();
        } catch (error) {
          console.error('Failed to delete device:', error);
          this.$message.error('删除失败：' + (error.response?.data?.message || error.message));
        }
      }).catch(() => {});
    },
    handlePageChange(page) {
      console.debug('page changed to', page);
       this.currentPage = page;
       this.fetchDevices();
    },
    async submitAdd() {
      if (!this.addForm.name) {
          this.$message.error("设备名称为必填项！");
          return;
      }

      try {
        await deviceApi.create({
          name: this.addForm.name,
          type: this.addForm.type || "其他",
          status: this.addForm.status || "正常",
          location: this.addForm.location,
          purchaseDate: this.addForm.purchaseDate || null
        });
        this.$message.success("添加成功！");
        this.addForm = { name: "", type: "", status: "正常", borrowed: "否", location: "", purchaseDate: "", borrowDate: "" };
        this.fetchDevices();
      } catch (error) {
        console.error('Failed to create device:', error);
        this.$message.error('添加失败：' + (error.response?.data?.message || error.message));
      }
    }
  }
};
</script>

<style scoped>
/* 主题色变量（局部替代，组件样式内使用） */
:root {
  --tech-cyan: #00c0ff;
  --tech-violet: #7a8cff;
  --deep-blue: #062238;
}

/* -------------------- 整体容器和顶部控制 -------------------- */
/* 将根容器名称改为 device-list-main-view 以匹配样式习惯 */
.device-list-main-view {
  width: 100%;
  height: 100%;
  padding: 20px; /* 增加外部 padding，让内容块与边缘有间距 */
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

/* -------------------- 下方分栏区域 -------------------- */
.content-split-area {
    flex-grow: 1; 
}

/* 移除 el-col 上的 wrapper 样式，让表格紧贴列空间 */
.table-content-block {
    display: flex;
    flex-direction: column;
    padding: 0 !important; 
    min-height: 320px; /* ensure table area doesn't collapse */
}


/* ========================= 左侧：设备列表表格 (高透明度玻璃化) ========================= */

.styled-table.glass-table {
    border-radius: 15px;
    overflow: hidden;
    background-color: rgba(255, 255, 255, 0.38) !important;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(122, 140, 255, 0.12);
    box-shadow: 0 8px 30px rgba(122, 140, 255, 0.06);
    flex-grow: 1;
    min-height: 220px; /* give table area a minimum height */
}

.styled-table /deep/ .el-table__header-wrapper th {
    background-color: rgba(255, 255, 255, 0.85) !important;
    color: var(--deep-blue);
    font-weight: 600;
    border-color: rgba(6,34,56,0.06);
}

/* 核心修改：确保表格行背景透明 */
.styled-table /deep/ .el-table__row {
    background-color: transparent !important; 
}

/* 数据行悬停效果 */
.styled-table /deep/ .el-table__row:hover {
    background: linear-gradient(90deg, rgba(0,192,255,0.04), rgba(122,140,255,0.03)) !important;
}

/* Tag 风格：在线为青色，警告为紫色 */
.borrow-tag {
    border-radius: 12px;
    padding: 0 10px;
    font-weight: 600;
}
.borrow-tag .el-tag--success {
    background: linear-gradient(90deg, var(--tech-cyan), var(--tech-violet));
    color: #fff;
}

/* 分页容器 */
.pagination-container {
    margin-top: 15px; 
    text-align: right;
    display: flex;
    justify-content: flex-end; 
}


/* ========================= 右侧：添加表单区 (渐变背景+玻璃输入框) ========================= */
.add-panel {
  /* 保持柔和渐变 */
  background: linear-gradient(135deg, rgba(0,192,255,0.12) 0%, rgba(122,140,255,0.14) 100%);
  padding: 20px;
  border-radius: 15px;
  box-shadow: 0 6px 20px rgba(122,140,255,0.06);
  height: 100%;
}

/* 右侧表单标题增强 */
.add-title {
    color: #fff; /* 标题颜色改为白色 */
    font-size: 20px;
    font-weight: bold; 
    text-shadow: 0 1px 2px rgba(0,0,0,0.15); 
    margin-bottom: 20px;
}
/* 核心修改：右侧表单标签颜色改为白色 */
.add-panel /deep/ .el-form-item__label {
    color: #fff;
}

/* 右侧表单输入框/选择框 - 玻璃化 */
.add-panel /deep/ .el-input__inner,
.add-panel /deep/ .el-textarea__inner,
.add-panel /deep/ .el-select .el-input__inner,
.add-panel /deep/ .el-date-editor.el-input,
.add-panel /deep/ .el-date-editor.el-input__inner {
    background-color: rgba(255,255,255,0.92) !important;
    border-radius: 8px;
    border: 1px solid rgba(6,34,56,0.06);
}

/* 确保 el-select 的 dropdown 背景也透明化，保持一致性 */
.el-popper /deep/ .el-select-dropdown {
    background-color: rgba(255, 255, 255, 0.9) !important;
    backdrop-filter: blur(5px);
    border-radius: 10px;
}
.el-popper /deep/ .el-select-dropdown__item {
    color: #333;
}


/* -------------------- 按钮/搜索/分页细节样式 (采用柔和紫色作为主色调) -------------------- */
.page-title {
  font-size: 22px;
  color: var(--deep-blue);
  margin: 0;
}

/* 编辑按钮 - 柔和紫作为主操作色 */
.edit-button-styled {
  background: linear-gradient(90deg, var(--tech-cyan), var(--tech-violet));
  color: #fff;
  border: none;
}
/* 删除按钮 - 柔和蓝色 */
.delete-button-styled { 
    background: rgba(255, 80, 80, 0.12);
    color: #d64545;
    border: 1px solid rgba(214,69,69,0.12);
}

/* 提交添加按钮 - 柔和紫 */
.submit-add-button-styled {
    width: 100%;
    margin-top: 10px;
    border-radius: 10px;
    background-color: #9575CD !important; 
    border-color: #9575CD !important;
    font-weight: bold;
    box-shadow: 0 4px 10px rgba(149, 117, 205, 0.4); 
}
</style>