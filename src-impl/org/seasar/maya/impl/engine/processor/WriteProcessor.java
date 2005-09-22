/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.maya.impl.engine.processor;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WriteProcessor extends TemplateProcessorSupport {

	private static final long serialVersionUID = -8069702863937020350L;

    private ProcessorProperty _value;
    private String _default;
    
    // MLD property, expectedType=java.lang.String
    public void setValue(ProcessorProperty value) {
        _value = value;
    }
    
    public void setDefault(String defaultValue) {
    	_default = defaultValue;
    }
    
    public ProcessStatus doStartProcess(Page topLevelPage) {
        if(_value != null) {
            String ret = (String)_value.getValue().execute();
            if(StringUtil.isEmpty(ret) && StringUtil.hasValue(_default)) {
            	ret = _default;
            }
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            cycle.getResponse().write(ret);
        }
        return SKIP_BODY;
    }
    
}
