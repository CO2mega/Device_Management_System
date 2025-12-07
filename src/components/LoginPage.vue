<template>
  <div class="main-container">
    
    <div class="background"></div>

    <div class="container">
      
      <div class="item">
        <h2 class="logo"><i class='bx bxl-xing'></i>智维</h2>
        <div class="text-item">
          <h2>用 户 <br><span>欢 迎 回 来！</span></h2>
          <p>设备管理，安全无忧</p>
          <div class="social-icon">
            <a href="#"><i class='bx bxl-facebook'></i></a>
            <a href="#"><i class='bx bxl-twitter'></i></a>
            <a href="#"><i class='bx bxl-youtube'></i></a>
            <a href="#"><i class='bx bxl-instagram'></i></a>
          </div>
        </div>
      </div>

      <div class="login-section" :class="{ active: isRegisterMode }">
        
        <div class="form-box login">
          <form @submit.prevent="handleLogin">
            <h2>登入</h2>
            
            <div class="input-box">
              <span class="icon"><i class='bx bxs-envelope'></i></span>
              <input type="text" v-model="username" required>
              <label>用户名</label>
            </div>
            
            <div class="input-box">
              <span class="icon"><i class='bx bxs-lock-alt'></i></span>
              <input type="password" v-model="password" required>
              <label>密码</label>
            </div>
            
            <div class="remember-password">
              <label><input type="checkbox">记住我</label>
              <a href="#" @click.prevent="showForgot = true">忘记密码</a>
            </div>
            
            <button class="btn" type="submit">登录</button>
            
            <div class="create-account">
              <p>创建新账户 
                <a href="#" class="register-link" @click.prevent="toggleMode">Sign Up</a>
              </p>
            </div>
          </form>
        </div>

        <div class="form-box register">
          <form @submit.prevent="handleRegister">
            <h2>注册</h2>
            
            <div class="input-box">
              <span class="icon"><i class='bx bxs-user'></i></span>
              <input type="text" v-model="registerUsername" required>
              <label>用户名</label>
            </div>
            
            <div class="input-box">
              <span class="icon"><i class='bx bxs-envelope'></i></span>
              <input type="email" v-model="registerEmail" required>
              <label>邮箱</label>
            </div>
            
            <div class="input-box">
              <span class="icon"><i class='bx bxs-lock-alt'></i></span>
              <input type="password" v-model="registerPassword" required>
              <label>密码</label>
            </div>
            
            <div class="remember-password">
              <label><input type="checkbox" required>同意协议</label>
            </div>
            
            <button class="btn" type="submit">Sign Up</button>
            
            <div class="create-account">
              <p>已有账户？ 
                <a href="#" class="login-link" @click.prevent="toggleMode">登录</a>
              </p>
            </div>
          </form>
        </div>

      </div>
    </div>

    <teleport to="body">
      <div v-if="showForgot" class="modal-mask">
        <div class="modal-box">
          <h2 class="modal-title">重置密码</h2>
          <div class="input-box modal-input-style">
             <input type="email" v-model="forgotEmail" placeholder="Enter Email" style="color:#000;">
          </div>
          <div class="modal-actions center">
            <button class="btn small-btn" @click="handleForgot">确认</button>
            <button class="btn small-btn cancel" @click="showForgot = false">取消</button>
          </div>
        </div>
      </div>
    </teleport>

  </div>
</template>

<script>
import { authApi } from '@/api/services';

export default {
  name: "LoginPage",
  data() {
    return {
      // 核心状态：false = 显示登录，true = 显示注册
      isRegisterMode: false, 
      
      // 控制忘记密码弹窗显示
      showForgot: false,     
      
      // 表单数据绑定
      username: "",
      password: "",
      registerUsername: "",
      registerEmail: "",
      registerPassword: "",
      forgotEmail: ""
    };
  },
  methods: {
    // 核心逻辑：切换登录/注册视图
    toggleMode() {
      this.isRegisterMode = !this.isRegisterMode;
    },

    // 处理登录逻辑
    async handleLogin() {
      // Call backend login API
      if (!this.username || !this.password) {
        alert('请输入用户名和密码');
        return;
      }

      try {
        const response = await authApi.login({ staffId: this.username, password: this.password });
        const data = response.data;
        const token = data?.token;
        const user = data?.user;
        if (token) {
          localStorage.setItem('token', token);
        }
        if (user) {
          localStorage.setItem('user', JSON.stringify(user));
        }

        // Redirect based on role if available
        const role = user?.role || 'USER';
        if (role === 'ADMIN' || role === 'ROLE_ADMIN') {
          this.$router.push('/menu/dashboard');
        } else {
          this.$router.push('/user-home');
        }

        alert('登录成功');
      } catch (err) {
        // Provide better error feedback
        const msg = err?.response?.data?.message || err.message || '登录失败';
        alert(msg);
      }
    },

    // 处理注册逻辑
    async handleRegister() {
      if (!this.registerUsername || !this.registerEmail || !this.registerPassword) {
        alert("请完整填写注册信息");
        return;
      }

      try {
        // Use registerUsername as staffId when not provided explicitly
        const registerPayload = {
          staffId: this.registerUsername,
          username: this.registerUsername,
          password: this.registerPassword,
          email: this.registerEmail,
          phone: ''
        };

        await authApi.register(registerPayload);
        alert(`注册成功，欢迎 ${this.registerUsername}！`);
        // 注册成功后自动切回登录界面
        this.isRegisterMode = false;
      } catch (err) {
        const msg = err?.response?.data?.message || err.message || '注册失败';
        alert(msg);
      }
    },

    // 处理忘记密码逻辑
    handleForgot() {
      alert(`找回密码邮件已发送至 ${this.forgotEmail}`);
      this.showForgot = false;
    }
  }
};
</script>

