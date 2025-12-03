<template>
  <div class="borrow-process-container">
    <h2>借用流程</h2>

    <!-- 流程步骤条 -->
    <el-steps :active="activeStep" finish-status="success" style="margin-bottom: 20px;">
      <el-step title="提交申请"></el-step>
      <el-step title="管理员审批"></el-step>
      <el-step title="领取设备"></el-step>
      <el-step title="归还设备"></el-step>
    </el-steps>

    <el-button type="primary" @click="nextStep">下一步</el-button>

    <!-- ✅ 借用申请表 -->
    <el-card class="borrow-form" style="margin-top: 30px;" v-loading="loading">
      <h3>借用申请</h3>
      <el-form :model="form" label-width="120px">
        <el-form-item label="选择设备">
          <el-select v-model="form.deviceId" placeholder="请选择设备" @change="handleDeviceChange">
            <el-option
              v-for="item in devices"
              :key="item.id"
              :label="item.name + '（' + item.status + '）'"
              :value="item.id"
              :disabled="item.status !== '正常'"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="借用开始日期">
          <el-date-picker v-model="form.startDate" type="date" placeholder="选择开始日期" value-format="yyyy-MM-dd"></el-date-picker>
        </el-form-item>

        <el-form-item label="预计归还日期">
          <el-date-picker v-model="form.endDate" type="date" placeholder="选择归还日期" value-format="yyyy-MM-dd"></el-date-picker>
        </el-form-item>

        <el-form-item label="借用说明">
          <el-input type="textarea" v-model="form.remark" placeholder="可填写借用说明"></el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm">提交申请</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { deviceApi, loanApi } from '@/api/services';

export default {
  name: "BorrowProcess",
  data() {
    return {
      activeStep: 0,
      devices: [],
      loading: false,
      form: {
        device: "",
        deviceId: null,
        startDate: "",
        endDate: "",
        remark: ""
      }
    };
  },
  mounted() {
    this.fetchDevices();
  },
  methods: {
    async fetchDevices() {
      this.loading = true;
      try {
        const response = await deviceApi.getAll({ size: 100, isLoaned: false });
        const data = response.data;
        
        this.devices = data.content.map(device => ({
          id: device.id,
          name: device.name,
          status: device.status || '正常'
        }));
      } catch (error) {
        console.error('Failed to fetch devices:', error);
        this.$message.error('获取设备列表失败');
      } finally {
        this.loading = false;
      }
    },
    nextStep() {
      if (this.activeStep < 3) this.activeStep++;
      else this.$message.success("流程已完成");
    },
    async submitForm() {
      if (!this.form.deviceId || !this.form.startDate || !this.form.endDate) {
        this.$message.error("请完整填写申请信息");
        return;
      }
      
      try {
        await loanApi.apply({
          deviceId: this.form.deviceId,
          expectedReturnDate: this.form.endDate,
          purpose: this.form.remark
        });
        this.$message.success(`已提交借用申请，等待管理员审批`);
        this.resetForm();
        this.activeStep = 1; // Move to approval step
        this.fetchDevices(); // Refresh device list
      } catch (error) {
        console.error('Failed to apply for loan:', error);
        this.$message.error('申请失败：' + (error.response?.data?.message || error.message));
      }
    },
    resetForm() {
      this.form = { device: "", deviceId: null, startDate: "", endDate: "", remark: "" };
    },
    handleDeviceChange(deviceId) {
      this.form.deviceId = deviceId;
      const device = this.devices.find(d => d.id === deviceId);
      if (device) {
        this.form.device = device.name;
      }
    }
  }
};
</script>

<style scoped>
.borrow-process-container {
  padding: 20px;
}

.borrow-form {
  padding: 20px;
  border-radius: 12px;
}
</style>
