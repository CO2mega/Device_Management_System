<template>
  <el-container class="menu-page">
    
    <el-header class="header">
      <div class="header-left">
        <div class="system-title">智维设备管理系统</div>
      </div>

      <div class="header-center">
        <el-input placeholder="关键词搜索" class="search-input" clearable>
          <template slot="prefix">
            <div class="search-icon-placeholder"></div>
          </template>
        </el-input>
      </div>

      <div class="header-right">
        <div class="icon-group">
          <i class="el-icon-bell icon-btn"></i>
          <i class="el-icon-time icon-btn"></i> 
          
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-avatar-trigger">
              <div class="user-avatar"></div>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="logout" icon="el-icon-switch-button">
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          
        </div>
      </div>
    </el-header>

    <el-container class="content-container">
      <el-aside width="220px" class="aside">
        <el-menu
          router
          :default-active="$route.path"
          background-color="transparent"
          text-color="#ffffff"
          active-text-color="#ffffff"
          class="main-menu"
        >
          <el-menu-item index="/menu/dashboard">
              <i class="el-icon-s-home"></i>
              <span>首页</span>
          </el-menu-item>

          <el-submenu index="2">
              <template slot="title">
                  <i class="el-icon-s-tools"></i>
                  <span>设备管理</span>
              </template>
              <el-menu-item index="/menu/device-list">设备列表</el-menu-item>
          </el-submenu>

          <el-submenu index="3">
              <template slot="title">
                  <i class="el-icon-tickets"></i>
                  <span>用户管理</span>
              </template>
              <el-menu-item index="/menu/user-list">用户列表</el-menu-item>
          </el-submenu>

          <el-submenu index="4">
              <template slot="title">
                  <i class="el-icon-setting"></i>
                  <span>借用管理</span>
              </template>
              <el-menu-item index="/menu/borrow-records">借用记录</el-menu-item> 
              <el-menu-item index="/menu/return-records">归还记录</el-menu-item>
          </el-submenu>
        </el-menu>
      </el-aside>

      <el-main class="main">
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
export default {
  name: "MenuPage",
  methods: {
    // 处理下拉菜单的指令
    handleCommand(command) {
      if (command === 'logout') {
        this.logout();
      }
    },
    // 执行退出操作，跳转回登录页
    logout() {
      // 实际应用中：在这里清除用户认证信息（如 Token, Session Storage等）
      console.log('用户退出登录');
      this.$router.push({ name: "LoginPage" });
    },
  },
  mounted() {
    if (this.$route.path === "/menu") {
      this.$router.replace("/menu/dashboard");
    }
  },
};
</script>

<style scoped>
/* -------------------- 整体布局及背景 (全屏铺满) -------------------- */
.menu-page {
  height: 100vh;
  width: 100vw;
  /* 亮色科技感背景：冷蓝 + 霓虹青色渐变 */
  background: linear-gradient(135deg, #e9f9ff 0%, #f3f8ff 50%, #fff6fb 100%);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.content-container {
  height: calc(100vh - 70px);
  flex-grow: 1;
}

/* -------------------- 顶部 Header 样式 -------------------- */
.header {
  height: 70px !important;
  /* 半透明玻璃感背景，配合 backdrop 模糊增强质感 */
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  box-shadow: 0 6px 18px rgba(8, 30, 60, 0.06);
  border-bottom: 1px solid rgba(8, 30, 60, 0.04);
  z-index: 100;
}

/* Header 左侧系统名称样式 */
.header-left {
  width: 220px;
  display: flex;
  align-items: center;
  padding-left: 30px;
}
.system-title {
  font-size: 28px;
  font-weight: 700;
  color: #062238; /* 深海蓝，和亮色背景对比更好 */
  white-space: nowrap;
}

/* 搜索框样式 */
.search-input {
  width: 320px;
  margin-left: 0;
}
.search-input /deep/ .el-input__inner {
  border-radius: 22px;
  background: #ffffff;
  border: 1px solid rgba(0, 192, 255, 0.18);
  height: 40px;
  line-height: 40px;
  box-shadow: 0 6px 18px rgba(8, 30, 60, 0.04);
  color: #062238;
  padding-left: 20px !important;
}
.search-input /deep/ .el-input__prefix {
  display: none;
}

/* 顶部右侧图标组 */
.icon-group {
  display: flex;
  align-items: center;
  margin-right: 10px;
}
.icon-btn {
  font-size: 20px;
  color: #0b2545; /* 深蓝图标，和亮色背景匹配 */
  margin-left: 24px;
  cursor: pointer;
  transition: color 0.18s, transform 0.18s;
}
.icon-btn:hover {
  color: #00c0ff; /* 青色高亮 */
  transform: translateY(-2px);
}

/* 头像触发区域 */
.user-avatar-trigger {
  margin-left: 25px;
  cursor: pointer;
  height: 36px;
  width: 36px;
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00c0ff, #7a8cff); /* 青到紫的霓虹感渐变 */
  border: 2px solid rgba(0, 192, 255, 0.18);
  box-shadow: 0 6px 18px rgba(122, 140, 255, 0.12);
}


/* -------------------- 侧边栏 Aside & Menu 样式 -------------------- */
.aside {
  width: 220px !important;
  /* 侧边栏使用浅色半透明面板，和背景有对比，但仍感觉轻盈 */
  background: linear-gradient(180deg, rgba(255,255,255,0.9), rgba(250,250,255,0.8));
  padding: 12px 8px;
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.6);
  border-right: 1px solid rgba(8, 30, 60, 0.03);
  overflow-y: auto;
}

