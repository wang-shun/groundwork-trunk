<template>
  <div style="position: relative" :class="{open:showDropdown}">
    <input class="form-control"
      v-model="val"
      :placeholder="placeholder"
      :type.once="type"
      @blur="showDropdown = false; if(blur) blur()"
      @keydown.down.prevent="down"
      @keydown.enter="hit"
      @keydown.esc="reset"
      @keydown.up.prevent="up"
      @keyup="keypress"
      @click="keypress"
      autocomplete="off"
      autocorrect="off"
      autocapitalize="off"
      spellcheck="false"
    />
    <ul class="dropdown-menu" ref="dropdown">
      <li v-for="(item, i) in items" :class="{active: isActive(i)}">
        <a @mousedown.prevent="hit" @mousemove="setActive(i)">
          <component :is="templateComp" :item="item"></component>
        </a>
      </li>
    </ul>
  </div>
</template>

<script>
import {delayer, getJSON} from '../utils/utils.js'
var DELAY = 300

function caretPos(el) {
    var pos = 0;
    if (document.selection) {
        el.focus();
        var sel = document.selection.createRange();
        sel.moveStart('character', -el.value.length);
        pos = sel.text.length;
    }
    else if (el.selectionStart || el.selectionStart == '0') pos = el.selectionStart;
    return (pos);
}

export default {
  name: 'typeahead',
  props: {
    async: {type: String},
    data: {type: Array},
    blur: {
      type: Function,
      default () { return }
    },
    delay: {type: Number, default: DELAY},
    asyncKey: {type: String, default: null},
    limit: {type: Number, default: 99999},
    matchCase: {type: Boolean, default: false},
    matchStart: {type: Boolean, default: false},
    onHit: {
      type: Function,
      default (item) { return item }
    },
    placeholder: {type: String},
    template: {type: String},
    type: {type: String, default: 'text'},
    value: {type: String, default: ''}
  },
  data () {
    return {
      asign: '',
      showDropdown: false,
      noResults: true,
      current: 0,
      items: [],
      index: 0,
      tokens: [],
      val: this.value,
      fromHit: false
    }
  },
  computed: {
    templateComp () {
      return {
        template: typeof this.template === 'string' ? '<span>' + this.template + '</span>' : '<strong v-html="item"></strong>',
        props: { item: {default: null} }
      }
    }
  },
  watch: {
    val (val, old) {
      this.$emit('input', val);
      if (val !== old && val !== this.asign) this.__update(val, old)
    },
    value (val) {
      if (this.val !== val) { this.val = val }
    }
  },
  methods: {
    setItems (data, explicit) {
      if(explicit === false) {
        return;
      }

      if(this.$parent.$parent.modalFlag) {
        this.$parent.$parent.modalFlag = false;
        return;
      }

      if(this.fromHit) {
        this.fromHit = false;
        this.showDropdown = false;
        return;
      }

      if (this.async) {
        this.items = this.asyncKey ? data[this.asyncKey] : data
        this.items = this.items.slice(0, this.limit)
      } else {
        var query = this.matchCase ? (this.tokens[this.index] || '') : (this.tokens[this.index] ? this.tokens[this.index].toLowerCase() : '')
        query = query.trim();

        if(!this.tokens.length && !explicit) {
            return;
        }

        this.items = (data || []).filter(value => {
          if (typeof value === 'object') { return true }

          value = this.matchCase ? value : value.toLowerCase()
          return this.matchStart ? value.indexOf(query) === 0 : value.indexOf(query) !== -1
        }).slice(0, this.limit)
      }

      this.showDropdown = this.items.length > 0
    },
    setValue (value) {
      if(this.val) {
        var tokensWithDelimeters = this.val.split(/([\(\)\,\;\*\:\s+])/).filter(String);

        for(var i = 0, iLimit = tokensWithDelimeters.length; i < iLimit; i++) {
          var part = tokensWithDelimeters[i];

          if(i == this.index) {
            tokensWithDelimeters[i] = value;
            break;
          }
        }

        this.val = tokensWithDelimeters.length ? tokensWithDelimeters.join('') : value;
      }
      else {
        this.val = value;
      }

      this.asign = value
      this.items = []
      this.tokens = []
      this.index = 0
      this.loading = false
      this.showDropdown = false
    },
    reset () { this.showDropdown = false },
    setActive (index) { this.current = index },
    isActive (index) { return this.current === index },
    hit (e) {
      e.preventDefault()
      this.fromHit = true
      this.setValue(this.onHit(this.items[this.current], this))
    },
    up (e) {
      if (this.current > 0) { this.current-- }
      else { this.current = this.items.length - 1 }

        var ul = this.$refs.dropdown,
            li = ul.children[this.current];

        ul.scrollTop = li.offsetHeight * this.current;
    },
    down () {
      this.$parent.$parent.modalFlag = false;

      if(!this.showDropdown) {
        if (this.data) {
          this.setItems(this.data, true)
        }
      }
      else {
        if (this.current < this.items.length - 1) { this.current++ }
        else { this.current = 0 }

        var ul = this.$refs.dropdown,
            li = ul.children[this.current];

        ul.scrollTop = li.offsetHeight * this.current;
      }
    },
    keypress (e) {
      this.$parent.$parent.modalFlag = false;

      if(!this.val) {
        this.tokens = [];
        this.index = 0;
      }
      else {
        var pos = caretPos(e.target);

        this.tokens = this.val.split(/([\(\)\,\;\*\:\s+])/).filter(String);
        var pos_ = 0;

        for(var i = 0, iLimit = this.tokens.length; i < iLimit; i++) {
          var part = this.tokens[i];

          if(pos == pos_) {
            this.index = i - 1;
            return;
          }

          if(pos <= pos_) {
            this.index = i - 1;
            return;
          }

          pos_ += part.length;
        }

        if(pos <= pos_) {
          this.index = i - 1;
          return;
        }

        this.index = 0;
      }
    }
  },
  created () {
    this.__update = delayer(function (val, old) {
      if (!this.val) {
        this.reset()
        return
      }

      this.asign = ''

      if (this.async) {
        getJSON(this.async + this.val).then(data => {
          this.setItems(data)
        })
      } else if (this.data) {
        this.setItems(this.data)
      }
    }, 'delay', DELAY);

    // Same as __update, but don't show explicitly:
    if (!this.val) {
      this.reset()
      return
    }

    this.asign = ''

    if (this.async) {
      getJSON(this.async + this.val).then(data => {
        this.setItems(data)
      })
    } else if (this.data) {
      this.setItems(this.data, false)
    }
  }
}
</script>

<style>
.dropdown-menu {
  max-height: 400px;
  overflow: auto;
}

.dropdown-menu > li > a {
  cursor: pointer;
}
</style>
