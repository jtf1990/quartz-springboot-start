
function alertCloseableWarnMessage(message, closed) {
    message = message||"系统错误";
    var $m = $('<div class="alert alert-warning alert-dismissible in fade message-centor" style="z-index: 2000" role="alert">' +
        '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
        message +
        '</div>');
    if(typeof closed == "function"){
        $m.on('closed.bs.alert',closed);
    }else if(typeof closed == 'string'){
        try{
            $m.on('closed.bs.alert', function (){eval(closed);});
        }catch (e) {
            //
        }
    }
    $("body").append($m);
}

function findUrlParam(name) {
    if(name == null || name == undefined||name ==''){
        return  null;
    }
    var search = location.search;
    if(search == null || search == undefined || search == ''){
        return null;
    }
    var arr = [];
    search = search.substring(1);
    search = decodeURI(search);
    var strings = search.split("&");
    $.each(strings,function(i, v){
        try{
          var ss =  v.split("=");
          if(ss[0]==name&&ss[1]!=null&&ss[1]!=undefined){
              arr.push(ss[1]);
          }
        }catch (e) {
            // ignore
        }
    });
    return arr;
}

function findOneUrlParam(name) {
    var resultArray = findUrlParam(name);
    if(resultArray && resultArray.length>0){
        return resultArray[0];
    }
}

function alertCloseableDangerMessage(message, closed) {
    message = message||"系统错误";
    var $m = $('<div class="alert alert-danger alert-dismissible in fade message-centor" style="z-index: 2000" role="alert">' +
        '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
        message +
        '</div>');
    if(typeof closed == "function"){
        $m.on('closed.bs.alert',closed);
    }else if(typeof closed == 'string'){
        try{
            $m.on('closed.bs.alert', function (){eval(closed);});
        }catch (e) {
            //
        }
    }
    $("body").append($m);
}

function alertCloseableSuccessMessage(message, closed) {
    message = message||"系统错误";
    var $m = $('<div class="alert alert-success alert-dismissible in fade message-centor" style="z-index: 2000" role="alert">' +
        '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
        message +
        '</div>');
    if(typeof closed == "function"){
        $m.on('closed.bs.alert',closed);
    }else if(typeof closed == 'string'){
        try{
            $m.on('closed.bs.alert', function (){eval(closed);});

        }catch (e) {
            //
        }
    }
    $("body").append($m);
}

function loading(showOrHide) {
    if(showOrHide){
        if($('#loading').length==0){
            var m = '<div class="modal" id="loading" tabindex="-1"  role="dialog" >' +
                '  <div class="modal-dialog modal-sm" role="document">' +
                '    <div style="background-color: transparent" class="modal-content">' +
                '       <img src="img/loading.gif" style="height: 50px"/>' +
                '    </div>' +
                '  </div>' +
                '</div>';
            $('body').append(m);
        }

        $('#loading').modal('show');
    }
    else{
        $('#loading').modal('hide');
    }

}