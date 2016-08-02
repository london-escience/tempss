/*
 * Copyright (c) 2015, Imperial College London
 * Copyright (c) 2015, The University of Edinburgh
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the names of the copyright holders nor the names of their
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * -----------------------------------------------------------------------------
 *
 * This file is part of the TemPSS - Templates and Profiles for Scientific 
 * Software - service, developed as part of the libhpc projects 
 * (http://www.imperial.ac.uk/lesc/projects/libhpc).
 *
 * We gratefully acknowledge the Engineering and Physical Sciences Research
 * Council (EPSRC) for their support of the projects:
 *   - libhpc: Intelligent Component-based Development of HPC Applications
 *     (EP/I030239/1).
 *   - libhpc Stage II: A Long-term Solution for the Usability, Maintainability
 *     and Sustainability of HPC Software (EP/K038788/1).
 */

package uk.ac.imperial.libhpc2.schemaservice.web.db;

import java.util.Map;

public class Profile {
	
	private int _id;
	private String _name;
	private String _templateId;
	private String _profileXml;
	private boolean _public;
	private String _owner;
	
	public Profile() {};
	
	public Profile(Map<String,Object> pData) {
		Integer id = (Integer)pData.get("id");
		if(id != null) {
			this._id = (Integer)pData.get("id");
		}
		this._name = (String)pData.get("name");
		this._templateId = (String)pData.get("templateId");
		this._profileXml = (String)pData.get("profileXml");
		this._owner = (String)pData.get("owner");
		this._public = false;
		if(pData.containsKey("public")) {
			if(((Integer)pData.get("public")) == 1) {
				this._public = true;
			}	
		}
		
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
	
	public boolean getPublic() {
		return this._public;
	}
	
	public void setPublic(boolean pPublic) {
		this._public = pPublic;
	}
	
	public String getOwner() {
		return this._owner;
	}
	
	public void setOwner(String pOwner) {
		this._owner = pOwner;
	}
}
