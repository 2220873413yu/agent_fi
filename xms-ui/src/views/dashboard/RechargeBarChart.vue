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
    setOptions({rechargeEnergyPool, key, lineType, lineTypePool, rechargeUsdt, rechargeDfc, rechargeOort} = {}) {
      const isMultiLine = Array.isArray(lineType)
      const colorList = ['#409EFF', '#E6A23C', '#67C23A', '#909399']
      const dataMap = {
        USDT: rechargeUsdt || [],
        DFC: rechargeDfc || [],
        OORT: rechargeOort || []
      }
      const series = isMultiLine
        ? lineType.map((name, index) => ({
          type: 'bar',
          name,
          stack: 'total',
          barWidth: '20px',
          itemStyle: {
            color: colorList[index % colorList.length],
            borderRadius: [4, 4, 0, 0]
          },
          emphasis: {
            itemStyle: {
              color: colorList[index % colorList.length]
            }
          },
          data: dataMap[name] || []
        }))
        : [
          {
            type: 'bar',
            name: lineType,
            stack: 'total',
            barWidth: '20px',
            itemStyle: {
              color: '#409EFF',
              borderRadius: [4, 4, 0, 0]
            },
            emphasis: {
              itemStyle: {
                color: '#66B1FF'
              }
            },
            data: rechargeUsdt
          },
          {
            type: 'bar',
            name: lineTypePool,
            stack: 'total',
            barWidth: '20px',
            itemStyle: {
              color: '#67C23A',
              borderRadius: [4, 4, 0, 0]
            },
            emphasis: {
              itemStyle: {
                color: '#85CE61'
              }
            },
            data: rechargeEnergyPool
          }
        ]
      this.chart.setOption({
        title: {
          text: this.title,
          left: 'center',
          top: 10,
          textStyle: {
            color: '#303133',
            fontSize: 16,
            fontWeight: 600
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
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
            label: {
              backgroundColor: '#409EFF'
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
        grid: {
          left: 15,
          right: 15,
          bottom: 20,
          top: 50,
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: key || [],
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
        },
        yAxis: {
          type: 'value',
          axisLine: {
            show: false
          },
          axisTick: {
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
        animation: {
          duration: 300
        },
        series
      })
    }
  }
}
</script>
