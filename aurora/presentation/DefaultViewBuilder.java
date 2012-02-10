/*
 * Created on 2009-7-23
 */
package aurora.presentation;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.ocm.ISingleton;

/**
 * Directly output tag content in view config. This builder will be set as
 * default IViewBuilder for unknown view config
 * 
 * @author Zhou Fan
 * 
 */
public class DefaultViewBuilder implements IViewBuilder, ISingleton {

    String getParsedContent(String text, CompositeMap model) {
        if (text.indexOf('$') >= 0)
            return TextParser.parse(text, model);
        else
            return text;
    }

    public void buildView(BuildSession session, ViewContext view_context)
            throws IOException, ViewCreationException {
        CompositeMap view = view_context.getView();
        CompositeMap model = view_context.getModel();
        String close_tag = "</" + view.getName() + ">";
        Writer out = session.getWriter();
            out.write('<');
            out.write(view.getName());
            if(view.size()>0){          // print attributes
                for(Iterator it = view.entrySet().iterator(); it.hasNext();){
                    Map.Entry entry = (Map.Entry)it.next();
                    Object key = entry.getKey();
                    if(key==null) continue;
                    out.write(' ');
                    out.write(key.toString());
                    out.write("=\"");
                    Object value = entry.getValue();
                    if(value!=null)
                        out.write(getParsedContent(value.toString(),model));
                    out.write('\"');                            
                }
            }
            out.write('>');
            Collection childs = view.getChilds();
            if(childs!=null){           // print childs
                try{
                    session.buildViews(model, childs);
                }catch(Exception ex){
                    throw new ViewCreationException(ex);
                }
                out.write(close_tag);
            }else{
                String text = view.getText();
                if(text!=null){ 
                    out.write(getParsedContent(text,model));
                    out.write(close_tag);
                }
            }
            out.flush();
    }

    public String[] getBuildSteps(ViewContext context) {
        return null;
    }

}
