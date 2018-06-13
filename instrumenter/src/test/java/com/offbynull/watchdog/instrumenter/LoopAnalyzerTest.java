package com.offbynull.watchdog.instrumenter;

import com.offbynull.watchdog.instrumenter.LoopAnalyzer.Loop;
import static com.offbynull.watchdog.instrumenter.LoopAnalyzer.walkCycles;
import java.util.ArrayList;
import static java.util.Collections.emptyMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

public class LoopAnalyzerTest {
    
    @Test
    public void mustFindLoop() {
        LabelNode label1 = new LabelNode();
        JumpInsnNode jump1 = new JumpInsnNode(Opcodes.IF_ICMPEQ, label1);
        InsnList insnList = new InsnList();
        insnList.add(new InsnNode(Opcodes.NOP));
        insnList.add(label1);
        insnList.add(new LdcInsnNode(1));
        insnList.add(new LdcInsnNode(2));
        insnList.add(jump1);
        insnList.add(new InsnNode(Opcodes.NOP));
        
        List<TryCatchBlockNode> tryCatchBlockNodes = new ArrayList<>();
        
        Set<Loop> actualLoops = walkCycles(insnList, tryCatchBlockNodes);
        Set<Loop> expectedLoops = new HashSet<>();
        expectedLoops.add(new Loop(jump1, label1));
        assertEquals(expectedLoops, actualLoops);
    }
    
    @Test
    public void mustTightLoop() {
        LabelNode label1 = new LabelNode();
        JumpInsnNode jump1 = new JumpInsnNode(Opcodes.IF_ICMPEQ, label1);
        InsnList insnList = new InsnList();
        insnList.add(label1);
        insnList.add(jump1);
        
        List<TryCatchBlockNode> tryCatchBlockNodes = new ArrayList<>();
        
        Set<Loop> actualLoops = walkCycles(insnList, tryCatchBlockNodes);
        Set<Loop> expectedLoops = new HashSet<>();
        expectedLoops.add(new Loop(jump1, label1));
        assertEquals(expectedLoops, actualLoops);
    }
    
    @Test
    public void mustFindMultipleLoopsToSameLabel() {
        LabelNode label1 = new LabelNode();
        JumpInsnNode jumpTo1_1 = new JumpInsnNode(Opcodes.IF_ICMPEQ, label1);
        JumpInsnNode jumpTo1_2 = new JumpInsnNode(Opcodes.IF_ICMPEQ, label1);
        JumpInsnNode jumpTo1_3 = new JumpInsnNode(Opcodes.IF_ICMPEQ, label1);
        InsnList insnList = new InsnList();
        insnList.add(new InsnNode(Opcodes.NOP));
        insnList.add(label1);
        insnList.add(new LdcInsnNode(1));
        insnList.add(new LdcInsnNode(2));
        insnList.add(jumpTo1_1);
        insnList.add(new LdcInsnNode(1));
        insnList.add(new LdcInsnNode(3));
        insnList.add(jumpTo1_2);
        insnList.add(new LdcInsnNode(1));
        insnList.add(new LdcInsnNode(4));
        insnList.add(jumpTo1_3);
        insnList.add(new InsnNode(Opcodes.NOP));
        
        List<TryCatchBlockNode> tryCatchBlockNodes = new ArrayList<>();
        
        Set<Loop> actualLoops = walkCycles(insnList, tryCatchBlockNodes);
        Set<Loop> expectedLoops = new HashSet<>();
        expectedLoops.add(new Loop(jumpTo1_1, label1));
        expectedLoops.add(new Loop(jumpTo1_2, label1));
        expectedLoops.add(new Loop(jumpTo1_3, label1));
        assertEquals(expectedLoops, actualLoops);
    }
    
    @Test
    public void mustFindMultipleIndependentLoops() {
        LabelNode label1 = new LabelNode();
        LabelNode label2 = new LabelNode();
        JumpInsnNode jumpTo1 = new JumpInsnNode(Opcodes.IF_ICMPEQ, label1);
        JumpInsnNode jumpTo2 = new JumpInsnNode(Opcodes.IF_ICMPEQ, label2);
        InsnList insnList = new InsnList();
        insnList.add(label1);
        insnList.add(new LdcInsnNode(1));
        insnList.add(new LdcInsnNode(2));
        insnList.add(jumpTo1);
        insnList.add(new InsnNode(Opcodes.NOP));
        insnList.add(label2);
        insnList.add(new LdcInsnNode(1));
        insnList.add(new LdcInsnNode(3));
        insnList.add(jumpTo2);
        
        List<TryCatchBlockNode> tryCatchBlockNodes = new ArrayList<>();
        
        Set<Loop> actualLoops = walkCycles(insnList, tryCatchBlockNodes);
        Set<Loop> expectedLoops = new HashSet<>();
        expectedLoops.add(new Loop(jumpTo1, label1));
        expectedLoops.add(new Loop(jumpTo2, label2));
        assertEquals(expectedLoops, actualLoops);
    }
    
