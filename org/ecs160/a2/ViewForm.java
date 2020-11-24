package org.ecs160.a2;
import com.codename1.ui.*;
import com.codename1.ui.layouts.*;

public class ViewForm extends Form {
    public ViewForm() {
        super(new BorderLayout());
        ViewTaskbar taskbar = new ViewTaskbar();
        ViewWorkspace workspace = new ViewWorkspace(taskbar);
        Button testingSaveButton = new Button("Save");
        testingSaveButton.addActionListener((evt) -> {
            workspace.mainCircuit.save("Testing");
        });
        this.add(BorderLayout.NORTH, testingSaveButton).add(BorderLayout.CENTER, workspace);
        this.add(BorderLayout.SOUTH, taskbar);
    }


}
