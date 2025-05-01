package com.example.demo.upload;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

	/**
	 * Initializes the storage system, creating necessary folders
	 */
	void init();
	
	/**
	 * Stores a file associated with a specific entity
	 * 
	 * @param file File to store
	 * @param entityType Entity type (route, workshop, user)
	 * @param entityId Entity ID
	 * @param position Image position (for entities that allow multiple images)
	 * @return Relative path where the file has been saved
	 */
	String store(MultipartFile file, String entityType, Long entityId, Integer position);
	
	/**
	 * Stores multiple files for an entity
	 * 
	 * @param files List of files to store
	 * @param entityType Entity type
	 * @param entityId Entity ID
	 * @return List of relative paths where the files have been saved
	 */
	List<String> storeMultiple(List<MultipartFile> files, String entityType, Long entityId);
	
	/**
	 * Gets a random user image
	 * 
	 * @return Relative path to a random user image
	 */
	String getRandomUserImage();
	
	/**
	 * Loads all files from the storage system
	 * 
	 * @return Stream of paths to the files
	 */
	Stream<Path> loadAll();
	
	/**
	 * Loads a specific file from an entity
	 * 
	 * @param entityType Entity type
	 * @param filename File name
	 * @return Path to the file
	 */
	Path load(String entityType, String filename);
	
	/**
	 * Loads an entity file as a resource
	 * 
	 * @param entityType Entity type
	 * @param filename File name
	 * @return File resource
	 */
	Resource loadAsResource(String entityType, String filename);
	
	/**
	 * Deletes all files
	 */
	void deleteAll();
	
	/**
	 * Deletes a specific file from an entity
	 * 
	 * @param entityType Entity type
	 * @param entityId Entity ID
	 * @param position Image position (for entities with multiple images)
	 * @return true if deleted successfully, false otherwise
	 */
	boolean delete(String entityType, Long entityId, Integer position);
}
