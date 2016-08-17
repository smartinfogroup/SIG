package com.cmex.tool;

import java.io.File;

import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.workitem.client.IWorkItemClient;

public class RtcConfigValueHolder {

private String rtcUrl;
private String userName;
private String password;
private String projectArea;
private String projectAreaFlag;

public String getProjectAreaFlag() {
	return projectAreaFlag;
}
public void setProjectAreaFlag(String projectAreaFlag) {
	this.projectAreaFlag = projectAreaFlag;
}
private ITeamRepository rtcRepository;
public ITeamRepository getRtcRepository() {
	return rtcRepository;
}
public void setRtcRepository(ITeamRepository rtcRepository) {
	this.rtcRepository = rtcRepository;
}
private File csvFile;

public File getCsvFile() {
	return csvFile;
}
public void setCsvFile(File csvFile) {
	this.csvFile = csvFile;
}
private IProjectArea pojectAreaInstance;
public IProjectArea getPojectAreaInstance() {
	return pojectAreaInstance;
}
public void setPojectAreaInstance(IProjectArea pojectAreaInstance) {
	this.pojectAreaInstance = pojectAreaInstance;
}
private IWorkItemClient  rtcWIClient;
public IWorkItemClient getRtcWIClient() {
	return rtcWIClient;
}
public void setRtcWIClient(IWorkItemClient rtcWIClient) {
	this.rtcWIClient = rtcWIClient;
}
public String getRtcUrl() {
	return rtcUrl;
}
public void setRtcUrl(String rtcUrl) {
	this.rtcUrl = rtcUrl;
}
public String getUserName() {
	return userName;
}
public void setUserName(String userName) {
	this.userName = userName;
}
public String getPassword() {
	return password;
}
public void setPassword(String password) {
	this.password = password;
}
public String getProjectArea() {
	return projectArea;
}
public void setProjectArea(String projectArea) {
	this.projectArea = projectArea;
}

}
