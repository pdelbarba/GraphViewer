import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

import org.jfree.data.xy.XYSeries;

public class DBData {

	private DBData() {
	}

	public static ArrayList<String> pullString(String column, String query,
			Connection con) throws SQLException {
		ArrayList<String> stringList = new ArrayList<String>();
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(query);
		while (rs.next()) {
			stringList.add(rs.getString(column));
		}
		return stringList;
	}

	// TODO finish these
	public static ArrayList<Integer> pullInt(String column, String query,
			Connection con) {
		return null;
	}

	public static ArrayList<XYSeries> pullSeries(String column, String query, Connection con, ArrayList<String> labelList, int start, int end) throws SQLException {
		ArrayList<XYSeries> out = new ArrayList<XYSeries>();
		Statement stData = con.createStatement();
		ResultSet rsData = stData.executeQuery(query);
		
		for(int i = 0; rsData.next() && i < 5; i++) {//TODO remove limit after testing
			Blob blob = rsData.getBlob("datarecord");
			byte[] n = blob.getBytes(1, (int) blob.length());
			int[] intarr = toIntArray(n);
			XYSeries series = new XYSeries(labelList.get(i));

			float t = 0;
			for (int j = 0; j < intarr.length && j < 600; j++, t += 0.1f) {//TODO remove limit after testing
				series.add(t, intarr[j]);
			}

			out.add(series);
		}
		return out;
	}
	
	private static int[] toIntArray(byte[] barr) {
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

	// some simple conversion utility methods
	static ArrayList<String> toArrayList(Vector<String> v) {
		return new ArrayList<String>(v);

	}

	static Vector<String> toVector(ArrayList<String> al) {
		return new Vector<String>(al);

	}
}