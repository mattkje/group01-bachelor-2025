package gruppe01.ntnu.no.warehouse.workflow.assigner.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.util.Set;

/**
 * Represents a License entity in the system.
 * Both workers and tasks can have licenses.
 * A license is a requirement for a worker to perform a task.
 */
@Entity
@Table(name = "license")
@Schema(description = "Represents a License entity in the system.")
public class License {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(description = "Unique identifier for the license.")
  private Long id;

  @Column(name = "name")
  @Schema(description = "The name of the license.")
  private String name;

  @ManyToMany(mappedBy = "licenses", fetch = FetchType.LAZY)
  @JsonIgnore
  @Schema(description = "The workers who have this license.")
  private Set<Worker> workers;

  @ManyToMany(mappedBy = "requiredLicense", fetch = FetchType.LAZY)
  @JsonIgnore
  @Schema(description = "The tasks that require this license.")
  private Set<Task> tasks;

  public License() {
  }

  public License(String name) {
    this.name = name;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setWorkers(Set<Worker> workers) {
    this.workers = workers;
  }

  public void setTasks(Set<Task> tasks) {
    this.tasks = tasks;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Set<Worker> getWorkers() {
    return workers;
  }

  public Set<Task> getTasks() {
    return tasks;
  }

  @Override
  public boolean equals(Object o) {
      if (this == o) {
          return true;
      }
      if (o == null || getClass() != o.getClass()) {
          return false;
      }
    License license = (License) o;
    return id != null && id.equals(license.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
