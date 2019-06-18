package com.aliware.tianchi.util;

import java.util.concurrent.atomic.AtomicLong;

public class MsgCounter {
    private static AtomicLong activeMsgCnt = new AtomicLong();
    private static AtomicLong sentMsgCnt = new AtomicLong();

    public static AtomicLong getActiveMsgCnt() {
        return activeMsgCnt;
    }

    public static AtomicLong getSentMsgCnt() {
        return sentMsgCnt;
    }
}
