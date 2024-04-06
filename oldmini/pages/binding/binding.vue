<template>
	<view style="padding-top:50%;text-align: center;">
		<text style="font-size: 40rpx;line-height: 60rpx;">点击绑定按钮
		成为管理员</text>
		<button open-type="getPhoneNumber" style="width: 80%;margin: 100rpx auto 0;background: #409eff;color: #fff;"
			@getphonenumber="binding">立即绑定</button>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				taskId:'',
				userCode:'',
			}
		},
		onLoad(options) {
			let scene = options.scene;
			let taskId = options.taskId;
			if (scene != undefined) {
				this.taskId = scene;
			}else{
				this.taskId = taskId;
			}
			this.wxSilentLogin();
		},
		methods: {
			//uni.login
			wxSilentLogin: function() {
				let that = this;
				uni.login({
					success(res) {
						that.userCode = res.code;
					},
					fail(err) {
						console.log(err)
					}
				})
			},
		// 获取用户手机号
		binding(res) {
			let that = this;
			that.wxSilentLogin();
			if (res.detail.errMsg == "getPhoneNumber:ok") {
				uni.showLoading({
					title: '请求中...',
					mask: true
				})
				uni.request({
					method: 'POST',
					url: that.$wxApi + '/api/app/hbhk/poster/getOpenid',
					data: {
						taskId: that.taskId,
						code: that.userCode,
						appid: that.$wxAppId,
						encryptedData: res.detail.encryptedData,
						iv: res.detail.iv,
					},
					success: (res) => {
						uni.showToast({
							title: res.data.msg,
							icon: "none"
						});
						uni.navigateTo({
						    url:'/pages/about/about?taskId=' + that.taskId
						});
		
					}
				});
		
			}
		},
		}
	}
</script>

<style>

</style>
