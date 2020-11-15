package org.ecs160.a2.Widgets;
import java.util.ArrayList;

public class NodeOutput extends Node {
    // one output could map to multiple inputs
    private ArrayList<NodeInput> nextInputs = new ArrayList<NodeInput>();
    private boolean val;

    public NodeOutput(Widget widget) {
        super(widget);
        this.val = false;
    }
    protected void update(boolean val) {
        this.val = val;
        for (NodeInput input: nextInputs) {
            input.update();
        }
    }
    public boolean getVal() {
        return val;
    }
    public void connect(NodeInput input) {
        nextInputs.add(input);
    }
    public void disconnect(NodeInput input) {
        nextInputs.remove(input);
    }
}
