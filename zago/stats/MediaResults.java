package stats;
public class MediaResults {

	private int minimum;
	private int maximum;
	private double media;
	
	public MediaResults()
	{
		minimum = Integer.MAX_VALUE;
		maximum = Integer.MIN_VALUE;
		media = Double.NaN;
	}
	
	public int getMinimum() {
		return minimum;
	}

	public void setMinimum(int minimum) {
		this.minimum = minimum;
	}

	public int getMaximum() {
		return maximum;
	}

	public void setMaximum(int maximum) {
		this.maximum = maximum;
	}

	public double getMedia() {
		return media;
	}

	public void setMedia(double media) {
		this.media = media;
	}
}
