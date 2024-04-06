<template>

	<view class="page">
		<uni-nav-bar shadow :fixed="true" left-icon="left" @clickLeft="back" statusBar
			:title="formData.Name || '任务配置'" />

		<view class="forms">
			<uni-forms ref="form" :modelValue="formData" :rules="rules" label-position="top" label-width="200px">
				<input v-model="formData.Id" style="display: none;">

				<uni-forms-item label="名称" name="Name" required>
					<uni-easyinput type="text" v-model="formData.Name" placeholder="请输入名称" />
				</uni-forms-item>

				<uni-forms-item label="主题" name="Topic" required>
					<uni-easyinput type="text" v-model="formData.Topic" placeholder="请输入主题" />
				</uni-forms-item>

				<uni-forms-item label="招呼语" name="Hello" required>
					<uni-easyinput type="textarea" v-model="formData.Hello" placeholder="请输入招呼语"></uni-easyinput>
				</uni-forms-item>

				<uni-forms-item label="添加好友间隔" name="MinFriendRequestInterval" required>
					<uni-easyinput type="number" v-model="formData.MinFriendRequestInterval" placeholder="请输入添加好友间隔" />
				</uni-forms-item>

				<uni-forms-item label="每日最大添加好友数" name="MaxPerDayFriendRequestCount" required>
					<uni-easyinput type="number" v-model="formData.MaxPerDayFriendRequestCount"
						placeholder="请输入每日最大添加好友数" />
				</uni-forms-item>

				<uni-forms-item label="持续执行" name="Always" required>
					<uni-section type="line">
						<view class="uni-px-5">
							<uni-data-checkbox mode="tag" v-model="formData.Always"
								:localdata="[{text: '开启',value: 1},{text: '关闭',value: 0}]"></uni-data-checkbox>
						</view>
					</uni-section>
				</uni-forms-item>

				<uni-forms-item label="启用收集" name="EnableCollect" required>
					<uni-section type="line">
						<view class="uni-px-5">
							<uni-data-checkbox mode="tag" v-model="formData.EnableCollect"
								:localdata="[{text: '开启',value: 1},{text: '关闭',value: 0}]"></uni-data-checkbox>
						</view>
					</uni-section>
				</uni-forms-item>

				<uni-forms-item label="回复模板">
					<button style="background: #2979ff;" class="mini-btn" type="primary" size="mini"
						@click="template(0)">配置话术</button>
				</uni-forms-item>

				<uni-forms-item label="多次添加回复模板">
					<button style="background: #2979ff;" class="mini-btn" type="primary" size="mini"
						@click="template(1)">配置话术</button>
				</uni-forms-item>
				<button class="submit" @click="submit" style="background: #2979ff;color: #fff;">保存</button>
			</uni-forms>
		</view>


		<!-- 模板 -->
		<view class="showTemplate" v-if="showTemplate">
			<scroll-view style="height: 100%;" scroll-y="true">
				<view class="" style="padding:0 30rpx;box-sizing: border-box; padding-bottom: 260rpx;">
					<view class="templateBox" v-for="(item,index) in templateData">

						<input v-model="item.Id" style="display: none;">
						<input v-model="item.WechatFriendRequestTaskId" style="display: none;">
						<input v-model="item.mode" style="display: none;">

						<uni-forms ref="form" :modelValue="item" label-position="top" label-width="200px">
							<uni-forms-item label="消息类型">
								<uni-section type="line">
									<view class="uni-px-5">
										<uni-data-checkbox mode="tag" v-model="item.MsgType"
											:localdata="[{text: '文本',value: 1},{text: '图片',value: 3},{text: '视频',value: 43}]"></uni-data-checkbox>
									</view>
								</uni-section>
							</uni-forms-item>

							<uni-forms-item label="内容">
								<block v-if="item.MsgType == 1">
									<uni-easyinput type="textarea" v-model="item.Content"
										placeholder="请输入内容"></uni-easyinput>
								</block>

								<block v-else-if="item.MsgType == 3 || item.MsgType == 43">
									<input v-model="item.Content" style="display: none;">

									<block v-if="item.MsgType == 3">
										<view class="" v-if="item.ImageUrl">
											<image :src="item.ImageUrl" mode="widthFix" style="width: 300rpx;"></image>
										</view>
										<button style="background: #2979ff;" class="mini-btn" type="primary" size="mini"
											@click="uploadImg(index)">上传图片</button>
									</block>
									<block v-else-if="item.MsgType == 43">
										<view class="" v-if="item.VideoUrl">
											<video :src="item.VideoUrl" style="width: 300rpx;"></video>
										</view>
										<button style="background: #2979ff;" class="mini-btn" type="primary" size="mini"
											@click="uploadVideo(index)">上传视频</button>
									</block>
								</block>
							</uni-forms-item>

							<uni-forms-item label="间隔时间(秒)">
								<uni-easyinput type="number" v-model="item.Interval"
									placeholder="请输入时间"></uni-easyinput>
							</uni-forms-item>

							<view class="btn2">
								<button @click.stop='saveTemplate(item,index)' class="addBtn"
									style="background: #2979ff;color: #fff;border-radius: 0;">保存</button>
								<button @click.stop='delTemplate(item,index)' class="closeBtn"
									style="background: red;color: #fff;border-radius: 0;">删除</button>
							</view>
						</uni-forms>
					</view>

					<view class="btn">
						<button @click.stop='addTemplate' class="addBtn"
							style="background: #2979ff;color: #fff;border-radius: 0;">新增</button>
						<button @click.stop="hideTemplate" class="closeBtn"
							style="background: red;color: #fff;border-radius: 0;">关闭</button>
					</view>

				</view>
			</scroll-view>
		</view>




	</view>
