<template>
	<view class="page">
		<block v-if="poster.process == 0">
			<button style="padding: 0;margin-left: 0; text-align: left;" class="nullBtn" open-type="getPhoneNumber"
				@getphonenumber="getPhoneNumber">
				<image class="posterImg" :src="poster.poster" mode="widthFix"></image>
			</button>


			<view class="settingBtn" @click="setting" v-if="taskManageOpenid != ''">
				<image src="/static/setting_icon.png" mode=""></image>
			</view>


		</block>
		<block v-else>
			<image class="posterImg" :src="poster.poster" mode="widthFix"></image>
		</block>

	</view>
</template>

<script>
	export default {
		data() {
			return {
				title: '',
				path: '',
				userCode: '',
				taskId: '',
				poster: [],
				openid: '',
				taskManageOpenid: '',
			}
		},
		onLoad(options) {
			let scene = options.scene;
			let taskId = options.taskId;

			if (scene != undefined) {
				this.taskId = scene;
				this.path = '/pages/about/about?taskId=' + scene;
			}

			if (taskId != undefined) {
				this.taskId = taskId;
				this.path = '/pages/about/about?taskId=' + taskId;
			}


			this.openid = uni.getStorageSync('openid')
			this.getPosterData();
			this.wxSilentLogin();

		},
		methods: {

			setting() {
				let that = this;
				if (that.openid == '') {
					uni.showModal({
						title: '提示',
						content: '请先授权登录',
					})
					return true;
				} else if (that.openid != that.taskManageOpenid) {
					uni.showModal({
						title: '提示',
						content: '您不是管理员',
					})
				} else if (that.openid == that.taskManageOpenid) {
					uni.navigateTo({
						url: '/pages/setting/setting?taskId=' + that.taskId + '&companyId=' + that.poster[
							'CompanyId'] + '&openid=' + that.openid
					})
				} else {
					uni.showToast({
						title: '未知错误',
						icon: "none",
					})
				}



			},


			getPosterData() {
				let that = this;
				uni.request({
					method: 'POST',
					url: that.$wxApi + '/api/app/hbhk/poster/getPosterData',
					data: {
						appid: that.$wxAppId,
						taskId: that.taskId
					},
					success: (res) => {
						uni.hideLoading();
						if (res.data.code == 1) {
							that.poster = res.data.data;
							that.title = that.poster.Name;


							//修改标题
							uni.setNavigationBarTitle({
								title: that.title
							});

							//分享设置
							this.weChatShare();
						} else {
							uni.showToast({
								title: res.data.msg,
								icon: "none",
							})
						}
					}
				});
			},

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
			getPhoneNumber(res) {
				let that = this;
				that.wxSilentLogin();
				if (res.detail.errMsg == "getPhoneNumber:ok") {
					uni.showLoading({
						title: '请求中...',
						mask: true
					})
					uni.request({
						method: 'POST',
						url: that.$wxApi + '/api/app/hbhk/poster/sendUser',
						data: {
							key: that.poster.Guid,
							code: that.userCode,
							appid: that.$wxAppId,
							encryptedData: res.detail.encryptedData,
							iv: res.detail.iv,
						},
						success: (res) => {
							uni.hideLoading();
							let posterTips = that.poster.posterTips;
							let tisp = res.data.msg;
							if (posterTips != '' && posterTips != null && posterTips != undefined) {
								tisp = posterTips;
							}

							//存储用户信息
							uni.setStorageSync('openid', res.data.data)
							that.openid = res.data.data;
							that.getTaskManage();

							uni.showModal({
								title: '提示',
								content: tisp,
								success: function(res) {
									console.log(res)
								}
							})

							uni.showToast({
								title: res.data.msg,
								icon: "none",
							})

						}
					});

				}
			},


			// 判断是否管理员
			getTaskManage() {
				let that = this;
				uni.request({
					method: 'POST',
					url: that.$wxApi + '/api/app/hbhk/poster/getTaskManage',
					data: {
						openid: that.openid,
						taskId: that.taskId
					},
					success: (res) => {
						uni.hideLoading();
						if (res.data.code == 1) {
							that.taskManageOpenid = that.openid;
						}
					}
				});
			},
			//分享
			weChatShare() {
				let that = this;
				that.share = {
					title: that.title,
					path: that.path,
					imageUrl: '',
					desc: '',
					content: ''
				}
			},

		}
	}
</script>

<style>
	.nullBtn {
		background: none;
		padding: 0;
		text-align: left;
	}

	.nullBtn:after {
		border: 0;
	}

	.page {
		width: 100vw;
		height: 100vh;
	}

	.posterImg {
		width: 100%;
	}

	.settingBtn {
		position: fixed;
		right: 5%;
		bottom: 10%;
		width: 80rpx;
		height: 80rpx;
		z-index: 100;
		border: 1px solid #999;
		padding: 12rpx;
		box-sizing: border-box;
		border-radius: 50%;
	}

	.settingBtn image {
		width: 100%;
		height: 100%;
	}
</style>