/* 调整菜单的顶部间距 */
.main-menu {
  margin-top: 20px;
}

/* 菜单项和子菜单标题的基础样式 */
.el-menu {
  border-right: none;
  background-color: transparent !important;
  font-weight: 600;
}
.el-menu-item,
.el-submenu /deep/ .el-submenu__title {
  margin: 8px 12px;
  height: 48px;
  line-height: 48px;
  border-radius: 12px;
  padding: 0 15px !important;
  transition: all 0.25s ease;
  display: flex;
  align-items: center;
  color: #062238 !important;
}
.el-menu-item i,
.el-submenu /deep/ .el-submenu__title i {
  font-size: 18px;
  margin-right: 12px;
  color: #0b2545;
  transition: color 0.2s;
}
.el-menu-item:hover,
.el-submenu /deep/ .el-submenu__title:hover {
  background: linear-gradient(90deg, rgba(0,192,255,0.08), rgba(122,140,255,0.06));
  color: #062238 !important;
  border-radius: 12px;
}
.el-menu-item:hover i,
.el-submenu /deep/ .el-submenu__title:hover i {
  color: #00c0ff;
}
.el-menu-item.is-active {
  background: linear-gradient(90deg, #00c0ff 0%, #7a8cff 100%) !important;
  color: #ffffff !important;
  font-weight: 700;
  border-radius: 12px;
  padding-left: 15px !important;
  box-shadow: 0 6px 20px rgba(122, 140, 255, 0.12);
}
.el-menu-item.is-active i {
  color: #ffffff !important;
}
.el-menu .el-menu-item {
  background-color: transparent !important;
  border-radius: 12px;
  padding-left: 35px !important;
  color: rgba(6, 34, 56, 0.9) !important;
}
.el-menu .el-menu-item:hover {
  background: rgba(0, 192, 255, 0.06) !important;
}
.el-menu .el-menu-item.is-active {
  background: linear-gradient(90deg, #00c0ff 0%, #7a8cff 100%) !important;
  color: #ffffff !important;
}


/* -------------------- Main 内容区 样式 -------------------- */
.main {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  border-radius: 14px;
  box-shadow: 0 8px 24px rgba(8, 30, 60, 0.06);
  margin: 20px 30px 30px 20px;
  padding: 20px;
  overflow: auto;
  height: auto;
  flex-grow: 1;
  color: #062238;
}

/* -------------------- 响应式优化 -------------------- */
@media (max-width: 768px) {
  .header-left { padding-left: 12px; }
  .search-input { width: 180px; }
  .system-title { font-size: 20px; }
  .aside { display: none; }
}
</style>