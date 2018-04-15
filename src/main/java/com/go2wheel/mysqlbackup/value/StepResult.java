package com.go2wheel.mysqlbackup.value;

public class StepResult<T> {
	
	private boolean tsuduku;
	
	private T result;
	
	public static <T1>  StepResult<T1> tsudukuStepResult() {
		return new StepResult<>(true); 
	}
	
	public static <T1>  StepResult<T1> tsudukanaiStepResult() {
		return new StepResult<>(false); 
	}

	@Override
	public String toString() {
		return String.format("tsuduku: %s, result: %s", isTsuduku(), getResult());
	}
	
	public StepResult(boolean tsuduku) {
		this.setTsuduku(tsuduku);
	}
	
	public StepResult(T result, boolean tsuduku) {
		this.result = result;
		this.tsuduku = tsuduku;
	}

	public boolean isTsuduku() {
		return tsuduku;
	}

	public void setTsuduku(boolean tsuduku) {
		this.tsuduku = tsuduku;
	}

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}

}
