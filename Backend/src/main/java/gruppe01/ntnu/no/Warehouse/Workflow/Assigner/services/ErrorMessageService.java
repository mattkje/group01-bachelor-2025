package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ErrorMessage;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.ErrorMessageRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ErrorMessageService {
    private final ErrorMessageRepository errorMessageRepository;
    private final ZoneService zoneService;

    public ErrorMessageService(ErrorMessageRepository errorMessageRepository, ZoneService zoneService) {
        this.errorMessageRepository = errorMessageRepository;
        this.zoneService = zoneService;
    }

    public List<ErrorMessage> getAllErrorMessages() {
        return errorMessageRepository.findAll();
    }

    public ErrorMessage getErrorMessageById(long id) {
        return errorMessageRepository.findById(id).orElse(null);
    }

    public List<ErrorMessage> getErrorMessagesByZoneId(long zoneId) {
        return errorMessageRepository.findAll().stream()
                .filter(errorMessage -> errorMessage.getZoneId() == zoneId)
                .toList();
    }

    public void saveErrorMessage(ErrorMessage errorMessage) {
        errorMessageRepository.save(errorMessage);
    }

    public ErrorMessage createErrorMessage(long zoneId, ErrorMessage errorMessage) {
        if (zoneService.getZoneById(zoneId) == null) {
            return null;
        } else {
            errorMessage.setZoneId(zoneId);
            return errorMessageRepository.save(errorMessage);
        }
    }

    public ErrorMessage updateErrorMessage(long id, ErrorMessage updatedErrorMessage) {
        if (!errorMessageRepository.existsById(id)) {
            return null;
        } else {
            updatedErrorMessage.setId(id);
            return errorMessageRepository.save(updatedErrorMessage);
        }
    }

    public ErrorMessage deleteErrorMessage(long id) {
        if (!errorMessageRepository.existsById(id)) {
            return null;
        } else {
            errorMessageRepository.deleteById(id);
            return errorMessageRepository.findById(id).orElse(null);
        }
    }

    public Map<Long, ErrorMessage> generateErrorMessageMapFromZones() {
        Map<Long, ErrorMessage> errorMessageMap = new HashMap<>();
        zoneService.getAllZones().forEach(zone -> {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setZoneId(zone.getId());
            errorMessage.setMessage("");
            errorMessageMap.put(zone.getId(), errorMessage);
        });
        return errorMessageMap;
    }

    public void deleteAll() {
        errorMessageRepository.deleteAll();
    }

    public String getLastErrorMessageTime() {
        ErrorMessage lastErrorMessage = errorMessageRepository.findTopByOrderByTimeDesc();
        if (lastErrorMessage != null && lastErrorMessage.getTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return lastErrorMessage.getTime().format(formatter);
        }
        return "00:00";
    }

    public String getErrorMessageTimeByZone(long zoneId) {
        ErrorMessage lastErrorMessage = errorMessageRepository.findTopByZoneIdOrderByTimeDesc(zoneId);
        if (lastErrorMessage != null && lastErrorMessage.getTime() != null && lastErrorMessage.getZoneId() == zoneId) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return lastErrorMessage.getTime().format(formatter);
        }
        return "00:00";
    }
}
