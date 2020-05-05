package org.opencord.igmpproxy.statemacine;

public interface State {
    void join(boolean messageOutAllowed);

    void leave(boolean messageOutAllowed);

    void query(int maxResp);

    void timeOut(boolean sendQuery);
}
