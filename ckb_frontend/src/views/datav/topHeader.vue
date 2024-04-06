<template>
    <div id="top-header">
        <dv-decoration-8 class="header-left-decoration" />
        <dv-decoration-5 class="header-center-decoration" />
        <dv-decoration-8 class="header-right-decoration" :reverse="true" />
        <div class="center-title">卡若私域运营数据监测大屏</div>
        <div class="center-date">{{ currentTime }}</div>
    </div>
</template>
  
<script>
export default {
    name: 'TopHeader',
    data() {
        return {
            currentTime: '',
            timer: null
        }
    },
    mounted: function () {
        this.updateTime();
        setInterval(() => {
            this.updateTime();
        }, 1000);
    },
    methods: {
        updateTime() {
            const now = new Date();
            const hours = now.getHours().toString().padStart(2, '0');
            const minutes = now.getMinutes().toString().padStart(2, '0');
            const seconds = now.getSeconds().toString().padStart(2, '0');
            const year = now.getFullYear();
            const month = (now.getMonth() + 1).toString().padStart(2, '0');
            const day = now.getDate().toString().padStart(2, '0');
            this.currentTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
        }
    },
    beforeDestroy() {
        clearInterval(this.timer);
    }
}
</script>
  
<style lang="scss" scoped>
#top-header {
    position: relative;
    width: 100%;
    height: 100px;
    display: flex;
    justify-content: space-between;
    flex-shrink: 0;

    .header-center-decoration {
        width: 40%;
        height: 60px;
        margin-top: 30px;
    }

    .header-left-decoration,
    .header-right-decoration {
        width: 25%;
        height: 60px;
    }

    .center-title {
        position: absolute;
        font-size: 30px;
        font-weight: bold;
        left: 50%;
        top: 15px;
        transform: translateX(-50%);
    }

    .center-date {
        position: absolute;
        font-size: 20px;
        left: 50%;
        top: 100px;
        transform: translateX(-50%);
    }
}
</style>