package ru.phosagro.jboss.esb.esb017.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
//@JsonRootName("responseJson")
public class ResponseDto implements Serializable {
	private String projectCode;
	private String numberOfTask;
	@JsonAlias({"Status", "status"})
	@JsonProperty
	private String status;

	private String responseJson;

	@JsonProperty("projectCode")
	public String getProjectCode() {
		return projectCode;
	}

	@JsonAlias({"ProjectCode", "project_code", "projectCode"})
	public void setProjectCode(String projectCode) {
		this.projectCode = projectCode;
	}

	@JsonProperty("numberOfTask")
	public String getNumberOfTask() {
		return numberOfTask;
	}

	@JsonAlias({"NumberOfTask", "number_of_task", "numberOfTask"})
	public void setNumberOfTask(String numberOfTask) {
		this.numberOfTask = numberOfTask;
	}


	@JsonProperty("responseJson")
	public void setResponseJson(String responseJson) {
		this.responseJson = responseJson;
	}


}
