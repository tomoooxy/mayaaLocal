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
/*
 * Copyright 2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.seasar.maya.impl.util.collection;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Helper Map implementation for use with different Attribute Maps.
 * @author Anton Koinov (Myfaces: http://myfaces.apache.org)
 * @author Masataka Kurihara �iModify�j
 */
public abstract class AbstractAttributeMap implements Map {
    
    private Set _keySet;
    private Collection _values;
    private Set _entrySet;

    public void clear() {
        List names = new ArrayList();
        for (Iterator it = getAttributeNames(); it.hasNext();) {
            names.add(it.next());
        }
        for (Iterator it = names.iterator(); it.hasNext();) {
            setAttribute((String) it.next(), null);
        }
    }

    public boolean containsKey(Object key) {
        return getAttribute(key.toString()) != null;
    }

    public boolean containsValue(Object findValue) {
        if (findValue == null) {
            return false;
        }
        for (Iterator it = getAttributeNames(); it.hasNext();) {
            Object value = getAttribute((String)it.next());
            if (findValue.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public Set entrySet() {
        return (_entrySet != null) ? _entrySet : (_entrySet = new EntrySet());
    }

    public Object get(Object key) {
        return getAttribute(key.toString());
    }

    public boolean isEmpty() {
        return !getAttributeNames().hasNext();
    }

    public Set keySet() {
        return (_keySet != null) ? _keySet : (_keySet = new KeySet());
    }

    public Object put(Object key, Object value) {
        String keyName = key.toString();
        Object retval = getAttribute(keyName);
        setAttribute(keyName, value);
        return retval;
    }

    public void putAll(Map t) {
        for (Iterator it = t.entrySet().iterator(); it.hasNext();) {
            Entry entry = (Entry) it.next();
            setAttribute(entry.getKey().toString(), entry.getValue());
        }
    }

    public Object remove(Object key) {
        String keyName = key.toString();
        Object retval = getAttribute(keyName);
        setAttribute(keyName, null);
        return retval;
    }

    public int size() {
        int size = 0;
        for (Iterator it = getAttributeNames(); it.hasNext();) {
            size++;
            it.next();
        }
        return size;
    }

    public Collection values() {
        return (_values != null) ? _values : (_values = new Values());
    }

    abstract protected Object getAttribute(String key);

    abstract protected void setAttribute(String key, Object value);

    abstract protected Iterator getAttributeNames();

    private class KeySet extends AbstractSet {
        
        public Iterator iterator() {
            return new KeyIterator();
        }

        public boolean isEmpty() {
            return AbstractAttributeMap.this.isEmpty();
        }

        public int size() {
            return AbstractAttributeMap.this.size();
        }

        public boolean contains(Object o) {
            return AbstractAttributeMap.this.containsKey(o);
        }

        public boolean remove(Object o) {
            return AbstractAttributeMap.this.remove(o) != null;
        }

        public void clear() {
            AbstractAttributeMap.this.clear();
        }
    
    }

    private class KeyIterator implements Iterator {
    
        protected final Iterator _it = getAttributeNames();

        protected Object _currentKey;

        public void remove() {
            if (_currentKey == null) {
                throw new NoSuchElementException();
            }
            AbstractAttributeMap.this.remove(_currentKey);
        }

        public boolean hasNext() {
            return _it.hasNext();
        }

        public Object next() {
            return _currentKey = _it.next();
        }
    
    }

    private class Values extends KeySet {
    
        public Iterator iterator() {
            return new ValuesIterator();
        }

        public boolean contains(Object o) {
            return AbstractAttributeMap.this.containsValue(o);
        }

        public boolean remove(Object o) {
            if (o == null) {
                return false;
            }
            for (Iterator it = iterator(); it.hasNext();) {
                if (o.equals(it.next())) {
                    it.remove();
                    return true;
                }
            }
            return false;
        }
    
    }

    private class ValuesIterator extends KeyIterator {

        public Object next() {
            super.next();
            return AbstractAttributeMap.this.get(_currentKey);
        }
    
    }

    private class EntrySet extends KeySet {
    
        public Iterator iterator() {
            return new EntryIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry entry = (Entry) o;
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key == null || value == null) {
                return false;
            }
            return value.equals(AbstractAttributeMap.this.get(key));
        }

        public boolean remove(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry entry = (Entry) o;
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key == null || value == null
                    || !value.equals(AbstractAttributeMap.this.get(key))) {
                return false;
            }
            return AbstractAttributeMap.this.remove(((Entry) o).getKey()) != null;
        }
    
    }

    private class EntryIterator extends KeyIterator {
    
        public Object next() {
            super.next();
            return new EntrySetEntry(_currentKey);
        }
    
    }

    private class EntrySetEntry implements Entry {
    
        private final Object _currentKey;

        public EntrySetEntry(Object currentKey) {
            _currentKey = currentKey;
        }

        public Object getKey() {
            return _currentKey;
        }

        public Object getValue() {
            return AbstractAttributeMap.this.get(_currentKey);
        }

        public Object setValue(Object value) {
            return AbstractAttributeMap.this.put(_currentKey, value);
        }
    
    }

}