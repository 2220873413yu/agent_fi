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
      default: '350px'
    },
    autoResize: {
      type: Boolean,
      default: true
    },
    chartData: {
      type: Object,
      required: true
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
    setOptions({ expectedData, actualData, key, seriesName = '每日注册用户数' } = {}) {
      this.chart.setOption({
        xAxis: [
          {
            type: 'category',
            data: key,
            axisPointer: {
              type: 'shadow'
            },
            axisLine: {
              lineStyle: {
                color: '#909399',
                width: 1
              }
            },
            axisTick: {
              show: false
            },
            axisLabel: {
              color: '#303133',
              fontSize: 14,
              margin: 12,
              fontWeight: 500
            }
          }
        ],
        grid: {
          left: 15,
          right: 15,
          bottom: 20,
          top: 40,
          containLabel: true
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            label: {
              backgroundColor: '#409EFF'
            },
            lineStyle: {
              color: 'rgba(64, 158, 255, 0.5)',
              width: 1
            },
            shadowStyle: {
              color: 'rgba(64, 158, 255, 0.1)'
            }
          },
          padding: [8, 12],
          backgroundColor: 'rgba(255, 255, 255, 0.9)',
          borderColor: '#eee',
          borderWidth: 1,
          textStyle: {
            color: '#303133',
            fontSize: 14,
            fontWeight: 500
          }
        },
        yAxis: {
          axisTick: {
            show: false
          },
          axisLine: {
            show: false
          },
          splitLine: {
            lineStyle: {
              color: '#eee',
              type: 'dashed'
            }
          },
          axisLabel: {
            color: '#303133',
            fontSize: 14,
            fontWeight: 500
          }
        },
        legend: {
          top: 10,
          right: 10,
          textStyle: {
            color: '#303133',
            fontSize: 14,
            fontWeight: 500
          }
        },
        animation: {
          duration: 300
        },
        series: [{
          name: seriesName,
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          showSymbol: false,
          lineStyle: {
            width: 3,
            color: '#5087EC'
          },
          itemStyle: {
            color: '#5087EC',
            borderWidth: 2
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [{
                offset: 0, color: 'rgba(80, 135, 236, 0.3)'
              }, {
                offset: 1, color: 'rgba(80, 135, 236, 0.1)'
              }]
            }
          },
          data: expectedData
        }]
      })
    }
  }
}
</script>
