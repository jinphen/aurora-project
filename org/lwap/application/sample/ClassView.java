/**
 * Created on: 2002-11-11 21:52:48
 * Author:     zhoufan
 */
package org.lwap.application.sample;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lwap.application.BaseService;

import uncertain.composite.CompositeMap;

public class ClassView extends BaseService {

  // ���ظ����createModel������������Service��Model
  public void createModel(HttpServletRequest request, HttpServletResponse response) 
  throws  IOException,ServletException
  {
  	// ��URL������ȡ��Ҫ��ʾ��java�������
  	String cls_name = request.getParameter("class");
  	if( cls_name == null) return;

	//��Model�д���һ����Ϊ"class"��CompositeMap���������д���һ����Ϊ"method-list"��CompositeMap
  	CompositeMap class_model = super.getModel().createChild(null,null,"class");
  	class_model.put("name",cls_name);
  	CompositeMap method_list = class_model.createChild(null,null,"method-list");
  	
  	
  	//��ȡ��class��ʵ��
  	Class cls = null;
  	try{
  		cls = Class.forName(cls_name);
  	}catch(Throwable ex){
  		throw new ServletException(ex);
  	}
   	
   		//��ȡ����ֱ�����������з���
  	Method[] methods = cls.getDeclaredMethods();
  	for(int i=0; i<methods.length; i++){
  			// ������̬�Ļ�ǹ��еķ���
  			int md = methods[i].getModifiers();
  			if( Modifier.isStatic(md) || !Modifier.isPublic(md)) continue;
  			
  			//��ÿ�����������ķ���������һ��CompositeMap�������÷�������Ҫ���ԷŽ���Map
  			CompositeMap method = new CompositeMap(null,null,"method");
  			method.put("access", Modifier.toString(md) );  			
  			method.put("return", methods[i].getReturnType().getName());
  			
  			Class[] params = methods[i].getParameterTypes();  			
  			StringBuffer param_declare = new StringBuffer();
  			for( int n=0; n<params.length; n++){
  				if( n>0) param_declare.append(',');
  				param_declare.append(params[n].getName());
  			}
  			method.put("declare", methods[i].getName() +'(' + param_declare + ')' );
  			
  			//��������CompositeMap��Ϊ��Map��ӵ�method-list��
  			method_list.addChild(method);
  	}
  	
  	//System.out.println( getModel().toXML());
  	
  	/*
  	 * ѭ����Ϻ�Model�ĽṹΪ
  	 * <model>
  	 *    <class name="java.lang.String">
  	 *         <method access="public" return="int" declare="length()" />
  	 *         ...
  	 *    </class>
  	 * </mode>
  	 */
  		
  }
}
