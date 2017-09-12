export class LoadingMore {
  static handleData(d, cb) {
    if (d.length >= 10) {
      d = d.splice(0, 10);
      cb && cb(d, true);
    } else {
      cb && cb(d, false);
    }
  }
}

export class MToast {
  static presentBottomToast(text, ctrl, dismiss=null) {
    if(text) {
      let toast = ctrl.create({
        message: text,
        duration: 2000,
        position: 'bottom'
      });
      if(dismiss)
        toast.onDidDismiss(dismiss);
      toast.present();
    }
  }
}

export class timeFormat {
  //时间转换 t:时间戳, f: 时间格式(yy-MM-dd hh:mm:ss)
  static timeFormatAll(t, f) {
    var d1 = new Date(t);
    var year = d1.getFullYear();
    var month = d1.getMonth() + 1;
    var month2 = "";
    month < 10 ? (month2 = "0" + month) : (month2 = month + "");
    var data = d1.getDate();
    var data2 = "";
    data < 10 ? (data2 = "0" + data) : (data2 = data + "");
    var hours = d1.getHours();
    var hours2 = "";
    hours < 10 ? (hours2 = "0" + hours) : (hours2 = hours + "");
    var minutes = d1.getMinutes();
    var minutes2 = "";
    minutes < 10 ? (minutes2 = "0" + minutes) : (minutes2 = minutes + "");
    var seconds = d1.getSeconds();
    var seconds2 = "";
    seconds < 10 ? (seconds2 = "0" + seconds) : (seconds2 = seconds + "");

    f = f.replace(/yy/g, year);
    f = f.replace(/MM/g, month2);
    f = f.replace(/dd/g, data2);
    f = f.replace(/hh/g, hours2);
    f = f.replace(/mm/g, minutes2);
    f = f.replace(/ss/g, seconds2);

    return f;
  }
}

export class mAlert{
  static presentConfirm(t, m, ctrl, cb) {
    let alert = ctrl.create({
      title: t,
      message: m,
      buttons: [
        {
          text: '取消',
          role: 'cancel',
          handler: () => {

          }
        },
        {
          text: '确定',
          handler: () => {
            cb && cb();
          }
        }
      ]
    });
    alert.present();
  }
}
