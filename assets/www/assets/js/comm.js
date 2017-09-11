/*全局配置*/
var Config={
	root:'http://39.108.54.14:8080/ygsh/',//接口根路径url
	ossroot:'https://ygsh.oss-cn-shenzhen.aliyuncs.com/',//oss根路径
  pagesize:10,
  isApp: true
};

var AJAX={
  url:function(){return "http://39.108.54.14:8080/ygsh/"},
  get:function(api, obj, cb, err){
    var user = CommonFunction.get("user");
    if (user) {
      obj.appid = user.appid;
      obj.timestamp = user.timestamp;
      obj = this.objKeySort(obj);
      obj.token = user.token;
      var arr1 = [];
      for (var i in obj) {
        arr1.push(i + "=" + obj[i]);
      }
      obj.sign = hex_md5(arr1.join("&")).toUpperCase();
      delete obj.token;
    }

    $.ajax({
      type: "get",
      url:AJAX.url()+api,
      data: obj,
      timeout: 1000*10,
      success: function (a) {
        console.log(a);
        if(a.code == 1) {
          cb&&cb(a.data);
        } else if (a.code == 110) {
          err&&err("该账号已在其他地方登录", 110);
          //CommonFunction.remove('user');
          //CommonFunction.remove('__utoken');
          //Comm.gotop("login.html");
          //Comm.message("");
        } else if (a.code == 140) {
          err&&err("绑定手机号参数缺失", 140);
        } else {
          err&&err(a.msg);
        }
      },
      error: function () {
        err&&err("网络错误");
      },
    });
  },
  post:function(api, obj, cb, err){
    var user = CommonFunction.get("user");
    if (user) {
      obj.appid = user.appid;
      obj.timestamp = user.timestamp;
      obj = this.objKeySort(obj);
      obj.token = user.token;
      var arr1 = [];
      for (var i in obj) {
        arr1.push(i + "=" + obj[i]);
      }
      obj.sign = hex_md5(arr1.join("&")).toUpperCase();
      delete obj.token;
    }

    $.ajax({
      type: "post",
      url:AJAX.url()+api,
      data: obj,
      timeout: 1000*10,
      success: function (a) {
        console.log(a);
        if(a.code == 1) {
          cb&&cb(a.data);
        } else if (a.code == 110) {
          //CommonFunction.remove('user');
          //CommonFunction.remove('__utoken');
          //Comm.gotop("login.html");
          //Comm.message("该账号已在其他地方登录");
        } else {
          err&&err(a.msg);
        }
      },
      error: function () {
        err&&err("网络错误");
      },
    });
  },
  objKeySort: function(obj) {//排序的函数
    var newkey = Object.keys(obj).sort();
    //先用Object内置类的keys方法获取要排序对象的属性名，再利用Array原型上的sort方法对获取的属性名进行排序，newkey是一个数组
    var newObj = {};//创建一个新的对象，用于存放排好序的键值对
    for (var i = 0; i < newkey.length; i++) {//遍历newkey数组
      newObj[newkey[i]] = obj[newkey[i]];//向新创建的对象中按照排好的顺序依次增加键值对
    }
    return newObj;//返回排好序的新对象
  }
};

