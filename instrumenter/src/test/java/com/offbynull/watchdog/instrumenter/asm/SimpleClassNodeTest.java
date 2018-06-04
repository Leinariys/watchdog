package com.offbynull.watchdog.instrumenter.asm;

import static com.offbynull.watchdog.instrumenter.asm.SearchUtils.searchForOpcodes;
import static com.offbynull.watchdog.instrumenter.testhelpers.TestUtils.readZipFromResource;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class SimpleClassNodeTest {

    @Test
    public void mustNotFindAnyJsrInstructions() throws Exception {
        byte[] input = readZipFromResource("JsrInlineTest.zip").get("JsrExceptionSuspendTest.class");
        
        ClassReader cr = new ClassReader(input);
        ClassNode classNode = new SimpleClassNode();
        cr.accept(classNode, 0);
        
        for (MethodNode methodNode : classNode.methods) {
            assertTrue(searchForOpcodes(methodNode.instructions, Opcodes.JSR).isEmpty());
        }
    }
    
}
