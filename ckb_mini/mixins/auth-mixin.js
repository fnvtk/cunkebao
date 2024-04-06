export default {
	methods: {
		checkLogin() {
			const pages = getCurrentPages();
			const currentPage = pages[pages.length - 1];
			let fullPath = '';
			let queryString = '';

			// #ifdef H5
			fullPath = window.location.hash ? window.location.hash.substring(1) : window.location.pathname;
			queryString = window.location.search;
			// #endif

			// #ifdef MP
			fullPath = `/${currentPage.route}`; // 在小程序中使用route
			// Combine existing options with the scene parameter if it exists
			const options = {
				...currentPage.options
			};

			queryString = this.objectToQueryString(options);

			if (this.$root.$mp.query.scene) {
				queryString += "&" + decodeURIComponent(this.$root.$mp.query.scene)
			}

			// #endif

			const redirectUrl = fullPath + "?" + queryString;


			// 如果当前已经在登录页，则不进行跳转
			if (fullPath.includes('pages/login/login') || fullPath.includes('pages/poster/index') || fullPath.includes(
					'pages/about/about') || fullPath.includes('pages/form/input')) {
				return;
			}
			// 未登录则跳转到登录页
			else if (!this.isLoggedIn()) {
				uni.redirectTo({
					url: '/pages/login/login?redirect=' + encodeURIComponent(redirectUrl),
				});
			}
		},

		isLoggedIn() {
			const token = uni.getStorageSync('token');
			return !!token;
		},

		// 将对象转换为查询字符串
		objectToQueryString(obj) {
			return Object.keys(obj).map(key => `${key}=${encodeURIComponent(obj[key])}`).join('&');
		},
	},

	onLoad() {
		this.checkLogin();
	},
};