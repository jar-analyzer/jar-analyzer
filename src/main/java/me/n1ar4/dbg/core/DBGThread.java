/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.n1ar4.dbg.core;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import me.n1ar4.dbg.gui.TableManager;
import me.n1ar4.dbg.parser.CoreParser;
import me.n1ar4.dbg.parser.MethodObject;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.util.ArrayList;
import java.util.List;

class DBGThread extends Thread {
    private static final Logger logger = LogManager.getLogger();
    private static volatile boolean running = false;
    private final DBGRunner runner;
    private final EventQueue eventQueue;
    private final String mainClassName;

    public DBGThread(DBGRunner runner) {
        this.runner = runner;
        this.mainClassName = runner.getMainClassName();
        this.eventQueue = this.runner.getEngine().getVm().eventQueue();
        running = true;
    }

    public static void stopRun() {
        logger.info("stop java-dbg thread");
        running = false;
    }

    private void resumeThread() {
        logger.info("resume java-dbg thread");
        runner.getEngine().getVm().resume();
    }

    @Override
    public void run() {
        while (running) {
            EventSet eventSet;
            try {
                eventSet = eventQueue.remove();
            } catch (Exception ex) {
                logger.info("target disconnect");
                break;
            }
            if (eventSet == null || eventSet.isEmpty()) {
                continue;
            }
            for (Event event : eventSet) {
                // break point for main method
                if (event instanceof VMStartEvent) {
                    logger.info("jvm start event");
                    this.resumeThread();
                } else if (event instanceof ClassPrepareEvent) {
                    ClassPrepareEvent cpe = (ClassPrepareEvent) event;
                    ReferenceType refType = cpe.referenceType();
                    logger.info("class prepare event: {}", refType.name());
                    if (refType.name().equals(this.mainClassName)) {
                        Method mainMethod = null;
                        for (Method method : refType.methods()) {
                            // main method
                            if (method.name().equals("main") &&
                                    method.signature().equals("([Ljava/lang/String;)V")) {
                                mainMethod = method;
                                break;
                            }
                        }
                        if (mainMethod != null) {
                            Location location = mainMethod.location();
                            this.runner.doBreakpointLoc(location, cpe.thread());
                            this.resumeThread();
                        }
                    }
                } else if (event instanceof BreakpointEvent) {
                    BreakpointEvent bpe = (BreakpointEvent) event;
                    logger.info("break event: {}", bpe.thread().name());
                    parseEvent(bpe.thread());
                    handleNewBPE(bpe);
                    bpe.request().disable();
                } else if (event instanceof StepEvent) {
                    TableManager.reset();
                    StepEvent see = (StepEvent) event;
                    logger.info("step event: {}", see.thread().name());
                    parseEvent(see.thread());
                    handleNewSE(see);
                    see.request().disable();
                } else if (event instanceof MethodEntryEvent) {
                    MethodEntryEvent mee = (MethodEntryEvent) event;
                    logger.info("method entry: {}", mee.method().name());
                    // do
                    mee.request().disable();
                } else if (event instanceof MethodExitEvent) {
                    MethodExitEvent mee = (MethodExitEvent) event;
                    logger.info("method exit: {}", mee.method().name());
                    // do
                    mee.request().disable();
                } else if (event instanceof VMDeathEvent) {
                    logger.info("vm death event");
                } else if (event instanceof VMDisconnectEvent) {
                    logger.info("vm disconnect event");
                }
            }

        }
        logger.info("stop dbg runner");
    }

    private void handleNewBPE(BreakpointEvent bpe) {
        logger.info("receive break point event");
        logger.info(bpe.toString());
    }

    private void handleNewSE(StepEvent se) {
        logger.info("receive step event");
        logger.info(se.toString());
    }

    private void parseEvent(ThreadReference thread) {
        try {
            List<StackFrame> frames = thread.frames();

            if (frames.isEmpty()) {
                logger.error("frame is null");
                return;
            }

            Render.refreshFrames(frames);

            StackFrame frame = frames.get(0);
            Location location = frame.location();
            String className = location.declaringType().name();
            String methodName = location.method().name();
            long codeIndex = location.codeIndex();
            logger.info("break event class: {} method: {} index: {}",
                    className, methodName, codeIndex);

            MethodObject mo = CoreParser.parse(location);
            Render.refreshMethodModel(mo, location.codeIndex());

            List<LocalVariable> all;
            try {
                all = frame.visibleVariables();
            } catch (AbsentInformationException e) {
                logger.warn("information not available: {}", e.toString());
                all = new ArrayList<>();
            }
            Render.refreshVariables(frame, all);
        } catch (IncompatibleThreadStateException e) {
            logger.error("Error accessing stack frame: {}", e.toString());
        }
    }
}
