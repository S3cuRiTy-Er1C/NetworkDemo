package com.research.firewall.beans;


enum HeaderType {
	TCP(6),
	UDP(17),
	Other(255);
	private int type;
	private HeaderType(int pType){
		type = pType;
	}
	
	public final int getNumber(){
		return type;
	}
	
	public static HeaderType getHeaderType(int pType){
		if(pType == TCP.type){
			return TCP;
		}else if(pType == UDP.type){
			return UDP;
		}else{
			return Other;
		}
	}
}
