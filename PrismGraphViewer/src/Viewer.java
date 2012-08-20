import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class Viewer {
	
	public Viewer(ArrayList<XYSeries> xyAL) {
		createGraph(xyAL);
	}
	
	private void createGraph(ArrayList<XYSeries> xyAL) {
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

}
