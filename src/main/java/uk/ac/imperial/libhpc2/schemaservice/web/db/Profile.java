package uk.ac.imperial.libhpc2.schemaservice.web.db;

import java.util.Map;

public class Profile {
	
	private int _id;
	private String _name;
	private String _templateId;
	private String _profileXml;
	
	public Profile() {};
	
	public Profile(Map<String,Object> pData) {
		Integer id = (Integer)pData.get("id");
		if(id != null) {
			this._id = (Integer)pData.get("id");
		}
		this._name = (String)pData.get("name");
		this._templateId = (String)pData.get("templateId");
		this._profileXml = (String)pData.get("profileXml");
	}
	
	public int getId() {
		return _id;
	}
	
	public void setId(int pId) {
		this._id = pId;
	}
	
	public String getName() {
		return _name;
	}
	
	public void setName(String pName) {
		this._name = pName;
	}
	
	public String getTemplateId() {
		return _templateId;
	}
	
	public void setTemplateId(String pTemplateId) {
		this._templateId = pTemplateId;
	}
	
	public String getProfileXml() {
		return _profileXml;
	}
	
	public void setProfileXml(String pProfileXml) {
		this._profileXml = pProfileXml;
	}
}
