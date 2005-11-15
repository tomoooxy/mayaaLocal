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
package org.seasar.maya.impl.builder.library;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

import org.seasar.maya.builder.library.PropertyDefinition;
import org.seasar.maya.builder.library.PropertySet;
import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.engine.processor.JspProcessor;
import org.seasar.maya.impl.util.ObjectUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TLDProcessorDefinition extends ProcessorDefinitionImpl {

    private Class _tagClass;
    private Class _teiClass;

    public void setProcessorClass(Class processorClass) {
        if(processorClass == null || 
                Tag.class.isAssignableFrom(processorClass) == false) {
            throw new IllegalArgumentException();
        }
        _tagClass = processorClass;
    }

    public Class getProcessorClass() {
        if(_tagClass == null) {
            throw new IllegalStateException();
        }
        return _tagClass;
    }

    public void setExtraInfoClass(Class extraInfoClass) {
        if(extraInfoClass == null || 
                TagExtraInfo.class.isAssignableFrom(extraInfoClass) == false) {
            throw new IllegalArgumentException();
        }
        _teiClass = extraInfoClass;
    }

    public Class getExtraInfoClass() {
        return _teiClass;
    }

    protected TemplateProcessor newInstance() {
        JspProcessor processor = new JspProcessor();
        processor.setTagClass(getProcessorClass());
        return processor;
    }
    
    protected void settingPropertySet(SpecificationNode injected, 
            TemplateProcessor processor, PropertySet propertySet) {
        Hashtable tagDataSeed = new Hashtable();

        for(Iterator it = propertySet.iteratePropertyDefinition(); it.hasNext(); ) {
            PropertyDefinition property = (PropertyDefinition)it.next();
            Object prop = property.createProcessorProperty(this, injected);
            if(prop != null) {
                ProcessorProperty currentProp = (ProcessorProperty) prop;
    	        JspProcessor jsp = (JspProcessor)processor;
    	        jsp.addProcessorProperty(currentProp);

                CompiledScript value = currentProp.getValue();
                tagDataSeed.put(
                        currentProp.getName().getQName().getLocalName(),
                        value.isLiteral() ? value.execute(null) : value);
            }
        }

        if (getExtraInfoClass() != null) {
            settingExtraInfo((JspProcessor) processor, tagDataSeed);
        }
    }

    protected void settingExtraInfo(JspProcessor processor, Hashtable seed) {
        TagExtraInfo tei =
                (TagExtraInfo) ObjectUtil.newInstance(getExtraInfoClass());

        boolean hasNestedVariable = existsNestedVariable(tei, seed);
        boolean hasDynamicName = existsDynamicName(seed);

        TLDScriptingVariableInfo variableInfo = new TLDScriptingVariableInfo();

        variableInfo.setTagExtraInfo(tei);
        variableInfo.setNestedVariable(hasNestedVariable);
        variableInfo.setDynamicName(hasDynamicName);
        if (hasNestedVariable) {
            ScriptableTagData tagData = new ScriptableTagData(seed);
            if (hasDynamicName) {
                variableInfo.setTagData(tagData);
            } else {
                variableInfo.setVariableInfos(tei.getVariableInfo(tagData));
            }
        }

        processor.setTLDScriptingVariableInfo(variableInfo);
    }

    protected boolean existsNestedVariable(TagExtraInfo tei, Hashtable seed) {
        VariableInfo[] dummy = tei.getVariableInfo(new DummyTagData(seed));
        for (int i = 0; i < dummy.length; i++) {
            if (dummy[i].getScope() == VariableInfo.NESTED) {
                return true;
            }
        }
        return false;
    }

    protected boolean existsDynamicName(Hashtable seed) {
        Enumeration keys = seed.keys();
        while (keys.hasMoreElements()) {
            if (seed.get(keys.nextElement()) instanceof CompiledScript) {
                return true;
            }
        }
        return false;
    }

    protected static class DummyTagData extends TagData {
        public DummyTagData(Hashtable seed) {
            super(seed);
        }

        public Object getAttribute(String attName) {
            return attName;
        }

        public String getAttributeString(String attName) {
            return attName;
        }
    }

    protected static class ScriptableTagData extends TagData {
        public ScriptableTagData(Hashtable seed) {
            super(seed);
        }

        public boolean isDynamicAttribute(String attName) {
            return (super.getAttribute(attName) instanceof CompiledScript);
        }

        public Object getAttribute(String attName) {
            Object value = super.getAttribute(attName);
            if (value instanceof CompiledScript) {
                return ((CompiledScript) value).execute(null);
            }
            return value;
        }

        public String getAttributeString(String attName) {
            Object value = getAttribute(attName);
            if (value != null) {
                return String.valueOf(value);
            }
            return (String) value;
        }
    }

}