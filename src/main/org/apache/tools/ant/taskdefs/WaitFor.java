/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.ConditionBase;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.types.EnumeratedAttribute;

import java.util.Hashtable;

/**
 * Wait for an external event to occur.
 *
 * Wait for an external process to start or to complete some
 * task. This is useful with the <code>parallel</code> task to
 * syncronize the execution of tests with server startup.
 *
 * The following attributes can be specified on a waitfor task:
 * <ul>
 * <li>maxwait - maximum length of time to wait before giving up</li>
 * <li>maxwaitunit - The unit to be used to interpret maxwait attribute</li>
 * <li>checkevery - amount of time to sleep between each check</li>
 * <li>checkeveryunit - The unit to be used to interpret checkevery attribute</li>
 * </ul>
 *
 * The maxwaitunit and checkeveryunit are allowed to have the following values:
 * millesond, second, minute, hour, day and week. The default is millisecond.
 *
 * @author <a href="mailto:denis@network365.com">Denis Hennessy</a>
 * @author <a href="mailto:umagesh@apache.org">Magesh Umasankar</a>
 */

public class WaitFor extends ConditionBase {
    private long maxWaitMillis = 1000l * 60l * 3l; // default max wait time
    private long checkEveryMillis = 500l;

    /**
     * Set the maximum length of time to wait
     */
    public void setMaxWait(long time) {
        maxWaitMillis = time;
    }

    /**
     * Set the max wait time unit
     */
    public void setMaxWaitUnit(Unit unit) {
        maxWaitMillis *= unit.getMultiplier();
    }

    /**
     * Set the time between each check
     */
    public void setCheckEvery(long time) {
        checkEveryMillis = time;
    }

    /**
     * Set the check every time unit
     */
    public void setCheckEveryUnit(Unit unit) {
        checkEveryMillis *= unit.getMultiplier();
    }

    /**
     * Check repeatedly for the specified conditions until they become
     * true or the timeout expires.
     */
    public void execute() throws BuildException {
        if (countConditions() > 1) {
            throw new BuildException("You must not nest more than one condition into <waitfor>");
        }
        if (countConditions() < 1) {
            throw new BuildException("You must nest a condition into <waitfor>");
        }
        Condition c = (Condition) getConditions().nextElement();

        long start = System.currentTimeMillis();
        long end = start + maxWaitMillis;

        while (System.currentTimeMillis() < end) {
            if (c.eval()) {
                return;
            }
            try {
                Thread.sleep(checkEveryMillis);
            } catch (InterruptedException e) {
            }
        }

        throw new BuildException("Task did not complete in time");
    }

    public static class Unit extends EnumeratedAttribute {

        private static final String MILLISECOND = "millisecond";
        private static final String SECOND = "second";
        private static final String MINUTE = "minute";
        private static final String HOUR = "hour";
        private static final String DAY = "day";
        private static final String WEEK = "week";

        private final static String[] units = {
            MILLISECOND, SECOND, MINUTE, HOUR, DAY, WEEK
        };

        private Hashtable timeTable = new Hashtable();

        public Unit() {
            timeTable.put(MILLISECOND, new Long(1l));
            timeTable.put(SECOND,      new Long(1000l));
            timeTable.put(MINUTE,      new Long(1000l * 60l));
            timeTable.put(HOUR,        new Long(1000l * 60l * 60l));
            timeTable.put(DAY,         new Long(1000l * 60l * 60l * 24l));
            timeTable.put(WEEK,        new Long(1000l * 60l * 60l * 24l * 7l));
        }

        public long getMultiplier() {
            String key = getValue().toLowerCase();
            Long l = (Long) timeTable.get(key);
            return l.longValue();
        }

        public String[] getValues() {
            return units;
        }
    }
}
