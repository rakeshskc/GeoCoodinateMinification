package com.geo.main;

import java.text.DecimalFormat;

public class MainGridSpatilIndex {
	private static final DecimalFormat formatter = new DecimalFormat(
			"0.00000000");

	public static final BoxWithId WORLD = new BoxWithId(0, 0, -85.0, -180.0,
			85.0, 180.0);
	public static final int TOTAL_BYTES = 6;
	private static final double[] TEST_POINTS = { -89.45, 12.89,
			-80.13933769598077, 26.09463685175853, -80.1192030100672,
			25.987245956376018, -86.2658, 12.1262, -122.801094, 49.104431,
			-79.508278, 43.837208, -81.175369, 42.778828, -122.793205,
			49.283764, -78.942932, 43.897545, -123.133568, 49.166592,
			-52.71283, 47.560539, -77.110886, 45.826653, -97.138451, 49.895077,
			-113.873505, 50.584435, -71.24646, 46.738228, -66.646332,
			45.964993, -73.510712, 45.536945, -117.295425, 33.039139,
			-77.796768, 48.098709, -79.337021, 43.856098, -122.968941,
			49.267132, -79.690331, 44.389355, -66.067657, 45.273918,
			-79.817734, 43.328674, -123.940063, 49.165882, -79.246864,
			43.159374, -104.93351, 39.813019, -111.375961, 56.732014,
			-79.341667, 43.6912, -82.19104, 42.404804, -119.276505, 50.27179,
			-79.640579, 43.59531, -123.329773, 48.407326, -52.71283, 47.560539,
			-106.647034, 52.146973, -119.589615, 49.489536, -63.582687,
			44.65107, -79.76667, 43.683334, -113.323975, 53.631611, -79.891205,
			43.526646, -75.69883, 45.427944, -79.843826, 43.255203, -83.026772,
			42.317432, -71.242798, 46.803284, -79.248253, 42.992157,
			-121.951469, 49.15794, -113.811241, 52.268112, -81.010002,
			46.490002, -64.778229, 46.087818, -79.419678, 44.608246,
			-79.428406, 43.887501, -119.477829, 49.882114, -71.888351,
			45.404476, -79.252075, 42.891411 };

	public static void main(String[] args) {
		for (int i = 0; i < TEST_POINTS.length; i += 2) {
			BoxCenter p1 = new BoxCenter(TEST_POINTS[i], TEST_POINTS[i + 1]);
			double[] list = encode(p1);
			for (int j = 0; j < list.length; j += 2) {

				System.out.println(list[j] + "\t" + list[j + 1]);

			}
			BoxCenter p2 = decode(list);
			double loss = Math.sqrt((p2.lon - p1.lon) * (p2.lon - p1.lon)
					+ (p2.lat - p1.lat) * (p2.lat - p1.lat));
			System.out.println(p2.lat + "\t" + p2.lon);
			System.out.println("loss[" + (i / 2) + "]: \t "
					+ formatter.format(loss));
			// break;
		}

	}

	private static String getBinaryString(double[] list) {
		String binStr = "";
		for (int j = 0; j < list.length; j += 2) {
			short sx = (short) (list[j] < 0 ? 256 + list[j] : list[j]);
			short sy = (short) (list[j + 1] < 0 ? 256 + list[j + 1]
					: list[j + 1]);
			binStr += String.format("%08d",
					Integer.parseInt(Integer.toBinaryString(sx)))
					+ String.format("%08d",
							Integer.parseInt(Integer.toBinaryString(sy)));
		}
		return binStr;
	}

	private static BoxCenter decode(double[] list) {
		BoxWithId box = WORLD;

		for (int i = 0; i < list.length; i += 2) {
			final double lonID = list[i];
			final double latID = list[i + 1];

			box = _getSubBoxById(box, lonID, latID);

		} // for i

		return box.center();
	}

	private static BoxWithId _getSubBoxById(BoxWithId box, double lonID,
			double latID) {
		BoxWithId newbox = new BoxWithId();
		newbox.lon1 = box.lon1 + (box.cellWidth() * lonID);
		newbox.lon2 = newbox.lon1 + box.cellWidth();
		newbox.lat1 = box.lat1 + (box.cellHeight() * latID);
		newbox.lat2 = newbox.lat1 + box.cellHeight();
		return newbox;
	}

	private static double[] encode(BoxCenter pt) {
		BoxWithId box1 = WORLD;
		double[] list = new double[TOTAL_BYTES];
		for (int i = 0; i < TOTAL_BYTES; i += 2) {
			box1 = _getSubBoxByCoord(box1, pt);
			list[i] = box1.lonID;
			list[i + 1] = box1.latID;
		}

		System.out.println(list.length);

		return list;
	}

	private static BoxWithId _getSubBoxByCoord(BoxWithId box, BoxCenter pt) {
		BoxWithId newbox = new BoxWithId();

		double lonID = Math.floor((pt.lon - box.lon1) / box.cellWidth());
		double latID = Math.floor((pt.lat - box.lat1) / box.cellHeight());

		newbox.lon1 = box.lon1 + (box.cellWidth() * lonID);
		newbox.lon2 = newbox.lon1 + box.cellWidth();

		newbox.lat1 = box.lat1 + (box.cellHeight() * latID);
		newbox.lat2 = newbox.lat1 + box.cellHeight();

		newbox.lonID = lonID;
		newbox.latID = latID;
		return newbox;
	}

}

class BoxCenter {
	public BoxCenter(double x, double y) {
		this.lon = x;
		this.lat = y;

	}

	@Override
	public String toString() {
		return "(" + lon + "," + lat + ")";
	}

	double lon;
	double lat;

}

class BoxWithId {
	private static final int SPLIT = 127;

	@Override
	public String toString() {
		return lonID + " , " + latID + " \t (" + lon1 + ", " + lat1 + ", "
				+ lon2 + ", " + lat2 + ")";
	}

	public BoxCenter center() {
		BoxCenter pt = new BoxCenter(lon1 + ((lon2 - lon1) / 2), lat1
				+ ((lat2 - lat1) / 2));
		return pt;
	}

	public BoxWithId(double latId, double lonId, double lon1, double lat1,
			double lon2, double lat2) {
		this.lonID = lonId;
		this.latID = latId;

		this.lon1 = lon1;
		this.lon2 = lon2;
		this.lat1 = lat1;
		this.lat2 = lat2;
	}

	public BoxWithId() {
	}

	double latID;
	double lonID;
	double lat1;
	double lat2;
	double lon1;
	double lon2;

	public double cellWidth() {
		double i = (lon2 - lon1) / SPLIT;
		return i < 0 ? 0 : i;
	}

	public double cellHeight() {
		double i = (lat2 - lat1) / SPLIT;
		return i < 0 ? 0 : i;
	}

}