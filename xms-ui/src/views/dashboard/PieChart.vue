<template>
  <div :class="className" :style="{height:height,width:width}" />
</template>

<script>
import * as echarts from 'echarts';
require('echarts/theme/macarons') // echarts theme
import resize from './mixins/resize'

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
    chartData: {
      type: Object,
      default: () => ({
        legend: [],
        seriesData: []
      })
    }
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
    setOptions({ legend = [], seriesData = [] } = {}) {
      this.chart.setOption({
        tooltip: {
          trigger: 'item',
          formatter: '{b} : {c} ({d}%)'
        },
        legend: {
          left: 'center',
          bottom: 10,
          icon: 'circle',
          data: legend
        },
        series: [
          {
            type: 'pie',
            radius: ['45%', '70%'],
            center: ['50%', '45%'],
            avoidLabelOverlap: false,
            label: {
              show: true,
              formatter: '{b}\n{d}%',
              color: '#606266'
            },
            labelLine: {
              show: true,
              length: 12,
              length2: 6
            },
            data: seriesData,
            animationEasing: 'cubicInOut',
            animationDuration: 800
          }
        ]
      })
    }
  }
}
</script>
