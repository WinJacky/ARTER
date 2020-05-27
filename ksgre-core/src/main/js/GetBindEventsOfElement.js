var obj = $('div');
var events = $._data(objs[0], 'events');

var obj2String = function (_obj) {
    var t = typeof _obj;
    if(t != 'object' || _obj === null) {
        if(t == 'string') {
            _obj = '"' + _obj + '"';
        }
        return String(_obj);
    } else {
        if(_obj instanceof Date) {
            return _obj.toLocaleString();
        }

        var n, v, json = [], arr = (_obj && _obj.constructor == Array);
        for(n in _obj) {
            v = _obj[n];
            t = typeof v;
            if(t == 'string') {
                v = '"' + v + '"';
            } else if (t == 'object' && v != null){
                v = this.obj2String(v);
            }
            json.push((arr ? '' : '"' + n + '":') + String(v));
        }
        return (arr ? '[': '{') + String(json) + ( arr ? ']': '}');
    }
};