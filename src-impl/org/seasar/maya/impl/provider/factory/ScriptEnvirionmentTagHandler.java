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
package org.seasar.maya.impl.provider.factory;

import org.seasar.maya.ParameterAware;
import org.seasar.maya.cycle.script.ScriptEnvironment;
import org.seasar.maya.impl.util.XMLUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ScriptEnvirionmentTagHandler
        extends AbstractParameterAwareTagHandler {
    
    private ProviderTagHandler _parent;
    private ScriptEnvironment _scriptEnvironment;
    
    public ScriptEnvirionmentTagHandler(
            ProviderTagHandler parent, ServiceProvider beforeProvider) {
        super("scriptEnvironment");
        if(parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
        putHandler(new ScopeTagHandler(this));
    }
    
    protected void start(
    		Attributes attributes, String systemID, int lineNumber) {
        _scriptEnvironment = (ScriptEnvironment)XMLUtil.getObjectValue(
                attributes, "class", ScriptEnvironment.class);
        _parent.getServiceProvider().setScriptEnvironment(_scriptEnvironment);
    }
    
    protected void end(String body) {
        _scriptEnvironment = null;
    }
    
    public ScriptEnvironment getScriptEnvironment() {
        if(_scriptEnvironment == null) {
            throw new IllegalStateException();
        }
        return _scriptEnvironment;
    }
    
    public ParameterAware getParameterAware() {
        return getScriptEnvironment();
    }

}
