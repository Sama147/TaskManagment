import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.toedter.calendar.JDateChooser;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class GUItry extends JFrame {

    private JPanel taskListPanel;
    private JTextField taskNameField;
    private JDateChooser dateChooser;
    private JComboBox<String> dropdown;
    private java.util.List<String> section = new ArrayList<>();
    private Map<String, Integer> sectionTaskCount = new HashMap<>();
    private Map<String, java.util.List<String>> tasksBySection = new HashMap<>();
    private final String DATA_FILE = "tasks.txt";

    public GUItry() {
        setTitle("Task Manager");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.decode("#f4f4f4"));

        // Header
        JLabel header = new JLabel("Task Manager", JLabel.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setBorder(new EmptyBorder(20, 10, 20, 10));
        add(header, BorderLayout.NORTH);

        // Main content container
        JSplitPane mainPanel = new JSplitPane();
        mainPanel.setDividerLocation(300);
        mainPanel.setResizeWeight(0.3);
        add(mainPanel, BorderLayout.CENTER);

        // Left form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        JLabel taskLabel = new JLabel("Task Name:");
        taskNameField = new JTextField(15);

        JLabel dateLabel = new JLabel("Due Date:");
        dateChooser = new JDateChooser();
        dateChooser.setPreferredSize(new Dimension(150, 25));

        JLabel sectionLabel = new JLabel("Section:");
        dropdown = new JComboBox<>();

        JButton addButton = new JButton("➕ Add Task");
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.addActionListener(e -> addTask());

        JButton sectionBtn = new JButton("➕ Add Section");
        sectionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        sectionBtn.addActionListener(e -> {
            FirstFrame ff = new FirstFrame(this);
            ff.setVisible(true);
        });

        // Add components
        for (JComponent comp : new JComponent[]{taskLabel, taskNameField, dateLabel, dateChooser, sectionLabel, dropdown, addButton, sectionBtn}) {
            comp.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
            formPanel.add(comp);
            formPanel.add(Box.createVerticalStrut(10));
        }

        mainPanel.setLeftComponent(formPanel);

        // Right panel (task display)
        taskListPanel = new JPanel();
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        taskListPanel.setBackground(Color.WHITE);
        taskListPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(taskListPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        mainPanel.setRightComponent(scrollPane);

        updateDropDown();
        loadTasks();
        setVisible(true);

        Runtime.getRuntime().addShutdownHook(new Thread(this::saveAllTasks));
    }

    public void addSection(String newSection) {
        if (!section.contains(newSection)) {
            section.add(newSection);
            sectionTaskCount.put(newSection, 0);
            tasksBySection.putIfAbsent(newSection, new ArrayList<>());
            updateDropDown();
            renderSections();
        }
    }

    private void updateDropDown() {
        dropdown.removeAllItems();
        for (String s : section) {
            dropdown.addItem(s);
        }
    }

    private void addTask() {
        String taskName = taskNameField.getText().trim();
        Date selectedDate = dateChooser.getDate();
        String selectedSection = (String) dropdown.getSelectedItem();

        if (taskName.isEmpty() || selectedDate == null || selectedSection == null) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dueDate = sdf.format(selectedDate);
        String taskText = taskName + " (Due: " + dueDate + ")";

        tasksBySection.computeIfAbsent(selectedSection, k -> new ArrayList<>()).add(taskText);
        sectionTaskCount.put(selectedSection, sectionTaskCount.getOrDefault(selectedSection, 0) + 1);

        taskNameField.setText("");
        dateChooser.setDate(null);
        renderSections();
    }

    private void renderSections() {
        taskListPanel.removeAll();
        for (String sec : section) {
            JLabel sectionTitle = new JLabel(sec);
            sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
            sectionTitle.setBorder(new EmptyBorder(10, 0, 5, 0));
            taskListPanel.add(sectionTitle);

            java.util.List<String> tasks = tasksBySection.getOrDefault(sec, new ArrayList<>());
            for (String task : tasks) {
                JLabel taskLabel = new JLabel("• " + task);
                taskLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                taskLabel.setBorder(new EmptyBorder(2, 10, 2, 0));
                taskListPanel.add(taskLabel);
            }

            taskListPanel.add(Box.createVerticalStrut(10));
        }
        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    private void saveAllTasks() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (String sec : section) {
                writer.println("[SECTION] " + sec);
                java.util.List<String> tasks = tasksBySection.getOrDefault(sec, new ArrayList<>());
                for (String task : tasks) {
                    writer.println(task);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTasks() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String currentSection = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("[SECTION] ")) {
                    currentSection = line.substring(10).trim();
                    addSection(currentSection);
                } else if (currentSection != null) {
                    tasksBySection.computeIfAbsent(currentSection, k -> new ArrayList<>()).add(line);
                }
            }
            renderSections();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new GUItry();
    }
}