var CommonFunction = {
  //短信重发限制
  noteLimit:function(classN){
    if(pageData.registerFlag==true){
      pageData.registerFlag=false;
      setTimeout(function(){
        pageData.registerFlag=true;
      },1000*60+1000);
    }
    var d1=document.getElementsByClassName(classN)[0];
    $("." + classN).addClass("codeColor2");
    var x=1*60;
    var time1=setInterval(function(){
      x=x-1;
      d1.innerHTML=x+"秒后重发";
      if(x==0){
        clearInterval(time1);
      }
    },1000*1);
    var time2=setTimeout(function(){
      console.log(d1,classN);
      $("." + classN).removeClass("codeColor2");
      d1.innerHTML="获取验证码";
    },1000*60+2000);
  },
  //请求处理
  requireData: function (data, success, error) {
    if(data.code == 1) {
      success&&success(data.data)
    } else if (data.code == 110) {
      CommonFunction.remove('user');
      CommonFunction.remove('__utoken');
      Comm.gotop("login.html");
      Comm.message("该账号已在其他地方登录");
    } else if (data.code == 88) {
      CommonFunction.remove('user');
      CommonFunction.remove('__utoken');
      Comm.gotop("login.html");
      Comm.message("出错了！账户已被禁用，请联系客服");
    } else {
      error&&error(data.msg);
      Comm.message(data.msg);
    }
  },
  //Page请求处理
  requirePageData: function (data, success, error) {
    if(data.code == 1) {
      success&&success(data.data)
    } else if (data.code == 110) {
      CommonFunction.remove('user');
      CommonFunction.remove('__utoken');
      Comm.gotop("login.html");
      Comm.message("该账号已在其他地方登录");
    } else if (data.code == 88) {
      CommonFunction.remove('user');
      CommonFunction.remove('__utoken');
      Comm.gotop("login.html");
      Comm.message("出错了！账户已被禁用，请联系客服");
    } else {
      error&&error(data.msg);
      Comm.message(data.msg);
    }
  },
  //打印
  print: function (a, b, c) {
    console.log(a, b, c);
  },
  save: function (key, obj) {
    key = 'ygsh_y_' + key;
    localStorage.setItem(key, JSON.stringify(obj));
  },
  get: function (key) {
    key = 'ygsh_y_' + key;
    var obj = JSON.parse(localStorage.getItem(key));
    return obj;
  },
  remove: function (key) {
    key = 'ygsh_y_' + key;
    localStorage.removeItem(key);
  },
  //获取主价格
  getMainPrice: function(price){
    return (price+"").split(".")[0];
  },
  //获取副价格
  getSubPrice: function(price){
    return (price+"").split(".")[1] ? ((price+"").split(".")[1].length > 1 ? (price+"").split(".")[1] : (price+"").split(".")[1] + "0") : "00";
  },
  //获取总价格
  getSumPrice: function(p){
    return this.getMainPrice(p) + "." + this.getSubPrice(p);
  },
  //获取url参数
  getUrlQueryString: function(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return (r[2]); return null;
  },
  //储存历史搜索
  savesearch: function(o){
    var u = Comm.db("user");
    console.log(u);
    var addFlag = true;
    if(Comm.db("search_" + u.recommendsn) == null){
      var arr1 = [];
      arr1.unshift(o);
      Comm.db("search_" + u.recommendsn, arr1);
    }else{
      var arr1 = Comm.db("search_" + u.recommendsn);
      for(var index in arr1){
        if(arr1[index].keyword == o.keyword){
          addFlag = false;
          break;
        }
      }
      if(arr1.length < 12 && addFlag == true){
        arr1.unshift(o);
        Comm.db("search_" + u.recommendsn, arr1);
      }else if(arr1.length >= 12 && addFlag == true){
        arr1.unshift(o);
        arr1.pop();
        Comm.db("search_" + u.recommendsn, arr1);
      }
    }
  },
  //储存歌曲历史搜索
  savesong: function(o){
    var u = Comm.db("user");
    var addFlag = true;
    if(Comm.db("song_h_" + u.recommendsn) == null){
      var arr1 = [];
      arr1.unshift(o);
      Comm.db("song_h_" + u.recommendsn, arr1);
    }else{
      var arr1 = Comm.db("song_h_" + u.recommendsn);
      for(var index in arr1){
        if(arr1[index].babysongid == o.babysongid){
          addFlag = false;
          break;
        }
      }
      if(arr1.length < 12 && addFlag == true){
        arr1.unshift(o);
        Comm.db("song_h_" + u.recommendsn, arr1);
      }else if(arr1.length >= 12 && addFlag == true){
        arr1.unshift(o);
        arr1.pop();
        Comm.db("song_h_" + u.recommendsn, arr1);
      }
    }
  },
  //时间转换 t:时间戳, f: 时间格式(yy-MM-dd hh:mm:ss)
  timeFormat:function(t, f){
    var d1 = new Date(t);
    var year = d1.getFullYear();
    var month = d1.getMonth() + 1;
    month < 10 && (month = "0" + month);
    var data = d1.getDate();
    data < 10 && (data = "0" + data);
    var hours = d1.getHours();
    hours < 10 && (hours = "0" + hours);
    var minutes = d1.getMinutes();
    minutes < 10 && (minutes = "0" + minutes);
    var seconds = d1.getSeconds();
    seconds < 10 && (seconds = "0" + seconds);

    f = f.replace(/yy/g, year);
    f = f.replace(/MM/g, month);
    f = f.replace(/dd/g, data);
    f = f.replace(/hh/g, hours);
    f = f.replace(/mm/g, minutes);
    f = f.replace(/ss/g, seconds);

    return f;
  },
  //重新计算倒计时间
  timeSurplus:function(finishTime){
    var num1=finishTime.length;
    var returndata=[];
    for(var i=0;i<num1;i++){
      var timeSurplus=finishTime[i];
      var d1=new Object();
      d1.h=parseInt(timeSurplus/(60*60));
      d1.m=parseInt(timeSurplus/60%60);
      d1.s=parseInt(timeSurplus%60);
      if(d1.h<10){
        d1.h='0'+d1.h;
      }
      if(d1.m<10){
        d1.m='0'+d1.m;
      }
      if(d1.s<10){
        d1.s='0'+d1.s;
      }
      returndata.push(d1);
    }
    return returndata;
  },
  removeEmoji: function(id) {
    var regStr = /[\uD83C|\uD83D|\uD83E][\uDC00-\uDFFF][\u200D|\uFE0F]|[\uD83C|\uD83D|\uD83E][\uDC00-\uDFFF]|[0-9|*|#]\uFE0F\u20E3|[0-9|#]\u20E3|[\u203C-\u3299]\uFE0F\u200D|[\u203C-\u3299]\uFE0F|[\u2122-\u2B55]|\u303D|[\A9|\AE]\u3030|\uA9|\uAE|\u3030/ig;
    var org_val = $("#" + id).val().toString();

    if(regStr.test(org_val)){
      $("#" + id).val(org_val.replace(regStr,""));
    }
  }
};






var SubmitEvent = {
    regDict: {
        m: {t: '手机号码格式错误', e: /^1[34578]\d{9}$/},
        p: {t: '密码格式错误（6-16位）', e: /^[a-zA-Z0-9]{6,16}$/},
        c: {t: '验证码格式错误', e: /^.{6,6}$/},
        length4: {t: '位数错误（4-20位）', e: /^.{4,20}$/},
        required: {t: '必填项', e: /^.{1,999}$/},
        money: {t: '价格格式错误', e: /(^[1-9]\d*(\.\d{1,2})?$)|(^0(\.\d{1,8})?$)/},
        amount: {t: '数量格式错误', e: /(^[1-9]\d*(\.\d{1,2})?$)|(^0(\.\d{1,8})?$)/},
        bankac: {t: '位数错误（10-20位）', e: /^.{10,20}$/},
        email: {t: '邮箱格式错误', e: /^[A-Za-z0-9\u4e00-\u9fa5]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/},
        nz: {t: '请输入正整数', e: /^\d+$/},
        rn: {t: '请输入正确姓名', e: /[\u4E00-\u9FA5]{2,5}(?:·[\u4E00-\u9FA5]{2,5})*/},
        cd: {t: '请输入正确身份证号', e: /(^[1-9]\d{5}(18|19|([23]\d))\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$)|(^[1-9]\d{5}\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\d{2}$)/
        }
    },
    getParams:function (n) {
        !n&&(n="param");
        var valok=true;
        var obj = {};
        $("["+n+"]").each(function () {
            var p = this.getAttribute(n);
            if(p){
                obj[p]="";
                if(this.tagName=="INPUT" || this.tagName=="TEXTAREA" ){
                    obj[p] = this.value;
                } else {
                    var d = this.getAttribute("data");
                    if(d) {
                        obj[p] = d;
                    }
                }
            }
            var va = this.getAttribute("va");
            var reg = SubmitEvent.regDict[va];

            if(va && reg) {
                if(!reg.e.test(obj[p]) && valok) {
                    var vat = this.getAttribute("vat");
                    if(obj[p].toString().length>0)vat=null;
                    mui.toast(vat?vat:reg.t);
                    valok=false;
                }
            }
        });
        return valok?obj:null;
    },
    params:function (obj, n) {
        !n&&(n="param");
        $("["+n+"]").each(function () {
            var p = this.getAttribute(n);
            if(obj[p]){
                if(this.tagName=="INPUT" || this.tagName=="TEXTAREA" ){
                    this.value = obj[p];
                } else {
                    this.setAttribute("data",obj[p]);
                }
            }
        });
    }
};


//事件格式化
Date.prototype.format = function(format) {
    !format&&(format = "yyyy-MM-dd HH:mm:ss");
    var date = {
        "M+": this.getMonth() + 1,
        "d+": this.getDate(),
        "H+": this.getHours(),
        "m+": this.getMinutes(),
        "s+": this.getSeconds(),
        "q+": Math.floor((this.getMonth() + 3) / 3),
        "S+": this.getMilliseconds()
    };
    if (/(y+)/i.test(format)) {
        format = format.replace(RegExp.$1, (this.getFullYear() + '').substr(4 - RegExp.$1.length));
    }
    for (var k in date) {
        if (new RegExp("(" + k + ")").test(format)) {
            format = format.replace(RegExp.$1, RegExp.$1.length == 1
                ? date[k] : ("00" + date[k]).substr(("" + date[k]).length));
        }
    }
    return format;
};


var Tool = {
    getDict:function (list, key) {
        var dict = {};
        for(var i=0;i<list.length;i++){
            if(list[i][key]) {
                dict[list[i][key]] = list[i];
            }
        }
        return dict;
    },
    getIds:function (list, key) {
        var ids = [];
        for(var i=0;i<list.length;i++){
            if(list[i][key]) {
                ids.push(list[i][key]);
            }
        }
        return ids.length>0?ids.join(","):null;
    }

};
var Cart = {
    save:function (goods, count) {
        var cart = Comm.db("cart");
        !cart&&(cart=[]);
        var exist = -1;
        for(var i=0;i<cart.length;i++){
            if(cart[i].goodsid==goods.goodsid){
                exist = i;
                break;
            }
        }
        if(exist<0){
            cart.unshift({goodsid:goods.goodsid, count:parseInt(count)});
        } else {
            cart[i].count = parseInt(cart[i].count) + parseInt(count);
        }
        Comm.db("cart", cart);
    },
    get:function () {
        var cart = Comm.db("cart");
        !cart&&(cart=[]);
        return cart;
    },
    change:function (g, c) {
        var cart = Comm.db("cart");
        !cart&&(cart=[]);
        for(var i=0;i<cart.length;i++) {
            if(cart[i].goodsid==g){
                if(c) {
                    cart[i].count = c;
                } else {
                    cart.splice(i, 1);
                }
                break;
            }
        }
        Comm.db("cart", cart);
    },
    delete: function(a){
      if (a.length <= 0) {
        Comm.message("请选择要删除的商品");
      } else {
        var cart = Comm.db("cart");
        !cart&&(cart=[]);
        for (var i=0;i<a.length;i++) {
          for (var j=0;j<cart.length;j++) {
            if (a[i] == cart[j].goodsid) {
              cart.splice(j, 1);
              break;
            }
          }
        }
        Comm.db("cart", cart);
        Comm.message("已删除选中商品");
      }
    },
};

var __pageinfo={android_home:0,wback:[]};

var Comm = {
  upImg: function(url,cb) {
    __pageinfo.wback.push(cb);
    callNative('upImg', url);
  }
}

function callNative(m,d){
  if(ios()){
    var data={method:m+(d==null?'':':')};
    if(d)data['data']=dataToString(d);
    window.webkit.messageHandlers.WeiLai.postMessage(data);
  }
  else{
    if(window.WeiLai && (typeof window.WeiLai[m] == typeof function(){})){
      if(d==null)
        window.WeiLai[m]();
      else
        window.WeiLai[m](dataToString(d));
    }
  }
}
function ios(){return window.webkit!=null&&window.webkit.messageHandlers!=null;}
function dataToString(o){if(o==null)return '';if(typeof(o)==typeof({})||typeof(o)==typeof([]))return JSON.stringify(o);return o+'';}

function _w9_wcallback(data) {
  var temp = __pageinfo.wback.pop();
  //__pageinfo.wback = null;
  //android back
  if (data && data == 'ANDROID_000000') {
    window['androidback'] && window.androidback();
    return;
  }
  if (temp == null) return;
  var a = data + '';
  if (data.length > 5) {
    var ss = data.charAt(0), ee = data.charAt(data.length - 1);
    if ((ss == '{' && ee == '}') || (ss == '[' && ee == ']')) {
      try {
        a = JSON.parse(data);
      }
      catch (e) {
        a = data + '';
      }
    }
  }
  temp(a);
}

var mm = {
  alert: function(message, title, btnValue, cb) {
    mui.alert(message, title, btnValue, function(){
      cb && cb();
    });
  },
  confirm: function(message, title, btnValue, cb) {
    mui.confirm(message, title, btnValue, function(data){
      cb && cb(data);
    });
  },
  prompt: function(message, placeholder, title, btnValue, cb) {
    mui.prompt(message, placeholder, title, btnValue, function(data){
      cb && cb(data);
    });
  },
  toast: function(message, obj) {
    !obj && (obj = {duration: 'short'});
    mui.toast(message, obj);
  }
}

function area2(areaPicker, argustemp, cb) {
  Area.init(function () {
    areaPicker = new mui.PopPicker({layer: 3});
    areaPicker.setData(Area._d);
    if(argustemp) {
      argustemp=null;
      showPicker.call(argustemp);
    }
    cb && cb(areaPicker, argustemp);
  });
}

function showPicker(ee, areaPicker, argustemp, id) {
  var e = ee.target;
  console.log(e);
  if(areaPicker){
    areaPicker.show(function (v) {
      var aid = null;
      for(var i=0;i<v.length;i++){if(v[i].value)aid=v[i].value;}
      var at = (v.length>0&&v[0].text?v[0].text+' ':'') + (v.length>1&&v[1].text?v[1].text+' ':'') + (v.length>2&&v[2].text?v[2].text+' ':'');
      $("#" + id).val(at).attr("data", aid).css('color', '#001111');
    })
  } else {
    argustemp=arguments;
  }
}

//function slider(gallery, cb) {
//  gallery = mui('.mui-slider');
//  gallery.slider({
//    interval:2000//自动轮播周期，若为0则不自动播放，默认为0；
//  });
//  cb && cb(gallery);
//}

//var cordova = require('cordova');
//console.log(cordova);
//var nativeApiProvider = require('cordova/android/nativeapiprovider');
//var code = {};
//var nativeFunction = {
//  //n: 调用函数方法名, jsonObj: JSON格式, cb: 回调, t: 传给原生的时间戳，需要在回调的时候返回相同的时间戳
//  nativeCall: function (n, jsonObj, cb) {
//    var t = new Date().getTime();
//    code[t] = cb;
//    console.log(code);
//    //native_fns(n, jsonObj, t);
//    window._cordovaNative.toastMessage(jsonObj, t);
//  },
//  //d: JSON格式返回对象比如{code: 1, msg: 2}, t: 返回的时间戳
//  nativeCallback2: function (d, t) {
//    var f = code[t];
//    if(typeof f == 'function') {
//      console.log(f);
//      f(d);
//    }
//  },
//  nativeCallback: function (d) {
//    $(".data1").html(d);
//  },
//  test1: function(n, jsonObj, cb) {
//    var t = new Date().getTime();
//    code[t] = cb;
//    window._cordovaNative.toastMessage(jsonObj, t);
//  },
//  test2: function(n, jsonObj, cb) {
//    var t = new Date().getTime();
//    code[t] = cb;
//    nativeApiProvider.get().toastMessage(jsonObj, t);
//  },
//  test3: function(n, jsonObj, cb, err) {
//    console.log("test");
//    $(".data0").html("test");
//    var t = new Date().getTime();
//    code[t] = cb;
//    CusPlugin.testMethod(cb, err, {});
//  }
//}
