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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import lombok.extern.slf4j.Slf4j;

import nl.rivm.screenit.service.FileService;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.apache.commons.lang3.StringUtils.chomp;

@Slf4j
@Service
@Conditional(S3FileServiceCondition.class)
public class S3FileServiceImpl implements FileService, InitializingBean
{

	@Autowired
	@Qualifier("s3bucketEndpointOverride")
	private String s3bucketEndpointOverride;

	@Autowired
	@Qualifier("s3bucketAccessId")
	private String s3bucketAccessId;

	@Autowired
	@Qualifier("s3bucketAccessSecret")
	private String s3bucketAccessSecret;

	@Autowired
	@Qualifier("s3bucketRegion")
	private String s3bucketRegion;

	@Autowired
	@Qualifier("s3bucketName")
	private String s3bucketName;

	private S3Client s3;

	@Override
	public boolean exists(String fullFilePath)
	{
		try
		{
			return s3.headObject(HeadObjectRequest.builder()
				.bucket(s3bucketName)
				.key(getS3Path(fullFilePath))
				.build()).sdkHttpResponse().isSuccessful();
		}
		catch (S3Exception e)
		{
			return false;
		}
	}

	@Override
	public void save(String fullFilePath, File tempFile) throws IOException
	{
		save(fullFilePath, new FileInputStream(tempFile), tempFile.length());
	}

	@Override
	public void save(String fullFilePath, InputStream content, Long contentLength) throws IOException
	{
		if (StringUtils.isBlank(fullFilePath))
		{
			throw new IllegalStateException("Er is geen pad opgegeven om op te slaan");
		}
		try
		{
			PutObjectResponse objectResponse = s3.putObject(PutObjectRequest
				.builder()
				.bucket(s3bucketName)
				.key(getS3Path(fullFilePath)).build(), RequestBody.fromInputStream(content, contentLength));

			if (!objectResponse.sdkHttpResponse().isSuccessful())
			{
				throw new IOException("HTTP error bij uploaden bestand " + fullFilePath + " naar S3 (code=" + objectResponse.sdkHttpResponse().statusCode() + ")");
			}
		}
		catch (S3Exception e)
		{
			throw new IOException("Fout bij uploaden van bestand " + fullFilePath + " naar S3", e);
		}
	}

	@Override
	public File load(String fullFilePath)
	{
		try
		{
			File file = File.createTempFile(FilenameUtils.getBaseName(fullFilePath), "." + FilenameUtils.getExtension(fullFilePath));
			LOG.debug("Tijdelijk bestand {} aangemaakt van S3 bestand {}", file.getPath(), fullFilePath);
			try (InputStream documentStream = loadAsStream(fullFilePath))
			{
				Files.write(file.toPath(), IOUtils.toByteArray(documentStream), StandardOpenOption.WRITE);
			}
			return file;
		}
		catch (Exception e)
		{
			LOG.error("Fout bij downloaden van bestand {} als 'File' uit S3 door {}", fullFilePath, e.getMessage(), e);
			return null;
		}
	}

	@Override
	public InputStream loadAsStream(String fullFilePath) throws IOException
	{
		if (StringUtils.isBlank(fullFilePath))
		{
			return null;
		}
		try
		{
			return s3.getObject(GetObjectRequest
				.builder()
				.bucket(s3bucketName)
				.key(getS3Path(fullFilePath))
				.build());
		}
		catch (S3Exception e)
		{
			LOG.error("Fout bij downloaden van bestand {} uit S3 door {}", fullFilePath, e.getMessage(), e);
			throw new IOException(e);
		}
	}

	@Override
	public boolean delete(String fullFilePath)
	{
		if (StringUtils.isBlank(fullFilePath))
		{
			return false;
		}
		try
		{
			ObjectIdentifier key = ObjectIdentifier.builder().key(getS3Path(fullFilePath)).build();
			DeleteObjectsRequest request = DeleteObjectsRequest
				.builder()
				.bucket(s3bucketName)
				.delete(Delete
					.builder()
					.objects(key)
					.build())
				.build();
			DeleteObjectsResponse response = s3.deleteObjects(request);
			return response.sdkHttpResponse().isSuccessful();
		}
		catch (S3Exception e)
		{
			LOG.error("Fout bij verwijderen van bestand {} uit S3 door {}", fullFilePath, e.getMessage(), e);
		}
		return false;
	}

	@Override
	public void afterPropertiesSet()
	{
		final AwsBasicCredentials basicCredentials = AwsBasicCredentials.create(s3bucketAccessId, s3bucketAccessSecret);

		s3 = S3Client
			.builder()
			.credentialsProvider(StaticCredentialsProvider.create(basicCredentials))
			.region(Region.of(s3bucketRegion))
			.applyMutation(this::setEndpointOverrideIfPresent)
			.build();
	}

	private void setEndpointOverrideIfPresent(final S3ClientBuilder s3ClientBuilder) throws IllegalArgumentException
	{
		if (StringUtils.isNotBlank(s3bucketEndpointOverride))
		{
			try
			{
				final URI endpointOverrideURI = URI.create(s3bucketEndpointOverride);
				s3ClientBuilder.endpointOverride(endpointOverrideURI);
			}
			catch (final IllegalArgumentException e)
			{
				LOG.error("Invalid endpoint override URI", e);
				throw e;
			}
		}
	}

	private String getS3Path(String path)
	{
		String formattedPath = path.replace("\\", "/").replaceAll("\\\\", "/").replaceAll("//", "/");
		return formattedPath.endsWith("/") ? chomp(formattedPath) : formattedPath;
	}
}
