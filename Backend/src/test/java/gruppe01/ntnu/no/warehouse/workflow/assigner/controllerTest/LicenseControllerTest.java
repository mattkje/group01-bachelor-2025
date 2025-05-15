package gruppe01.ntnu.no.warehouse.workflow.assigner.controllerTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import gruppe01.ntnu.no.warehouse.workflow.assigner.controllers.LicenseController;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.License;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.LicenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@WebMvcTest(LicenseController.class)
@AutoConfigureMockMvc(addFilters = false)
class LicenseControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private LicenseService licenseService;

  @Test
  void testGetAllLicenses() throws Exception {
    // Arrange
    License license1 = new License();
    License license2 = new License();
    when(licenseService.getAllLicenses()).thenReturn(List.of(license1, license2));

    // Act & Assert
    mockMvc.perform(get("/api/licenses"))
        .andExpect(status().isOk());
    verify(licenseService, times(1)).getAllLicenses();
  }

  @Test
  void testGetLicenseById() throws Exception {
      long licenseId = 1L;
      License license = new License();
      when(licenseService.getLicenseById(licenseId)).thenReturn(license);

      mockMvc.perform(get("/api/licenses/{id}", licenseId))
          .andExpect(status().isOk());
      verify(licenseService, times(1)).getLicenseById(licenseId);
  }

  @Test
  void testCreateLicense() throws Exception {
      License license = new License();
      license.setName("Test License");
      when(licenseService.createLicense(any(License.class))).thenReturn(license);

      mockMvc.perform(post("/api/licenses")
              .contentType("application/json")
              .content("{\"name\":\"Test License\"}"))
          .andExpect(status().isOk());
      verify(licenseService, times(1)).createLicense(any(License.class));
  }

  @Test
  void testUpdateLicense() throws Exception {
      long licenseId = 1L;
      License license = new License();
      license.setName("Updated License");
      when(licenseService.getLicenseById(licenseId)).thenReturn(license);
      when(licenseService.updateLicense(eq(licenseId), any(License.class))).thenReturn(license);

      mockMvc.perform(put("/api/licenses/{id}", licenseId)
              .contentType("application/json")
              .content("{\"name\":\"Updated License\"}"))
          .andExpect(status().isOk());
      verify(licenseService, times(1)).updateLicense(eq(licenseId), any(License.class));
  }

  @Test
  void testDeleteLicense() throws Exception {
      long licenseId = 1L;
      License license = new License();
      when(licenseService.getLicenseById(licenseId)).thenReturn(license);
      when(licenseService.deleteLicense(licenseId)).thenReturn(license);

      mockMvc.perform(delete("/api/licenses/{id}", licenseId))
          .andExpect(status().isOk());
      verify(licenseService, times(1)).deleteLicense(licenseId);
  }
}