<style>
/* 引入谷歌字体 Poppins */
@import url('https://fonts.googleapis.com/css2?family=Poppins:ital,wght@0,300;0,400;0,500;1,500&display=swap');

/* === 全局重置 === */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: "Poppins", sans-serif;
}

/* === 主容器布局 === */
.main-container {
  height: 100vh;
  width: 100vw;
  overflow: hidden; /* 防止滑动时出现滚动条 */
  position: relative;
  background: #000;
}

/* 背景 */
.background {
  background: url("@/assets/4.png") no-repeat center center/cover;
  height: 100vh;
  width: 100%;
  filter: blur(6px);
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}

/* === 中间卡片样式 === */
.container {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 75%;
  height: 550px;
  margin-top: 20px;
  background: url('@/assets/4.png') no-repeat center center/cover;
  border-radius: 20px;
  overflow: hidden;
  z-index: 2;
}

/* === 左侧内容区 (Logo & Welcome) === */
.item {
  position: absolute;
  top: 0;
  left: 0;
  width: 58%;
  height: 100%;
  color: #fff;
  padding: 80px;
  display: flex;
  justify-content: space-between;
  flex-direction: column;
}

.item .logo {
  font-size: 30px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.text-item h2 { font-size: 40px; line-height: 1; }
.text-item p { margin: 20px 0; }
.social-icon a i { font-size: 24px; margin-right: 15px; color: #fff; transition: 0.5s; }
.social-icon a:hover i { transform: scale(1.2); }

/* === 右侧登录/注册区域 (核心动画区) === */
.login-section {
  position: absolute;
  top: 0;
  right: 0;
  width: calc(100% - 58%); /* 剩余宽度 */
  height: 100%;
  color: #fff;
  
  /* 玻璃拟态效果 */
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.05); 
}

/* 通用的表单容器 */
.form-box {
  position: absolute;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  padding: 0 40px;
}

/* --- 动画核心：注册表单 --- */
.form-box.register {
  transform: translateX(430px); /* 默认移出右侧视野 */
  transition: transform 0.6s ease; /* 动画时长 */
  transition-delay: 0s; /* 消失时立即执行 */
  pointer-events: none; /* 隐藏时不可点击 */
}

/* 当父级有 .active 类时 (即点击了注册) */
.login-section.active .form-box.register {
  transform: translateX(0px); /* 移入视野 */
  transition-delay: 0.7s; /* 延迟执行，等待登录框先移开 */
  pointer-events: auto;
}

/* --- 动画核心：登录表单 --- */
.form-box.login {
  transform: translateX(0px); /* 默认在视野内 */
  transition: transform 0.6s ease;
  transition-delay: 0.7s; /* 出现时延迟 */
}

/* 当父级有 .active 类时 (即点击了注册) */
.login-section.active .form-box.login {
  transform: translateX(430px); /* 秼出右侧视野 */
  transition-delay: 0s; /* 立即移走 */
  pointer-events: none;
}

/* === 输入框与浮动标签 === */
.input-box {
  width: 100%;
  height: 50px;
  border-bottom: 2px solid #fff; /* 只有下划线 */
  margin: 30px 0;
  position: relative;
}

.input-box input {
  width: 100%;
  height: 100%;
  background: transparent;
  border: none;
  outline: none;
  font-size: 16px;
  color: #fff;
  padding-right: 28px;
}

/* 标签文字：绝对定位 */
.input-box label {
  position: absolute;
  top: 50%;
  left: 0;
  transform: translateY(-50%); /* 垂直居中 */
  pointer-events: none; /* 让点击穿透到 input */
  transition: 0.5s ease;
}

/* 当 input 聚焦 或 有内容时，标签上浮 */
.input-box input:focus ~ label,
.input-box input:valid ~ label {
  top: -5px; /* 上移 */
}

.input-box .icon {
  position: absolute;
  top: 13px;
  right: 0;
  font-size: 19px;
  color: #fff;
}

/* === 按钮样式 === */
.btn {
  width: 100%;
  height: 45px;
  background: #1f67ba; /* 蓝色 */
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4px 10px rgba(0,0,0,0.4);
}

/* === 底部链接 === */
.create-account { margin: 25px; text-align: center; font-size: 14.5px; }
.create-account p a { color: #fff; font-weight: 600; text-decoration: none; }
.create-account p a:hover { text-decoration: underline; }

.remember-password { display: flex; justify-content: space-between; font-size: 14px; margin-bottom: 15px;}
.remember-password a { color: #fff; text-decoration: none; }

/* === 忘记密码弹窗样式 === */
.modal-mask {
  position: fixed;
  z-index: 9999;
  top: 0; left: 0;
  width: 100vw; height: 100vh;
  background: rgba(0, 0, 0, 0.7);
  display: flex; align-items: center; justify-content: center;
}
.modal-box {
  background: #fff;
  border-radius: 10px;
  padding: 30px;
  width: 320px;
  text-align: center;
  color: #333;
}
.small-btn { width: 45%; margin: 5px; }
.small-btn.cancel { background: #666; }
</style>