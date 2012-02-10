/*
 * Created on 2007-10-31
 */
package aurora.service.validation;


public class Parameter implements IParameter {
    
    public static final String DEFAULT_DATA_TYPE = "java.lang.String";
    
    String      name;
    String      inputPath;
    String      outputPath;
    String      dataType;
    boolean     isRequired = false;
    boolean     isInput = true;
    boolean     isOutput = false;
    String      databaseTypeName;
    String      defaultValue;
    
    public Parameter(){
    }
    
    /**
     * @return the accessPath
     */
    public String getInputPath() {
        return inputPath;
    }
    /**
     * @param path the accessPath to set
     */
    public void setParameterPath(String path) {
        this.inputPath = path;
    }
    /**
     * @return the databaseTypeName
     */
    public String getDatabaseTypeName() {
        return databaseTypeName;
    }
    /**
     * @param databaseTypeName the databaseTypeName to set
     */
    public void setDatabaseTypeName(String databaseTypeName) {
        this.databaseTypeName = databaseTypeName;
    }
    /**
     * @return the dataType
     */
    public String getDataType() {
        return dataType==null?DEFAULT_DATA_TYPE:dataType;
    }
    /**
     * @param dataType the dataType to set
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    /**
     * @return the isInput
     */
    public boolean getInput() {
        return isInput;
    }
    /**
     * @param isInput the isInput to set
     */
    public void setInput(boolean isInput) {
        this.isInput = isInput;
    }
    /**
     * @return the isOutput
     */
    public boolean getOutput() {
        return isOutput;
    }
    /**
     * @param isOutput the isOutput to set
     */
    public void setOutput(boolean isOutput) {
        this.isOutput = isOutput;
    }
    /**
     * @return the isRequired
     */
    public boolean isRequired() {
        return isRequired;
    }
    
    public boolean getRequired(){
        return isRequired;
    }
    /**
     * @param isRequired the isRequired to set
     */
    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
        if(inputPath==null) inputPath = '@' + name;
    }

    /**
     * @return the defaultValue
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public String toString(){
        StringBuffer out = new StringBuffer();
        out.append(dataType==null?"[DataType undefined]":dataType).append(' ');
        out.append(name).append(' ');
        out.append("from ").append(inputPath).append(' ');
        out.append("required:").append(isRequired).append(" input:").append(isInput).append(" output:").append(isOutput);
        return out.toString();
    }

    /**
     * @return the outputPath
     */
    public String getOutputPath() {
        return outputPath==null?inputPath:outputPath;
    }

    /**
     * @param outputPath the outputPath to set
     */
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
    
    

}
