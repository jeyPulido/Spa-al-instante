<template>
  <div class="fullscreen error-bg text-white q-pa-md flex flex-center">
    <div class="error-card text-center q-pa-xl">
      <q-icon name="spa" size="96px" color="white" />
      <div class="error-code">404</div>

      <div class="text-h4 text-weight-bold q-mt-md">Página no encontrada</div>

      <div class="text-subtitle1 q-mt-sm text-grey-3">
        Parece que esta ruta se perdió entre aromas y relajación 🌿
      </div>

      <q-separator dark class="q-my-lg" />

      <div class="text-body1 q-mb-lg">
        No te preocupes, en <strong>Spa al Instante</strong> siempre hay un camino de regreso al
        bienestar.
      </div>

      <div class="row justify-center q-gutter-md">
        <q-btn color="primary" icon="home" label="Volver al inicio" to="/" unelevated no-caps />

        <q-btn
          color="white"
          text-color="primary"
          outline
          icon="spa"
          label="Ver servicios"
          to="/#servicios"
          no-caps
        />
      </div>

      <div class="q-mt-xl text-caption text-grey-4">
        © 2025 Spa al Instante · Relajación • Bienestar • Armonía
      </div>
    </div>
  </div>
</template>

<script setup></script>

<style scoped>
.error-bg {
  background: linear-gradient(135deg, #2e7d6f, #4db6ac);
}

.error-card {
  max-width: 520px;
  width: 100%;
  border-radius: 20px;
  background: rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(6px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.25);
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  line-height: 1;
  margin-top: 10px;
  opacity: 0.85;
}
</style>
