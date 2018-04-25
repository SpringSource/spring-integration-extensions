/*
 * Copyright 2012-2017 the original author or authors.
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

package org.springframework.integration.smb.filters;

import jcifs.smb.SmbException;
import org.springframework.integration.file.filters.AbstractDirectoryAwareFileListFilter;
import org.springframework.integration.file.filters.AbstractSimplePatternFileListFilter;

import jcifs.smb.SmbFile;

import java.io.UncheckedIOException;

/**
 * Implementation of {@link AbstractSimplePatternFileListFilter} for SMB.
 *
 * @author Markus Spann
 *
 */
public class SmbSimplePatternFileListFilter extends AbstractSimplePatternFileListFilter<SmbFile> {

	public SmbSimplePatternFileListFilter(String pathPattern) {
		super(pathPattern);
	}

	/**
	 * Gets the specified SMB file's name.
	 * @param file SMB file object
	 * @return file name
	 * @see AbstractSimplePatternFileListFilter#getFilename(java.lang.Object)
	 */
	@Override
	protected String getFilename(SmbFile file) {
		return (file != null) ? file.getName() : null;
	}

	/**
	 * Indicates whether the file is a directory or not.
	 * @param file SMB file object
	 * @return true if it's a directory.
	 * @see AbstractDirectoryAwareFileListFilter#isDirectory(java.lang.Object)
	 */
	@Override
	protected boolean isDirectory(SmbFile file) {
		try {
			return file.isDirectory();
		}
		catch (SmbException e) {
			throw new UncheckedIOException(e);
		}
	}
}
