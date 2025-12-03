<template>
  <div class="user-list-main-view">
    
    <div class="top-header-controls">
      <h2 class="page-title">用户列表</h2>
      
      <div class="actions-group">
        <el-input
          v-model="search"
          placeholder="请输入工号或用户名搜索"
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
      
      <el-col :span="17" class="table-content-block">
          
          <el-table
            :data="filteredUsers.slice((currentPage-1)*pageSize, currentPage*pageSize)"
            border
            style="width: 100%;"
            class="styled-table glass-table"
            v-loading="loading"
          >
            <el-table-column prop="staffId" label="工号" width="100"></el-table-column>
            <el-table-column prop="username" label="用户名" width="120"></el-table-column>
            <el-table-column prop="role" label="角色" width="100" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : 'success'" class="borrow-tag">
                  {{ scope.row.role === 'ADMIN' ? '管理员' : '普通用户' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="email" label="邮箱" min-width="180"></el-table-column>
            <el-table-column prop="phone" label="手机号" width="140"></el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'warning'" class="borrow-tag">
                  {{ scope.row.status === 'ACTIVE' ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column label="操作" align="center" width="160">
              <template slot-scope="scope">
                <el-button 
                  size="mini" 
                  class="edit-button-styled" 
                  @click="handleEdit(scope.row)"
                >
                  编辑
                </el-button>
                <el-button 
                  size="mini" 
                  class="delete-button-styled" 
                  @click="handleDelete(scope.row)"
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
          <h2 class="add-title">添加用户</h2>
          <el-form :model="addForm" label-width="80px" label-position="top">
            
            <el-form-item label="工号" required>
              <el-input v-model="addForm.staffId" placeholder="请输入用户工号" />
            </el-form-item>

            <el-form-item label="用户名" required>
              <el-input v-model="addForm.username" placeholder="请输入用户名" />
            </el-form-item>

            <el-form-item label="密码" required>
              <el-input v-model="addForm.password" type="password" placeholder="请输入密码" />
            </el-form-item>

            <el-form-item label="角色">
              <el-select v-model="addForm.role" placeholder="请选择角色" style="width: 100%">
                <el-option label="普通用户" value="USER" />
                <el-option label="管理员" value="ADMIN" />
              </el-select>
            </el-form-item>

            <el-form-item label="邮箱">
              <el-input v-model="addForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            
            <el-form-item label="手机号">
              <el-input v-model="addForm.phone" placeholder="请输入手机号" />
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
      title="编辑用户信息"
      :visible.sync="editDialogVisible"
      width="500px"
      class="styled-dialog"
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="工号"><el-input v-model="editForm.staffId" disabled /></el-form-item>
        <el-form-item label="用户名"><el-input v-model="editForm.username" /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="editForm.password" type="password" placeholder="不修改请留空" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" placeholder="请选择角色" style="width: 100%;">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" placeholder="请选择状态" style="width: 100%;">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="禁用" value="LOCKED" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱"><el-input v-model="editForm.email" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="editForm.phone" /></el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" class="edit-save-button">保存</el-button>
      </div>
    </el-dialog>
    
  </div>
</template>

<script>
import { userApi } from '@/api/services';

export default {
  name: "UserList",
  data() {
    return {
      search: "",
      pageSize: 10,
      currentPage: 1,
      totalElements: 0,
      editDialogVisible: false,
      addDialogVisible: false,
      editForm: {},
      addForm: {
        staffId: "",
        username: "",
        password: "",
        role: "USER",
        email: "",
        phone: ""
      },
      users: [],
      loading: false
    };
  },
  computed: {
    filteredUsers() {
      if (!this.search) return this.users;
      const lowerSearch = this.search.toLowerCase();
      return this.users.filter(u => 
        (u.username && u.username.toLowerCase().includes(lowerSearch)) || 
        (u.staffId && u.staffId.toLowerCase().includes(lowerSearch))
      );
    }
  },
  mounted() {
    this.fetchUsers();
  },
  methods: {
    async fetchUsers() {
      this.loading = true;
      try {
        const response = await userApi.getAll({
          search: this.search || undefined,
          page: this.currentPage - 1,
          size: this.pageSize
        });
        const data = response.data;
        this.users = data.content.map(user => ({
          id: user.id,
          staffId: user.staffId,
          username: user.username,
          role: user.role,
          status: user.status,
          email: user.email || '',
          phone: user.phone || '',
          createdAt: user.createdAt
        }));
        this.totalElements = data.totalElements;
      } catch (error) {
        console.error('Failed to fetch users:', error);
        this.$message.error('获取用户列表失败');
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.currentPage = 1;
      this.fetchUsers();
    },
    handleEdit(row) {
      this.editForm = { ...row, password: '' };
      this.editDialogVisible = true;
    },
    async submitEdit() {
      try {
        const updateData = {
          staffId: this.editForm.staffId,
          username: this.editForm.username,
          role: this.editForm.role,
          status: this.editForm.status,
          email: this.editForm.email,
          phone: this.editForm.phone
        };
        if (this.editForm.password) {
          updateData.password = this.editForm.password;
        }
        await userApi.update(this.editForm.id, updateData);
        this.$message.success("用户信息修改成功！");
        this.editDialogVisible = false;
        this.fetchUsers();
      } catch (error) {
        console.error('Failed to update user:', error);
        this.$message.error('修改失败：' + (error.response?.data?.message || error.message));
      }
    },
    handleDelete(row) {
      this.$confirm(`确定删除用户【${row.username} - ${row.staffId}】吗？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(async () => {
        try {
          await userApi.delete(row.id);
          this.$message.success("删除成功");
          this.fetchUsers();
        } catch (error) {
          console.error('Failed to delete user:', error);
          this.$message.error('删除失败：' + (error.response?.data?.message || error.message));
        }
      }).catch(() => {});
    },
    handlePageChange(page) {
      this.currentPage = page;
      this.fetchUsers();
    },
    async submitAdd() {
      if (!this.addForm.staffId || !this.addForm.username || !this.addForm.password) {
          this.$message.error("工号、用户名和密码为必填项！");
          return;
      }

      try {
        await userApi.create({
          staffId: this.addForm.staffId,
          username: this.addForm.username,
          password: this.addForm.password,
          role: this.addForm.role || 'USER',
          email: this.addForm.email,
          phone: this.addForm.phone
        });
        this.$message.success("用户添加成功！");
        this.addForm = { staffId: "", username: "", password: "", role: "USER", email: "", phone: "" };
        this.fetchUsers();
      } catch (error) {
        console.error('Failed to create user:', error);
        this.$message.error('添加失败：' + (error.response?.data?.message || error.message));
      }
    }
  }
};
</script>

<style scoped>
:root {
  --tech-cyan: #00c0ff;
  --tech-violet: #7a8cff;
  --deep-blue: #062238;
}

/* -------------------- 整体容器和顶部控制 -------------------- */
.user-list-main-view {
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

/* -------------------- 下方分栏区域 -------------------- */
.content-split-area {
    flex-grow: 1; 
}

.table-content-block {
    display: flex;
    flex-direction: column;
    padding: 0 !important; 
}


/* ========================= 左侧：用户列表表格 (增加透明度，更能透出背景) ========================= */

.styled-table.glass-table {
    border-radius: 15px;
    overflow: hidden;
    
    /* !!! 核心修改：降低表格主体背景不透明度 !!! */
    background-color: rgba(255, 255, 255, 0.38) !important; /* 调整为 0.3，使其更透明 */
    backdrop-filter: blur(10px);
    border: 1px solid rgba(122, 140, 255, 0.12);
    box-shadow: 0 8px 30px rgba(122, 140, 255, 0.06);
    flex-grow: 1;
}

/* !!! 核心修改：调整表头背景不透明度 !!! */
.styled-table /deep/ .el-table__header-wrapper th {
    background-color: rgba(255, 255, 255, 0.85) !important; /* 表头略微降低透明度，但仍保持可见性 */
    color: var(--deep-blue);
    font-weight: 600;
    border-color: rgba(6,34,56,0.06);
}

/* !!! 核心修改：确保表格行背景透明或低透明度 !!! */
.styled-table /deep/ .el-table__row {
    background-color: transparent !important; /* 默认行背景透明 */
}

/* 数据行悬停效果也要保持低透明度 */
.styled-table /deep/ .el-table__row:hover {
    background: linear-gradient(90deg, rgba(0,192,255,0.04), rgba(122,140,255,0.03)) !important;
    cursor: pointer;
}

/* Tag 优化 */
.borrow-tag {
    border-radius: 12px; 
    padding: 0 10px; 
    font-weight: 600;
}


/* 分页容器 */
.pagination-container {
    margin-top: 15px; 
    text-align: right;
    display: flex;
    justify-content: flex-end; 
}


/* ========================= 右侧：添加表单区 (保持原有的渐变背景) ========================= */
.add-panel {
  /* 保持柔和渐变 */
  background: linear-gradient(135deg, rgba(0,192,255,0.12) 0%, rgba(122,140,255,0.14) 100%);
  padding: 20px;
  border-radius: 15px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.12);
  height: 100%;
  /* 将 panel 内文字颜色改为黑色（仅此处修改） */
  color: #000;
}

/* 右侧表单标题增强 */
.add-title {
    color: #000; /* 改为黑色 */
    font-size: 20px;
    font-weight: bold; 
    text-shadow: 0 1px 2px rgba(0,0,0,0.03);
    margin-bottom: 20px;
}
/* 右侧表单标签颜色 */
.add-panel /deep/ .el-form-item__label {
    color: #000 !important;
}

/* 右侧表单输入框/选择框 - 玻璃化 (保持不变) */
.add-panel /deep/ .el-input__inner,
.add-panel /deep/ .el-textarea__inner,
.add-panel /deep/ .el-select .el-input__inner,
.add-panel /deep/ .el-date-editor.el-input,
.add-panel /deep/ .el-date-editor.el-input__inner {
    background-color: rgba(255, 255, 255, 0.92) !important;
    border: 1px solid rgba(6,34,56,0.06);
    color: #333;
    border-radius: 8px; 
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


/* -------------------- 按钮/搜索/分页细节样式 -------------------- */
.page-title {
  font-size: 22px;
  color: var(--deep-blue);
  margin: 0;
}
.search-box-styled {
    width: 250px;
}
.search-box-styled /deep/ .el-input__inner {
    border-radius: 18px;
    height: 40px;
    background: rgba(255,255,255,0.9);
    border: 1px solid rgba(0,192,255,0.14);
}

/* 编辑按钮 */
.edit-button-styled { 
  background: linear-gradient(90deg, var(--tech-cyan), var(--tech-violet));
  color: #fff;
  border: none;
}
/* 删除按钮 */
.delete-button-styled { 
    background: rgba(255, 80, 80, 0.12);
    color: #d64545;
    border: 1px solid rgba(214,69,69,0.12);
}

/* 提交添加按钮 */
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