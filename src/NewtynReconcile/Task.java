package NewtynReconcile;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Task extends JPanel implements ActionListener{
	private static final long serialVersionUID = -4696785506456780016L;
	
	private Function function;
	private String name;
	private JLabel nameLabel;
	private JButton launchButton;
	private boolean launched;
	
	public Task(final Function function) {
		super();
		this.launched = false;
		this.setLayout(new BorderLayout());
		this.function = function;
		this.name = function.getFunctionName();
		
		this.nameLabel = new JLabel();
		this.nameLabel.setText(name);
		add(this.nameLabel,BorderLayout.NORTH);
		
		this.launchButton = new JButton("Launch");
		this.launchButton.addActionListener(this);
		add(this.launchButton,BorderLayout.SOUTH);
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (!launched) {
			function.launch(this);
			launched = true;
		}
	}
	public void signalClose() {
		launched = false;
	}
	public void endTask() {
		function.close();
	}
}
