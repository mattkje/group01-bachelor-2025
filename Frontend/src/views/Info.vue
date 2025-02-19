<script setup lang="ts">
  import Toolbar from "../components/Toolbar.vue";
  </script>

  <template>
    <Toolbar title="Info" />
    <div class="container">
      <h1>About This Application</h1>
      <p>This application is developed as part of a bachelor thesis project. It utilizes Monte Carlo simulations to model and analyze complex systems and processes.</p>

      <h2>Monte Carlo Simulations</h2>
      <p>Monte Carlo simulations are a class of computational algorithms that rely on repeated random sampling to obtain numerical results. They are used to model the probability of different outcomes in a process that cannot easily be predicted due to the intervention of random variables.</p>
      <p>In this application, Monte Carlo simulations are used to:</p>
      <ul>
        <li>Analyze risk and uncertainty in prediction models.</li>
        <li>Optimize complex systems and processes.</li>
        <li>Perform sensitivity analysis to understand the impact of different variables.</li>
      </ul>

      <h2>Contributors</h2>
      <p>This project is developed by the following contributors:</p>
      <ul>
        <li>Håkon Svensen Karlsen - Project Lead and Developer</li>
        <li>Adrian Faustino Johansen - Backend Developer</li>
        <li>Matti Kjellstadli - Frontend Developer</li>
      </ul>
    </div>
  </template>

  <style scoped>
  .container {
    padding: 2rem;
  }

  h1, h2 {
    margin-top: 1rem;
  }

  ul {
    list-style-type: disc;
    margin-left: 2rem;
  }
  </style>