</template>

<script>
	export default {
		data() {
			return {
				showTemplate: false,
				taskId: '',
				// 表单数据
				formData: {
					Id: '',
					Name: '',
					Topic: '',
					Hello: '',
					MinFriendRequestInterval: '',
					MaxPerDayFriendRequestCount: '',
					Always: 1,
					EnableCollect: 1,
					templateType: '',
				},
				openid: '',
				templateData: [],
				rules: {
					Name: {
						rules: [{
							required: true,
							errorMessage: '请输入名称',
						}]
					},
					Topic: {
						rules: [{
							required: true,
							errorMessage: '请输入主题',
						}]
					},
					Hello: {
						rules: [{
							required: true,
							errorMessage: '请输入招呼语',
						}]
					},
					MinFriendRequestInterval: {
						rules: [{
							required: true,
							errorMessage: '请输入添加好友间隔',
						}]
					},
					MaxPerDayFriendRequestCount: {
						rules: [{
							required: true,
							errorMessage: '请输入每日最大添加好友数',
						}]
					},
				}
			}
		},
		onLoad(options) {
			this.taskId = options.taskId;
			this.companyId = options.companyId;
			this.openid = options.openid;
			if (this.taskId != '') {
				this.getData();
			}

		},
		methods: {
			//获取数据
			getData() {
				let that = this;
				uni.request({
					method: 'POST',
					url: that.$wxApi + '/api/app/hbhk/poster/getTaskData',
					data: {
						appid: that.$wxAppId,
						taskId: that.taskId,
						companyId: that.companyId,
						openid: that.openid
					},
					success: (res) => {
						if (res.data.code == 1) {
							that.formData = res.data.data
						} else {
							uni.showToast({
								title: res.data.msg,
								icon: "none",
							})
							uni.redirectTo({
								url: '/pages/about/about?taskId=' + that.taskId
							})
						}
					}
				});
			},

			//获取模板
			template(type) {
				let that = this;
				that.templateType = type;
				uni.request({
					method: 'POST',
					url: that.$wxApi + '/api/app/hbhk/poster/getTemplateData',
					data: {
						appid: that.$wxAppId,
						taskId: that.taskId,
						companyId: that.companyId,
						mode: type,
						openid: that.openid
					},
					success: (res) => {
						if (res.data.code == 1) {
							that.templateData = res.data.data;
							that.showTemplate = true;
						} else {
							uni.showToast({
								title: res.data.msg,
								icon: "none",
							})
						}
					}
				});
			},

			//关闭弹窗
			hideTemplate() {
				this.showTemplate = false;
				this.templateData = [];
				this.templateType = '';
			},
			//添加新话术
			addTemplate() {
				let data = {
					Id: 0,
					WechatFriendRequestTaskId: 0,
					MsgType: 1,
					mode: '',
					Content: '',
					Interval: '',
					VideoUrl: '',
					ImageUrl: '',
				}
				this.templateData.push(data)
			},
			//保存话术
			saveTemplate(data, index) {
				let that = this;
				if (data['Id'] == '') {
					data['mode'] = that.templateType;
					data['WechatFriendRequestTaskId'] = that.taskId;
				}
				uni.request({
					method: 'POST',
					url: that.$wxApi + '/api/app/hbhk/poster/saveTemplate',
					data: data,
					success: (res) => {
						uni.showToast({
							title: res.data.msg,
							icon: "none",
						})
					}
				});
			},


			//删除话术
			delTemplate(data, index) {
				let that = this;
				uni.showModal({
					title: '删除提示',
					content: `是否确认删除改话术`,
					success: res => {
						if (res.confirm) {
							if (data['Id'] != '') {
								uni.request({
									method: 'POST',
									url: that.$wxApi + '/api/app/hbhk/poster/delTemplate',
									data: data,
									success: (res) => {
										if (res.data.code == 1) {
											that.templateData.splice(index, 1)
										}
										uni.showToast({
											title: res.data.msg,
											icon: "none",
										})
									}
								});
							} else {
								that.templateData.splice(index, 1)
							}

						}
					}
				});
			},

			//提交
			submit() {
				this.$refs.form.validate().then(data => {
					data['Id'] = this.formData['Id'];
					data['CompanyId'] = this.formData['CompanyId'];
					data['Openid'] = this.openid;

					uni.request({
						method: 'POST',
						url: this.$wxApi + '/api/app/hbhk/poster/saveTask',
						data: data,
						success: (res) => {
							uni.showToast({
								title: res.data.msg,
								icon: "none",
							})
						}
					});
				}).catch(err => {
					uni.showToast({
						title: err[0]['errorMessage'],
						icon: "none",
					})

				})
			},
			// 上传图片
			uploadImg(index) {
				let _this = this;
				uni.chooseImage({
					count: 1,
					success: (chooseImageRes) => {
						uni.showLoading({
							mask: true,
							title: '上传中...'
						})
						const tempFilePaths = chooseImageRes.tempFilePaths;
						uni.uploadFile({
							url: _this.$wxApi + '/api/app/spsj/task/uploadFile',
							filePath: tempFilePaths[0],
							name: 'file',
							formData: {
								appid: _this.$wxAppId,
							},
							success: (res) => {
								uni.hideLoading();
								let data = JSON.parse(res['data'])
								if (data.code == 1) {
									_this.templateData[index]['ImageUrl'] = data.data;
									uni.showToast({
										icon: 'none',
										title: data.msg
									})
									// #ifndef MP-WEIXIN
									this.$forceUpdate();
									// #endif

									// #ifdef MP-WEIXIN
									this.$forceUpdate();
									// #endif

								} else {
									uni.showToast({
										icon: 'none',
										title: '图片上传失败'
									})
								}
							}
						});
					}
				});

			},
			// 上传视频
			uploadVideo(index) {
				let _this = this;
				uni.chooseVideo({
					count: 1,
					sourceType: ['camera', 'album'],
					success: (chooseVideo) => {
						uni.showLoading({
							mask: true,
							title: '上传中...'
						})
						const tempFilePaths = chooseVideo.tempFilePath;
						console.log(tempFilePaths)
						uni.uploadFile({
							url: _this.$wxApi + '/api/app/spsj/task/uploadFile',
							filePath: tempFilePaths,
							name: 'file',
							formData: {
								appid: _this.$wxAppId,
							},
							success: (res) => {
								uni.hideLoading();
								let data = JSON.parse(res['data'])
								if (data.code == 1) {
									_this.templateData[index]['VideoUrl'] = data.data;
									uni.showToast({
										icon: 'none',
										title: data.msg
									})
									// #ifndef MP-WEIXIN
									this.$forceUpdate();
									// #endif

									// #ifdef MP-WEIXIN
									this.$forceUpdate();
									// #endif

								} else {
									uni.showToast({
										icon: 'none',
										title: '图片上传失败'
									})
								}
							}
						});
					}
				});

			},
		}
	}
