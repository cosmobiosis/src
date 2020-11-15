package org.ecs160.a2.Widgets;

public class NodeInput extends Node {
    private NodeOutput prev; // previous output connected

    public NodeInput(Widget widget) {
        super(widget);
        prev = null;
    }

    public void update() {
        this.getWidget().update();
    }

    public boolean getVal() {
        if (prev == null) {
            return false;
        } else {
            return prev.getVal();
        }
    }
    public void connect(NodeOutput output) {
        prev = output;
    }
    public void disconnect() {
        prev = null;
    }
    public boolean connected() {
        return prev != null;
    }
}
