package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.worldsimulation.WorldSimulation;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/simulation")
public class WorldSimulationController {

    @Autowired
    private WorldSimulation worldSimulation;

   @PostMapping("/start")
   public void startSimulation(@RequestParam int simulationTime, @RequestParam int simCount) throws Exception {
       worldSimulation.runWorldSimulation(simulationTime, LocalDate.now());
       // Add logic to handle simCount if needed
   }
    @PostMapping("/pause")
    public void pauseSimulation() throws InterruptedException, IOException, ExecutionException {
        worldSimulation.pauseSimulation();
    }

    @PostMapping("/stop")
    public void stopSimulation() throws InterruptedException, IOException, ExecutionException {
        worldSimulation.stopSimulation();
    }

    @GetMapping("/getStatus")
    public int getSimulationStatus() {
        return worldSimulation.getPauseStatus();
    }

    @PostMapping("/fastForward")
    public void fastForwardSimulation(@RequestParam double speedMultiplier) {
        worldSimulation.changeSimulationSpeed(speedMultiplier);
    }

    @GetMapping("/currentTime")
    public LocalTime getCurrentSimulationTime() {
        return worldSimulation.getCurrentTime();
    }

    @GetMapping("/currentDate")
    public LocalDate getCurrentDate() {
        return worldSimulation.getCurrentDate();
    }

    @GetMapping("/CurrentDateTime")
    public LocalDateTime getCurrentDateTime() {
        return worldSimulation.getCurrentDateTime();
    }
}
