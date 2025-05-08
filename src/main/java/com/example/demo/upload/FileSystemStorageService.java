package com.example.demo.upload;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service("storageService")
public class FileSystemStorageService implements StorageService {

	private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageService.class);
	private final Path rootLocation;
	private final StorageProperties storageProperties;
	private final List<String> VALID_ENTITY_TYPES = Arrays.asList("route", "workshop", "user");

	public FileSystemStorageService(StorageProperties properties) {
		this.storageProperties = properties;
		
		if (properties.getLocation().trim().isEmpty()) {
			throw new StorageException("File upload location cannot be empty");
		}

		this.rootLocation = Paths.get(properties.getLocation());
	}

	@Override
	public void init() {
		try {
			// Create root folder if it doesn't exist
			Files.createDirectories(rootLocation);
			
			// Create folders for each entity type
			for (String entityType : VALID_ENTITY_TYPES) {
				Path entityPath = Paths.get(storageProperties.getEntityLocation(entityType));
				Files.createDirectories(entityPath);
			}
			
		} catch (Exception e) {
			throw new StorageException("Could not initialize storage system", e);
		}
	}

	@Override
	public String store(MultipartFile file, String entityType, Long entityId, Integer position) {
		validateEntityType(entityType);
		
		if (file.isEmpty()) {
			throw new StorageException("Failed to store empty file");
		}
		
		Path entityPath = Paths.get(storageProperties.getEntityLocation(entityType));
		
		try {
			if (!Files.exists(entityPath)) {
				Files.createDirectories(entityPath);
			}
			
			String originalFilename = file.getOriginalFilename();
			if (originalFilename == null || !originalFilename.contains(".")) {
				throw new StorageException("Could not determine file extension");
			}
			
			// Extract file extension
			String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
			
			// Create filename according to required format
			String newFilename;
			if (entityType.equals("user")) {
				// For users, the name is simply the ID (only one image per user)
				newFilename = entityId + extension;
			} else {
				// For routes and workshops, the name includes position
				// NOTE: The 'position' acts as a unique identifier for the image
				// and represents its original position at the moment of upload.
				// It does not necessarily reflect the current display order, which is maintained
				// in the imageUrls array of the entity (Workshop/Route)
				newFilename = entityId + "_" + position + extension;
			}
			
			// Full path where the file will be saved
			Path destinationPath = entityPath.resolve(newFilename).normalize().toAbsolutePath();
			
			// Verify that the destination is within the allowed folder
			if (!destinationPath.getParent().normalize().equals(entityPath.normalize().toAbsolutePath())) {
				throw new StorageException("Cannot store file outside the designated directory");
			}
			
			// Copy the file
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
			}
			
			// Return the relative path (URL to access the image)
			return storageProperties.getEntityUrl(entityType) + "/" + newFilename;
			
		} catch (IOException e) {
			throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
		}
	}
	
	@Override
	public List<String> storeMultiple(List<MultipartFile> files, String entityType, Long entityId) {
		List<String> storedPaths = new ArrayList<>();
		
		for (int i = 0; i < files.size(); i++) {
			String path = store(files.get(i), entityType, entityId, i);
			storedPaths.add(path);
		}
		
		return storedPaths;
	}
	
	@Override
	public String getRandomUserImage() {
		String defaultImagesPath = this.storageProperties.getEntityLocation("user") + "/default";
		Path defaultImagesDir = Paths.get(defaultImagesPath);
		
		try {
			if (!Files.exists(defaultImagesDir)) {
				// Si el directorio no existe, crearlo y devolver placeholder
				Files.createDirectories(defaultImagesDir);
				return storageProperties.getBaseUrl() + "/user/default/placeholder-user.png";
			}
			
			List<Path> defaultImages = Files.list(defaultImagesDir)
					.filter(path -> {
						String fileName = path.getFileName().toString().toLowerCase();
						return fileName.endsWith(".jpg") || 
							   fileName.endsWith(".jpeg") || 
							   fileName.endsWith(".png") ||
							   fileName.endsWith(".webp");
					})
					.collect(Collectors.toList());
			
			if (defaultImages.isEmpty()) {
				return storageProperties.getBaseUrl() + "/user/default/placeholder-user.png";
			}
			
			// Seleccionar una imagen aleatoria
			int randomIndex = new Random().nextInt(defaultImages.size());
			Path selectedImage = defaultImages.get(randomIndex);
			
			// Convertir ruta del sistema de archivos a URL relativa
			String relativePath = defaultImagesDir.relativize(selectedImage).toString()
					.replace('\\', '/');
			
			return storageProperties.getBaseUrl() + "/user/default/" + relativePath;
			
		} catch (IOException e) {
			logger.error("Error al acceder al directorio de imágenes por defecto", e);
			return storageProperties.getBaseUrl() + "/user/default/placeholder-user.png";
		}
	}

	@Override
	public Stream<Path> loadAll() {
		try {
			return Files.walk(this.rootLocation, 3)  // Increased depth to include subfolders
					.filter(path -> !Files.isDirectory(path))
					.map(this.rootLocation::relativize);
		} catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}
	}
	
	@Override
	public Path load(String entityType, String filename) {
		validateEntityType(entityType);
		Path entityPath = Paths.get(storageProperties.getEntityLocation(entityType));
		return entityPath.resolve(filename);
	}
	
	@Override
	public Resource loadAsResource(String entityType, String filename) {
		try {
			Path file = load(entityType, filename);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new StorageFileNotFoundException("Could not read file: " + filename);
			}
		} catch (MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}

	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(rootLocation.toFile());
	}
	
	@Override
	public boolean delete(String entityType, Long entityId, Integer position) {
		validateEntityType(entityType);
		
		Path entityPath = Paths.get(storageProperties.getEntityLocation(entityType));
		
		try {
			if (entityType.equals("user")) {
				// For users, find files that start with the ID
				try (Stream<Path> files = Files.list(entityPath)) {
					return files
						.filter(path -> path.getFileName().toString().startsWith(entityId.toString() + "."))
						.findAny()
						.map(path -> {
							try {
								Files.delete(path);
								return true;
							} catch (IOException e) {
								return false;
							}
						})
						.orElse(false);
				}
			} else {
				// For routes and workshops, find files with ID and position
				try (Stream<Path> files = Files.list(entityPath)) {
					return files
						.filter(path -> path.getFileName().toString().startsWith(entityId.toString() + "_" + position))
						.findAny()
						.map(path -> {
							try {
								Files.delete(path);
								return true;
							} catch (IOException e) {
								return false;
							}
						})
						.orElse(false);
				}
			}
		} catch (IOException e) {
			throw new StorageException("Error deleting file", e);
		}
	}
	
	private void validateEntityType(String entityType) {
		if (!VALID_ENTITY_TYPES.contains(entityType.toLowerCase())) {
			throw new IllegalArgumentException("Invalid entity type: " + entityType + 
					". Valid types: " + String.join(", ", VALID_ENTITY_TYPES));
		}
	}
}
