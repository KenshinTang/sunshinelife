/**
 * Created by lemon on 2017/5/10.
 */


//url---string类型，当前获取列表的url
//params———object类型，当前获取列表所需参数，pagesize与pageno可不传
//cb———funciton类型，获取完列表后的回调函数（刷新、加载更多后均会回调，注意回调次数）
//sid———string类型，scroll全局容器的id，用于mui初始化
//cid———string类型，列表容器的id
//get———bool类型，是否为ger请求，用于后台出现需要post情况
//必须实现对象的getItem方法，return当前行元素
//   var m = new muiScroll("aaa",{},function(){})
//   m.getItem=function(d){return "<div>"+d.name+"</div>"};
function muiScroll(url, params, cb, sid, cid, get) {
    sid&&(this.sid=sid);
    cid&&(this.cid=cid);
    get==false&&(this.get=get);
    url&&(this.url=url);
    params&&(this._params=params);
    this.scrollcb=cb;

    var me = this;
  console.log(me);
    mui.init({
        pullRefresh : {
            container:"#"+this.sid,
            down : {
                height:50,
                contentdown : "下拉可以刷新",
                contentover : "释放立即刷新",
                contentrefresh : "正在刷新...",
                callback :function () {
                    me.pulldownRefresh();
                }
            },
            up : {
                auto:true,
                height:50,
                contentrefresh : "正在加载...",
                contentnomore:'没有更多数据了',
                callback :function () {
                    me.pullupLoadMore();
                }
            }
        }
    });
}
muiScroll.request=function (g,p,cb) {
    if(g){
        AJAX.GET(p.url, cb);
    } else {
        AJAX.POST(p.url, p.data, cb);
    }
};
muiScroll.prototype.firstload=false;
muiScroll.prototype.curpage=0;
muiScroll.prototype.sid="refreshContainer";
muiScroll.prototype.cid="listView";
muiScroll.prototype.get=true;
muiScroll.prototype.url="";
muiScroll.prototype._params={};
muiScroll.prototype.params=function (obj) {
    if(obj)this._params=obj;
    return this._params;
};
muiScroll.prototype.scrollcb=function () {};
muiScroll.prototype.pulldownRefresh = function () {
    this.getList(true, false);
};
muiScroll.prototype.pullupLoadMore = function () {
    this.curpage++;
    this.getList(false, this.firstload);
    this.firstload=false;
};
muiScroll.prototype.getList = function (refresh, loading) {
    refresh&&(this.curpage=1);
    loading&&Comm.loading(true);
    this._params.pagesize=config.pagesize;
    this._params.pageno=this.curpage;
    var url = this.url;
    if(this.get){
        url +="?";
        for(var i in this._params){
            url += (i+"="+this._params[i]+"&");
        }
        url=url.slice(0,url.length-1);
    }
    var me = this;
    muiScroll.request(this.get,{url:url,data:this._params}, function (r) {
        var eles = [];
        if(r.code==1){
            !r.data&&(r.data=[]);
            me.pages = parseInt((r.totalCount-1)/config.pagesize)+1;
            for(var i=0;i<r.data.length;i++){
                eles.push(me.getItem(r.data[i]));
            }
        } else {
            mui.toast(r.msg);
        }
        if(refresh) {
            $("#"+me.cid).html(eles.join(""));
            mui('#'+me.sid).pullRefresh().endPulldownToRefresh();
            mui('#'+me.sid).pullRefresh().refresh(true);
        } else {
            $("#"+me.cid).append(eles.join(""));
        }
        var nomore = me.curpage>=me.pages;
        mui('#'+me.sid).pullRefresh().endPullupToRefresh(nomore);
        nomore&&($(".mui-pull-bottom-pocket").addClass("mui-visibility"));

        loading&&Comm.loading();
        me.scrollcb&&me.scrollcb();
    });
};
muiScroll.prototype.getItem = function (d) {return "";};