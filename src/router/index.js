import VueRouter from 'vue-router'
import index from '../components/indexPage/index.vue'
import login from "../components/login/login.vue";
import adminPage from "../components/adminPage/admin.vue"
import settingPage from "../components/settingPage/setting.vue"
import store from '../store'; // 导入Vuex store


const router = new VueRouter({
    mode: 'history',    // Routing mode, this mode will not display the pound sign # in the address
    routes: [
        {
            path: '/',
            name: 'index',
            component: index
        },
        {
            path: '/login',
            name: 'login',
            component: login
        },
        {
            path: '/admin',
            name: 'admin',
            component: adminPage,
            meta: { requiresAuth: true, allowedRoles: ['ADMIN', 'SUPERADMIN'] }
        },
        {
            path: '/superadmin/setting',
            name: 'setting',
            component: settingPage,
            meta: { requiresAuth: true, allowedRoles: ['SUPERADMIN'] }

        }
    ]
})

router.beforeEach((to, from, next) => {


    const isLoggedIn = store.getters.isLoggedIn;
    const role = store.getters.getRole;

    // 定义角色对应的路由权限
    const rolePermissions = {
        ADMIN: ['/admin'],
        SUPERADMIN: ['/superadmin','/admin']
    };

    // 检查是否需要验证权限
    if (to.matched.some(record => record.meta.requiresAuth)) {
        if (!isLoggedIn) {
            next('/login'); // 未登录则跳转到登录页
        } else if (!role || !rolePermissions[role]) {
            next('/unauthorized'); // 没有角色或者没有定义权限则跳转到未授权页
        } else if (role === 'ADMIN' && to.path.startsWith('/admin')) {
            next(); // 角色为 "admin" 且访问路径以 "/admin" 开头，放行
        } else if (role === 'SUPERADMIN' && to.path.startsWith('/superadmin')) {
            next(); // 角色为 "superadmin" 且访问路径以 "/superadmin" 开头，放行
        } else if (!rolePermissions[role].includes(to.path)) {
            next('/unauthorized'); // 没有权限访问该页面则跳转到未授权页
        } else {
            next(); // 有权限则继续访问页面
        }
    } else {
        next(); // 不需要验证权限的页面直接放行
    }

    // console.log('Logged In:', isLoggedIn);
    // console.log('Role:', role);
    // console.log('Allowed Roles:', rolePermissions[role]);
    // console.log('Target Path:', to.path);


});

export default router