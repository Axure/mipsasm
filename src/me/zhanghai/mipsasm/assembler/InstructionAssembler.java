/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.mipsasm.assembler;

public interface InstructionAssembler {

    void offset(Instruction instruction, AssemblyContext context);

    void assemble(Instruction instruction, AssemblyContext context);
}
