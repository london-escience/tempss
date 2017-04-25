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

package uk.ac.imperial.libhpc2.schemaservice;

/**
 * A metadata object containing details of a component for which
 * a template and associated profile mappings have been made.
 */
public class TempssObject {

    private String _id;
    private String _name;
    private String _schema;
    private String _transform;
    private String _constraints;
    private boolean _ignore;

    
    public TempssObject(String id, String name, String schema, String transform) {
    	this(id, name, schema, transform, null, false);
    }
    
    public TempssObject(String id, String name, String schema, String transform, boolean ignore) {
    	this(id, name, schema, transform, null, ignore);
    }
    
    public TempssObject(String id, String name, String schema, String transform, String constraints) {
    	this(id, name, schema, transform, constraints, false);
    }
    
    public TempssObject(String id, String name, String schema, String transform, String constraints, boolean ignore) {
    	this._id = id;
        this._name = name;
        this._schema = schema;
        this._transform = transform;
        this._constraints = constraints;
        this._ignore = ignore;
    }
    
    public TempssObject(TempssObject pObj) {
    	this._id = pObj.getId();
        this._name = pObj.getName();
        this._schema = pObj.getSchema();
        this._transform = pObj.getTransform();
        this._constraints = pObj.getConstraints();
        this._ignore = pObj.ignore();
    }

    public String getId() {
        return _id;
    }
    
    public void setId(String id) {
        this._id = id;
    }
    
    public String getName() {
        return _name;
    }
    
    public void setName(String name) {
        this._name = name;
    }
    
    public String getSchema() {
        return _schema;
    }
    
    public void setSchema(String schema) {
        this._schema = schema;
    }
    
    public String getTransform() {
        return _transform;
    }
    
    public void setTransform(String transform) {
        this._transform = transform;
    }
    
    public String getConstraints() {
        return _constraints;
    }
    
    public void setConstraints(String constraints) {
        this._constraints = constraints;
    }
    
    public boolean ignore() {
    	return this._ignore;
    }
    
    public void setIgnore(boolean ignore) {
    	this._ignore = ignore;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("TemPSS Template: " + getId());
        s.append("\n\tName: " + getName());
        s.append("\n\tSchema: " + getSchema());
        s.append("\n\tTransform: " + getTransform());
        s.append("\n\tConstraints: " + getConstraints());
        s.append("\n\tIgnore template? " + ((ignore()) ? "YES" : "NO"));
        return s.toString();
    }
}
