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

import org.seasar.maya.impl.MayaException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TooManyLoopException extends MayaException {

    private static final long serialVersionUID = 8318566911470435206L;

    private int _max;
    
    public TooManyLoopException(int max) {
        _max = max;
    }
    
    public int getMax() {
        return _max;
    }
    
    protected String[] getMessageParams() {
        return new String[] { Integer.toString(_max) };
    }
    
}