</script>

<style>
	.page {}

	.showTemplate {
		position: fixed;
		left: 0;
		top: 0;
		width: 100%;
		height: 100%;
		background: #fff;
		z-index: 100;
		padding-top: calc(var(--status-bar-height) + 60px);
	}

	.forms {
		padding: 30rpx;
		padding-bottom: 110rpx;
		box-sizing: border-box;
	}

	.templateBox {
		position: relative;
		border: 1px solid #ccc;
		padding: 30rpx;
		box-sizing: border-box;
		border-radius: 20rpx;
		margin: 30rpx 0;
		padding-bottom: 80rpx;
	}

	.btn2 {
		width: 100%;
		position: absolute;
		left: 0;
		bottom: 0;
		overflow: hidden;
		z-index: 100;
		border-radius: 0 0 20rpx 20rpx;
		overflow: hidden;
	}

	.btn2 button {
		width: 50%;
		float: left;
		height: 66rpx;
		line-height: 66rpx;
		font-size: 26rpx;
	}



	.btn {
		position: fixed;
		left: 0;
		bottom: 0;
		width: 100%;
		overflow: hidden;
		z-index: 100;
	}

	.btn button {
		width: 50%;
		float: left;
	}

	.submit {
		position: fixed;
		left: 0;
		bottom: 0;
		width: 100%;
		border-radius: 0;
	}
</style>