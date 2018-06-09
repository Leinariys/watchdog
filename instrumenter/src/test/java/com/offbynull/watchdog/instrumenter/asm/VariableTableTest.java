package com.offbynull.watchdog.instrumenter.asm;

import static com.offbynull.watchdog.instrumenter.testhelpers.TestUtils.readZipFromResource;
import com.offbynull.watchdog.instrumenter.asm.VariableTable.Variable;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public final class VariableTableTest {
    
    private ClassNode classNode;
    private MethodNode methodNode;
    
    @BeforeEach
    public void setUp() throws IOException {
        byte[] classData = readZipFromResource("SimpleStub.zip").get("SimpleStub.class");
        
        ClassReader classReader = new ClassReader(classData);
        classNode = new ClassNode();
        classReader.accept(classNode, 0);
        
        methodNode = classNode.methods.get(1); // stub should be here
    }

    @Test
    public void mustBeAbleToAccessThisReference() {
        VariableTable fixture = new VariableTable(classNode, methodNode);
        Variable var = fixture.getArgument(0);
        
        assertEquals(var.getType(), Type.getObjectType(classNode.name));
        assertEquals(var.getIndex(), 0);
        assertTrue(var.isUsed());
    }

    @Test
    public void mustBeAbleToAccessParameter() {
        // Augment stub method before testing
        methodNode.desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.LONG_TYPE);
        
        VariableTable fixture = new VariableTable(classNode, methodNode);
        Variable var = fixture.getArgument(1);
        
        assertEquals(var.getType(), Type.LONG_TYPE);
        assertEquals(var.getIndex(), 1);
        assertTrue(var.isUsed());
    }

    @Test
    public void mustBeAbleToAcquireExtraVariable() {
        VariableTable fixture = new VariableTable(classNode, methodNode);
        
        Variable var1 = fixture.acquireExtra(Type.LONG_TYPE);
        assertEquals(var1.getType(), Type.LONG_TYPE);
        assertEquals(var1.getIndex(), 1);
        assertTrue(var1.isUsed());
        
        Variable var2 = fixture.acquireExtra(Type.BOOLEAN_TYPE);
        assertEquals(var2.getType(), Type.BOOLEAN_TYPE);
        assertEquals(var2.getIndex(), 3);
        assertTrue(var2.isUsed());
    }

    @Test
    public void mustBeAbleToReleaseExtraVariable() {
        VariableTable fixture = new VariableTable(classNode, methodNode);
        Variable var1 = fixture.acquireExtra(Type.LONG_TYPE);
        Variable var2 = fixture.acquireExtra(Type.LONG_TYPE);
        
        fixture.releaseExtra(var1);
        fixture.releaseExtra(var2);
        
        assertFalse(var1.isUsed());
        assertFalse(var2.isUsed());
    }

    @Test
    public void mustFailIfReleasingAlreadyReleasedExtraVariable() {
        VariableTable fixture = new VariableTable(classNode, methodNode);
        Variable var1 = fixture.acquireExtra(Type.LONG_TYPE);
        
        fixture.releaseExtra(var1);
        
        assertThrows(IllegalArgumentException.class, () -> {
            fixture.releaseExtra(var1);
        });
    }

    @Test
    public void mustFailIfAccessingReleasedExtraVariableType() {
        VariableTable fixture = new VariableTable(classNode, methodNode);
        Variable var1 = fixture.acquireExtra(Type.LONG_TYPE);
        
        fixture.releaseExtra(var1);
        
        assertThrows(IllegalArgumentException.class, () -> {
            var1.getType();
        });
    }
    
    @Test
    public void mustFailIfAccessingReleasedExtraVariableIndex() {
        VariableTable fixture = new VariableTable(classNode, methodNode);
        Variable var1 = fixture.acquireExtra(Type.LONG_TYPE);
        
        fixture.releaseExtra(var1);
        
        assertThrows(IllegalArgumentException.class, () -> {
            var1.getIndex();
        });
    }

    @Test
    public void mustNotReturnTheSameObjectIfReacquiringExtraVariable() {
        VariableTable fixture = new VariableTable(classNode, methodNode);
        Variable var1 = fixture.acquireExtra(Type.LONG_TYPE);
        
        fixture.releaseExtra(var1);
        
        Variable var1Reacquired = fixture.acquireExtra(Type.LONG_TYPE);
        
        assertFalse(var1.isUsed());
        
        assertEquals(var1Reacquired.getType(), Type.LONG_TYPE);
        assertEquals(var1Reacquired.getIndex(), 1);
        assertTrue(var1Reacquired.isUsed());
        assertThrows(IllegalArgumentException.class, () -> {
            var1.getIndex();
        });
    }
    
}
