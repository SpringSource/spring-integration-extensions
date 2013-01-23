/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.splunk.support;

import com.splunk.Service;

/**
 * Splunk server entity
 *
 * @author Jarred Li
 * @since 1.0
 *
 */
public class SplunkServer {

	private String host = Service.DEFAULT_HOST;
	private int port = Service.DEFAULT_PORT;
	private String scheme = Service.DEFAULT_SCHEME;
	private String app;
	private String owner;
	private String username;
	private String password;
	private int timeout;

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * 
	 * @return
	 */
	public String getScheme() {
		return scheme;
	}

	/**
	 * 
	 * @param scheme
	 */
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	/**
	 * 
	 * @return
	 */
	public String getApp() {
		return app;
	}

	/**
	 * 
	 * @param app
	 */
	public void setApp(String app) {
		this.app = app;
	}

	/**
	 * 
	 * @return
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * 
	 * @param owner
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the userName
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the timeout in ms.
	 */
	public int getTimeout() {
		return timeout;
	}
	
	/**
	 * set the timeout in ms.
	 * @param timeout
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}
