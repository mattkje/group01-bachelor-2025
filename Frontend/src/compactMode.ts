import { ref, provide, inject } from 'vue';

const compactModeSymbol = Symbol('compactMode');

export const provideCompactMode = () => {
    const compactMode = ref(false);
    const toggleCompactMode = () => {
        compactMode.value = !compactMode.value;
    };
    provide(compactModeSymbol, { compactMode, toggleCompactMode });
};

export const useCompactMode = () => {
    const compactModeState = inject(compactModeSymbol);
    if (!compactModeState) {
        throw new Error('useCompactMode must be used within a provider');
    }
    return compactModeState;
};