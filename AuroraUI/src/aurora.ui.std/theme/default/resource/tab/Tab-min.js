$A.Tab=Ext.extend($A.Component,{sd:"scroll-disabled",tslo:"tab-scroll-left-over",tsro:"tab-scroll-right-over",tsl:"tab-scroll-left",tsr:"tab-scroll-right",tc:"tab-close",tbo:"tab-btn-over",tbd:"tab-btn-down",ts:"tab-scroll",constructor:function(a){this.intervalIds=[];$A.Tab.superclass.constructor.call(this,a)},initComponent:function(a){$A.Tab.superclass.initComponent.call(this,a);this.scriptwidth=a.scriptwidth||60;this.head=this.wrap.child("div[atype=tab.strips]");this.body=this.wrap.child("div.item-tab-body");this.scrollLeft=this.wrap.child("div[atype=scroll-left]");this.scrollRight=this.wrap.child("div[atype=scroll-right]");this.script=this.head.parent();this.selectTab(a.selected||0)},processListener:function(a){$A.Tab.superclass.processListener.call(this,a);this.script.parent()[a]("mousedown",this.onMouseDown,this);this.script.parent()[a]("mouseup",this.onMouseUp,this);this.script.parent()[a]("mouseover",this.onMouseOver,this);this.script.parent()[a]("mouseout",this.onMouseOut,this);this.script[a]("click",this.onClick,this);this.script[a]("mousewheel",this.onMouseWheel,this)},initEvents:function(){$A.Tab.superclass.initEvents.call(this);this.addEvents("select","beforeopen")},selectTab:function(e){var a=this.getTab(e);if(!a){return}if(a.strip.hasClass(this.sd)){this.selectTab(a.index+1);return}var d=a.strip,i=a.body;e=a.index;if(d){if(this.activeTab){this.activeTab.replaceClass("active","unactive")}this.activeTab=d;d.replaceClass("unactive","active");var b=d.dom.offsetLeft,g=d.getWidth(),c=this.script.getScroll().left,h=this.script.getWidth(),f=this.head.getWidth();tr=b+g-c-h,tl=c-b;if(tr>0){this.scrollRight.removeClass(this.sd);this.scrollLeft.removeClass(this.sd);this.script.scrollTo("left",c+tr)}else{if(tl>0){this.scrollLeft.removeClass(this.sd);this.script.scrollTo("left",c-tl);this.scrollRight.removeClass(this.sd)}}if(h+this.script.getScroll().left>=f){this.script.scrollTo("left",f-h);this.scrollRight.addClass(this.sd)}else{if(e==0){this.script.scrollTo("left",0);this.scrollLeft.addClass(this.sd)}}}if(i){if(this.activeBody){this.activeBody.setLeft("-10000px");this.activeBody.setTop("-10000px")}this.activeBody=i;i.setLeft("0px");i.setTop("0px")}if(this.items[e].ref&&i.loaded!=true){this.load(this.items[e].ref,i,e);i.loaded=true}else{this.fireEvent("select",this,e)}},stripTpl:['<div class="strip unactive"  unselectable="on" onselectstart="return false;"><div style="height:26px;width:{stripwidth2}px">','<div class="strip-left"></div>','<div style="width:{stripwidth}px;" class="strip-center"><div class="tab-close"></div>{prompt}</div>','<div class="strip-right"></div>',"</div></div>"],bodyTpl:'<div style="width:{bodywidth}px;height:{bodyheight}px;left:-10000px;top:-10000px;" class="tab"></div>',openTab:function(e,a){var b=0;for(;b<this.items.length;b++){if(this.items[b].ref&&this.items[b].ref==e){this.selectTab(b);return}}var d=this.fireEvent("beforeopen",this,b);if(d!=false){this.items.push({ref:e});var f=$A.TextMetrics.measure(document.body,a).width+20;f=f<this.scriptwidth?this.scriptwidth:f;var c=this.head.getWidth()+f+6;this.head.setWidth(c);if(c>this.script.getWidth()){this.scrollLeft.setStyle({display:"block"});this.scrollRight.setStyle({display:"block"});this.script.setStyle("padding-left","1px")}new Ext.Template(this.stripTpl).append(this.head.dom,{prompt:a,stripwidth:f,stripwidth2:f+6});new Ext.Template(this.bodyTpl).append(this.body.dom,{bodywidth:this.body.getWidth(),bodyheight:this.body.getHeight()});this.selectTab(b)}},closeTab:function(g){var f=this.getTab(g);if(!f){return}var e=f.strip,a=f.body,c=f.index;if(!e.child("div."+this.tc)){$A.showWarningMessage("警告","该Tab页无法被关闭!");return}if(this.activeBody==f.body){this.activeBody=null;this.activeTab=null}this.items.splice(c,1);var d=this.head.getWidth()-e.getWidth(),b=a.cmps;this.head.setWidth(d);if(d<=this.script.getWidth()){this.scrollLeft.setStyle({display:"none"});this.scrollRight.setStyle({display:"none"});this.script.setStyle("padding-left","0")}e.remove();a.remove();delete a.loaded;setTimeout(function(){var k=$A.focusWindow?$A.focusWindow.cmps:{};for(var h in b){var i=b[h];if(i.destroy){try{delete k[h];i.destroy()}catch(j){alert("销毁window出错: "+j)}}}},10);this.selectTab(c)},setDisabled:function(a){var b=this.getTab(a);if(!b){return}if(this.items.length>1){if(this.activeTab==b.strip){this.selectTab(b.index+(this.getTab(b.index+1)?1:-1))}b.strip.addClass(this.sd)}},setEnabled:function(a){var b=this.getTab(a);if(!b){return}b.strip.removeClass(this.sd)},getTab:function(g){var c=Ext.DomQuery.select("div.tab",this.body.dom),f=Ext.DomQuery.select("div.strip",this.head.dom),e,a;if(Ext.isNumber(g)){if(g<0){g+=f.length}g=Math.round(g);if(f[g]){e=Ext.get(f[g]);a=Ext.get(c[g])}}else{g=Ext.get(g);for(var d=0,b=f.length;d<b;d++){if(Ext.get(f[d])==g){e=g;a=Ext.get(c[d]);g=d;break}}}return e?{strip:e,body:a,index:g}:null},scrollTo:function(a){if(a=="left"){this.script.scrollTo("left",this.script.getScroll().left-this.scriptwidth);this.scrollRight.removeClass(this.sd);if(this.script.getScroll().left<=0){this.scrollLeft.addClass(this.sd);this.scrollLeft.replaceClass(this.tslo,this.tsl);this.stopScroll()}}else{if(a=="right"){this.script.scrollTo("left",this.script.getScroll().left+this.scriptwidth);this.scrollLeft.removeClass(this.sd);if(this.script.getScroll().left+this.script.getWidth()>=this.head.getWidth()){this.scrollRight.addClass(this.sd);this.scrollRight.replaceClass(this.tsro,this.tsr);this.stopScroll()}}}},stopScroll:function(){if(this.scrollInterval){clearInterval(this.scrollInterval);delete this.scrollInterval}},onClick:function(b){var a=Ext.get(b.target);if(a.hasClass(this.tc)){this.closeTab(a.parent(".strip"))}},onMouseWheel:function(a){var b=a.getWheelDelta();if(b>0){this.scrollTo("left");a.stopEvent()}else{this.scrollTo("right");a.stopEvent()}},onMouseDown:function(d){var b=Ext.get(d.target),a=b.parent(".strip"),c=this;if(b.hasClass(c.tc)){b.removeClass(c.tbo);b.addClass(c.tbd)}else{if(b.hasClass(c.ts)&&!b.hasClass(c.sd)){if(b.hasClass(c.tslo)){c.scrollTo("left")}else{c.scrollTo("right")}c.scrollInterval=setInterval(function(){if(b.hasClass(c.ts)&&!b.hasClass(c.sd)){if(b.hasClass(c.tslo)){c.scrollTo("left")}else{c.scrollTo("right")}if(b.hasClass(c.sd)){clearInterval(c.scrollInterval)}}},100)}else{if(a.hasClass("strip")&&!a.hasClass("active")&&!a.hasClass(c.sd)){c.selectTab(a)}}}},onMouseUp:function(a){this.stopScroll()},onMouseOver:function(c){var b=Ext.get(c.target),a=b.parent(".strip");if(b.hasClass(this.ts)&&!b.hasClass(this.sd)){if(b.hasClass(this.tsl)){b.replaceClass(this.tsl,this.tslo)}else{if(b.hasClass(this.tsr)){b.replaceClass(this.tsr,this.tsro)}}}else{if(b.hasClass(this.tc)){b.addClass(this.tbo)}}if(a){var b=a.child("div."+this.tc);if(b){if(this.currentBtn){this.currentBtn.hide()}this.currentBtn=b;b.show()}}},onMouseOut:function(c){var b=Ext.get(c.target),a=b.parent(".strip");if(b.hasClass(this.ts)&&!b.hasClass(this.sd)){this.stopScroll();if(b.hasClass(this.tslo)){b.replaceClass(this.tslo,this.tsl)}else{if((b.hasClass(this.tsro))){b.replaceClass(this.tsro,this.tsr)}}}else{if(b.hasClass(this.tc)){b.removeClass(this.tbo);b.removeClass(this.tbd)}}if(a){b=a.child("div."+this.tc);if(b){b.hide()}}},showLoading:function(a){a.update(_lang["tab.loading"]);a.setStyle("text-align","center");a.setStyle("line-height",5)},clearLoading:function(a){a.update("");a.setStyle("text-align","");a.setStyle("line-height","")},load:function(c,e,b){c=c+(c.indexOf("?")!=-1?"&":"?")+"_vw="+this.width+"&_vh="+(this.height-Ext.fly(this.head).getHeight());var d=this,a=Ext.get(e);a.cmps={};d.showLoading(a);Ext.Ajax.request({url:c,success:function(f,g){var h=f.responseText;d.intervalIds[b]=setInterval(function(){if(!$A.focusTab){clearInterval(d.intervalIds[b]);d.clearLoading(a);$A.focusTab=a;try{a.update(h,true,function(){$A.focusTab=null;d.fireEvent("select",d,b)})}catch(i){$A.focusTab=null}}},10)}})},setWidth:function(e){e=Math.max(e,2);if(this.width==e){return}$A.Tab.superclass.setWidth.call(this,e);this.body.setWidth(e-2);this.script.setWidth(e-38);if(e-38<this.head.getWidth()){this.scrollLeft.setStyle({display:"block"});this.scrollRight.setStyle({display:"block"});this.script.setStyle("padding-left","1px");var d=this.script.getScroll().left,c=this.script.getWidth(),b=this.head.getWidth();if(d<=0){this.scrollLeft.addClass(this.sd)}else{this.scrollLeft.removeClass(this.sd)}if(d+c>=b){if(!this.scrollRight.hasClass(this.sd)){this.scrollRight.addClass(this.sd)}else{this.script.scrollTo("left",b-c)}}else{this.scrollRight.removeClass(this.sd)}}else{this.scrollLeft.setStyle({display:"none"});this.scrollRight.setStyle({display:"none"});this.script.setStyle("padding-left","0");this.script.scrollTo("left",0)}var f=Ext.DomQuery.select("div.tab",this.body.dom);for(var g=0;g<f.length;g++){var a=f[g];Ext.fly(a).setWidth(e-4)}},setHeight:function(d){d=Math.max(d,25);if(this.height==d){return}$A.Tab.superclass.setHeight.call(this,d);this.body.setHeight(d-26);var b=Ext.DomQuery.select("div.tab",this.body.dom);for(var c=0;c<b.length;c++){var a=b[c];Ext.fly(a).setHeight(d-28)}}});