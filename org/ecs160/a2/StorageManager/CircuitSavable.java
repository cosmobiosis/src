package org.ecs160.a2.StorageManager;
import com.codename1.io.Externalizable;
import com.codename1.io.Util;
import org.ecs160.a2.Objects.*;
import org.ecs160.a2.Objects.Interface.LogicGate;
import org.ecs160.a2.Objects.Interface.Widget;
import org.ecs160.a2.Utilities.WorkspaceUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;

public class CircuitSavable implements Externalizable {
    private Circuit circuit;
    public CircuitSavable(Circuit circuit) { this.circuit = circuit; }
    public Circuit extractCircuit() { return circuit; }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void externalize(DataOutputStream out) throws IOException {
        ArrayList<CircuitSavable> savableCircuits =  new ArrayList<>();
        for (Circuit subCircuit : circuit.getSubCircuits())
            savableCircuits.add(new CircuitSavable(subCircuit));
        Util.writeObject(savableCircuits, out);

        out.writeInt(circuit.getSwitches().size());
        out.writeInt(circuit.getLeds().size());

        ArrayList<GateSavable> savableGates =  new ArrayList<>();
        for (LogicGate gate: circuit.getGates())
            savableGates.add(new GateSavable(gate));
        Util.writeObject(savableGates, out);

        // Start to write connectivity
        Util.writeObject(getConnectivityGraph(), out);
    }

    @Override
    public void internalize(int version, DataInputStream in) throws IOException {
        // TODO: Internalize
        circuit = new Circuit(Integer.MAX_VALUE, Integer.MAX_VALUE);
        ArrayList<CircuitSavable> savablesSubcircuits = (ArrayList<CircuitSavable>)Util.readObject(in);
        for (CircuitSavable savableCircuit : savablesSubcircuits)
            circuit.getSubCircuits().add(savableCircuit.extractCircuit());

        int numSwitches = in.readInt();
        for (int i = 0; i < numSwitches; i++)
            circuit.getSwitches().add(new Switch(Integer.MAX_VALUE, Integer.MAX_VALUE));
        int numLeds = in.readInt();
        for (int i = 0; i < numLeds; i++)
            circuit.getLeds().add(new Led(Integer.MAX_VALUE, Integer.MAX_VALUE));

        ArrayList<GateSavable> savableGates = (ArrayList<GateSavable>)Util.readObject(in);
        for (GateSavable savableGate : savableGates)
            circuit.getGates().add(savableGate.extractGate());

        HashMap<HashMap<String, ArrayList<Integer>>,
                HashMap<String, ArrayList<Integer>>> connectivity =
                (HashMap)Util.readObject(in);
        for (HashMap<String, ArrayList<Integer>> inputAddress : connectivity.keySet()) {
            HashMap<String, ArrayList<Integer>> outputAddress = connectivity.get(inputAddress);

            int inputJ = extractOutputIndex(inputAddress);
            NodeInput input = getWidgetFrom(inputAddress).getAllInputNodes().get(inputJ);

            int outputJ = extractOutputIndex(outputAddress);
            NodeOutput output = getWidgetFrom(outputAddress).getAllOutputNodes().get(outputJ);
            
            WorkspaceUtil.getInstance().connect(input, output);
        }
    }

    public Widget getWidgetFrom(HashMap<String, ArrayList<Integer>> address) {
        String typeName = extractTypeName(address);
        int i = address.get(typeName).get(0);
        switch (typeName) {
            case "Circuit":
                return circuit.getSubCircuits().get(i);
            case "Switch":
                return circuit.getSwitches().get(i);
            case "Led":
                return circuit.getLeds().get(i);
            case "LogicGate":
                return circuit.getGates().get(i);
            default:
                throw new IllegalArgumentException("Unknown Object Type");
        }
    }

    public HashMap<HashMap<String, ArrayList<Integer>>,
            HashMap<String, ArrayList<Integer>>> getConnectivityGraph() {
        HashMap<HashMap<String, ArrayList<Integer>>,
                HashMap<String, ArrayList<Integer>>> connectivity = new HashMap<>();
        // connectivity: NodeInput => NodeOutput
        ArrayList<Widget> widgets = circuit.getAllWidgets();
        for (int i = 0; i < widgets.size(); i++) {
            Widget keyWidget = widgets.get(i);
            String keyString;
            if (keyWidget instanceof Circuit)
                keyString = "Circuit";
            else if (keyWidget instanceof Switch)
                keyString = "Switch";
            else if (keyWidget instanceof Led)
                keyString = "Led";
            else if (keyWidget instanceof LogicGate)
                keyString = "LogicGate";
            else
                throw new IllegalArgumentException("Unknown Object Type");

            ArrayList<NodeInput> inputs = keyWidget.getAllInputNodes();
            for (int j = 0; j < inputs.size(); j++) {
                // key
                ArrayList<Integer> inputIndices = new ArrayList<>();
                inputIndices.add(i);
                inputIndices.add(j);

                HashMap<String, ArrayList<Integer>> key =
                        new HashMap<>();
                key.put(keyString, inputIndices);
                // value
                NodeInput input = inputs.get(j);
                if (!input.connected())
                    continue;
                NodeOutput output = input.getConnectedOutput();
                HashMap<String, ArrayList<Integer>> value = getOutputAddress(output);
                if (value == null)
                    continue;
                connectivity.put(key, value);
            }
        }
        return connectivity;
    }

    public HashMap<String, ArrayList<Integer>> getOutputAddress(NodeOutput target) {
        HashMap<String, ArrayList<Integer>> address = new HashMap<>();
        ArrayList<Integer> indices = new ArrayList<>();;

        ArrayList<Circuit> subCircuits = circuit.getSubCircuits();
        ArrayList<Switch> switches = circuit.getSwitches();
        ArrayList<LogicGate> gates = circuit.getGates();

        for (int i = 0; i < subCircuits.size(); i++) {
            ArrayList<NodeOutput> outputNodes = subCircuits.get(i).getAllOutputNodes();
            for (int j = 0; j < outputNodes.size(); j++)
                if (target == outputNodes.get(j)) {
                    indices.add(i);
                    indices.add(j);
                    address.put("Circuit", indices);
                    return address;
                }
        }
        for (int i = 0; i < switches.size(); i++) {
            ArrayList<NodeOutput> outputNodes = switches.get(i).getAllOutputNodes();
            for (int j = 0; j < outputNodes.size(); j++)
                if (target == outputNodes.get(j)) {
                    indices.add(i);
                    indices.add(j);
                    address.put("Switch", indices);
                    return address;
                }
        }
        for (int i = 0; i < gates.size(); i++) {
            ArrayList<NodeOutput> outputNodes = gates.get(i).getAllOutputNodes();
            for (int j = 0; j < outputNodes.size(); j++)
                if (target == outputNodes.get(j)) {
                    indices.add(i);
                    indices.add(j);
                    address.put("LogicGate", indices);
                    return address;
                }
        }
        return null;
    }

    public String extractTypeName(HashMap<String, ArrayList<Integer>> address) {
        for (String key : address.keySet())
            return key;
        return null;
    }
    public int extractOutputIndex(HashMap<String, ArrayList<Integer>> address) {
        for (String key : address.keySet())
            return address.get(key).get(1);
        return -1;
    }

    @Override
    public String getObjectId() {
        return "CircuitSavable";
    }
}
