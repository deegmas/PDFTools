package pdftools.uploadingfiles;

import java.io.IOException;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import pdftools.uploadingfiles.storage.DeleteOldFile;
import pdftools.uploadingfiles.storage.StorageProperties;
import pdftools.uploadingfiles.storage.StorageService;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class UploadingFilesApplication {

	public static void main(String[] args) {
		SpringApplication.run(UploadingFilesApplication.class, args);
		Thread thread = new Thread();
		DeleteOldFile deleteOldFile = new DeleteOldFile();
		while(true) {
			try {
				deleteOldFile.deleteOldFile();
				thread.sleep(60000);
			} catch (IOException | InterruptedException e) {
				e.getStackTrace();
			}
		}
	}

	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
			storageService.deleteAll();
			storageService.init();
		};
	}
}
