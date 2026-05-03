<template>
  <div v-loading="loading" :style="'height:' + height" element-loading-text="���ڼ���ҳ�棬���Ժ�">
    <iframe
      :id="iframeId"
      :src="src"
      frameborder="no"
      style="width: 100%; height: 100%"
    ></iframe>
  </div>
</template>

<script>
export default {
  props: {
    src: {
      type: String,
      default: "/"
    },
    iframeId: {
      type: String
    }
  },
  data() {
    return {
      loading: false,
      height: document.documentElement.clientHeight - 94.5 + "px;"
    };
  },
  mounted() {
    var _this = this;
    const iframeId = ("#" + this.iframeId).replace(/\//g, "\\/");
    const iframe = document.querySelector(iframeId);
    // iframeҳ��loading����
    if (iframe.attachEvent) {
      this.loading = true;
      iframe.attachEvent("onload", function () {
        _this.loading = false;
      });
    } else {
      this.loading = true;
      iframe.onload = function () {
        _this.loading = false;
      };
    }
  }
};
</script>
