<template>
  <q-page class="bg-secondary">
    <q-page-sticky position="bottom-right" :offset="[18, 18]" class="carrito-flotante">
      <q-btn fab color="primary" icon="shopping_cart" @click="irCarrito">
        <q-badge
          v-if="carrito.length > 0"
          color="red"
          floating
          transparent
          :label="carrito.length"
        />
      </q-btn>
    </q-page-sticky>

    <div class="hero-section flex flex-center column text-white">
      <p class="text-h3 text-weight-bold q-mb-sm">Spa al Instante</p>
      <p class="text-subtitle1 q-mb-lg">Relájate, renueva tu energía y déjate consentir.</p>
    </div>
    <div id="servicios" class="section">
      <p class="section-title">Nuestros Servicios</p>
      <div class="servicios-grid">
        <q-card v-for="s in servicios" :key="s.id" class="my-card">
          <q-card-section class="text-center">
            <q-icon name="spa" size="50px" color="primary" />
            <p class="text-h6 q-mt-md">{{ s.nombre }}</p>
            <p class="text-body2">{{ s.descripcion }}</p>
            <p class="text-subtitle2 text-weight-bold q-mt-sm">$ {{ s.precio }} MXN</p>
            <q-btn
              color="primary"
              label="Agregar al carrito"
              class="q-mt-md"
              @click="agregarCarrito(s)"
            />
          </q-card-section>
        </q-card>
      </div>
    </div>
    <q-footer class="bg-dark text-white text-center q-pa-md"> © 2025 Spa al Instante </q-footer>
  </q-page>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import axios from 'axios'
import { useRouter } from 'vue-router'
const $q = useQuasar()
const router = useRouter()
const servicios = ref([])
const carrito = ref([])
async function cargarServicios() {
  const res = await axios.get('http://localhost:8082/api/servicios')
  servicios.value = res.data
}
function cargarCarritoLocal() {
  const data = localStorage.getItem('carrito')
  carrito.value = data ? JSON.parse(data) : []
}
function guardarCarrito() {
  localStorage.setItem('carrito', JSON.stringify(carrito.value))
}
function agregarCarrito(servicio) {
  carrito.value.push(servicio)
  guardarCarrito()
  $q.notify({
    type: 'positive',
    message: `${servicio.nombre} agregado al carrito`,
  })
}
function irCarrito() {
  router.push('/carrito')
}
onMounted(() => {
  cargarServicios()
  cargarCarritoLocal()
})
</script>
<style scoped>
.hero-section {
  height: 70vh;
  background: url('/statics/images.jpg') center/cover no-repeat;
}
.section {
  padding: 40px 20px;
}
.section-title {
  text-align: center;
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 25px;
}
.servicios-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}
.my-card {
  border-radius: 12px;
}
.carrito-flotante {
  z-index: 9999;
}
</style>
