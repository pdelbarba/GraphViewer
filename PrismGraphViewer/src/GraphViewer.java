import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.data.xy.XYSeries;

@SuppressWarnings("serial")
public class GraphViewer extends JFrame {

	private Connection con;
	private Vector<String> labelList;
	private ArrayList<XYSeries> xyAL;
	private JPanel contentPane;
	private JTextField txtUsername;
	private JPasswordField passwordField;
	private JList<String> listGraphs;
	private JTextArea taProcess;
	private String taProcessTxt = "";

	public GraphViewer() {
		labelList = new Vector<String>();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 370, 390);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblUsername = new JLabel("Login");
		lblUsername.setBounds(12, 12, 92, 15);
		contentPane.add(lblUsername);

		txtUsername = new JTextField();
		txtUsername.setBounds(55, 39, 114, 19);
		contentPane.add(txtUsername);
		txtUsername.setColumns(10);

		passwordField = new JPasswordField();
		passwordField.setBounds(55, 67, 114, 19);
		contentPane.add(passwordField);

		JLabel lblUser = new JLabel("User:");
		lblUser.setBounds(12, 41, 70, 15);
		contentPane.add(lblUser);

		JLabel lblPass = new JLabel("Pass:");
		lblPass.setBounds(12, 69, 70, 15);
		contentPane.add(lblPass);

		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (txtUsername.getText() != null) {
					printToStatus("Connecting...");
					if (connect()) {
						pullData();
					} else {
					}// TODO what if connect fails?
				}
			}
		});
		btnConnect.setBounds(55, 98, 114, 19);
		contentPane.add(btnConnect);

		taProcess = new JTextArea();
		taProcess.setEditable(false);
		taProcess.setAutoscrolls(true);
		taProcess.setLineWrap(true);
		JScrollPane sptaProcess = new JScrollPane(taProcess);// enables scrolling
		sptaProcess.setBounds(12, 129, 185, 129);
		contentPane.add(sptaProcess);

		listGraphs = new JList<String>(labelList);
		JScrollPane splistGraphs = new JScrollPane(listGraphs);
		splistGraphs.setBounds(207, 9, 147, 308);
		contentPane.add(splistGraphs);

		JSpinner spinFile = new JSpinner();
		spinFile.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
			}
		});
		spinFile.setModel(new SpinnerNumberModel(1, 1, 62, 1));
		spinFile.setBounds(69, 270, 49, 20);
		contentPane.add(spinFile);

		JLabel lblFileNumber = new JLabel("File #:");
		lblFileNumber.setBounds(12, 270, 70, 15);
		contentPane.add(lblFileNumber);

		JLabel lblDataRange = new JLabel("Data range:");
		lblDataRange.setBounds(12, 302, 97, 15);
		contentPane.add(lblDataRange);

		JSpinner spinMindr = new JSpinner();
		spinMindr.setModel(new SpinnerNumberModel(new Integer(0),
				new Integer(0), null, new Integer(1)));
		spinMindr.setBounds(12, 329, 70, 20);
		contentPane.add(spinMindr);

		JSpinner spinMaxdr = new JSpinner();
		spinMaxdr.setModel(new SpinnerNumberModel(new Integer(1),
				new Integer(1), null, new Integer(1)));
		spinMaxdr.setBounds(109, 329, 70, 20);
		contentPane.add(spinMaxdr);

		JLabel lblTo = new JLabel("to");
		lblTo.setBounds(87, 329, 70, 15);
		contentPane.add(lblTo);

		JLabel lblS = new JLabel("s");
		lblS.setBounds(185, 329, 27, 15);
		contentPane.add(lblS);

		JButton btnShowGraph = new JButton("Show Graph...");
		btnShowGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Viewer(xyAL);
			}
		});
		btnShowGraph.setBounds(217, 326, 137, 25);
		contentPane.add(btnShowGraph);
	}

	private boolean connect() {
		xyAL = new ArrayList<XYSeries>();

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			String url = "jdbc:mysql://localhost/PRISMData";
			con = DriverManager.getConnection(url, txtUsername.getText(),
					new String(passwordField.getPassword()));
			printToStatus("Connected to database, Running query");

			return true;
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
			printToStatus("ERROR: " + ex.toString());
			return false;
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
			printToStatus("ERROR: " + ex.toString());
			return false;
		} catch (InstantiationException ex) {
			ex.printStackTrace();
			printToStatus("ERROR: " + ex.toString());
			return false;
		} catch (SQLException ex) {
			ex.printStackTrace();
			printToStatus("ERROR: " + ex.toString());
			return false;
		}
	}

	private void printToStatus(String txt) {
		taProcessTxt += txt + "\n";
		taProcess.setText(taProcessTxt);
		taProcess.updateUI();
	}

	private void pullData() {
		String fileID = JOptionPane.showInputDialog("File number:");
		if (fileID == null)
			System.exit(0);
		try {

			// Pull list of labels from db*********************************
			String queryLabel = "SELECT label FROM EDFSignalHeader WHERE fileID = "
					+ fileID;
			labelList = DBData.toVector(DBData.pullString("label", queryLabel,
					con));
			
			//printToStatus("Exectued query, pulling data");

			// pull list of data records from db***************************
			String queryData = "SELECT datarecord FROM EDFDataRecord WHERE fileID = "
					+ fileID;
			xyAL = DBData.pullSeries("datarecord", queryData, con,
					DBData.toArrayList(labelList), 0, 5);

			con.close();
			printToStatus("Data pulled, creating graph");
			new Viewer(xyAL);

		} catch (SQLException ex) {
			ex.printStackTrace();
			printToStatus("ERROR: " + ex.toString());
		}
	}

	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GraphViewer frame = new GraphViewer();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		// TODO chart formatting
		// chart1.setBackgroundPaint(Color.white); // Setting the plot
		// properties
		// XYPlot plot = (XYPlot) chart.getPlot();
		// plot.setBackgroundPaint(Color.lightGray);
		// plot.setDomainGridlinePaint(Color.white);
		// plot.setRangeGridlinePaint(Color.white); plot.setAxisOffset(new
		// RectangleInsets(5.0, 5.0, 5.0, 5.0));
		// plot.setDomainCrosshairVisible(true);
		// plot.setRangeCrosshairVisible(true);
		//
		// DateAxis axis = (DateAxis) plot.getDomainAxis();
		// axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));

	}
}