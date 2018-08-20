<template>
  <div id="app">
    <router-view></router-view>
  </div>
</template>

<script>
export default {
  name: 'app'
}
</script>

<style>
#app {
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
  font-size: 1em;
}

#toast-error {
    background-color: rgba(255, 216, 209, .95);
    color: red;
    border: 2px solid red;
}

#toast-success {
    background-color: rgba(215, 255, 211, .95);
    color: green;
    border: 2px solid green;
}

#toast-success, #toast-error {
    border-top-width: 0;
    border-top-left-radius: 0;
    border-top-right-radius: 0;
    max-width: 50%;
    padding: 20px;
}

.hsy-dialog .mask {
    position: fixed;
    top: 0;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 2;
}

.hsy-dialog.info .main {
    min-width: 300px;
}

.hsy-dialog.info .main .body .success {
    color: green;
}

.hsy-dialog.info .main .body .failure {
    color: red;
}

.hsy-dialog.edit {
    overflow-y: auto;
}

.hsy-dialog.edit .main {
    min-width: 760px;
    width: 75%;
    z-index: 2000;
    top: 30px;
}

.hsy-dialog .main {
    z-index: 2;
}

.hsy-dialog .main .body {
    max-width: 100% !important;
}

.hsy-dialog.edit .main .body .control-label {
    padding-left: 0;
    padding-right: 0;
}

.text-nav {
    font-size: 1.1em;
    padding: 20px;
    margin: 0;
    background: rgba(220, 220, 220, 0.85);
    line-height: 34px;
    z-index: 1;
}

.margin-top90 {
    margin-top: 90px;
}

</style>
