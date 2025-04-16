package com.example.demo.servicesImpl;

import java.text.Normalizer;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.example.demo.entities.Workshop;
import com.example.demo.repositories.WorkshopRepository;
import com.example.demo.services.WorkshopService;

@Service("workshopService")
public class WorkshopServiceImpl implements WorkshopService {

    @Autowired
    @Qualifier("workshopRepository")
    private WorkshopRepository workshopRepository;
    
    @Override
    public Workshop findById(Long id) {
        return workshopRepository.findById(id).orElse(null);
    }

    @Override
    public List<Workshop> findByCity(String city) {
        return workshopRepository.findByCity(normalizeCity(city));
    }

    @Override
    public Workshop saveWorkshop(Workshop workshop) {
        return workshopRepository.save(workshop);
    }

    @Override
    public String normalizeCity(String city) {
        if (city == null) {
            return "";
        }
        
        String normalized = city.toLowerCase();
        
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}", "");
        
        return normalized;
    }
}