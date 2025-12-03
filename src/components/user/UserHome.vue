<template>
  <el-container class="menu-layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-left">
        <div class="logo">设备管理系统 - 用户端</div>
      </div>

      <div class="header-center">
        <el-input placeholder="搜索设备或功能..." class="search-input" clearable>
          <template slot="prefix">
            <i class="el-icon-search"></i>
          </template>
        </el-input>
      </div>

      <div class="header-right">
        <el-dropdown>
          <span class="el-dropdown-link">
            <i class="el-icon-user"></i> 用户：{{ username }} <i class="el-icon-arrow-down el-icon--right"></i>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item @click.native="goProfile">个人中心</el-dropdown-item>
            <el-dropdown-item divided @click.native="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </el-header>

    <el-container>
      <!-- 左侧菜单栏 -->
      <el-aside width="220px" class="aside">
        <el-menu
          router
          :default-active="$route.path"
          background-color="transparent"
          text-color="#000000"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/user-home/profile">
            <i class="el-icon-user-solid"></i>
            <span>个人中心</span>
          </el-menu-item>

          <el-menu-item index="/user-home/device-search">
            <i class="el-icon-search"></i>
            <span>设备查询</span>
          </el-menu-item>

          <el-menu-item index="/user-home/borrow-process">
            <i class="el-icon-s-operation"></i>
            <span>借用流程</span>
          </el-menu-item>

          <el-menu-item index="/user-home/records">
            <i class="el-icon-document"></i>
            <span>记录与归还</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 右侧主内容区 -->
      <el-main class="main">
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
export default {
  name: "UserHome",
  data() {
    return {
      username: "普通用户", // 可从登录页传递或本地存储中读取
    };
  },
  methods: {
    logout() {
      this.$router.push({ name: "LoginPage" });
    },
    goProfile() {
      this.$router.push("/user-home/profile");
    },
  },
  mounted() {
    // 若直接访问 /user-home，则默认跳转到个人中心
    if (this.$route.path === "/user-home") {
      this.$router.replace("/user-home/profile");
    }
  },
};
</script>

<style scoped>
.menu-layout {
  height: 100vh;
  background: #eef2f7;
  overflow: hidden;
}

/* Header 样式 */
.header {
  height: 70px;
  background: linear-gradient(90deg, #b8d9f5 0%, #cbe9e3 100%);
  color: #000;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left .logo {
  font-size: 20px;
  font-weight: bold;
  letter-spacing: 1px;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.search-input {
  width: 320px;
  border-radius: 30px;
}

.header-right {
  color: #333;
}

.el-dropdown-link {
  cursor: pointer;
  color: #333;
  font-size: 15px;
}

.el-dropdown-link:hover {
  color: #409EFF;
}

/* Aside 菜单栏 */
.aside {
  background: linear-gradient(180deg, #b8d9f5 0%, #cbe9e3 100%);
  color: #000;
  padding-top: 20px;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.1);
}

.el-menu {
  border-right: none;
  font-weight: 500;
}

.el-menu-item:hover {
  background: rgba(255, 255, 255, 0.2) !important;
  border-radius: 8px;
}

.el-menu-item.is-active {
  background-color: rgba(64, 158, 255, 0.1) !important;
  border-left: 4px solid #409EFF;
  font-weight: bold;
}

/* Main 内容区 */
.main {
  background-color: #f9fafc;
  padding: 40px;
  border-radius: 12px;
  margin: 16px;
  box-shadow: inset 0 2px 6px rgba(0, 0, 0, 0.05);
}
</style>
