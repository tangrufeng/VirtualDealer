package com.effecia.mina;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class MinaCommandPo  implements Serializable{
	private static final long serialVersionUID = 7067659888750837056L;
	private String requestType;
	private String moduleType = "SITE";
	private String gameType;
	private String commandType;
	private Map<String, Object> parameter = new HashMap<String, Object>();
	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public String getModuleType() {
		return moduleType;
	}

	public void setModuleType(String moduleType) {
		this.moduleType = moduleType;
	}


	public MinaCommandPo(){
		
	}
	
	public MinaCommandPo(String commandType){
		this.commandType = commandType;
	}
	
	public String getCommandType() {
		return commandType;
	}

	public void setCommandType(String commandType) {
		this.commandType = commandType;
	}

	public String getGameType() {
		return gameType;
	}

	public void setGameType(String gameType) {
		this.gameType = gameType;
	}

	public Map<String, Object> getParameter() {
		return parameter;
	}

	public void setParameter(Map<String, Object> parameters) {
		this.parameter = parameters;
	}

	@Override
	public String toString() {
		return "MinaCommandPo [requestType=" + requestType + ", moduleType="
				+ moduleType + ", gameType=" + gameType + ", commandType="
				+ commandType + ", parameter=" + parameter + "]";
	}
	
	
}
