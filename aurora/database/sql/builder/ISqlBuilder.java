/*
 * Created on 2008-3-28
 */
package aurora.database.sql.builder;

import aurora.database.sql.ISqlStatement;

/**
 * Define object that can convert a IStatement instance to SQL text.
 * Instance of this interface should be customized with specific database.
 * @author Zhou Fan
 *
 */
public interface ISqlBuilder {
    
    public void setRegistry(ISqlBuilderRegistry registry);
    
    public String createSql(ISqlStatement sqlStatement);

}