    @Test
    public void mustFindMultipleLoopsInsideLargerLoop() {
        LabelNode labelMain = new LabelNode();
        LabelNode label1 = new LabelNode();
        LabelNode label2 = new LabelNode();
        JumpInsnNode jumpMain = new JumpInsnNode(Opcodes.IF_ICMPEQ, labelMain);
        JumpInsnNode jumpTo1 = new JumpInsnNode(Opcodes.IF_ICMPEQ, label1);
        JumpInsnNode jumpTo2_1 = new JumpInsnNode(Opcodes.IF_ICMPEQ, label2);
        JumpInsnNode jumpTo2_2 = new JumpInsnNode(Opcodes.IF_ICMPNE, label2);
        InsnList insnList = new InsnList();
        insnList.add(labelMain);
        insnList.add(label1);
        insnList.add(new LdcInsnNode(1));
        insnList.add(new LdcInsnNode(2));
        insnList.add(jumpTo1);
        insnList.add(new InsnNode(Opcodes.NOP));
        insnList.add(label2);
        insnList.add(new LdcInsnNode(1));
        insnList.add(new LdcInsnNode(3));
        insnList.add(jumpTo2_1);
        insnList.add(jumpTo2_2);
        insnList.add(jumpMain);
        
        List<TryCatchBlockNode> tryCatchBlockNodes = new ArrayList<>();
        
        Set<Loop> actualLoops = walkCycles(insnList, tryCatchBlockNodes);
        Set<Loop> expectedLoops = new HashSet<>();
        expectedLoops.add(new Loop(jumpTo1, label1));
        expectedLoops.add(new Loop(jumpTo2_1, label2));
        expectedLoops.add(new Loop(jumpTo2_2, label2));
        expectedLoops.add(new Loop(jumpMain, labelMain));
        assertEquals(expectedLoops, actualLoops);
    }
    
    @Test
    public void mustNotAccountForDeadCode() {
        LabelNode label1 = new LabelNode();
        LabelNode label2 = new LabelNode();
        LabelNode labelDead = new LabelNode();
        JumpInsnNode jumpTo1 = new JumpInsnNode(Opcodes.IF_ICMPEQ, label1);
        JumpInsnNode jumpTo2 = new JumpInsnNode(Opcodes.IF_ICMPNE, label2);
        JumpInsnNode forceTo2 = new JumpInsnNode(Opcodes.GOTO, label2);
        JumpInsnNode jumpToDead = new JumpInsnNode(Opcodes.IF_ICMPNE, labelDead);
        InsnList insnList = new InsnList();
        insnList.add(label1);
        insnList.add(new LdcInsnNode(1));
        insnList.add(new LdcInsnNode(2));
        insnList.add(jumpTo1);
        insnList.add(forceTo2);
        insnList.add(new InsnNode(Opcodes.NOP));
        insnList.add(new InsnNode(Opcodes.NOP));
        insnList.add(new InsnNode(Opcodes.NOP));
        insnList.add(labelDead);
        insnList.add(new LdcInsnNode(1));
        insnList.add(new LdcInsnNode(2));
        insnList.add(jumpToDead);
        insnList.add(new InsnNode(Opcodes.NOP));
        insnList.add(new InsnNode(Opcodes.NOP));
        insnList.add(new InsnNode(Opcodes.NOP));
        insnList.add(label2);
        insnList.add(new LdcInsnNode(1));
        insnList.add(new LdcInsnNode(3));
        insnList.add(jumpTo2);
        
        List<TryCatchBlockNode> tryCatchBlockNodes = new ArrayList<>();
        
        Set<Loop> actualLoops = walkCycles(insnList, tryCatchBlockNodes);
        Set<Loop> expectedLoops = new HashSet<>();
        expectedLoops.add(new Loop(jumpTo1, label1));
        expectedLoops.add(new Loop(jumpTo2, label2));
        assertEquals(expectedLoops, actualLoops);
    }
    
    @Test
    public void mustProperlyHandleTableSwitchLoops() {
        LabelNode label1 = new LabelNode();
        LabelNode label2 = new LabelNode();
        LabelNode label3 = new LabelNode();
        LabelNode labelDefault = new LabelNode();
        LabelNode labelEnd = new LabelNode();
        TableSwitchInsnNode tableSwitch = new TableSwitchInsnNode(0, 3, labelDefault, label1, label2, label3);
        JumpInsnNode forceToEnd_1 = new JumpInsnNode(Opcodes.GOTO, labelEnd);
        JumpInsnNode forceToEnd_2 = new JumpInsnNode(Opcodes.GOTO, labelEnd);
        JumpInsnNode forceToEnd_3 = new JumpInsnNode(Opcodes.GOTO, labelEnd);
        InsnList insnList = new InsnList();
        insnList.add(labelDefault);
        insnList.add(tableSwitch);
        insnList.add(label1);
        insnList.add(new InsnNode(Opcodes.NOP));
        insnList.add(forceToEnd_1);
        insnList.add(label2);
        insnList.add(new InsnNode(Opcodes.NOP));
        insnList.add(forceToEnd_2);
        insnList.add(label3);
        insnList.add(new InsnNode(Opcodes.NOP));
        insnList.add(forceToEnd_3);
        insnList.add(labelEnd);
        
        List<TryCatchBlockNode> tryCatchBlockNodes = new ArrayList<>();
        
        Set<Loop> actualLoops = walkCycles(insnList, tryCatchBlockNodes);
        Set<Loop> expectedLoops = new HashSet<>();
        expectedLoops.add(new Loop(tableSwitch, labelDefault));
        assertEquals(expectedLoops, actualLoops);
    }
}
