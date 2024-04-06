<template>
    <div class="dashboard">
        <!-- 数据统计模块 -->
        <div class="stats">
            <div class="stat-item">

                <div>设备总数</div>

                <div class="card">2</div>
                <div class="card">1</div>

                <div style="margin-left: 5px;">台</div>

            </div>
            <div class="stat-item">
                <div>微信总数</div>
                <div class="card">3</div>
                <div class="card">2</div>
                <div class="card">3</div>
                <div style="margin-left: 5px;">个</div>
            </div>
            <div class="stat-item">
                <div>好友总数</div>
                <div class="card">3</div>
                <div class="card">8</div>
                <div class="card">5</div>
                <div class="card">2</div>
                <div class="card">1</div>

                <div style="margin-left: 5px;">人</div>
            </div>
        </div>
        <!-- 地图模块 -->
        <div class="map-container">
            <div id="chinaMap" style="width: 100%; height: 100%;"></div>
        </div>
    </div>
</template>
  
<script>
import chinaMapData from '@/assets/data/china.json'; // Make sure this path is correct depending on where you store the map data

export default {
    name: 'Middle',

    data() {
        return {
            config: {

            }
        };
    },
    mounted() {
        var myChart = this.$echarts.init(document.getElementById('chinaMap'));
        this.$echarts.registerMap('china', chinaMapData)
        var option = {
            series: [{
                type: 'map',
                map: 'china',
                zoom: 1.2
                // ... 其他配置
            }]
        };
        myChart.setOption(option);
    }

}
</script>
<style scoped>
.dashboard {
    display: flex;
    flex-direction: column;
    height: 100%;
    width: 100%;
}

.stats {
    display: flex;
    justify-content: space-around;
    align-items: center;
    padding: 10px;
    height: 200px;

    /* 透明背景 */
}

.stat-item {
    display: flex;
    font-size: 20px;
    text-align: center;
    color: #fff;
    margin: 0 10px;
    line-height: 44px;
}

.map-container {
    flex: 5;
    /* 地图模块占据更多的空间 */
    position: relative;
}

#chinaMap {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
}

.card {
    background-color: rgba(4, 49, 128, .6);
    color: #08e5ff;
    height: 40px;
    width: 40px;
    font-size: 25px;
    font-weight: bold;
    line-height: 40px;
    text-align: center;
    margin-left: 10px;
}
</style>