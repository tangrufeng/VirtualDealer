package org.tom.vd.bean;

/**
 * <p>Title: ProgessBean.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: jc-yt.com</p>
 * @author tang
 * @version 1.0
 *
 */
public class ProgessBean {

	private String timeline;
	
	private String status;
	
	private int bcolor;
	
	private int bnum;
	
	private int fcolor;
	
	private int fnum;
	
	private int nextDealer;
	
	private int nextColor;
	
	

	public int getNextDealer() {
		return nextDealer;
	}

	public void setNextDealer(int nextDealer) {
		this.nextDealer = nextDealer;
	}

	public int getNextColor() {
		return nextColor;
	}

	public void setNextColor(int nextColor) {
		this.nextColor = nextColor;
	}

	public String getTimeline() {
		return timeline;
	}

	public void setTimeline(String timeline) {
		this.timeline = timeline;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getBcolor() {
		return bcolor;
	}

	public void setBcolor(int bcolor) {
		this.bcolor = bcolor;
	}

	public int getBnum() {
		return bnum;
	}

	public void setBnum(int bnum) {
		this.bnum = bnum;
	}

	public int getFcolor() {
		return fcolor;
	}

	public void setFcolor(int fcolor) {
		this.fcolor = fcolor;
	}

	public int getFnum() {
		return fnum;
	}

	public void setFnum(int fnum) {
		this.fnum = fnum;
	}

	@Override
	public String toString() {
		return "ProgessBean [timeline=" + timeline + ", status=" + status
				+ ", bcolor=" + bcolor + ", bnum=" + bnum + ", fcolor="
				+ fcolor + ", fnum=" + fnum + "]";
	}
	
	
	

}
