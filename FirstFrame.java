import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class FirstFrame extends JFrame {
    private final GUItry mainFrame;

    //Takes an instance of GUItry as parameter to allow interaction with it.
    public FirstFrame(GUItry mainFrame) {
        this.mainFrame = mainFrame;

        setTitle("Add Section");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(300, 150);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.LIGHT_GRAY);

        JTextField sectionField = new JTextField();
        sectionField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(sectionField);

        JButton addButton = new JButton("Add Section");
        addButton.addActionListener(e -> {
            String newSection = sectionField.getText().trim();
            if (!newSection.isEmpty()) {
                mainFrame.addSection(newSection);
                sectionField.setText("");
            }
        });

        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(addButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        add(panel, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }
}
