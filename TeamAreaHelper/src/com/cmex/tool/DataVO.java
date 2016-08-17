package com.cmex.tool;

import java.util.List;

import com.ibm.team.repository.common.IContributor;

public class DataVO {
	
	private String emailId;
	
	private String flag;
	
	private List<String> roles;
	
	private IContributor contributor;

	private List<String> teamAreas;
	
	public List<String> getTeamAreas() {
		return teamAreas;
	}

	public void setTeamAreas(List<String> teamAreas) {
		this.teamAreas = teamAreas;
	}

	public IContributor getContributor() {
		return contributor;
	}

	public void setContributor(IContributor contributor) {
		this.contributor = contributor;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}
	
	

}
