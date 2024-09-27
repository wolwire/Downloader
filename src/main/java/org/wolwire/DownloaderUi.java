package org.wolwire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.wolwire.MultiThreadedDownloader;

public class DownloaderUi {

    public static void main(String[] args) {
        // Create a new JFrame (main window)
        JFrame frame = new JFrame("Simple UI Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 700);
        JPanel panel = new JPanel();
        frame.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Create a label
        JLabel label = new JLabel("Enter your name:");

        // Create a text field (input box)
        JTextField textField = new JTextField(20);

        // Create a button
        JButton button = new JButton("Submit");

        // Create an output label to display the result
        JLabel outputLabel = new JLabel("");

        // Add action listener to the button
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // When the button is clicked, update the output label
                String name = textField.getText();
                outputLabel.setText("Hello, " + name + "!");
            }
        });

        // Add components to the frame
        frame.add(label);
        frame.add(textField);
        frame.add(button);
        frame.add(outputLabel);

        // Make the window visible
        frame.setVisible(true);
    }
}