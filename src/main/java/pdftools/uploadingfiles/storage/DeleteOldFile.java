package pdftools.uploadingfiles.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.FileSystemUtils;

/**
 * Delete old files in storage folder for clear storage
 * @author Deegmas
 * 
 */
public class DeleteOldFile {
	
	private Path rootLocation;
	
	private final int EXPIRATION_TIME = 120000;
	
	/**
	 * Delete file in the storage folder if the file is stored since 2 minutes
	 * @throws IOException
	 */
	public void deleteOldFile() throws IOException {
		
		StorageProperties storageProperties = new StorageProperties();
		this.rootLocation = Paths.get(storageProperties.getLocation());
		
		List<File> listOfFile = Files.list(this.rootLocation).map(Path::toFile).collect(Collectors.toList());
		
		for (File path : listOfFile) {
			long lastModifiedTime = Files.getLastModifiedTime(path.toPath()).toMillis();
			long timeSinceLastModifiedTime = new Date().getTime() - lastModifiedTime;
			if (timeSinceLastModifiedTime > EXPIRATION_TIME) {
				FileSystemUtils.deleteRecursively(path.toPath());
			}
		}
	}
}