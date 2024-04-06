import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'

/**
 * Note: sub-menu only appear when route children.length >= 1
 * Detail see: https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
 *
 * hidden: true                   if set true, item will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menu
 *                                if not set alwaysShow, when item has more than one children route,
 *                                it will becomes nested mode, otherwise not show the root menu
 * redirect: noRedirect           if set noRedirect will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
    roles: ['admin','editor']    control the page roles (you can set multiple roles)
    title: 'title'               the name show in sidebar and breadcrumb (recommend set)
    icon: 'svg-name'/'el-icon-x' the icon show in the sidebar
    breadcrumb: false            if set false, the item will hidden in breadcrumb(default is true)
    activeMenu: '/example/list'  if set path, the sidebar will highlight the path you set
  }
 */

/**
 * constantRoutes
 * a base page that does not have permission requirements
 * all roles can be accessed
 */
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true
  },

  {
    path: '/404',
    component: () => import('@/views/404'),
    hidden: true
  },

  // 工作台
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: '首页',
        component: () => import('@/views/dashboard/index')
      }
    ]
  },
  // 数据大屏
  {
    path: '/dataview',
    component: Layout,
    children: [
      {
        path: 'view/:objectid',
        name: '编辑方案',
        component: () => import('@/views/datav/index')
      }
    ]
  },
  // 内容
  {
    path: '/business/material',
    component: Layout,
    children: [
      {
        path: 'new/*',
        name: '新建',
        component: () => import('@/views/business/material/new')
      },
      {
        path: 'edit/*',
        name: '编辑',
        component: () => import('@/views/business/material/edit')
      }
    ]
  },
  // 内容库
  {
    path: '/business/materiallib',
    component: Layout,
    children: [
      {
        path: 'new',
        name: '新建',
        component: () => import('@/views/business/materiallib/new')
      },
      {
        path: 'edit/*',
        name: '编辑',
        component: () => import('@/views/business/materiallib/edit')
      }
    ]
  },
  // 朋友圈同步
  {
    path: '/business/materialpushmomenttask/',
    component: Layout,
    children: [
      {
        path: 'new',
        name: '新建',
        component: () => import('@/views/business/materialpushmomenttask/new')
      },
      {
        path: 'edit/*',
        name: '编辑',
        component: () => import('@/views/business/materialpushmomenttask/edit')
      }
    ]
  },
  // 社群同步
  {
    path: '/business/materialpushchatroomtask/',
    component: Layout,
    children: [
      {
        path: 'new',
        name: '新建',
        component: () => import('@/views/business/materialpushchatroomtask/new')
      },
      {
        path: 'edit/*',
        name: '编辑',
        component: () => import('@/views/business/materialpushchatroomtask/edit')
      }
    ]
  },
  // 加友任务
  {
    path: '/business/friendrequesttask/',
    component: Layout,
    children: [
      {
        path: 'new',
        name: '新建',
        component: () => import('@/views/business/friendrequesttask/index')
      },
      {
        path: 'edit/*',
        name: '编辑',
        component: () => import('@/views/business/friendrequesttask/index')
      }
    ]
  },
  // 个人信息
  {
    path: '/system/sysuser',
    component: Layout,
    children: [
      {
        path: 'profile',
        name: '个人信息',
        component: () => import('@/views/system/sysuser/profile')
      },
      {
        path: 'opera/objectid/:objectid',
        name: '操作权限',
        component: () => import('@/views/system/sysopera/index')
      }
    ]
  },

  {
    path: '/system/sysrole',
    component: Layout,
    children: [
      {
        path: 'opera/objectid/:objectid',
        name: '操作权限',
        component: () => import('@/views/system/sysopera/index')
      }
    ]
  },

  // 工作台方案
  {
    path: '/system/syssolution',
    component: Layout,
    children: [
      {
        path: 'view/:objectid',
        name: '编辑方案',
        component: () => import('@/views/system/syssolution/view')
      }
    ]
  },
  // 对象管理/设置审批
  {
    path: '/system/sysobject',
    component: Layout,
    children: [
      {
        path: 'configworkflow/*',
        name: '设置审批',
        component: () => import('@/views/system/sysobject/configworkflow')
      },
      {
        path: 'jump/*',
        name: '跳转',
        component: () => import('@/views/system/sysobject/jump')
      }
    ]
  },

  // 通用对象的配置
  {
    path: '/(system|business)/:id',
    component: Layout,
    children: [
      {
        path: 'home',
        name: '主页',
        component: () => import('@/views/commonobject/home/index')
      },
      {
        path: 'edit/*',
        name: '编辑',
        component: () => import('@/views/commonobject/edit/index')
      },
      {
        path: 'new',
        name: '新建',
        component: () => import('@/views/commonobject/new/index')
      },
      {
        path: 'view/*',
        name: '详情',
        component: () => import('@/views/commonobject/view/index')
      }

    ]
  },

  // 404 page must be placed at the end !!!
  { path: '*', redirect: '/404', hidden: true }
]

const createRouter = () =>
  new Router({
    // mode: 'history', // require service support
    scrollBehavior: () => ({ y: 0 }),
    routes: constantRoutes
  })

const router = createRouter()

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

export default router
