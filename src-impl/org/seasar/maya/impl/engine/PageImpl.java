/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl.engine;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.TemplateProcessor.ProcessStatus;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.cycle.script.ScriptUtil;
import org.seasar.maya.impl.engine.specification.SpecificationImpl;
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class PageImpl extends SpecificationImpl
        implements Page, Serializable, CONST_IMPL {

	private static final long serialVersionUID = -8688634709901129128L;

    private String _pageName;
    private String _extension;
    private List _templates;

    public PageImpl(String pageName, String extension) {
        if(StringUtil.isEmpty(pageName)) {
            throw new IllegalArgumentException();
        }
        _pageName = pageName;
        _extension = extension;
    }

    public String getPageName() {
        return _pageName;
    }

    public String getExtension() {
        return _extension;
    }

    private boolean match(Template template,  String suffix) {
        String templateSuffix = template.getSuffix();
        if((StringUtil.isEmpty(templateSuffix) && StringUtil.isEmpty(suffix)) ||
                templateSuffix.equals(suffix)) {
            return true;
        }
        return false;
    }
    
    public synchronized Template getTemplate(String suffix) {
        if(suffix == null) {
            throw new IllegalArgumentException();
        }
        Template template = null;
        if(_templates != null) {
            for(Iterator it = new ChildSpecificationsIterator(_templates); 
                    it.hasNext(); ) {
                Object obj = it.next();
                if(obj instanceof Template) {
                    Template test = (Template)obj;
                    if(match(test, suffix)) {
                        template = test;
                        break;
                    }
                }
            }
        }
        if(template == null) {
            StringBuffer name = new StringBuffer(_pageName);
            if(StringUtil.hasValue(suffix)) {
                String separator = EngineUtil.getEngineSetting(
                        SUFFIX_SEPARATOR, "$");
                name.append(separator).append(suffix);
            }
            String extension = getExtension();
            if(StringUtil.hasValue(extension)) {
                name.append(".").append(extension);
            }
            ServiceProvider provider = ProviderFactory.getServiceProvider();
            SourceDescriptor source = 
                provider.getPageSourceDescriptor(name.toString());
            if(source.exists()) {
                template = new TemplateImpl(this, suffix);
                template.setSource(source);
                synchronized (this) {
                    if (_templates == null) {
                        _templates = new ArrayList();
                    }
                    _templates.add(new SoftReference(template));
                }
            }
        }
        return template;
    }

    private String getTemplateSuffix(Specification specification) {
        SpecificationNode maya = SpecificationUtil.getMayaNode(specification);
        if(maya != null) {
            String value = SpecificationUtil.getAttributeValue(
                    maya, QM_TEMPLATE_SUFFIX);
            if(value != null) {
                return value;
            }
        }
        return null;
    }

    protected String getTemplateSuffix() {
        String text = getTemplateSuffix(this);
        if(text == null) {
            text = getTemplateSuffix(EngineUtil.getEngine());
        }
        if(StringUtil.hasValue(text)) {
            CompiledScript action = ScriptUtil.compile(text, String.class);
            return (String)action.execute();
        }
        return "";
    }

    protected Template getTemplate() {
        String suffix;
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        String requestedSuffix = cycle.getRequest().getRequestedSuffix();
        if(StringUtil.hasValue(requestedSuffix)) {
            suffix = requestedSuffix;
        } else {
            suffix = getTemplateSuffix();
        }
        Template template = getTemplate(suffix);
        if(template == null && StringUtil.hasValue(suffix) &&
                StringUtil.isEmpty(requestedSuffix)) {
            template = getTemplate("");
        }
        if(template == null) {
            throw new PageNotFoundException(
                    _pageName, requestedSuffix, _extension);
        }
        return template;
    }
    
    private void saveToCycle() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setOriginalNode(this);
        cycle.setInjectedNode(this);
    }

    public ProcessStatus doPageRender() throws PageForwarded {
        saveToCycle();
        Object model = SpecificationUtil.getSpecificationModel(this);
        SpecificationUtil.startScope(model, null);
        SpecificationUtil.execEvent(this, QM_BEFORE_RENDER);
        ProcessStatus ret = null;
        if("maya".equals(getExtension()) == false) {
            Template template = getTemplate();
            ret = template.doTemplateRender();
            saveToCycle();
        }
        SpecificationUtil.execEvent(this, QM_AFTER_RENDER);
        SpecificationUtil.endScope();
        return ret;
    }

}
