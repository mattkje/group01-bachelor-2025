<script setup lang="ts">

</script>

<template>
  <div class="container">
    <div class="logo">
      <img src="@/assets/icons/wws.svg" alt="Logo" class="logo-icon"/>
      <span class="logo-text">Warehouse&nbsp;Workflow<br><span class="regular-font">Simulator™</span></span>
    </div>
    <p>This application is developed as part of a bachelor thesis project. It utilizes Monte Carlo simulations to model
      and analyze complex systems and processes.</p>

    <h2>Monte Carlo Simulations</h2>
    <p>Monte Carlo simulations are a class of computational algorithms that rely on repeated random sampling to obtain
      numerical results. They are used to model the probability of different outcomes in a process that cannot easily be
      predicted due to the intervention of random variables.</p>
    <p>In this application, Monte Carlo simulations are used to:</p>
    <ul>
      <li>Analyze risk and uncertainty in prediction models.</li>
      <li>Optimize complex systems and processes.</li>
      <li>Perform sensitivity analysis to understand the impact of different variables.</li>
    </ul>

    <h2>Contributors</h2>
    <p>This project is developed by the following contributors:</p>
    <ul class="contributors-list">
      <li>
        <img src="@/assets/images/ntnu.png" alt="NTNU" class="contributor-image"/>
       <span>NTNU - Academic Partner</span>
      </li>
      <li>
        <img src="@/assets/images/solwr.png" alt="Solwr" class="contributor-image"/>
        <span>Solwr Robotics - Project Partner and Mentor</span>
      </li>
      <li>
        <img src="../assets/images/haakon.png" alt="Håkon Svensen Karlsen" class="contributor-image"/>
        <span>Håkon Svensen Karlsen - Project Lead and Developer</span>
      </li>
      <li>
        <img src="@/assets/images/adrian.png" alt="Adrian Faustino Johansen" class="contributor-image"/>
        <span>Adrian Faustino Johansen - Backend Developer</span>
      </li>
      <li>
        <img src="@/assets/images/matti.png" alt="Matti Kjellstadli" class="contributor-image"/>
        <span>Matti Kjellstadli - Frontend Developer</span>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.container {
  height: 100%;
  padding: 2rem;
  border-radius: 10px;
}

.logo {
  display: flex;
  align-items: center;
  margin-bottom: 2rem;
}

.logo-icon {
  width: 110px;
  margin-right: 0.5rem;
}

.logo-text {
  padding-top: 0.3rem;
  font-size: 2rem;
  line-height: 2rem;
  font-weight: bold;
  position: relative;
}

.logo-text::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 0;
  width: 50px;
  height: 5px;
  background-color: #e74c3c;
  border-radius: 2px;
}

h2 {
  margin-top: 1rem;
  position: relative;
  padding-bottom: 0.5rem;
}

h2::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 50px;
  height: 3px;
  background-color: #e74c3c;
  border-radius: 2px;
}

ul {
  list-style-type: disc;
  margin-left: 2rem;
}

.contributors-list {
  list-style: none;
  padding: 0;
}

.contributors-list li {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
}

.contributor-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 50%;
  margin-right: 1rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>