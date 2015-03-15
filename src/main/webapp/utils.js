'use strict';
/**
 * Created by highlandcows on 06/12/14.
 */
Function.prototype.method = function(name, func) {
    if (!this.prototype[name]) {
        this.prototype[name] = func;
        return this;
    }
};

// Array Remove - Based on John Resig (MIT Licensed)
Array.method('remove', function(from, to) {
    var rest = this.slice((to || from) + 1 || this.length);
    this.length = from < 0 ? this.length + from : from;
    return this.push.apply(this, rest);
});