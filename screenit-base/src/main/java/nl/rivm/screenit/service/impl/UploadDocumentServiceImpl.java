package nl.rivm.screenit.service.impl;

/*-
 * ========================LICENSE_START=================================
 * screenit-base
 * %%
 * Copyright (C) 2012 - 2022 Facilitaire Samenwerking Bevolkingsonderzoek
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * =========================LICENSE_END==================================
 */

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import nl.rivm.screenit.dao.UploadDocumentDao;
import nl.rivm.screenit.model.UploadDocument;
import nl.rivm.screenit.model.enums.FileStoreLocation;
import nl.rivm.screenit.service.FileService;
import nl.rivm.screenit.service.UploadDocumentService;
import nl.topicuszorg.hibernate.spring.dao.HibernateService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class UploadDocumentServiceImpl implements UploadDocumentService
{
	@Autowired
	@Qualifier("locatieFilestore")
	private String locatieFilestore;

	@Autowired
	private FileService fileService;

	@Autowired
	private UploadDocumentDao uploadDocumentDao;

	@Autowired
	private HibernateService hibernateService;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(UploadDocument document)
	{
		delete(document, false);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(UploadDocument document, boolean deleteFile)
	{
		if (deleteFile)
		{
			fileService.delete(getFullFilePath(document));
		}

		uploadDocumentDao.delete(document);
	}

	@Override
	public File load(UploadDocument uploadDocument)
	{
		if (uploadDocument == null)
		{
			throw new IllegalStateException("Het meegegeven uploaddocument is null");
		}
		else
		{
			return fileService.load(getFullFilePath(uploadDocument));
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void saveOrUpdate(UploadDocument document, FileStoreLocation fileStoreLocation) throws IOException, IllegalStateException
	{
		saveOrUpdate(document, fileStoreLocation, null, true);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void saveOrUpdate(UploadDocument document, FileStoreLocation fileStoreLocation, Long id) throws IOException, IllegalStateException
	{
		saveOrUpdate(document, fileStoreLocation, id, true);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void saveOrUpdate(UploadDocument document, FileStoreLocation fileStoreLocation, Long id, boolean verwijderTmpFile) throws IOException,
		IllegalArgumentException
	{
		if (document == null)
		{
			throw new IllegalArgumentException("UploadDocument is leeg.");
		}

		if (id == null && fileStoreLocation.isSaveFileWithId())
		{
			throw new IllegalArgumentException("Id verwacht voor het opslaan van de juiste directory");
		}

		File tmpFile = null;
		if (verwijderTmpFile)
		{
			tmpFile = document.getFile();
		}

		String path;
		if (id != null)
		{
			path = fileStoreLocation.getPath(id);
		}
		else
		{
			path = fileStoreLocation.getPath();
		}

		if (save(document, path))
		{
			uploadDocumentDao.saveOrUpdate(document);
		}

		hibernateService.saveOrUpdate(document);

		if (tmpFile != null && !tmpFile.delete())
		{
			LOG.warn("Tijdelijk bestand {} is niet verwijderd", tmpFile.getPath());
		}
	}

	private boolean save(UploadDocument uploadDocument, String path) throws IOException
	{
		if (StringUtils.isBlank(uploadDocument.getPath()))
		{
			String filestoreFileName = generateFiltestoreFileName();
			String fullFilePath = locatieFilestore + path + System.getProperty("file.separator") + filestoreFileName;

			fileService.save(fullFilePath, uploadDocument.getFile());
			LOG.debug("UploadDocument {} is geupload onder {}", uploadDocument.getId(), fullFilePath);

			uploadDocument.setPath(path + System.getProperty("file.separator") + filestoreFileName);
			return true;
		}

		return false;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(UploadDocument uploadDocument) throws IOException
	{
		fileService.save(getFullFilePath(uploadDocument), uploadDocument.getFile());

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Bestand voor UploadDocument {} vervangen", uploadDocument.getId());
		}

		uploadDocumentDao.saveOrUpdate(uploadDocument);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteDocumentFromList(UploadDocument document, List<UploadDocument> documents)
	{
		try
		{
			documents.removeIf(uplDoc -> uplDoc.getId().equals(document.getId()));
			delete(document, true);
		}
		catch (Exception e)
		{
			LOG.error("Er is een fout opgetreden! " + e.getMessage(), e);
		}
	}

	private String getFullFilePath(UploadDocument document)
	{
		StringBuilder fullPath = new StringBuilder();
		fullPath.append(locatieFilestore);
		boolean fileStoreEindigtOpDirSeparator = StringUtils.endsWith(locatieFilestore, System.getProperty("file.separator"));
		boolean uploadDocumentPathBegintMetSeparator = StringUtils.startsWith(document.getPath(), System.getProperty("file.separator"));
		if (!fileStoreEindigtOpDirSeparator && !uploadDocumentPathBegintMetSeparator)
		{
			fullPath.append(System.getProperty("file.separator"));
		}

		String path = StringUtils.replace(document.getPath(), System.getProperty("file.separator") + System.getProperty("file.separator"),
			System.getProperty("file.separator"));
		fullPath.append(path);
		return fullPath.toString();
	}

	private String generateFiltestoreFileName()
	{
		String uniqueFileName = UUID.randomUUID().toString();
		int random = (int) (Math.random() * 10.0D);
		return uniqueFileName + "-" + System.currentTimeMillis() + "-" + random;
	}

}
