package com.example.demo.upload;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FileUploadController {

	private final StorageService storageService;

	public FileUploadController(StorageService storageService) {
		this.storageService = storageService;
	}
	
	/**
	 * Serves a file from any entity type
	 * @param entityType Entity type (route, workshop, user)
	 * @param filename File name
	 * @return The requested resource
	 */
	@GetMapping("/images/{entityType}/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveEntityFile(
			@PathVariable String entityType,
			@PathVariable String filename) {

		Resource file = storageService.loadAsResource(entityType, filename);
		
		if (file == null) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok().body(file);
	}

	/**
	 * Uploads an image for a specific entity type
	 * @param file The file to upload
	 * @param entityType Entity type (route, workshop, user)
	 * @param entityId Entity ID
	 * @param position Image position (for entities that support multiple images)
	 * @param redirectAttributes Attributes for redirection
	 * @return Redirection URL
	 */
	@PostMapping("/upload")
	public String handleFileUpload(
			@RequestParam MultipartFile file,
			@RequestParam String entityType,
			@RequestParam Long entityId,
			@RequestParam(required = false, defaultValue = "0") Integer position,
			RedirectAttributes redirectAttributes) {

		String storedPath = storageService.store(file, entityType, entityId, position);
		redirectAttributes.addFlashAttribute("message",
				"The image has been uploaded successfully: " + storedPath);

		return "redirect:/admin";
	}
	
	/**
	 * Uploads multiple images for a specific entity type
	 * @param files Files to upload
	 * @param entityType Entity type (route, workshop, user)
	 * @param entityId Entity ID
	 * @param redirectAttributes Attributes for redirection
	 * @return Redirection URL
	 */
	@PostMapping("/upload-multiple")
	public String handleMultipleFileUpload(
			@RequestParam List<MultipartFile> files,
			@RequestParam String entityType,
			@RequestParam Long entityId,
			RedirectAttributes redirectAttributes) {

		List<String> storedPaths = storageService.storeMultiple(files, entityType, entityId);
		redirectAttributes.addFlashAttribute("message",
				storedPaths.size() + " images have been uploaded successfully");

		return "redirect:/admin";
	}
	
	@org.springframework.web.bind.annotation.ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}
}
