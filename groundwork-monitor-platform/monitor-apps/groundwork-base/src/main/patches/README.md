Patches to JavaScript Resources
=================================

Date: May 14, 2015
From: David S Taylor, Dmitriy Khudorozhkov

We've experienced some problems with including JavaScript libraries into JBoss Portal. Usually this is due to
integration issues with require.js or similar module frameworks. This document describes the patches we made
to JavaScript libraries to address these integrations issues.

Libraries patched:
- [D3](http://d3js.org/)
- [Bootstrap Date Picker](bootstrap-datepicker)

The patch involves the removal of require.js definition block at the very start of library (the following is
the code from bootstrap-datepicker):

```
   !function(a) {
      "use strict";
      if ("function" == typeof define && define.amd) define(["jquery", "moment"], a);
      else if ("object" == typeof exports) a(require("jquery"), require("moment"));
      else {
         if ("undefined" == typeof jQuery) throw "bootstrap-datetimepicker requires jQuery to be loaded first";
         if ("undefined" == typeof moment) throw "bootstrap-datetimepicker requires Moment.js to be loaded first";
         a(jQuery, moment)
      }
}
```

And then re-wrapping the library into following format (what it does: prevents global variable space pollution):

```
  (function() {
      // code follows
  }());
```

This patches directory includes the initial and the "fixed" variants of libraries (bootstrap-datepicker only provides minified version).
- bootstrap-datetimepicker.min-original.js
- bootstrap-datetimepicker.min.js
- d3-original.js
- d3.js

The patched versions do not hve the -original extension.

### Versions
- d3 : 3.5.5
- datetimepicker - 4.7.14

### Patched Files

The patched files are also located in this project under:

- src/main/webapp/app/scripts/d3/d3.js
- src/main/webapp/app/scripts/bootstrap-datepicker/bootstrap-datetimepicker.min.js

These files are included in the src/main/webapp/WEB-INF/wro.xml definition:

```
    <js minimize="false">/app/scripts/d3/d3.js</js>
    <js minimize="false">/app/scripts/bootstrap-datepicker/bootstrap-datetimepicker.min.js</js>
```



