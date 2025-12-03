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
    <el-card class="borrow-form" style="margin-top: 30px;">
      <h3>借用申请</h3>
      <el-form :model="form" label-width="120px">
        <el-form-item label="选择设备">
          <el-select v-model="form.device" placeholder="请选择设备">
            <el-option
              v-for="item in devices"
              :key="item.name"
              :label="item.name + '（' + item.status + '）'"
              :value="item.name"
              :disabled="item.status !== '正常'"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="借用开始日期">
          <el-date-picker v-model="form.startDate" type="date" placeholder="选择开始日期"></el-date-picker>
        </el-form-item>

        <el-form-item label="借用结束日期">
          <el-date-picker v-model="form.endDate" type="date" placeholder="选择结束日期"></el-date-picker>
        </el-form-item>

        <el-form-item label="备注">
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
export default {
  name: "BorrowProcess",
  data() {
    return {
      activeStep: 0,
      // 模拟设备列表
      devices: [
        { name: "温度传感器", status: "正常" },
        { name: "压力测试仪", status: "故障" },
        { name: "流量计", status: "正常" },
      ],
      form: {
        device: "",
        startDate: "",
        endDate: "",
        remark: ""
      }
    };
  },
  methods: {
    nextStep() {
      if (this.activeStep < 3) this.activeStep++;
      else this.$message.success("流程已完成");
    },
    submitForm() {
      if (!this.form.device || !this.form.startDate || !this.form.endDate) {
        this.$message.error("请完整填写申请信息");
        return;
      }
      this.$message.success(`已提交申请：${this.form.device} 从 ${this.form.startDate} 到 ${this.form.endDate}`);
      this.resetForm();
    },
    resetForm() {
      this.form = { device: "", startDate: "", endDate: "", remark: "" };
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
