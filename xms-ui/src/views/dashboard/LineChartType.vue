<template>
  <div :class="className" :style="{height:height,width:width}"/>
</template>

<script>
import * as echarts from 'echarts';

require('echarts/theme/macarons') // echarts theme
import resize from './mixins/resize'

const animationDuration = 6000

export default {
  mixins: [resize],
  props: {
    className: {
      type: String,
      default: 'chart'
    },
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '300px'
    },
    title: {
      type: String,
      default: ''
    },
    title1: {
      type: String,
      default: ''
    },
    autoResize: {
      type: Boolean,
      default: true
    },
    chartData: {
      type: Object,
      required: true
    },

  },
  data() {
    return {
      chart: null
    }
  },
  watch: {
    chartData: {
      deep: true,
      handler(val) {
        this.setOptions(val)
      }
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initChart()
    })
  },
  beforeDestroy() {
    if (!this.chart) {
      return
    }
    this.chart.dispose()
    this.chart = null
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$el, 'macarons')
      this.setOptions(this.chartData)
    },
    setOptions({ orderAmount, rewardAmount,key } = {}) {
      this.chart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            crossStyle: {
              color: '#999'
            }
          }
        },
        legend: {},
        xAxis: [
          {
            type: 'category',
            data: key,
            axisPointer: {
              type: 'shadow'
            }
          }
        ],
        grid: {
          left: 40,
          right: 10,
          bottom: 20,
          top: 30,
          containLabel: true
        },
        yAxis: [
          {
            type: 'value',
            name: this.title,
            // min: 0,
            // max: 250,
            // interval: 50
          },
          {
            type: 'value',
            name: this.title1,
            // min: 0,
            // max: 25,
            // interval: 5
          }
        ],
        series: [
          {
            name: '近60天报单数量',
            type: 'bar',
            animationDuration: 2800,
            animationEasing: 'cubicInOut',
            itemStyle: {color: 'rgb(249, 134, 51)'},
            data: orderAmount
          },
          {
            name: '近60天拨比数量',
            itemStyle: {color: 'rgb(56, 96, 245)'},
            type: 'line',
            animationDuration: 2800,
            animationEasing: 'cubicInOut',
            data: rewardAmount
          }
        ]
      })
    }
  }
}
</script>
