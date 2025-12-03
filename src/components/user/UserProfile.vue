<template>
  <div class="profile-container">
    <h2 class="profile-title">个人信息</h2>

    <!-- 用户基本信息卡片 -->
    <el-card class="profile-card">
      <div class="profile-top">
        <!-- 用户头像 -->
        <el-upload
          class="avatar-uploader"
          action="#"
          :show-file-list="false"
          :on-change="handleAvatarChange"
          :before-upload="beforeAvatarUpload"
        >
          <img v-if="user.avatar" :src="user.avatar" class="avatar" />
          <i v-else class="el-icon-plus avatar-placeholder"></i>
        </el-upload>

        <!-- 用户信息 -->
        <el-descriptions title="用户信息" column="1" border>
          <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ user.email }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ user.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ user.registeredAt }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ user.role }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 操作按钮 -->
      <div class="profile-actions">
        <el-button type="primary" @click="editDialog = true">修改信息</el-button>
        <el-button @click="passwordDialog = true">重置密码</el-button>
      </div>
    </el-card>

    <!-- 最近借用记录 -->
    <el-card class="recent-records" style="margin-top: 20px;">
      <h3>最近借用记录</h3>
      <el-table
        :data="recentRecords"
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
      </el-table>
    </el-card>

    <!-- 修改信息弹窗 -->
    <el-dialog title="修改信息" :visible.sync="editDialog">
      <el-form :model="user" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="user.username"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="user.email"></el-input>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="user.phone"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="editDialog = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </span>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog title="重置密码" :visible.sync="passwordDialog">
      <el-form label-width="100px">
        <el-form-item label="新密码">
          <el-input type="password" v-model="newPassword"></el-input>
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input type="password" v-model="confirmPassword"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="passwordDialog = false">取消</el-button>
        <el-button type="primary" @click="resetPassword">提交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "UserProfile",
  data() {
    return {
      user: {
        username: "普通用户",
        email: "user@example.com",
        phone: "13800138000",
        registeredAt: "2024-06-01",
        role: "用户",
        avatar: null,
      },
      recentRecords: [
        { device: "温度传感器", borrowDate: "2024-09-01", returnDate: "2024-09-05", status: "已归还", image: require('@/assets/OIP-C.webp') },
        { device: "压力测试仪", borrowDate: "2024-09-10", returnDate: "", status: "借用中", image: require('@/assets/OIP-C (1).webp') },
        { device: "流量计", borrowDate: "2024-09-05", returnDate: "", status: "逾期", image: require('@/assets/OIP-C (2).webp') },
      ],
      editDialog: false,
      passwordDialog: false,
      newPassword: "",
      confirmPassword: "",
    };
  },
  methods: {
    handleAvatarChange(file) {
      const reader = new FileReader();
      reader.onload = e => {
        this.user.avatar = e.target.result;
      };
      reader.readAsDataURL(file.raw);
    },
    beforeAvatarUpload(file) {
      const isImg = file.type.startsWith("image/");
      if (!isImg) this.$message.error("只能上传图片文件");
      return isImg;
    },
    saveProfile() {
      this.editDialog = false;
      this.$message.success("用户信息已保存");
    },
    resetPassword() {
      if (this.newPassword !== this.confirmPassword) {
        this.$message.error("两次密码输入不一致");
        return;
      }
      this.passwordDialog = false;
      this.newPassword = "";
      this.confirmPassword = "";
      this.$message.success("密码已重置");
    },
  },
};
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
}

.profile-card {
  padding: 20px;
  border-radius: 12px;
  background-color: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.profile-top {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.avatar-uploader {
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: 28px;
  color: #ccc;
}

.profile-actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}

.recent-records {
  padding: 15px;
  border-radius: 12px;
  background-color: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}
</style>
