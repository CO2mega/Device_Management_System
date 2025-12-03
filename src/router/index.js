import Vue from 'vue'
import Router from 'vue-router'

// 登录页
import LoginPage from '../components/LoginPage.vue'

// 管理员端
import MenuPage from '../components/MenuPage.vue'
import DashboardHome from '../components/dashboard/DashboardHome.vue'
import DeviceList from '../components/device/DeviceList.vue'
import UserList from '../components/user/UserList.vue'
import BorrowRecords from '../components/borrow/BorrowRecords.vue' 
import ReturnRecords from '../components/borrow/ReturnRecords.vue'

// 用户端
import UserHome from '../components/user/UserHome.vue'   // ✅ 新增用户主页
import UserProfile from '../components/user/UserProfile.vue'
import UserDeviceSearch from '../components/user/UserDeviceSearch.vue'
import BorrowProcess from '../components/user/BorrowProcess.vue'
import UserRecords from '../components/user/UserRecords.vue'

Vue.use(Router)

export default new Router({
  mode: 'history',
  routes: [
    {
      path: '/',
      name: 'LoginPage',
      component: LoginPage
    },

    // ✅ 管理员端菜单页
    {
      path: '/menu',
      component: MenuPage,
      redirect: '/menu/dashboard',
      children: [
        { path: 'dashboard', name: 'DashboardHome', component: DashboardHome },
        { path: 'device-list', name: 'DeviceList', component: DeviceList },
        { path: 'user-list', name: 'UserList', component: UserList },
        { path: 'borrow-records', name: 'BorrowRecords', component: BorrowRecords }, 
        { path: 'return-records', name: 'ReturnRecords', component: ReturnRecords },
      ]
    },

    // ✅ 用户端
    {
      path: '/user-home',
      name: 'UserHome',
      component: UserHome,
      redirect: '/user-home/profile', // 默认跳转
      children: [
        { path: 'profile', name: 'UserProfile', component: UserProfile },
        { path: 'device-search', name: 'UserDeviceSearch', component: UserDeviceSearch },
        { path: 'borrow-process', name: 'BorrowProcess', component: BorrowProcess },
        { path: 'records', name: 'UserRecords', component: UserRecords },
      ]
    },

    // 兜底路由
    { path: '*', redirect: '/' }
  ]
})
