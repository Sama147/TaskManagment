import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.toedter.calendar.JDateChooser;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GUItry extends JFrame {

    private JPanel taskListPanel;
    private JTextField taskNameField;
    private JDateChooser dateChooser;
    private JComboBox<String> dropdown;
    private JComboBox<String> priorityDropdown;
    private List<String> sections = new ArrayList<>();
    private Map<String, Integer> sectionTaskCount = new HashMap<>();
    private Map<String, List<Task>> tasksBySection = new HashMap<>();
    private final String DATA_FILE = "tasks.txt";
    private JTextField binarySearchField;
    private List<Task> allTasksList = new ArrayList<>();

    public static class Task {
        String name;
        String priority;
        Date dueDate;

        public Task(String name, String priority, Date dueDate) {
            this.name = name;
            this.priority = priority;
            this.dueDate = dueDate;
        }

        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return name + " (Priority: " + priority + ", Due: " + sdf.format(dueDate) + ")";
        }
    }

    public GUItry() {
        setTitle("Task Manager");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.decode("#f4f4f4"));

        JLabel header = new JLabel("Task Manager", JLabel.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setBorder(new EmptyBorder(20, 10, 20, 10));
        add(header, BorderLayout.NORTH);

        JSplitPane mainPanel = new JSplitPane();
        mainPanel.setDividerLocation(300);
        mainPanel.setResizeWeight(0.3);
        add(mainPanel, BorderLayout.CENTER);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        JLabel sectionTitleLabel = new JLabel("Task Section:");
        sectionTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        dropdown = new JComboBox<>();
        dropdown.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        dropdown.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton sectionBtn = new JButton("➕ Add Section");
        sectionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        sectionBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        sectionBtn.addActionListener(e -> {
            FirstFrame ff = new FirstFrame(this);
            ff.setVisible(true);
        });

        JLabel taskLabel = new JLabel("Task Name:");
        taskLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        taskNameField = new JTextField(15);
        taskNameField.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        taskNameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel priorityLabel = new JLabel("Priority Level:");
        priorityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        priorityDropdown = new JComboBox<>(new String[] { "High", "Moderate", "Low" });
        priorityDropdown.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        priorityDropdown.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel dateLabel = new JLabel("Due Date:");
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        dateChooser = new JDateChooser();
        dateChooser.setPreferredSize(new Dimension(150, 25));
        dateChooser.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        dateChooser.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton addButton = new JButton("➕ Add Task");
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        addButton.addActionListener(e -> addTask());

        formPanel.add(sectionTitleLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(dropdown);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(sectionBtn);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(taskLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(taskNameField);
        formPanel.add(Box.createVerticalStrut(30));
        formPanel.add(priorityLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(priorityDropdown);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(dateLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(dateChooser);
        formPanel.add(Box.createVerticalStrut(30));
        formPanel.add(addButton);

        mainPanel.setLeftComponent(formPanel);

        taskListPanel = new JPanel();
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        taskListPanel.setBackground(Color.WHITE);
        taskListPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(taskListPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.add(createSearchPanel(), BorderLayout.NORTH);
        displayPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.setRightComponent(displayPanel);

        updateDropDown();
        loadTasks();
        setVisible(true);

        Runtime.getRuntime().addShutdownHook(new Thread(this::saveAllTasks));
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout());
        binarySearchField = new JTextField();
        binarySearchField.setPreferredSize(new Dimension(400, 30));

        binarySearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String query = binarySearchField.getText().trim().toLowerCase();
                if (query.isEmpty()) {
                    renderSections();
                } else {
                    binarySearch(query);
                }
            }
        });

        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchControls.add(binarySearchField);

        JButton sortByPriorityDateButton = new JButton("By Priority and Date");
        sortByPriorityDateButton.addActionListener(e -> sortByPriorityAndDate());
        searchControls.add(sortByPriorityDateButton);

        searchPanel.add(searchControls, BorderLayout.NORTH);
        return searchPanel;
    }

    public void addSection(String newSection) {
        if (!sections.contains(newSection)) {
            sections.add(newSection);
            sectionTaskCount.put(newSection, 0);
            tasksBySection.putIfAbsent(newSection, new ArrayList<>());
            updateDropDown();
            renderSections();
        }
    }

    private void updateDropDown() {
        dropdown.removeAllItems();
        for (String s : sections) {
            dropdown.addItem(s);
        }
    }

    private void addTask() {
        String taskName = taskNameField.getText().trim();
        Date selectedDate = dateChooser.getDate();
        String selectedSection = (String) dropdown.getSelectedItem();
        String priority = (String) priorityDropdown.getSelectedItem();

        if (taskName.isEmpty() || selectedDate == null || selectedSection == null || priority == null) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        tasksBySection.computeIfAbsent(selectedSection, k -> new ArrayList<>()).add(new Task(taskName, priority, selectedDate));
        sectionTaskCount.put(selectedSection, sectionTaskCount.getOrDefault(selectedSection, 0) + 1);

        taskNameField.setText("");
        dateChooser.setDate(null);
        priorityDropdown.setSelectedIndex(0);
        renderSections();
    }

    private void renderSections() {
        taskListPanel.removeAll();
        allTasksList.clear();

        for (String section : sections) {
            JPanel sectionContainer = new JPanel();
            sectionContainer.setLayout(new BoxLayout(sectionContainer, BoxLayout.Y_AXIS));
            sectionContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel sectionHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
            sectionHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel sectionTitle = new JLabel("📁 " + section);
            sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
            sectionTitle.setBorder(new EmptyBorder(10, 5, 5, 5));

            JButton deleteSectionButton = new JButton("Delete Section");
            deleteSectionButton.addActionListener(e -> deleteSection(section));

            sectionHeader.add(sectionTitle);
            sectionHeader.add(deleteSectionButton);
            sectionContainer.add(sectionHeader);

            List<Task> tasks = tasksBySection.getOrDefault(section, new ArrayList<>());
            for (Task task : tasks) {
                allTasksList.add(task);
                JPanel taskPanel = createTaskPanel(section, task);
                taskPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                sectionContainer.add(taskPanel);
            }
            taskListPanel.add(sectionContainer);
            taskListPanel.add(Box.createVerticalStrut(5));
        }

        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    private JPanel createTaskPanel(String section, Task task) {
        JPanel taskPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        taskPanel.setBackground(Color.WHITE);
        taskPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel taskLabel = new JLabel("• " + task.toString());
        taskLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton deleteButton = new JButton("X");
        deleteButton.setMargin(new Insets(2, 6, 2, 6));
        deleteButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deleteButton.setFocusable(false);
        deleteButton.setBackground(Color.WHITE);
        deleteButton.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        deleteButton.setToolTipText("Delete task");
        deleteButton.addActionListener(e -> removeTask(section, task));

        taskPanel.add(taskLabel);
        taskPanel.add(deleteButton);
        return taskPanel;
    }

    private void removeTask(String section, Task task) {
        List<Task> tasks = tasksBySection.get(section);
        if (tasks != null && tasks.remove(task)) {
            sectionTaskCount.put(section, sectionTaskCount.get(section) - 1);
            saveAllTasks();
            renderSections();
        }
    }

    private void deleteSection(String sectionToDelete) {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete section '" + sectionToDelete + "' and all its tasks?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            sections.remove(sectionToDelete);
            tasksBySection.remove(sectionToDelete);
            sectionTaskCount.remove(sectionToDelete);
            saveAllTasks();
            updateDropDown();
            renderSections();
        }
    }

    private void saveAllTasks() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (String section : sections) {
                writer.println("[SECTION] " + section);
                List<Task> tasks = tasksBySection.getOrDefault(section, new ArrayList<>());
                for (Task task : tasks) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    writer.println(task.name + "," + task.priority + "," + sdf.format(task.dueDate));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTasks() {
        File file = new File(DATA_FILE);
        if (!file.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String currentSection = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("[SECTION] ")) {
                    currentSection = line.substring(10).trim();
                    addSection(currentSection);
                } else if (currentSection != null) {
                    String[] taskData = line.split(",");
                    if (taskData.length == 3) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            Date dueDate = sdf.parse(taskData[2]);
                            tasksBySection.computeIfAbsent(currentSection, k -> new ArrayList<>())
                                    .add(new Task(taskData[0], taskData[1], dueDate));
                        } catch (java.text.ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            renderSections();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void binarySearch(String query) {
        List<Task> sortedTasks = new ArrayList<>(allTasksList);
        Collections.sort(sortedTasks, Comparator.comparing(task -> task.name.toLowerCase()));

        taskListPanel.removeAll();
        for (String section : sections) {
            JPanel sectionContainer = new JPanel();
            sectionContainer.setLayout(new BoxLayout(sectionContainer, BoxLayout.Y_AXIS));
            sectionContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel sectionHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
            sectionHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel sectionTitle = new JLabel("📁 " + section);
            sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
            sectionTitle.setBorder(new EmptyBorder(10, 5, 5, 5));

            JButton deleteSectionButton = new JButton("Delete Section");
            deleteSectionButton.addActionListener(e -> deleteSection(section));

            sectionHeader.add(sectionTitle);
            sectionHeader.add(deleteSectionButton);
            sectionContainer.add(sectionHeader);

            for (Task task : sortedTasks) {
                if (tasksBySection.containsKey(section) && tasksBySection.get(section).contains(task) &&
                        task.name.toLowerCase().contains(query.toLowerCase())) {
                    JPanel taskPanel = createTaskPanel(section, task);
                    taskPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    sectionContainer.add(taskPanel);
                }
            }
            taskListPanel.add(sectionContainer);
            taskListPanel.add(Box.createVerticalStrut(5));
        }
        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    private void sortByPriorityAndDate() {
        List<Task> allTasks = new ArrayList<>();
        for (List<Task> taskList : tasksBySection.values()) {
            allTasks.addAll(taskList);
        }

        Collections.sort(allTasks, (task1, task2) -> {
            if (task1.dueDate != null && task2.dueDate != null) {
                int dateComparison = task1.dueDate.compareTo(task2.dueDate);
                if (dateComparison != 0) {
                    return dateComparison;
                }
            } else if (task1.dueDate == null && task2.dueDate != null) {
                return 1;
            } else if (task1.dueDate != null && task2.dueDate == null) {
                return -1;
            }
            return Integer.compare(getPriorityValue(task1.priority), getPriorityValue(task2.priority));
        });

        taskListPanel.removeAll();

        Map<String, List<Task>> sortedTasksBySection = new HashMap<>();
        for (String section : sections) {
            sortedTasksBySection.put(section, new ArrayList<>());
        }

        for (Task task : allTasks) {
            for (Map.Entry<String, List<Task>> entry : tasksBySection.entrySet()) {
                if (entry.getValue().contains(task)) {
                    sortedTasksBySection.get(entry.getKey()).add(task);
                    break;
                }
            }
        }

        for (String section : sections) {
            JPanel sectionContainer = new JPanel();
            sectionContainer.setLayout(new BoxLayout(sectionContainer, BoxLayout.Y_AXIS));
            sectionContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel sectionHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
            sectionHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel sectionTitle = new JLabel("📁 " + section);
            sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
            sectionTitle.setBorder(new EmptyBorder(10, 5, 5, 5));

            JButton deleteSectionButton = new JButton("Delete Section");
            deleteSectionButton.addActionListener(e -> deleteSection(section));

            sectionHeader.add(sectionTitle);
            sectionHeader.add(deleteSectionButton);
            sectionContainer.add(sectionHeader);

            List<Task> sortedTasksInCurrentSection = sortedTasksBySection.getOrDefault(section, new ArrayList<>());
            for (Task task : sortedTasksInCurrentSection) {
                JPanel taskPanel = createTaskPanel(section, task);
                taskPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                sectionContainer.add(taskPanel);
            }
            taskListPanel.add(sectionContainer);
            taskListPanel.add(Box.createVerticalStrut(5));
        }
        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    private int getPriorityValue(String priority) {
        switch (priority) {
            case "High":
                return 1;
            case "Moderate":
                return 2;
            case "Low":
                return 3;
            default:
                return 4;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUItry::new);
    }

    // Inner class for adding sections
    public static class FirstFrame extends JFrame {
        public FirstFrame(GUItry parent) {
            setTitle("Add Section");
            setSize(300, 150);
            setLayout(new FlowLayout());
            setLocationRelativeTo(parent);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JTextField sectionField = new JTextField(20);
            JButton addSectionBtn = new JButton("Add Section");

            addSectionBtn.addActionListener(e -> {
                String newSection = sectionField.getText().trim();
                if (!newSection.isEmpty()) {
                    parent.addSection(newSection);
                    dispose();
                }
            });

            add(sectionField);
            add(addSectionBtn);
        }
    }
}