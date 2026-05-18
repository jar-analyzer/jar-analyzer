/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.taint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaintTransferTest {

    @Test
    void emptyTransfer_hasNoTaint() {
        TaintTransfer t = new TaintTransfer();
        assertFalse(t.hasTaint());
    }

    @Test
    void markLocal_makesItTainted() {
        TaintTransfer t = new TaintTransfer();
        t.markLocal(2);
        assertTrue(t.hasTaint());
        assertTrue(t.isLocalTainted(2));
        assertFalse(t.isLocalTainted(0));
        assertFalse(t.isLocalTainted(1));
    }

    @Test
    void markLocal_negativeIsIgnored() {
        TaintTransfer t = new TaintTransfer();
        t.markLocal(-1);
        assertFalse(t.hasTaint());
        assertFalse(t.isLocalTainted(-1));
    }

    @Test
    void markMultipleLocals_supported() {
        TaintTransfer t = new TaintTransfer();
        t.markLocal(0); // this
        t.markLocal(2); // arg1
        assertTrue(t.isLocalTainted(0));
        assertFalse(t.isLocalTainted(1));
        assertTrue(t.isLocalTainted(2));
    }

    @Test
    void returnTainted_separatelyTracked() {
        TaintTransfer t = new TaintTransfer();
        assertFalse(t.isReturnTainted());
        t.setReturnTainted(true);
        assertTrue(t.hasTaint()); // hasTaint 应该把 returnTainted 也算进去
        assertTrue(t.isReturnTainted());
    }

    @Test
    void copy_isDeep() {
        TaintTransfer a = new TaintTransfer();
        a.markLocal(3);
        a.setReturnTainted(true);

        TaintTransfer b = a.copy();
        b.markLocal(7);
        b.setReturnTainted(false);

        assertTrue(a.isLocalTainted(3));
        assertFalse(a.isLocalTainted(7), "copy 修改不应反向影响原对象");
        assertTrue(a.isReturnTainted(), "copy 修改 returnTainted 不应反向影响原对象");

        assertTrue(b.isLocalTainted(3));
        assertTrue(b.isLocalTainted(7));
        assertFalse(b.isReturnTainted());
    }

    @Test
    void reset_clearsState() {
        TaintTransfer t = new TaintTransfer();
        t.markLocal(2);
        t.setReturnTainted(true);
        t.reset();
        assertFalse(t.hasTaint());
        assertFalse(t.isLocalTainted(2));
        assertFalse(t.isReturnTainted());
    }
}
