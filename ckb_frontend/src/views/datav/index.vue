<template>
    <div id="data-view">
        <dv-full-screen-container>
            <top-header />
            <div class="main-content">
                <div class="column">Column 1</div>
                <div class="column">
                    <middle />
                </div>
                <div class="column">Column 3</div>
            </div>
        </dv-full-screen-container>
    </div>
</template>

<script>
import topHeader from './topHeader'
import middle from './middle'
export default {
    name: 'DataView',
    components: {
        topHeader, middle
    },
    data() {
        return {}
    },
    methods: {}
}
</script>

<style lang="scss" scoped>
#data-view {
    width: 100%;
    height: 100%;
    background-color: #030409;
    color: #fff;

    #dv-full-screen-container {
        background-image: url('~@/assets/images/bg.png');
        background-size: 100% 100%;
        box-shadow: 0 0 3px blue;
        display: flex;
        flex-direction: column;
    }

    .main-content {
        flex: 1;
        display: flex;
    }

    .column {
        flex: 1;
    }
}
</style>