<script setup>
import { ref, onMounted, watch } from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  title: {
    type: String,
    default: 'Activity Chart'
  },
  chartType: {
    type: String,
    default: 'bar' // 'bar' or 'line'
  }
});

const chartRef = ref(null);
let myChart = null;

const initChart = () => {
  if (!chartRef.value) return;
  myChart = echarts.init(chartRef.value);
  updateChart();
};

const updateChart = () => {
  if (!myChart) return;
  
  const dates = props.data.map(item => item.date);
  const values = props.data.map(item => item.value);

  const option = {
    title: {
      text: props.title,
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value',
      name: 'Hours'
    },
    series: [
      {
        data: values,
        type: props.chartType,
        itemStyle: {
          color: '#1989fa'
        },
        smooth: true,
        areaStyle: props.chartType === 'line' ? {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(25, 137, 250, 0.5)' },
              { offset: 1, color: 'rgba(25, 137, 250, 0)' }
            ])
        } : null
      }
    ]
  };

  myChart.setOption(option);
};

watch(() => props.data, () => {
  updateChart();
}, { deep: true });

onMounted(() => {
  initChart();
  window.addEventListener('resize', () => myChart && myChart.resize());
});
</script>

<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<style scoped>
.chart-container {
  width: 100%;
  height: 300px;
  background: white;
  border-radius: 12px;
  padding: 10px;
  margin-bottom: 20px;
}
</style>
