(function(){var x="-",f="__",v="_",o=".",l=" ",r="",g="none",u="outline",k="left",b="td",e="recordid",y="row-selected",j="required",m="item-notBlank",c="item-invalid",p="table-row-alt",w="table-rowbox",t=o+w,a="table.rowcheck",n="table.rowradio",q="table-ckb ",s="table-select-all",i="multiple",h="checkedvalue",d="-readonly";C="-c",U="-u",IC="item-ckb",ICS=IC+"-self",$ICS=o+ICS,ICU=IC+U,ICC=IC+C,ICRU=IC+d+U,ICRC=IC+d+C,IR="item-radio-img",IRC=IR+C,IRU=IR+U,IRRC=IR+d+C,IRRU=IR+d+U,X=$ICS+l+o+ICC,TC="table-cell",TCE=TC+"-editor",CC="cellcheck",RC="rowcheck",EC="click",ECC="cellclick",ER="render",ERC="rowclick",EES="editorshow",ENES="nexteditorshow",EKD="keydown",ES="select";EMD="mousedown",ERS="resize",NF="未找到",M="方法!";$A.Table=Ext.extend($A.Component,{initComponent:function(z){$A.Table.superclass.initComponent.call(this,z);var A=this.wrap;this.cb=A.child("div[atype=table.headcheck]");this.tbody=A.child("tbody");this.fb=A.child("tfoot");this.initTemplate()},processListener:function(z){$A.Table.superclass.processListener.call(this,z);this.tbody[z](EC,this.onClick,this);if(this.canwheel){this.tbody[z]("mousewheel",this.onMouseWheel,this)}if(this.cb){this.cb[z](EC,this.onHeadClick,this)}this[z](ECC,this.onCellClick,this)},processDataSetLiestener:function(z){var A=this.dataset;if(A){A[z]("ajaxfailed",this.onAjaxFailed,this);A[z]("metachange",this.onLoad,this);A[z]("update",this.onUpdate,this);A[z]("reject",this.onUpdate,this);A[z]("add",this.onAdd,this);A[z]("submit",this.onBeforSubmit,this);A[z]("submitfailed",this.onAfterSuccess,this);A[z]("submitsuccess",this.onAfterSuccess,this);A[z]("query",this.onBeforeLoad,this);A[z]("load",this.onLoad,this);A[z]("loadfailed",this.onAjaxFailed,this);A[z]("valid",this.onValid,this);A[z]("beforeremove",this.onBeforeRemove,this);A[z]("remove",this.onRemove,this);A[z]("clear",this.onLoad,this);A[z]("refresh",this.onLoad,this);A[z]("fieldchange",this.onFieldChange,this);A[z]("indexchange",this.onIndexChange,this);A[z](ES,this.onSelect,this);A[z]("unselect",this.onUnSelect,this);A[z]("selectall",this.onSelectAll,this);A[z]("unselectall",this.onUnSelectAll,this)}},initEvents:function(){$A.Table.superclass.initEvents.call(this);this.addEvents(ER,ECC,ERC,EES,ENES)},bind:function(z){if(Ext.isString(z)){z=$(z);if(!z){return}}this.dataset=z;this.processDataSetLiestener("on");this.onLoad()},initTemplate:function(){this.cellTpl=new Ext.Template(['<div class="table-cell {cellcls}" id="',this.id,'_{name}_{recordid}">{text}</div>']);this.cbTpl=new Ext.Template(['<center><div class="{cellcls}" id="',this.id,'_{name}_{recordid}"></div></center>'])},createRow:function(B,D,G){if(G){var z=this.wrap.query("tbody tr");for(var F=D,A=z.length;F<A;F++){Ext.fly(z[F]).toggleClass(p).select(t).each(function(I){this.setSelectStatus(this.dataset.findById(I.getAttributeNS(r,e)))},this)}}var H=this.tbody.dom.insertRow(D),E=this.parseCss(this.renderRow(B,D));H.id=this.id+x+B.id;H.style.cssText=E.style;H.className=(D%2==1?p+l:r)+E.cls;Ext.each(this.columns,function(I){this.createCell(H,I,B)},this)},createEmptyRow:function(){this.emptyRow=this.tbody.dom.insertRow(-1);Ext.each(this.columns,function(z){Ext.fly(this.emptyRow.insertCell(-1)).set({atype:TC,dataindex:z.name}).setStyle({"text-align":z.align||k,display:z.hidden?g:r}).update("&#160;")},this)},removeEmptyRow:function(){if(this.emptyRow){this.tbody.dom.removeChild(this.emptyRow);this.emptyRow=null}},getCheckBoxStatus:function(z,D,B){var F=this.dataset.getField(D),A=F.getPropertity(h),E=z.data[D];return IC+(B?d:r)+((E&&E==A)?C:U)},createCell:function(I,z,E){var F=this.getEditor(z,E),A=z.type,H=z.name,D,K=r,B;if(I.tagName.toLowerCase()=="tr"){B=I.insertCell(-1)}else{B=Ext.fly(I).parent(b).dom}if(F!=r){var G=$A.CmpManager.get(F);if(G&&(G instanceof $A.CheckBox)){A=CC}else{K=TCE}}else{if(H&&Ext.isDefined(E.getField(H).get(h))){A=CC;D=true}}if(A==RC||A=="rowradio"){D=this.dataset.execSelectFunction(E)?r:d;Ext.fly(B).set({atype:A==RC?a:n,recordid:E.id}).addClass(w);B.innerHTML=this.cbTpl.applyTemplate({cellcls:A==RC?q+IC+D+U:"table-radio "+IR+D+U,name:H,recordid:E.id})}else{Ext.fly(B).set({atype:TC,recordid:E.id,dataindex:H}).setStyle({"text-align":z.align||k,dLEFT:z.hidden?g:r});if(A==CC){B.innerHTML=this.cbTpl.applyTemplate({cellcls:q+this.getCheckBoxStatus(E,H,D),name:H,recordid:E.id})}else{var J=E.getMeta().getField(H);if(J&&Ext.isEmpty(E.data[H])&&E.isNew==true&&J.get(j)==true){K=K+l+m}B.innerHTML=this.cellTpl.applyTemplate({text:this.renderText(E,z,E.data[H]),cellcls:K,name:H,recordid:E.id})}}},onSelect:function(B,A,D){if(!A||D){return}var z=Ext.get(this.id+f+A.id);z.parent(t).addClass(ICS);if(z){if(this.selectionmodel==i){this.setCheckBoxStatus(z,true)}else{this.setRadioStatus(z,true);var B=this.dataset;B.locate((B.currentPage-1)*B.pagesize+B.indexOf(A)+1)}this.setSelectStatus(A)}},onUnSelect:function(B,A,D){if(!A||D){return}var z=Ext.get(this.id+f+A.id);z.parent(t).addClass(ICS);if(z){if(this.selectionmodel==i){this.setCheckBoxStatus(z,false)}else{this.setRadioStatus(z,false)}this.setSelectStatus(A)}},onSelectAll:function(){this.clearChecked();this.isSelectAll=true;this.isUnSelectAll=false;this.wrap.addClass(s)},onUnSelectAll:function(){this.clearChecked();this.isSelectAll=false;this.isUnSelectAll=true;this.wrap.removeClass(s)},clearChecked:function(){var z=this.wrap;z.select(X).replaceClass(ICC,ICU);z.select($ICS).removeClass(ICS)},setRadioStatus:function(z,A){if(!A){z.removeClass(IRC).addClass(IRU)}else{z.addClass(IRC).removeClass(IRU)}},setCheckBoxStatus:function(z,A){if(!A){z.removeClass(ICC).addClass(ICU)}else{z.addClass(ICC).removeClass(ICU)}},setSelectDisable:function(B,A){var z=this.dataset.selected.indexOf(A)==-1;if(this.selectionmodel==i){B.removeClass([ICC,ICU]).addClass(z?ICRU:ICRC)}else{B.removeClass([IRC,IRU,IRRC,IRRU]).addClass(z?IRRU:IRRC)}},setSelectEnable:function(B,A){var z=this.dataset.selected.indexOf(A)==-1;if(this.selectionmodel==i){B.removeClass([ICRU,ICRC]).addClass(z?ICU:ICC)}else{B.removeClass([IRU,IRC,IRRU,IRRC]).addClass(z?IRU:IRC)}},setSelectStatus:function(A){if(this.dataset.selectfunction){var z=Ext.get(this.id+f+A.id);if(!this.dataset.execSelectFunction(A)){this.setSelectDisable(z,A)}else{this.setSelectEnable(z,A)}}},onHeadClick:function(D){var z=this.cb,B=this.dataset,A=z.hasClass(ICC);this.setCheckBoxStatus(z,!A);if(!A){B.selectAll()}else{B.unSelectAll()}},setEditor:function(A,B){var z=this.findColByName(A),D=Ext.get([this.id,A,this.selectedId].join(v));z.editor=B;if(D){this.focusdiv=D;if(B==r){D.removeClass(TCE)}else{if(!$(B) instanceof $A.CheckBox){D.addClass(TCE)}}}},getEditor:function(D,A){var B=D.editor||r;if(D.editorfunction){var z=window[D.editorfunction];if(z==null){alert(NF+D.editorfunction+M);return null}B=z.call(window,A,D.name)||r}return B},showEditor:function(I,z,H){if(I==-1){return}var A=this.findColByName(z);if(!A){return}var B=this.dataset.getAt(I);if(!B){return}if(B.id!=this.selectedId){this.selectRow(I)}var F=this.getEditor(A,B);this.setEditor(z,F);var E=this;if(E.currentEditor){E.currentEditor.editor.el.un(EKD,E.onEditorKeyDown,E);var G=E.currentEditor.focusCheckBox;if(G){G.setStyle(u,g);E.currentEditor.focusCheckBox=null}}if(F!=r){var D=$(F);setTimeout(function(){var J=B.get(z),L=Ext.get([E.id,z,B.id].join(v)),K=L.getXY();E.currentEditor={record:B,ov:J,name:z,editor:D};D.bind(E.dataset,z);D.render(B);if(D instanceof $A.CheckBox){D.move(-1000,K[1]+5);D.el.on(EKD,E.onEditorKeyDown,E);D.onClick();E.currentEditor.focusCheckBox=L;L.setStyle(u,"1px dotted blue")}else{if(F){E.positionEditor();D.isEditor=true;D.isFireEvent=true;D.isHidden=false;D.focus();E.editing=true;D.el.on(EKD,E.onEditorKeyDown,E);D.on(ES,E.onEditorSelect,E);Ext.fly(document.documentElement).on(EMD,E.onEditorBlur,E);Ext.fly(window).on(ERS,E.positionEditor,E);if(H){H.call(window,D)}E.fireEvent(EES,E,D,I,z,B)}}},10)}},onEditorSelect:function(){var z=this;setTimeout(function(){z.hideEditor()},1)},onEditorKeyDown:function(D,A){var B=D.keyCode;if(B==27){if(this.currentEditor){var z=this.currentEditor.editor;if(z){z.clearInvalid();z.render(z.binder.ds.getCurrentRecord())}}this.hideEditor()}else{if(B==13){if(!(this.currentEditor&&this.currentEditor.editor&&this.currentEditor.editor instanceof $A.TextArea)){this.showNextEditor()}}else{if(B==9){D.stopEvent();this.showNextEditor()}}}},showNextEditor:function(){this.hideEditor();var K=this;if(K.currentEditor){var I=K.currentEditor.editor;if(I){var O=function(R){if(R instanceof $A.Lov){R.showLovWindow()}},F=K.dataset,E=I.binder.name,z=I.record,Q=F.data.indexOf(z),A=null;if(Q!=-1){var P=K.columns,B=0,J;for(var H=0,G=P.length;H<G;H++){if(P[H].name==E){B=H+1;break}}for(var H=B,G=P.length;H<G;H++){var D=P[H];if(D.hidden!=true){J=K.getEditor(D,z);if(J!=r){A=D.name;break}}}if(K.currentEditor){var L=K.currentEditor.focusCheckBox;if(L){L.setStyle(u,g);K.currentEditor.focusCheckBox=null}}if(A){var I=$(J);if(I instanceof $A.CheckBox){K.currentEditor={record:z,ov:z.get(A),name:A,editor:I};setTimeout(function(){I.bind(F,A);I.render(z);var S=Ext.get([K.id,A,z.id].join(v)),R=S.getXY();I.move(-1000,R[1]);I.focus();I.el.on(EKD,K.onEditorKeyDown,K);K.currentEditor.focusCheckBox=S;S.setStyle(u,"1px dotted blue")},10)}else{K.fireEvent(ECC,K,Q,A,z,O)}}else{var N=F.getAt(Q+1);if(!N&&K.autoappend!==false){F.create();N=F.getAt(Q+1)}if(N){K.selectRow(Q+1);for(var H=0,G=P.length;H<G;H++){var D=P[H],J=K.getEditor(D,N);if(J!=r){var I=$(J),A=D.name;if(I instanceof $A.CheckBox){K.currentEditor={record:N,ov:N.get(A),name:A,editor:I};setTimeout(function(){I.bind(F,A);I.render(N);var S=Ext.get([K.id,A,N.id].join(v)),R=S.getXY();I.move(-1000,R[1]);I.focus();I.el.on(EKD,K.onEditorKeyDown,K);K.currentEditor.focusCheckBox=S;S.setStyle(u,"1px dotted blue")},10)}else{K.fireEvent(ECC,K,Q+1,A,N,O)}break}}}}}K.fireEvent(ENES,K,Q,A)}}},positionEditor:function(){var z=this.currentEditor.editor,B=this.focusdiv,A=B.getXY();z.setHeight(B.getHeight()-2);z.setWidth(B.getWidth()-5<22?22:(B.getWidth()-5));z.move(A[0],A[1]);if(z.isExpanded&&z.isExpanded()){if(Ext.isIE){if(this.t){clearTimeout(this.t)}this.t=setTimeout(function(){z.syncPopup()},1)}else{z.syncPopup()}}},hideEditor:function(){if(this.currentEditor&&this.editing){var z=this.currentEditor.editor;if(z){if(!z.canHide||z.canHide()){z.el.un(EKD,this.onEditorKeyDown,this);z.un(ES,this.onEditorSelect,this);Ext.fly(document.documentElement).un(EMD,this.onEditorBlur,this);Ext.fly(window).un(ERS,this.positionEditor,this);z.move(-10000,-10000);z.onBlur();z.isFireEvent=false;z.isHidden=true;this.editing=false}}}},selectRow:function(E,A){var D=this.dataset,z=D.getAt(E),B=(D.currentPage-1)*D.pagesize+E+1;this.selectedId=z.id;if(this.selectTr){this.selectTr.removeClass(y)}this.selectTr=Ext.get(this.id+x+z.id);if(this.selectTr){this.selectTr.addClass(y)}this.selectRecord=z;if(A!==false&&B!=null){D.locate.defer(5,D,[B,false])}},drawFootBar:function(z){if(!this.fb){return}Ext.each([].concat((z)?z:this.columns),function(F){var D=Ext.isString(F)?this.findColByName(F):F;if(D&&D.footerrenderer){var E=$A.getRenderer(D.footerrenderer);if(E==null){alert(NF+D.footerrenderer+M);return}var B=D.name,A=E.call(window,this.dataset.data,B);if(!Ext.isEmpty(A)){this.fb.child("td[dataindex="+B+"]").update(A)}}},this)},showColumn:function(A){var z=this.findColByName(A);if(z){if(z.hidden===true){delete z.hidden;this.wrap.select("td[dataindex="+A+"]").setStyle("display",r)}}},hideColumn:function(A){var z=this.findColByName(A);if(z){if(z.hidden!==true){this.wrap.select("td[dataindex="+A+"]").setStyle("display",g);z.hidden=true}}},findColByName:function(A){if(A){var D=this.columns;for(var B=0,z=D.length;B<z;B++){var E=D[B];if(E.name&&E.name.toLowerCase()===A.toLowerCase()){return E}}}return},parseCss:function(B){var E=r,z=r;if(Ext.isArray(B)){for(var A=0;A<B.length;A++){var D=this.parseCss(B[A]);E+=";"+D.style;z+=l+D.cls}}else{if(Ext.isString(B)){var F=!!B.match(/^([^,:;]+:[^:;]+;)*[^,:;]+:[^:;]+;*$/);z=F?r:B;E=F?B:r}}return{style:E,cls:z}},renderText:function(z,A,E){var D=A.renderer;if(D){var B=$A.getRenderer(D);if(B==null){alert(NF+D+M);return E}E=B.call(window,E,z,A.name);return E==null?r:E}return E==null?r:E},renderRow:function(z,E){var D=this.rowrenderer,A=null;if(D){var B=$A.getRenderer(D);if(B==null){alert(NF+D+M);return A}A=B.call(window,z,E);return !A?r:A}return A},renderEditor:function(D,z,B,A){this.createCell(D.dom,B,z)},onClick:function(G,J){var z=J.tagName.toLowerCase()==b,H=z?Ext.fly(J):Ext.fly(J).parent(b);if(H){var D=H.getAttributeNS(r,"atype"),I=H.getAttributeNS(r,e),B=this.dataset;if(D==TC){var F=B.findById(I),K=B.indexOf(F),A=H.getAttributeNS(r,"dataindex");this.fireEvent(ECC,this,K,A,F);this.fireEvent(ERC,this,K,F)}else{if(D==a){var E=Ext.get(this.id+f+I);if(E.hasClass(ICRU)||E.hasClass(ICRC)){return}if(this.isSelectAll&&!E.parent($ICS)){E.replaceClass(ICU,ICC)}else{if(this.isUnselectAll&&!E.parent($ICS)){E.replaceClass(ICC,ICU)}}E.hasClass(ICC)?B.unSelect(I):B.select(I)}else{if(D==n){var E=Ext.get(this.id+f+I);if(E.hasClass(IRRU)||E.hasClass(IRRC)){return}B.select(I)}}}}},onCellClick:function(B,D,A,z,E){this.showEditor(D,A,E)},onUpdate:function(B,F,A,J){this.setSelectStatus(F);var z=Ext.get([this.id,A,F.id].join(v));if(z){var H=this.findColByName(A);var G=this.getEditor(H,F);if(G!=r&&($(G) instanceof $A.CheckBox)){this.renderEditor(z,F,H,G)}else{var K=this.renderText(F,H,J);z.update(K)}}var L=this.columns;for(var E=0,D=L.length;E<D;E++){var H=L[E];if(H.name!=A){var I=Ext.get([this.id,H.name,F.id].join(v));if(I){if(H.editorfunction){var G=this.getEditor(H,F);this.renderEditor(I,F,H,G)}if(H.renderer){var K=this.renderText(F,H,F.get(H.name));I.update(K)}}}}this.drawFootBar(A)},onLoad:function(){var B=this.wrap,E=this.dataset.data;this.clearBody();var z=B.removeClass(s).child("div[atype=table.headcheck]");if(z&&this.selectable&&this.selectionmodel==i){this.setCheckBoxStatus(z,false)}var A=E.length;if(A==0){this.createEmptyRow()}else{for(var D=0;D<A;D++){this.createRow(E[D],D)}}this.drawFootBar();$A.Masker.unmask(B);this.fireEvent(ER,this)},onValid:function(D,z,A,B){var F=this.findColByName(A);if(F){var E=Ext.get([this.id,A,z.id].join(v));if(E){if(B==false){E.addClass(c)}else{E.removeClass([m,c])}}}},onAdd:function(B,z,A){this.removeEmptyRow();this.createRow(z,A,A!==this.dataset.data.length-1);this.selectRow(this.dataset.indexOf(z));this.setSelectStatus(z)},onRemove:function(B,z,A){var D=Ext.get(this.id+x+z.id);if(D){D.remove()}this.selectTr=null;$A.Masker.unmask(this.wrap);this.drawFootBar()},clearBody:function(){while(this.tbody.dom.childNodes.length){this.tbody.dom.removeChild(this.tbody.dom.firstChild)}this.emptyRow=null},getDataIndex:function(B){for(var A=0,D=this.dataset.data,z=D.length;A<z;A++){if(D[A].id==B){return A}}return -1},onEditorBlur:function(A,z){if(this.currentEditor&&!this.currentEditor.editor.isEventFromComponent(z)){this.hideEditor.defer(Ext.isIE9?10:0,this)}},onMouseWheel:function(A){A.stopEvent();if(this.editing==true){return}var B=A.getWheelDelta(),z=this.dataset;if(B>0){z.pre()}else{if(B<0){z.next()}}},onIndexChange:function(B,A){var z=this.getDataIndex(A.id);if(z==-1){return}this.selectRow(z,false)},onFieldChange:function(D,z,E,A,B){if(A==j){var F=Ext.get([this.id,E.name,z.id].join(v));if(F){F[B==true?"addClass":"removeClass"](m)}}},onBeforeRemove:function(){$A.Masker.mask(this.wrap,_lang["grid.mask.remove"])},onBeforeLoad:function(){$A.Masker.mask(this.wrap,_lang["grid.mask.loading"])},onAfterSuccess:function(){$A.Masker.unmask(this.wrap)},onBeforSubmit:function(z){$A.Masker.mask(this.wrap,_lang["grid.mask.submit"])},onAjaxFailed:function(A,z){$A.Masker.unmask(this.wrap)}})})();