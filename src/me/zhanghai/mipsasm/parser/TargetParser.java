/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.mipsasm.parser;

import me.zhanghai.mipsasm.assembler.InstructionIndex;
import me.zhanghai.mipsasm.assembler.Target;
import me.zhanghai.mipsasm.assembler.TargetLabel;
import me.zhanghai.mipsasm.util.IoUtils;

public class TargetParser {

    private TargetParser() {}

    public static Target parse(String targetString) throws ParserException {
        try {
            return Target.of(InstructionIndex.of(IoUtils.parseUnsignedInteger(targetString)));
        } catch (NumberFormatException e) {
            try {
                return Target.of(TargetLabel.of(targetString));
            } catch (IllegalArgumentException ex) {
                throw new IllegalLabelException("Target label name: " + targetString, ex);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalOperandException("Target value length too long", e);
        }
    }
}
