import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class MedicationTracker extends JFrame {
    private JTextField medicineNameField;
    private JTextField dosageField;
    private JSpinner hourSpinner;
    private JSpinner minuteSpinner;
    private JComboBox<String> amPmCombo;
    private JComboBox<String> frequencyCombo;
    private JPanel medicationsPanel;
    private JLabel nextReminderLabel;
    private List<Medication> medications;
    private javax.swing.Timer reminderTimer;
    private JScrollPane scrollPane;
    private Medication currentlyEditingMedication = null;
    private JButton addButton;

    public MedicationTracker() {
        medications = new ArrayList<>();
        setupUI();
        startReminderTimer();
    }

    private void setupUI() {
        setTitle("Medication Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 800);
        setLocationRelativeTo(null);

        // Main panel using BorderLayout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Input form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Medication"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Set larger font for all form components
        Font formFont = new Font("SansSerif", Font.PLAIN, 16);
        Insets fieldInsets = new Insets(5, 5, 5, 5);

        // Medicine Name
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Medicine Name:");
        nameLabel.setFont(formFont);
        formPanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        medicineNameField = new JTextField(20);
        medicineNameField.setFont(formFont);
        medicineNameField.setPreferredSize(new Dimension(220, 32));
        formPanel.add(medicineNameField, gbc);

        // Dosage
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel dosageLabel = new JLabel("Dosage:");
        dosageLabel.setFont(formFont);
        formPanel.add(dosageLabel, gbc);
        gbc.gridx = 1;
        JPanel dosagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        String[] dosagePresets = {"1 Tablet", "2 Tablets", "5 ml", "10 ml", "Custom"};
        JComboBox<String> dosageCombo = new JComboBox<>(dosagePresets);
        dosageCombo.setFont(formFont);
        dosageCombo.setPreferredSize(new Dimension(120, 32));
        dosageField = new JTextField(15);
        dosageField.setFont(formFont);
        dosageField.setPreferredSize(new Dimension(120, 32));
        dosageField.setVisible(false);
        dosageCombo.addActionListener(e -> {
            if ("Custom".equals(dosageCombo.getSelectedItem())) {
                dosageField.setVisible(true);
            } else {
                dosageField.setVisible(false);
            }
            dosagePanel.revalidate();
            dosagePanel.repaint();
        });
        dosagePanel.add(dosageCombo);
        dosagePanel.add(Box.createHorizontalStrut(5));
        dosagePanel.add(dosageField);
        formPanel.add(dosagePanel, gbc);

        // Time
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel timeLabel = new JLabel("Time:");
        timeLabel.setFont(formFont);
        formPanel.add(timeLabel, gbc);
        gbc.gridx = 1;
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        SpinnerNumberModel hourModel = new SpinnerNumberModel(8, 1, 12, 1);
        SpinnerNumberModel minuteModel = new SpinnerNumberModel(0, 0, 59, 1);
        hourSpinner = new JSpinner(hourModel);
        minuteSpinner = new JSpinner(minuteModel);
        hourSpinner.setFont(formFont);
        minuteSpinner.setFont(formFont);
        ((JSpinner.DefaultEditor) hourSpinner.getEditor()).getTextField().setFont(formFont);
        ((JSpinner.DefaultEditor) minuteSpinner.getEditor()).getTextField().setFont(formFont);
        hourSpinner.setPreferredSize(new Dimension(50, 32));
        minuteSpinner.setPreferredSize(new Dimension(50, 32));
        String[] amPm = {"AM", "PM"};
        amPmCombo = new JComboBox<>(amPm);
        amPmCombo.setFont(formFont);
        amPmCombo.setPreferredSize(new Dimension(70, 32));
        timePanel.add(hourSpinner);
        timePanel.add(new JLabel(":"));
        timePanel.add(minuteSpinner);
        timePanel.add(Box.createHorizontalStrut(5));
        timePanel.add(amPmCombo);
        formPanel.add(timePanel, gbc);

        // Frequency
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel freqLabel = new JLabel("Frequency:");
        freqLabel.setFont(formFont);
        formPanel.add(freqLabel, gbc);
        gbc.gridx = 1;
        String[] frequencies = {"Daily", "Alternate Days", "Weekly", "Every 6 Hours", "Every 8 Hours", "One-time", "Custom"};
        frequencyCombo = new JComboBox<>(frequencies);
        frequencyCombo.setFont(formFont);
        frequencyCombo.setPreferredSize(new Dimension(220, 32));
        formPanel.add(frequencyCombo, gbc);

        // Add Button
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        addButton = new JButton("Add Medication");
        addButton.setFont(formFont.deriveFont(Font.BOLD, 18f));
        addButton.setPreferredSize(new Dimension(350, 38));
        addButton.addActionListener(e -> addMedication());
        formPanel.add(addButton, gbc);

        // Add a small rigid area below the button to prevent rendering issues
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)), gbc);

        // Adjust alignment for form panel to align to top and not stretch vertically
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        formPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, formPanel.getPreferredSize().height)); // Constrain vertical growth

        // Panel to hold form and reminder label
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(formPanel);
        topPanel.add(Box.createVerticalStrut(10));

        // Next Reminder Label
        nextReminderLabel = new JLabel("Next Reminder: None");
        nextReminderLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        nextReminderLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        topPanel.add(nextReminderLabel);

        // Medications Panel
        medicationsPanel = new JPanel();
        medicationsPanel.setLayout(new BoxLayout(medicationsPanel, BoxLayout.Y_AXIS));
        medicationsPanel.setBorder(BorderFactory.createTitledBorder("Added Medications"));
        medicationsPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // Ensure panel aligns left to take available width

        scrollPane = new JScrollPane(medicationsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Add components to main panel using BorderLayout
        mainPanel.add(topPanel, BorderLayout.PAGE_START);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void addMedication() {
        String name = medicineNameField.getText().trim();
        String dosage;
        JComboBox<String> dosageCombo = (JComboBox<String>) ((JPanel) dosageField.getParent()).getComponent(0);
        if ("Custom".equals(dosageCombo.getSelectedItem())) {
            dosage = dosageField.getText().trim();
        } else {
            dosage = (String) dosageCombo.getSelectedItem();
        }
        
        if (name.isEmpty() || dosage.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields");
            return;
        }

        int hour = (int) hourSpinner.getValue();
        int minute = (int) minuteSpinner.getValue();
        String amPm = (String) amPmCombo.getSelectedItem();
        String frequency = (String) frequencyCombo.getSelectedItem();

        if (currentlyEditingMedication == null) {
            // Add new medication
            Medication medication = new Medication(name, dosage, hour, minute, amPm, frequency);
            medications.add(medication);
        } else {
            // Save changes to existing medication
            currentlyEditingMedication.setName(name);
            currentlyEditingMedication.setDosage(dosage);
            currentlyEditingMedication.setHour(hour);
            currentlyEditingMedication.setMinute(minute);
            currentlyEditingMedication.setAmPm(amPm);
            currentlyEditingMedication.setFrequency(frequency);
            currentlyEditingMedication = null; // Clear editing state
            addButton.setText("Add Medication"); // Reset button text
        }

        updateMedicationsPanel();
        updateNextReminder();
        clearForm();
    }

    private void updateMedicationsPanel() {
        medicationsPanel.removeAll();
        
        for (Medication med : medications) {
            JPanel card = createMedicationCard(med);
            medicationsPanel.add(card);
            medicationsPanel.add(Box.createVerticalStrut(10));
        }
        
        medicationsPanel.revalidate();
        medicationsPanel.repaint();
    }

    private JPanel createMedicationCard(Medication med) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        infoPanel.add(new JLabel("Medicine: " + med.getName()));
        infoPanel.add(new JLabel("Dosage: " + med.getDosage()));
        infoPanel.add(new JLabel("Time: " + med.getTimeString()));
        infoPanel.add(new JLabel("Frequency: " + med.getFrequency()));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");

        editButton.addActionListener(e -> editMedication(med));
        deleteButton.addActionListener(e -> deleteMedication(med));

        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);

        // Set fixed size for the card height and allow horizontal stretching
        card.setPreferredSize(new Dimension(card.getPreferredSize().width, 120));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setMinimumSize(new Dimension(card.getPreferredSize().width, 120));

        // Make the card fill the width in the BoxLayout
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        return card;
    }

    private void editMedication(Medication med) {
        currentlyEditingMedication = med;

        // Populate form fields with medication data
        medicineNameField.setText(med.getName());
        
        // Handle dosage - need to check if it's a preset or custom
        JComboBox<String> dosageCombo = (JComboBox<String>) ((JPanel) dosageField.getParent()).getComponent(0);
        boolean isPreset = false;
        for (int i = 0; i < dosageCombo.getItemCount(); i++) {
            if (med.getDosage().equals(dosageCombo.getItemAt(i))) {
                dosageCombo.setSelectedItem(med.getDosage());
                dosageField.setVisible(false);
                isPreset = true;
                break;
            }
        }
        if (!isPreset) {
            dosageCombo.setSelectedItem("Custom");
            dosageField.setText(med.getDosage());
            dosageField.setVisible(true);
        }
        ((JPanel) dosageField.getParent()).revalidate();
        ((JPanel) dosageField.getParent()).repaint();
        
        // Handle time
        LocalTime medTime = med.getLocalTime();
        int hour = medTime.getHour();
        String amPm = "AM";
        if (hour >= 12) {
            amPm = "PM";
            if (hour > 12) hour -= 12;
        }
        if (hour == 0) hour = 12; // 00:xx is 12 AM
        hourSpinner.setValue(hour);
        minuteSpinner.setValue(medTime.getMinute());
        amPmCombo.setSelectedItem(amPm);

        // Handle frequency
        frequencyCombo.setSelectedItem(med.getFrequency());

        // Change button text
        addButton.setText("Save Changes");
    }

    private void deleteMedication(Medication med) {
        medications.remove(med);
        updateMedicationsPanel();
        updateNextReminder();
    }

    private void clearForm() {
        medicineNameField.setText("");
        // Reset dosage fields
        JComboBox<String> dosageCombo = (JComboBox<String>) ((JPanel) dosageField.getParent()).getComponent(0);
        dosageCombo.setSelectedItem(dosageCombo.getItemAt(0)); // Select first preset
        dosageField.setText("");
        dosageField.setVisible(false);
        ((JPanel) dosageField.getParent()).revalidate();
        ((JPanel) dosageField.getParent()).repaint();
        
        hourSpinner.setValue(8);
        minuteSpinner.setValue(0);
        amPmCombo.setSelectedItem("AM");
        frequencyCombo.setSelectedItem("Daily");
        currentlyEditingMedication = null; // Ensure editing state is null
        addButton.setText("Add Medication"); // Ensure button text is reset
    }

    private void updateNextReminder() {
        if (medications.isEmpty()) {
            nextReminderLabel.setText("Next Reminder: None");
            return;
        }

        LocalTime now = LocalTime.now();
        Medication nextMed = null;
        LocalTime nextTime = null;

        for (Medication med : medications) {
            LocalTime medTime = med.getLocalTime();
            if (medTime.isAfter(now)) {
                if (nextTime == null || medTime.isBefore(nextTime)) {
                    nextTime = medTime;
                    nextMed = med;
                }
            }
        }

        if (nextMed != null) {
            nextReminderLabel.setText("Next Reminder: " + nextMed.getName() + " at " + nextMed.getTimeString());
        } else {
            nextReminderLabel.setText("Next Reminder: None for today");
        }
    }

    private void startReminderTimer() {
        reminderTimer = new javax.swing.Timer(60000, e -> updateNextReminder());
        reminderTimer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MedicationTracker().setVisible(true);
        });
    }
}

class Medication {
    private String name;
    private String dosage;
    private int hour;
    private int minute;
    private String amPm;
    private String frequency;

    public Medication(String name, String dosage, int hour, int minute, String amPm, String frequency) {
        this.name = name;
        this.dosage = dosage;
        this.hour = hour;
        this.minute = minute;
        this.amPm = amPm;
        this.frequency = frequency;
    }

    public void setName(String name) { this.name = name; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public void setHour(int hour) { this.hour = hour; }
    public void setMinute(int minute) { this.minute = minute; }
    public void setAmPm(String amPm) { this.amPm = amPm; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getName() { return name; }
    public String getDosage() { return dosage; }
    public String getTimeString() {
        return String.format("%02d:%02d %s", hour, minute, amPm);
    }
    public String getFrequency() { return frequency; }

    public LocalTime getLocalTime() {
        int hour24 = hour;
        if (amPm.equals("PM") && hour != 12) hour24 += 12;
        if (amPm.equals("AM") && hour == 12) hour24 = 0;
        return LocalTime.of(hour24, minute);
    }
} 