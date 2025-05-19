# Warehouse Management Simulator – Bachelor Thesis Project

## Overview

This project is a bachelor thesis developed in collaboration with **Solwr Software AS**, a Norwegian company specializing in logistics solutions for the retail and commercial sectors. The goal was to design and implement a **proof-of-concept warehouse management application** that assists warehouse managers in allocating workers efficiently across zones using **Monte Carlo simulations** and **machine learning**.

The application highlights best practices in design, development, and documentation while offering clear visualizations of predicted task durations. Its predictive capabilities aim to improve operational efficiency by simulating and adapting to real-world warehouse conditions. The system is designed to run locally and serve as a **functional prototype and source of inspiration** for future internal software projects at Solwr.

---

## Background

### About Solwr Software AS

Solwr is a Norwegian tech company focused on solving logistics challenges in the retail industry. With over 100 employees, their solutions include:

* **Grab™** and **Sort™**: Robotic assistants for warehouse automation
* **Trace™**: A specialized ERP system used by roughly **one-third of all grocery products in Norway** during their lifecycle

Originally founded as **Data Pro AS** in 1982, the company has deep roots in distribution-focused ERP systems. In 2016, the robotics company **Currence Robotics** was launched by the same founder. These two entities merged in 2022 under the name **Solwr**, combining digital and robotic innovations to strengthen logistics operations.

---

## Features
- **Monte Carlo Simulations**: Predicts possible delays based on historical data and task variability.
- **Machine Learning Integration**: Utilizes past data to improve prediction accuracy over time.
- **Interactive Interface**: Displays real-time predictions and allows manual adjustments.
- **Scenario Testing**: Enables managers to explore different worker assignment strategies and their potential impact.

---

## Technologies
The technologies and frameworks used in this project include:

- **Programming Languages**: Java, TypeScript, JavaScript
- **Simulation Frameworks**: Monte Carlo Simulation Libraries
- **Frontend**: Vue.js with TypeScript
- **Backend**: Spring Boot with MySQL database, RESTful APIs
- **Machine Learning**: Smile library with RandomForest for predictive modeling
- **Build Tools**: Maven for backend, npm for frontend
- **Version Control**: Git with GitHub for collaboration
- **Testing**: JUnit for backend

---

## Project Scope
- **Development Period**: February - May (Bachelor's Project at NTNU Ålesund)
- **Student Engagement**: 3–4 months of work
- **Project Type**: Bachelor thesis project

---

## Installation

### Prerequisites
Ensure the following tools are installed on your system:
- **Node.js**  and npm
- **Java** (JDK 23 or later)
- **Maven** (for building the backend)
- **MySQL** (for the database)

---

### Backend Setup (Spring Boot)
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd <repository-folder>/Backend
   ```

2. Configure the database:
    - Create a MySQL database (e.g., `warehouse`).
    - Update the `application.properties` file in `src/main/resources` with your database credentials:
      ```properties
      spring.datasource.url=jdbc:mysql://localhost:3306/warehouse
      spring.datasource.username=<your-username>
      spring.datasource.password=<your-password>
      ```
### 3. CSV Setup
- Place the required CSV files in the directory:  
  `src/main/java/gruppe01/ntnu/no/warehouse/workflow/assigner/machinelearning/datasets`.  
  These files will be used by the application to load initial data for the machine learning model.

- Ensure the CSV files are properly formatted and include the following fields:  
  `id`, `distance_m`, `dpack_equivalent_amount`, `lines`, `weight_g`, `volume_ml`, `avg_height`, `picker`, `time_s`.

- Required CSV files:
    - **`synthetic_pickroutes_DRY_time.csv`**: Historical data for dry goods task durations.
    - **`synthetic_pickroutes_FREEZE_time.csv`**: Historical data for frozen goods task durations.
    - **`synthetic_pickroutes_FRUIT_time.csv`**: Historical data for fruit and produce task durations.

4. Build and run the backend:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

   The backend will start on `http://localhost:8080`.

---

### Frontend Setup (Vue.js)
1. Navigate to the frontend directory:
   ```bash
   cd <repository-folder>/Frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Configure the API endpoint:
    - Update the API base URL in the frontend configuration file (e.g., `src/config.js` or `.env`):
      ```javascript
      VUE_APP_API_BASE_URL=http://localhost:8080
      ```

4. Start the development server:
   ```bash
   npm run serve
   ```

   The frontend will start on `http://localhost:8081`.

---

### Running the Application
1. Ensure the backend and frontend are running.
2. Open the frontend in your browser at `http://localhost:8081`.
3. The application will communicate with the backend at `http://localhost:8080`.

---

### Optional: Build for Production
- **Frontend**:
  ```bash
  npm run build
  ```
  The production build will be available in the `dist` folder.

- **Backend**:
  Package the application as a JAR:
  ```bash
  mvn package
  ```
  The JAR file will be available in the `target` folder. Run it with:
  ```bash
  java -jar target/<your-app-name>.jar
  ```

---

## Contributors
- **Students**:
    - Adrian Faustino Johansen
    - Håkon Svensen Karlsen
    - Matti Kjellstadli
- **Academic Supervisor**: Di Wu
- **Industry Partner**: Solwr Software AS

## License
The licensing terms will be defined based on NTNU's standard agreement between companies and students. More details can be found [here](https://i.ntnu.no/wiki/-/wiki/Norsk/Standardavtale+mellom+bedrift+og+student).
