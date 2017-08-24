var GDict = {
    _data: null, _d: {}, _a: null, _menuData: null,
    //通过id（int）或code（string）获取子节点[推荐]
    //source,是否引用原对象（默认false或null）不建议使用原始对象引用
    //enable 是否展示所有数据，默认为null(1),只显示已启用数据，-1显示所有数据
    get: function (v, source, enable) {
        if (enable == null) enable = 1;
        //如果传入ID，为int则先取得code
        if (typeof (v) == typeof (0)) {
            v = GDict.getItem(v);
            if (v == null) return [];
            v = v.dictcode;
        }
        var temp = [], a;
        if (GDict._data) {
            a = GDict._data[v];
            if (source) return a == null ? [] : a;
            if (v == 'category') temp.push({ dictid: 0, dictpid: -1, dictname: '无扩展', dictcode: 'null' });
            if (a) for (var i = 0; i < a.length; i++) {
                if (enable == -1 || enable == a[i].enable)
                    temp.push({
                        dictid: a[i].dictid, dictpid: a[i].dictpid, dictname: a[i].dictname, dictcode: a[i].dictcode,
                        orderindex: a[i].orderindex, remark: a[i].remark, dicttype: a[i].dicttype, enable: a[i].enable
                    });
            }
        }
        return temp;
    },
    //根据PID.Pcode，和code获取对象
    getItemByCode: function (pid, code) {
        var list = GDict.get(pid);
        for (var i = 0; i < list.length; i++)
            if (list[i].dictcode == code)
                return list[i];
        return null;
    },
    //通过id获取字典名称[推荐]
    getName: function (id) {
        var o = GDict.getItem(id);
        if (o) return o.dictname;
        return '-';
    },
    //通过id获取字典对象[推荐]
    getItem: function (id) {
        if (GDict._d[id])
            return GDict._d[id];
        return null;
    },
    //获取原始数据不推荐[性能极低]
    getList: function (havenull) {
        var data = [], d = GDict._a;
        if (havenull)
            data.push({ id: -1, name: '无上级', pid: 0 });
        for (var i = 0; i < d.length; i++) {
            data.push({ id: d[i].dictid, name: d[i].dictname, pid: d[i].dictpid, enable: d[i].enable });
        }
        return data;
    },
    //用户管理字典一般情况不可调用[性能极低]
    getManageList: function (haveroot) {
        var data = [], expanded = false, d = GDict._a;
        if (haveroot) {
            expanded = true;
            data.push({ id: 0, pid: -1, name: '字典类别', expanded: expanded });
        }
        if (d) for (var i = 0; i < d.length; i++) {
            if (d[i].dicttype == 'sys')
                data.push({ id: d[i].dictid, pid: d[i].dictpid, name: d[i].dictname + (haveroot ? '（' + d[i].dictcode + '）' : ''), expanded: expanded });
        }
        return data;
    },
    //是如加载成功
    loaded: function () { return GDict._data != null },
    //初始化字典
	init:function(data){
        if(data==null&&window._dict)data=window._dict;
		var cd = null, cs = 'cs';
		GDict._a = data;
		GDict._data = {};
		//序列化
		for (var i = 0; i < data.length; i++)
			GDict._d[data[i].dictid] = data[i];
		//构建树
		for (var i = 0; i < data.length; i++) {
			cd = data[i];
			if (cd.dictpid > 0) {
				if (GDict._d[cd.dictpid][cs] == null) {
					GDict._d[cd.dictpid][cs] = [];
					//构建类型
					GDict._data[GDict._d[cd.dictpid].dictcode] = GDict._d[cd.dictpid][cs];
				}
				//传递引用
				GDict._d[cd.dictpid][cs].push(cd);
			}
		}
	}
}
