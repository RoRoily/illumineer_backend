package com.buaa01.illumineer_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommandType {
    /**
     * 建立连接
     */
    CONNETION(100),

    /**
     * 通知功能，发送
     */

    NOTICE(188),

    ERROR(-1),
    ;

    private final Integer code;

    public static CommandType match(Integer code) {
        for (CommandType value: CommandType.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return ERROR;
    }
}