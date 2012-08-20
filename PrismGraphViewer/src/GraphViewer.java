import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

public class GraphViewer extends JFrame {

	Connection con;
	ArrayList<XYSeries> xyAL;
	private JPanel contentPane;
	private JTextField txtUsername;
	private JPasswordField passwordField;

	public GraphViewer() {
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
			}
		});
		btnConnect.setBounds(55, 98, 114, 19);
		contentPane.add(btnConnect);
		
		JTextArea taProcess = new JTextArea();//edit with taProcess.setText(STRING);
		taProcess.setEditable(false);
		taProcess.setBounds(12, 129, 185, 129);
		contentPane.add(taProcess);
		
		JList listGraphs = new JList();
		listGraphs.setBounds(207, 9, 147, 308);
		contentPane.add(listGraphs);
		
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
		spinMindr.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		spinMindr.setBounds(12, 329, 70, 20);
		contentPane.add(spinMindr);
		
		JSpinner spinMaxdr = new JSpinner();
		spinMaxdr.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
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
			}
		});
		btnShowGraph.setBounds(217, 326, 137, 25);
		contentPane.add(btnShowGraph);
	}

	private void connect() {
		xyAL = new ArrayList<XYSeries>();

		String username = JOptionPane.showInputDialog("Username:");
		if (username == null)
			System.exit(0);

		JPasswordField password = new JPasswordField(10);
		int action = JOptionPane.showConfirmDialog(null, password,
				"Password:",
				JOptionPane.OK_CANCEL_OPTION);
		if (action < 0)
			System.exit(0);

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			String url = "jdbc:mysql://localhost/PRISMData";
			con = DriverManager.getConnection(url, username, new String(
			password.getPassword()));
			System.out.println("Connected to database: " + con.toString() + " Running query");
			pullData();
			con.close();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	private void pullData() {
		String fileID = JOptionPane.showInputDialog("File number:");
		if (fileID == null)
			System.exit(0);
		try {
			
			//Pull list of labels from db*********************************
			ArrayList<String> labelList = new ArrayList<String>();
			String queryLabel = "SELECT label FROM EDFSignalHeader WHERE fileID = "
					+ fileID;
			Statement stLabel = con.createStatement();
			ResultSet rsLabel = stLabel.executeQuery(queryLabel);
			while (rsLabel.next()) {
				labelList.add(rsLabel.getString("Label"));
			}
	
			//pull list of data records from db***************************
			String queryData = "SELECT datarecord FROM EDFDataRecord WHERE fileID = "
					+ fileID;
			
			Statement stData = con.createStatement();
			ResultSet rsData = stData.executeQuery(queryData);
			
			System.out.println("Exectued query, pulling data");
			
			for(int i = 0; rsData.next() && i < 5; i++) {//TODO remove limit after testing
				Blob blob = rsData.getBlob("datarecord");
				byte[] n = blob.getBytes(1, (int) blob.length());
				int[] intarr = toIntArray(n);
				XYSeries series = new XYSeries(labelList.get(i));

				float t = 0;
				for (int j = 0; j < intarr.length && j < 600; j++, t += 0.1f) {//TODO remove limit after testing
					series.add(t, intarr[j]);
				}

				xyAL.add(series);
			}
			
			System.out.println("Data pulled, creating graph");
			
			new Viewer(xyAL);
		} catch (SQLException ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}


	private int[] toIntArray(byte[] barr) {
		// Pad the size to multiple of 2
		int size = (barr.length / 2) + ((barr.length % 2 == 0) ? 0 : 1);

		ByteBuffer bb = ByteBuffer.allocate(size * 2);
		bb.put(barr);

		bb.order(ByteOrder.LITTLE_ENDIAN);

		int[] result = new int[size];
		bb.rewind();
		while (bb.remaining() > 0) {
			result[bb.position() / 2] = (int) bb.getShort();
		}

		return result;
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
		//TODO chart formatting
//		 chart1.setBackgroundPaint(Color.white); // Setting the plot
//		 properties
//		 XYPlot plot = (XYPlot) chart.getPlot();
//		 plot.setBackgroundPaint(Color.lightGray);
//		 plot.setDomainGridlinePaint(Color.white);
//		 plot.setRangeGridlinePaint(Color.white); plot.setAxisOffset(new
//		 RectangleInsets(5.0, 5.0, 5.0, 5.0));
//		 plot.setDomainCrosshairVisible(true);
//		 plot.setRangeCrosshairVisible(true);
//		
//		 DateAxis axis = (DateAxis) plot.getDomainAxis();
//		 axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));

	}
}