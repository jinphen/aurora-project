/**
 * @class Radio
 * 单项选择框控件标签
 * <pre><code>
	&lt;a:init-procedure&gt;
        &lt;a:model-query model="sys.sys_user_role_groups" rootPath="role_list"/&gt;
    &lt;/a:init-procedure&gt;
	...
    &lt;a:radio labelField="label" valueField="value" options="/model/role_list"
	bindTarget="dsId" name="fieldName" layout="horizontal"
	labelExpression="xxx"/&gt;
	&lt;br&gt;
	&lt;a:radio bindTarget="dsId" name="fieldName"&gt;
		&lt;a:items&gt;
			&lt;a:item label="yes" value="Y"/&gt;
			&lt;a:item label="no" value="N"/&gt;
		&lt;/a:items&gt;
	&lt;/a:radio&gt;
   </code></pre>
 * @extends Component
 * @author 牛佳庆
 */

/**
 * 选项集的dataset中用来显示选项描述的field
 * @property labelField
 * @type String
 * @default label
 */

/**
 * 指定选项集的model地址
 * @property options
 * @type String
 */
 
/**
 * 选项集的dataset中用来定义选项被选中后的值的field
 * @property valueField
 * @type String
 * @default value
 */
 
/**
 * 选项的布局
 * @property layout
 * @type String
 * @default horizontal
 */
 
/**
 * 选项的描述
 * @property labelExpression
 * @type String
 */