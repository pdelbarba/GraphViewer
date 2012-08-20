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
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.WindowConstants;

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

public class GraphViewer {

	Connection con;
	ArrayList<XYSeries> xyAL;

	public GraphViewer() {
		connect();
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
			//Pull list of labels from db
			ArrayList<String> labelList = new ArrayList<String>();
			String queryLabel = "SELECT label FROM EDFSignalHeader WHERE fileID = "
					+ fileID;
			Statement stLabel = con.createStatement();
			ResultSet rsLabel = stLabel.executeQuery(queryLabel);
			while (rsLabel.next()) {
				labelList.add(rsLabel.getString("Label"));
			}
	
			//pull list of data records from db
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
			
			createGraph();
		} catch (SQLException ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}

	private void createGraph() {
		JFrame frame = new JFrame("Chart");
		frame.getContentPane().setLayout(
				new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));

		for (int i = 0; i < xyAL.size(); i++) {
			XYSeriesCollection dataset = new XYSeriesCollection();
			dataset.addSeries(xyAL.get(i));
			JFreeChart chart1 = ChartFactory.createXYLineChart(xyAL.get(i)
					.getKey().toString(),
					"Seconds",
					"uV",//TODO set to dynamic unit
					dataset, PlotOrientation.VERTICAL,
					false,
					true,
					true);
			frame.add(new ChartPanel(chart1));
		}

		frame.pack();
		frame.setSize(800, 800);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
		new GraphViewer();

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