/*
 * Created on 2008-4-14
 */
package aurora.database.sql.builder;

import aurora.database.sql.ISqlStatement;

public interface ISqlBuilderRegistry {

    public IDatabaseProfile getDatabaseProflie();
    
    public void setDatabaseProfile(IDatabaseProfile profile);
    
    public ISqlBuilder      getBuilder( ISqlStatement   statement );
    
    public void registerSqlBuilder( Class statement_type, ISqlBuilder sql_builder );
    
    public String getSql( ISqlStatement statement );

}
