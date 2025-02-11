<script setup lang="ts">
    import { defineProps } from 'vue';
    import homeIcon from '../assets/icons/overview.svg';
    import searchIcon from '../assets/icons/overview.svg';
    import notificationsIcon from '../assets/icons/overview.svg';
    import profileIcon from '../assets/icons/overview.svg';

    const props = defineProps({
      title: {
        type: String,
        required: true
      }
    });
    </script>

    <template>
      <div class="toolbar">
        <div class="toolbar-title">{{ props.title }}</div>
        <div class="toolbar-item">
          <img src="@/assets/icons/overview.svg" alt="Home" class="icon" />
        </div>
        <div class="toolbar-item">
          <img src="@/assets/icons/overview.svg" alt="Search" class="icon" />
        </div>
        <div class="toolbar-item">
          <img src="@/assets/icons/overview.svg" alt="Notifications" class="icon" />
        </div>
        <div class="toolbar-item">
          <img src="@/assets/icons/overview.svg" alt="Profile" class="icon" />
        </div>
      </div>
    </template>

    <style scoped>
    .toolbar {
      display: flex;
      justify-content: flex-end;
      background-color: #ffffff;
      padding: 1rem;
      border-bottom: 1px solid #e0e0e0;
      position: relative;
    }

    .toolbar-title {
      position: absolute;
      left: 1rem;
      font-size: 1.5rem;
      font-weight: bold;
    }

    .toolbar-item {
      margin-left: 1rem;
    }

    .icon {
      width: 24px;
      height: 24px;
      cursor: pointer;
    }
    </style>