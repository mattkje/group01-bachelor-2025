package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.MonteCarloDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MonteCarloService {

    @Autowired
    private MonteCarloDataRepository monteCarloDataRepository;

    public void generateMonteCarloData(LocalDateTime now, boolean b) {

    }
}
