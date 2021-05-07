package pdftools.uploadingfiles;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pdftools.service.PdfService;
import pdftools.uploadingfiles.storage.StorageFileNotFoundException;
import pdftools.uploadingfiles.storage.StorageService;

@Controller
public class FileUploadController {

	private final StorageService storageService;

	@Autowired
	public FileUploadController(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/")
	public String home() {
		return "home";
	}
	
	@GetMapping("/encrypt")
	public String encryptForm() {
		return "encrypt";
	}
	
	@GetMapping("/split")
	public String splitForm() {
		return "split";
	}
	
	@GetMapping("/merge")
	public String mergeForm() {
		return "merge";
	}
	
	@GetMapping("/pdftoimg")
	public String pdfToImgForm() {
		return "pdfToImg";
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> getFiles(@PathVariable String filename) {

		Resource file = storageService.loadAsResource(filename);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}

	@PostMapping("/pdftoimg")
	public String upoloadPdfToImageConversion(@RequestParam("file") MultipartFile file, 
											  RedirectAttributes redirectAttributes) {
		if (file.isEmpty()) {
			redirectAttributes.addFlashAttribute("message",
					"Veuillez choisir un fichier");
			
			return "redirect:/pdftoimg";
		}
		
		storageService.store(file);

		PdfService convertToImage = new PdfService();
		String convertedFilename = "";
		try {
			convertedFilename = convertToImage.pdfToImage(file.getOriginalFilename());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "redirect:/files/" + convertedFilename;
	}
	
	@PostMapping("/encrypt")
	public String uploadToEncryptPdf(@RequestParam("file") MultipartFile file,
									@RequestParam(name="pwd") String password,
									RedirectAttributes redirectAttributes)
									throws InvalidPasswordException, IOException {
		if (file.isEmpty()) {
			redirectAttributes.addFlashAttribute("message",
					"Veuillez choisir un fichier");
			return "redirect:/encrypt";
		}
		
		storageService.store(file);
		
		if (password.isEmpty()) {
			redirectAttributes.addFlashAttribute("message",
					"Veuillez saisir un mot de passe");
			
			return "redirect:/encrypt";
		}
		
		PdfService encryptService = new PdfService();
		String filename = encryptService.encryptedPdf(file.getOriginalFilename(), password);
			
		return "redirect:/files/" + filename;
	}
	
	@PostMapping("/split")
	public String splitpdf(@RequestParam("file") MultipartFile file,
							RedirectAttributes redirectAttributes)
							throws InvalidPasswordException, IOException {

		if (file.isEmpty()) {
			redirectAttributes.addFlashAttribute("message",
					"Veuillez choisir un fichier");
			
			return "redirect:/split";
		}
		
		storageService.store(file);
		
		PdfService convertService = new PdfService();
		String zip = convertService.splitPdf(file.getOriginalFilename());
			
		
		return "redirect:/files/" + zip;
	}
	
	@PostMapping("/merge")
	public String uploadToMergePdf(@RequestParam("file") MultipartFile[] files, 
									RedirectAttributes redirectAttributes)
									throws InvalidPasswordException, IOException {

		if (files.length < 1) {
			redirectAttributes.addFlashAttribute("message",
					"Veuillez choisir un fichier");
			
			return "redirect:/merge";
		}
		
		storageService.store(files);

		String filesUploaded[] = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			filesUploaded[i] = files[i].getOriginalFilename();
		}

		PdfService mergeFiles = new PdfService();
		String filenameMerged = mergeFiles.mergePdf(filesUploaded);

		return "redirect:/files/" + filenameMerged;
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}
}