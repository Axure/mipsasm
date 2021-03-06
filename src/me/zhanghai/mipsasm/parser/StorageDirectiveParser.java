/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.mipsasm.parser;

import me.zhanghai.mipsasm.assembler.AssemblyContext;
import me.zhanghai.mipsasm.assembler.StorageDirective;
import me.zhanghai.mipsasm.util.IoUtils;

public abstract class StorageDirectiveParser {

    private StorageDirectiveParser() {}

    static void parse(String[] operandStringList, AssemblyContext context, int length) throws ParserException {

        if (operandStringList.length == 0) {
            throw new OperandCountMismatchException("Expected: >0; found: 0");
        }

        for (String operandString : operandStringList) {
            StorageDirective storage;
            try {
                storage = StorageDirective.of(IoUtils.parseUnsignedInteger(operandString), length);
            } catch (IllegalArgumentException e) {
                throw new IllegalOperandException("Storage: " + operandString, e);
            }
            context.appendAssemblable(storage);
        }
    }
}
