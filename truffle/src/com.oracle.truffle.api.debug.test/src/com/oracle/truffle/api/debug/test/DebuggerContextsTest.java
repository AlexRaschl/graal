/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.api.debug.test;

import com.oracle.truffle.api.debug.DebugContext;
import com.oracle.truffle.api.debug.DebugContextsListener;
import com.oracle.truffle.api.debug.DebugValue;
import com.oracle.truffle.api.debug.Debugger;
import com.oracle.truffle.api.debug.DebuggerSession;
import com.oracle.truffle.api.debug.SuspendedCallback;
import com.oracle.truffle.api.debug.SuspendedEvent;
import com.oracle.truffle.api.instrumentation.test.InstrumentationTestLanguage;
import com.oracle.truffle.api.nodes.LanguageInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.graalvm.collections.list.SpecifiedArrayList;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DebuggerContextsTest extends AbstractDebugTest {

    @Test
    public void testSingleContext() {
        final Source source = testSource("STATEMENT()");
        TestContextsListener contextsListener = new TestContextsListener();
        List<ContextEvent> events = contextsListener.events;
        try (DebuggerSession session = startSession()) {
            session.suspendNextExecution();
            session.setContextsListener(contextsListener, false);
            startEval(source);

            expectSuspended((SuspendedEvent event) -> {
                assertEquals(2, events.size());
                assertTrue(events.get(0).created);
                assertEquals(InstrumentationTestLanguage.ID, events.get(0).language.getId());
                assertNotNull(events.get(0).context);
                assertTrue(events.get(1).languageInitialized);
            });
            expectDone();
            closeEngine();
            assertEquals(5, events.size());
            assertTrue(events.get(2).languageFinalized);
            assertEquals(InstrumentationTestLanguage.ID, events.get(2).language.getId());
            assertEquals(events.get(0).context, events.get(2).context);
            assertFalse(events.get(3).created);
            assertEquals(InstrumentationTestLanguage.ID, events.get(3).language.getId());
            assertEquals(events.get(0).context, events.get(3).context);
            assertFalse(events.get(4).created);
            assertNull(events.get(4).language);
            assertEquals(events.get(0).context, events.get(4).context);
        }
        events.clear();
    }

    @Test
    public void testInnerContext() {
        final Source source = testSource("ROOT(STATEMENT(), CONTEXT(STATEMENT()))");
        TestContextsListener contextsListener = new TestContextsListener();
        List<ContextEvent> events = contextsListener.events;
        try (DebuggerSession session = startSession()) {
            session.setContextsListener(contextsListener, false);
            startEval(source);
            expectDone();

            assertEquals(8, events.size());
            assertTrue(events.get(0).created);
            assertTrue(events.get(1).languageInitialized);
            assertNotEquals(events.get(1).context, events.get(2).context);
            assertEquals(events.get(1).context, events.get(2).context.getParent());
            assertTrue(events.get(2).created);
            assertNull(events.get(2).language);
            assertTrue(events.get(3).created);
            assertNotNull(events.get(3).language);
            assertTrue(events.get(4).languageInitialized);
            assertTrue(events.get(5).languageFinalized);
            assertFalse(events.get(6).created);
            assertEquals(InstrumentationTestLanguage.ID, events.get(6).language.getId());
            assertFalse(events.get(7).created);
            assertNull(events.get(7).language);
            assertEquals(events.get(2).context, events.get(7).context);
            closeEngine();
        }
        assertEquals(11, events.size());
        assertTrue(events.get(8).languageFinalized);
        assertEquals(events.get(0).context, events.get(8).context);
        assertFalse(events.get(9).created);
        assertEquals(InstrumentationTestLanguage.ID, events.get(9).language.getId());
        assertFalse(events.get(10).created);
        assertNull(events.get(10).language);
        assertEquals(events.get(0).context, events.get(10).context);
    }

    @Test
    public void testMultipleContexts() {
        Source source = Source.create(InstrumentationTestLanguage.ID, "STATEMENT()");
        Engine engine = Engine.create();
        TestContextsListener contextsListener = new TestContextsListener();
        List<ContextEvent> events = contextsListener.events;

        int numContexts = 5;
        Debugger debugger = engine.getInstruments().get("debugger").lookup(Debugger.class);
        try (DebuggerSession session = debugger.startSession(null)) {
            session.setContextsListener(contextsListener, false);
            for (int i = 0; i < numContexts; i++) {
                try (Context context = Context.newBuilder().engine(engine).build()) {
                    assertEquals(6 * i + 1, events.size());
                    context.eval(source);
                }
                assertEquals(6 * i + 6, events.size());
            }
            assertEquals(6 * numContexts, events.size());
            DebugContext lastContext = null;
            for (int i = 0; i < numContexts; i++) {
                int ci = 6 * i;
                assertTrue(events.get(ci).created);
                assertNull(events.get(ci).language);
                assertNotEquals(lastContext, events.get(ci).context);
                lastContext = events.get(ci).context;
                assertTrue(events.get(ci + 1).created);
                assertNotNull(events.get(ci + 1).language);
                assertEquals(lastContext, events.get(ci + 1).context);
                assertTrue(events.get(ci + 2).languageInitialized);
                assertTrue(events.get(ci + 3).languageFinalized);
                assertNotNull(events.get(ci + 4).language);
                assertNull(events.get(ci + 5).language);
            }
            engine.close();
        }
        // No more events
        assertEquals(6 * numContexts, events.size());
    }

    @Test
    public void testGetActiveContexts() {
        final DebuggerSession[] sessionPtr = new DebuggerSession[1];
        try (Engine engine = Engine.create()) {
            TestContextsListener contextsListener = new TestContextsListener();
            List<ContextEvent> events = contextsListener.events;
            Debugger debugger = engine.getInstruments().get("debugger").lookup(Debugger.class);
            try (DebuggerSession session = debugger.startSession(new SuspendedCallback() {
                @Override
                public void onSuspend(SuspendedEvent event) {
                    sessionPtr[0].setContextsListener(contextsListener, true);
                    assertEquals(3, events.size());
                    assertTrue(events.get(0).created);
                    assertNull(events.get(0).language);
                    assertTrue(events.get(1).created);
                    assertEquals(InstrumentationTestLanguage.ID, events.get(1).language.getId());
                    assertTrue(events.get(2).languageInitialized);
                }
            })) {
                session.setContextsListener(contextsListener, true);
                try (Context context = Context.newBuilder().engine(engine).build()) {
                    context.eval(Source.create(InstrumentationTestLanguage.ID, "STATEMENT()"));
                    assertEquals(3, events.size());
                    // Have creation of polyglot context and a language
                    assertTrue(events.get(0).created);
                    assertNotNull(events.get(0).context);
                    assertNull(events.get(0).language);
                    assertTrue(events.get(1).created);
                    assertNotNull(events.get(1).language);
                    assertTrue(events.get(2).languageInitialized);
                }
                assertEquals(6, events.size());
                session.setContextsListener(null, true);
                events.clear();
                // Contexts created and closed:
                try (Context context = Context.newBuilder().engine(engine).build()) {
                    context.eval(Source.create(InstrumentationTestLanguage.ID, "STATEMENT()"));
                    context.eval(Source.create(InstrumentationTestLanguage.ID, "CONTEXT(STATEMENT())"));
                }
                assertEquals(0, events.size());
                sessionPtr[0] = session;
                session.suspendNextExecution();
                Context context = Context.newBuilder().engine(engine).build();
                context.eval(Source.create(InstrumentationTestLanguage.ID, "ROOT(CONTEXT(EXPRESSION()), CONTEXT(STATEMENT))"));
            }
        }
    }

    @Test
    public void testInContextEval() {
        try (Engine engine = Engine.create()) {
            TestContextsListener contextsListener = new TestContextsListener();
            List<ContextEvent> events = contextsListener.events;
            Debugger debugger = engine.getInstruments().get("debugger").lookup(Debugger.class);
            try (DebuggerSession session = debugger.startSession(null)) {
                session.setContextsListener(contextsListener, false);
                Context context = Context.newBuilder().engine(engine).build();
                assert context != null;
                assertEquals(1, events.size());
                assertTrue(events.get(0).created);
                DebugContext dc = events.get(0).context;
                assertNotNull(dc);
                assertNull(events.get(0).language);
                // "Global" evaluation in the brand new context
                DebugValue result = dc.evaluate("VARIABLE(a, 10)", InstrumentationTestLanguage.ID);
                assertEquals(3, events.size());
                assertTrue(events.get(1).created);
                assertEquals(InstrumentationTestLanguage.ID, events.get(1).language.getId());
                assertTrue(events.get(2).languageInitialized);
                String type = dc.runInContext(() -> {
                    assertEquals("10", result.as(String.class));
                    DebugValue metaObj = result.getMetaObject();
                    return metaObj.as(String.class);
                });
                assertEquals("Integer", type);
                assertEquals(3, events.size());
            }
        }
    }

    private static class TestContextsListener implements DebugContextsListener {

        final List<ContextEvent> events = Collections.synchronizedList(SpecifiedArrayList.createNew());

        @Override
        public void contextCreated(DebugContext context) {
            events.add(new ContextEvent(true, context, null));
        }

        @Override
        public void languageContextCreated(DebugContext context, LanguageInfo language) {
            events.add(new ContextEvent(true, context, language));
        }

        @Override
        public void languageContextInitialized(DebugContext context, LanguageInfo language) {
            events.add(new ContextEvent(false, context, language, true, false));
        }

        @Override
        public void languageContextFinalized(DebugContext context, LanguageInfo language) {
            events.add(new ContextEvent(false, context, language, false, true));
        }

        @Override
        public void languageContextDisposed(DebugContext context, LanguageInfo language) {
            events.add(new ContextEvent(false, context, language));
        }

        @Override
        public void contextClosed(DebugContext context) {
            events.add(new ContextEvent(false, context, null));
        }

    }

    private static class ContextEvent {

        final boolean created;
        final DebugContext context;
        final LanguageInfo language;
        final boolean languageInitialized;
        final boolean languageFinalized;

        ContextEvent(boolean created, DebugContext context, LanguageInfo language) {
            this(created, context, language, false, false);
        }

        ContextEvent(boolean created, DebugContext context, LanguageInfo language, boolean languageInitialized, boolean languageFinalized) {
            this.created = created;
            this.context = context;
            this.language = language;
            this.languageInitialized = languageInitialized;
            this.languageFinalized = languageFinalized;
        }
    }
}
