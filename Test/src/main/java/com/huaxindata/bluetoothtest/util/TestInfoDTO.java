package com.huaxindata.bluetoothtest.util;

public class TestInfoDTO {

	public String vin;
	public long time;
	public int statespeack;
	public int statelisten;
	public int upload;
	public boolean choose;
	public int show;

	public int getShow() {
		return show;
	}

	public void setShow(int show) {
		this.show = show;
	}

	public boolean isChoose() {
		return choose;
	}

	public void setChoose(boolean choose) {
		this.choose = choose;
	}

	public TestInfoDTO() {
		super();
	}

	public TestInfoDTO(String vin, long time, int statespeack, int statelisten,
			int upload, int show) {
		super();
		this.vin = vin;
		this.time = time;
		this.statespeack = statespeack;
		this.statelisten = statelisten;
		this.upload = upload;
		this.show = show;
	}

	public String getVin() {
		return vin;
	}

	public void setVin(String vin) {
		this.vin = vin;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getStatespeack() {
		return statespeack;
	}

	public void setStatespeack(int statespeack) {
		this.statespeack = statespeack;
	}

	public int getStatelisten() {
		return statelisten;
	}

	public void setStatelisten(int statelisten) {
		this.statelisten = statelisten;
	}

	public int getUpload() {
		return upload;
	}

	public void setUpload(int upload) {
		this.upload = upload;
	}

}
